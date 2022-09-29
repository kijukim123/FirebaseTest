package com.example.pda;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.sql.ResultSet;
import java.sql.Statement;

public class PDA_AND_MOVE_POPUP_N extends BaseActivity {
    String SPreInputR;
    String SPreInputA;
    String StrQTY;
    String UserId,Language;
    Button btn_yes, btn_no;
    LinearLayout focus;
    TextView text;


    String Log_menuID= "PDA_AND_MOVE_PN" ;   // 로그 기록 용 화면 이름
    String Log_action = "BARCODE_MOVE" ;      // 로그 기록 용 action 이름


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_popup);
        main();
        text.setText("해당 품번은 적재할 수 없습니다.\n그래도 적재하시겠습니까?");
        text.setFocusable(true);
        text.requestFocus();

        btn_yes.setOnClickListener(view -> btn_yes());

        btn_no.setOnClickListener((view -> btn_no()));

    }

    void main(){
        Intent intent = getIntent();
        SPreInputR = intent.getStringExtra("RackBarcode");
        SPreInputA = intent.getStringExtra("ABarcode");
        StrQTY = intent.getStringExtra("StrQTY");

        btn_yes = findViewById(R.id.btn_yes);
        btn_no = findViewById(R.id.btn_no);
        focus = findViewById(R.id.focus);
        text = findViewById(R.id.text);


        UserId = ((PDA_AND_MAIN) PDA_AND_MAIN.context).Email;  //사용자 아이디
        Language = ((PDA_AND_LOGIN) PDA_AND_LOGIN.context).language;  //사용자 언어
    }

    public void btn_yes(){
        System.out.println("yes");
        main();

        try{
            main();
            String query5 = "EXEC dbo.SP_PDA_WM00020_SAVE '" + SPreInputR + "','" + SPreInputA + "','" + StrQTY + "','" + UserId +  "','" + Language + "'";
            Statement st5 =connectDB();
            ResultSet rs5 = st5.executeQuery(query5);

            while (rs5.next()){
                if(rs5.getString(2).equals("저장되었습니다.")) {
                    Intent intent = new Intent(PDA_AND_MOVE_POPUP_N.this, PDA_AND_MOVE.class);
                    intent.putExtra("PARTNM", rs5.getString(4));
                    intent.putExtra("BARCODE",SPreInputA);
                    intent.putExtra("QTY", qty(StrQTY).toString());
                    System.out.println("intnet대기");
                    setResult(1, intent);
                    System.out.println("1 저장");
                    finish();
                    regLog(Log_menuID, UserId, Log_action);
                }
            }
        }catch(Exception e){
            //Toast.makeText(getApplicationContext(), "데이터베이스에 안들어갔습니다.", Toast.LENGTH_SHORT).show();
            System.out.println("WM00020_SAVE가 안돌아감!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }

    }
    public void btn_no(){
        Intent intent = new Intent(PDA_AND_MOVE_POPUP_N.this, PDA_AND_MOVE.class);
        setResult(100,intent);
        finish();

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