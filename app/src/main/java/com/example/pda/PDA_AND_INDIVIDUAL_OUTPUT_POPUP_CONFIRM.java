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

public class PDA_AND_INDIVIDUAL_OUTPUT_POPUP_CONFIRM extends BaseActivity {

    private Context mContext;
    private String UserId;
    public ArrayList<String> ListNameIndividual = null;
    public ArrayList<String> ListRackIndividual = null;
    public ArrayList<String> ListQtyIndividual = null;
    public ArrayList<String> ListOutqtyIndividual = null;
    public ArrayList<String> ListBarcodeIndividual = null;
    public ArrayList<String> ListCancelIndividual = null;
    public String PopupConfirm,Language;

    private String Date, CustCD;
    private int ListSize, i;

    String Log_menuID= "PDA_AND_INDIVIDUAL_OUTPUT" ;   // 로그 기록 용 화면 이름
    String Log_action = "OUTPUT" ;      // 로그 기록 용 action 이름


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_popup);


        Button btn_yes = (Button) findViewById(R.id.btn_yes);
        Button btn_no = (Button) findViewById(R.id.btn_no);
        TextView text = (TextView)findViewById(R.id.text);

        text.setText("저장하시겠습니까?");

        UserId = ((PDA_AND_MAIN) PDA_AND_MAIN.context).Email;  //사용자 아이디
        Language = ((PDA_AND_LOGIN) PDA_AND_LOGIN.context).language;  //사용자 언어

        Intent intent = getIntent();
        ListNameIndividual = getIntent().getStringArrayListExtra("NameList");
        ListRackIndividual = getIntent().getStringArrayListExtra("RackList");
        ListQtyIndividual = getIntent().getStringArrayListExtra("QtyList");
        ListOutqtyIndividual = getIntent().getStringArrayListExtra("OutqtyList");
        ListBarcodeIndividual = getIntent().getStringArrayListExtra("BarcodeList");
        ListCancelIndividual = getIntent().getStringArrayListExtra("CancelList");
        Date = getIntent().getStringExtra("Date");
        CustCD = getIntent().getStringExtra("CustCD");
        ListSize = ListBarcodeIndividual.size();
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println(ListOutqtyIndividual);
        System.out.println(ListBarcodeIndividual);
        System.out.println(Date);
        System.out.println(CustCD);
        System.out.println(ListSize);
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");


        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                for (i=0;i<= ListSize-1; i++) {
                    System.out.println("_______________________________저장버튼 누르기 전__________________________________");
                    System.out.println(i);
                    System.out.println(ListBarcodeIndividual.get(i));
                    System.out.println(ListOutqtyIndividual.get(i));
                    try {
                        String query5 = "EXEC SP_PDA_WM00120_SAVE '" + Date + "','" + CustCD + "','" + ListBarcodeIndividual.get(i) + "','" + ListOutqtyIndividual.get(i) + "','" + UserId + "'";
                        System.out.println("EXEC SP_PDA_WM00120_SAVE '" + Date + "','" + CustCD + "','" + ListBarcodeIndividual.get(i) + "','" + ListOutqtyIndividual.get(i) + "','" + UserId + "'");
                        Statement st5 = connectDB();
                        ResultSet rs5 = st5.executeQuery(query5);
                        regLog(Log_menuID, UserId, Log_action);
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
                }
                setResult(2);
                finish();
                System.out.println("_______________________________저장버튼 누르기 후__________________________________");
            }
        });



        btn_no.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupConfirm = "1";
                /*
                Intent parentIntent = new Intent(getApplicationContext(), PDA_AND_INDIVIDUAL_OUTPUT.class);
                ArrayList<String> List_NAME_INDIVIDUAL = new ArrayList<>(ListNameIndividual);
                parentIntent.putExtra("NameList", List_NAME_INDIVIDUAL);
                ArrayList<String> List_RACK_INDIVIDUAL = new ArrayList<>(ListRackIndividual);
                parentIntent.putExtra("RackList", List_RACK_INDIVIDUAL);
                ArrayList<String> List_QTY_INDIVIDUAL = new ArrayList<>(ListQtyIndividual);
                parentIntent.putExtra("QtyList", List_QTY_INDIVIDUAL);
                ArrayList<String> List_OUTQTY_INDIVIDUAL = new ArrayList<>(ListOutqtyIndividual);
                parentIntent.putExtra("OutqtyList", List_OUTQTY_INDIVIDUAL);
                ArrayList<String> List_BARCODE_INDIVIDUAL = new ArrayList<>(ListBarcodeIndividual);
                parentIntent.putExtra("BarcodeList", List_BARCODE_INDIVIDUAL);
                ArrayList<String> List_CANCEL_INDIVIDUAL = new ArrayList<>(ListCancelIndividual);
                parentIntent.putExtra("CancelList", List_CANCEL_INDIVIDUAL);
                parentIntent.putExtra("PopupConfirm", PopupConfirm);
                startActivity(parentIntent);
                 */
                setResult(100);
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