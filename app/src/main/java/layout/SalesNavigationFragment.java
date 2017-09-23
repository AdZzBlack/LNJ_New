package layout;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.SupportMapFragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.inspira.gms.GlobalVar;
import com.inspira.gms.LibInspira;
import com.inspira.gms.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.inspira.gms.IndexInternal.global;

public class SalesNavigationFragment extends Fragment implements OnMapReadyCallback, android.location.LocationListener {
    private Spinner spinner;
    private static List<String> listUserNumber;
    private Calendar calendar;
    private String actionUrl;
    private EditText txtEndDate;
    private EditText txtStartDate;
    private List<Double> latitude;
    private List<Double> longitude;
    private List<String> dateLocation;
    private GoogleMap googleMap;
    protected String formatDate;
    protected SimpleDateFormat sdf;
    private LiveLocation liveLocation;
    private Geocoder geocoder;
    private List<String> addresses;
    private List<String> datetime;
    private ArrayAdapter<String> adapter;

    public SalesNavigationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listUserNumber = new ArrayList<>();
        actionUrl = "Sales/getTrackingData/";
        new getDataTracking(true, -1, null, null).execute(actionUrl);
        formatDate = "dd MMMM yyyy";
        sdf = new SimpleDateFormat(formatDate, Locale.US);
        geocoder = new Geocoder(getContext(), Locale.getDefault());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sales_navigation, container, false);
        getActivity().setTitle("Tracker");
        return v;
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        if (spinner != null)
            if (spinner.getChildCount() == 1)
                new getDataTracking(true, -1, null, null).execute(actionUrl);
        FragmentManager fm = getChildFragmentManager();
        SupportMapFragment fragment = (SupportMapFragment) fm.findFragmentById(R.id.Mapfragment);
        if (fragment == null) {
            fragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.Mapfragment, fragment).commit();
            fragment.getMapAsync(this);
        }
        spinner = (Spinner) getView().findViewById(R.id.edtSalesman);
        ((Button) getView().findViewById(R.id.btnSearch)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupQuery();
            }
        });
        ((Button) getView().findViewById(R.id.btnLive)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                liveLocation = new LiveLocation(Long.valueOf(LibInspira.getShared(global.settingpreferences, global.settings.interval, "")));
            }
        });
        ((FloatingActionButton) getView().findViewById(R.id.showSearchFilter)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((RelativeLayout) getView().findViewById(R.id.formLayout)).setVisibility(View.VISIBLE);
                ((RelativeLayout) getView().findViewById(R.id.Mapfragment)).setVisibility(View.GONE);
                FragmentManager fm = getChildFragmentManager();
                SupportMapFragment fragment = (SupportMapFragment) fm.findFragmentById(R.id.Mapfragment);
                if (fragment == null) {
                    fragment = SupportMapFragment.newInstance();
                    fm.beginTransaction().replace(R.id.Mapfragment, fragment).commit();
                    fragment.getMapAsync(SalesNavigationFragment.this);
                }
            }
        });
        ((FloatingActionButton) getView().findViewById(R.id.showLocationList)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(GlobalVar.listeffect);
                AddressesFragment addressesFragment = new AddressesFragment();
                addressesFragment.setDataFragment(addresses.toArray(new String[0]), datetime.toArray(new String[0]));
                LibInspira.ReplaceFragment(getActivity().getSupportFragmentManager(), R.id.fragment_container, addressesFragment);
            }
        });
        calendarProcessor();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /*********************  ********************************************************/
    //Method di bawah ini merupakan hasil dari implements MapReadyCallBack
    /*****************************************************************************/
    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        addresses = new ArrayList<>();
        datetime = new ArrayList<>();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        googleMap.clear();
        if (dateLocation != null) {
            for (int i = 0; i < dateLocation.size(); i++) {
                builder.include(new LatLng(latitude.get(i), longitude.get(i)));
                String[] dateTime = dateLocation.get(i).split(" ");
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    dateTime[0] = sdf.format(simpleDateFormat.parse(dateTime[0]));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                googleMap.addMarker(new MarkerOptions().position(
                        new LatLng(latitude.get(i), longitude.get(i))
                ).title(
                        spinner.getSelectedItem().toString()
                ).snippet(
                        "Tracking\nTanggal: " + dateTime[0] + "\nJam: " + dateTime[1]
                ));
                try {
                    List<android.location.Address> address = geocoder.getFromLocation(latitude.get(i), longitude.get(i), 1);
                    addresses.add(address.get(0).getAddressLine(0));
                    datetime.add(dateTime[0] + " " + dateTime[1]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker marker) {
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
            }
            LatLngBounds bounds = builder.build();
            int padding = 50;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            googleMap.animateCamera(cameraUpdate);
            googleMap.moveCamera(cameraUpdate);
            Log.i("SalesNavigationFragment", "map updated");
        }
    }

    public void setupQuery() {
        ((RelativeLayout) getView().findViewById(R.id.formLayout)).setVisibility(View.GONE);
        ((RelativeLayout) getView().findViewById(R.id.Mapfragment)).setVisibility(View.VISIBLE);
        new getDataTracking(false, spinner.getSelectedItemPosition(), txtStartDate.getText().toString(), txtEndDate.getText().toString()).execute(actionUrl);
    }

    private class LiveLocation {
        Thread thread;

        LiveLocation(final Long delay) {
            ((RelativeLayout) getView().findViewById(R.id.formLayout)).setVisibility(View.GONE);
            ((RelativeLayout) getView().findViewById(R.id.Mapfragment)).setVisibility(View.VISIBLE);
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            new getDataTracking(false, spinner.getSelectedItemPosition(), null, null).execute(actionUrl);
                            Thread.sleep(delay);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            thread.start();
        }
    }

    private class getDataTracking extends AsyncTask<String, Void, String> {
        private JSONObject jsonObject;
        private boolean isUsers;
        private int indexOfUser;
        private String startDate, endDate;

        getDataTracking(boolean isUsersData, int indexOfUser, String startDate, String endDate) {
            isUsers = isUsersData;
            this.indexOfUser = indexOfUser;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                jsonObject = new JSONObject();
                jsonObject.put("isUsers", isUsers);
                if (indexOfUser != -1)
                    jsonObject.put("nomortuser", listUserNumber.get(indexOfUser));
                if (!isUsers) {
                    jsonObject.put("startFilter", startDate);
                    jsonObject.put("endFilter", endDate);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return LibInspira.executePost(getContext(), urls[0], jsonObject);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("SalesNavigationFragment", s);
            try {
                JSONArray jsonarray = new JSONArray(s);
                if(jsonarray.length() > 0) {
                    if (isUsers) {
                        List<String> list = new ArrayList<>();
                        for (int i = 0; i < jsonarray.length(); i++) {
                            JSONObject obj = jsonarray.getJSONObject(i);
                            if (!obj.getString("nomoruser").equals(LibInspira.getShared(global.userpreferences, global.user.nomor, ""))) {
                                listUserNumber.add(obj.getString("nomoruser"));
                                list.add(obj.getString("userid"));
                            }
                        }
                        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, list);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(adapter);
                    } else {
                        latitude = new ArrayList<>();
                        longitude = new ArrayList<>();
                        dateLocation = new ArrayList<>();
                        for (int i = 0; i < jsonarray.length(); i++) {
                            JSONObject obj = jsonarray.getJSONObject(i);
                            latitude.add(Double.valueOf(obj.getString("latitude")));
                            longitude.add(Double.valueOf(obj.getString("longitude")));
                            dateLocation.add(obj.getString("trackingDate"));
                        }
                        onMapReady(googleMap);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void calendarProcessor() {
        final DatePickerDialog.OnDateSetListener dateStart;
        final DatePickerDialog.OnDateSetListener dateEnd;
        calendar = Calendar.getInstance();
        txtStartDate = (EditText) getView().findViewById(R.id.startDateInput);
        txtEndDate = (EditText) getView().findViewById(R.id.endDateInput);

        txtStartDate.setText(sdf.format(calendar.getTime()));
        txtEndDate.setText(sdf.format(calendar.getTime()));

        dateStart = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                txtStartDate.setText(sdf.format(calendar.getTime()));
            }
        };

        dateEnd = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                txtEndDate.setText(sdf.format(calendar.getTime()));
            }
        };

        txtStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getContext(), R.style.DialogTheme, dateStart, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        txtEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getContext(), R.style.DialogTheme, dateEnd, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }


}
