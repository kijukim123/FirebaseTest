package com.example.pda;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.m3.sdk.scannerlib.Barcode;
import com.m3.sdk.scannerlib.BarcodeListener;
import com.m3.sdk.scannerlib.BarcodeManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class PDA_AND_LOOKUP_L extends BaseActivity {

    String Email = ((PDA_AND_MAIN) PDA_AND_MAIN.context).Email;  //사용자 아이디
    String Language = ((PDA_AND_LOGIN) PDA_AND_LOGIN.context).language;  //사용자 언어
    String sbarcode;
    TextView barcode,notice,scannum;
    EditText hidden;
    Button btn_clear;

    Statement st = connectDB();
    ResultSet rs;
    Statement n_st = connectDB();
    ResultSet n_rs;

    //리스트뷰, 리스트뷰 어뎁터 초기화
    ListView listview;
    ListAdapter_Lookup_Rack listViewAdapter;

    // lib
    Barcode mBarcode = null;
    BarcodeListener mListener = null;
    BarcodeManager mManager = null;
    Barcode.Symbology mSymbology = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pda_and_lookup_l);

        listViewAdapter = new ListAdapter_Lookup_Rack();
        mBarcode = new Barcode(this);
        mManager = new BarcodeManager(this);
        mSymbology = mBarcode.getSymbologyInstance();
        main();


        /************바코드 스캔 *************/
        mListener=new BarcodeListener() {
            @Override
            public void onBarcode(String s) {
                sbarcode = s.trim();
                barcode.setTextColor(Color.parseColor("#7A7A7A"));
                //barcode.setText(sbarcode);
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

        //초기화 버튼
        btn_clear.setOnClickListener(v -> clear());




    }

    public void main(){
        barcode = findViewById(R.id.barcode);
        hidden = findViewById(R.id.hidden);
        notice = findViewById(R.id.notice);
        scannum = findViewById(R.id.scannum);
        listview = findViewById(R.id.listview);
        btn_clear = findViewById(R.id.btn_clear);

        /*********뒤로가기, 로그아웃 **********/
        ImageButton back = findViewById(R.id.back);
        ImageButton logout = findViewById(R.id.logout);
        mainbtn(back, logout, Email);
    }

    /******************* 초기화 버튼 **********************/
    public void clear(){
        main();
        barcode.setText("");
        notice.setText("위치명 을(를) 스캔하세요");
        notice.setTextColor(Color.parseColor("#7A7A7A"));
        listViewAdapter.clearItem();
        listViewAdapter.notifyDataSetChanged();
        scancnt();


    }

    public void scancnt(){
        main();
        int num = listViewAdapter.getCount();
        String snum = num + " Records To Display ...";
        scannum.setText(snum);
    }

    /************바코드 스캔 *************/
    public void scan(){
        main();

        try{
            n_rs = n_st.executeQuery("select RACKNM from TB_WM_RACK_MNGR_L where RACKCD = '" +sbarcode + "'");

            if(!n_rs.next()) {
                barcode.setText("");
                notice.setTextColor(Color.parseColor("#FFFF0000"));
                notice.setText("존재하지 않는 바코드입니다.");

                hidden.setText("");
                hidden.setFocusable(true);
                hidden.requestFocus();
            }
            else {
                barcode.setText(n_rs.getString(1));
                String query = "EXEC dbo.SP_PDA_WM00060_INQUERY_RACK '" + sbarcode + "' ,'" + Language + "'";
                rs= st.executeQuery(query);
                listViewAdapter.clearItem();
                while (rs.next()) {
                    listViewAdapter.addItem(
                            rs.getString(3),
                            qty(rs.getString(4)).toString(),
                            rs.getString(1));
                    //리스트뷰에 어뎁터 set
                    listview.setAdapter(listViewAdapter);
                }
                scancnt();

                notice.setTextColor(Color.parseColor("#7A7A7A"));
                notice.setText("완료되었습니다.");
            }
        }catch(SQLException e){}
    }


    /********* 팝업 종료 후 호출되는 코드  *******/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 500){
            PDA_AND_MAIN MA = (PDA_AND_MAIN)PDA_AND_MAIN._Main_Activity;
            MA.finish();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mManager.dismiss();
    }
}