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
import android.widget.Toast;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PDA_AND_OUTPUT_PARTNO_POPUP extends BaseActivity {
    private TextView PARTNO, PARTNM, TVMessage;
    private String StrQTY;
    private Context mContext;
    private String UserId, Language;
    private String PartNo, PartNm, Barcode, OriginQTY;
    private int SelectedSpinner;
    private List<Integer> LIST_PARTNO_POPUP_NO = new ArrayList<>();
    private List<String> LIST_PARTNO_POPUP_RACK = new ArrayList<>();
    private List<String> LIST_PARTNO_POPUP_QTY = new ArrayList<>();
    private ListView BarcodePopupListview;
    private int i;
    private String rack, pnum;
    private EditText ETtempQTY;
    private String SOutQTY, SBarcode;
    public ArrayList<String> ListNameOUTPUT = null;
    public ArrayList<String> ListPickNumberOUTPUT = null;
    public ArrayList<String> ListNumberOUTPUT = null;
    public ArrayList<String> ListCheckOUTPUT = null;
    public ArrayList<String> ListPartNoOUTPUT = null;
    private String StrPN, SPreInputA, StrPARTNO, StrPARTNM;
    private String StrOutNumber, StrOutCust, StrOutDate;
    private List<String> LIST_PARTNO_OUT = new ArrayList<>();
    private List<String> LIST_Name_OUT = new ArrayList<>();
    private List<String> LIST_PickNumber_OUT = new ArrayList<>();
    private List<String> LIST_Number_OUT = new ArrayList<>();
    private List<String> LIST_CHECK_OUT = new ArrayList<>();
    private int AlreadyCheck;
    private int BreakPoint;
    private String ErrorMessage;
    private int PartnoPopoupIntent, Count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pda_and_output_partno_popup);

        UserId = ((PDA_AND_MAIN) PDA_AND_MAIN.context).Email;  //사용자 아이디
        Language = ((PDA_AND_LOGIN) PDA_AND_LOGIN.context).language;  //사용자 언어

        Count = 0;

        Intent getIntent = getIntent();
        SBarcode = getIntent.getStringExtra("SBarcode");
        StrPN = getIntent.getStringExtra("SPreInputPN");
        SPreInputA = getIntent.getStringExtra("SPreInputA");
        StrPARTNO = getIntent.getStringExtra("StrPARTNO");
        StrPARTNM = getIntent.getStringExtra("StrPARTNM");
        StrOutNumber = getIntent.getStringExtra("StrOutNumber");
        StrOutCust = getIntent.getStringExtra("StrOutCust");
        StrOutDate = getIntent.getStringExtra("StrOutDate");

        System.out.println(StrPN);
        System.out.println(StrPARTNO);
        System.out.println(StrPARTNM);
        System.out.println(StrOutNumber);
        System.out.println(StrOutCust);
        System.out.println(StrOutDate);
        System.out.println(ListNameOUTPUT);
        System.out.println(ListPickNumberOUTPUT);
        System.out.println(ListNumberOUTPUT);
        System.out.println(ListCheckOUTPUT);
        System.out.println(ListPartNoOUTPUT);



        Button btn_save = (Button) findViewById(R.id.button_partno_popup_save);
        Button btn_close = (Button) findViewById(R.id.button_partno_popup_close);

        TVMessage = findViewById(R.id.tv_partno_popup_message);

        PARTNO = findViewById(R.id.tv_partno_popup_partno);
        PARTNM = findViewById(R.id.tv_partno_popup_partnm);
        PARTNO.setText(StrPARTNO);
        PARTNM.setText(StrPARTNM);


        ETtempQTY = findViewById(R.id.et_partno_popup_temptext);


        // 프로시져 돌려서 리스트뷰에 집어 넣는 부분이 잘못 되었다.
        try {
            String query2 = "EXEC SP_PDA_WM00120P1_INQUERY '" + StrPARTNO + "','ko'";
            System.out.println("EXEC SP_PDA_WM00120P1_INQUERY '" + StrPARTNO + "','ko'");
            Statement st2 = connectDB();
            ResultSet rs2 = st2.executeQuery(query2);

            while (rs2.next()) {

                //System.out.println("반환값 : " + rs2.getString(0));
                System.out.println("반환값 : " + rs2.getString(1));
                System.out.println("반환값 : " + rs2.getString(2));
                System.out.println("반환값 : " + rs2.getString(3));
                System.out.println("반환값 : " + rs2.getString(4));
                System.out.println("반환값 : " + rs2.getString(5));
                if (rs2.getString(2).isEmpty() == false) {
                    for (i = 1; i <= 1000; i++) {
                        LIST_PARTNO_POPUP_NO.add(i);
                    }
                    PARTNO.setText(rs2.getString(2));
                    PARTNM.setText(rs2.getString(3));
                    //LIST_INDIVIDUAL_POPUP_NO.add(rs2.getString(0));
                    LIST_PARTNO_POPUP_RACK.add(rs2.getString(4));
                    LIST_PARTNO_POPUP_QTY.add(qty(rs2.getString(5)));
                    CustomListIndividualPopup adapterPopup = new CustomListIndividualPopup(PDA_AND_OUTPUT_PARTNO_POPUP.this);
                    BarcodePopupListview = (ListView) findViewById(R.id.listview_partno_popup);
                    BarcodePopupListview.setAdapter(adapterPopup);

                    //첫번째 값 기본값으로 세팅
                    rack = LIST_PARTNO_POPUP_RACK.get(0);
                    OriginQTY = LIST_PARTNO_POPUP_QTY.get(0);

                    BarcodePopupListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            int check_position = BarcodePopupListview.getCheckedItemPosition();   //리스트뷰의 포지션을 가져옴.
                            Object objRack = (Object) adapterView.getAdapter().getItem(i);  //리스트뷰의 포지션 내용을 가져옴.
                            OriginQTY = LIST_PARTNO_POPUP_QTY.get(i);
                            System.out.println(LIST_PARTNO_POPUP_QTY.get(i));
                            rack = objRack.toString();
                            System.out.println("WHAT IS RACK :: " + rack);
                        }
                    });


                }

            }
        } catch (Exception e) {
            //Toast.makeText(getApplicationContext(), "데이터베이스에 안들어갔습니다.", Toast.LENGTH_SHORT).show();
            System.out.println("SP_PDA_WM00120P1_INQUERY 안돌아감!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }


        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TVMessage.setText("출고할 수량을 입력하세요 :");
                TVMessage.setTextColor(Color.GRAY);
                ETtempQTY.setText("");
                ETtempQTY.setTextColor(Color.GRAY);
                ETtempQTY.requestFocus();

                //EditText에 있는 이동 수량을 가져와서 StringQTY에 넣어준다.
                //ETtempQTY.requestFocus();

                //키보드 보이게 하는 부분
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

                ETtempQTY.setFocusableInTouchMode(true);
                ETtempQTY.setFocusable(true);


                //엔터키 방식
                ETtempQTY.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        switch (keyCode) {
                            case KeyEvent.KEYCODE_ENTER:
                                SOutQTY = ETtempQTY.getText().toString();


                                try {
                                    String query8 = "EXEC SP_PDA_WM00030_INQUERY_MERGE '" + StrPN + "','" + SPreInputA + "','" + SOutQTY + "','" + UserId + "','" + Language + "'";
                                    System.out.println(query8);
                                    Statement st8 = connectDB();
                                    ResultSet rs8 = st8.executeQuery(query8);

                                    //RESULT MESSAGE TEST
                                    while (rs8.next()) {

                                        for (i = 0; i < 1; i++) {
                                            Count = Count + 1;
                                            if (Count == 1) {
                                                if (!rs8.getString(1).equals("")) {
                                                    ErrorMessage = rs8.getString(1);
                                                    System.out.println(ErrorMessage);
                                                    System.out.println("Intent ErrorMessage : " + ErrorMessage);
                                                    Intent intentClose = new Intent(PDA_AND_OUTPUT_PARTNO_POPUP.this, PDA_AND_OUTPUT.class);
                                                    intentClose.putExtra("ErrorMessage", ErrorMessage);
                                                    setResult(3110, intentClose);
                                                    finish();
                                                }
                                                else if (rs8.getString(1).equals("")){
                                                    System.out.println("ERROR MESSAGE IS NULL");
                                                    ErrorMessage = "null";
                                                    System.out.println("Intent ErrorMessage : " + ErrorMessage);
                                                    Intent intentClose = new Intent(PDA_AND_OUTPUT_PARTNO_POPUP.this, PDA_AND_OUTPUT.class);
                                                    intentClose.putExtra("ErrorMessage", ErrorMessage);
                                                    setResult(3110, intentClose);
                                                    finish();
                                                }
                                            }

                                        }



                                    }
                                } catch (Exception e) {
                                    System.out.println("Second Table Search Error");
                                }




                        }

                        return false;
                    }
                });


            }
        });

        btn_close.setOnClickListener((view -> {

            Intent intentClose = new Intent(PDA_AND_OUTPUT_PARTNO_POPUP.this, PDA_AND_OUTPUT.class);
            setResult(3110, intentClose);
            finish();
        }));


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
            super(context, R.layout.listitemrow_barcode_individual_popup, LIST_PARTNO_POPUP_RACK);
            this.context = context;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.listitemrow_output_partno, null, true);
            TextView ItemNo = (TextView) rowView.findViewById(R.id.partno_popup_item_no);
            TextView ItemRack = (TextView) rowView.findViewById(R.id.partno_popup_item_rack);
            TextView ItemQty = (TextView) rowView.findViewById(R.id.partno_popup_item_qty);
            //Button approve = (Button) rowView.findViewById(R.id.approve_user);
            ItemNo.setText(LIST_PARTNO_POPUP_NO.get(position).toString());
            ItemRack.setText(LIST_PARTNO_POPUP_RACK.get(position).toString());
            ItemQty.setText(LIST_PARTNO_POPUP_QTY.get(position).toString());
            //approve.setText("승인");

            return rowView;
        }

    }



}