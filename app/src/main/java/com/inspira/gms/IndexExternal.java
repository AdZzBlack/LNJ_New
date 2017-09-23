package com.inspira.gms;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
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
import layout.ChooseCustomerProspectingFragment;
import layout.ChooseGroupFragment;
import layout.ChoosePeriodeFragment;
import layout.ContactFragment;
import layout.DashboardExternalFragment;
import layout.DashboardInternalFragment;
import layout.FilterSalesOmzetFragment;
import layout.PenjualanFragment;
import layout.PriceListFragment;
import layout.SalesNavigationFragment;
import layout.SalesOrderListFragment;
import layout.SettingFragment;


public class IndexExternal extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static GlobalVar global;
    public static JSONObject jsonObject;   //added by Tonny @30-Jul-2017
    public  static TextView tvUsername, tvSales, tvTarget;  //modified by Tonny @02-Aug-2017
    public static NavigationView navigationView;
    private static Context context;  //added by Tonny @02-Aug-2017

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_index_external);

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
        LibInspira.AddFragment(this.getSupportFragmentManager(), R.id.fragment_container, new DashboardExternalFragment());
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        LibInspira.clearShared(global.salespreferences);
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
        tvSales.setVisibility(View.GONE);
        tvTarget.setVisibility(View.GONE);
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
            // Handle the camera action
            LibInspira.ReplaceFragment(getSupportFragmentManager(), R.id.fragment_container, new SettingFragment());  //added by Tonny @04-Aug-2017
        } else if (id == R.id.action_changepassword) {  //added by Tonny @30-Jul-2017
            LibInspira.ReplaceFragment(getSupportFragmentManager(), R.id.fragment_container, new ChangePasswordFragment());
        } else if (id == R.id.action_logout) {
            GlobalVar.clearDataUser();

            Intent intent = new Intent(IndexExternal.this, Login.class);
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

        if (id == R.id.nav_dashboard) {
            // Handle the camera action
            LibInspira.ReplaceFragment(getSupportFragmentManager(), R.id.fragment_container, new DashboardExternalFragment());  //added by Tonny @01-Aug-2017
        } else if (id == R.id.nav_catalogue) {
            LibInspira.ReplaceFragment(getSupportFragmentManager(), R.id.fragment_container, new PriceListFragment());  //added by Tonny @01-Aug-2017
        } else if (id == R.id.nav_cart) {

        } else if (id == R.id.nav_trackinginformation) {

        } else if (id == R.id.nav_accountreceivablereport) {

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }
}
