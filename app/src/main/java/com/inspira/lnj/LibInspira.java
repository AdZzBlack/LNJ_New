/******************************************************************************
    Author           : Tonny (Created 08-Jul-2017)
    Description      : Library Inspira
******************************************************************************/
package com.inspira.lnj;

//import android.app.Fragment;  // is the Fragment class in the native version of the Android SDK. It was introduced in Android 3 (API 11)
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.Fragment; // is the Fragment class for compatibility for older version < API 11
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.support.v7.app.NotificationCompat;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import layout.ScalingUtilities;

/**
 * Created by Tonny on 7/8/2017.
 */

public class LibInspira {
    private static ProgressDialog loadingDialog;
    private static String hostUrl;
    public static String inspiraDateTimeFormat = "yyyy-MM-dd hh:mm:ss";  //added by Tonny @26-Aug-2017 format standar datetime pada database inspira
    public static String inspiraDateFormat = "yyyy-MM-dd";  //added by Tonny @26-Aug-2017 format standar datetime pada database inspira
    private static String dialogValue;


    public static void GoToActivity(String _activityName){

    }
    public static void GoToActivity(Integer _activityIndex){

    }

    public static void ReplaceFragment(FragmentManager _fragmentManager, Integer _fragmentContainerID, Fragment _fragment){
        FragmentTransaction fragmentTransaction = _fragmentManager.beginTransaction();
        //fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left);
        fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        fragmentTransaction.replace(_fragmentContainerID, _fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    //added by Tonny @01-Nov-2017
    public static void ReplaceFragmentNoBackStack(FragmentManager _fragmentManager, Integer _fragmentContainerID, Fragment _fragment){
        FragmentTransaction fragmentTransaction = _fragmentManager.beginTransaction();
        fragmentTransaction.replace(_fragmentContainerID, _fragment);
        fragmentTransaction.commit();
    }

    public static void AddFragment(FragmentManager _fragmentManager, Integer _fragmentContainerID, Fragment _fragment){
        FragmentTransaction fragmentTransaction = _fragmentManager.beginTransaction();
        //fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left);
        fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        fragmentTransaction.add(_fragmentContainerID, _fragment);
        //fragmentTransaction.addToBackStack(null);  //remarked by Tonny @08-Jul-2017, masih belum tau apakah diperlukan
        fragmentTransaction.commit();
    }

    public static void RemoveFragment(FragmentManager _fragmentManager, Integer _fragmentContainerID, Fragment _fragmentName){
        FragmentTransaction fragmentTransaction = _fragmentManager.beginTransaction();
        fragmentTransaction.remove(_fragmentName);
        fragmentTransaction.commit();
    }

    public static void BackFragment(FragmentManager _fragmentManager)
    {
        _fragmentManager.popBackStack();
    }

    public static void BackFragmentCount(FragmentManager _fragmentManager, Integer _count){
        for(int i = 0; i < _count; ++i) {
            _fragmentManager.popBackStack();
        }
    }

    //added by Tonny @15-Jul-2017
    public static void showShortToast(Context _context, String _message){
        Context context = _context;
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, _message, duration);
        toast.show();
    }

    //added by Tonny @15-Jul-2017
    public static void showLongToast(Context _context, String _message){
        Context context = _context;
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, _message, duration);
        toast.show();
    }

    //added by ADI @30-Jul-2017
    public static Boolean contains(String _textData, String _textSearch)
    {
        Boolean showData = true;
        String[] piecesSearch = _textSearch.toLowerCase().trim().split("\\ ");
        String[] piecesData = _textData.toLowerCase().trim().split("\\ ");
        ArrayList<Boolean> checked = new ArrayList<Boolean>();
        for(int i=0; i<piecesSearch.length; i++)
        {
            int ctrFound = 0;
            for(int j=0; j<piecesData.length; j++)
            {
                if(piecesData[j].contains(piecesSearch[i])) ctrFound++;
            }
            if(ctrFound==0) checked.add(false);
            else checked.add(true);
        }
        for(int j=0; j<checked.size(); j++)
        {
            if(!checked.get(j)) showData = false;
        }
        return showData;
    }

    //added by ADI @26-Jul-2017
    public static String delimeter(String _strNumber)
    {
        DecimalFormat format=new DecimalFormat("#,###");
        if(_strNumber.equals("null") || _strNumber.equals("")) return "-";
        Double Raw = Double.parseDouble(_strNumber);
        String result = String.valueOf(format.format(Raw));
        return result;
    }

    //added by ADI @26-Jul-2017
    public static String delimeter(String _strNumber, Boolean _withCommas)
    {
        DecimalFormat format;

        if(_withCommas) format = new DecimalFormat("#,###.##");
        else format = new DecimalFormat("#,###");

        if(_strNumber.equals("null")) return "-";
        Double Raw = Double.parseDouble(_strNumber);
        String result = String.valueOf(format.format(Raw));
        return result;
    }

    //function digunakan pada addtextchangedlistener untuk melakukan format angka ketika menulis
    //added by ADI @26-Aug-2017
    public static void formatNumberEditText(EditText _tv, TextWatcher _tw, Boolean _allowZero, Boolean _withCommas)
    {
        String value = _tv.getText().toString();
        try
        {
            _tv.removeTextChangedListener(_tw);

            if (value != null && !value.equals(""))
            {

                if(value.startsWith(",")){
                    _tv.setText("0,");
                }
                if(!_allowZero)
                {
                    if(value.startsWith("0") && !value.startsWith("0,")){
                        _tv.setText("");
                    }
                }


                String str = _tv.getText().toString();
                if (!value.equals(""))
                    _tv.setText(delimeter(str.replace(",", ""), _withCommas));
                _tv.setSelection(_tv.getText().toString().length());
            }
            _tv.addTextChangedListener(_tw);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            _tv.addTextChangedListener(_tw);
        }
    }

    public static void createNotification(Application _activity, int _icon, Context  _context, String _title, String _content)
    {
        //contoh pengisian, _icon = R.drawable.babies_logo;
        final Intent emptyIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(_context, 0, emptyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        android.support.v4.app.NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(_activity)
                        .setSmallIcon(_icon)
                        .setContentTitle(_title)
                        .setContentText(_content)
                        .setContentIntent(pendingIntent);
        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        NotificationManager notificationManager = (NotificationManager) _context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, mBuilder.build());

    }

    //added by ADI @26-Jul-2017
    public static boolean isNetworkAvaliable(Context _context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if ((connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null && connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED) || (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI) != null && connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    //added by ADI @02-Aug-2017
    public static String getVersion(Context _context)
    {
        String version = "";
        try
        {
            PackageInfo pInfo = _context.getPackageManager().getPackageInfo(_context.getPackageName(), 0);
            version = pInfo.versionName;
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    //added by Adi @24-Jul-2017
    public static String getShared(SharedPreferences _preference, String _key, String _defaultValue) {
        return _preference.getString(_key, _defaultValue);
    }

    //added by Adi @24-Jul-2017
    public static void setShared(SharedPreferences _preference, String _key, String _param) {
        SharedPreferences.Editor editor = _preference.edit();
        editor.putString(_key, _param);
        //editor.commit();
        editor.apply();
    }

    //added by Adi @24-Jul-2017
    public static void clearShared(SharedPreferences _preference) {
        //preference.edit().clear().commit();
        _preference.edit().clear().apply();
    }

    //added by ADI @26-Jul-2017
    public static void hideLoading()
    {
        loadingDialog.dismiss();
    }

    //added by ADI @26-Jul-2017
    public static void showLoading(Context _context, String _title, String _message)
    {
        loadingDialog = new ProgressDialog(_context);
        loadingDialog.setTitle(_title);
        loadingDialog.setMessage(_message);
        loadingDialog.setCancelable(true);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.show();
    }

    //added by ADI @26-Jul-2017
    // Execute POST JSON and Retrieve Data JSON
    public static String executePost(Context _context, String _targetURL, JSONObject _jsonObject){
        GlobalVar global = new GlobalVar(_context);
        String url = getShared(global.sharedpreferences, global.shared.server, "");
        hostUrl = "https://" + url + GlobalVar.webserviceURL;

        Log.d("host", hostUrl + _targetURL);

        InputStream inputStream = null;
        String result = "";
        try {

            // 1. create HttpClient
            HttpClient httpClient = new DefaultHttpClient();
            HttpParams httpParameters = httpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);  //modified by Tonny @07-Sep-2017 5000 --> 10000
            HttpConnectionParams.setSoTimeout(httpParameters, 10000);  //modified by Tonny @07-Sep-2017 5000 --> 10000
            HttpConnectionParams.setTcpNoDelay(httpParameters, true);

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost( hostUrl + _targetURL );

            // 3. convert JSONObject to JSON to String
            String json = _jsonObject.toString();
            Log.d("json_obj_LibInspira",json);

            // 4. ** Alternative way to convert Person object to JSON string usin Jackson Lib
            // ObjectMapper mapper = new ObjectMapper();
            // json = mapper.writeValueAsString(person);

            // 5. set json to StringEntity
            StringEntity stringEntity = new StringEntity(json);

            // 6. set httpPost Entity
            httpPost.setEntity(stringEntity);

            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpClient.execute(httpPost);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
//            Log.d("InputStream", e.getLocalizedMessage());
        }

        // 11. return result
        return result;
    }

    //added by ADI @26-Jul-2017
    // Execute POST JSON and Retrieve Data JSON
    public static String executePost(Context _context, String _targetURL, JSONObject _jsonObject, int _timeoutMiliSecond){
        GlobalVar global = new GlobalVar(_context);
        String url = getShared(global.sharedpreferences, "server", "");
        hostUrl = "https://" + url + GlobalVar.webserviceURL;

        Log.d("host", hostUrl + _targetURL);

        InputStream inputStream = null;
        String result = "";
        try {

            // 1. create HttpClient
            HttpClient httpClient = new DefaultHttpClient();
            HttpParams httpParameters = httpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, _timeoutMiliSecond);
            HttpConnectionParams.setSoTimeout(httpParameters, _timeoutMiliSecond);
            HttpConnectionParams.setTcpNoDelay(httpParameters, true);

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost( hostUrl + _targetURL );

            // 3. convert JSONObject to JSON to String
            String json = _jsonObject.toString();

            // 4. ** Alternative way to convert Person object to JSON string usin Jackson Lib
            // ObjectMapper mapper = new ObjectMapper();
            // json = mapper.writeValueAsString(person);

            // 5. set json to StringEntity
            StringEntity stringEntity = new StringEntity(json);

            // 6. set httpPost Entity
            httpPost.setEntity(stringEntity);

            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpClient.execute(httpPost);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
//            Log.d("InputStream", e.getLocalizedMessage());
        }

        // 11. return result
        return result;
    }

    public static String executePost_local(Context _context, String _targetURL, JSONObject _jsonObject){
        GlobalVar global = new GlobalVar(_context);
        //String url = getShared(global.sharedpreferences, global.shared.server, "");
        hostUrl = GlobalVar.LOCAL_SERVER_URL+"/wsCello/cello/index.php/api/";

        Log.d("host", hostUrl + _targetURL);

        InputStream inputStream = null;
        String result = "";
        try {

            // 1. create HttpClient
            HttpClient httpClient = new DefaultHttpClient();
            HttpParams httpParameters = httpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);  //modified by Tonny @07-Sep-2017 5000 --> 10000
            HttpConnectionParams.setSoTimeout(httpParameters, 10000);  //modified by Tonny @07-Sep-2017 5000 --> 10000
            HttpConnectionParams.setTcpNoDelay(httpParameters, true);

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost( hostUrl + _targetURL );

            // 3. convert JSONObject to JSON to String
            String json = _jsonObject.toString();
            Log.d("json_obj_LibInspira",json);

            // 4. ** Alternative way to convert Person object to JSON string usin Jackson Lib
            // ObjectMapper mapper = new ObjectMapper();
            // json = mapper.writeValueAsString(person);

            // 5. set json to StringEntity
            StringEntity stringEntity = new StringEntity(json);

            // 6. set httpPost Entity
            httpPost.setEntity(stringEntity);

            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpClient.execute(httpPost);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
//            Log.d("InputStream", e.getLocalizedMessage());
        }

        // 11. return result
        return result;
    }

    private static String convertInputStreamToString(InputStream _inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(_inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        _inputStream.close();
        return result;
    }


    public static String decodeFile(String name, String path, int DESIREDWIDTH, int DESIREDHEIGHT) {
        String strMyImagePath = null;
        Bitmap scaledBitmap = null;

        try {
            // Part 1: Decode image
            Bitmap unscaledBitmap = ScalingUtilities.decodeFile(path, DESIREDWIDTH, DESIREDHEIGHT, ScalingUtilities.ScalingLogic.FIT);

            if (!(unscaledBitmap.getWidth() <= DESIREDWIDTH && unscaledBitmap.getHeight() <= DESIREDHEIGHT)) {
                // Part 2: Scale image
                scaledBitmap = ScalingUtilities.createScaledBitmap(unscaledBitmap, DESIREDWIDTH, DESIREDHEIGHT, ScalingUtilities.ScalingLogic.FIT);
            } else {
                unscaledBitmap.recycle();
                return path;
            }

            // Store to tmp file

            String extr = Environment.getExternalStorageDirectory().toString();
            File mFolder = new File(extr + "/TMMFOLDER");
            if (!mFolder.exists()) {
                mFolder.mkdir();
            }

            String s = name;

            File f = new File(mFolder.getAbsolutePath(), s);

            strMyImagePath = f.getAbsolutePath();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(f);
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, fos);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {

                e.printStackTrace();
            } catch (Exception e) {

                e.printStackTrace();
            }

            scaledBitmap.recycle();
        } catch (Throwable e) {
        }

        if (strMyImagePath == null) {
            return path;
        }
        return strMyImagePath;

    }

    // added by shodiq @1-Aug-2017
    // find the name of fake location app
    // if it does not exist return null
    public static String findMockLocationApp(Context context) {
        String appName = null;
        PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo applicationInfo : packages) {
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(applicationInfo.packageName, PackageManager.GET_PERMISSIONS);
                String[] requestedPermissions = packageInfo.requestedPermissions;

                if (requestedPermissions != null) {
                    for (int i = 0; i < requestedPermissions.length; i++) {
                        if (requestedPermissions[i].equals("android.permission.ACCESS_MOCK_LOCATION")) {
                            //&& !applicationInfo.packageName.equals(context.getPackageName()
                            appName += (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : "(unknown)");
                        }
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {

            }
        }

        return appName;
    }

    // added by shodiq @1-Aug-2017
    // get Mock Location of android developer setting
    public static boolean isMockSettingsON(Context context) {
        if (Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ALLOW_MOCK_LOCATION).equals("0"))
            return false;
        else
            return true;
    }

    //added by Tonny @07-Aug-2017  untuk convert integer menjadi nama bulan
    public static String getMonth(String _month) {
        int _intMonth = Integer.parseInt(_month);
        return new DateFormatSymbols().getMonths()[_intMonth-1];
    }

    public static void alertbox(String _title, String _message, final Activity _activity, final Runnable _commandOK, final Runnable _commandCancel) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(_activity);
        alertDialog.setTitle(_title);
        alertDialog.setMessage(_message);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (_commandOK != null)
                    _commandOK.run();
            } });
        if (_commandCancel != null){
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    _commandCancel.run();
                } });}
        alertDialog.show();
    }

    public static void alertBoxYesNo(String _title, String _message, final Activity _activity, final Runnable _commandYES, final Runnable _commandNO){
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(_activity);
        alertDialog.setTitle(_title);
        alertDialog.setMessage(_message);
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (_commandYES != null)
                    _commandYES.run();
            } });
        if (_commandNO != null){
            alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    _commandNO.run();
                } });}
        alertDialog.show();
    }

    //added by Tonny @25-Aug-2017  untuk mendapatkan tanggal hari ini format default (yyyy/MM/dd)
    public static String getCurrentDate(){
        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

    //added by Tonny @25-Aug-2017  untuk mendapatkan tanggal hari ini format custom
    public static String getCurrentDate(String _dateFormat){
        Calendar c = Calendar.getInstance();
        _dateFormat = _dateFormat.replace("Y", "y");  //added by Tonny @05-Sep-2017
        _dateFormat = _dateFormat.replace("D", "d");  //added by Tonny @05-Sep-2017
//        System.out.println("Current time => " + c.getTime());

        SimpleDateFormat df = new SimpleDateFormat(_dateFormat);
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

    //added by Tonny @26-Aug-2017 untuk mendapatkan tanggal pertama pada bulan ini dengan format tertentu
    public static String getFirstDateInSpecificMonth(String _strDate, String _newFormat){
        SimpleDateFormat sdf = new SimpleDateFormat(inspiraDateFormat);
        String newDate = "";
        try {
            Date date = sdf.parse(_strDate);
            String strDate = "01";
            String month = new SimpleDateFormat("MM").format(date);
            String year = new SimpleDateFormat("yyyy").format(date);
            newDate = year  + "-" + month + "-" + strDate;
        } catch (Exception e) {
            e.printStackTrace();
        }
        newDate = FormatDateBasedOnInspiraDateFormat(newDate, _newFormat);
        return newDate;
    }

    public static String getFirstDateInSpecificYear(String _strDate, String _newFormat){
//        Calendar c = Calendar.getInstance();   // this takes current date
//        c.set(Calendar.DAY_OF_YEAR, 1);
//        SimpleDateFormat df = new SimpleDateFormat(_newFormat);
//        String formattedDate = df.format(c.getTime());
//        return formattedDate;
        SimpleDateFormat sdf = new SimpleDateFormat(inspiraDateFormat);
        String newDate = "";
        try {
            Date date = sdf.parse(_strDate);
            String strDate = "01";
            String month = "01";
            String year = new SimpleDateFormat("yyyy").format(date);
            newDate = year  + "-" + month + "-" + strDate;
        } catch (Exception e) {
            e.printStackTrace();
        }
        newDate = FormatDateBasedOnInspiraDateFormat(newDate, _newFormat);
        return newDate;
    }

    //added by Tonny @26-Aug-2017
    //untuk melakukan format datetime berdasarkan format db inspira

    public static String FormatDateBasedOnInspiraDateFormat(String _strDate, String _newFormat){
        SimpleDateFormat currentFormat = new SimpleDateFormat(inspiraDateFormat);
        if (_strDate.length() > 10){ //jika _strDate berformat yyyy/MM/dd hh:mm:ss, maka gunakan inspiraDateTimeFormat
            currentFormat = new SimpleDateFormat(inspiraDateTimeFormat);
        }
        Date date = null;
        try {
            date = currentFormat.parse(_strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(date == null){
            return "";
        }
        SimpleDateFormat newFormat = new SimpleDateFormat(_newFormat);
        String strDate = newFormat.format(date);
        return strDate;
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    //added by Tonny @28-Nov-2017  untuk cek jika jarak melebihi radius
    public static boolean isDistanceOverRadius(double oldLatitude, double oldLongitude, double newLatitude, double newLongitude, double radiusInMetre) {
        double theta = oldLongitude - newLongitude;
        double dist = Math.sin(deg2rad(oldLatitude))
                * Math.sin(deg2rad(newLatitude))
                + Math.cos(deg2rad(oldLatitude))
                * Math.cos(deg2rad(newLatitude))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515; // distance in Kilometers
        dist = dist * 1000; // distance in meters
        Log.i("Distance value: ", String.valueOf(dist));
//        LibInspira.ShowLongToast(getApplicationContext(), "old location: " + oldLatitude + " - " + oldLongitude);
//        LibInspira.ShowLongToast(getApplicationContext(), "new location: " + newLatitude + " - " + newLongitude);
//        LibInspira.ShowLongToast(getApplicationContext(), "location distance: " + dist);
//        LibInspira.ShowLongToast(getApplicationContext(), "location radius: " + radiusInMetre);

        return dist > radiusInMetre;
    }

    public static String getDialogValue(boolean _isNumeric) {
        if(_isNumeric && dialogValue.equals("")){
            dialogValue = "0";
        }
        return dialogValue;
    }

    public static void setDialogValue(String _dialogValue) {
        dialogValue = _dialogValue;
    }
    //added by Tonny @09-Oct-2017
    //untuk menampilkan dialog untuk input numeric
    public static void showNumericInputDialog(String _title, String _message, final Activity _activity, final Context _context, final Runnable _commandOK, final Runnable _commandCancel) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(_activity);
        final EditText editText = new EditText(_context);
        editText.setKeyListener(DigitsKeyListener.getInstance());
        alertDialog.setTitle(_title);
        alertDialog.setMessage(_message);
        alertDialog.setView(editText);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (_commandOK != null)
                    setDialogValue(editText.getText().toString());
                _commandOK.run();
            } });
        if (_commandCancel != null){
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    _commandCancel.run();
                } });}
        alertDialog.show();
    }

    //added by Tonny @27-Dec-2017
    public static void showInputDialog(String _title, String _message, final Activity _activity, final Context _context, final Runnable _commandOK, final Runnable _commandCancel) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(_activity);
        final EditText editText = new EditText(_context);
        alertDialog.setTitle(_title);
        alertDialog.setMessage(_message);
        alertDialog.setView(editText);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (_commandOK != null)
                    setDialogValue(editText.getText().toString());
                _commandOK.run();
            } });
        if (_commandCancel != null){
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    _commandCancel.run();
                } });}
        alertDialog.show();
    }

    //added by Tonny @14-Dec-2017  // untuk pengecekan data (input) kembar dalam sebuah datapreferences
    public static boolean isAlreadyOnList(String _data, String _newdata, String _splitter){
        boolean result = false;
        List<String> dataList = new ArrayList<String>(Arrays.asList(_data.split(_splitter)));
        for (String value :
                dataList) {
            if (value.equals(_newdata)) {
                result = true;
            }
        }
        return result;
    }

    public static String readTextFile(Context _context, String _fileName){
        String text = "";
        List<String> mLines = new ArrayList<>();
        InputStream inputStream = null;
        try {
            inputStream = _context.getAssets().open(_fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) //mLines.add(line);
                text = text + line + "\n";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text;
    }

    public static String boldText(String _text){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            _text = Html.fromHtml("<b>"+ _text +"</b>", Html.FROM_HTML_MODE_LEGACY).toString();
        } else {
            _text = Html.fromHtml("<b>"+ _text +"</b>").toString();
        }
        return _text;
    }

    public static File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath(), ".LNJ");
        if(!storageDir.exists()){
            storageDir.mkdirs();
        }

        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );
        return image;
    }
}
