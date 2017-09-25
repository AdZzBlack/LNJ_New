package com.inspira.lnj;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import layout.ChangePasswordFragment;
import layout.DashboardInternalFragment;


public class IndexInternal extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static GlobalVar global;
    public static JSONObject jsonObject;   //added by Tonny @30-Jul-2017
    public  static TextView tvUsername, tvSales, tvTarget;  //modified by Tonny @02-Aug-2017
    public static NavigationView navigationView;
    private static Context context;  //added by Tonny @02-Aug-2017

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_index_internal);

        // Start Registering FCM
        Intent intent = new Intent(this, MyFirebaseInstanceIDService.class);
        startService(intent);

        global = new GlobalVar(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        context = getApplicationContext();
        LibInspira.AddFragment(this.getSupportFragmentManager(), R.id.fragment_container, new DashboardInternalFragment());
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setNavigationItemSelectedListener(this);
        Menu navmenu = navigationView.getMenu();
        if(LibInspira.getShared(global.userpreferences, global.user.role_settingtarget, "0").equals("0"))
        {
            navmenu.findItem(R.id.nav_target).setVisible(false);
        }

        RefreshUserData();

        //added by Shodiq @01-Aug-2017
        // Permission for enabling location feature only for SDK Marshmallow | Android 6
        if (Build.VERSION.SDK_INT >= 23)
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1600);
    }

    @Override
    protected void onResume() {
        super.onResume();
        RefreshUserData();
    }

    public static void RefreshUserData(){
        View navigationHeader = navigationView.getHeaderView(0);
        tvUsername = (TextView) navigationHeader.findViewById(R.id.tvUsername);
        tvUsername.setText(LibInspira.getShared(global.userpreferences, global.user.nama, "User").toUpperCase());
        tvSales = (TextView) navigationHeader.findViewById(R.id.tvSales);
        tvTarget = (TextView) navigationHeader.findViewById(R.id.tvTarget);
        //modified by Tonny @03-Aug-2017 function untuk get omzet dan target dijadikan satu
        String actionUrl = "Sales/getOmzetTarget/";
        new checkOmzetTarget().execute( actionUrl );
        tvSales = (TextView) navigationHeader.findViewById(R.id.tvSales);
        tvTarget = (TextView) navigationHeader.findViewById(R.id.tvTarget);
    }

    /******************************************************************************
        Class     : checkOmzetTarget
        Author    : Tonny
        Date      : 01-Aug-2017
        Function  : Untuk mendapatkan omzet dan target dari sales berdasarkan kode/nomor sales
    ******************************************************************************/
    private static class checkOmzetTarget extends AsyncTask<String, Void, String> {
        JSONObject jsonObject;

        @Override
        protected String doInBackground(String... urls) {
            try {
                jsonObject = new JSONObject();
                Log.d("kodesales: ", LibInspira.getShared(global.userpreferences, global.user.nomor_sales, ""));
                jsonObject.put("kodesales", LibInspira.getShared(global.userpreferences, global.user.nomor_sales, ""));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return LibInspira.executePost(context, urls[0], jsonObject);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //Log.d("omzettarget", result);  //remarked by Tonny @04-Aug-2017
            try {
                JSONArray jsonarray = new JSONArray(result);
                if (jsonarray.length() > 0) {

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_index_internal_settings, menu);

        if(LibInspira.getShared(global.userpreferences, global.user.role_setting, "0").equals("0"))
        {
            menu.findItem(R.id.action_settings).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {  //added by Tonny @30-Jul-2017

        } else if (id == R.id.action_changepassword) {  //added by Tonny @30-Jul-2017
            LibInspira.ReplaceFragment(getSupportFragmentManager(), R.id.fragment_container, new ChangePasswordFragment());
        } else if (id == R.id.action_logout) {
            GlobalVar.clearDataUser();

            Intent intent = new Intent(IndexInternal.this, Login.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        LibInspira.clearShared(global.temppreferences);

        if (id == R.id.nav_dashboard) {
            // Handle the camera action
            LibInspira.ReplaceFragment(getSupportFragmentManager(), R.id.fragment_container, new DashboardInternalFragment());  //added by Tonny @01-Aug-2017
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }
}
