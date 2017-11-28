/******************************************************************************
    Author           : Tonny
    Description      : untuk menyimpan data detail checkpoint
    History          :

******************************************************************************/
package layout;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.inspira.lnj.GlobalVar;
import com.inspira.lnj.LibInspira;
import com.inspira.lnj.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FormSetDetailLocationFragment extends Fragment implements View.OnClickListener{

    private GlobalVar global;
    private JSONObject jsonObject;
    private Events events;

    private EditText etPlace, etLatitude, etLongitude, etRadius, etDuration, etNotes;
    private Button btnSave, btnDelete;
    
    private SaveWaypoint saveWaypoint;
    private DeleteWaypoint deleteWaypoint;  //added by Tonny @23-Nov-2017
    private CheckIsChecked checkIsChecked;  //added by Tonny @28-Nov-2017

    private TableLayout tlEvent;  //added by Tonny @27-Nov-2017
    private CheckBox cbEvent;
    private ArrayList arrNomorCheckPoint;
    private ArrayList<CheckBox> arrCbEvent;

    public FormSetDetailLocationFragment() {
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
        View v = inflater.inflate(R.layout.fragment_form_set_detail_location, container, false);
        getActivity().setTitle("Set Detail Location");
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

        etPlace = (EditText) getView().findViewById(R.id.etPlace);
        if(!LibInspira.getShared(global.tempmapspreferences, global.tempMaps.placename, "").equals("")){
            etPlace.setText(LibInspira.getShared(global.tempmapspreferences, global.tempMaps.placename, ""));
        }

        etDuration = (EditText) getView().findViewById(R.id.etDuration);
        if(!LibInspira.getShared(global.tempmapspreferences, global.tempMaps.duration, "").equals("")){
            etDuration.setText(LibInspira.getShared(global.tempmapspreferences, global.tempMaps.duration, ""));
        }

        etLatitude = (EditText) getView().findViewById(R.id.etLatitude);
        etLongitude = (EditText) getView().findViewById(R.id.etLongitude);
        if(!LibInspira.getShared(global.tempmapspreferences, global.tempMaps.latitude, "").equals("")){
            etLatitude.setText(LibInspira.getShared(global.tempmapspreferences, global.tempMaps.latitude, ""));
        }
        if(!LibInspira.getShared(global.tempmapspreferences, global.tempMaps.longitude, "").equals("")){
            etLongitude.setText(LibInspira.getShared(global.tempmapspreferences, global.tempMaps.longitude, ""));
        }
        etRadius = (EditText) getView().findViewById(R.id.etRadius);
        etRadius.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                LibInspira.formatNumberEditText(etRadius, this, true, false);
                //LibInspira.setShared(global.tempmapspreferences, global.tempMaps.radius, etRadius.getText().toString().replace(",", ""));
            }
        });

        etDuration = (EditText) getView().findViewById(R.id.etDuration);
        etDuration.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                LibInspira.formatNumberEditText(etDuration, this, true, false);
//                LibInspira.setShared(global.tempmapspreferences, global.tempMaps.duration, etDuration.getText().toString().replace(",", ""));
            }
        });
        etNotes = (EditText) getView().findViewById(R.id.etNotes);
        if(!LibInspira.getShared(global.tempmapspreferences, global.tempMaps.notes, "").equals("")){
            etNotes.setText(LibInspira.getShared(global.tempmapspreferences, global.tempMaps.notes, ""));
        }

        btnDelete = (Button) getView().findViewById(R.id.btnDelete);
        if(LibInspira.getShared(global.tempmapspreferences, global.tempMaps.mode, "").equals("update")) {
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(this);
            //get data from tempmapsshared
        }else{
            btnDelete.setVisibility(View.GONE);
            btnDelete.setOnClickListener(null);
        }

        btnSave = (Button) getView().findViewById(R.id.btnSave);
        btnSave.setOnClickListener(this);

        tlEvent = (TableLayout) getView().findViewById(R.id.tlEvent);
        arrNomorCheckPoint = new ArrayList();
        String actionUrl = "Track/getEvent/";
        events = new Events();
        events.execute(actionUrl);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id==R.id.btnSave)
        {
            if(!arrNomorCheckPoint.isEmpty()){
                LibInspira.alertBoxYesNo("Save Waypoint", "Do you want to save this waypoint data?", getActivity(), new Runnable() {
                    @Override
                    public void run() {
                        // TODO: Saving selected events into a var
                        String strEvent = "";
                        for (Object value: arrNomorCheckPoint){
                            strEvent = strEvent + value + "~";
                        }
                        // TODO: Save the data to preferences and database, and go back to previous fragment
                        LibInspira.setShared(global.tempmapspreferences, global.tempMaps.event, strEvent);
                        LibInspira.setShared(global.tempmapspreferences, global.tempMaps.placename, etPlace.getText().toString());
                        LibInspira.setShared(global.tempmapspreferences, global.tempMaps.latitude, etLatitude.getText().toString());
                        LibInspira.setShared(global.tempmapspreferences, global.tempMaps.longitude, etLongitude.getText().toString());
                        LibInspira.setShared(global.tempmapspreferences, global.tempMaps.radius, etRadius.getText().toString().replace(",", ""));
                        LibInspira.setShared(global.tempmapspreferences, global.tempMaps.duration, etDuration.getText().toString().replace(",", ""));
                        LibInspira.setShared(global.tempmapspreferences, global.tempMaps.notes, etNotes.getText().toString());

                        Log.wtf("placename ", etPlace.getText().toString());
                        Log.wtf("latitude ", etLatitude.getText().toString());
                        Log.wtf("longitude ", etLongitude.getText().toString());
                        Log.wtf("radius ", etRadius.getText().toString());

                        String actionUrl = "Track/insertWaypoint/";

                        //added by Tonny @23-Nov-2017 pengecekan jika dalam mode edit/update
                        if(LibInspira.getShared(global.tempmapspreferences, global.tempMaps.mode, "").equals("update")){
                            actionUrl = "Track/updateWaypoint/";
                        }
                        saveWaypoint = new SaveWaypoint();
                        saveWaypoint.execute(actionUrl);
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        //do nothing
                    }
                });
            }else{
                LibInspira.showLongToast(getContext(), "You must select at least one event to proceed");
            }
        }else if(id==R.id.btnDelete){  //added by Tonny @23-Nov-2017
            LibInspira.alertBoxYesNo("Delete Waypoint", "Do you want to delete this waypoint?", getActivity(), new Runnable() {
                @Override
                public void run() {
                    String actionUrl = "Track/deleteWaypoint/";
                    deleteWaypoint = new DeleteWaypoint();
                    deleteWaypoint.execute(actionUrl);
                }
            }, new Runnable() {
                @Override
                public void run() {
                    //do nothing
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(events != null){
            events.cancel(true);
        }
        if(saveWaypoint != null){
            saveWaypoint.cancel(true);
        }
        if(checkIsChecked != null){
            checkIsChecked.cancel(true);
        }
    }

    private class Events extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            jsonObject = new JSONObject();
            return LibInspira.executePost(getContext(), urls[0], jsonObject);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("events", result);
            try {
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    String tempData = "";
                    arrCbEvent = new ArrayList();
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        LibInspira.hideLoading();
                        if(!obj.has("query") && tlEvent != null){  //jika success
                            tempData = obj.getString("nomor") + "~" + obj.getString("kode") + "~" + obj.getString("nama") + "|" ;
                            TableRow tr = new TableRow(getContext());
                            cbEvent = new CheckBox(getContext());
                            cbEvent.setId(Integer.parseInt(obj.getString("nomor")));
                            cbEvent.setText(obj.getString("nama"));
                            cbEvent.setChecked(false);
                            arrCbEvent.add(cbEvent);
                            tr.addView(cbEvent);
                            tlEvent.addView(tr);
                        }
                        else
                        {
                            LibInspira.showLongToast(getContext(), "Retrieving data failed");
                            LibInspira.BackFragment(getFragmentManager());
                            LibInspira.hideLoading();
                        }
                    }

                    if(!LibInspira.getShared(global.tempmapspreferences, global.tempMaps.event, "").equals(tempData)){
                        LibInspira.setShared(global.tempmapspreferences, global.tempMaps.event, tempData);
                    }

                    for (Object value: arrCbEvent){
                        final CheckBox cb = (CheckBox) value;
                        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                        {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if(arrNomorCheckPoint == null){
                                    arrNomorCheckPoint = new ArrayList();
                                }
                                if (isChecked){
                                    arrNomorCheckPoint.add(String.valueOf(cb.getId()));
                                }else{
                                    arrNomorCheckPoint.remove(String.valueOf(cb.getId()));
                                }
                                if(!arrNomorCheckPoint.isEmpty()){
                                    for (Object value: arrNomorCheckPoint){
                                        Log.wtf("value ", value.toString());
                                    }
                                }
                            }
                        });
                    }

                    if(LibInspira.getShared(global.tempmapspreferences, global.tempMaps.mode, "").equals("update")){
                        String actionUrl = "Track/getEventDetail/";
                        checkIsChecked = new CheckIsChecked();
                        checkIsChecked.execute(actionUrl);
                    }
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
            LibInspira.showLoading(getContext(), "Retrieving data", "Loading");
        }
    }

    private class SaveWaypoint extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                jsonObject = new JSONObject();
                Log.wtf("arrnomorcheckpoint", LibInspira.getShared(global.tempmapspreferences,global.tempMaps.event,""));
                jsonObject.put("arrnomorcheckpoint", LibInspira.getShared(global.tempmapspreferences,global.tempMaps.event,""));  //added by Tonny @28-Nov-2017  nomor checkpoint = event
                jsonObject.put("nomor", LibInspira.getShared(global.tempmapspreferences,global.tempMaps.nomor,""));
                jsonObject.put("nama", LibInspira.getShared(global.tempmapspreferences,global.tempMaps.placename,""));
                jsonObject.put("duration", LibInspira.getShared(global.tempmapspreferences,global.tempMaps.duration,""));
                jsonObject.put("radius", LibInspira.getShared(global.tempmapspreferences,global.tempMaps.radius,""));
                jsonObject.put("latitude", LibInspira.getShared(global.tempmapspreferences,global.tempMaps.latitude,""));
                jsonObject.put("longitude", LibInspira.getShared(global.tempmapspreferences,global.tempMaps.longitude,""));
                jsonObject.put("keterangan", LibInspira.getShared(global.tempmapspreferences,global.tempMaps.notes,""));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return LibInspira.executePost(getContext(), urls[0], jsonObject);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("tes", result);
            Log.wtf("result ", result);
            try {
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        LibInspira.hideLoading();
                        if(!obj.has("query")){  //jika success
                            LibInspira.showLongToast(getContext(), "Saving Waypoint Success");
                            LibInspira.clearShared(global.tempmapspreferences); // delete temppreferences
                        }
                        else
                        {
                            LibInspira.showLongToast(getContext(), "Saving Waypoint Failed");
                        }
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
                LibInspira.showLongToast(getContext(), e.getMessage());
                LibInspira.hideLoading();
            }
            LibInspira.hideLoading();
            LibInspira.BackFragment(getFragmentManager());
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LibInspira.showLoading(getContext(), "Saving Waypoint", "Loading");
        }
    }

    private class DeleteWaypoint extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                jsonObject = new JSONObject();
                jsonObject.put("nomor", LibInspira.getShared(global.tempmapspreferences,global.tempMaps.nomor,""));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return LibInspira.executePost(getContext(), urls[0], jsonObject);
        }
        // onPostExecute displays the results of the AsyncTask.
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
                            LibInspira.clearShared(global.tempmapspreferences); // delete temppreferences
                        }
                        else
                        {
                            LibInspira.showLongToast(getContext(), "Deleting Waypoint Failed");
                        }
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
                LibInspira.showLongToast(getContext(), e.getMessage());
                LibInspira.hideLoading();
            }
            LibInspira.hideLoading();
            LibInspira.BackFragment(getFragmentManager());
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LibInspira.showLoading(getContext(), "Deleting Waypoint", "Loading");
        }
    }

    //untuk mendapatkan event apa saja yg terpilih (untuk tampilan checkbox yg tercentang pada saat mode update)
    private class CheckIsChecked extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                jsonObject = new JSONObject();
                jsonObject.put("nomormhwaypoint", LibInspira.getShared(global.tempmapspreferences,global.tempMaps.nomor,""));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return LibInspira.executePost(getContext(), urls[0], jsonObject);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.wtf("result ", result);
            try {
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        LibInspira.hideLoading();
                        if(!obj.has("query")){  //jika success
                            for (Object value: arrCbEvent){
                                final CheckBox cb = (CheckBox) value;
                                if (cb.getId() == Integer.parseInt(obj.getString("nomormhcheckpoint"))){
                                    cb.setChecked(true);
                                }
                            }
                        }
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
                LibInspira.showLongToast(getContext(), e.getMessage());
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }
}
