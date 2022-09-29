package com.example.pda;


import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.sql.SQLException;
import java.sql.Statement;

public class PopupActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        String Email = ((PDA_AND_MAIN) PDA_AND_MAIN.context).Email;  //사용자 아이디
        System.out.println("                " + Email);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_popup);

        Button btn_yes = (Button) findViewById(R.id.btn_yes);
        Button btn_no = (Button) findViewById(R.id.btn_no);
        TextView text = (TextView) findViewById(R.id.text);

        text.setText("로그아웃 하시겠습니까?");

        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Statement logoutst= connectDB();
                String logoutquery = regLogs("PDA_AND_LOGOUT",Email,"LOGOUT");
                try {
                    logoutst.executeUpdate(logoutquery);
                } catch (SQLException e) {}

                removekey("ID");
                removekey("NAME");
                removekey("CODE");
                removekey("DATE");
                setResult(500);
                finish();
            }
        });

        btn_no.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(5000);
                finish();
            }
        }));
    }


    //바깥 레이어 클릭 시 안닫히게
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

    //안드로이드 백버튼 막기
    @Override
    public void onBackPressed(){
        return;
    }

}