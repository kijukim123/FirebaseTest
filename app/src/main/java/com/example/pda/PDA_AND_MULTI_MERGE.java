package com.example.pda;

import com.m3.sdk.scannerlib.Barcode;
import com.m3.sdk.scannerlib.BarcodeListener;
import com.m3.sdk.scannerlib.BarcodeManager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PDA_AND_MULTI_MERGE extends BaseActivity {


    String overlap;
    String Email = ((PDA_AND_MAIN) PDA_AND_MAIN.context).Email;  //사용자 아이디
    String Language = ((PDA_AND_LOGIN) PDA_AND_LOGIN.context).language;  //사용자 언어
    // lib
    Barcode mBarcode = null;
    BarcodeListener mListener = null;
    BarcodeManager mManager = null;
    Barcode.Symbology mSymbology = null;

    //UI
    TextView barcode,notice,pnum,scannum,amount;
    EditText hidden;
    Spinner use_spinner;
    Button btn_clear,btn_merge;

    //바코드 스캔
    String sbarcode;
    CallableStatement bar_cs = null;
    ResultSet bar_rs = null;

    //리스트뷰, 리스트뷰 어뎁터 초기화
    ListView listview;
    ListAdapter_multiplemerge listViewAdapter;

    //병합
    Statement mer_st = connectDB();

    //위치
    Statement st =connectDB();
    ResultSet rs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pda_and_multi_merge);

        main();
        mBarcode = new Barcode(this);
        mManager = new BarcodeManager(this);
        mSymbology = mBarcode.getSymbologyInstance();

        listViewAdapter = new ListAdapter_multiplemerge();

        mListener = new BarcodeListener() {
            @Override
            public void onBarcode(String s) {
                main();
                overlap = "N";
                sbarcode = s.trim();
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

        String str_spinner = null;
        String mer_barcode = null;
        String st_barcode = barcode.getText().toString();

        if (use_spinner.getSelectedItem().toString().equals("사용")) {
            str_spinner = "Y";
        } else if (use_spinner.getSelectedItem().toString().equals("미사용")) {
            str_spinner = "N";
        }

        if (resultCode == 0) // yes 버튼 클릭 한 경우
        {

            for (int i = 0; i < listViewAdapter.getCount(); i++) {
                mer_barcode = listViewAdapter.listViewItemList.get(i).getRowtext1();
                try {
                    if ((str_spinner.equals("Y")) && (i == listViewAdapter.getCount() - 1)) {
                        mer_st.executeQuery("dbo.SP_PDA_WM00100_SAVE '" + st_barcode + "','" + mer_barcode + "','" + "Y" + "','" + Email + "','"+ Language + "'");
                    } else {
                        mer_st.executeQuery("dbo.SP_PDA_WM00100_SAVE '" + st_barcode + "','" + mer_barcode + "','" + "N" + "','" + Email + "','" + Language + "'");
                    }
                } catch (SQLException e) {
                }
            }
            clear();
            notice.setText("저장되었습니다.");
            scancnt();

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

        barcode = (TextView) findViewById(R.id.barcode);
        hidden = (EditText) findViewById(R.id.hidden);
        pnum = (TextView) findViewById(R.id.pnum);
        notice = (TextView) findViewById(R.id.notice);
        listview = (ListView) findViewById(R.id.listview);
        scannum = (TextView) findViewById(R.id.scannum);
        listview = findViewById(R.id.listview);
        use_spinner = findViewById(R.id.use_spinner);
        btn_clear = (Button) findViewById(R.id.btn_clear);
        btn_merge = (Button) findViewById(R.id.btn_merge);
        amount = (TextView) findViewById(R.id.amount);
    }

    //바코드 scan
    public void scan(){
        main();

        try {
            String query = "EXEC dbo.SP_PDA_WM00100_INQUERY_BARCODE '" + sbarcode + "', '" + Language + "'";
            bar_cs = multi_connectDB(query);
            bar_rs = bar_cs.executeQuery();
            barcode.setTextColor(Color.parseColor("#7A7A7A"));
            notice.setTextColor(Color.parseColor("#7A7A7A"));
            notice.setText("바코드 을(를) 스캔하세요.");


            if (pnum.getText().toString().equals("")) {          //기준 바코드


                if (!bar_rs.next()) {   //사용 불가
                    notice.setTextColor(Color.parseColor("#FFFF0000"));
                    notice.setText("해당하는 바코드 정보가 없습니다.");
                    hidden.setFocusable(true);
                    hidden.requestFocus();
                    hidden.setText("");

                } else {  //사용 가능
                    barcode.setText(sbarcode);
                    pnum.setText(bar_rs.getString(1));
                    amount.setText(qty(bar_rs.getString(3)));
                    hidden.setFocusable(true);
                    hidden.requestFocus();
                    hidden.setText("");
                }
            }

            else if (!pnum.getText().toString().equals("")) {   //병합 바코드 추가
                hidden.setFocusable(true);
                hidden.requestFocus();

                if (!bar_rs.next()) {
                    notice.setText("해당하는 바코드 정보가 없습니다.");
                    notice.setTextColor(Color.parseColor("#FFFF0000"));

                    hidden.setFocusable(true);
                    hidden.requestFocus();
                    hidden.setText("");

                } else {
                    String query2 = "select RACKNM from TB_WM_RACK_MNGR_L A LEFT JOIN TB_WM_BCOD B ON A.RACKCD = B.LOCCD where B.BARCODE = '" + sbarcode + "'";
                    st = connectDB();
                    rs = st.executeQuery(query2);
                    while (rs.next()) {

                        notice.setTextColor(Color.parseColor("#7A7A7A"));
                        notice.setText("바코드 을(를) 스캔하세요.");
                        if (!pnum.getText().toString().equals(bar_rs.getString(1))) {
                            notice.setText("동일한 품번이 아닙니다.");
                            notice.setTextColor(Color.parseColor("#FFFF0000"));

                            hidden.setFocusable(true);
                            hidden.requestFocus();
                            hidden.setText("");
                        } else if (barcode.getText().toString().equals(bar_rs.getString(4))) {
                            notice.setText("기준 바코드와 동일한 바코드 입니다.");
                            notice.setTextColor(Color.parseColor("#FFFF0000"));

                            hidden.setFocusable(true);
                            hidden.requestFocus();
                            hidden.setText("");
                        } else if (listViewAdapter.getCount() == 0) {    //리스트 뷰에 값이 하나도 없을 때
                            listViewAdapter.addItem(
                                    bar_rs.getString(4),
                                    qty(bar_rs.getString(3)).toString(),
                                    rs.getString(1));
                            //리스트뷰에 어뎁터 set
                            listview.setAdapter(listViewAdapter);

                            hidden.setFocusable(true);
                            hidden.requestFocus();
                            hidden.setText("");
                        } else if (listViewAdapter.getCount() != 0) {
                            for (int i = 0; i < listViewAdapter.getCount(); i++) {
                                if (listViewAdapter.listViewItemList.get(i).getRowtext1().equals(bar_rs.getString(4))) {
                                    overlap = "Y";
                                    break;
                                }
                            }
                            if (overlap.equals("Y")) {
                                notice.setText("이미 스캔한 바코드 입니다.");
                                notice.setTextColor(Color.parseColor("#FFFF0000"));
                                hidden.setFocusable(true);
                                hidden.requestFocus();
                                hidden.setText("");
                            } else {
                                listViewAdapter.addItem(
                                        bar_rs.getString(4),
                                        qty(bar_rs.getString(3)).toString(),
                                        rs.getString(1));
                                //리스트뷰에 어뎁터 set
                                listview.setAdapter(listViewAdapter);

                                hidden.setFocusable(true);
                                hidden.requestFocus();
                                hidden.setText("");
                            }

                        }
                    }
                }
                /******************* 병합 바코드 추가 개수 **********************/
                scancnt();
            }
        } catch (SQLException e) {}
    }

    //scan 개수
    public void scancnt(){
        main();
        int num = listViewAdapter.getCount();
        String snum = num + " Scanned";
        scannum.setText(snum);
    }

    //spinner
    public void spinner(){
        use_spinner = findViewById(R.id.use_spinner);
        String[] items = {"미사용", "사용"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_dropdown_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        use_spinner.setAdapter(adapter);
    }

    //초기화
    public void clear(){
        main();
        barcode.setText("");
        hidden.setText("");
        pnum.setText("");
        amount.setText("");

        notice.setText("바코드 을(를) 스캔하세요.");
        notice.setTextColor(Color.parseColor("#7A7A7A"));
        barcode.setTextColor(Color.parseColor("#7A7A7A"));

        hidden.setFocusable(true);
        hidden.requestFocus();

        listViewAdapter.clearItem();
        listViewAdapter.notifyDataSetChanged();

        use_spinner.setSelection(0);

        scancnt();

    }

    //병합
    public void merge(){
        main();

        if(pnum.getText().equals("") || listViewAdapter.getCount()==0 ){

            moveActivity(Confirm.class, "바코드 을(를) 스캔하세요.");
        }
        else {
            Intent intent = new Intent(PDA_AND_MULTI_MERGE.this, PDA_AND_MERGE_POPUP.class);
            startActivityForResult(intent, 0);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mManager.dismiss();
    }
}

