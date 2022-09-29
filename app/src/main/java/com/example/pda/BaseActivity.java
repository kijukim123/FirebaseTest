package com.example.pda;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
    }


    /**********  뒤로가기, 로그아웃   ******************/
    public void mainbtn(ImageButton back, ImageButton logout, String Email){

        back.setOnClickListener(view -> finish());

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), PopupActivity.class);
                startActivityForResult(intent,1);
            }
        });
    }

    /******* SharedPreferences 메서드  ******/
    /******* (경로: shift2번 -> Device File Explorer /data/data/{packageName}/shared_prefs/{keyName}.xml **********/
    public void record(String name, String key, String value) {
        SharedPreferences pref = getSharedPreferences(name, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        Set<String> oldSet = pref.getStringSet(key, new HashSet<String>());

        //make a copy, update it and save it
        Set<String> set = new HashSet<String>();
        set.add(value);
        set.addAll(oldSet);

        editor.putStringSet(key, set);
        editor.commit();
    }

    public void removekey(String key) {

        SharedPreferences pref = getSharedPreferences("UserInfo", MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        edit.remove(key);
        edit.apply();

    }

    public void putSettingItem(String key, String value) {
        SharedPreferences preferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getSettingItem(String key) {

        return getSharedPreferences("UserInfo", 0).getString(key, null);
    }


    /******* DB와 연결하는 함수 ******/
    public Statement connectDB() {

        Connection connect;              //database Connect
        Statement st = null;

        try {
            ConnectionHelper connectionHelper = new ConnectionHelper();
            connect = connectionHelper.ConnectionClass();

            if (connect != null) {

                st = connect.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            }
        } catch (Exception ex) {
        }
        return st;
    }

    /******* Multi ResultSet ******/
    public CallableStatement multi_connectDB(String query) throws SQLException {

        CallableStatement cs = null;
        cs = connectDB().getConnection().prepareCall(query);
        //ResultSet rs = cs.executeQuery();

        return cs;
    }







    /******* 토스트 메시지 ******/
    public void MakeToast(Activity activity, String msg) {
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
    }

    /******* Intent 함수    *************/
    public void moveActivity(Class next) {
        Intent intent = new Intent(this, next);
        startActivity(intent);
    }

    public void moveActivity(Class next, String str1) {
        Intent intent = new Intent(this, next);

        intent.putExtra("str1", str1);
        startActivity(intent);
    }


    /*************** 로그 등록 *********************/
    public String regLogs(String menu, String userid, String action) {

        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:sss", Locale.KOREA);
        SimpleDateFormat ymdFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);

        String IP = getLocalIpAddress();
        String query = "INSERT INTO SYLOGACTION VALUES ('" + menu + "'" + "," + "'NSTORM_V4_ANDROID_PDA'" + "," + "'" + userid + "'" +
                "," + "'" + IP + "'" + "," + "'" + action + "'" + "," + "'" + dataFormat.format(new Date()) + "'" + "," +
                "'" + ymdFormat.format(new Date()) + "');";

        return query;
    }

    public void regLog(String menu, String userid, String action) {
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:sss", Locale.KOREA);
        SimpleDateFormat ymdFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);

        String IP = getLocalIpAddress();
        String query = "INSERT INTO SYLOGACTION VALUES ('" + menu + "'" + "," + "'NSTORM_V4_ANDROID_PDA'" + "," + "'" + userid + "'" +
                "," + "'" + IP + "'" + "," + "'" + action + "'" + "," + "'" + dataFormat.format(new Date()) + "'" + "," +
                "'" + ymdFormat.format(new Date()) + "');";

        Statement st = connectDB();

        try{
            st.executeUpdate(query);
        }catch (SQLException e) {}
    }

    /************** 수량을 정수형으로 저장하기 위한 *****************/
    public Integer strToInt(String str){
        double d = Double.parseDouble(str);
        int i = (int)d;

        return i;
    }
    /************** 소수점이 0인 경우 *****************/
    public String qty (String qty){
        double d = Double.parseDouble(qty);
        DecimalFormat format = new DecimalFormat("0.####");
        return format.format(d);
    }

    /************** 사용자 IP *****************/
    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }



    // 뒷배경 흐리게 하기
    public void background(){
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.7f;
        getWindow().setAttributes(layoutParams);
    }
    public void popupsize(){
        // 사이즈 조절
        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        int width = (int) (dm.widthPixels * 0.9); // Display 사이즈의 90%
        int height = (int) (dm.heightPixels * 0.9); // Display 사이즈의 90%
        getWindow().getAttributes().width = width;
        getWindow().getAttributes().height = height;
    }


}


















