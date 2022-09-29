package com.example.pda;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Set;


public class PDA_AND_LOGIN extends BaseActivity {

    public static Context context;
    private AutoCompleteTextView aetEmail;
    private EditText etPass;
    private Button btnLogin;
    private Spinner spinnerLang;
    public String Email, Pwd,language;


    ArrayList<String> id_list = new ArrayList<String>();
    String Log_menuID= "PDA_AND_LOGIN" ;   // 로그 기록 용 화면 이름
    String Log_action = "LOGIN" ;      // 로그 기록 용 action 이름

    Toast toast=null;
    long backKeyPressedTime = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pda_and_login);

        context = this;


        aetEmail = findViewById(R.id.et_login_email);
        etPass = findViewById(R.id.et_login_pass);
        btnLogin = findViewById(R.id.btn_login1);
        spinnerLang = findViewById(R.id.spinner_language);
        aetEmail.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line,  id_list));


        /************* Language Spinner *******************************/
        ArrayList lanarr = new ArrayList();
        Statement spinnerst = connectDB();
        ResultSet spinnerrs ;
        try {
            spinnerrs = spinnerst.executeQuery(" SELECT LANGUAGE, LANGTEXT from SYSYSTEMLANGUAGE where USE_YN='Y'");
            while(spinnerrs.next()) {
                lanarr.add(spinnerrs.getString(2)) ;
                }
            ArrayAdapter arrayAdapter = new ArrayAdapter<>(getApplicationContext(),R.layout.spinner_item,lanarr);
            spinnerLang.setAdapter((arrayAdapter));
            spinnerLang.setPrompt("Language");
            language = spinnerLang.getSelectedItem().toString();
            spinnerrs.beforeFirst();
            while(spinnerrs.next()){
                if(language.equals(spinnerrs.getString(2))){
                    language=spinnerrs.getString(1);
                }
            }



        }catch (SQLException exception) {
        }



        /************* 이전에 로그인에 성공한 아이디 자동 완성 ******************************/
        record("id", "idkey", "");          //id.xml 파일에 아무 값이 없을 경우 앱이 실행되지 않는 것을 방지

        SharedPreferences pref = getSharedPreferences("id", MODE_PRIVATE);
        Set<String> get_set = pref.getStringSet("idkey",null);
        for(String value : get_set) {
            id_list.add(value);
        }

        /***************************   로그인 버튼   ****************************************/
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Email = aetEmail.getText().toString();
                Pwd = etPass.getText().toString();
                Statement st, st2, logst;
                String userid=null, username=null, usercode=null, userindate = null, useroutdate=null;

                String loginquery = "EXEC dbo.SP_PDA_NLOGIN_INQUERY_EXECUTE_LOGIN '" + Email + "', '" + Pwd + "', '" + language + "'";
                String userinfoquery = "Select * From SYUSERMASTER WHERE USERID = " + "'" + Email + "'";
                String reglog = regLogs(Log_menuID, Email,Log_action);
                st = connectDB();
                st2= connectDB();
                logst = connectDB();
                ResultSet rs=null, rs2=null;

                try {
                    rs = st.executeQuery(loginquery);
                    rs2 = st2.executeQuery(userinfoquery);

                    while (rs.next()) {
                        if (Email.equals("")) { //"ID를 입력하세요", "암호를 입력하세요" 등
                            MakeToast(PDA_AND_LOGIN.this, "ID를 입력하세요.");
                            break;
                        }

                        else if (Pwd.equals("")){
                            MakeToast(PDA_AND_LOGIN.this, "암호를 입력하세요.");
                            break;
                        }
                        else if (!rs.getString(1).isEmpty()) { //1번 테이블이 비어있지 않으면
                            MakeToast(PDA_AND_LOGIN.this, "로그인 실패");
                            break;
                        }
                        else if (rs.getString(1).isEmpty()) {  //로그인 성공
                            logst.executeUpdate(reglog);
                            while(rs2.next()) {


                                username = rs2.getString(2); //이름
                                usercode = rs2.getString(4); //사업장
                                userindate = rs2.getString(5); //입사 날짜


                                putSettingItem("ID",Email);
                                putSettingItem("NAME",username);
                                putSettingItem("CODE",usercode);
                                putSettingItem("DATE",userindate);

                            }
                            record("id", "idkey", Email);
                            moveActivity(PDA_AND_MAIN.class,Email);

                            MakeToast(PDA_AND_LOGIN.this, "로그인 성공");
                            aetEmail.setText("");
                            etPass.setText("");
                            aetEmail.setFocusable(true);
                            aetEmail.requestFocus();
                            //System.out.println("0000000000000000000000" + language);
                        }
                        }
                    }catch (SQLException e){
                }

            }
        });
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







