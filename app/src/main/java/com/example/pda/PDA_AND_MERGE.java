package com.example.pda;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.m3.sdk.scannerlib.Barcode;
import com.m3.sdk.scannerlib.BarcodeListener;
import com.m3.sdk.scannerlib.BarcodeManager;


public class PDA_AND_MERGE extends BaseActivity {

    String prebarcode, pre_st_barcode, pre_mer_barcode;
    String Email = ((PDA_AND_MAIN) PDA_AND_MAIN.context).Email;  //사용자 아이디
    String Language = ((PDA_AND_LOGIN) PDA_AND_LOGIN.context).language;  //사용자 언어

    //기준 바코드
    Statement st_st = connectDB();
    ResultSet st_rs;
    ResultSet st_rs2;
    CallableStatement cs = null;
    String pnum;

    //레거시 확인
    Statement l_st = connectDB();
    ResultSet l_rs;

    //병합 바코드
    ResultSet mer_rs2;
    CallableStatement cs2 = null;

    //신규 바코드
    Statement new_st = connectDB();
    ResultSet new_rs;

    // lib
    Barcode mBarcode = null;
    BarcodeListener mListener = null;
    BarcodeManager mManager = null;
    Barcode.Symbology mSymbology = null;

    TextView st_barcode, st_name, st_location, st_amount, mer_barcode, mer_location, mer_amount, notice;
    EditText stb_hidden, merb_hidden;
    Spinner use_spinner;
    Button btn_clear, btn_merge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pda_and_merge);

        main();
        mBarcode = new Barcode(this);
        mManager = new BarcodeManager(this);
        mSymbology = mBarcode.getSymbologyInstance();

        mListener = new BarcodeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBarcode(String s) {
                prebarcode = s.trim();

                scan();

            }

            @Override
            public void onBarcode(String s, String s1) {
            }

            @Override
            public void onGetSymbology(int i, int i1) {
            }
        };
        mManager.addListener(mListener);

        spinner();
        btn_clear.setOnClickListener(v -> clear());
        btn_merge.setOnClickListener(v -> merge());


    }

    /********* 팝업 종료 후 호출되는 코드  ***********/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        main();

        if (resultCode == 0) // yes 버튼 클릭 한 경우
        {

            if (use_spinner.getSelectedItem().toString().equals("미사용")) {
                try {
                    new_rs = new_st.executeQuery("EXEC dbo.SP_PDA_WM00040_SAVE '" + pre_st_barcode + "','" +
                            pre_mer_barcode + "','N',' " + Email + "','" + Language + "'");
                    notice.setTextColor(Color.parseColor("#808080"));

                } catch (SQLException e) {
                }

            } else if (use_spinner.getSelectedItem().toString().equals("사용")) {
                try {
                    new_rs = new_st.executeQuery("EXEC dbo.SP_PDA_WM00040_SAVE '" + pre_st_barcode + "','" +
                            pre_mer_barcode + "','Y',' " + Email + "','" + Language + "'");
                    notice.setTextColor(Color.parseColor("#808080"));


                    stb_hidden.setFocusable(true);
                    stb_hidden.requestFocus();

                } catch (SQLException e) {
                }
            }

            clear();
            notice.setText("저장되었습니다.");
        }

        if (resultCode == 500){
            PDA_AND_MAIN MA = (PDA_AND_MAIN)PDA_AND_MAIN._Main_Activity;
            MA.finish();
            finish();
        }

    }

    //main
    public void main() {

        st_barcode = (TextView) findViewById(R.id.st_barcode);
        stb_hidden = (EditText) findViewById(R.id.stb_hidden);
        mer_barcode = (TextView) findViewById(R.id.mer_barcode);
        merb_hidden = (EditText) findViewById(R.id.merb_hidden);
        btn_clear = (Button) findViewById(R.id.btn_clear);
        btn_merge = (Button) findViewById(R.id.btn_merge);
        st_name = (TextView) findViewById(R.id.st_name);
        st_location = (TextView) findViewById(R.id.st_location);
        st_amount = (TextView) findViewById(R.id.st_amount);
        mer_location = (TextView) findViewById(R.id.mer_location);
        mer_amount = (TextView) findViewById(R.id.mer_amount);
        notice = (TextView) findViewById(R.id.notice);
        use_spinner = (Spinner) findViewById(R.id.use_spinner);

        /*********뒤로가기, 로그아웃 **********/
        ImageButton back = findViewById(R.id.back);
        ImageButton logout = findViewById(R.id.logout);
        mainbtn(back, logout, Email);
    }

    //스캔
    public void scan() {
        try {
            if (st_barcode.getText().equals("")) {
                pre_st_barcode = prebarcode;
                if (!pre_st_barcode.equals("")) {   //기준 바코드

                    st_rs = st_st.executeQuery("SELECT PARTNO FROM TB_WM_BCOD WHERE BARCODE='" + pre_st_barcode + "'");

                    if (st_rs.next()) {
                        do {
                            pnum = st_rs.getString(1);  //품번
                        } while (st_rs.next());
                    } else {
                        l_rs = l_st.executeQuery("SELECT PARTNO FROM TB_WM_BCOD A JOIN TB_BM_BCOD_LEGACY B ON A.BARCODE = B.BARCODE WHERE B.LEGACY_BARCODE = '" + pre_st_barcode + "'");
                        if(!l_rs.next()){
                            pnum = null;
                        }
                        else{
                            pnum = l_rs.getString(1);  //legacy 인 경우 품번
                        }
                    }

                    String query = "EXEC dbo.SP_PDA_WM00040_INQUERY_BARCODE '" + pre_st_barcode + "', 'null ','" + pnum + "' ,'" + Language + "'";
                    cs = multi_connectDB(query);
                    st_rs2 = cs.executeQuery();
                    while (st_rs2.next()) {
                        if (!st_rs2.getString(1).equals("")) { //바코드 사용 불가능
                            notice.setTextColor(Color.parseColor("#FFFF0000"));
                            notice.setText((st_rs2.getString(1)));
                            stb_hidden.setFocusable(true);
                            stb_hidden.requestFocus();
                            stb_hidden.setText("");

                        } else if (st_rs2.getString(1).equals("")) {  //바코드 사용 가능

                            //st_barcode.setText(pre_st_barcode);
                            st_barcode.setTextColor(Color.parseColor("#7A7A7A"));
                            notice.setText("바코드 을(를) 스캔하세요.");
                            notice.setTextColor(Color.parseColor("#7A7A7A"));


                            if (cs.getMoreResults()) {
                                st_rs2 = cs.getResultSet();
                                while (st_rs2.next()) {
                                    st_barcode.setText(st_rs2.getString(1)); //바코드
                                    st_name.setText(st_rs2.getString(3));  //품명
                                    st_amount.setText(qty(st_rs2.getString(4)).toString());  //수량
                                    st_location.setText(st_rs2.getString(5));  //위치
                                    stb_hidden.setFocusable(true);
                                    stb_hidden.requestFocus();
                                    stb_hidden.setText("");
                                }
                            }
                        }
                    }
                }
            } else { //병합 바코드 스캔 차례
                pre_mer_barcode = prebarcode;
                if (!pre_mer_barcode.equals("")) {
                    String query1 = "EXEC dbo.SP_PDA_WM00040_INQUERY_BARCODE '" + pre_mer_barcode + "', '" + pre_st_barcode + "','" + pnum + "' ,'" + Language + "'";
                    cs2 = multi_connectDB(query1);
                    mer_rs2 = cs2.executeQuery();

                    while (mer_rs2.next()) {
                        if (!mer_rs2.getString(1).equals("")) { //바코드 사용 불가능
                            notice.setTextColor(Color.parseColor("#FFFF0000"));
                            notice.setText(mer_rs2.getString(1));
                            mer_barcode.setText("");
                            mer_location.setText("");
                            mer_amount.setText("");
                            stb_hidden.setText("");
                            stb_hidden.requestFocus();
                            stb_hidden.setText("");

                        } else if (mer_rs2.getString(1).equals("")) { //바코드 사용 가능
                            mer_barcode.setText(pre_mer_barcode);
                            mer_barcode.setTextColor(Color.parseColor("#808080"));

                            if (cs2.getMoreResults()) {
                                mer_rs2 = cs2.getResultSet();
                                while (mer_rs2.next()) {
                                    notice.setTextColor(Color.parseColor("#808080"));
                                    notice.setText("");
                                    mer_amount.setText(qty(mer_rs2.getString(4)).toString());  //수량
                                    mer_location.setText(mer_rs2.getString(5));  //위치
                                    stb_hidden.requestFocus();
                                    stb_hidden.setText("");
                                }
                            }
                        }
                    }
                }
            }

        } catch (SQLException e) {
        }

    }

    //스피너
    public void spinner() {
        //스피너
        Spinner use_spinner = (Spinner) findViewById(R.id.use_spinner);
        String[] items = {"미사용", "사용"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_dropdown_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        use_spinner.setAdapter(adapter);
    }

    //초기화 버튼
    public void clear() {

        main();

        stb_hidden.setText("");
        merb_hidden.setText("");

        st_barcode.setText("");
        st_barcode.setTextColor(Color.parseColor("#7A7A7A"));
        st_name.setText("");
        st_location.setText("");
        st_amount.setText("");

        mer_barcode.setText("");
        mer_location.setText("");
        mer_amount.setText("");

        notice.setText("바코드 을(를) 스캔하세요.");
        notice.setTextColor(Color.parseColor("#7A7A7A"));

        stb_hidden.setFocusable(true);
        stb_hidden.requestFocus();

        use_spinner.setSelection(0);
    }

    //병합 버튼
    public void merge() {
        main();
        if (mer_amount.getText().equals("")) {
            moveActivity(Confirm.class,"바코드 을(를) 스캔하세요.");
        } else {
            Intent intent = new Intent(PDA_AND_MERGE.this, PDA_AND_MERGE_POPUP.class);
            startActivityForResult(intent, 0);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mManager.dismiss();
    }

}