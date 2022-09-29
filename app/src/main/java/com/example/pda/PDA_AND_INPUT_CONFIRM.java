package com.example.pda;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.m3.sdk.scannerlib.Barcode;
import com.m3.sdk.scannerlib.BarcodeListener;
import com.m3.sdk.scannerlib.BarcodeManager;

import org.w3c.dom.Text;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class PDA_AND_INPUT_CONFIRM extends BaseActivity {

    String Log_menuID = "PDA_AND_INPUT";   // 로그 기록 용 화면 이름
    String Log_action = "BARCODE_INPUT";      // 로그 기록 용 action 이름

    String SPreInputR, SPreInputA;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_popup);

        Intent intent = getIntent();
        SPreInputR = intent.getStringExtra("RBarcode");
        SPreInputA = intent.getStringExtra("PBarcode");


        Button btn_yes = (Button) findViewById(R.id.btn_yes);
        Button btn_no = (Button) findViewById(R.id.btn_no);
        TextView text = findViewById(R.id.text);
        text.setText("해당 품번은 적재할 수 없습니다. \n그래도 적재하시겠습니까?");

        text.setFocusable(true);
        text.requestFocus();


        btn_no.setOnClickListener(v -> {
            setResult(100);
            finish();
        });

        btn_yes.setOnClickListener(v -> {
            Intent putintent = new Intent(this, PDA_AND_INPUT.class);
            putintent.putExtra("SPreInputR",SPreInputR);
            putintent.putExtra("SPreInputA",SPreInputA);
            setResult(1, putintent);
            finish();
        });
    }



};