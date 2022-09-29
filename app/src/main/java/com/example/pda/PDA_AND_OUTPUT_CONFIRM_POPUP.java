package com.example.pda;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PDA_AND_OUTPUT_CONFIRM_POPUP extends BaseActivity {

    private Context mContext;
    private String UserId, Language;
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
    public String PopupConfirm;
    private int saveYes;

    private String Date, CustCD;
    private int ListSize, i;

    Connection connect;              //database Connect
    String ConnectionResult = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_popup);

        saveYes = 0;

        Button btn_yes = (Button) findViewById(R.id.btn_yes);
        Button btn_no = (Button) findViewById(R.id.btn_no);
        TextView text = (TextView)findViewById(R.id.text);

        text.setText("저장하시겠습니까?");


        //쿠키에서 UserId 값 받아오기
        //mContext = this;
        //UserId = PreferenceManager.getString(mContext, "UID");

        UserId = ((PDA_AND_MAIN) PDA_AND_MAIN.context).Email;  //사용자 아이디
        Language = ((PDA_AND_LOGIN) PDA_AND_LOGIN.context).language;  //사용자 언어

        Intent getIntent = getIntent();
        StrPN = getIntent.getStringExtra("SPreInputPN");
        SPreInputA = getIntent.getStringExtra("SPreInputA");
        StrPARTNO = getIntent.getStringExtra("StrPARTNO");
        StrPARTNM = getIntent.getStringExtra("StrPARTNM");
        StrOutNumber = getIntent.getStringExtra("StrOutNumber");
        StrOutCust = getIntent.getStringExtra("StrOutCust");
        StrOutDate = getIntent.getStringExtra("StrOutDate");
        ListNameOUTPUT = getIntent.getStringArrayListExtra("NameList");
        ListPickNumberOUTPUT = getIntent.getStringArrayListExtra("PickNumberList");
        ListNumberOUTPUT = getIntent.getStringArrayListExtra("NumberList");
        ListCheckOUTPUT = getIntent.getStringArrayListExtra("CheckList");
        ListPartNoOUTPUT = getIntent.getStringArrayListExtra("PartNo");

        System.out.println("THIS IS STRPN :: " + StrPN);


        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                try {
                    String query5 = "EXEC SP_PDA_WM00030_SAVE2 '" + StrPN + "','" + UserId + "'";
                    System.out.println("EXEC SP_PDA_WM00030_SAVE2 '" + StrPN + "','" + UserId + "'");
                    Statement st5 = connectDB();
                    ResultSet rs5 = st5.executeQuery(query5);
                    System.out.println("_______________________________저장버튼 누르기 중__________________________________");
                    while (rs5.next()) {
                        System.out.println("반환값 : " + rs5.getString(2));
                        if (!rs5.getString(1).isEmpty()) {
                            System.out.println("개별 출고 내용이 저장 됐다면 반환값은 : " + rs5.getString(1));
                        }
                    }

                } catch (Exception e) {
                    //Toast.makeText(getApplicationContext(), "데이터베이스에 안들어갔습니다.", Toast.LENGTH_SHORT).show();
                    System.out.println("~~~~~~~~~~~~~~~~WM00120_SAVE가 안돌아감!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                }


                Intent parentIntent = new Intent(PDA_AND_OUTPUT_CONFIRM_POPUP.this, PDA_AND_OUTPUT.class);
                saveYes = 1;
                parentIntent.putExtra("saveYes", saveYes);
                setResult(2110, parentIntent);
                finish();
                System.out.println("_______________________________저장버튼 누르기 후__________________________________");
            }
        });


        btn_no.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent parentIntent = new Intent(PDA_AND_OUTPUT_CONFIRM_POPUP.this, PDA_AND_OUTPUT.class);
                //setResult(2210, parentIntent);
                finish();
            }
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


}