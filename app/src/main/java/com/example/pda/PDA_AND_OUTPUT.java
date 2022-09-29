package com.example.pda;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.m3.sdk.scannerlib.Barcode;
import com.m3.sdk.scannerlib.BarcodeListener;
import com.m3.sdk.scannerlib.BarcodeManager;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PDA_AND_OUTPUT extends BaseActivity {
    int i;
    private ImageButton back;
    private ImageButton logout;
    private Button Save, Reset;
    private EditText EtPreOut;
    private Context mContext;
    private String UserId, Language;
    private String SPreInput;
    private String SPreInputPN;
    private String SPreInputA;
    private TextView TvOutNumber;
    private TextView TvOutCustName;
    private TextView TvOutDate;
    private List<String> LIST_PARTNO_OUT = new ArrayList<>();
    private List<String> LIST_Name_OUT = new ArrayList<>();
    private List<String> LIST_PickNumber_OUT = new ArrayList<>();
    private List<String> LIST_Number_OUT = new ArrayList<>();
    private List<String> LIST_CHECK_OUT = new ArrayList<>();
    private ListView BarcodeOutListview;
    private TextView Alert1;
    private TextView Alert2;
    private int IntPickNumber;
    private String StrPARTNO, StrPARTNM;
    private String StrOutNumber, StrOutCust, StrOutDate;
    private String RsOutNumber, RsCust, RsOutDate;
    public ArrayList<String> ListNameOUTPUT = null;
    public ArrayList<String> ListPickNumberOUTPUT = null;
    public ArrayList<String> ListNumberOUTPUT = null;
    public ArrayList<String> ListCheckOUTPUT = null;
    private ArrayList<String> ListPartNoOUTPUT = null;
    private ArrayList<String> NewListPartNo = null;
    private String PARTNO, PARTNM;
    public ArrayList<String> TempNameOUTPUT = null;
    public ArrayList<String> TempPickNumberOUTPUT = null;
    public ArrayList<String> TempNumberOUTPUT = null;
    public ArrayList<String> TempCheckOUTPUT = null;
    private String ErrorMessage, SBarcode;
    private int PartnoPopoupIntent;
    private int Count, PickNumOver, saveYes;
    private String BarcodeErrorMessage, PartnoErrorMessage, Status;

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
        setContentView(R.layout.activity_pda_and_output_main);

        main();

        //피킹수량 확인 스위치
        PickNumOver = 0;

        //저장버튼 클릭 이벤트
        Save.setOnClickListener(v -> saveButton());
        //초기화버튼 클릭 이벤트
        Reset.setOnClickListener(v -> reset());

        //M3 SDK관련 설정
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


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent parentIntent) {
        super.onActivityResult(requestCode, resultCode, parentIntent);
        System.out.println("THIS IS OnActivityResult CHECKPOINT...");
        //ArrayList초기화
        LIST_PARTNO_OUT.clear();
        LIST_Name_OUT.clear();
        LIST_PickNumber_OUT.clear();
        LIST_Number_OUT.clear();
        LIST_CHECK_OUT.clear();
        if (requestCode == 1020) {
            requestCodeEvent();
            saveYes = parentIntent.getIntExtra("saveYes", 0);
            System.out.println("saveYes : " + saveYes);
            if (saveYes == 1){
                LIST_PARTNO_OUT.clear();
                LIST_Name_OUT.clear();
                LIST_PickNumber_OUT.clear();
                LIST_Number_OUT.clear();
                LIST_CHECK_OUT.clear();
                CustomListBarcodeOut Ruesult1020Adapter = new CustomListBarcodeOut(PDA_AND_OUTPUT.this);
                BarcodeOutListview = (ListView) findViewById(R.id.listview_barcode_out);
                BarcodeOutListview.setAdapter(Ruesult1020Adapter);
                Alert1.setText(LIST_Name_OUT.size() + " Scanned");
                Alert2.setTextColor(Color.BLACK);
                Alert2.setText("저장되었습니다.");
            }
        } else if (requestCode == 1030) {
            Count = 0;
            ErrorMessage = parentIntent.getStringExtra("ErrorMessage");
            System.out.println("GET INTENT ERROR MESSAGE : " + ErrorMessage);
            requestCodeEvent();
            if (!ErrorMessage.equals("null")) {
                System.out.println("에러메시지가 있을 경우");
                Alert1.setText(LIST_Name_OUT.size() + " Scanned");
                Alert2.setTextColor(Color.RED);
                Alert2.setText(ErrorMessage);
            } else if (ErrorMessage.equals("null")) {
                System.out.println("에러메시지가 없을 경우");
                Alert1.setText(LIST_Name_OUT.size() + " Scanned");
                Alert2.setTextColor(Color.BLACK);
                Alert2.setText("출고번호을(를) 스캔하세요.");
            }

        } else if (requestCode == 1040) {
            requestCodeEvent();
        } else if (requestCode == 3110){
            requestCodeEvent();
        }
        else if (resultCode == 500) {
            PDA_AND_MAIN MA = (PDA_AND_MAIN) PDA_AND_MAIN._Main_Activity;
            MA.finish();
            finish();
        } else {
            System.out.println("THIS IS REQUEST CODE EXCEPTION CHECKPOINT");
        }
    }

    //바코드출고 관련 어뎁터
    public class CustomListBarcodeOut extends ArrayAdapter<String> {
        private final Activity context;

        public CustomListBarcodeOut(Activity context) {
            super(context, R.layout.listitemrow_barcode_output, LIST_Name_OUT);
            this.context = context;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.listitemrow_barcode_output, null, true);
            TextView ItemName = (TextView) rowView.findViewById(R.id.out_item_name);
            TextView ItemPickNumber = (TextView) rowView.findViewById(R.id.out_item_picking);
            TextView ItemNumber = (TextView) rowView.findViewById(R.id.out_item_number);
            TextView ItemConfirm = (TextView) rowView.findViewById(R.id.out_item_confirm);
            //Button approve = (Button) rowView.findViewById(R.id.approve_user);
            ItemName.setText(LIST_Name_OUT.get(position).toString());
            ItemPickNumber.setText(LIST_PickNumber_OUT.get(position).toString());
            ItemNumber.setText(LIST_Number_OUT.get(position).toString());
            ItemConfirm.setText(LIST_CHECK_OUT.get(position).toString());

            if (LIST_PickNumber_OUT.get(position).toString().equals(LIST_Number_OUT.get(position).toString())) {
                System.out.println("PICKING NUMBER AND OUT NUMBER IS SAME");
                ItemName.setBackgroundColor(Color.YELLOW);
                ItemPickNumber.setBackgroundColor(Color.YELLOW);
                ItemNumber.setBackgroundColor(Color.YELLOW);
                EtPreOut.setText("");
                //피킹수량 확인 스위치
                PickNumOver = 0;
            } else if (LIST_Number_OUT.get(position).toString().equals("0")) {
                System.out.println("PICKING NUMBER IS ZERO");
                EtPreOut.setText("");
                //피킹수량 확인 스위치
                PickNumOver = 0;
            } else if (Integer.parseInt(LIST_PickNumber_OUT.get(position).toString()) < Integer.parseInt(LIST_Number_OUT.get(position).toString())){
                System.out.println("PICKING NUMBER IS SMALLER THAN OUT NUMBER");
                ItemName.setBackgroundColor(Color.parseColor("#FF5050"));
                ItemPickNumber.setBackgroundColor(Color.parseColor("#FF5050"));
                ItemNumber.setBackgroundColor(Color.parseColor("#FF5050"));
                PickNumOver = 1;
                EtPreOut.setText("");
            }
            else {
                System.out.println("PICKING NUMBER IS NOT ZERO");
                ItemName.setBackgroundColor(Color.LTGRAY);
                ItemPickNumber.setBackgroundColor(Color.LTGRAY);
                ItemNumber.setBackgroundColor(Color.LTGRAY);
                EtPreOut.setText("");
                //피킹수량 확인 스위치
                PickNumOver = 0;
            }

            ItemConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NewListPartNo = ListPartNoOUTPUT;
                    System.out.println(NewListPartNo);

                    //+버튼을 처음 눌렀을 경우
                    if (NewListPartNo == null) {
                        System.out.println(LIST_PARTNO_OUT);
                        StrPARTNO = LIST_PARTNO_OUT.get(position);
                        System.out.println(StrPARTNO);
                    } else if (LIST_PARTNO_OUT != null) {
                        StrPARTNO = LIST_PARTNO_OUT.get(position);
                    }
                    //+버튼을 두번째 또는 그이상 눌렀을 경우
                    else {
                        LIST_PARTNO_OUT = NewListPartNo;
                        StrPARTNO = NewListPartNo.get(position);
                        System.out.println(StrPARTNO);
                    }
                    StrPARTNM = LIST_Name_OUT.get(position);
                    SPreInputPN = TvOutNumber.getText().toString();
                    Intent OutPopupIntent = new Intent(getApplicationContext(), PDA_AND_OUTPUT_PLUS_POPUP.class);
                    OutPopupIntent.putExtra("SPreInputPN", SPreInputPN);
                    OutPopupIntent.putExtra("StrPARTNO", StrPARTNO);
                    OutPopupIntent.putExtra("StrPARTNM", StrPARTNM);
                    System.out.println("***************************************************************");
                    StrOutNumber = TvOutNumber.getText().toString();
                    StrOutCust = TvOutCustName.getText().toString();
                    StrOutDate = TvOutDate.getText().toString();
                    //출고번호, 고객사명, 출고일자 넘기기
                    OutPopupIntent.putExtra("StrOutNumber", StrOutNumber);
                    OutPopupIntent.putExtra("StrOutCust", StrOutCust);
                    OutPopupIntent.putExtra("StrOutDate", StrOutDate);
                    //품명, 피킹수량, 수량, 확인 리스트 넘기기
                    ArrayList<String> List_NAME_OUTPUT = new ArrayList<>(LIST_Name_OUT);
                    OutPopupIntent.putExtra("NameList", List_NAME_OUTPUT);
                    ArrayList<String> List_PICKNUMBER_OUTPUT = new ArrayList<>(LIST_PickNumber_OUT);
                    OutPopupIntent.putExtra("PickNumberList", List_PICKNUMBER_OUTPUT);
                    ArrayList<String> List_NUMBER_OUTPUT = new ArrayList<>(LIST_Number_OUT);
                    OutPopupIntent.putExtra("NumberList", List_NUMBER_OUTPUT);
                    ArrayList<String> List_CHECK_OUTPUT = new ArrayList<>(LIST_CHECK_OUT);
                    OutPopupIntent.putExtra("CheckList", List_CHECK_OUTPUT);
                    ArrayList<String> List_PARTNO_OUTPUT = new ArrayList<>(LIST_PARTNO_OUT);
                    OutPopupIntent.putExtra("PartNo", List_PARTNO_OUTPUT);
                    //startActivity(OutPopupIntent);
                    startActivityForResult(OutPopupIntent, 1040);
                }
            });

            return rowView;
        }
    }


    void requestCodeEvent() {
        // requestCode : 부모액티비티에서 보낸 Code
        // resultCode : 자식액티비티에서 보낸 Code (setResult)
        System.out.println("THIS IS OnActivityResult RequestCode CHECKPOINT...");
        try {  //렉바코드 데이터베이스 조회
            //출고번호 조회
            String query11 = "EXEC SP_PDA_WM00030_INQUERY_DETL '" + SPreInputPN + "','" + Language + "'";
            System.out.println(query11);
            Statement st11 = connectDB();
            ResultSet rs11 = st11.executeQuery(query11);

            while (rs11.next()) {
                //갱신된 출고번호 정보 조회
                if (!rs11.getString(1).isEmpty()) {
                    LIST_PARTNO_OUT.add(rs11.getString(1));
                    LIST_Name_OUT.add(rs11.getString(2));
                    LIST_PickNumber_OUT.add(qty(rs11.getString(3)));
                    LIST_Number_OUT.add(qty(rs11.getString(4)));
                    LIST_CHECK_OUT.add("+");
                    CustomListBarcodeOut Ruesult1040Adapter = new CustomListBarcodeOut(PDA_AND_OUTPUT.this);
                    BarcodeOutListview = (ListView) findViewById(R.id.listview_barcode_out);
                    BarcodeOutListview.setAdapter(Ruesult1040Adapter);
                    Alert1.setText(LIST_Name_OUT.size() + " Scanned");
                    Alert2.setTextColor(Color.BLACK);
                    Alert2.setText("출고번호을(를) 스캔하세요.");
                    System.out.println(rs11.getString(1));
                    System.out.println(rs11.getString(2));
                    System.out.println(rs11.getString(3));
                    System.out.println(rs11.getString(4));
                }
            }
        } catch (Exception ex) {
            //Toast.makeText(getApplicationContext(), "데이터베이스에 안들어갔습니다.", Toast.LENGTH_SHORT).show();
            System.out.println("ResultCode :: 1040, Message :: Database Catch Exception");
        }
    }


    void main() {
        UserId = ((PDA_AND_MAIN) PDA_AND_MAIN.context).Email;  //사용자 아이디
        Language = ((PDA_AND_LOGIN) PDA_AND_LOGIN.context).language;  //사용자 언어

        EtPreOut = findViewById(R.id.et_pre_out);
        TvOutNumber = findViewById(R.id.tv_out_number);
        TvOutCustName = findViewById(R.id.tv_out_name);
        TvOutDate = findViewById(R.id.tv_out_date);

        Alert1 = findViewById(R.id.barcode_output_alert1);
        Alert2 = findViewById(R.id.barcode_output_alert2);

        Count = 0;

        ImageButton back = findViewById(R.id.back);
        ImageButton logout = findViewById(R.id.logout);
        mainbtn(back, logout, UserId);

        Save = findViewById(R.id.button_out_save);
        Reset = findViewById(R.id.button_out_reset);

        //editText (한글,영어,숫자)만 받기
        EtPreOut.setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                Pattern ps = Pattern.compile("^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ\\u318D\\u119E\\u11A2\\u2022\\u2025a\\u00B7\\uFE55]+$");
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(EtPreOut.getWindowToken(), 0);
                if (source.equals("") || ps.matcher(source).matches()) {
                    return source;
                } else if (TvOutNumber != null && TvOutNumber.length() > 10) {
                    //랙위치 텍스트뷰에 유효한 값이 있으면 EditText 초기화
                    //EtPreMove.setText("");
                } else if (SPreInputA != null && SPreInputA.length() > 10) {
                    //랙위치 텍스트뷰에 유효한 값이 있으면 EditText 초기화
                    //EtPreOut.setText("");
                }
                return "";
            }
        }, new InputFilter.LengthFilter(20)});

        if (RsOutNumber == null) {

        } else if (RsOutNumber != null) {
            TvOutNumber.setText(RsOutNumber);
            TvOutCustName.setText(RsCust);
            TvOutDate.setText(RsOutDate);
            //리스트뷰 초기화
            LIST_PARTNO_OUT.clear();
            LIST_Name_OUT.clear();
            LIST_PickNumber_OUT.clear();
            LIST_Number_OUT.clear();
            LIST_CHECK_OUT.clear();
            LIST_PARTNO_OUT.addAll(ListPartNoOUTPUT);
            LIST_Name_OUT.addAll(ListNameOUTPUT);
            LIST_PickNumber_OUT.addAll(ListPickNumberOUTPUT);
            LIST_Number_OUT.addAll(ListNumberOUTPUT);
            LIST_CHECK_OUT.addAll(ListCheckOUTPUT);
            CustomListBarcodeOut adapterRecieve = new CustomListBarcodeOut(PDA_AND_OUTPUT.this);
            BarcodeOutListview = (ListView) findViewById(R.id.listview_barcode_out);
            BarcodeOutListview.setAdapter(adapterRecieve);
        }
    }


    void scan() {

        //기존에 있던 피킹리스트 정보 초기화
        LIST_Name_OUT.clear();
        LIST_PickNumber_OUT.clear();
        LIST_Number_OUT.clear();
        LIST_CHECK_OUT.clear();
        CustomListBarcodeOut adapterOutOrigin = new CustomListBarcodeOut(PDA_AND_OUTPUT.this);
        BarcodeOutListview = (ListView) findViewById(R.id.listview_barcode_out);
        BarcodeOutListview.setAdapter(adapterOutOrigin);


        try {  //렉바코드 데이터베이스 조회
            //출고번호 조회
            String query1 = "EXEC SP_PDA_WM00030_INQUERY2 '" + SPreInput + "','" + SPreInput + "','" + UserId + "','" + Language + "'";
            Statement st1 = connectDB();
            ResultSet rs1 = st1.executeQuery(query1);
            System.out.println(query1);

            while (rs1.next()) {
                //바코드 종류가 "PICKING_NO"이고 MSG가 없는 경우
                if (rs1.getString(1).equals("PICKING_NO") && rs1.getString(2).equals("")) {
                    SPreInputPN = SPreInput;
                    TvOutNumber.setText(SPreInputPN);

                    //두번째 테이블 가져오기기
                    String query5 = "EXEC SP_PDA_WM00030_INQUERY2 '" + SPreInputPN + "','" + SPreInputPN + "','" + UserId + "','" + Language + "'";
                    System.out.println(query5);
                    CallableStatement cs5 = multi_connectDB(query5);
                    ResultSet rs5 = cs5.executeQuery();
                    if (cs5.getMoreResults()) {
                        rs5 = cs5.getResultSet();

                        while (rs5.next()) {
                            LIST_PARTNO_OUT.add(rs5.getString(1));
                            LIST_Name_OUT.add(rs5.getString(2));
                            LIST_PickNumber_OUT.add(qty(rs5.getString(3)));
                            LIST_Number_OUT.add(qty(rs5.getString(4)));
                            LIST_CHECK_OUT.add("+");


                            CustomListBarcodeOut adapterOut = new CustomListBarcodeOut(PDA_AND_OUTPUT.this);
                            BarcodeOutListview = (ListView) findViewById(R.id.listview_barcode_out);
                            BarcodeOutListview.setAdapter(adapterOut);
                            Alert1.setText(LIST_Name_OUT.size() + " Scanned");
                            Alert2.setTextColor(Color.BLACK);
                            Alert2.setText("출고번호을(를) 스캔하세요.");
                            System.out.println("++++++++++++++++++++++++++++++++++++++");
                            System.out.println(rs5.getString(1));
                            System.out.println(rs5.getString(2));
                            System.out.println(qty(rs5.getString(3)));
                            System.out.println(qty(rs5.getString(4)));
                        }

                        //세번째 테이블 가져오기
                        if (cs5.getMoreResults()) {
                            rs5 = cs5.getResultSet();

                            while (rs5.next()) {
                                TvOutNumber.setText(rs5.getString(1));
                                StrOutNumber = rs5.getString(1);
                                TvOutCustName.setText(rs5.getString(3));
                                StrOutCust = rs5.getString(3);
                                TvOutDate.setText(rs5.getString(4));
                                StrOutDate = rs5.getString(4);
                                Status = rs5.getString(5);
                                EtPreOut.setText("");
                                System.out.println("++++++++++++++++++++++++++++++++++++++");
                                System.out.println(rs5.getString(1));
                                System.out.println(rs5.getString(2));
                                System.out.println(rs5.getString(3));
                                System.out.println(rs5.getString(4));
                                System.out.println(rs5.getString(5));
                                //이미 처리된 출고번호
                                if (Status.equals("SF40")){
                                    System.out.println("STATUS IS SF40");
                                    LIST_PARTNO_OUT.clear();
                                    LIST_Name_OUT.clear();
                                    LIST_PickNumber_OUT.clear();
                                    LIST_Number_OUT.clear();
                                    LIST_CHECK_OUT.clear();
                                    CustomListBarcodeOut adapterStatus = new CustomListBarcodeOut(PDA_AND_OUTPUT.this);
                                    BarcodeOutListview = (ListView) findViewById(R.id.listview_barcode_out);
                                    BarcodeOutListview.setAdapter(adapterStatus);
                                    //TvOutNumber.setText("");
                                    //TvOutDate.setText("");
                                    //TvOutCustName.setText("");
                                    Alert1.setText(LIST_Name_OUT.size() + " Scanned");
                                    Alert2.setTextColor(Color.RED);
                                    Alert2.setText("이미 처리된 출고번호 입니다.");
                                }
                                else{
                                    System.out.println("STATUS IS NOT SF40");
                                }
                            }
                        }
                    }
                }
                //에러 메시지 있는 경우
                else if (rs1.getString(1).equals("PICKING_NO") && !rs1.getString(2).isEmpty()) {
                    Alert1.setText(LIST_Name_OUT.size() + " Scanned");
                    Alert2.setTextColor(Color.RED);
                    Alert2.setText(rs1.getString(2));
                    EtPreOut.setText("");

                } else if (TvOutNumber.getText().toString().equals("")) {
                    System.out.println("THIS IS CHECK POINT IF OUTNUMBER IS NULL");
                    Alert1.setText(LIST_Name_OUT.size() + " Scanned");
                    Alert2.setTextColor(Color.RED);
                    Alert2.setText("출고번호를 먼저 스캔해 주세요.");
                    EtPreOut.setText("");
                }
                //스캔된 바코드가 출고번호가 아닐 경우
                else {  //일반상품바코드, 병합상품바코드, 아무것도 아닌바코드
                    notPnBarcode();
                }
            }
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "데이터베이스에 안들어갔습니다.", Toast.LENGTH_SHORT).show();
        }

        EtPreOut.setFocusable(true);
        EtPreOut.requestFocus();
    }

    void notPnBarcode() {
        SPreInputA = SPreInput;
        try {  //렉바코드 데이터베이스 조회
            System.out.println("++++++++++++++++++++" + SPreInputPN + "++++++++++++++++++++");
            //바코드 타입 조회
            String query2 = "EXEC SP_PDA_WM00030_INQUERY2 '" + SPreInputPN + "','" + SPreInput + "','" + UserId + "','" + Language + "'";
            System.out.println(query2);
            CallableStatement cs2 = multi_connectDB(query2);
            ResultSet rs2 = cs2.executeQuery();

            while (rs2.next()) {
                System.out.println("TYPE :: " + rs2.getString(1));
                System.out.println("MSG :: " + rs2.getString(2));
                if (rs2.getString(1).equals("BARCODE") && rs2.getString(2).equals("")) {
                    System.out.println("THIS IS BARCODE TYPE");
                    if (cs2.getMoreResults()) {
                        rs2 = cs2.getResultSet();


                        while (rs2.next()) {
                            if (rs2.getString(1).isEmpty() == false) {
                                LIST_PARTNO_OUT.add(rs2.getString(1));
                                LIST_Name_OUT.add(rs2.getString(2));
                                LIST_PickNumber_OUT.add(qty(rs2.getString(3)));
                                LIST_Number_OUT.add(qty(qty(rs2.getString(4))));
                                LIST_CHECK_OUT.add("+");
                                PDA_AND_OUTPUT.CustomListBarcodeOut adapter7 = new PDA_AND_OUTPUT.CustomListBarcodeOut(PDA_AND_OUTPUT.this);
                                BarcodeOutListview = (ListView) findViewById(R.id.listview_barcode_out);
                                BarcodeOutListview.setAdapter(adapter7);
                                Alert1.setText(LIST_Name_OUT.size() + " Scanned");
                                Alert2.setTextColor(Color.BLACK);
                                System.out.println("************************************");
                                System.out.println(rs2.getString(1));
                                System.out.println(rs2.getString(2));
                                System.out.println(rs2.getString(3));
                                System.out.println(rs2.getString(4));
                                SPreInput = "";
                                SPreInputA = null;
                                EtPreOut.setText("");
                                System.out.println("BARCODE, MSG IS NULL :: ");
                                System.out.println(LIST_Name_OUT);
                                System.out.println(LIST_PickNumber_OUT);
                                System.out.println(LIST_Number_OUT);

                            }
                        }
                    }
                } else if (rs2.getString(1).equals("BARCODE") && !rs2.getString(2).equals("")) {
                    //이미 등록된 바코드 처리(2022.09.27)
                    BarcodeErrorMessage = rs2.getString(2);
                    try {  //렉바코드 데이터베이스 조회
                        //출고번호 조회
                        String query11 = "EXEC SP_PDA_WM00030_INQUERY_DETL '" + SPreInputPN + "','" + Language + "'";
                        System.out.println(query11);
                        Statement st11 = connectDB();
                        ResultSet rs11 = st11.executeQuery(query11);

                        while (rs11.next()) {
                            //갱신된 출고번호 정보 조회
                            if (!rs11.getString(1).isEmpty()) {
                                LIST_PARTNO_OUT.add(rs11.getString(1));
                                LIST_Name_OUT.add(rs11.getString(2));
                                LIST_PickNumber_OUT.add(qty(rs11.getString(3)));
                                LIST_Number_OUT.add(qty(rs11.getString(4)));
                                LIST_CHECK_OUT.add("+");
                                CustomListBarcodeOut alreadyAdapter = new CustomListBarcodeOut(PDA_AND_OUTPUT.this);
                                BarcodeOutListview = (ListView) findViewById(R.id.listview_barcode_out);
                                BarcodeOutListview.setAdapter(alreadyAdapter);
                                Alert1.setText(LIST_Name_OUT.size() + " Scanned");
                                Alert2.setTextColor(Color.RED);
                                Alert2.setText(BarcodeErrorMessage);
                            }
                        }
                    } catch (Exception ex) {
                        //Toast.makeText(getApplicationContext(), "데이터베이스에 안들어갔습니다.", Toast.LENGTH_SHORT).show();
                        System.out.println("이미 등록된 바코드 입니다.");
                    }
                } else if (rs2.getString(1).equals("PARTNO") && rs2.getString(2).equals("")) {

                    //두번째 테이블 조회
                    if (cs2.getMoreResults()) {
                        rs2 = cs2.getResultSet();
                        while (rs2.next()) {
                        }
                        //세번째 테이블 조회
                        if (cs2.getMoreResults()) {
                            rs2 = cs2.getResultSet();
                            while (rs2.next()) {
                                PARTNO = rs2.getString(1);
                                PARTNM = rs2.getString(2);

                                for (i = 0; i < 1000; i++) {
                                    Count = Count + 1;
                                    if (Count == 1) {
                                        System.out.println("THIS IS CHECK POINT INTENT PARTNO POPUP");
                                        Intent PartnoIntent = new Intent(getApplicationContext(), PDA_AND_OUTPUT_PARTNO_POPUP.class);
                                        PartnoIntent.putExtra("PARTNO", PARTNO);
                                        PartnoIntent.putExtra("PARTNM", PARTNM);
                                        PartnoIntent.putExtra("SPreInputA", SPreInputA);
                                        PartnoIntent.putExtra("SPreInputPN", SPreInputPN);
                                        PartnoIntent.putExtra("StrPARTNO", PARTNO);
                                        PartnoIntent.putExtra("StrPARTNM", PARTNM);
                                        StrOutNumber = TvOutNumber.getText().toString();
                                        StrOutCust = TvOutCustName.getText().toString();
                                        StrOutDate = TvOutDate.getText().toString();
                                        //출고번호, 고객사명, 출고일자 넘기기
                                        PartnoIntent.putExtra("StrOutNumber", StrOutNumber);
                                        PartnoIntent.putExtra("StrOutCust", StrOutCust);
                                        PartnoIntent.putExtra("StrOutDate", StrOutDate);
                                        PartnoIntent.putExtra("SBarcode", SPreInputA);
                                        //startActivity(PartnoIntent);
                                        startActivityForResult(PartnoIntent, 1030);

                                        /*
                                        //0.3초 후 팝업 시작
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                Intent PartnoIntent = new Intent(getApplicationContext(), PDA_AND_OUTPUT_PARTNO_POPUP.class);
                                                PartnoIntent.putExtra("PARTNO", PARTNO);
                                                PartnoIntent.putExtra("PARTNM", PARTNM);
                                                PartnoIntent.putExtra("SPreInputA", SPreInputA);
                                                PartnoIntent.putExtra("SPreInputPN", SPreInputPN);
                                                PartnoIntent.putExtra("StrPARTNO", PARTNO);
                                                PartnoIntent.putExtra("StrPARTNM", PARTNM);
                                                StrOutNumber = TvOutNumber.getText().toString();
                                                StrOutCust = TvOutCustName.getText().toString();
                                                StrOutDate = TvOutDate.getText().toString();
                                                //출고번호, 고객사명, 출고일자 넘기기
                                                PartnoIntent.putExtra("StrOutNumber", StrOutNumber);
                                                PartnoIntent.putExtra("StrOutCust", StrOutCust);
                                                PartnoIntent.putExtra("StrOutDate", StrOutDate);
                                                PartnoIntent.putExtra("SBarcode", SPreInputA);
                                                //startActivity(PartnoIntent);
                                                System.out.println("THIS IS CHECKING ABOUT PARTNO POPUP STACK");
                                                startActivityForResult(PartnoIntent, 1030);
                                            }
                                        }, 300);

                                         */
                                    }
                                }
                            }
                        }
                    }
                } else if (rs2.getString(1).equals("PARTNO") && !rs2.getString(2).equals("")) {
                    PartnoErrorMessage = rs2.getString(2);
                    System.out.println(PartnoErrorMessage);
                    try {  //렉바코드 데이터베이스 조회
                        //출고번호 조회
                        String query11 = "EXEC SP_PDA_WM00030_INQUERY_DETL '" + SPreInputPN + "','" + Language + "'";
                        System.out.println(query11);
                        Statement st11 = connectDB();
                        ResultSet rs11 = st11.executeQuery(query11);

                        while (rs11.next()) {
                            //갱신된 출고번호 정보 조회
                            if (!rs11.getString(1).isEmpty()) {
                                LIST_PARTNO_OUT.add(rs11.getString(1));
                                LIST_Name_OUT.add(rs11.getString(2));
                                LIST_PickNumber_OUT.add(qty(rs11.getString(3)));
                                LIST_Number_OUT.add(qty(rs11.getString(4)));
                                LIST_CHECK_OUT.add("+");
                                CustomListBarcodeOut alreadyAdapter = new CustomListBarcodeOut(PDA_AND_OUTPUT.this);
                                BarcodeOutListview = (ListView) findViewById(R.id.listview_barcode_out);
                                BarcodeOutListview.setAdapter(alreadyAdapter);
                                Alert1.setText(LIST_Name_OUT.size() + " Scanned");
                                Alert2.setTextColor(Color.RED);
                                Alert2.setText(PartnoErrorMessage);
                            }
                        }
                    } catch (Exception ex) {
                        //Toast.makeText(getApplicationContext(), "데이터베이스에 안들어갔습니다.", Toast.LENGTH_SHORT).show();
                        System.out.println("Partno Error Exception");
                    }
                }
            }
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "데이터베이스에 안들어갔습니다.", Toast.LENGTH_SHORT).show();
        }
    }


    void reset() {
        main();
        TvOutNumber.setText("");
        TvOutCustName.setText("");
        TvOutDate.setText("");
        EtPreOut.setText("");
        LIST_Name_OUT.clear();
        LIST_PickNumber_OUT.clear();
        LIST_Number_OUT.clear();
        LIST_CHECK_OUT.clear();
        CustomListBarcodeOut adapterOut = new CustomListBarcodeOut(PDA_AND_OUTPUT.this);
        BarcodeOutListview = (ListView) findViewById(R.id.listview_barcode_out);
        BarcodeOutListview.setAdapter(adapterOut);
        Alert1.setText("0 Scanned");
        Alert2.setText("출고번호를 스캔하세요.");
        Alert2.setTextColor(Color.BLACK);
    }

    void saveButton() {
        main();
        if (SPreInputPN == null) {
            SPreInputPN = TvOutNumber.getText().toString();
        }

        if (TvOutNumber.getText().toString().equals("")) {
            moveActivity(Confirm.class, "출고번호 을(를) 스캔하세요.");
        }
        else if (PickNumOver == 1){
            Alert1.setText(LIST_Name_OUT.size() + " Scanned");
            Alert2.setTextColor(Color.RED);
            Alert2.setText("현재 상태에서는 저장 할 수 없습니다.");
        }
        else {
            Intent saveIntent = new Intent(getApplicationContext(), PDA_AND_OUTPUT_CONFIRM_POPUP.class);
            saveIntent.putExtra("SPreInputPN", SPreInputPN);
            saveIntent.putExtra("StrPARTNO", StrPARTNO);
            saveIntent.putExtra("StrPARTNM", StrPARTNM);
            System.out.println("***************************************************************");
            StrOutNumber = TvOutNumber.getText().toString();
            StrOutCust = TvOutCustName.getText().toString();
            StrOutDate = TvOutDate.getText().toString();
            //출고번호, 고객사명, 출고일자 넘기기
            saveIntent.putExtra("StrOutNumber", StrOutNumber);
            saveIntent.putExtra("StrOutCust", StrOutCust);
            saveIntent.putExtra("StrOutDate", StrOutDate);
            //품명, 피킹수량, 수량, 확인 리스트 넘기기
            ArrayList<String> List_NAME_OUTPUT = new ArrayList<>(LIST_Name_OUT);
            saveIntent.putExtra("NameList", List_NAME_OUTPUT);
            ArrayList<String> List_PICKNUMBER_OUTPUT = new ArrayList<>(LIST_PickNumber_OUT);
            saveIntent.putExtra("PickNumberList", List_PICKNUMBER_OUTPUT);
            ArrayList<String> List_NUMBER_OUTPUT = new ArrayList<>(LIST_Number_OUT);
            saveIntent.putExtra("NumberList", List_NUMBER_OUTPUT);
            ArrayList<String> List_CHECK_OUTPUT = new ArrayList<>(LIST_CHECK_OUT);
            saveIntent.putExtra("CheckList", List_CHECK_OUTPUT);
            ArrayList<String> List_PARTNO_OUTPUT = new ArrayList<>(LIST_PARTNO_OUT);
            saveIntent.putExtra("PartNo", List_PARTNO_OUTPUT);
            //startActivity(saveIntent);
            startActivityForResult(saveIntent, 1020);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mManager.dismiss();
    }

}