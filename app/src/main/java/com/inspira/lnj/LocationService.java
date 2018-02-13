package com.inspira.lnj;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.inspira.lnj.IndexInternal.global;

public class LocationService extends Service implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    IBinder mBinder;

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    private double getLatitude;
    private double getLongitude;
    private double oldLatitude = 0.0;
    private double oldLongitude = 0.0;

    private boolean isMock;
    private boolean isNewLocation;
    private boolean canSave;
    private boolean isCheckin;

    private String trackingMode, jam_awal, jam_akhir;

    public static JSONObject jsonObject;

    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mLocationRef = mRootRef.child("location");

    @Override
    public IBinder onBind(Intent intent) {
        // A client is binding to the service with bindService()
        return mBinder;
    }

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            checkLocationPermission();
        }

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
            }
        }
        else {
            buildGoogleApiClient();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        trackingMode = settingsharedpreferences.getString("tracking", "");
//        jam_awal = settingsharedpreferences.getString("jam_awal", "0");
//        jam_akhir = settingsharedpreferences.getString("jam_akhir", "0");

        isMock = false;
        isNewLocation = true;
        isCheckin = false;  //added by Tonny @06-Feb-2018

        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        DatabaseReference mChildRef = mLocationRef.child(LibInspira.getShared(global.userpreferences, global.user.nomor, ""));
        mChildRef.child("lat").setValue("0");
        mChildRef.child("lon").setValue("0");

        Intent restartServiceTask = new Intent(getApplicationContext(),this.getClass());
        restartServiceTask.setPackage(getPackageName());
        PendingIntent restartPendingIntent = PendingIntent.getService(getApplicationContext(), 1,restartServiceTask, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager myAlarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        myAlarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartPendingIntent);

        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        DatabaseReference mChildRef = mLocationRef.child(LibInspira.getShared(global.userpreferences, global.user.nomor, ""));
        mChildRef.child("lat").setValue("0");
        mChildRef.child("lon").setValue("0");
    }

    @Override
    public void onLocationChanged(Location location) {
        if (Build.VERSION.SDK_INT >= 18) {
            isMock = location.isFromMockProvider();
        } else {
            isMock = Settings.Secure.getString(getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION).equals("0");
        }

        if(isMock){
            Toast.makeText(this, "GPS Spoofing Detected", Toast.LENGTH_LONG).show();
        }
        else{
            mLastLocation = location;

            getLatitude = location.getLatitude();
            getLongitude = location.getLongitude();

            Log.d("ok", "onLocationChanged: ");
            if(!LibInspira.getShared(global.userpreferences, global.user.nomor, "").equals(""))
            {
                Log.d("ok", getLatitude + ", " + getLongitude);
                DatabaseReference mChildRef = mLocationRef.child(LibInspira.getShared(global.userpreferences, global.user.nomor, ""));
                mChildRef.child("lat").setValue(String.valueOf(getLatitude));
                mChildRef.child("lon").setValue(String.valueOf(getLongitude));

                if(LibInspira.getShared(global.userpreferences, global.user.role_isdriver, "").equals("1"))
                {
                    double radius = getRadius(oldLatitude, oldLongitude, getLatitude, getLongitude);
                    if(radius>=5.0 && !LibInspira.getShared(global.userpreferences,global.user.checkin_nomortdsuratjalan,"").equals(""))
                    {
                        String actionUrl = "Track/InsertHistory/";
                        new updateLocation().execute(actionUrl);
                    }
                }

                //added by Tonny @01-Feb-2018
                //check driver position radius for insert, update on whcheckin (check in/out)
                if(LibInspira.getShared(global.userpreferences, global.user.role_isdriver, "").equals("1"))
                {
                    double radius = getRadius(oldLatitude, oldLongitude, getLatitude, getLongitude);
                    if(radius >= 0.1 && !LibInspira.getShared(global.datapreferences, global.data.deliveryorderlist, "").equals(""))
                    {
                        String data = LibInspira.getShared(global.datapreferences, global.data.deliveryorderlist, "");
                        String[] pieces = data.trim().split("\\|");
                        if(pieces.length > 1 && !pieces[0].equals(""))
                        {
                            for(int i=0 ; i < pieces.length ; i++) {
                                if (!pieces[i].equals("")) {
                                    String actionUrl = "Scanning/GetNextWP/";
                                    new GetNextWP(pieces[i].replace("D", "")).execute(actionUrl);
                                }
                            }
                        }
                    }
                }

//                checkingHours();
//                if (!isMock && canSave) {
//                    Log.d("ok", getLatitude + ", " + getLongitude);
//                    DatabaseReference mChildRef = mLocationRef.child(LibInspira.getShared(global.userpreferences, global.user.nomor, ""));
//                    mChildRef.child("lat").setValue(String.valueOf(getLatitude));
//                    mChildRef.child("lon").setValue(String.valueOf(getLongitude));
//
//                    String actionUrl = "Salestracking/sendLocationBEX/";
//                    new updateLocation().execute(actionUrl);
//                }
//                else
//                {
//                    DatabaseReference mChildRef = mLocationRef.child(LibInspira.getShared(global.userpreferences, global.user.nomor, ""));
//                    mChildRef.child("lat").setValue("0");
//                    mChildRef.child("lon").setValue("0");
//                }
            }

            isMock = false;
            isNewLocation = true;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setSmallestDisplacement(1);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    public void checkingHours()
    {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

            Date dateawal = sdf.parse(jam_awal);
            Date dateakhir = sdf.parse(jam_akhir);

            String date = sdf.format(new Date());
            Date datesekarang = sdf.parse(date);

            if (datesekarang.after(dateawal) && datesekarang.before(dateakhir)) {
                canSave = true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int res) {
//        mGoogleApiClient.connect();
//        Toast.makeText(this, "Google Play Services connection suspended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
//        mGoogleApiClient.connect();
//        Toast.makeText(this, "Google Play Services connection failed", Toast.LENGTH_SHORT).show();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    public static double getRadius(double lat1, double lng1, double lat2, double lng2) {
//        double earthRadius = 3958.75; // miles (or 6371.0 kilometers)
        double earthRadius = 6371.0;
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = earthRadius * c;

        return dist;
    }

    private class updateLocation extends AsyncTask<String, Void, String> {
        String user_nomor = LibInspira.getShared(global.userpreferences, global.user.nomor, "");
        String job_nomor = LibInspira.getShared(global.userpreferences,global.user.checkin_nomortdsuratjalan,"");
        String latitude = String.valueOf(getLatitude);
        String longitude = String.valueOf(getLongitude);

        @Override
        protected String doInBackground(String... urls) {
            try {
                jsonObject = new JSONObject();
                jsonObject.put("user_nomor", user_nomor);
                jsonObject.put("job_nomor", job_nomor);
                jsonObject.put("latitude", latitude);
                jsonObject.put("longitude", longitude);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return LibInspira.executePost(getApplicationContext(), urls[0], jsonObject);
        }

        @Override
        protected void onPostExecute(String result) {
            canSave = true;
            oldLatitude = getLatitude;
            oldLongitude = getLongitude;
        }

        @Override
        protected void onPreExecute() {
            canSave = false;
        }
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            //stopSelf(msg.arg1); <- don't use, ur gonna kill this
        }
    }

    private class GetNextWP extends AsyncTask<String, Void, String> {
        private String job_nomor;
        private GetNextWP(String _nomorDO)
        {
            job_nomor = _nomorDO;
        }
        String user_nomor = LibInspira.getShared(global.userpreferences, global.user.nomor, "");
//        String job_nomor = LibInspira.getShared(global.userpreferences,global.user.checkin_nomortdsuratjalan,"");

        @Override
        protected String doInBackground(String... urls) {
            try {
                jsonObject = new JSONObject();
                jsonObject.put("user_nomor", user_nomor);
                jsonObject.put("job_nomor", job_nomor);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return LibInspira.executePost(getApplicationContext(), urls[0], jsonObject);
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("tes", result);
            try {
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        LibInspira.hideLoading();
                        if(!obj.has("query")){  //jika success
//                            LibInspira.showShortToast(getApplicationContext(), "Next Checkpoint: " + obj.getString("namacheckpoint_next"));
                            String checkin_type = obj.getString("checkin_type");
                            double wpRadius = obj.getDouble("radius_next") / 1000;
                            double wpLat = obj.getDouble("latitude_next");
                            double wpLon = obj.getDouble("longitude_next");
                            double radius = getRadius(wpLat, wpLon, getLatitude, getLongitude);
                            String namacheckpoint = obj.getString("namacheckpoint_next");
                            String actionUrl = "Scanning/InsertCheckin/";
                            if((checkin_type.equals("IN") && radius <= wpRadius && !isCheckin)
                                    || (checkin_type.equals("OUT") && radius > wpRadius && isCheckin)){
                                Log.wtf("status", "inserting");
                                new InsertCheckin(namacheckpoint, checkin_type, job_nomor).execute(actionUrl);
                            }
                            Log.wtf("getNextWP: ", obj.getString("namacheckpoint_next"));
                        }else{
                            Log.wtf("error: ", obj.getString("message"));
                        }
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
                LibInspira.showLongToast(getApplicationContext(), e.getMessage());
            }
        }
    }

    private class InsertCheckin extends AsyncTask<String, Void, String> {
        private String checkpoint, checkinType, job_nomor;
        private InsertCheckin(String _checkpoint, String _type, String _nomorDO)
        {
            checkinType = _type;
            job_nomor = _nomorDO;
            checkpoint = _checkpoint;
        }
        String user_nomor = LibInspira.getShared(global.userpreferences, global.user.nomor, "");
        //        String job_nomor = LibInspira.getShared(global.userpreferences,global.user.checkin_nomortdsuratjalan,"");
        String latitude = String.valueOf(getLatitude);
        String longitude = String.valueOf(getLongitude);

        @Override
        protected String doInBackground(String... urls) {
            try {
                jsonObject = new JSONObject();
                jsonObject.put("nomorsopir", user_nomor);
                jsonObject.put("nomortdsuratjalan", job_nomor);
                jsonObject.put("latitude", latitude);
                jsonObject.put("longitude", longitude);
                jsonObject.put("checkpoint", checkpoint);
                jsonObject.put("checkin_type", checkinType);
                jsonObject.put("checkin_mode", "AUTO");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return LibInspira.executePost(getApplicationContext(), urls[0], jsonObject);
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("tes", result);
            try {
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        LibInspira.hideLoading();
                        if(!obj.has("query")){  //jika success
                            Log.wtf("Insert CheckIn: ","insert history success");
                            if(checkinType.equals("IN")){
                                isCheckin = true;
                            }else{
                                isCheckin = false;
                            }
                        }
                        else
                        {
                            Log.wtf("Checkin error: ", obj.getString("message"));
                        }
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
                LibInspira.showLongToast(getApplicationContext(), e.getMessage());
            }
        }
    }
}