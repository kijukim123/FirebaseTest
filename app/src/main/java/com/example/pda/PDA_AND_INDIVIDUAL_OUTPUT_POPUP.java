package com.example.pda;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PDA_AND_INDIVIDUAL_OUTPUT_POPUP extends BaseActivity {
    private TextView PARTNO, PARTNM,alert1,et;
    private String StrQTY;
    private Context mContext;
    private String UserId;
    private String PartNo, PartNm, Barcode, OriginQTY;
    private int SelectedSpinner;
    private List<Integer> LIST_INDIVIDUAL_POPUP_NO = new ArrayList<>();
    private List<String> LIST_INDIVIDUAL_POPUP_RACK = new ArrayList<>();
    private List<String> LIST_INDIVIDUAL_POPUP_QTY = new ArrayList<>();
    private ListView BarcodePopupListview;
    private int i;
    private String rack, pnum;
    private EditText ETtempQTY;
    private String SOutQTY;
    public ArrayList<String> ListNameIndividual = null;
    public ArrayList<String> ListRackIndividual = null;
    public ArrayList<String> ListQtyIndividual = null;
    public ArrayList<String> ListOutqtyIndividual = null;
    public ArrayList<String> ListBarcodeIndividual = null;
    public ArrayList<String> ListCancelIndividual = null;
    private String StrOutDate;
    Button btn_save ;
    Button btn_close ;
    String Language;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pda_and_individual_output_popup);

        UserId = ((PDA_AND_MAIN) PDA_AND_MAIN.context).Email;  //사용자 아이디
        Language = ((PDA_AND_LOGIN) PDA_AND_LOGIN.context).language;  //사용자 언어

        main();
        btn_save.setOnClickListener(view -> save());
        btn_close.setOnClickListener(view -> finish());

        ETtempQTY.setTextColor(Color.WHITE);
        PARTNO.setFocusable(true);
        PARTNO.requestFocus();


        Intent intent = getIntent();
        PartNo = intent.getStringExtra("PARTNO");
        PartNm = intent.getStringExtra("PARTNM");
        Barcode = intent.getStringExtra("BARCODE");
        SelectedSpinner = intent.getIntExtra("CustPosition", 0);
        StrOutDate = intent.getStringExtra("StrOutDate");

        PARTNO.setText(PartNo);
        PARTNM.setText(PartNm);

        content();




    }

    void content(){
        main();
        PARTNO.setFocusable(true);
        PARTNO.requestFocus();

        try{
            String query2 = "EXEC SP_PDA_WM00120P1_INQUERY '" + PartNo + "','"+ Language +"'";
            Statement st2 = connectDB();
            ResultSet rs2 = st2.executeQuery(query2);
            int i = 1;
            while (rs2.next()){
                PARTNO.setText(rs2.getString(2));
                PARTNM.setText(rs2.getString(3));
                LIST_INDIVIDUAL_POPUP_NO.add(i);

                //LIST_INDIVIDUAL_POPUP_NO.add(rs2.getString(0));
                LIST_INDIVIDUAL_POPUP_RACK.add(rs2.getString(4));
                LIST_INDIVIDUAL_POPUP_QTY.add(qty(rs2.getString(5)).toString());
                CustomListIndividualPopup adapterPopup = new CustomListIndividualPopup(PDA_AND_INDIVIDUAL_OUTPUT_POPUP.this);
                BarcodePopupListview = (ListView) findViewById(R.id.listview_individual_popup);
                BarcodePopupListview.setAdapter(adapterPopup);

                //첫번째 값 기본값으로 세팅
                rack = LIST_INDIVIDUAL_POPUP_RACK.get(0);
                OriginQTY = LIST_INDIVIDUAL_POPUP_QTY.get(0);

                BarcodePopupListview.setOnItemClickListener((parent, view, i1, id) ->{
                    OriginQTY = LIST_INDIVIDUAL_POPUP_QTY.get(i1);
                    rack = LIST_INDIVIDUAL_POPUP_RACK.get(i1);
                });

                alert1.setText(i + " Records To Display ...");
                i++;

            }
        }catch(Exception e){
            //Toast.makeText(getApplicationContext(), "데이터베이스에 안들어갔습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    void main(){
        PARTNO = findViewById(R.id.tv_individual_popup_partno);
        PARTNM = findViewById(R.id.tv_individual_popup_partnm);
        ETtempQTY = findViewById(R.id.et_individual_popup_temptext);
        alert1 = findViewById(R.id.barcode_individual_popup_alert1);
        et = findViewById(R.id.et);
        btn_save = findViewById(R.id.button_individual_popup_save);
        btn_close =  findViewById(R.id.button_individual_popup_close);
    }

    void save(){
        main();
        //EditText에 있는 이동 수량을 가져와서 StringQTY에 넣어준다.
        et.setText("출고할 수량을 입력하세요 :");
        ETtempQTY.setText("");
        ETtempQTY.setTextColor(Color.GRAY);
        ETtempQTY.requestFocus();

        //키보드 보이게 하는 부분
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        ETtempQTY.setFocusableInTouchMode(true);
        ETtempQTY.setFocusable(true);


        //엔터키 방식
        ETtempQTY.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch(keyCode){
                    case KeyEvent.KEYCODE_ENTER:
                        SOutQTY = ETtempQTY.getText().toString();

                        /*
                        ListNameIndividual.add(PartNm);
                        ListRackIndividual.add(rack);
                        ListQtyIndividual.add(OriginQTY);
                        ListOutqtyIndividual.add(SOutQTY);
                        ListBarcodeIndividual.add(Barcode);
                        */

                        Intent parentIntent = new Intent(getApplicationContext(), PDA_AND_INDIVIDUAL_OUTPUT.class);
                        parentIntent.putExtra("PartNm", PartNm);
                        parentIntent.putExtra("rack", rack);
                        parentIntent.putExtra("OriginQTY", OriginQTY);
                        parentIntent.putExtra("SOutQTY", SOutQTY);
                        parentIntent.putExtra("Barcode", Barcode);
                        //parentIntent.putExtra("CustPositon", SelectedSpinner);
                        //parentIntent.putExtra("StrOutDate", StrOutDate);

                        setResult(1,parentIntent);


                        finish();
                }
                return false;
            }
        });

    }

    //바깥 레이어 클릭 시 안닫히게
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            return false;
        }
        return true;
    }

    //안드로이드 백버튼 막기
    @Override
    public void onBackPressed() {
        return;
    }

    //바코드취소 관련 어뎁터
    public class CustomListIndividualPopup extends ArrayAdapter<String> {
        private final Activity context;

        public CustomListIndividualPopup(Activity context) {
            super(context, R.layout.listitemrow_barcode_individual_popup, LIST_INDIVIDUAL_POPUP_RACK);
            this.context = context;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.listitemrow_barcode_individual_popup, null, true);
            TextView ItemNo = (TextView) rowView.findViewById(R.id.individual_popup_item_no);
            TextView ItemRack = (TextView) rowView.findViewById(R.id.individual_popup_item_rack);
            TextView ItemQty = (TextView) rowView.findViewById(R.id.individual_popup_item_qty);
            //Button approve = (Button) rowView.findViewById(R.id.approve_user);
            ItemNo.setText(LIST_INDIVIDUAL_POPUP_NO.get(position).toString());
            ItemRack.setText(LIST_INDIVIDUAL_POPUP_RACK.get(position).toString());
            ItemQty.setText(LIST_INDIVIDUAL_POPUP_QTY.get(position).toString());
            //approve.setText("승인");

            return rowView;
        }

    }



}