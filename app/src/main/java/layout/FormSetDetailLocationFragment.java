/******************************************************************************
    Author           : ADI
    Description      : dashboard untuk internal
    History          :

******************************************************************************/
package layout;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.inspira.lnj.GlobalVar;
import com.inspira.lnj.LibInspira;
import com.inspira.lnj.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FormSetDetailLocationFragment extends Fragment implements View.OnClickListener{

    private GlobalVar global;
    private JSONObject jsonObject;
    private Events events;

    private String[] arraySpinner;
    private Spinner spEvent;
    private EditText etPlace, etLatitude, etLongitude, etRadius, etDuration, etNotes;

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
        spEvent = (Spinner) getView().findViewById(R.id.spEvent);

        String actionUrl = "Track/getEvent/";
        events = new Events();
        events.execute(actionUrl);

        etPlace = (EditText) getView().findViewById(R.id.etPlace);
        if(!LibInspira.getShared(global.mapspreferences, global.maps.placename, "").equals("")){
            etPlace.setText(LibInspira.getShared(global.mapspreferences, global.maps.placename, ""));
        }
        etLatitude = (EditText) getView().findViewById(R.id.etLatitude);
        etLongitude = (EditText) getView().findViewById(R.id.etLongitude);
        if(!LibInspira.getShared(global.mapspreferences, global.maps.latitude, "").equals("")){
            etLatitude.setText(LibInspira.getShared(global.mapspreferences, global.maps.latitude, ""));
        }
        if(!LibInspira.getShared(global.mapspreferences, global.maps.longitude, "").equals("")){
            etLongitude.setText(LibInspira.getShared(global.mapspreferences, global.maps.longitude, ""));
        }
        etRadius = (EditText) getView().findViewById(R.id.etRadius);
        etDuration = (EditText) getView().findViewById(R.id.etDuration);
        etNotes = (EditText) getView().findViewById(R.id.etNotes);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
                    arraySpinner = new String[jsonarray.length()];
                    String tempData = "";
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        LibInspira.hideLoading();
                        if(!obj.has("query")){  //jika success
                            tempData = obj.getString("nomor") + "~" + obj.getString("kode") + "~" + obj.getString("nama") + "|" ;
                            arraySpinner[i] = obj.getString("nama");
                        }
                        else
                        {
                            LibInspira.showLongToast(getContext(), "Retrieving data failed");
                            LibInspira.BackFragment(getFragmentManager());
                            LibInspira.hideLoading();
                        }
                    }
                    if(!LibInspira.getShared(global.mapspreferences, global.maps.event, "").equals(tempData)){
                        LibInspira.setShared(global.mapspreferences, global.maps.event, tempData);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.support_simple_spinner_dropdown_item, arraySpinner);
                    spEvent.setAdapter(adapter);
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
}
