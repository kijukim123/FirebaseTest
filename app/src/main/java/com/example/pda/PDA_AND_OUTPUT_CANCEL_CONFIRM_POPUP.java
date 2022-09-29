package com.example.pda;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TextView;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PDA_AND_OUTPUT_CANCEL_CONFIRM_POPUP extends BaseActivity {

    private Context mContext;
    private String UserId;
    public ArrayList<String> ListPartnoOutCancel = null;
    public ArrayList<String> ListPartnmOutCancel = null;
    public ArrayList<String> ListQtyOutCancel = null;
    public ArrayList<String> ListBarcodeOutCancel = null;
    private String StrOutNumber, StrOutCancelPlace, SPreInputR,N;
    private List<String> List_PARTNO_OUT_CANCEL = new ArrayList<>();
    private List<String> List_PARTNM_OUT_CANCEL = new ArrayList<>();
    private List<String> List_QTY_OUT_CANCEL = new ArrayList<>();
    private List<String> List_BARCODE_OUT_CANCEL = new ArrayList<>();
    public String Language;
    private String Date, CustCD;
    private int ListSize, i;
    TextView text;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_popup);


        Button btn_yes = (Button) findViewById(R.id.btn_yes);
        Button btn_no = (Button) findViewById(R.id.btn_no);
        text = findViewById(R.id.text);

        UserId = ((PDA_AND_MAIN) PDA_AND_MAIN.context).Email;  //사용자 아이디
        Language = ((PDA_AND_LOGIN) PDA_AND_LOGIN.context).language;  //사용자 언어


        Intent getIntent = getIntent();
        //출고번호, 랙 바코드 받기
        StrOutNumber = getIntent().getStringExtra("StrOutNumber");
        SPreInputR = getIntent().getStringExtra("SPreInputR");
        N = getIntent.getStringExtra("N");

        if(N.equals("N")) text.setText("해당 랙에 적재할 수 없는 품번이 존재합니다. \n그래도 적재하시겠습니까?");
        else text.setText("출고취소 하시겠습니까?");

       //받은 Intent 확인
        System.out.println("from here---------------------받은 Intent 확인----------------------");
        System.out.println(StrOutNumber);
        System.out.println(StrOutCancelPlace);
        System.out.println(SPreInputR);
        System.out.println(ListPartnoOutCancel);
        System.out.println(ListPartnmOutCancel);
        System.out.println(ListQtyOutCancel);
        System.out.println(ListBarcodeOutCancel);
        System.out.println("to here---------------------받은 Intent 확인----------------------");



        btn_yes.setOnClickListener(view -> yes());


        btn_no.setOnClickListener((view -> {
            setResult(100);
            finish();
        }));




    }

    void yes(){
        System.out.println("_______________________________저장버튼 누르기 전__________________________________");
        try {
            String query5 = "EXEC SP_PDA_WM00090_SAVE '" + StrOutNumber + "','"+ SPreInputR + "','" + UserId + "'";
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
            System.out.println("~~~~~~~~~~~~~~~~SP_PDA_WM00090_SAVE 안돌아감!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }

        Intent intent1 = new Intent(getApplicationContext(), PDA_AND_OUTPUT_CANCEL.class);
        setResult(1);
        finish();
        System.out.println("_______________________________저장버튼 누르기 후__________________________________");
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