package com.example.pda;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class Confirm extends BaseActivity {

    Button close;

    ArrayList<String> user_list = new ArrayList<String>();
    TextView text;
    TextView title;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        Intent intent = getIntent();
        String str1 = intent.getStringExtra("str1");


        text = findViewById(R.id.text);
        title = findViewById(R.id.title);

        title.setText("Confirm");

        text.append(" \n"+ str1 +  "\n ");

        close = findViewById(R.id.close);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}


