package layout;
import android.app.TimePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.inspira.gms.LibInspira;
import com.inspira.gms.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.inspira.gms.IndexInternal.global;

public class SettingFragment extends Fragment implements View.OnClickListener, View.OnTouchListener{
    EditText edtInterval, edtRadius;
    TextView tvStartTracking, tvEndTracking;
    Spinner spTracking;
    Button btnUpdate;
    private TimePickerDialog tp;
    private Integer timetype = 0;
    public SettingFragment() {
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
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        edtInterval = (EditText) getView().findViewById(R.id.edtInterval);
        edtRadius = (EditText) getView().findViewById(R.id.edtRadius);
        tvStartTracking = (TextView) getView().findViewById(R.id.tvStartTracking);
        tvEndTracking = (TextView) getView().findViewById(R.id.tvEndTracking);
        spTracking = (Spinner) getView().findViewById(R.id.spTracking);
        btnUpdate = (Button) getView().findViewById(R.id.btnUpdate);
        getActivity().setTitle("Settings");

        edtRadius.setOnTouchListener(this);  //added by Tonny @07-Aug-2017
        edtInterval.setOnTouchListener(this);  //added by Tonny @07-Aug-2017
        tvStartTracking.setOnClickListener(this);
        tvEndTracking.setOnClickListener(this);
        btnUpdate.setOnClickListener(this);

        Calendar newTime = Calendar.getInstance();
        tp= new TimePickerDialog(getActivity(), R.style.DialogTheme, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                try {
                    String time = selectedHour + ":" + selectedMinute;
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                    Date newtime = sdf.parse(time);
                    time = sdf.format(newtime);

                    if(timetype == 1) {
                        tvStartTracking.setText(time);
                    }else if(timetype == 2){
                        tvEndTracking.setText(time);
                    }
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        }, newTime.get(Calendar.HOUR_OF_DAY), newTime.get(Calendar.MINUTE), true);

        String actionUrl = "Settings/getSettings/";
        new checkSettings().execute( actionUrl );
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.tvStartTracking){
            timetype = 1;
            tp.show();
        }else if(id == R.id.tvEndTracking){
            timetype = 2;
            tp.show();
        }else if(id == R.id.btnUpdate) {
            String actionUrl = "Settings/setSettings/";
            new updateSettings().execute(actionUrl);
            LibInspira.ShowShortToast(getContext(), "Saving...");
        }
//        }else if(id == R.id.edtInterval){
//            edtInterval.setText(edtInterval.getText().toString().replace(",",""));
//        }else if(id == R.id.edtRadius){
//            edtRadius.setText(edtInterval.getText().toString().replace(",",""));
//        }
    }

    //added by Tonny @07-Aug-2017 menggunakan onTouch karena onClick tidak berjalan dengan baik untuk
    //menjalankan pengecekan pada edtInterval dan edtRadius
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int id = v.getId();
        if(id == R.id.edtInterval){
            edtInterval.setText(edtInterval.getText().toString().replace(",",""));
        }else if(id == R.id.edtRadius){
            edtRadius.setText(edtRadius.getText().toString().replace(",",""));
        }
        return false;
    }

    private class checkSettings extends AsyncTask<String, Void, String> {
        JSONObject jsonObject;

        @Override
        protected String doInBackground(String... urls) {
            jsonObject = new JSONObject();
            return LibInspira.executePost(getContext(), urls[0], jsonObject);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("settings", result);
            //LibInspira.ShowShortToast(getContext(), "Updating...");
            try {
                JSONArray jsonarray = new JSONArray(result);
                if (jsonarray.length() > 0) {
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if (!obj.has("query")) {
                            String success = obj.getString("success");
                            if (success.equals("true")) {
                                Log.d("status", "UPDATING");
                                LibInspira.setShared(global.settingpreferences, global.settings.interval, obj.getString("interval"));
                                LibInspira.setShared(global.settingpreferences, global.settings.radius, obj.getString("radius"));
                                LibInspira.setShared(global.settingpreferences, global.settings.tracking, obj.getString("tracking"));
                                LibInspira.setShared(global.settingpreferences, global.settings.jam_awal, obj.getString("jam_awal"));
                                LibInspira.setShared(global.settingpreferences, global.settings.jam_akhir, obj.getString("jam_akhir"));
                                int ms = Integer.parseInt(LibInspira.getShared(global.settingpreferences, global.settings.interval, "0"));
                                Integer minute = 0;
                                if (ms > 0){
                                    minute = ms/1000/60;  //added by Tonny @09-Aug-2017 dari ms diubah menjadi minute
                                }
                                //modified by Tonny @07-Aug-2017
                                //edtInterval.setText(LibInspira.delimeter(LibInspira.getShared(global.settingpreferences, global.settings.interval, "0"), true));
                                edtInterval.setText(LibInspira.delimeter(minute.toString()));
                                edtRadius.setText(LibInspira.delimeter(LibInspira.getShared(global.settingpreferences, global.settings.radius, "0"), true));
                                if(obj.getString("tracking").equals("GPS Only")){
                                    spTracking.setSelection(0);
                                }
                                else{
                                    spTracking.setSelection(1);
                                }
                                tvStartTracking.setText(LibInspira.getShared(global.settingpreferences, global.settings.jam_awal, "00:00"));
                                tvEndTracking.setText(LibInspira.getShared(global.settingpreferences, global.settings.jam_akhir, "00:00"));

                            }else{
                                Log.d("status", "FAILED");
                                LibInspira.ShowShortToast(getContext(), "Not success");
                            }
                        }else{
                            LibInspira.setShared(global.settingpreferences, global.settings.interval, "0");
                            LibInspira.setShared(global.settingpreferences, global.settings.radius, "0");
                            LibInspira.setShared(global.settingpreferences, global.settings.tracking, "GPS Only");
                            LibInspira.setShared(global.settingpreferences, global.settings.jam_awal, "00:00");
                            LibInspira.setShared(global.settingpreferences, global.settings.jam_akhir, "00:00");
                            LibInspira.ShowShortToast(getContext(), "Update Settings Failed");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class updateSettings extends AsyncTask<String, Void, String> {
        JSONObject jsonObject;
        //remarked by Tonny @07-Aug-2017
        //String interval = edtInterval.getText().toString();
        Integer ms = Integer.parseInt(edtInterval.getText().toString()) * 1000 * 60;  //modified by Tonny @09-Aug-2017 diubah dari minute menjadi millisecond (ms)
        String interval = ms.toString();
        String radius = edtRadius.getText().toString();
        String tracking = spTracking.getSelectedItem().toString();
        String jam_awal = tvStartTracking.getText().toString();
        String jam_akhir = tvEndTracking.getText().toString();

        @Override
        protected String doInBackground(String... urls) {
            try {
                jsonObject = new JSONObject();
                jsonObject.put("interval", interval);
                jsonObject.put("radius", radius);
                jsonObject.put("tracking", tracking);
                jsonObject.put("jam_awal", jam_awal);
                jsonObject.put("jam_akhir", jam_akhir);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return LibInspira.executePost(getContext(), urls[0], jsonObject);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONArray jsonarray = new JSONArray(result);
                for (int i = jsonarray.length() - 1; i >= 0; i--) {
                    JSONObject obj = jsonarray.getJSONObject(i);

                    String success = obj.getString("success");
                    if(success.equals("true")){
                        LibInspira.ShowShortToast(getContext(), "Setting Updated");
                        LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new DashboardInternalFragment());
                    }else{
                        Log.d("FAILED: ", success);
                        LibInspira.ShowShortToast(getContext(), "Update Settings Failed");
                    }
                }
                LibInspira.hideLoading();
            }catch(Exception e)
            {
                e.printStackTrace();
                LibInspira.ShowShortToast(getContext(), "Update Settings Failed");
                LibInspira.hideLoading();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LibInspira.showLoading(getContext(), "Settings", "Loading");
        }
    }

}
