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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.m3.sdk.scannerlib.Barcode;
import com.m3.sdk.scannerlib.BarcodeListener;
import com.m3.sdk.scannerlib.BarcodeManager;


public class PDA_AND_INDIVIDUAL_OUTPUT extends BaseActivity implements Serializable {
    ImageButton back;
    ImageButton logout;
    TextView OutDate;
    TextView OutDateBack, OutDateFore;
    SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd"); // 날짜 포맷
    String Yesterday, Tomorrow;
    Integer Yint, Tint;
    Spinner SpinnerCUSTCD;
    List<String> Spinner_LIST_CUSTCD = new ArrayList<>();
    List<String> Spinner_LIST_CUSTC = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;
    EditText EtPreIndividualOut;
    String SPreInput;
    String SPreInputA;
    public List<String> LIST_Name_INDIVIDUAL_OUT = new ArrayList<>();
    List<String> LIST_RACK_INDIVIDUAL_OUT = new ArrayList<>();
    List<String> LIST_QTY_INDIVIDUAL_OUT = new ArrayList<>();
    List<String> LIST_OUTQTY_INDIVIDUAL_OUT = new ArrayList<>();
    List<String> LIST_BARCODE_INDIVIDUAL_OUT = new ArrayList<>();
    List<String> LIST_CANCEL_INDIVIDUAL_OUT = new ArrayList<>();
    ListView BarcodeIndiOutListview;
    TextView Alert1;
    TextView Alert2;
    String PARTNO, PARTNM;
    String RsPartNm, RsRack, RsOriginQty, RsOutQty, RsBarcode;
    Double IntOriginQty, IntOutQty;
    ArrayList<String> ListNameIndividual = null;
    ArrayList<String> ListRackIndividual = null;
    ArrayList<String> ListQtyIndividual = null;
    public ArrayList<String> ListOutqtyIndividual = null;
    public ArrayList<String> ListBarcodeIndividual = null;
    public ArrayList<String> ListCancelIndividual = null;
    int i, ListSize;
    Button ResetButton, SaveButton;
    String Date, CustCD, CustC;
    String PopupConfirm;
    int SelectedSpinner, SelectedSpinnerIntent;
    String StrOutDate;
    String UserId, Language;
    String sdate;

    Date date = new Date();

    Calendar cal = Calendar.getInstance();
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    // lib
    Barcode mBarcode = null;
    BarcodeListener mListener = null;
    BarcodeManager mManager = null;
    Barcode.Symbology mSymbology = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pda_and_individual_output_main);

        Yint = 0;
        UserId = ((PDA_AND_MAIN) PDA_AND_MAIN.context).Email;  //사용자 아이디
        Language = ((PDA_AND_LOGIN) PDA_AND_LOGIN.context).language;  //사용자 언어



        main();

        //spinner (고객사 정보) 불러오기
        customer();

        //초기화 버튼 클릭 이벤트 처리
        ResetButton.setOnClickListener(v -> reset());

        //저장 버튼 클릭 이벤트 처리
        SaveButton.setOnClickListener(v -> save());


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

        //기준 일자

        OutDate = findViewById(R.id.tv_indiout_date);
        cal.setTime(new Date());
        sdate = df.format(cal.getTime());
        OutDate.setText(sdate);
        OutDateBack.setOnClickListener(v -> {
            cal.add(Calendar.DATE, -1);
            OutDate.setText(df.format(cal.getTime()));
        });
        OutDateFore.setOnClickListener(v -> {
            cal.add(Calendar.DATE, 1);
            OutDate.setText(df.format(cal.getTime()));
        });


    }


    /************팝업 종료 후 호출 *************/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent parentIntent) {

        super.onActivityResult(requestCode, resultCode, parentIntent);

        main();
        //병합 품번 재고 팝업에서 엔터키 누른 경우
        if (resultCode == 1) {
            //병합 팝업에서 받은 내용들

            RsPartNm = parentIntent.getStringExtra("PartNm");
            RsRack = parentIntent.getStringExtra("rack");
            RsOriginQty = parentIntent.getStringExtra("OriginQTY");
            RsOutQty = parentIntent.getStringExtra("SOutQTY");
            RsBarcode = parentIntent.getStringExtra("Barcode");

            //병합바코드 일 경우 출고수량 숫자만 증가
            //병합 바코드 중에서 중복된 행이 몇번째 행인지 파악해서 그행에 있는 출고수량을 올려줘야함
            ListSize = LIST_BARCODE_INDIVIDUAL_OUT.size();

            String overlap = "N";
            if (ListSize != 0) {
                for (i = 0; i < ListSize; i++) {
                    System.out.println("***" + i);

                    if (LIST_BARCODE_INDIVIDUAL_OUT.get(i).equals(RsBarcode)) { //즁복인 경우
                        overlap = "Y";
                        try {
                            IntOriginQty = Double.parseDouble(LIST_OUTQTY_INDIVIDUAL_OUT.get(i));
                            IntOutQty = Double.parseDouble(RsOutQty);
                            String SOutQty = String.valueOf(IntOriginQty + IntOutQty);
                            LIST_OUTQTY_INDIVIDUAL_OUT.set(i, qty(SOutQty));
                            CustomListBarcodeIndiOut adapterOut2 = new CustomListBarcodeIndiOut(PDA_AND_INDIVIDUAL_OUTPUT.this);
                            BarcodeIndiOutListview = (ListView) findViewById(R.id.listview_barcode_indiout);
                            BarcodeIndiOutListview.setAdapter(adapterOut2);
                        } catch (IndexOutOfBoundsException e) {
                            e.printStackTrace();
                        }
                    }
                }

                if(overlap.equals("N")){
                    LIST_Name_INDIVIDUAL_OUT.add(RsPartNm);
                    LIST_RACK_INDIVIDUAL_OUT.add(RsRack);
                    LIST_QTY_INDIVIDUAL_OUT.add(qty(RsOriginQty));
                    LIST_OUTQTY_INDIVIDUAL_OUT.add(qty(RsOutQty));
                    LIST_BARCODE_INDIVIDUAL_OUT.add(RsBarcode);
                    LIST_CANCEL_INDIVIDUAL_OUT.add("ㅡ");
                    CustomListBarcodeIndiOut adapterOut2 = new CustomListBarcodeIndiOut(PDA_AND_INDIVIDUAL_OUTPUT.this);
                    BarcodeIndiOutListview = (ListView) findViewById(R.id.listview_barcode_indiout);
                    BarcodeIndiOutListview.setAdapter(adapterOut2);
                }

            }

            else if(ListSize == 0) {
                LIST_Name_INDIVIDUAL_OUT.add(RsPartNm);
                LIST_RACK_INDIVIDUAL_OUT.add(RsRack);
                LIST_QTY_INDIVIDUAL_OUT.add(qty(RsOriginQty));
                LIST_OUTQTY_INDIVIDUAL_OUT.add(qty(RsOutQty));
                LIST_BARCODE_INDIVIDUAL_OUT.add(RsBarcode);
                LIST_CANCEL_INDIVIDUAL_OUT.add("ㅡ");
                CustomListBarcodeIndiOut adapterOut2 = new CustomListBarcodeIndiOut(PDA_AND_INDIVIDUAL_OUTPUT.this);
                BarcodeIndiOutListview = (ListView) findViewById(R.id.listview_barcode_indiout);
                BarcodeIndiOutListview.setAdapter(adapterOut2);

            }
            ListSize = LIST_BARCODE_INDIVIDUAL_OUT.size();
            Alert1.setText(LIST_Name_INDIVIDUAL_OUT.size() + " Scanned");
            Alert2.setTextColor(Color.BLACK);
            Alert2.setText("저장되었습니다.");

        }

        if (resultCode == 2) {
            reset();
            Alert2.setTextColor(Color.BLACK);
            Alert2.setText("저장되었습니다.");
        }

        if (resultCode == 500){
            PDA_AND_MAIN MA = (PDA_AND_MAIN)PDA_AND_MAIN._Main_Activity;
            MA.finish();
            finish();
        }
    }





    //바코드 개별 출고 관련 어뎁터
    public class CustomListBarcodeIndiOut extends ArrayAdapter<String> {
        private final Activity context;

        public CustomListBarcodeIndiOut(Activity context) {
            super(context, R.layout.listitemrow_barcode_individual_output, LIST_Name_INDIVIDUAL_OUT);
            this.context = context;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.listitemrow_barcode_individual_output, null, true);
            TextView ItemName = (TextView) rowView.findViewById(R.id.individual_item_name);
            TextView ItemRack = (TextView) rowView.findViewById(R.id.individual_item_rack);
            TextView ItemQty = (TextView) rowView.findViewById(R.id.individual_item_qty);
            TextView ItemOutQty = (TextView) rowView.findViewById(R.id.individual_item_outqty);
            TextView ItemBarcode = (TextView) rowView.findViewById(R.id.individual_item_barcode);
            TextView ItemCancel = (TextView) rowView.findViewById(R.id.individual_item_cancel);
            ItemCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LIST_Name_INDIVIDUAL_OUT.remove(position);
                    LIST_RACK_INDIVIDUAL_OUT.remove(position);
                    LIST_QTY_INDIVIDUAL_OUT.remove(position);
                    LIST_OUTQTY_INDIVIDUAL_OUT.remove(position);
                    LIST_BARCODE_INDIVIDUAL_OUT.remove(position);
                    LIST_CANCEL_INDIVIDUAL_OUT.remove(position);
                    CustomListBarcodeIndiOut adapterOut = new CustomListBarcodeIndiOut(PDA_AND_INDIVIDUAL_OUTPUT.this);
                    BarcodeIndiOutListview = (ListView) findViewById(R.id.listview_barcode_indiout);
                    BarcodeIndiOutListview.setAdapter(adapterOut);
                }
            });
            //Button approve = (Button) rowView.findViewById(R.id.approve_user);
            ItemName.setText(LIST_Name_INDIVIDUAL_OUT.get(position).toString());
            ItemRack.setText(LIST_RACK_INDIVIDUAL_OUT.get(position).toString());
            ItemQty.setText(LIST_QTY_INDIVIDUAL_OUT.get(position).toString());
            ItemOutQty.setText(LIST_OUTQTY_INDIVIDUAL_OUT.get(position).toString());
            ItemBarcode.setText(LIST_BARCODE_INDIVIDUAL_OUT.get(position).toString());
            ItemCancel.setText(LIST_CANCEL_INDIVIDUAL_OUT.get(position).toString());
            //approve.setText("승인");

            return rowView;
        }

    }

    void main() {
        Alert1 = findViewById(R.id.barcode_indioutput_alert1);
        Alert2 = findViewById(R.id.barcode_indioutput_alert2);
        SpinnerCUSTCD = findViewById(R.id.spinner_individual_custom);
        OutDate = findViewById(R.id.tv_indiout_date);
        ResetButton = findViewById(R.id.button_indiout_reset);
        SaveButton = findViewById(R.id.button_indiout_save);
        EtPreIndividualOut = findViewById(R.id.et_pre_indiout);
        OutDateBack = findViewById(R.id.outdate_back);
        OutDateFore = findViewById(R.id.outdate_foreward);
        BarcodeIndiOutListview = (ListView) findViewById(R.id.listview_barcode_indiout);

        /*********뒤로가기, 로그아웃 **********/
        ImageButton back = findViewById(R.id.back);
        ImageButton logout = findViewById(R.id.logout);
        mainbtn(back, logout, UserId);
    }

    void reset() {
        main();
        //리스트뷰 초기화
        LIST_Name_INDIVIDUAL_OUT.clear();
        LIST_RACK_INDIVIDUAL_OUT.clear();
        LIST_QTY_INDIVIDUAL_OUT.clear();
        LIST_OUTQTY_INDIVIDUAL_OUT.clear();
        LIST_BARCODE_INDIVIDUAL_OUT.clear();
        LIST_CANCEL_INDIVIDUAL_OUT.clear();
        PDA_AND_INDIVIDUAL_OUTPUT.CustomListBarcodeIndiOut adapterOut = new PDA_AND_INDIVIDUAL_OUTPUT.CustomListBarcodeIndiOut(PDA_AND_INDIVIDUAL_OUTPUT.this);
        BarcodeIndiOutListview = (ListView) findViewById(R.id.listview_barcode_indiout);
        BarcodeIndiOutListview.setAdapter(adapterOut);
        //출고처 선택 스피너 초기화
        SpinnerCUSTCD.setSelection(0);
        //날짜 금일로 리셋
        //OutDate.setText(mFormat.format(date));
        //Yint = 0;
        OutDate = findViewById(R.id.tv_indiout_date);
        cal.setTime(new Date());
        sdate = df.format(cal.getTime());
        OutDate.setText(sdate);

        Alert1.setText(LIST_Name_INDIVIDUAL_OUT.size() + " Records To Display ...");
        Alert2.setTextColor(Color.BLACK);
        Alert2.setText("바코드 을(를) 스캔하세요.");
    }

    void save() {
        main();
        System.out.println(LIST_Name_INDIVIDUAL_OUT.size());
        if (SpinnerCUSTCD.getSelectedItemPosition() == 0) {
            moveActivity(Confirm.class, "고객사를 선택해주세요.");
        } else if (LIST_Name_INDIVIDUAL_OUT.size() == 0) {
            moveActivity(Confirm.class, "저장할 정보가 없습니다.");
        } else if (SpinnerCUSTCD.getSelectedItemPosition() != 0) {
            Date = OutDate.getText().toString();
            CustCD = SpinnerCUSTCD.getSelectedItem().toString();
            CustC = Spinner_LIST_CUSTC.get(SelectedSpinner);
            Intent intentConfirm = new Intent(getApplicationContext(), PDA_AND_INDIVIDUAL_OUTPUT_POPUP_CONFIRM.class);
            intentConfirm.putExtra("Date", Date);
            intentConfirm.putExtra("CustCD", CustC);


            ArrayList<String> List_NAME_INDIVIDUAL = new ArrayList<>(LIST_Name_INDIVIDUAL_OUT);
            intentConfirm.putExtra("NameList", List_NAME_INDIVIDUAL);
            ArrayList<String> List_RACK_INDIVIDUAL = new ArrayList<>(LIST_RACK_INDIVIDUAL_OUT);
            intentConfirm.putExtra("RackList", List_RACK_INDIVIDUAL);
            ArrayList<String> List_QTY_INDIVIDUAL = new ArrayList<>(LIST_QTY_INDIVIDUAL_OUT);
            intentConfirm.putExtra("QtyList", List_QTY_INDIVIDUAL);
            ArrayList<String> List_OUTQTY_INDIVIDUAL = new ArrayList<>(LIST_OUTQTY_INDIVIDUAL_OUT);
            intentConfirm.putExtra("OutqtyList", List_OUTQTY_INDIVIDUAL);
            ArrayList<String> List_BARCODE_INDIVIDUAL = new ArrayList<>(LIST_BARCODE_INDIVIDUAL_OUT);
            intentConfirm.putExtra("BarcodeList", List_BARCODE_INDIVIDUAL);
            ArrayList<String> List_CANCEL_INDIVIDUAL = new ArrayList<>(LIST_CANCEL_INDIVIDUAL_OUT);
            intentConfirm.putExtra("CancelList", List_CANCEL_INDIVIDUAL);


            startActivityForResult(intentConfirm, 2);
        }

    }

    void scan() {
        SPreInputA = SPreInput;

        try {  //렉바코드 데이터베이스 조회
            //출고번호 조회
            String query4 = "EXEC SP_PDA_WM00120_INQUERY '" + SPreInputA + "','" + Language + "'";
            CallableStatement cs4 = multi_connectDB(query4);
            ResultSet rs4 = cs4.executeQuery();


            while (rs4.next()) {
                // 병합 품번이 아니고, WM바코드 있고, BM바코드 있는 경우
                if (rs4.getString(1).isEmpty() && rs4.getString(2).equals("BARCODE")) {
                    EtPreIndividualOut.setText("");

                    //두번째 테이블 가져오기기
                    if (cs4.getMoreResults()) {
                        rs4 = cs4.getResultSet();

                        while (rs4.next()) {
                            if (LIST_BARCODE_INDIVIDUAL_OUT.contains(rs4.getString(1))) {

                                Alert1.setText(LIST_Name_INDIVIDUAL_OUT.size() + " Scanned");
                                Alert2.setTextColor(Color.RED);
                                Alert2.setText("이미 등록된 바코드입니다.");
                            } else {
                                LIST_Name_INDIVIDUAL_OUT.add(rs4.getString(3));
                                LIST_RACK_INDIVIDUAL_OUT.add(rs4.getString(4));
                                LIST_QTY_INDIVIDUAL_OUT.add(qty(rs4.getString(5)).toString());
                                LIST_OUTQTY_INDIVIDUAL_OUT.add(qty(rs4.getString(5)).toString());
                                LIST_BARCODE_INDIVIDUAL_OUT.add(rs4.getString(1));
                                LIST_CANCEL_INDIVIDUAL_OUT.add("ㅡ");
                                PDA_AND_INDIVIDUAL_OUTPUT.CustomListBarcodeIndiOut adapterOut = new PDA_AND_INDIVIDUAL_OUTPUT.CustomListBarcodeIndiOut(PDA_AND_INDIVIDUAL_OUTPUT.this);
                                BarcodeIndiOutListview = (ListView) findViewById(R.id.listview_barcode_indiout);
                                BarcodeIndiOutListview.setAdapter(adapterOut);
                                Alert1.setText(LIST_Name_INDIVIDUAL_OUT.size() + " Scanned");
                                Alert2.setTextColor(Color.BLACK);
                                Alert2.setText("바코드 스캔이 완료되었습니다.");
                            }

                        }
                    }
                }
                // 병합 품번이고, WM바코드 있고, BM바코드 있는 경우
                else if (rs4.getString(1).isEmpty() && rs4.getString(2).equals("PARTNO")) {
                    //팝업창으로 액티비티 전환
                    //두번째 테이블 가져오기기

                    if (cs4.getMoreResults()) {
                        rs4 = cs4.getResultSet();

                        while (rs4.next()) {
                            // 넘겨줄 PARTNM 받아서 intent로 넘기기
                            PARTNO = rs4.getString(1);
                            PARTNM = rs4.getString(2);
                        }
                    }
                    //현재 선택된 고객사
                    SelectedSpinner = SpinnerCUSTCD.getSelectedItemPosition();
                    //현재 날짜
                    StrOutDate = OutDate.getText().toString();
                    //intent에 담아서 넘기기
                    Intent intent = new Intent(PDA_AND_INDIVIDUAL_OUTPUT.this, PDA_AND_INDIVIDUAL_OUTPUT_POPUP.class);
                    intent.putExtra("PARTNO", PARTNO);
                    intent.putExtra("PARTNM", PARTNM);
                    intent.putExtra("BARCODE", SPreInputA);
                    intent.putExtra("CustPosition", SelectedSpinner);
                    intent.putExtra("StrOutDate", StrOutDate);
                    startActivityForResult(intent, 1);
                } else if (rs4.getString(1).equals("입고된 바코드가 아닙니다.")) {
                    Alert1.setText(LIST_Name_INDIVIDUAL_OUT.size() + " Scanned");
                    Alert2.setTextColor(Color.RED);
                    Alert2.setText("입고된 바코드가 아닙니다.");
                    EtPreIndividualOut.setText("");
                } else if (rs4.getString(1).equals("존재하지 않는 바코드입니다.")) {
                    Alert1.setText(LIST_Name_INDIVIDUAL_OUT.size() + " Scanned");
                    Alert2.setTextColor(Color.RED);
                    Alert2.setText("존재하지 않는 바코드입니다.");
                    EtPreIndividualOut.setText("");
                }
                else{
                    System.out.println("THIS IS EXCEPTION WHEN BARCODE SCAN");
                }

            }


        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "데이터베이스에 안들어갔습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    void customer() {
        main();
        Spinner_LIST_CUSTCD.add("고객사를 선택하세요.");
        Spinner_LIST_CUSTC.add("");
        try {
            String query1 = "EXEC SP_COMMONCODE_INQUERY_CUSTCD 'Y', '" + Language + "'";
            Statement st1 = connectDB();
            ResultSet rs1 = st1.executeQuery(query1);

            while (rs1.next()) {
                if (!rs1.getString(1).isEmpty()) {
                    Spinner_LIST_CUSTCD.add(rs1.getString(2));
                    Spinner_LIST_CUSTC.add(rs1.getString(1));
                    arrayAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, Spinner_LIST_CUSTCD);
                    SpinnerCUSTCD.setAdapter(arrayAdapter);
                }
            }
        } catch (Exception e) {
            //Toast.makeText(getApplicationContext(), "데이터베이스에 안들어갔습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mManager.dismiss();
    }
}