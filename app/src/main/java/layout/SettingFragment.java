package layout;
import android.Manifest;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
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

import com.inspira.lnj.LibInspira;
import com.inspira.lnj.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.inspira.lnj.IndexInternal.global;

public class SettingFragment extends Fragment implements View.OnClickListener, View.OnTouchListener{
    private final static int REQUEST_READ_PHONE_STATE = 1;
    EditText edtInterval, edtRadius;
    TextView tvStartTracking, tvEndTracking;
    Spinner spTracking;
    Button btnUpdate, btnGetNumber;
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
        btnGetNumber = (Button) getView().findViewById(R.id.btnGetNumber);
        getActivity().setTitle("Settings");

        edtRadius.setOnTouchListener(this);  //added by Tonny @07-Aug-2017
        edtInterval.setOnTouchListener(this);  //added by Tonny @07-Aug-2017
        tvStartTracking.setOnClickListener(this);
        tvEndTracking.setOnClickListener(this);
        btnUpdate.setOnClickListener(this);
        btnGetNumber.setOnClickListener(this);

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
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.btnGetNumber){
//            TelephonyManager tMgr = (TelephonyManager)getContext().getSystemService(getContext().TELEPHONY_SERVICE);
//            String mPhoneNumber = tMgr.getDeviceId();
//            LibInspira.ShowLongToast(getContext(), mPhoneNumber);
            int permissionCheck = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
            } else {
                TelephonyManager tMgr = (TelephonyManager)getContext().getSystemService(getContext().TELEPHONY_SERVICE);
                String mPhoneNumber = tMgr.getDeviceId();
                LibInspira.ShowLongToast(getContext(), mPhoneNumber);
            }
        }else if(id == R.id.btnUpdate) {
            String actionUrl = "Settings/setSettings/";
            new updateSettings().execute(actionUrl);
            LibInspira.ShowShortToast(getContext(), "Saving...");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    if (grantResults.length > 0){
                        boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                        if(cameraAccepted){
                            LibInspira.ShowLongToast(getContext(), "Permission granted");
                        }else{
                            LibInspira.ShowLongToast(getContext(), "Permission Denied");
                            if(shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE)){
                                displayAlertMessage("You need to allow access to both permission",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
                                            }
                                        });
                                return;
                            }
                        }
                    }
                    break;
                }
                break;

            default:
                break;
        }
    }

    public void displayAlertMessage(String message, DialogInterface.OnClickListener listener){
        new AlertDialog.Builder(getContext())
                .setMessage(message)
                .setPositiveButton("OK", listener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
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
