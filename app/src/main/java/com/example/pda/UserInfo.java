package com.example.pda;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class UserInfo extends BaseActivity {

    Button close;

    ArrayList<String> user_list = new ArrayList<String>();
    TextView info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        info = findViewById(R.id.text);

        info.append("\n");
        info.append("ID: " + getSettingItem("ID"));
        info.append("\n");
        info.append("이름: " + getSettingItem("NAME"));
        info.append("\n");



        close = findViewById(R.id.close);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}


