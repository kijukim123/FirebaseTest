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

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class PDA_AND_LOOKUP_B extends BaseActivity {

    // lib
    Barcode mBarcode = null;
    BarcodeListener mListener = null;
    BarcodeManager mManager = null;
    Barcode.Symbology mSymbology = null;

    //UI
    TextView barcode,notice,pname,location,amount,status,scannum;
    EditText hidden;
    Button btn_clear;
    String Email = ((PDA_AND_MAIN) PDA_AND_MAIN.context).Email;  //사용자 아이디
    String Language = ((PDA_AND_LOGIN) PDA_AND_LOGIN.context).language;  //사용자 언어
    String pre_barcode;

    CallableStatement cs = null;
    ResultSet rs = null;

    //리스트뷰, 리스트뷰 어뎁터 초기화
    ListView listview;
    ListAdapter_Lookup_Barcode listViewAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pda_and_lookup_b);

        main();
        listViewAdapter = new ListAdapter_Lookup_Barcode();
        mBarcode = new Barcode(this);
        mManager = new BarcodeManager(this);
        mSymbology = mBarcode.getSymbologyInstance();



        /************바코드 스캔 *************/
        mListener=new BarcodeListener() {
            @Override
            public void onBarcode(String s) {
                pre_barcode = s.trim();
                scan();
            }

            @Override
            public void onBarcode(String s, String s1) {}

            @Override
            public void onGetSymbology(int i, int i1) {}
        };
        mManager.addListener(mListener);

        btn_clear.setOnClickListener(v -> clear());

    }

    public void main(){
        barcode = (TextView) findViewById(R.id.barcode);
        hidden = (EditText) findViewById(R.id.hidden);
        notice = (TextView) findViewById(R.id.notice);
        pname = (TextView) findViewById(R.id.pname);
        location = (TextView) findViewById(R.id.location);
        amount = (TextView) findViewById(R.id.amount);
        status = (TextView) findViewById(R.id.status);
        scannum = (TextView) findViewById(R.id.scannum);
        listview = findViewById(R.id.listview);
        btn_clear = (Button) findViewById(R.id.btn_clear);
        /*********뒤로가기, 로그아웃 **********/
        ImageButton back = findViewById(R.id.back);
        ImageButton logout = findViewById(R.id.logout);
        mainbtn(back, logout, Email);
    }

    public void clear(){
        main();
        barcode.setText("");
        hidden.setText("");
        pname.setText("");
        location.setText("");
        amount.setText("");
        status.setText("");
        notice.setText("바코드 을(를) 스캔하세요.");

        barcode.setTextColor(Color.parseColor("#7A7A7A"));
        notice.setTextColor(Color.parseColor("#7A7A7A"));

        listViewAdapter.clearItem();
        listViewAdapter.notifyDataSetChanged();

        scannum.setText("0 Records To Display ...");

        hidden.setFocusable(true);
        hidden.requestFocus();
    }

    public void scancnt(){
        main();
        int num = listViewAdapter.getCount();
        String snum = num + " Records To Display ...";
        scannum.setText(snum);
    }

    public void scan(){
        main();
        clear();

        try {
            cs = multi_connectDB("dbo.SP_PDA_WM00070_INQUERY '" + pre_barcode + "','" + Language + "'");
            rs = cs.executeQuery();

            while(rs.next()){
                if(rs.getString(1).equals("존재하지 않는 바코드입니다.")){   //비어있지않음 => 존재하지 않는 바코드
                    notice.setTextColor(Color.parseColor("#FFFF0000"));
                    notice.setText(rs.getString(1));
                    //barcode.setTextColor(Color.parseColor("#FFFF0000"));

                    hidden.setFocusable(true);
                    hidden.requestFocus();
                }

                else {  // 비어있음 => 존재하는 바코드드
                    notice.setText(rs.getString(1));
                    barcode.setTextColor(Color.parseColor("#7A7A7A"));

                    if(cs.getMoreResults()){
                        rs = cs.getResultSet();
                        while(rs.next()){
                            barcode.setText(rs.getString(1));
                            pname.setText(rs.getString(3));
                            location.setText(rs.getString(4));
                            amount.setText(qty(rs.getString(5)).toString());
                            status.setText(rs.getString(6));
                        }
                    }
                    if(cs.getMoreResults()){
                        rs = cs.getResultSet();

                        while(rs.next()) {
                            listViewAdapter.addItem(
                                    rs.getString(1),
                                    rs.getString(2),
                                    qty(rs.getString(4)).toString(),
                                    rs.getString(3));
                            //리스트뷰에 어뎁터 set
                            listview.setAdapter(listViewAdapter);

                           scancnt();
                        }
                    }
                }
            }
        }catch (SQLException e){}
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