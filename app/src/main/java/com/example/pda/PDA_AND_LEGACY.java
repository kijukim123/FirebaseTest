package com.example.pda;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import com.m3.sdk.scannerlib.Barcode;
import com.m3.sdk.scannerlib.BarcodeListener;
import com.m3.sdk.scannerlib.BarcodeManager;


public class PDA_AND_LEGACY extends BaseActivity {
    InputMethodManager inputMethodManager;
    String Email = ((PDA_AND_MAIN) PDA_AND_MAIN.context).Email;  //사용자 아이디
    String Language = ((PDA_AND_LOGIN) PDA_AND_LOGIN.context).language;  //사용자 언어
    private String sregacy, spname,pnum, a=null,current;

    private EditText hidden,amount ;
    private TextView pname,regacy,notice,scannum ;
    Button btn_store,btn_clear ;
    ImageButton search ;

    Statement r_st = connectDB();
    ResultSet r_rs;
    Statement s_st=connectDB();
    ResultSet s_rs;

    //리스트뷰, 리스트뷰 어뎁터 초기화
    ListView listview;
    ListAdapter_Legacy listViewAdapter;

    private String StrPreInput;


    // lib
    Barcode mBarcode = null;
    BarcodeListener mListener = null;
    BarcodeManager mManager = null;
    Barcode.Symbology mSymbology = null;

    String Log_menuID= "PDA_AND_LEGACY" ;   // 로그 기록 용 화면 이름
    String Log_action = "PUBLISH_LEGACY" ;      // 로그 기록 용 action 이름

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pda_and_legacy);
        main();
        listViewAdapter = new ListAdapter_Legacy();
        amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {current= amount.getText().toString();}
            @Override
            public void afterTextChanged(Editable s) {}
        });
        /********바코드 인식 ***********/

        mBarcode = new Barcode(this);
        mManager = new BarcodeManager(this);
        mSymbology = mBarcode.getSymbologyInstance();


        mListener = new BarcodeListener() {
            @Override
            public void onGetSymbology(int nSymbol, int nVal) {}

            @Override
            public void onBarcode(String strBarcode) {
                if(amount.isFocused()){
                    hidden.setFocusable(true);
                    hidden.requestFocus();
                    if(amount.getText().toString().equals("0.0") || amount.getText().toString().equals("0")){
                        amount.setText("0");
                    }
                    else{
                        amount.setText(qty(current));
                    }
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(amount.getWindowToken(), 0);
                }

                StrPreInput = strBarcode.trim();
                scan();
            }

            @Override
            public void onBarcode(String barcode, String codeType) {
            }
        };
        mManager.addListener(mListener);

        amount.setOnKeyListener((v, keyCode, event) -> {
            switch (keyCode) {
                case KeyEvent.KEYCODE_ENTER:
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(amount.getWindowToken(), 0);
                    if(amount.getText().toString().equals("")){
                        amount.setText("0");
                    }
                    else{
                        double d = Double.parseDouble(amount.getText().toString());
                        amount.setText(qty(String.valueOf(d)));
                    }
                    hidden.setFocusable(true);
                    hidden.requestFocus();

            }
                    return false;
            });

        search.setOnClickListener(v -> search());
        btn_store.setOnClickListener(v -> store());
        btn_clear.setOnClickListener(v -> clear());



    }


    /********* 팝업 종료 후 호출되는 코드  ***********/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        main();

        if (resultCode == 0) // 액티비티가 정상적으로 종료되었을 경우
        {
            spname = data.getExtras().getString("name");
            pnum = data.getExtras().getString("num");
            sregacy = data.getExtras().getString("regacy");
            System.out.println("*********************************:정상종료 " + spname);
            System.out.println("*********************************:정상종료 " + pnum);
            System.out.println("*********************************:정상종료 " + sregacy);
            pname.setText(spname);
        }
        if (resultCode == 500){
            PDA_AND_MAIN MA = (PDA_AND_MAIN)PDA_AND_MAIN._Main_Activity;
            MA.finish();
            finish();
        }
    }

    //main
    public void main(){
        notice = findViewById(R.id.notice);
        hidden = findViewById(R.id.hidden);
        pname = findViewById(R.id.pname);
        regacy = findViewById(R.id.regacy);
        btn_store = findViewById(R.id.btn_store);
        btn_clear = findViewById(R.id.btn_clear);
        search = findViewById(R.id.search);
        listview = findViewById(R.id.listview);
        amount = findViewById(R.id.amount);
        scannum = findViewById(R.id.scannum);

        /*********뒤로가기, 로그아웃 **********/
        ImageButton back = findViewById(R.id.back);
        ImageButton logout = findViewById(R.id.logout);
        mainbtn(back, logout, Email);

    }

    //저장 버튼
    public void store(){
        main();

        if(regacy.getText().equals("")){
            moveActivity(Confirm.class,"바코드 을(를) 스캔하세요.");
        }

        else if(pname.getText().toString().equals("")){
            moveActivity(Confirm.class,"품번을 입력해주세요.");
        }

        else if(amount.getText().toString().equals("0") || amount.getText().toString().equals("0.0") ){
            moveActivity(Confirm.class,"수량을 입력해주세요.");
        }
        else {
            a = amount.getText().toString();
            String r = regacy.getText().toString();

            /**** 저장 기능 *******/
            try {
                s_rs = s_st.executeQuery("EXEC dbo.SP_PDA_WM00110_SAVE '" + "1001" + "','" + r + "','" + pnum + "','" + a + "','" + Email + "','" + Language + "'");
                while (s_rs.next()) {
                    notice.setText(s_rs.getString(1));
                    listViewAdapter.addItem(
                            s_rs.getString(2),
                            s_rs.getString(3),
                            spname, a);
                    //리스트뷰에 어뎁터 set
                    listview.setAdapter(listViewAdapter);

                    hidden.setFocusable(true);
                    hidden.requestFocus();
                    regacy.setText("");
                    pname.setText("");
                    amount.setText("");

                    regLog(Log_menuID, Email, Log_action);

                    scancnt();
                }
            } catch (SQLException e) {
            }
        }

    }

    //초기화
    public void clear(){

        main();

        hidden.setFocusable(true);
        hidden.requestFocus();
        regacy.setText("");
        pname.setText("");
        amount.setText("0");
        notice.setTextColor(Color.parseColor("#7A7A7A"));
        notice.setText("바코드 을(를) 스캔하세요");

        listViewAdapter.clearItem();
        listViewAdapter.notifyDataSetChanged();

        scancnt();
    }

    //품목 검색
    public void search(){
        Intent intent = new Intent(PDA_AND_LEGACY.this, PDA_AND_LEGACY_POPUP.class);
        startActivityForResult(intent,0);
    }

    //바코드 인식
    public void scan(){
        main();
        try {
            r_rs = r_st.executeQuery("EXEC dbo.SP_PDA_WM00110_INQUERY '" + StrPreInput + "','" + Language + "'");  //A220715000000005
            while (r_rs.next()) {
                if (r_rs.getString(1).equals("F")) { //사용할 수 없는 레거시 바코드

                    notice.setText(r_rs.getString(2));
                    notice.setTextColor(Color.parseColor("#FFFF0000"));

                    hidden.setText("");
                    hidden.setFocusable(true);
                    regacy.requestFocus();


                } else if (r_rs.getString(1).equals("T")) {  //사용 가능한 바코드
                    regacy.setText(StrPreInput);
                    notice.setTextColor(Color.parseColor("#808080"));
                    notice.setText(r_rs.getString(2));
                    hidden.setText("");
                    regacy.setFocusable(true);
                    regacy.requestFocus();
                }
            }
        } catch (SQLException e) {
        }
    }

    //바코드 추가 개수
    public void scancnt(){
        scannum = findViewById(R.id.scannum);
        main();
        int num = listViewAdapter.getCount();
        String snum = num + " Records To Display ...";
        scannum.setText(snum);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mManager.dismiss();
    }
}



