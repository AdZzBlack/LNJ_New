package com.inspira.lnj;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
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
import layout.ChatFragment;
import layout.ChooseGroupFragment;
import layout.ChooseUserFragment;
import layout.ContactFragment;
import layout.DashboardInternalFragment;
import layout.FormPhotoEmptyContainer;
import layout.SettingFragment;


public class IndexInternal extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static GlobalVar global;
    public static JSONObject jsonObject;   //added by Tonny @30-Jul-2017
    public  static TextView tvUsername;
    public static NavigationView navigationView;
    private static Context context;  //added by Tonny @02-Aug-2017

    public static ChatFragment chatFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_index_internal);

        startService(new Intent(getApplicationContext(), BackgroundTask.class));
        chatFrag = new ChatFragment();
        chatFrag.setup(this.getApplicationContext());

        // Start Registering FCM
        Intent intent = new Intent(this, MyFirebaseInstanceIDService.class);
        startService(intent);

        global = new GlobalVar(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        RefreshUserData();

        //added by Shodiq @01-Aug-2017
        // Permission for enabling location feature only for SDK Marshmallow | Android 6
        if (Build.VERSION.SDK_INT >= 23)
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE}, 1600);

        if(LibInspira.getShared(global.userpreferences, global.user.role_cantracked, "").equals("1"))
        {
            startService(new Intent(this, LocationService.class));
        }
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
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {  //modified by Tonny @01-Oct-2017  pengecekan jika drawer null
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_index_internal_settings, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_doc_done).setVisible(false);
        menu.findItem(R.id.action_settings).setVisible(true);
        menu.findItem(R.id.action_logout).setVisible(true);
        menu.findItem(R.id.action_changepassword).setVisible(true);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {  //added by Tonny @30-Jul-2017
            LibInspira.ReplaceFragment(getSupportFragmentManager(), R.id.fragment_container, new SettingFragment());  //added by Tonny @04-Aug-2017
        } else if (id == R.id.action_changepassword) {  //added by Tonny @30-Jul-2017
            LibInspira.ReplaceFragment(getSupportFragmentManager(), R.id.fragment_container, new ChangePasswordFragment());
        } else if (id == R.id.action_logout) {
            GlobalVar.clearDataUser();

            Intent intent = new Intent(IndexInternal.this, Login.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            finish();
            return true;
        } else if (id == R.id.action_releasenote){  //added by Tonny @11-Jan-2018
            Context context = IndexInternal.this;
            PackageInfo pInfo = null;
            try {
                pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            String version = pInfo.versionName;
            LibInspira.alertbox(LibInspira.boldText("Release Notes " + "(" + version + ")"), LibInspira.readTextFile(context, "releasenote.txt")
                    , this, new Runnable() {
                @Override
                public void run() {
                    //do nothing
                }
            }, null);
        } else if (id == R.id.action_doc_done){  //added by Tonny @09-Jan-2018 muncul pada saat digunakan di scan docs utk kembali ke dashboard
//            LibInspira.ReplaceFragmentNoBackStack(getSupportFragmentManager(), R.id.fragment_container, new DashboardInternalFragment());
            LibInspira.clearShared(global.temppreferences);  //added by Tonny @09-Jan-2018
            LibInspira.BackFragment(getSupportFragmentManager());
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
            LibInspira.ReplaceFragment(getSupportFragmentManager(), R.id.fragment_container, new DashboardInternalFragment());  //added by Tonny @01-Aug-2017
//            LibInspira.ReplaceFragment(getSupportFragmentManager(), R.id.fragment_container, new FormPhotoEmptyContainer());
        }
        else if (id == R.id.nav_chat) {
            LibInspira.setShared(global.sharedpreferences, global.shared.position, "contact");
            LibInspira.ReplaceFragment(getSupportFragmentManager(), R.id.fragment_container, new ContactFragment());
        }
        else if (id==R.id.nav_groupmessage){  //added by Tonny @23-Aug-2017
            LibInspira.setShared(global.sharedpreferences, global.shared.position, "conversation");
            LibInspira.ReplaceFragment(getSupportFragmentManager(), R.id.fragment_container, new ChooseGroupFragment());
        }
        else if (id == R.id.nav_tracking) {
            LibInspira.setShared(global.sharedpreferences, global.shared.position, "tracking");
            LibInspira.ReplaceFragment(getSupportFragmentManager(), R.id.fragment_container, new ChooseUserFragment());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }
}
