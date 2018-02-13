/******************************************************************************
    Author           : Tonny
    Description      : PDF converter parent for report
    History          :

******************************************************************************/
package layout;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inspira.lnj.GlobalVar;
import com.inspira.lnj.LibInspira;
import com.inspira.lnj.LibPDF;
import com.inspira.lnj.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PdfParentFragment extends Fragment implements View.OnClickListener{
    protected GlobalVar global;
    protected JSONObject jsonObject;

    private String actionUrl;
    private int layout;

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public int getLayout() {
        return layout;
    }

    public void setLayout(int layout) {
        this.layout = layout;
    }

    public PdfParentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
//        global = new GlobalVar(getActivity());
//        getView().findViewById(R.id.btnGetReport).setOnClickListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if(id==R.id.btnGetReport)
        {
            new getData().execute( getActionUrl() );
        }
    }

    protected void onPreExecuteGetData(){
        LibInspira.showLoading(getContext(), "Loading", "Get Report");
    }

    protected void doInBackgroundGetData(){
        jsonObject = new JSONObject();
        try {
            jsonObject.put("job_nomor", LibInspira.getShared(global.temppreferences, global.temp.report_job, ""));
            jsonObject.put("driver", LibInspira.getShared(global.temppreferences, global.temp.report_user, ""));
            jsonObject.put("startdate", LibInspira.getShared(global.temppreferences, global.temp.report_startdate, ""));
            jsonObject.put("enddate", LibInspira.getShared(global.temppreferences, global.temp.report_enddate, ""));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void onPostExecuteGetData(String result){
        Log.d("resultQuery", result);
        Boolean error = false;
        try
        {
            JSONArray jsonarray = new JSONArray(result);
            if(jsonarray.length() > 0){
                for (int i = 0; i < jsonarray.length(); i++) {
                    JSONObject obj = jsonarray.getJSONObject(i);
                    if(obj.has("error")){
                        error = true;
                    }
                }
                if(!error)
                {
                    Calendar cal = Calendar.getInstance();
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

                    new LibPDF(getActivity()).createPDF_livetracking(result, format.format(cal.getTime()));
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        LibInspira.hideLoading();
    }

    protected class getData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            doInBackgroundGetData();
            return LibInspira.executePost(getContext(), urls[0], jsonObject);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            onPostExecuteGetData(result);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            onPreExecuteGetData();
        }
    }
}
