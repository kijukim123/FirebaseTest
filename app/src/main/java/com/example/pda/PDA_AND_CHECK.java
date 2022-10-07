package com.example.pda;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PDA_AND_CHECK extends BaseActivity {

    // lib
    Barcode mBarcode = null;
    BarcodeListener mListener = null;
    BarcodeManager mManager = null;
    Barcode.Symbology mSymbology = null;

    String Email = ((PDA_AND_MAIN) PDA_AND_MAIN.context).Email;  //사용자 아이디
    String Language = ((PDA_AND_LOGIN) PDA_AND_LOGIN.context).language;  //사용자 언어
    String seq,sdate,pre_barcode,rack_barcode,m_barcode;
    String qty;
    TextView barcode, tv_date,notice,scannum;
    EditText hidden;
    ImageButton predate, nextdate;
    Button rack_clear,btn_clear;

    //랙 정보
    Statement r_st = connectDB();
    ResultSet r_rs ;
    Statement r_st2 = connectDB();
    ResultSet r_rs2;
    //바코드 스캔
    CallableStatement b_cs= null;
    ResultSet b_rs=null;
    //랙 초기화
    Statement clear_st = connectDB();
    ResultSet clear_rs;
    Statement clear_st2 = connectDB();

    Calendar cal = Calendar.getInstance();
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    //리스트뷰 어뎁터 초기화
    ListView listview;
    ListAdapter_checkbarcode listViewAdapter;
    private BarcodeManager barcodeManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pda_and_check);

        date();
        main();
        predate.setOnClickListener(v -> minusdate());
        nextdate.setOnClickListener(v -> plusdate());


        /************바코드 스캔 *************/
        mBarcode = new Barcode(this);
        mManager = new BarcodeManager(this);
        mSymbology = mBarcode.getSymbologyInstance();
        listViewAdapter = new ListAdapter_checkbarcode();

        mListener = new BarcodeListener() {
            @Override
            public void onBarcode(String s) {
                pre_barcode = s.trim();

                if (pre_barcode.startsWith("R")) {    //아직 랙 바코드가 스캔 안된 상황
                    rackbarcode();
                }

                else {  //상품 바코드 스캔
                    mbarcode();
                }
            }
            @Override
            public void onBarcode(String s, String s1) {}

            @Override
            public void onGetSymbology(int i, int i1) {}
        };
        mManager.addListener(mListener);

        rack_clear.setOnClickListener(v -> rackclear());
        btn_clear.setOnClickListener(v -> btnclear());
    }

    /********* 팝업 종료 후 호출되는 코드  *******/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        main();
        String current_date = tv_date.getText().toString();
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 0) // yes 버튼 클릭 한 경우
        {
            try{
                clear_rs =clear_st.executeQuery("dbo.SP_PDA_WM00060_SAVE '" + current_date + "' , '" + rack_barcode + "' , '" + Email +"'");
                while(clear_rs.next()){
                    seq = clear_rs.getString(1);
                }
                try {
                    clear_st2.executeQuery("dbo.SP_PDA_WM00060_SAVE_DETL '" + current_date + "','" + rack_barcode + "','" + seq + "','" + Email + "'");
                }catch (SQLException e){}

                listViewAdapter.clearItem();
                listViewAdapter.notifyDataSetChanged();
                notice.setTextColor(Color.parseColor("#7A7A7A"));
                notice.setText("바코드 을(를) 스캔하세요.");
                int num = listViewAdapter.getCount();
                String snum = num + " Records To Display ...";
                scannum.setText(snum);
            }catch (SQLException e ){}
        }

        if (resultCode == 500){
            PDA_AND_MAIN MA = (PDA_AND_MAIN)PDA_AND_MAIN._Main_Activity;
            MA.finish();
            finish();
        }
    }

    //스캔 개수
    public void scancnt(){
        main();
        int num = listViewAdapter.getCount();
        String snum = num + " Records To Display ...";
        scannum.setText(snum);
    }

    //랙 바코드 스캔
    public void rackbarcode(){
        main();
        listViewAdapter.clearItem();
        listViewAdapter.notifyDataSetChanged();
        rack_barcode = pre_barcode;
        // barcode.setText(rack_barcode);
        try {
            r_rs2 = r_st2.executeQuery("select * from TB_WM_RACK_MNGR_L where RACKCD = '" + rack_barcode +"'");
            r_rs = r_st.executeQuery("EXEC dbo. SP_PDA_WM00060_INQUERY_RACK '" + rack_barcode + "', '" + Language + "'");
            if(!r_rs2.next()) {
                notice.setText("해당하는 위치의 정보가 없습니다.");
                notice.setTextColor(Color.parseColor("#FFFF0000"));
                barcode.setText("");
                hidden.setText("");
                hidden.setFocusable(true);
                hidden.requestFocus();
            }
            else{
                System.out.println("******************************" + r_rs2.getString(3));
                barcode.setText(r_rs2.getString(3));
                notice.setText("바코드 을(를) 스캔하세요.");
                notice.setTextColor(Color.parseColor("#7A7A7A"));

                if(!r_rs.next()){
                    listViewAdapter.clearItem();
                    listViewAdapter.notifyDataSetChanged();
                    notice.setText("바코드 을(를) 스캔하세요.");
                    notice.setTextColor(Color.parseColor("#7A7A7A"));
                }
                else{

                    do{
                        listViewAdapter.addItem(
                                r_rs.getString(3),
                                qty(r_rs.getString(4)),
                                r_rs.getString(1));
                        //리스트뷰에 어뎁터 set
                        listview.setAdapter(listViewAdapter);
                        listViewAdapter.notifyDataSetChanged();

                        scancnt();

                        notice.setText("바코드 을(를) 스캔하세요.");
                        notice.setTextColor(Color.parseColor("#7A7A7A"));
                    } while (r_rs.next());
                }
            }


        } catch (SQLException e) {}

    }

    //상품 바코드 스캔
    public void mbarcode(){
        main();
        m_barcode = pre_barcode;
        try{
            b_cs = multi_connectDB("dbo.SP_PDA_WM00060_INQUERY '" + m_barcode +"','" + rack_barcode + "','" + Email + "','" + Language + "'");
            b_rs = b_cs.executeQuery();
            while(b_rs.next()){
                if(b_rs.getString(1).equals("")) {  //오류 메시지 없는 경우  =>적합한 바코드
                    notice.setTextColor(Color.parseColor("#7A7A7A"));
                    notice.setText(b_rs.getString(2));

                    if(b_cs.getMoreResults()){
                        if(b_cs.getMoreResults()){
                            b_rs=b_cs.getResultSet();
                            listViewAdapter.clearItem();
                            while(b_rs.next()){
                                listViewAdapter.addItem(
                                        b_rs.getString(3),
                                        qty(b_rs.getString(4)),
                                        b_rs.getString(1));
                                //리스트뷰에 어뎁터 set
                                listview.setAdapter(listViewAdapter);
                                listViewAdapter.notifyDataSetChanged();
                            }
                            scancnt();
                        }
                    }
                }
                else {   //메시지 있음
                    notice.setText(b_rs.getString(1));
                    notice.setTextColor(Color.parseColor("#FFFF0000"));

                    hidden.setText("");
                    hidden.setFocusable(true);
                    hidden.requestFocus();
                }
            }
        }catch (SQLException e){

        }
    }

    //main
    public void main(){
        /*********뒤로가기, 로그아웃 **********/
        ImageButton back = findViewById(R.id.back);
        ImageButton logout = findViewById(R.id.logout);
        mainbtn(back, logout, Email);

        tv_date = findViewById(R.id.date);
        predate = findViewById(R.id.predate);
        nextdate = findViewById(R.id.nextdate);
        barcode= findViewById(R.id.barcode);
        hidden = findViewById(R.id.hidden);
        listview = findViewById(R.id.listview);
        notice = findViewById(R.id.notice);
        scannum = findViewById(R.id.scannum);
        listview = findViewById(R.id.listview);
        rack_clear = findViewById(R.id.rack_clear);
        btn_clear = findViewById(R.id.btn_clear);
    }

    //랙 초기화
    public void rackclear() {
        if(barcode.getText().equals("")){
            moveActivity(Confirm.class, "위치명 을(를) 스캔하세요.");
        }
        else{
            Intent intent = new Intent(PDA_AND_CHECK.this, PDA_AND_CHECK_POPUP.class);
            startActivityForResult(intent,0);
        }

    }

    //기준 일자
    public void date(){
        tv_date = findViewById(R.id.date);
        cal.setTime(new Date());
        sdate = df.format(cal.getTime());
        tv_date.setText(sdate);
    }
    public void plusdate(){
        tv_date = findViewById(R.id.date);
        cal.add(Calendar.DATE,1);
        tv_date.setText(df.format(cal.getTime()));
    }
    public void minusdate(){
        tv_date = findViewById(R.id.date);
        cal.add(Calendar.DATE,-1);
        tv_date.setText(df.format(cal.getTime()));
    }

    //초기화 버튼
    public void btnclear(){
        main();
        barcode.setText("");
        date();
        listViewAdapter.clearItem();
        listViewAdapter.notifyDataSetChanged();

        notice.setText("위치명 을(를) 스캔하세요.");
        notice.setTextColor(Color.parseColor("#7A7A7A"));
        scannum.setText( "0 Records To Display ...");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mManager.dismiss();
    }
}