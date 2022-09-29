package com.example.pda;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class PDA_AND_MAIN extends BaseActivity {

    public static Context context;
    public static Activity _Main_Activity;


    ImageButton back;
    ImageButton logout;
    String Email;

    //화면
    Button legacy;
    Button merge;
    Button multiple_merge;
    Button div;
    Button lookup;
    Button lookup_lack;
    Button barcode_check;
    Button btn_barcode_input;
    Button btn_barcode_move;
    Button barcode_output;
    Button barcode_output_cancel;
    Button individual_output;

    String Log_menuID= "PDA_MENU" ;   // 로그 기록 용 화면 이름
    String Log_action = "LOGOUT" ;      // 로그 기록 용 action 이름

    ImageButton info;

    Toast toast=null;
    long backKeyPressedTime = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pda_and_main);

        context = this;
        _Main_Activity = PDA_AND_MAIN.this;

        /********** 화면 이동 *******************/
        //레거시 바코드 발행
        legacy = findViewById(R.id.legacy_barcode);
        legacy.setOnClickListener(view -> moveActivity(PDA_AND_LEGACY.class));

        //바코드 입고
        btn_barcode_input = findViewById(R.id.btn_barcode_input);
        btn_barcode_input.setOnClickListener(view -> moveActivity(PDA_AND_INPUT.class));

        //바코드 이동
        btn_barcode_move=findViewById((R.id.btn_barcode_move));
        btn_barcode_move.setOnClickListener(view -> moveActivity(PDA_AND_MOVE.class));

        //바코드 개별 출고
        individual_output = findViewById(R.id.btn_individual_output);
        individual_output.setOnClickListener(v -> moveActivity(PDA_AND_INDIVIDUAL_OUTPUT.class));

        //바코드 출고
        barcode_output = findViewById(R.id.btn_barcode_output);
        barcode_output.setOnClickListener(v -> moveActivity(PDA_AND_OUTPUT.class));

        //바코드 출고 취소
        barcode_output_cancel = findViewById(R.id.btn_barcode_output_cancel);
        barcode_output_cancel.setOnClickListener(v -> moveActivity(PDA_AND_OUTPUT_CANCEL.class));

        //바코드 병합
        merge = findViewById(R.id.bar_merge);
        merge.setOnClickListener(v -> moveActivity(PDA_AND_MERGE.class));

        //바코드 다중 병합
        multiple_merge = findViewById(R.id.bar_mul_merge);
        multiple_merge.setOnClickListener(v -> moveActivity(PDA_AND_MULTI_MERGE.class));

        //바코드 분할
        div = findViewById(R.id.bar_div);
        div.setOnClickListener(v -> moveActivity(PDA_AND_DIVISION.class));

        //바코드 조회
        lookup = findViewById(R.id.lookup_barcode);
        lookup.setOnClickListener(v -> moveActivity(PDA_AND_LOOKUP_B.class));

        //랙 조회
        lookup_lack = findViewById(R.id.lookup_lack);
        lookup_lack.setOnClickListener(v -> moveActivity(PDA_AND_LOOKUP_L.class));


       //바코드 실사
        barcode_check = findViewById(R.id.barcode_check);
        barcode_check.setOnClickListener(v -> moveActivity(PDA_AND_CHECK.class));




/******* LoginActivity에서 넘긴 userid  ************/
        // id = findViewById(R.id.id);
        Intent intent = getIntent();
        Email = intent.getStringExtra("str1");

        System.out.println("****************************" + Email);



        info = findViewById(R.id.info);
        logout = findViewById(R.id.logout);

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveActivity(UserInfo.class,Email);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PDA_AND_MAIN.this, PopupActivity.class);
                startActivityForResult(intent,1);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 500)
        {
           finish();
        }
    }
    //안드로이드 백버튼 막기
    @Override
    public void onBackPressed(){
        if(System.currentTimeMillis() > backKeyPressedTime + 2000){
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime+2000){
            finishAffinity();
            System.runFinalization();
            System.exit(0);
            toast.cancel();
        }
    }


}