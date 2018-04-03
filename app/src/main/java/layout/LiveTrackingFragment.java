package layout;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.AvoidType;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.constant.Unit;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.inspira.lnj.GlobalVar;
import com.inspira.lnj.LibInspira;
import com.inspira.lnj.R;

import java.util.ArrayList;

public class LiveTrackingFragment extends Fragment implements OnMapReadyCallback,
        LocationListener,
        View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnCameraMoveStartedListener{

    private Marker[] mCurrLocationMarker;
    private Marker mMyLocationMarker;
    private Polyline mDirection;
    private GoogleMap mMap;
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference(); // untuk mendaparkan root dari database di firebase
    private DatabaseReference mLocationRef = mRootRef.child("location");  //untuk mendapatkan node (field) by name dari database firebase
    private DatabaseReference[] mChildRef;
    private LatLng[] positionNow;
    private LatLng mylocation;

    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;

    private int cameratype = 2;

    public static GlobalVar global;

    public static LiveTrackingFragment newInstance(String _name) {
        LiveTrackingFragment newFragment = new LiveTrackingFragment();
        Bundle args = new Bundle();
        args.putString("name", _name);
        newFragment.setArguments(args);
        return newFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_salestracking, container, false);
        getActivity().setTitle("Live Tracking - " + getArguments().getString("name"));

        global = new GlobalVar(getContext());

        mChildRef = new DatabaseReference[Integer.parseInt(LibInspira.getShared(global.temppreferences, global.temp.selected_count, ""))];
        mCurrLocationMarker = new Marker[Integer.parseInt(LibInspira.getShared(global.temppreferences, global.temp.selected_count, ""))];
        positionNow = new LatLng[Integer.parseInt(LibInspira.getShared(global.temppreferences, global.temp.selected_count, ""))];

        String[] pieces = LibInspira.getShared(global.temppreferences, global.temp.selected_nomor_user, "").trim().split("\\|");
        for(int i=0 ; i < pieces.length ; i++) {
            if (!pieces[i].equals("")) {
                mChildRef[i] = mLocationRef.child(pieces[i]);
            }
        }

        if(LibInspira.getShared(global.temppreferences, global.temp.selected_count, "").equals("1"))
        {
            cameratype = 2;
            moveCamera();
        }
        else
        {
            cameratype = 3;
            moveCamera();
        }

        return v;
    }

    public void onClick(View v) {
        int id = v.getId();
        v.startAnimation(global.buttoneffect);
        if(id== R.id.btn_followme)
        {
            cameratype = 1;
            moveCamera();
        }
        else if(id==R.id.btn_followtarget)
        {
            cameratype = 2;
            moveCamera();
        }
    }

    public void onMapReady(final GoogleMap map) {
        mMap = map;
        mMap.setOnCameraMoveStartedListener(this);
        mMap.setTrafficEnabled(true);
        for(int i=0; i< Integer.parseInt(LibInspira.getShared(global.temppreferences, global.temp.selected_count, "")); i++)
        {
            final int finalI = i;
            mChildRef[i].addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child("lat").getValue(String.class)!=null)
                    {
                        final LatLng latLng = new LatLng(Double.parseDouble(dataSnapshot.child("lat").getValue(String.class)), Double.parseDouble(dataSnapshot.child("lon").getValue(String.class)));

                        if (mCurrLocationMarker[finalI] != null) {
                            mCurrLocationMarker[finalI].setPosition(latLng);

                            final Handler handler = new Handler();
                            final long start = SystemClock.uptimeMillis();
                            Projection proj = mMap.getProjection();
                            Point startPoint = proj.toScreenLocation(positionNow[finalI]);
                            startPoint.offset(0, -100);
                            final LatLng startLatLng = proj.fromScreenLocation(startPoint);
                            final long duration = 1500;
                            final BounceInterpolator interpolator = new BounceInterpolator();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("ok", "run: ");
                                    long elapsed = SystemClock.uptimeMillis() - start;
                                    float t = interpolator.getInterpolation((float) elapsed / duration);
                                    double lng = t * latLng.longitude + (1 - t) * positionNow[finalI].longitude;
                                    double lat = t * latLng.latitude + (1 - t) * positionNow[finalI].latitude;
                                    positionNow[finalI] = new LatLng(lat, lng);
                                    mCurrLocationMarker[finalI].setPosition(positionNow[finalI]);

                                    moveCamera();

                                    if (t < 1.0) {
                                        // Post again 16ms later.
                                        handler.postDelayed(this, 16);
                                    }
                                }
                            });
                        }
                        else
                        {
                            String[] pieces = LibInspira.getShared(global.temppreferences, global.temp.selected_nama_user, "").trim().split("\\|");

                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.rotation(0);
                            markerOptions.position(latLng);
                            markerOptions.title(pieces[finalI]);

                            mCurrLocationMarker[finalI] = mMap.addMarker(markerOptions);
                            positionNow[finalI] = latLng;

                            moveCamera();

                            if(LibInspira.getShared(global.temppreferences, global.temp.selected_count, "").equals("1"))
                            {
                                drawDirection();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    /*public LatLng interpolate(float fraction, LatLng a, LatLng b) {
        double lat = (b.latitude - a.latitude) * fraction + a.latitude;
        double lng = (b.longitude - a.longitude) * fraction + a.longitude;
        return new LatLng(lat, lng);
    }*/

    public void moveCamera()
    {
        if(cameratype==2)
        {
            if(positionNow[0] != null)
            {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(positionNow[0]));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(20));
            }
        }
        else if(cameratype==1)
        {
            if(mylocation != null)
            {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(mylocation));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(20));
            }
        }
        else if(cameratype==3)
        {
            if(mylocation != null)
            {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(mylocation));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if(mMyLocationMarker!=null)
        {
            mMyLocationMarker.remove();
        }

        Double lat = location.getLatitude();
        Double lon = location.getLongitude();
        mylocation = new LatLng(lat, lon);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.rotation(0);
        markerOptions.position(mylocation);
        markerOptions.title("My Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        mMyLocationMarker = mMap.addMarker(markerOptions);

        moveCamera();
        if(LibInspira.getShared(global.temppreferences, global.temp.selected_count, "").equals("1")) drawDirection();
    }

    private void drawDirection()
    {
        if(mylocation != null && positionNow[0] != null)
        {
            if(mDirection!=null) mDirection.remove();

            GoogleDirection.withServerKey(getResources().getString(R.string.google_server_key))
                    .from(mylocation)
                    .to(positionNow[0])
                    .transportMode(TransportMode.DRIVING)
                    .alternativeRoute(true)
                    .avoid(AvoidType.INDOOR)
                    .unit(Unit.METRIC)
                    .execute(new DirectionCallback() {
                        @Override
                        public void onDirectionSuccess(Direction direction, String rawBody) {
                            if (direction.isOK()) {
                                Route route = direction.getRouteList().get(0);
                                Leg leg = route.getLegList().get(0);

                                ArrayList<LatLng> directionPositionList = leg.getDirectionPoint();
                                PolylineOptions polylineOptions = DirectionConverter.createPolyline(getActivity(), directionPositionList, 5, Color.MAGENTA);
                                mDirection = mMap.addPolyline(polylineOptions);
                            } else {
                                // Do something
                            }
                        }

                        @Override
                        public void onDirectionFailure(Throwable t) {
                            // Do something
                            if(mDirection!=null) mDirection.remove();
                        }
                    });
        }
        else
        {
            if(mDirection!=null) mDirection.remove();
        }
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(LibInspira.getShared(global.temppreferences, global.temp.selected_count, "").equals("1"))
        {
            getView().findViewById(R.id.tl_footer).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.btn_followme).setOnClickListener(this);
            getView().findViewById(R.id.btn_followtarget).setOnClickListener(this);
        }
        else
        {
            getView().findViewById(R.id.tl_footer).setVisibility(View.GONE);
        }

        FragmentManager fm = getChildFragmentManager();
        SupportMapFragment fragment = (SupportMapFragment) fm.findFragmentById(R.id.map_container);
        if (fragment == null) {
            fragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map_container, fragment).commit();
            fragment.getMapAsync(this);
        }

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
            }
        }
        else {
            buildGoogleApiClient();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setSmallestDisplacement(1);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onCameraMoveStarted(int i) {
        cameratype = 0;
    }

    //remarked by Tonny @03-Apr-2018 tidak terpakai

    /*public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }*/
}