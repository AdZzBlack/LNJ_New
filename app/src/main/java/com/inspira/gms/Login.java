package com.inspira.gms;

import android.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Tonny on 7/22/2017.
 */

public class Login extends AppCompatActivity implements View.OnClickListener{
    EditText edtUsername, edtPassword;
    Button btnSubmit;
    GlobalVar global;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        global = new GlobalVar(this);
        LibInspira.setShared(global.sharedpreferences, global.shared.server, "vpn.inspiraworld.com:99");

        edtUsername = (EditText) findViewById(R.id.edtUsername);
        edtPassword = (EditText) findViewById(R.id.edtPassword);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(this);

        edtUsername.setVisibility(View.INVISIBLE);
        edtPassword.setVisibility(View.INVISIBLE);
        btnSubmit.setVisibility(View.INVISIBLE);

        if(LibInspira.getShared(global.userpreferences,global.user.hash,"").equals(""))
        {
            edtUsername.setVisibility(View.VISIBLE);
            edtPassword.setVisibility(View.VISIBLE);
            btnSubmit.setVisibility(View.VISIBLE);
        }
        else
        {
            Log.d("hash", LibInspira.getShared(global.userpreferences,global.user.hash,""));
            String actionUrl = "Login/checkUser/";
            new checkUser().execute( actionUrl );
        }

        // made by Shodiq
        //modified by ADI @01-Sep-2017
        // Permission for enabling location feature only for SDK Marshmallow | Android 6
//        if (Build.VERSION.SDK_INT >= 23)
//            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
//                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1600);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 3);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnSubmit){
            String actionUrl = "Login/loginUser/";
            new loginUser().execute( actionUrl );
        }
    }

    private void setdatauser(JSONObject obj)
    {
        try
        {
            LibInspira.setShared(global.userpreferences, global.user.nomor, obj.getString("user_nomor"));
            LibInspira.setShared(global.userpreferences, global.user.nomor_android, obj.getString("user_nomor_android"));
            LibInspira.setShared(global.userpreferences, global.user.nomor_sales, obj.getString("user_nomor_sales"));
            LibInspira.setShared(global.userpreferences, global.user.kode_sales, obj.getString("user_kode_sales"));  //added by Tonny @05-Sep-2017
            LibInspira.setShared(global.userpreferences, global.user.password, obj.getString("user_password"));  //added by Tonny @30-Jul-2017
            LibInspira.setShared(global.userpreferences, global.user.nama, obj.getString("user_nama"));
            LibInspira.setShared(global.userpreferences, global.user.tipe, obj.getString("user_tipe"));
            LibInspira.setShared(global.userpreferences, global.user.role, obj.getString("user_role"));
            LibInspira.setShared(global.userpreferences, global.user.hash, obj.getString("user_hash"));
            LibInspira.setShared(global.userpreferences, global.user.cabang, obj.getString("user_cabang"));
            LibInspira.setShared(global.userpreferences, global.user.namacabang, obj.getString("user_nama_cabang"));
            LibInspira.setShared(global.userpreferences, global.user.telp, obj.getString("user_telp"));

            LibInspira.setShared(global.userpreferences, global.user.role_isowner, obj.getString("role_isowner"));
            LibInspira.setShared(global.userpreferences, global.user.role_issales, obj.getString("role_issales"));
            LibInspira.setShared(global.userpreferences, global.user.role_setting, obj.getString("role_setting"));
            LibInspira.setShared(global.userpreferences, global.user.role_settingtarget, obj.getString("role_settingtarget"));
            LibInspira.setShared(global.userpreferences, global.user.role_salesorder, obj.getString("role_salesorder"));
            LibInspira.setShared(global.userpreferences, global.user.role_stockmonitoring, obj.getString("role_stockmonitoring"));
            LibInspira.setShared(global.userpreferences, global.user.role_pricelist, obj.getString("role_pricelist"));
            LibInspira.setShared(global.userpreferences, global.user.role_addscheduletask, obj.getString("role_addscheduletask"));
            LibInspira.setShared(global.userpreferences, global.user.role_salestracking, obj.getString("role_salestracking"));
            LibInspira.setShared(global.userpreferences, global.user.role_hpp, obj.getString("role_hpp"));
            LibInspira.setShared(global.userpreferences, global.user.role_crossbranch, obj.getString("role_crossbranch"));
            LibInspira.setShared(global.userpreferences, global.user.role_creategroup, obj.getString("role_creategroup"));

            LibInspira.setShared(global.settingpreferences, global.settings.interval, obj.getString("setting_interval"));
            LibInspira.setShared(global.settingpreferences, global.settings.radius, obj.getString("setting_radius"));
            LibInspira.setShared(global.settingpreferences, global.settings.tracking, obj.getString("setting_tracking"));
            LibInspira.setShared(global.settingpreferences, global.settings.jam_awal, obj.getString("setting_jamawal"));
            LibInspira.setShared(global.settingpreferences, global.settings.jam_akhir, obj.getString("setting_jamakhir"));
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Toast.makeText(Login.this, "Login Failed", Toast.LENGTH_LONG).show();
        }
    }

    /******************************************************************************
        Procedure : loginUser
        Author    : Tonny
        Date      : 26-Jul-2017
        Function  : Untuk berinteraksi dengan webservice untuk method loginUser
    ******************************************************************************/
    private class loginUser extends AsyncTask<String, Void, String> {
        String username = edtUsername.getText().toString();
        String password = edtPassword.getText().toString();

        JSONObject jsonObject;
        @Override
        protected String doInBackground(String... urls) {
            try {
                jsonObject = new JSONObject();
                jsonObject.put("username", username);
                jsonObject.put("password", password);
                jsonObject.put("token", LibInspira.getShared(global.userpreferences,global.user.token, ""));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return LibInspira.executePost(Login.this, urls[0], jsonObject);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("resultQuery", result);
            try {
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if(!obj.has("query")){
                            setdatauser(obj);

                            LibInspira.hideLoading();

                            if(LibInspira.getShared(global.userpreferences, global.user.tipe, "").equals("0"))
                            {
                                Intent intent = new Intent(Login.this, IndexInternal.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
                                finish();
                            }
                            else if(LibInspira.getShared(global.userpreferences, global.user.tipe, "").equals("1"))
                            {
                                Intent intent = new Intent(Login.this, IndexExternal.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
                                finish();
                            }
                        }
                        else
                        {
                            Toast.makeText(Login.this, "Login Failed", Toast.LENGTH_LONG).show();
                            LibInspira.hideLoading();
                        }
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
                Toast.makeText(Login.this, "Login Failed", Toast.LENGTH_LONG).show();
                LibInspira.hideLoading();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LibInspira.showLoading(Login.this, "Login", "Loading");
        }
    }

    /******************************************************************************
        Procedure : checkUser
        Author    : ADI
        Date      : 26-Jul-2017
        Function  : Untuk berinteraksi dengan webservice untuk method checkUser
    ******************************************************************************/
    private class checkUser extends AsyncTask<String, Void, String> {
        JSONObject jsonObject;
        @Override
        protected String doInBackground(String... urls) {
            try {
                jsonObject = new JSONObject();
                jsonObject.put("hash", LibInspira.getShared(global.userpreferences,global.user.hash,""));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return LibInspira.executePost(Login.this, urls[0], jsonObject);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("tes", result);
            try {
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if(!obj.has("query")){
                            LibInspira.hideLoading();
                            String success = obj.getString("success");
                            if(success.equals("true")){
                                setdatauser(obj);

                                if(LibInspira.getShared(global.userpreferences, global.user.tipe, "").equals("0"))
                                {
                                    Intent intent = new Intent(Login.this, IndexInternal.class);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
                                    finish();
                                }
                                else if(LibInspira.getShared(global.userpreferences, global.user.tipe, "").equals("1"))
                                {
                                    Intent intent = new Intent(Login.this, IndexExternal.class);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
                                    finish();
                                }
                            }
                            else
                            {
                                GlobalVar.clearDataUser();

                                edtUsername.setVisibility(View.VISIBLE);
                                edtPassword.setVisibility(View.VISIBLE);
                                btnSubmit.setVisibility(View.VISIBLE);
                                Toast.makeText(getBaseContext(), "User has login at another device", Toast.LENGTH_LONG).show();
                            }
                        }
                        else
                        {
                            edtUsername.setVisibility(View.VISIBLE);
                            edtPassword.setVisibility(View.VISIBLE);
                            btnSubmit.setVisibility(View.VISIBLE);
                            Toast.makeText(Login.this, "Login Failed", Toast.LENGTH_LONG).show();
                            LibInspira.hideLoading();
                        }
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
                edtUsername.setVisibility(View.VISIBLE);
                edtPassword.setVisibility(View.VISIBLE);
                btnSubmit.setVisibility(View.VISIBLE);
                Toast.makeText(Login.this, "Login Failed", Toast.LENGTH_LONG).show();
                LibInspira.hideLoading();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LibInspira.showLoading(Login.this, "Login", "Loading");
        }
    }
}
