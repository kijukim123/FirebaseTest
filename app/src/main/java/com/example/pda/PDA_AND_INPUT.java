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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.m3.sdk.scannerlib.Barcode;
import com.m3.sdk.scannerlib.BarcodeListener;
import com.m3.sdk.scannerlib.BarcodeManager;


public class PDA_AND_INPUT extends BaseActivity {

    Button btn_input;
    Button btn_cancel;
    Button ResetButton;
    ImageButton back;
    ImageButton logout;
    List<String> LIST_Name_Input = new ArrayList<>();
    List<String> LIST_Number_Input = new ArrayList<>();
    List<String> LIST_Barcode_Input = new ArrayList<>();
    List<String> LIST_Name_Cancel = new ArrayList<>();
    List<String> LIST_Number_Cancel = new ArrayList<>();
    List<String> LIST_Barcode_Cancel = new ArrayList<>();
    ListView BarcodeInputListview;
    ListView BarcodeCancelListview;
    TextView LocationBarcodeInput;
    EditText EtPreInput;
    String SPreInput;
    String SPreInputR;
    String SPreInputA;
    String RgBtn1Click;
    String RgBtn2Click;
    TextView Alert1;
    TextView Alert2;
    String UserId, Language;
    LinearLayout visible;

    // lib
    Barcode mBarcode = null;
    BarcodeListener mListener = null;
    BarcodeManager mManager = null;
    Barcode.Symbology mSymbology = null;

    String Log_menuID = "PDA_AND_INPUT";   // 로그 기록 용 화면 이름
    String Log_action = "BARCODE_INPUT";      // 로그 기록 용 action 이름

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pda_and_input);

        //main() 메소드 호출
        main();
        //input() 메소드 호출
        input();
        //버튼 클릭 이벤트시에만 색깔 나오고, 아닐경우 회색표시
        btn_input.setBackgroundColor(R.drawable.colors);
        btn_cancel.setBackgroundColor(Color.LTGRAY);


        UserId = ((PDA_AND_MAIN) PDA_AND_MAIN.context).Email;  //사용자 아이디
        Language = ((PDA_AND_LOGIN) PDA_AND_LOGIN.context).language;  //사용자 언어


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


        //초기화 버튼
        ResetButton.setOnClickListener(v -> reset());

        //LocationBarcodeInput 텍스트 뷰 setFilters
        LocationBarcodeInput.setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (LocationBarcodeInput != null && LocationBarcodeInput.length() > 1) {
                    //String SPreInput을 초기화 (랙위치 텍스트뷰에 랙위치값이 있으면)
                    SPreInput.replace(SPreInput, "");
                    System.out.println(SPreInput);
                }
                return source;
            }
        }, new InputFilter.LengthFilter(20)});


        //바코드 입고 버튼
        btn_input.setOnTouchListener((v, event) -> {
            //Alert1 초기화
            Alert1.setText("0 Scanned");
            //버튼 클릭 이벤트시에만 색깔 나오고, 아닐경우 회색표시
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                btn_input.setBackgroundColor(Color.LTGRAY);
                visible.setVisibility(View.VISIBLE);
                input();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                btn_input.setBackgroundColor(R.drawable.colors);
                btn_cancel.setBackgroundColor(Color.LTGRAY);
                visible.setVisibility(View.VISIBLE);

            }
            return false;
        });


        //바코드 출고 버튼
        btn_cancel.setOnTouchListener((v, event) -> {
            //버튼 클릭 이벤트시에만 색깔 나오고, 아닐경우 회색표시
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                btn_cancel.setBackgroundColor(Color.LTGRAY);
                visible.setVisibility(View.GONE);
                cancel();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                btn_cancel.setBackgroundColor(R.drawable.colors);
                btn_input.setBackgroundColor(Color.LTGRAY);
                visible.setVisibility(View.GONE);

            }
            return false;
        });


    }

    /********* 팝업 종료 후 호출되는 코드  ***********/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        main();

        if (resultCode == 1)
        {
            SPreInputR = data.getStringExtra("SPreInputR");
            SPreInputA = data.getStringExtra("SPreInputA");

            try {
                String query3 = "EXEC dbo.SP_PDA_WM00010_SAVE '" + SPreInputR + "','" + SPreInputA + "','" + UserId + "','" + Language + "'";
                System.out.println(query3);
                Statement st3 = connectDB();
                ResultSet rs3 = st3.executeQuery(query3);

                while (rs3.next()) {
                    if (!rs3.getString(1).isEmpty()) {
                        LIST_Barcode_Input.add(rs3.getString(1));
                        LIST_Name_Input.add(rs3.getString(3));
                        LIST_Number_Input.add(qty(rs3.getString(4)));
                        Alert1.setText(LIST_Barcode_Input.size() + " Records To Display ...");
                        Alert2.setTextColor(Color.BLACK);
                        Alert2.setText("입고 처리가 완료되었습니다.");
                        CustomListBarcodeInput adapterInput = new CustomListBarcodeInput(PDA_AND_INPUT.this);
                        BarcodeInputListview = (ListView) findViewById(R.id.listview_barcode_input);
                        BarcodeInputListview.setAdapter(adapterInput);
                        regLog(Log_menuID, UserId, Log_action);
                    }
                }
            } catch (SQLException e) {
            }
        }
        if (resultCode == 100) {
            Alert2.setTextColor(Color.BLACK);
            Alert2.setText("상품 바코드 을(를) 스캔하세요");
        }

        if (resultCode == 500){
            PDA_AND_MAIN MA = (PDA_AND_MAIN)PDA_AND_MAIN._Main_Activity;
            MA.finish();
            finish();
        }
    }

    void main() {
        visible = findViewById(R.id.visible);
        Alert1 = findViewById(R.id.barcode_input_alert1);
        Alert2 = findViewById(R.id.barcode_input_alert2);

        btn_input = findViewById(R.id.rg_btn1);
        btn_cancel = findViewById(R.id.rg_btn2);
        ResetButton = findViewById(R.id.button);

        EtPreInput = findViewById(R.id.et_pre_input);
        LocationBarcodeInput = findViewById(R.id.tv_barcode_location);

        BarcodeInputListview = (ListView) findViewById(R.id.listview_barcode_input);
        BarcodeCancelListview = (ListView) findViewById(R.id.listview_barcode_cancel);

        ImageButton back = findViewById(R.id.back);
        ImageButton logout = findViewById(R.id.logout);
        mainbtn(back, logout, UserId);

        //프레임레이아웃에서 리스트뷰 비활성화 상태로 초기화
        BarcodeInputListview.setVisibility(View.INVISIBLE);
        BarcodeCancelListview.setVisibility(View.INVISIBLE);

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

                    if(RgBtn2Click == "1"){
                        CustomListBarcodeCancel adapterCancel = new CustomListBarcodeCancel(PDA_AND_INPUT.this);
                        BarcodeCancelListview = (ListView) findViewById(R.id.listview_barcode_cancel);
                        BarcodeCancelListview.setAdapter(adapterCancel);
                    }

                    else if (RgBtn1Click == "1"){
                        CustomListBarcodeInput adapterInput = new CustomListBarcodeInput(PDA_AND_INPUT.this);
                        BarcodeInputListview = (ListView) findViewById(R.id.listview_barcode_input);
                        BarcodeInputListview.setAdapter(adapterInput);
                    }

                    Alert2.setText(rs4.getString(3));
                    Alert2.setTextColor(Color.RED);

                } else if (rs4.getString(1).equals("T")) {
                    if (rs4.getString(2).equals("RACK") && RgBtn1Click == "1") {
                        SPreInputR = SPreInput;
                        LocationBarcodeInput.setText(rs4.getString(5));
                        LIST_Name_Input.clear();
                        LIST_Number_Input.clear();
                        LIST_Barcode_Input.clear();
                        CustomListBarcodeInput adapterInput = new CustomListBarcodeInput(PDA_AND_INPUT.this);
                        BarcodeInputListview = (ListView) findViewById(R.id.listview_barcode_input);
                        BarcodeInputListview.setAdapter(adapterInput);
                        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!조회가 제대로 됐을 경우!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                        System.out.println(rs4.getString(5));
                        EtPreInput.setText("");
                        Alert1.setText("0 Scanned");
                        Alert2.setTextColor(Color.BLACK);
                        Alert2.setText("상품 바코드 을(를) 스캔하세요");
                    }
                    else if(rs4.getString(2).equals("RACK") && RgBtn2Click == "1"){
                        //입고 취소시에는 랙바코드 스캔 필요 없음
                        Alert1.setText("0 Scanned");
                        Alert2.setText("취소할 상품 바코드를 스캔하세요");
                    }
                    else if (rs4.getString(2).equals("PART") && RgBtn1Click.equals("1")) {
                        if (LocationBarcodeInput.getText().equals("")) {
                            Alert2.setText("입고위치를 스캔하세요.");
                            Alert2.setTextColor(Color.RED);
                        } else {
                            Pscan();
                        }
                    } else {
                        Pscan();
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("데이터베이스에 안들어갔습니다.");
        }
    }

    void Pscan() {
        SPreInputA = SPreInput;
        //입고 처리
        if (RgBtn1Click == "1") {
            try {  //상품바코드 데이터베이스 추가
                //입고처리 프로시저 돌리기
                String query = "EXEC dbo.SP_PDA_WM00010_SAVE_VALIDATION '" + SPreInputR + "','" + SPreInputA + "','" + UserId + "','" + Language + "'";
                System.out.println(query);
                Statement st = connectDB();
                ResultSet rs = st.executeQuery(query);

                while (rs.next()) {
                    System.out.println(rs.getString(2) + "/////////////////////////////////////////");
                    //입고가 제대로 됐을 경우
                    if (rs.getString(2).isEmpty()) {
                        try {
                            String query3 = "EXEC dbo.SP_PDA_WM00010_SAVE '" + SPreInputR + "','" + SPreInputA + "','" + UserId + "','" + Language + "'";
                            System.out.println(query3);
                            Statement st3 = connectDB();
                            ResultSet rs3 = st3.executeQuery(query3);

                            while (rs3.next()) {
                                if (!rs3.getString(1).isEmpty()) {
                                    LIST_Barcode_Input.add(rs3.getString(1));
                                    LIST_Name_Input.add(rs3.getString(3));
                                    LIST_Number_Input.add(qty(rs3.getString(4)).toString());
                                    Alert1.setText(LIST_Barcode_Input.size() + " Records To Display ...");
                                    Alert2.setTextColor(Color.BLACK);
                                    Alert2.setText("입고 처리가 완료되었습니다.");
                                    CustomListBarcodeInput adapterInput = new CustomListBarcodeInput(PDA_AND_INPUT.this);
                                    BarcodeInputListview = (ListView) findViewById(R.id.listview_barcode_input);
                                    BarcodeInputListview.setAdapter(adapterInput);
                                    regLog(Log_menuID, UserId, Log_action);
                                }
                            }
                        } catch (Exception e) {
                            //Toast.makeText(getApplicationContext(), "데이터베이스에 안들어갔습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else if(rs.getString(1).equals("N")) {
                        Intent intent = new Intent(this, PDA_AND_INPUT_CONFIRM.class);
                        intent.putExtra("RBarcode",SPreInputR);
                        intent.putExtra("PBarcode",SPreInputA);
                        startActivityForResult(intent,0);
                    }
                    else if(rs.getString(1).equals("F")) {
                        CustomListBarcodeInput adapterInput = new CustomListBarcodeInput(PDA_AND_INPUT.this);
                        BarcodeInputListview = (ListView) findViewById(R.id.listview_barcode_input);
                        BarcodeInputListview.setAdapter(adapterInput);
                        Alert2.setTextColor(Color.RED);
                        Alert2.setText(rs.getString(2));

                    }
                }

            } catch (Exception ex) {
                //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                System.out.println("error00.");
            }
        } else if (RgBtn2Click == "1") {
            Alert1.setText("0 Scanned");
            try {  //상품바코드 데이터베이스 추가
                //입고 취소 프로시저 돌리기
                String query = "EXEC dbo.SP_PDA_WM00010_CANCEL_BARCODE '" + SPreInputA + "','" + UserId + "','" + Language + "'";
                Statement st = connectDB();
                ResultSet rs = st.executeQuery(query);

                if (!rs.next()) {
                    CustomListBarcodeCancel adapterCancel = new CustomListBarcodeCancel(PDA_AND_INPUT.this);
                    BarcodeCancelListview = (ListView) findViewById(R.id.listview_barcode_cancel);
                    BarcodeCancelListview.setAdapter(adapterCancel);
                    Alert2.setTextColor(Color.RED);
                    Alert2.setText("존재하지 않는 바코드 입니다.");
                }
                do {
                    if (rs.getString(1).equals("T")) {
                        LIST_Barcode_Cancel.add(rs.getString(3));
                        LIST_Name_Cancel.add(rs.getString(5));
                        LIST_Number_Cancel.add(qty(rs.getString(6).toString()));
                        Alert1.setText(LIST_Barcode_Cancel.size() + " Scanned");
                        Alert2.setTextColor(Color.BLACK);
                        Alert2.setText("입고 취소가 완료되었습니다.");
                        CustomListBarcodeCancel adapterCancel = new CustomListBarcodeCancel(PDA_AND_INPUT.this);
                        BarcodeCancelListview = (ListView) findViewById(R.id.listview_barcode_cancel);
                        BarcodeCancelListview.setAdapter(adapterCancel);
                        regLog(Log_menuID, UserId, "BARCODE_INPUT_CANCLE");


                    } else if (rs.getString(1).equals("F")) {
                        CustomListBarcodeCancel adapterCancel = new CustomListBarcodeCancel(PDA_AND_INPUT.this);
                        BarcodeCancelListview = (ListView) findViewById(R.id.listview_barcode_cancel);
                        BarcodeCancelListview.setAdapter(adapterCancel);
                        Alert2.setTextColor(Color.RED);
                        Alert2.setText(rs.getString(2));

                    }
                } while (rs.next());


            } catch (Exception ex) {
                //Toast.makeText(getApplicationContext(), "데이터베이스에 안들어갔습니다.", Toast.LENGTH_SHORT).show();
            }   //상품바코드 데이터베이스 추가끝
        }
    }

    void reset() {
        main();
        LIST_Name_Input.clear();
        LIST_Number_Input.clear();
        LIST_Barcode_Input.clear();
        LIST_Name_Cancel.clear();
        LIST_Number_Cancel.clear();
        LIST_Barcode_Cancel.clear();
        CustomListBarcodeInput adapterInput = new CustomListBarcodeInput(PDA_AND_INPUT.this);
        BarcodeInputListview = (ListView) findViewById(R.id.listview_barcode_input);
        BarcodeInputListview.setAdapter(adapterInput);
        CustomListBarcodeCancel adapterCancel = new CustomListBarcodeCancel(PDA_AND_INPUT.this);
        BarcodeCancelListview = (ListView) findViewById(R.id.listview_barcode_cancel);
        BarcodeCancelListview.setAdapter(adapterCancel);
        LocationBarcodeInput.setText("");
        Alert1.setText("0 Scaneed");
        Alert2.setTextColor(Color.BLACK);
        Alert2.setText("입고위치를 스캔하세요.");

    }

    void cancel() {
        main();
        Alert2.setTextColor(Color.BLACK);
        Alert2.setText("취소할 상품 바코드를 스캔하세요");
        //버튼이 눌러져 있는지 아닌지 표시(버튼이 눌러져있으면 "1", 안눌러져있으면 "0")
        RgBtn1Click = "0";
        RgBtn2Click = "1";
        //입고처리 리스트뷰 클리어
        LIST_Name_Input.clear();
        LIST_Number_Input.clear();
        LIST_Barcode_Input.clear();
        LocationBarcodeInput.setText("");
        CustomListBarcodeInput adapterInput = new CustomListBarcodeInput(PDA_AND_INPUT.this);
        BarcodeInputListview = (ListView) findViewById(R.id.listview_barcode_input);
        BarcodeInputListview.setAdapter(adapterInput);
    }

    void input() {
        main();
        Alert2.setTextColor(Color.BLACK);
        Alert2.setText("입고위치를 스캔하세요");
        if (!LocationBarcodeInput.getText().equals("")) {
            Alert2.setText("상품 바코드 을(를) 스캔하세요.");
        }
        //버튼이 눌러져 있는지 아닌지 표시(버튼이 눌러져있으면 "1", 안눌러져있으면 "0")
        RgBtn1Click = "1";
        RgBtn2Click = "0";
        //입고취소 리스트뷰 클리어
        LIST_Name_Cancel.clear();
        LIST_Number_Cancel.clear();
        LIST_Barcode_Cancel.clear();
        CustomListBarcodeCancel adapterCancel = new CustomListBarcodeCancel(PDA_AND_INPUT.this);
        BarcodeCancelListview = (ListView) findViewById(R.id.listview_barcode_cancel);
        BarcodeCancelListview.setAdapter(adapterCancel);
    }

    //바코드입고 관련 어뎁터
    public class CustomListBarcodeInput extends ArrayAdapter<String> {
        private final Activity context;

        public CustomListBarcodeInput(Activity context) {
            super(context, R.layout.listitemrow_barcode_input, LIST_Name_Input);
            this.context = context;
            //리스트뷰 활성화, 비활성화 설정
            BarcodeInputListview.setVisibility(View.VISIBLE);
            BarcodeCancelListview.setVisibility(View.INVISIBLE);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.listitemrow_barcode_input, null, true);
            TextView ItemName = (TextView) rowView.findViewById(R.id.input_item_name);
            TextView ItemNumber = (TextView) rowView.findViewById(R.id.input_item_number);
            TextView ItemBarcode = (TextView) rowView.findViewById(R.id.input_item_barcode);
            //Button approve = (Button) rowView.findViewById(R.id.approve_user);
            ItemName.setText(LIST_Name_Input.get(position));
            ItemNumber.setText(LIST_Number_Input.get(position));
            ItemBarcode.setText(LIST_Barcode_Input.get(position));
            //approve.setText("승인");

            return rowView;
        }

    }

    //바코드취소 관련 어뎁터
    public class CustomListBarcodeCancel extends ArrayAdapter<String> {
        private final Activity context;

        public CustomListBarcodeCancel(Activity context) {
            super(context, R.layout.listitemrow_barcode_input_cancel, LIST_Name_Cancel);
            this.context = context;
            //리스트뷰 활성화, 비활성화 설정
            BarcodeInputListview.setVisibility(View.INVISIBLE);
            BarcodeCancelListview.setVisibility(View.VISIBLE);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.listitemrow_barcode_input_cancel, null, true);
            TextView ItemName = (TextView) rowView.findViewById(R.id.cancel_item_name);
            TextView ItemNumber = (TextView) rowView.findViewById(R.id.cancel_item_number);
            TextView ItemBarcode = (TextView) rowView.findViewById(R.id.cancel_item_barcode);
            //Button approve = (Button) rowView.findViewById(R.id.approve_user);
            ItemName.setText(LIST_Name_Cancel.get(position).toString());
            ItemNumber.setText(LIST_Number_Cancel.get(position).toString());
            ItemBarcode.setText(LIST_Barcode_Cancel.get(position).toString());
            //approve.setText("승인");

            return rowView;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mManager.dismiss();
    }

};