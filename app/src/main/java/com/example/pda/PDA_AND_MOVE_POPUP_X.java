package com.example.pda;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.ResultSet;
import java.sql.Statement;

public class PDA_AND_MOVE_POPUP_X extends BaseActivity {
    String SPreInputR;
    String SPreInputA;
    String available;
    TextView BARCODE, PARTNO, PARTNM, QTY;
    String StrQTY;
    String UserId, Language;
    EditText MoveQTY;
    Button btn_save;
    Button btn_cancel;
    String Log_menuID= "PDA_AND_MOVE_PX" ;   // 로그 기록 용 화면 이름
    String Log_action = "BARCODE_MOVE" ;      // 로그 기록 용 action 이름


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pda_and_move_popup_x);

        main();
        MoveQTY.setText("");
        LinearLayout focus = findViewById(R.id.focus);
        focus.requestFocus();

        Intent intent = getIntent();

        available = intent.getStringExtra("available");
        SPreInputR = intent.getStringExtra("RackBarcode");
        SPreInputA = intent.getStringExtra("ABarcode");

        main();
        content();
        btn_save.setOnClickListener(view -> save());

        btn_cancel.setOnClickListener(v -> {
            Intent intent1 = new Intent(PDA_AND_MOVE_POPUP_X.this, PDA_AND_MOVE.class);
            setResult(100, intent1);
            finish();
        });


    }

    void main(){

        UserId = ((PDA_AND_MAIN) PDA_AND_MAIN.context).Email;  //사용자 아이디
        Language = ((PDA_AND_LOGIN) PDA_AND_LOGIN.context).language;  //사용자 언어

        btn_save = (Button) findViewById(R.id.btn_move_save);
        btn_cancel = (Button) findViewById(R.id.btn_move_cancel);

        BARCODE = findViewById(R.id.tv_move_barcode);
        PARTNO = findViewById(R.id.tv_move_partno);
        PARTNM = findViewById(R.id.tv_move_partnm);
        QTY = findViewById(R.id.tv_move_qty);
        MoveQTY = findViewById(R.id.et_move_moveqty);
    }
    void save(){
        main();
        System.out.println("MOVEQTY IS :: " + MoveQTY.getText().toString());
        if(MoveQTY.getText().toString().equals("")){
            System.out.println("MOVEQTY IS NULL");
            StrQTY = MoveQTY.getText().toString();
        }else{
            //EditText에 있는 이동 수량을 가져와서 StringQTY에 넣어준다.
            StrQTY = MoveQTY.getText().toString();
        }


        if(available.equals("N")){
            if (StrQTY.equals("0") || StrQTY.equals("")){
                System.out.println("-----------THIS IS INTQTY CHECK POINT----------");
                Toast.makeText(getApplicationContext(), "0개 이상의 이동수량을 입력해 주세요.", Toast.LENGTH_SHORT).show();
            }

            else if (strToInt(StrQTY) > strToInt(QTY.getText().toString())){
                Toast.makeText(getApplicationContext(), "이동수량은(는) 수량 보다 작아야 합니다.", Toast.LENGTH_SHORT).show();
            }
            else {
                Intent intent2 = new Intent(PDA_AND_MOVE_POPUP_X.this, PDA_AND_MOVE.class);
                intent2.putExtra("RackBarcode", SPreInputR);
                intent2.putExtra("ABarcode", SPreInputA);
                intent2.putExtra("StrQTY", StrQTY);
                setResult(5, intent2);
                finish();
            }
        }

        else{
            //int IntQTY = Integer.parseInt(StrQTY);
            if (StrQTY.equals("0") || StrQTY.equals("")){
                System.out.println("-----------THIS IS INTQTY CHECK POINT----------");
                Toast.makeText(getApplicationContext(), "0개 이상의 이동수량을 입력해 주세요.", Toast.LENGTH_SHORT).show();
            }
            else{
                try{
                    String query2 = "EXEC dbo.SP_PDA_WM00020_SAVE '" + SPreInputR + "','" + SPreInputA + "','" + StrQTY + "','" + UserId +  "','"+Language +"'";
                    Statement st2 = connectDB();
                    ResultSet rs2 = st2.executeQuery(query2);

                    while (rs2.next()){
                        if(rs2.getString(2).equals("저장되었습니다.")) {
                            Intent intent = new Intent(PDA_AND_MOVE_POPUP_X.this, PDA_AND_MOVE.class);
                            intent.putExtra("PARTNM", PARTNM.getText().toString());
                            intent.putExtra("BARCODE", BARCODE.getText().toString());
                            intent.putExtra("QTY", StrQTY);
                            setResult(0, intent);
                            System.out.println(PARTNM.getText().toString());
                            System.out.println(BARCODE.getText().toString());
                            System.out.println(StrQTY);
                            finish();
                            regLog(Log_menuID, UserId, Log_action);
                        }
                    }
                }catch(Exception e){
                    //Toast.makeText(getApplicationContext(), "데이터베이스에 안들어갔습니다.", Toast.LENGTH_SHORT).show();
                    System.out.println("WM00020_SAVE가 안돌아감!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                }
            }
        }
    }
    void content(){
        main();
        try{
            String query1 = "EXEC dbo.SP_PDA_WM00020P1_INQUERY '" + SPreInputA + "','" + SPreInputR + "','" + Language +"'";
            Statement st1 = connectDB();
            ResultSet rs1 = st1.executeQuery(query1);

            while (rs1.next()){
                System.out.println("반환값 : " + rs1.getString(1));
                if(!rs1.getString(1).isEmpty()) {
                    BARCODE.setText(rs1.getString(1));
                    PARTNO.setText(rs1.getString(2));
                    PARTNM.setText(rs1.getString(3));
                    QTY.setText(qty(rs1.getString(4)).toString());
                }
            }
        }catch(Exception e){
            //Toast.makeText(getApplicationContext(), "데이터베이스에 안들어갔습니다.", Toast.LENGTH_SHORT).show();
            System.out.println("WM00020_SAVE_INQUERY가 안돌아감!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
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