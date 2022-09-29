package com.example.pda;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PDA_AND_LEGACY_POPUP extends BaseActivity {
    String pname,pnum;
    String Language = ((PDA_AND_LOGIN) PDA_AND_LOGIN.context).language;  //사용자 언어
    Button btn_close, btn_yes;
    Spinner category_spin;
    Statement st=connectDB();
    ResultSet rs;

    //리스트뷰, 리스트뷰 어뎁터 초기화
    ListView listView;
    ListAdapter_Legacy_Popup listViewAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pda_and_legacy_popup);

        main();
        background();
        popupsize();
        spinner();


        /************ 팝업 내용 표시 ******************/

        listViewAdapter = new ListAdapter_Legacy_Popup();
        popupcontent();


        listView.setOnItemClickListener((parent, view, position, id) ->{
            pname = listViewAdapter.listViewItemList.get(position).getRowtext3();  //선택한 품명
            pnum = listViewAdapter.listViewItemList.get(position).getRowtext2();   //품번
        });




        /************ 팝업창 선택, 닫기 버튼  ****************/

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PDA_AND_LEGACY_POPUP.this, PDA_AND_LEGACY.class);
                setResult(1,intent);
                finish();
            }
        });

        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PDA_AND_LEGACY_POPUP.this, PDA_AND_LEGACY.class);
                intent.putExtra("name",pname);
                intent.putExtra("num",pnum);
               // intent.putExtra("regacy",sregacy);
                setResult(0, intent);
                finish();
            }
        });

    }

    public void main(){
        btn_close = findViewById(R.id.btn_close);
        btn_yes = findViewById(R.id.btn_yes);
        category_spin = findViewById(R.id.category_spin);
        listView = (ListView) findViewById(R.id.listview);
    }
    public void spinner(){
        main();
        String[] items = {"ALL", "완제품", "반제품", "자재", "기타"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_dropdown_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        category_spin.setAdapter(adapter);
    }
    public void popupcontent(){
        category_spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    listViewAdapter.clearItem();
                    try {
                        rs = st.executeQuery("EXEC dbo.SP_PDA_WM00110P1_INQUERY '','" + Language + "'");
                        int i = 1;
                        while (rs.next()) {
                            listViewAdapter.addItem(
                                    Integer.toString(i),
                                    rs.getString(1),
                                    rs.getString(2),
                                    rs.getString(3));
                            //리스트뷰에 어뎁터 set
                            listView.setAdapter(listViewAdapter);
                            i++;
                        }
                    } catch (SQLException e) {
                    }
                }
                if (position == 1) {
                    listViewAdapter.clearItem();
                    try {
                        rs = st.executeQuery("EXEC dbo.SP_PDA_WM00110P1_INQUERY 'A0A','" + Language + "'");
                        int i = 1;
                        while (rs.next()) {
                            listViewAdapter.addItem(
                                    Integer.toString(i),
                                    rs.getString(1),
                                    rs.getString(2),
                                    rs.getString(3));
                            //리스트뷰에 어뎁터 set
                            listView.setAdapter(listViewAdapter);
                            i++;
                        }
                    } catch (SQLException e) {
                    }
                }
                if (position == 2) {
                    listViewAdapter.clearItem();
                    try {
                        rs = st.executeQuery("EXEC dbo.SP_PDA_WM00110P1_INQUERY 'A0S','" + Language + "'");
                        int i = 1;
                        while (rs.next()) {
                            listViewAdapter.addItem(
                                    Integer.toString(i),
                                    rs.getString(1),
                                    rs.getString(2),
                                    rs.getString(3));
                            //리스트뷰에 어뎁터 set
                            listView.setAdapter(listViewAdapter);
                            i++;
                        }
                    } catch (SQLException e) {
                    }
                }
                if (position == 3) {
                    listViewAdapter.clearItem();
                    try {
                        rs = st.executeQuery("EXEC dbo.SP_PDA_WM00110P1_INQUERY 'A0M','" + Language + "'");
                        int i = 1;
                        while (rs.next()) {
                            listViewAdapter.addItem(
                                    Integer.toString(i),
                                    rs.getString(1),
                                    rs.getString(2),
                                    rs.getString(3));
                            //리스트뷰에 어뎁터 set
                            listView.setAdapter(listViewAdapter);
                            i++;
                        }
                    } catch (SQLException e) {
                    }
                }
                if (position == 4) {
                    listViewAdapter.clearItem();
                    try {
                        rs = st.executeQuery("EXEC dbo.SP_PDA_WM00110P1_INQUERY 'A0E','" + Language + "'");
                        int i = 1;
                        while (rs.next()) {
                            listViewAdapter.addItem(
                                    Integer.toString(i),
                                    rs.getString(1),
                                    rs.getString(2),
                                    rs.getString(3));
                            //리스트뷰에 어뎁터 set
                            listView.setAdapter(listViewAdapter);
                            i++;
                        }
                    } catch (SQLException e) {
                    }
                }

            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }



}























