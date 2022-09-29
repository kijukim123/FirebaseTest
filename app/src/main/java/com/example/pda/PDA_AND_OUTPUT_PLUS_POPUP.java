package com.example.pda;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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

public class PDA_AND_OUTPUT_PLUS_POPUP extends BaseActivity {
    private TextView PARTNO, PARTNM;
    private String StrQTY;
    private Context mContext;
    private String UserId;
    private String PartNo, PartNm, Barcode, OriginQTY;
    private int SelectedSpinner;
    private List<Integer> LIST_OUTPUT_POPUP_NO = new ArrayList<>();
    private List<String> LIST_OUTPUT_POPUP_BARCODE = new ArrayList<>();
    private List<String> LIST_OUTPUT_POPUP_PICKINGQTY = new ArrayList<>();
    private List<String> LIST_OUTPUT_POPUP_QTY = new ArrayList<>();
    private ListView BarcodePopupListview;
    private int i;
    private String rack, pnum;
    private EditText ETtempQTY;
    private String SOutQTY;
    public ArrayList<String> ListNameOUTPUT = null;
    public ArrayList<String> ListPickNumberOUTPUT = null;
    public ArrayList<String> ListNumberOUTPUT = null;
    public ArrayList<String> ListCheckOUTPUT = null;
    public ArrayList<String> ListPartNoOUTPUT = null;
    private String StrPN, StrPARTNO, StrPARTNM;
    private String StrOutNumber, StrOutCust, StrOutDate;
    private String Language;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pda_and_output_popup);

        UserId = ((PDA_AND_MAIN) PDA_AND_MAIN.context).Email;  //사용자 아이디
        Language = ((PDA_AND_LOGIN) PDA_AND_LOGIN.context).language;  //사용자 언어


        Intent getIntent = getIntent();
        StrPN = getIntent().getStringExtra("SPreInputPN");
        StrPARTNO = getIntent().getStringExtra("StrPARTNO");
        StrPARTNM = getIntent().getStringExtra("StrPARTNM");
        StrOutNumber = getIntent().getStringExtra("StrOutNumber");
        StrOutCust = getIntent().getStringExtra("StrOutCust");
        StrOutDate = getIntent().getStringExtra("StrOutDate");
        ListNameOUTPUT = getIntent().getStringArrayListExtra("NameList");
        ListPickNumberOUTPUT = getIntent().getStringArrayListExtra("PickNumberList");
        ListNumberOUTPUT = getIntent().getStringArrayListExtra("NumberList");
        ListCheckOUTPUT = getIntent().getStringArrayListExtra("CheckList");
        ListPartNoOUTPUT = getIntent().getStringArrayListExtra("PartNo");
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


        PARTNO = findViewById(R.id.tv_output_popup_partno);
        PARTNM = findViewById(R.id.tv_output_popup_partnm);
        PARTNO.setText(StrPARTNO);
        PARTNM.setText(StrPARTNM);
        System.out.println("/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/");
        System.out.println(StrPARTNO);
        System.out.println(StrPARTNM);
        System.out.println("/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/");


        Button btn_close = (Button) findViewById(R.id.button_output_popup_close);


        mContext = this;
        UserId = PreferenceManager.getString(mContext, "UID");
        System.out.println(UserId + "///////////////////////////////////////////////////////////////");


        try {
            String query2 = "EXEC SP_PDA_WM00030P1_INQUERY '" + StrPN + "','" + StrPARTNO + "','" + Language + "'";
            System.out.println("EXEC SP_PDA_WM00030P1_INQUERY '" + StrPN + "','" + StrPARTNO + "','" + Language + "'");
            Statement st2 = connectDB();
            ResultSet rs2 = st2.executeQuery(query2);

            while (rs2.next()) {
                if (rs2.getString(2).isEmpty() == false) {
                    for (i = 1; i <= 1000; i++) {
                        LIST_OUTPUT_POPUP_NO.add(i);
                    }

                    //LIST_INDIVIDUAL_POPUP_NO.add(rs2.getString(0));
                    LIST_OUTPUT_POPUP_BARCODE.add(rs2.getString(1));
                    LIST_OUTPUT_POPUP_PICKINGQTY.add(qty(rs2.getString(2)));
                    LIST_OUTPUT_POPUP_QTY.add("ㅡ");
                    CustomListOutputPopup adapterPopup = new CustomListOutputPopup(PDA_AND_OUTPUT_PLUS_POPUP.this);
                    BarcodePopupListview = (ListView) findViewById(R.id.listview_output_popup);
                    BarcodePopupListview.setAdapter(adapterPopup);


                }

            }
        } catch (Exception e) {
            //Toast.makeText(getApplicationContext(), "데이터베이스에 안들어갔습니다.", Toast.LENGTH_SHORT).show();
            System.out.println("SP_PDA_WM00120P1_INQUERY 안돌아감!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }





        btn_close.setOnClickListener((view -> {
            Intent intentClose = new Intent(PDA_AND_OUTPUT_PLUS_POPUP.this, PDA_AND_OUTPUT.class);
            //setResult(4010, intentClose);
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
    public class CustomListOutputPopup extends ArrayAdapter<String> {
        private final Activity context;

        public CustomListOutputPopup(Activity context) {
            super(context, R.layout.listitemrow_output_popup, LIST_OUTPUT_POPUP_BARCODE);
            this.context = context;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.listitemrow_output_popup, null, true);
            TextView ItemNo = (TextView) rowView.findViewById(R.id.output_popup_item_no);
            TextView ItemBARCODE = (TextView) rowView.findViewById(R.id.output_popup_item_barcode);
            TextView ItemQty = (TextView) rowView.findViewById(R.id.output_popup_item_qty);
            TextView ItemDelete = (TextView) rowView.findViewById(R.id.output_popup_item_delete);
            ItemDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    //마이너스 버튼을 눌렀을 경우 데이터베이스에서 해당 값 빼기
                    try {
                        System.out.println("minus button clicked For get barcode :: " + LIST_OUTPUT_POPUP_BARCODE.get(position));
                        String query3 = "EXEC SP_PDA_WM00030P1_DELETE '" + StrPN + "','" + LIST_OUTPUT_POPUP_BARCODE.get(position) + "'";
                        System.out.println("EXEC SP_PDA_WM00030P1_DELETE '" + StrPN + "','" + LIST_OUTPUT_POPUP_BARCODE.get(position) + "'");
                        Statement st3 = connectDB();
                        ResultSet rs3 = st3.executeQuery(query3);

                        while (rs3.next()) {

                        }
                    } catch (Exception e) {
                        //Toast.makeText(getApplicationContext(), "데이터베이스에 안들어갔습니다.", Toast.LENGTH_SHORT).show();
                        System.out.println("SP_PDA_WM00030P1_DELETE 안돌아감!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    }


                    LIST_OUTPUT_POPUP_NO.remove(position);
                    LIST_OUTPUT_POPUP_BARCODE.remove(position);
                    LIST_OUTPUT_POPUP_PICKINGQTY.remove(position);
                    LIST_OUTPUT_POPUP_QTY.remove(position);
                    CustomListOutputPopup adapterPopup = new CustomListOutputPopup(PDA_AND_OUTPUT_PLUS_POPUP.this);
                    BarcodePopupListview = (ListView) findViewById(R.id.listview_output_popup);
                    BarcodePopupListview.setAdapter(adapterPopup);

                }
            });
            ItemNo.setText(LIST_OUTPUT_POPUP_NO.get(position).toString());
            ItemBARCODE.setText(LIST_OUTPUT_POPUP_BARCODE.get(position).toString());
            ItemQty.setText(LIST_OUTPUT_POPUP_PICKINGQTY.get(position).toString());
            ItemDelete.setText(LIST_OUTPUT_POPUP_QTY.get(position).toString());

            return rowView;
        }

    }

}