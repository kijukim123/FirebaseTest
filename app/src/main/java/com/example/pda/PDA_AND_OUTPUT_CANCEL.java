package com.example.pda;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.m3.sdk.scannerlib.Barcode;
import com.m3.sdk.scannerlib.BarcodeListener;
import com.m3.sdk.scannerlib.BarcodeManager;

public class PDA_AND_OUTPUT_CANCEL extends BaseActivity {
    Button Save, Reset;
    EditText EtPreOutCancel;
    String UserId;
    String SPreInput;
    String SPreInputPN;
    String SPreInputR;
    TextView TvOutNumber;
    TextView TvOutCancelPlace;
    String StrOutNumber, StrOutCancelPlace;
    public List<String> LIST_STATUS = new ArrayList<>();
    TextView Alert1;
    TextView Alert2;
    String Language;
    String N="X";

    //리스트뷰, 리스트뷰 어뎁터 초기화
    ListView listview;
    ListAdapter_output_cancel listViewAdapter;


    // lib
    Barcode mBarcode = null;
    BarcodeListener mListener = null;
    BarcodeManager mManager = null;
    Barcode.Symbology mSymbology = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pda_and_output_cancel_main);

        UserId = ((PDA_AND_MAIN) PDA_AND_MAIN.context).Email;  //사용자 아이디
        Language = ((PDA_AND_LOGIN) PDA_AND_LOGIN.context).language;  //사용자 언어

        ImageButton back = findViewById(R.id.back);
        ImageButton logout = findViewById(R.id.logout);
        mainbtn(back, logout, UserId);

        listViewAdapter = new ListAdapter_output_cancel();

        main();


        mBarcode = new Barcode(this);
        mManager = new BarcodeManager(this);
        mSymbology = mBarcode.getSymbologyInstance();

        mListener = new BarcodeListener() {
            @Override
            public void onBarcode(String s) {
                SPreInput = s.trim();
                System.out.println("SPreInput");
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

        Save.setOnClickListener(v -> save());

        Reset.setOnClickListener(v -> reset());

    }

    /************팝업 종료 후 호출 *************/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent parentIntent) {

        super.onActivityResult(requestCode, resultCode, parentIntent);

        main();
        //병합 품번 재고 팝업에서 엔터키 누른 경우
        if (resultCode == 1) {
            reset();
        }

        if (resultCode == 500){
            PDA_AND_MAIN MA = (PDA_AND_MAIN)PDA_AND_MAIN._Main_Activity;
            MA.finish();
            finish();
        }
    }


    void scan() {
        System.out.println("scan()");
        main();
        Alert2.setTextColor(Color.BLACK);
        if (SPreInput.startsWith("P")) {
            TvOutNumber.setText(SPreInput);
            SPreInputPN = SPreInput;

            System.out.println("set");
            try {  //출고번호 스캔 후 반환값 리스트뷰에 세팅
                //출고번호 조회
                String query1 = "EXEC SP_PDA_WM00090_INQUERY '" + SPreInputPN + "','" + SPreInputPN + "','','" + Language + "'";
                CallableStatement cs2 = multi_connectDB(query1);
                ResultSet rs2 = cs2.executeQuery();

                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!데이터베이스 연결만 됐을 경우!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                while (rs2.next()) {
                    //조회가 제대로 됐을 경우
                    if (rs2.getString(1).equals("PICKING_NO")) {
                        //두번째 테이블 가져오기기
                        if (cs2.getMoreResults()) {
                            rs2 = cs2.getResultSet();
                            System.out.println("두 번째 테이블");
                            while (rs2.next()) {
                                if (rs2.getString(5).equals("SF80")) {
                                    //두번째 테이블에서 PICKINGNO가져와서 텍스트뷰에 세팅
                                    Alert2.setTextColor(Color.RED);
                                    Alert2.setText("이미 출고 취소 처리가 완료된 바코드입니다.");
                                } else {
                                    //두번째 테이블에서 PICKINGNO가져와서 텍스트뷰에 세팅
                                    TvOutNumber.setText(rs2.getString(1));

                                    //세번째 테이블 가져오기
                                    if (cs2.getMoreResults()) {
                                        rs2 = cs2.getResultSet();
                                        System.out.println("세 번째 테이블");
                                        LIST_STATUS.clear();
                                        while (rs2.next()) {
                                            LIST_STATUS.add(rs2.getString(5));
                                            //세번쨰 테이블에서 데이터 받아서 리스트뷰에 세팅
                                            // LIST_STATUS.add(rs2.getString(3));
                                            listViewAdapter.addItem(
                                                    rs2.getString(1),
                                                    qty(rs2.getString(4)),
                                                    rs2.getString(3));
                                            //리스트뷰에 어뎁터 set
                                            listview.setAdapter(listViewAdapter);
                                            System.out.println("어댑터");
                                            scancnt();
                                            Alert2.setTextColor(Color.BLACK);
                                            Alert2.setText("입고할 랙 바코드를 스캔하세요.");
                                            EtPreOutCancel.setText("");

                                        }
                                    }
                                }
                            }
                        }
                    } else if (rs2.getString(2).equals("존재하지 않는 바코드입니다.")) {
                        SPreInput = "";
                        SPreInputPN = "";
                        EtPreOutCancel.setText("");
                        TvOutNumber.setText("");
                        listViewAdapter.clearItem();
                        listViewAdapter.notifyDataSetChanged();
                        scancnt();
                        Alert2.setTextColor(Color.RED);
                        Alert2.setText(rs2.getString(2));
                    }
                }
            } catch (Exception ex) {
                Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
            }


        } else if (SPreInput.startsWith("R")) {

            SPreInputR = SPreInput;
            try {  //렉바코드 데이터베이스 조회
                //출고번호 조회
                String query4 = "EXEC SP_PDA_WM00090_INQUERY '" + SPreInputR + "','" + SPreInputPN + "','" + SPreInputR + "','" + Language + "'";
                CallableStatement cs4 = multi_connectDB(query4);
                ResultSet rs4 = cs4.executeQuery();

                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!데이터베이스 연결만 됐을 경우!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                while (rs4.next()) {
                    //조회가 제대로 됐을 경우
                    if (rs4.getString(1).equals("RACKCD")) {

                        //출고번호가 없고 랙바코드만 스캔된 경우
                        if (SPreInputPN == null) {
                            Alert2.setTextColor(Color.RED);
                            Alert2.setText("출고번호 을(를) 스캔하세요.");
                            EtPreOutCancel.setText("");
                        } else if (SPreInputPN != null) {
                            //두번째 테이블 가져오기기

                            if (cs4.getMoreResults()) {
                                rs4 = cs4.getResultSet();

                                while (rs4.next()) {
                                    //두번째 테이블에서 RACKCD가져와서 텍스트뷰에 세팅
                                    System.out.println(rs4.getString(2));
                                    TvOutCancelPlace.setText(rs4.getString(2));
                                    EtPreOutCancel.setText("");
                                    //listview.setAdapter(listViewAdapter);
                                    scancnt();
                                    Alert2.setTextColor(Color.BLACK);
                                    Alert2.setText("바코드 스캔이 완료되었습니다.");  // 문구 수정 필요성 확인


                                    if (cs4.getMoreResults()) {
                                        rs4 = cs4.getResultSet();


                                        listViewAdapter.clearItem();
                                        LIST_STATUS.clear();
                                        listViewAdapter.clearItem();
                                        while (rs4.next()) {
                                            LIST_STATUS.add(rs4.getString(5));
                                            listViewAdapter.addItem(
                                                    rs4.getString(1),
                                                    qty(rs4.getString(4)),
                                                    rs4.getString(3));
                                            //리스트뷰에 어뎁터 set
                                            listview.setAdapter(listViewAdapter);
                                        }
                                    }
                                }
                            }
                        }
                    } else if (rs4.getString(2).equals("존재하지 않는 바코드입니다.")) {
                        Alert2.setTextColor(Color.RED);
                        Alert2.setText(rs4.getString(2));
                        EtPreOutCancel.setText("");
                    }
                }
            } catch (Exception ex) {
                Toast.makeText(getApplicationContext(), "error.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Alert2.setTextColor(Color.RED);
            Alert2.setText("존재하지 않는 바코드입니다.");
        }
    }

    void main() {
        EtPreOutCancel = findViewById(R.id.et_pre_out_cancel);
        TvOutNumber = findViewById(R.id.tv_out_cancel_number);
        TvOutCancelPlace = findViewById(R.id.tv_out_cancel_place);
        Alert1 = findViewById(R.id.barcode_out_cancel_alert1);
        Alert2 = findViewById(R.id.barcode_out_cancel_alert2);
        Save = findViewById(R.id.button_out_cancel_save);
        Reset = findViewById(R.id.button_out_cancel_reset);
        listview = findViewById(R.id.listview_barcode_out_cancel);
    }

    void reset() {
        main();
        SPreInput = "";
        SPreInputR = "";
        SPreInputPN = "";
        EtPreOutCancel.setText("");
        TvOutNumber.setText("");
        TvOutCancelPlace.setText("");
        listViewAdapter.clearItem();
        listViewAdapter.notifyDataSetChanged();
        scancnt();
        Alert2.setTextColor(Color.BLACK);
        Alert2.setText("출고번호를 스캔하세요");
    }

    void save() {
        main();
        StrOutNumber = TvOutNumber.getText().toString();
        StrOutCancelPlace = TvOutCancelPlace.getText().toString();

        if (TvOutNumber.getText().equals("")) {
            moveActivity(Confirm.class, "출고번호 을(를) 스캔하세요.");
        } else if (TvOutCancelPlace.getText().equals("")) {
            moveActivity(Confirm.class, "위치바코드 을(를) 스캔하세요.");
        } else if (LIST_STATUS.contains("F")) {
            moveActivity(Confirm.class, "해당 랙에 적재할 수 없는 품번이 존재합니다.");
        } else {
            if(LIST_STATUS.contains("N")) N="N";
            Intent outCancelIntent = new Intent(getApplicationContext(), PDA_AND_OUTPUT_CANCEL_CONFIRM_POPUP.class);
            //출고번호, 위치명, 랙바코드 넘기기
            outCancelIntent.putExtra("StrOutNumber", StrOutNumber);
            outCancelIntent.putExtra("SPreInputR", SPreInputR);
            outCancelIntent.putExtra("N",N);
            startActivityForResult(outCancelIntent, 1);
        }

    }

    //scan 개수
    public void scancnt() {
        main();
        int num = listViewAdapter.getCount();
        String snum = num + " Records To Display ...";
        Alert1.setText(snum);
    }

    //바코드출고 관련 어뎁터
    public class ListAdapter_output_cancel extends BaseAdapter {
        public ArrayList<ListViewItem> listViewItemList = new ArrayList<ListViewItem>();
        @Override
        public int getCount() {
            return listViewItemList.size();
        }

        @Override
        public Object getItem(int position) {
            return listViewItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final int pos = position;
            final Context context = parent.getContext();

            // "listview_item" Layout을 inflate하여 convertView 참조 획득.
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.listitemrow_barcode_out_cancel, parent, false);
            }

            // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
            TextView textView1 = (TextView) convertView.findViewById(R.id.textView1);
            TextView textView2 = (TextView) convertView.findViewById(R.id.textView2);
            TextView textView3 = (TextView) convertView.findViewById(R.id.textView3);

            // Data Set(filteredItemList)에서 position에 위치한 데이터 참조 획득
            ListViewItem listViewItem = listViewItemList.get(position);

            textView1.setText(listViewItem.getRowtext1());
            textView2.setText(listViewItem.getRowtext2());
            textView3.setText(listViewItem.getRowtext3());

            //System.out.println("8888888888888888"+LIST_STATUS.get(position));
            if(!LIST_STATUS.isEmpty()){

                switch (LIST_STATUS.get(position)) {
                    case "N":
                        textView1.setBackgroundColor(Color.parseColor("#FFFA8956"));
                        textView2.setBackgroundColor(Color.parseColor("#FFFA8956"));
                        textView3.setBackgroundColor(Color.parseColor("#FFFA8956"));
                        break;
                    case "F":
                        textView1.setBackgroundColor(Color.parseColor("#F64046"));
                        textView2.setBackgroundColor(Color.parseColor("#F64046"));
                        textView3.setBackgroundColor(Color.parseColor("#F64046"));
                        break;
                    case "O":
                        textView1.setBackgroundColor(Color.parseColor("#FFE493"));
                        textView2.setBackgroundColor(Color.parseColor("#FFE493"));
                        textView3.setBackgroundColor(Color.parseColor("#FFE493"));
                        break;
                }

            }


            return convertView;
        }

        public void addItem(String text1, String text2, String text3) {
            ListViewItem item = new ListViewItem();
            item.setRowtext1(text1);
            item.setRowtext2(text2);
            item.setRowtext3(text3);
            listViewItemList.add(item);
        }

        public void clearItem() {
            listViewItemList.clear();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mManager.dismiss();
    }
}