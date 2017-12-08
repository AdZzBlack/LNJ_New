/******************************************************************************
    Author           : Tonny
    Description      : untuk mengatur atau menambahkan waypoint baru
    History          :

******************************************************************************/
package layout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.inspira.lnj.GlobalVar;
import com.inspira.lnj.LibInspira;
import com.inspira.lnj.R;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;

public class WaypointFragment extends Fragment implements GoogleMap.OnInfoWindowClickListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, android.location.LocationListener, View.OnClickListener{

    private static boolean isGPSEnabled;
    private static boolean isNetworkEnabled;

    private static LocationManager manager;
    private static LocationManager managerGPS;
    private static Location myLocation;
    private static Location myLocationGPS;

    private double getLatitude;
    private double getLongitude;

    private String placeName;
    private int radius;

    private GoogleMap mGoogleMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Marker mCurrLocationMarker, wpMarker;
    private Circle wpCircle;

    private HashMap<String, String> hashMap;
    private HashMap<String, HashMap<String, String>> extraMarkerInfo;

    private GlobalVar global;
    private JSONObject jsonObject;

    private GetWaypoints getWaypoints;
//    private ArrayList<Marker> arrListMarker;
//    private ArrayList<GoogleMap> arrListMap;

    public WaypointFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_form_set_location, container, false);
        getActivity().setTitle("View Waypoints");
        return v;
    }


    /*****************************************************************************/
    //OnAttach dijalankan pada saat fragment ini terpasang pada Activity penampungnya
    /*****************************************************************************/
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    //added by Tonny @15-Jul-2017
    //untuk mapping UI pada fragment, jangan dilakukan pada OnCreate, tapi dilakukan pada onActivityCreated
    @Override
    public void onActivityCreated(Bundle bundle){
        super.onActivityCreated(bundle);
        global = new GlobalVar(getActivity());
        FragmentManager fm = getChildFragmentManager();
//        SupportMapFragment fragment = (SupportMapFragment) fm.findFragmentById(R.id.map_container);
        getView().findViewById(R.id.btn_set);
        getView().findViewById(R.id.btn_set).setEnabled(false); //di-enable setelah memilih lokasi
        getView().findViewById(R.id.btn_set).setOnClickListener(this);

//        if (fragment == null) {
//            fragment = SupportMapFragment.newInstance();
//            fm.beginTransaction().replace(R.id.map_container, fragment).commit();
//            fragment.getMapAsync(this);
//        }else{
//            String actionUrl = "Track/getWayPoints/";
//            getWaypoints = new GetWaypoints();
//            getWaypoints.execute(actionUrl);
//        }

        //modified by Tonny @28-Nov-2017  akan diload terus untuk membersihkan semua
        SupportMapFragment fragment = SupportMapFragment.newInstance();
        fm.beginTransaction().replace(R.id.map_container, fragment).commit();
        fragment.getMapAsync(this);

        //added by Tonny @08-Nov-2017
        final SupportPlaceAutocompleteFragment autocompleteFragment  = (SupportPlaceAutocompleteFragment) fm.findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(final Place place) {
                // TODO: Get info about the selected place.
//                mGoogleMap.clear();
                removeCurrentLocationMarker();

                placeName = place.getName().toString();
                Log.wtf("Place: ", place.getName().toString());

                //Place current location marker
                LatLng latLng = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(placeName);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                markerOptions.draggable(true);
                mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

                mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        //untuk hide button direction dan show googlemaps
                        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
                        return false;
                    }
                });

                mGoogleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                    @Override
                    public void onMarkerDragStart(Marker marker) {
                        marker.hideInfoWindow();
                        autocompleteFragment.setText("");
                    }

                    @Override
                    public void onMarkerDrag(Marker marker) {
//                        circle.setCenter(marker.getPosition());
                    }

                    @Override
                    public void onMarkerDragEnd(Marker marker) {
                        marker.setTitle(marker.getPosition().toString());
                        marker.showInfoWindow();
                    }
                });

                //move map camera
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
                mGoogleMap.animateCamera(cameraUpdate);

                //enable btnSet
                getView().findViewById(R.id.btn_set).setEnabled(true);

                fillMapsTempData("", placeName, 0, place.getLatLng().latitude, place.getLatLng().longitude, 10, "");
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                placeName = "";
                Log.wtf("Error: ", "An error occurred: " + status);
            }
        });

        //added by Tonny @16-Nov-2017 jika button "x" pada autocomplete ditekan
        autocompleteFragment.getView().findViewById(R.id.place_autocomplete_clear_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // example : way to access view from PlaceAutoCompleteFragment
                        // ((EditText) autocompleteFragment.getView()
                        // .findViewById(R.id.place_autocomplete_search_input)).setText("");
                        autocompleteFragment.setText("");
                        removeCurrentLocationMarker();
                        view.setVisibility(View.GONE);
                        getView().findViewById(R.id.btn_set).setEnabled(false);
                        LibInspira.setShared(global.tempmapspreferences, global.tempMaps.mode, "update");
                        LibInspira.setShared(global.tempmapspreferences, global.tempMaps.placename, "");
                    }
                });

        //untuk membatasi region autocomplete
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setCountry("ID")
                .build();

        autocompleteFragment.setFilter(typeFilter);
    }

    //added by Tonny @23-Nov-2017
    public void fillMapsTempData(String _nomor, String _placeName, double _duration, double _latitude, double _longitude, double _radius, String _notes){
        //set nomor, placename, latitude, longitude shared
        if(_nomor.isEmpty()){
            LibInspira.setShared(global.tempmapspreferences, global.tempMaps.mode, "insert");
        }else{
            LibInspira.setShared(global.tempmapspreferences, global.tempMaps.mode, "update");
        }
        LibInspira.setShared(global.tempmapspreferences, global.tempMaps.nomor, _nomor);  //isi value dengan "" jika mode insert
        LibInspira.setShared(global.tempmapspreferences, global.tempMaps.placename, _placeName);
        LibInspira.setShared(global.tempmapspreferences, global.tempMaps.latitude, String.valueOf(_latitude));
        LibInspira.setShared(global.tempmapspreferences, global.tempMaps.longitude, String.valueOf(_longitude));
        LibInspira.setShared(global.tempmapspreferences, global.tempMaps.duration, String.valueOf(_duration));
        LibInspira.setShared(global.tempmapspreferences, global.tempMaps.radius, String.valueOf(_radius));
        LibInspira.setShared(global.tempmapspreferences, global.tempMaps.notes, _notes);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id==R.id.btn_set)
        {
            // TODO: Get lat lng and save it to tempmapspreferences and then go to FormSetDetailLocation
            LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new FormSetDetailLocationFragment());
        }
    }

    @Override
    public void onStop() {
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if(manager != null){
            manager.removeUpdates(this);
        }
        if(managerGPS != null){
            managerGPS.removeUpdates(this);
        }
        super.onStop();
    }

    @Override
    public void onLocationChanged(Location location) {
//        mLastLocation = location;
//        if (mCurrLocationMarker != null) {
//            mCurrLocationMarker.remove();
//        }

//        Log.d("map", "onLocationChanged: ");

//        getLatitude = location.getLatitude();
//        getLongitude = location.getLongitude();

//        //Place current location marker
//        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(latLng);
//        markerOptions.title("Your Location");
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
//        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);
//
////        move map camera
//        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
//        mGoogleMap.animateCamera(cameraUpdate);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getLatitude = 0.0;
        getLongitude = 0.0;

        Criteria criteriagps = new Criteria();
        Criteria criterianetwork = new Criteria();
        criteriagps.setAccuracy(Criteria.ACCURACY_FINE);
        criterianetwork.setAccuracy(Criteria.ACCURACY_COARSE);

        managerGPS = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
        managerGPS.getBestProvider(criteriagps, true);

        manager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
        manager.getBestProvider(criterianetwork, true);

        isGPSEnabled = managerGPS.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (isGPSEnabled || isNetworkEnabled) {
            if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            managerGPS.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            if (managerGPS != null || manager != null) {
                myLocationGPS = managerGPS.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                myLocation = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                Location location = null;
                if (myLocationGPS != null) {
                    location = myLocationGPS;
                } else if (myLocation != null) {
                    location = myLocation;
                }

                try
                {
                    getLatitude = location.getLatitude();
                    getLongitude = location.getLongitude();
                }
                catch (Exception e)
                {
                    getLatitude = 0.0;
                    getLongitude = 0.0;
                }

//                Toast.makeText(getActivity(), getLatitude + ", " + getLongitude, Toast.LENGTH_LONG).show();

                //added by Tonny @28-Nov-2017  diarahkan ke waypoint yang dibuat terakhir
                if(!LibInspira.getShared(global.tempmapspreferences, global.tempMaps.latitude, "").equals("") && !LibInspira.getShared(global.tempmapspreferences, global.tempMaps.latitude, "").equals("")){
                    LatLng latlng = new LatLng(Double.parseDouble(LibInspira.getShared(global.tempmapspreferences, global.tempMaps.latitude, "")),
                            Double.parseDouble(LibInspira.getShared(global.tempmapspreferences, global.tempMaps.longitude, "")));
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng, 16);
                    mGoogleMap.animateCamera(cameraUpdate);
                    LibInspira.clearShared(global.tempmapspreferences);
                }else{
                    LatLng latlng = new LatLng(getLatitude, getLongitude);
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng, 16);
                    mGoogleMap.animateCamera(cameraUpdate);
                }
            }
        }
        else
        {
//            Toast.makeText(getActivity(), getLatitude + ", " + getLongitude, Toast.LENGTH_LONG).show();

            LatLng latLng = new LatLng(getLatitude, getLongitude);
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Your Location");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

            //added by Tonny @28-Nov-2017  diarahkan ke waypoint yang dibuat terakhir
            if(!LibInspira.getShared(global.tempmapspreferences, global.tempMaps.latitude, "").equals("") && !LibInspira.getShared(global.tempmapspreferences, global.tempMaps.latitude, "").equals("")){
                LatLng latlng = new LatLng(Double.parseDouble(LibInspira.getShared(global.tempmapspreferences, global.tempMaps.latitude, "")),
                        Double.parseDouble(LibInspira.getShared(global.tempmapspreferences, global.tempMaps.longitude, "")));
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng, 16);
                mGoogleMap.animateCamera(cameraUpdate);
                LibInspira.clearShared(global.tempmapspreferences);
            }else{
                LatLng latlng = new LatLng(getLatitude, getLongitude);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng, 16);
                mGoogleMap.animateCamera(cameraUpdate);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        //Initialize Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);
        }

        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                // TODO Auto-generated method stub
//                lstLatLngs.add(point);
//                mGoogleMap.clear();
//                mGoogleMap.addMarker(new MarkerOptions().position(point));
//                MarkerOptions markerOptions = new MarkerOptions();
//                markerOptions.position(point);
//                markerOptions.title(placeName);
//                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
//                mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

            }
        });

        mGoogleMap.setOnInfoWindowClickListener(this);

        //added by Tonny @22-Nov-2017
        String actionUrl = "Track/getWayPoints/";
        getWaypoints = new GetWaypoints();
        getWaypoints.execute(actionUrl);
    }
    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getContext())
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mGoogleMap.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getActivity(), "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if(!LibInspira.getShared(global.tempmapspreferences, global.tempMaps.mode, "").equals("insert")){
            //fillMapsTempData(objNomor, objNama, Double.parseDouble(objDuration), Double.parseDouble(objLat), Double.parseDouble(objLng), Double.parseDouble(objRadius), objKeterangan);
            // Get extra data with marker ID
            Log.wtf("marker clicked ", marker.getId());
            HashMap<String, String> markerData = extraMarkerInfo.get(marker.getId());
            fillMapsTempData(markerData.get("nomor"), markerData.get("nama"), Double.parseDouble(markerData.get("duration")), Double.parseDouble(markerData.get("lat")), Double.parseDouble(markerData.get("lng")),
                    Double.parseDouble(markerData.get("radius")), markerData.get("keterangan"));
            LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new FormSetDetailLocationFragment());
        }
    }

    private class GetWaypoints extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            jsonObject = new JSONObject();
//                jsonObject.put("nomorthorderjual", LibInspira.getShared(global.userpreferences,global.user.checkin_nomorthsuratjalan,""));
            return LibInspira.executePost(getContext(), urls[0], jsonObject);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("getting waypoints", result);
            try {
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    //added by Tonny @23-Nov-2017  untuk refresh waypoints
                    if(wpMarker != null) wpMarker.remove();
                    if(wpCircle != null) wpCircle.remove();

                    removeCurrentLocationMarker();

                    extraMarkerInfo = new HashMap<>();
                    if (wpMarker != null) wpMarker.remove();
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        LibInspira.hideLoading();
                        if(!obj.has("query")){  //jika success
                            if(!obj.getString("latitude").equals("") && !obj.getString("longitude").equals("")){
                                final String objNomor = obj.getString("nomor");
                                final String objKode = obj.getString("kode");
                                final String objNama = obj.getString("nama");
                                final String objDuration = obj.getString("durasi");
                                final String objLat = obj.getString("latitude");
                                final String objLng = obj.getString("longitude");
                                final String objRadius = obj.getString("radius");
                                final String objKeterangan = obj.getString("keterangan");
                                String marker_desc = "lat: " + objLat + "\n" +
                                        "lng: " + objLng + "\n" +
                                        "durasi: " + objDuration + "sec \n" +
                                        "radius: " + objRadius + "m \n\n" +
                                        objKeterangan;
                                String marker_title = "(" + objKode + ") " + objNama;

                                // Just save the entire json hashmap into the external variable
                                hashMap = new HashMap<>();
                                hashMap.put("nomor", obj.getString("nomor"));
                                hashMap.put("kode", obj.getString("kode"));
                                hashMap.put("nama", obj.getString("nama"));
                                hashMap.put("duration", obj.getString("durasi"));
                                hashMap.put("lat", obj.getString("latitude"));
                                hashMap.put("lng", obj.getString("longitude"));
                                hashMap.put("radius", obj.getString("radius"));
                                hashMap.put("keterangan", obj.getString("keterangan"));

                                LatLng latLng = new LatLng(Double.parseDouble(objLat), Double.parseDouble(objLng));
                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(latLng);
                                markerOptions.title(marker_title);
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                                markerOptions.snippet(marker_desc);
                                markerOptions.draggable(false);
                                wpMarker = mGoogleMap.addMarker(markerOptions);
                                Log.wtf("Marker ID ", wpMarker.getId());

                                extraMarkerInfo.put(wpMarker.getId(), hashMap);

                                mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                    @Override
                                    public boolean onMarkerClick(Marker marker) {
                                        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
                                        return false;
                                    }
                                });

                                // ... get a map.
                                // Add a circle on marker
                                wpCircle = mGoogleMap.addCircle(new CircleOptions()
                                        .center(latLng)
                                        .radius(Double.parseDouble(obj.getString("radius")))
                //                        .strokeColor(Color.RED)
                                        .fillColor(0x70fffda3));

                                //untuk override info window pada marker
                                mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                                    @Override
                                    public View getInfoWindow(Marker arg0) {
                                        return null;
                                    }

                                    @Override
                                    public View getInfoContents(Marker marker) {

                                        LinearLayout info = new LinearLayout(getContext());
                                        info.setOrientation(LinearLayout.VERTICAL);

                                        TextView title = new TextView(getContext());
                                        title.setTextColor(Color.BLACK);
                                        title.setGravity(Gravity.CENTER);
                                        title.setTypeface(null, Typeface.BOLD);
                                        title.setText(marker.getTitle());

                                        TextView snippet = new TextView(getContext());
                                        snippet.setTextColor(Color.GRAY);
                                        snippet.setText(marker.getSnippet());

                                        info.addView(title);
                                        info.addView(snippet);

                                        return info;
                                    }
                                });

//                                mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
//                                    @Override
//                                    public void onInfoWindowClick(Marker marker) {
//                                        if(!LibInspira.getShared(global.tempmapspreferences, global.tempMaps.mode, "").equals("insert")){
//                                            //fillMapsTempData(objNomor, objNama, Double.parseDouble(objDuration), Double.parseDouble(objLat), Double.parseDouble(objLng), Double.parseDouble(objRadius), objKeterangan);
//                                            // Get extra data with marker ID
//                                            HashMap<String, String> markerData = extraMarkerInfo.get(marker.getId());
//                                            fillMapsTempData(markerData.get("nomor"), markerData.get("nama"), Double.parseDouble(markerData.get("duration")), Double.parseDouble(markerData.get("lat")), Double.parseDouble(markerData.get("lng")),
//                                                    Double.parseDouble(markerData.get("radius")), markerData.get("keterangan"));
//                                            LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new FormSetDetailLocationFragment());
//                                        }
//                                    }
//                                });
                            }
                        }
                        else
                        {
                            LibInspira.showLongToast(getContext(), "Fetching the data failed");
                        }
                    }
//                    int index = 0;
//                    for (Object value: arrListMarker){
//                        final Marker marker = (Marker) value;
//
//                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
                LibInspira.showLongToast(getContext(), e.getMessage());
                LibInspira.hideLoading();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LibInspira.showLoading(getContext(), "Loading Waypoints", "Loading");
        }
    }

    @Override
    public void onDestroy() {
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if(manager != null){
            manager.removeUpdates(this);
        }
        if(managerGPS != null){
            managerGPS.removeUpdates(this);
        }
        super.onDestroy();
        if (getWaypoints != null){
            getWaypoints.cancel(true);
        }
    }

    private void removeCurrentLocationMarker(){
        if(mCurrLocationMarker != null){
            mCurrLocationMarker.remove();
        }
    }
}
