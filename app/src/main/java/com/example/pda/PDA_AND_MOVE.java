package com.example.pda;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.m3.sdk.scannerlib.Barcode;
import com.m3.sdk.scannerlib.BarcodeListener;
import com.m3.sdk.scannerlib.BarcodeManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class PDA_AND_MOVE extends BaseActivity {

    TextView TvMoveLocation;
    EditText EtPreMove;
    Button btnReset;
    List<String> LIST_Name_Move = new ArrayList<>();
    List<String> LIST_Number_Move = new ArrayList<>();
    List<String> LIST_Barcode_Move = new ArrayList<>();
    String SPreInput;
    String SPreInputR;
    String SPreInputA;
    String UserId, Language;
    TextView Alert1;
    TextView Alert2;
    ListView BarcodeMoveListview;
    String StrQTY;
    String RACK, BARCODE, PARTNM, QTY;


    // lib
    Barcode mBarcode = null;
    BarcodeListener mListener = null;
    BarcodeManager mManager = null;
    Barcode.Symbology mSymbology = null;

    String Log_menuID= "PDA_AND_MOVE" ;   // 로그 기록 용 화면 이름
    String Log_action = "BARCODE_MOVE" ;      // 로그 기록 용 action 이름

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pda_and_move);

        UserId = ((PDA_AND_MAIN) PDA_AND_MAIN.context).Email;  //사용자 아이디
        Language = ((PDA_AND_LOGIN) PDA_AND_LOGIN.context).language;  //사용자 언어

        main();

        mBarcode = new Barcode(this);
        mManager = new BarcodeManager(this);
        mSymbology = mBarcode.getSymbologyInstance();

        mListener = new BarcodeListener() {
            @Override
            public void onBarcode(String s) {
                SPreInput = s.trim();
                scan();
            }

            @Override
            public void onBarcode(String s, String s1) {

            }

            @Override
            public void onGetSymbology(int i, int i1) {

            }
        };
        mManager.addListener(mListener);
        btnReset.setOnClickListener(v -> clear());


    }


    /************팝업 종료 후 호출 *************/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        System.out.println("****************************"+resultCode);
        main();
        //X경우 - 병합바코드에서 랙 위치 이동했을 경우 리스트뷰에 띄워주기
        if (resultCode == 0 || resultCode == 1) {
            BARCODE = data.getStringExtra("BARCODE");
            PARTNM = data.getStringExtra("PARTNM");
            QTY = data.getStringExtra("QTY");

            LIST_Barcode_Move.add(BARCODE);
            LIST_Name_Move.add(PARTNM);
            LIST_Number_Move.add(QTY);
            Alert1.setText(LIST_Barcode_Move.size() + " Scanned");
            Alert2.setTextColor(Color.BLACK);
            Alert2.setText("이동 처리가 완료되었습니다.");
            CustomListBarcodeMove adapterInput = new CustomListBarcodeMove(PDA_AND_MOVE.this);
            BarcodeMoveListview = (ListView) findViewById(R.id.listview_barcode_move);
            BarcodeMoveListview.setAdapter(adapterInput);
        }
        //병합 품번 & 적재 불가능
        if(resultCode ==5){

            RACK = data.getStringExtra("RackBarcode");
            BARCODE = data.getStringExtra("ABarcode");
            QTY = data.getStringExtra("StrQTY");

            Intent intent = new Intent(getApplicationContext(), PDA_AND_MOVE_POPUP_N.class);
            intent.putExtra("RackBarcode", RACK);
            intent.putExtra("ABarcode", BARCODE);
            intent.putExtra("StrQTY", QTY);
            startActivityForResult(intent,1);
        }

        if (resultCode == 500){
            PDA_AND_MAIN MA = (PDA_AND_MAIN)PDA_AND_MAIN._Main_Activity;
            MA.finish();
            finish();
        }

    }

    void scan() {
        main();
        try {  //렉바코드 데이터베이스 조회
            //바코드 타입 조회
            String query4 = "EXEC SP_PDA_COMMON_BARCODE_TYPE '" + SPreInput + "','" + Language + "'";
            Statement st4 = connectDB();
            ResultSet rs4 = st4.executeQuery(query4);
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!데이터베이스 연결만 됐을 경우!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            while (rs4.next()) {
                if (rs4.getString(1).equals("F")) {
                    Alert2.setText(rs4.getString(3));
                    Alert2.setTextColor(Color.RED);
                } else if (rs4.getString(1).equals("T")) {
                    //랙 바코드
                    if (rs4.getString(2).equals("RACK") && TvMoveLocation.getText().equals("")) {
                        SPreInputR = SPreInput;
                        TvMoveLocation.setText(rs4.getString(5));
                        LIST_Name_Move.clear();
                        LIST_Number_Move.clear();
                        LIST_Barcode_Move.clear();
                        CustomListBarcodeMove adapterInput = new CustomListBarcodeMove(PDA_AND_MOVE.this);
                        BarcodeMoveListview = (ListView) findViewById(R.id.listview_barcode_move);
                        BarcodeMoveListview.setAdapter(adapterInput);
                        EtPreMove.setText("");
                        Alert2.setTextColor(Color.BLACK);
                        Alert2.setText("상품 바코드 을(를) 스캔하세요");
                    }
                    else if(rs4.getString(2).equals("RACK") && !TvMoveLocation.getText().equals("")){
                        Alert1.setText("0 Scanned");
                        SPreInputR = SPreInput;
                        TvMoveLocation.setText(rs4.getString(5));
                        LIST_Name_Move.clear();
                        LIST_Number_Move.clear();
                        LIST_Barcode_Move.clear();
                        CustomListBarcodeMove adapterInput = new CustomListBarcodeMove(PDA_AND_MOVE.this);
                        BarcodeMoveListview = (ListView) findViewById(R.id.listview_barcode_move);
                        BarcodeMoveListview.setAdapter(adapterInput);
                        EtPreMove.setText("");
                        Alert2.setTextColor(Color.BLACK);
                        Alert2.setText("상품 바코드 을(를) 스캔하세요");
                    }
                    else if (rs4.getString(2).equals("PART")&& !TvMoveLocation.getText().equals("")) {
                        SPreInputA = SPreInput;

                        try {
                            //입고처리 프로시저 돌리기
                            String query = "EXEC dbo.SP_PDA_WM00020_SAVE_VALIDATION '" + SPreInputR + "','" + SPreInputA + "','" + UserId +  "','" + Language + "'";
                            Statement st = connectDB();
                            ResultSet rs = st.executeQuery(query);
                            while(rs.next()){

                                switch (rs.getString(1)) {
                                    case "T" :
                                        StrQTY = rs.getString(5);

                                        String query5 = "EXEC dbo.SP_PDA_WM00020_SAVE '" + SPreInputR + "','" + SPreInputA + "','" + StrQTY + "','" + UserId + "','" + Language + "'";
                                        Statement st5 = connectDB();
                                        ResultSet rs5 = st5.executeQuery(query5);

                                        while (rs5.next()) {
                                            if (rs5.getString(2).equals("저장되었습니다.")) {
                                                LIST_Barcode_Move.add(SPreInputA);
                                                LIST_Name_Move.add(rs5.getString(4));
                                                LIST_Number_Move.add(qty(StrQTY).toString());
                                                Alert1.setText(LIST_Barcode_Move.size() + " Records To Display ...");
                                                Alert2.setTextColor(Color.BLACK);
                                                Alert2.setText("이동 처리가 완료되었습니다.");
                                                CustomListBarcodeMove adapterInput = new CustomListBarcodeMove(PDA_AND_MOVE.this);
                                                BarcodeMoveListview = (ListView) findViewById(R.id.listview_barcode_move);
                                                BarcodeMoveListview.setAdapter(adapterInput);
                                                regLog(Log_menuID, UserId, Log_action);
                                            }
                                        }
                                        break;

                                    case "F" :
                                        Alert2.setTextColor(Color.RED);
                                        Alert2.setText(rs.getString(2));

                                        break;

                                    case "N" :
                                        StrQTY = rs.getString(5);
                                        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!입고가 제한된 바코드입니다.!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                                        Intent intent = new Intent(getApplicationContext(), PDA_AND_MOVE_POPUP_N.class);
                                        intent.putExtra("RackBarcode", SPreInputR);
                                        intent.putExtra("ABarcode", SPreInputA);
                                        intent.putExtra("StrQTY", StrQTY);
                                        startActivityForResult(intent, 1);

                                        break;

                                    case "X" :
                                        String available = available_load(SPreInputR,SPreInputA);

                                        intent = new Intent(getApplicationContext(), PDA_AND_MOVE_POPUP_X.class);
                                        intent.putExtra("available",available);
                                        intent.putExtra("RackBarcode", SPreInputR);
                                        intent.putExtra("ABarcode", SPreInputA);
                                        startActivityForResult(intent, 0);

                                        break;


                                }

                            }

                        } catch (Exception e) {
                            System.out.println("WM00020_SAVE가 안돌아감!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                        }

                    }else if(rs4.getString(2).equals("PART")&& TvMoveLocation.getText().equals("")){
                        //Alert Setting
                        Alert1.setText("0 Scanned");
                        Alert2.setText("입고위치를 스캔하세요");
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("데이터베이스에 안들어갔습니다.");
        }
    }

    //적재 가능한지 확인
    String available_load(String SPreInputR, String SPreInputA){
        String available = "Y";
        try {
            String l_query = "SELECT RACKCD\n" +
                    "FROM TB_WM_RACK_MNGR_PART A LEFT JOIN TB_WM_BCOD B\n" +
                    "ON  A.PARTNO = B.PARTNO\n" +
                    "WHERE A.RACKCD = '" +SPreInputR+ "' AND B.BARCODE = '" +SPreInputA+ "'";

            Statement l_st = connectDB();
            ResultSet l_rs = l_st.executeQuery(l_query);

            if(!l_rs.next()){ //적재 불가능
                available = "N";
            }
        }catch (SQLException e){}
        return available;
    }
    //바코드이동 관련 어뎁터
    public class CustomListBarcodeMove extends ArrayAdapter<String> {
        private final Activity context;

        public CustomListBarcodeMove(Activity context) {
            super(context, R.layout.listitemrow_barcode_move, LIST_Name_Move);
            this.context = context;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.listitemrow_barcode_move, null, true);
            TextView ItemName = (TextView) rowView.findViewById(R.id.move_item_name);
            TextView ItemNumber = (TextView) rowView.findViewById(R.id.move_item_number);
            TextView ItemBarcode = (TextView) rowView.findViewById(R.id.move_item_barcode);
            //Button approve = (Button) rowView.findViewById(R.id.approve_user);
            ItemName.setText(LIST_Name_Move.get(position));
            ItemNumber.setText(LIST_Number_Move.get(position));
            ItemBarcode.setText(LIST_Barcode_Move.get(position));
            //approve.setText("승인");

            return rowView;
        }

    }

    void main(){
        Alert1 = findViewById(R.id.barcode_move_alert1);
        Alert2 = findViewById(R.id.barcode_move_alert2);
        TvMoveLocation = findViewById(R.id.tv_move_location);
        EtPreMove = findViewById(R.id.et_pre_move);
        btnReset = findViewById(R.id.button_move);

        /*********뒤로가기, 로그아웃 **********/
        ImageButton back = findViewById(R.id.back);
        ImageButton logout = findViewById(R.id.logout);
        mainbtn(back, logout, UserId);
    }

    void clear(){
        main();
        LIST_Name_Move.clear();
        LIST_Number_Move.clear();
        LIST_Barcode_Move.clear();
        CustomListBarcodeMove adapterInput = new CustomListBarcodeMove(PDA_AND_MOVE.this);
        BarcodeMoveListview = (ListView) findViewById(R.id.listview_barcode_move);
        BarcodeMoveListview.setAdapter(adapterInput);
        TvMoveLocation.setText("");
        EtPreMove.setText("");
        Alert1.setText("0 Scanned");
        Alert2.setText("입고위치를 스캔하세요");
        Alert2.setTextColor(Color.BLACK);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mManager.dismiss();
    }
}


