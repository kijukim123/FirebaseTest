package com.example.pda;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class PDA_AND_CHECK_POPUP extends BaseActivity {

    String Log_menuID= "PDA_AND_RACK_CLEAR" ;   // 로그 기록 용 화면 이름
    String Log_action = "Rack_Clear" ;      // 로그 기록 용 action 이름
    String Email = ((PDA_AND_MAIN) PDA_AND_MAIN.context).Email;  //사용자 아이디

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_popup);

        TextView str_confirm = (TextView) findViewById(R.id.text);
        str_confirm.setText("선택한 랙을 초기화 하시겠습니까?");
        Button btn_yes = (Button) findViewById(R.id.btn_yes);
        Button btn_no = (Button) findViewById(R.id.btn_no);

        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PDA_AND_CHECK_POPUP.this, PDA_AND_CHECK.class);
                setResult(0, intent);
                regLog(Log_menuID,Email,Log_action);

                finish();
            }
        });


        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PDA_AND_CHECK_POPUP.this, PDA_AND_CHECK.class);
                finish();
            }
        });
    }


}