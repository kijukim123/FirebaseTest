package com.example.pda;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.m3.sdk.scannerlib.Barcode;
import com.m3.sdk.scannerlib.BarcodeListener;
import com.m3.sdk.scannerlib.BarcodeManager;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class PDA_AND_DIVISION extends BaseActivity {

    String Email = ((PDA_AND_MAIN) PDA_AND_MAIN.context).Email;  //사용자 아이디
    String Language = ((PDA_AND_LOGIN) PDA_AND_LOGIN.context).language;  //사용자 언어
    private String sbarcode,current;

    // lib
    Barcode mBarcode = null;
    BarcodeListener mListener = null;
    BarcodeManager mManager = null;
    Barcode.Symbology mSymbology = null;

    //UI
    private TextView barcode,pnum,pname,location,amount,notice;
    EditText hidden,div_amount;
    Spinner use_spinner;
    Button btn_clear,btn_div;

    CallableStatement div_cs = null;
    ResultSet div_rs = null;
    //분할
    Statement div_st = connectDB();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pda_and_division);

        main();

        div_amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {current= div_amount.getText().toString();}
            @Override
            public void afterTextChanged(Editable s) {}
        });

        mBarcode = new Barcode(this);
        mManager = new BarcodeManager(this);
        mSymbology = mBarcode.getSymbologyInstance();


        notice.setTextColor(Color.parseColor("#808080"));
        notice.setText("바코드 을(를) 스캔하세요. ");


        mListener = new BarcodeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBarcode(String s) {
                if(div_amount.isFocused()){
                    hidden.setFocusable(true);
                    hidden.requestFocus();
                    if(div_amount.getText().toString().equals("") || div_amount.getText().toString().equals("0")){
                        div_amount.setText("0");
                    }
                    else{
                        div_amount.setText(qty(current));
                    }
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(div_amount.getWindowToken(), 0);
                }
                sbarcode = s.trim();
                scan();
            }
            @Override
            public void onBarcode(String s, String s1) {}
            @Override
            public void onGetSymbology(int i, int i1) {}
        };
        mManager.addListener(mListener);
        div_amount.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(div_amount.getWindowToken(), 0);
                if(div_amount.getText().toString().equals("")){
                    div_amount.setText("0");
                }
                else{
                    double d = Double.parseDouble(div_amount.getText().toString());
                    //d = (Math.round(d*10000)/10000.0);
                    div_amount.setText(qty(String.valueOf(d)));
                }
                hidden.setFocusable(true);
                hidden.requestFocus();

            }
            return false;
        });
        spinner();
        btn_clear.setOnClickListener(v -> clear());
        btn_div.setOnClickListener(v -> div());


    }

    /************팝업 종료 후 호출 *************/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        main();
        String div_a = div_amount.getText().toString();
        String str_spinner=null;

        if (use_spinner.getSelectedItem().toString().equals("예")) {
            str_spinner = "Y";
        }
        else if (use_spinner.getSelectedItem().toString().equals("아니요")){
            str_spinner = "N";
        }

        if (resultCode == 0) // yes 버튼 클릭 한 경우
        {
            try{
                div_st.executeQuery("dbo.SP_PDA_WM00050_SAVE '"+ sbarcode + "', '" + div_a +"' , '" + str_spinner + "' , '" + Email + "' , '" + Language + "'");
                clear();
                notice.setText("저장되었습니다.");

            }catch (SQLException e){}
        }

        if (resultCode == 500){
            PDA_AND_MAIN MA = (PDA_AND_MAIN)PDA_AND_MAIN._Main_Activity;
            MA.finish();
            finish();
        }

    }

    //main
    public void main(){
        /*********뒤로가기, 로그아웃 **********/
        ImageButton back = findViewById(R.id.back);
        ImageButton logout = findViewById(R.id.logout);
        mainbtn(back, logout, Email);

        notice = (TextView) findViewById(R.id.notice);
        barcode= (TextView) findViewById(R.id.barcode);
        btn_clear = findViewById(R.id.btn_clear);
        btn_div = findViewById(R.id.btn_div);
        pnum = (TextView) findViewById(R.id.pnum);
        pname = (TextView) findViewById(R.id.pname);
        location = (TextView) findViewById(R.id.location);
        amount = (TextView) findViewById(R.id.amount);
        hidden = (EditText) findViewById(R.id.hidden);
        notice = (TextView) findViewById(R.id.notice);
        barcode= (TextView) findViewById(R.id.barcode);
        use_spinner = findViewById(R.id.use_spinner);
        div_amount = (EditText) findViewById(R.id.div_amount);


    }

    //바코드 스캔
    public void scan(){
        main();
        try{
            div_cs = multi_connectDB("dbo.SP_PDA_WM00050_INQUERY_BARCODE '"+ sbarcode + "' ,'" + Language + "'");
            div_rs = div_cs.executeQuery();

            while(div_rs.next()){
                if(div_rs.getString(1).equals("")){   //사용할 수 있는 바코드
                    if(div_cs.getMoreResults()) {
                        div_rs = div_cs.getResultSet();
                        while(div_rs.next()) {
                            //값이 있으면 비우고 넣기
                            pnum.setText("");
                            pname.setText("");
                            location.setText("");
                            amount.setText("");

                            barcode.setText(div_rs.getString(1)); //바코드
                            pnum.setText(div_rs.getString(2)); // 품번
                            pname.setText(div_rs.getString(3)); //품명
                            location.setText(div_rs.getString(5)); //위치명
                            amount.setText(qty(div_rs.getString(4))); //수량
                        }
                    }
                    hidden.setText("");
                    hidden.setFocusable(true);
                    hidden.requestFocus();

                    notice.setTextColor(Color.parseColor("#808080"));
                    notice.setText("분할 수량을 입력하세요.");
                }
                else {   //사용할 수 없는 바코드
                    pnum.setText("");
                    pname.setText("");
                    location.setText("");
                    amount.setText("");
                    notice.setText(div_rs.getString(1));
                    notice.setTextColor(Color.parseColor("#FFFF0000"));

                    hidden.setText("");
                    hidden.setFocusable(true);
                    hidden.requestFocus();

                }
            }

        }catch (SQLException e){}
    }

    //spinner
    public void spinner(){
        use_spinner = findViewById(R.id.use_spinner);
        String[] items = {"아니요", "예"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_dropdown_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        use_spinner.setAdapter(adapter);
    }

    //초기화
    public void clear(){

        main();

        hidden.setText("");
        barcode.setText("");
        pnum.setText("");
        pname.setText("");
        location.setText("");
        amount.setText("");
        div_amount.setText("");

        barcode.setTextColor(Color.parseColor("#7A7A7A"));
        notice.setTextColor(Color.parseColor("#7A7A7A"));
        notice.setText("바코드 을(를) 스캔하세요.");

        hidden.setFocusable(true);
        hidden.requestFocus();

        use_spinner.setSelection(0);
    }

    //분할
    public void div() {
        main();

        if(barcode.getText().equals("")){
            moveActivity(Confirm.class,"바코드 을(를) 스캔하세요.");
        }

        else if(div_amount.getText().toString().equals("") || div_amount.getText().toString().equals("0")){
            moveActivity(Confirm.class,"분할 수량을 입력하세요.");
        }

        else if(!div_amount.getText().toString().equals("")){
            double d = Double.parseDouble(div_amount.getText().toString());
            if(d>= Double.parseDouble(amount.getText().toString())) {
                moveActivity(Confirm.class,"분할수량은(는) 수량 보다 작아야 합니다");
            }
            else{
                Intent intent = new Intent(PDA_AND_DIVISION.this, PDA_AND_DIVISION_POPUP.class);
                startActivityForResult(intent, 0);
            }
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mManager.dismiss();
    }
}