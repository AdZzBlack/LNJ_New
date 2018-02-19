/******************************************************************************
    Author           : Tonny
    Description      : PDF converter for deviation report
    History          :

******************************************************************************/
package layout;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;

import com.inspira.lnj.GlobalVar;
import com.inspira.lnj.LibInspira;
import com.inspira.lnj.LibPDF;
import com.inspira.lnj.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class PdfDeviationFragment extends PdfParentFragment implements View.OnClickListener{
    private EditText etSuratJalan, etDriver, etStartDate, etEndDate;
    private ImageButton btnClearSuratJalan, btnClearDriver, btnClearStartDate, btnClearEndDate;

    private String flag;
    private Calendar calDate;
    private DatePickerDialog.OnDateSetListener date;
    private SimpleDateFormat sdf;
    public PdfDeviationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setLayout(R.layout.fragment_form_pdf_livetracking);
        // Inflate the layout for this fragment
        View v = inflater.inflate(getLayout(), container, false);
        getActivity().setTitle("Deviation Report Filter");
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        etSuratJalan.setText(LibInspira.getShared(global.temppreferences, global.temp.report_job, ""));
        etDriver.setText(LibInspira.getShared(global.temppreferences, global.temp.report_user_name, ""));
        etStartDate.setText(LibInspira.getShared(global.temppreferences, global.temp.report_startdate, ""));
        etEndDate.setText(LibInspira.getShared(global.temppreferences, global.temp.report_enddate, ""));
    }

    @Override
    public void onActivityCreated(Bundle bundle){
        super.onActivityCreated(bundle);
        global = new GlobalVar(getActivity());
        getView().findViewById(R.id.btnGetReport).setOnClickListener(this);

        etSuratJalan = (EditText) getView().findViewById(R.id.etSuratJalan);
        etDriver = (EditText) getView().findViewById(R.id.etDriver);
        etStartDate = (EditText) getView().findViewById(R.id.etStartDate);
        etEndDate = (EditText) getView().findViewById(R.id.etEndDate);
        btnClearSuratJalan = (ImageButton) getView().findViewById(R.id.btnClearSuratjalan);
        btnClearDriver = (ImageButton) getView().findViewById(R.id.btnClearDriver);
        btnClearStartDate = (ImageButton) getView().findViewById(R.id.btnClearStartDate);
        btnClearEndDate = (ImageButton) getView().findViewById(R.id.btnClearEndDate);

        etSuratJalan.setOnClickListener(this);
        etDriver.setOnClickListener(this);
        etStartDate.setOnClickListener(this);
        etEndDate.setOnClickListener(this);
        btnClearSuratJalan.setOnClickListener(this);
        btnClearDriver.setOnClickListener(this);
        btnClearStartDate.setOnClickListener(this);
        btnClearEndDate.setOnClickListener(this);

        calDate = Calendar.getInstance();
        sdf = new SimpleDateFormat(LibInspira.inspiraDateFormat, Locale.US);
        date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                calDate.set(Calendar.YEAR, year);
                calDate.set(Calendar.MONTH, monthOfYear);
                calDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                if(flag.equals("startdate")) {
                    etStartDate.setText(sdf.format(calDate.getTime()));
                    LibInspira.setShared(global.temppreferences, global.temp.report_startdate, sdf.format(calDate.getTime()));
                }
                else if(flag.equals("enddate")) {
                    etEndDate.setText(sdf.format(calDate.getTime()));
                    LibInspira.setShared(global.temppreferences, global.temp.report_enddate, sdf.format(calDate.getTime()));
                }
            }
        };
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if(id==R.id.btnGetReport)
        {
            setActionUrl("Report/GetReportDeviationTracking/");
            new PdfParentFragment.getData().execute( getActionUrl() );
        }
        else if(id == R.id.etDriver){
            LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new ChooseDriverFragment());
        }
        else if(id == R.id.etStartDate){
            flag = "startdate";
            new DatePickerDialog(getActivity(), date, calDate.get(Calendar.YEAR), calDate.get(Calendar.MONTH), calDate.get(Calendar.DAY_OF_MONTH)).show();
        }
        else if(id == R.id.etEndDate){
            flag = "enddate";
            new DatePickerDialog(getActivity(), date, calDate.get(Calendar.YEAR), calDate.get(Calendar.MONTH), calDate.get(Calendar.DAY_OF_MONTH)).show();
        }
        else if(id == R.id.btnClearDriver){
            etDriver.setText("");
            LibInspira.setShared(global.temppreferences, global.temp.report_user, "");
            LibInspira.setShared(global.temppreferences, global.temp.report_user_name, "");
        }
        else if(id == R.id.btnClearStartDate){
            etStartDate.setText("");
            LibInspira.setShared(global.temppreferences, global.temp.report_startdate, "");
        }
        else if(id == R.id.btnClearEndDate){
            etEndDate.setText("");
            LibInspira.setShared(global.temppreferences, global.temp.report_enddate, "");
        }
    }

    @Override
    protected void doInBackgroundGetData() {
        super.doInBackgroundGetData();
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

    @Override
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

                    new LibPDF(getActivity()).createPDF_deviation(result, format.format(cal.getTime()));
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        LibInspira.hideLoading();
    }
}