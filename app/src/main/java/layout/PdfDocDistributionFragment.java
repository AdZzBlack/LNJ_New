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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.inspira.lnj.GlobalVar;
import com.inspira.lnj.LibInspira;
import com.inspira.lnj.LibPDF;
import com.inspira.lnj.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PdfDocDistributionFragment extends PdfParentFragment implements View.OnClickListener{
    private EditText etSuratJalan, etFrom, etTo, etStartDate, etEndDate;
    private Spinner spAction;
    private ImageButton btnClearSuratJalan, btnClearFrom, btnClearTo, btnClearAction, btnClearStartDate, btnClearEndDate;

    private String flag;
    private Calendar calDate;
    private DatePickerDialog.OnDateSetListener date;
    private SimpleDateFormat sdf;
    public PdfDocDistributionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setLayout(R.layout.fragment_form_pdf_doc_distribution);
        // Inflate the layout for this fragment
        View v = inflater.inflate(getLayout(), container, false);
        getActivity().setTitle("Deviation Report Filter");
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        etSuratJalan.setText(LibInspira.getShared(global.temppreferences, global.temp.report_job, ""));
        etFrom.setText(LibInspira.getShared(global.temppreferences, global.temp.report_user_from_name, ""));
        etTo.setText(LibInspira.getShared(global.temppreferences, global.temp.report_user_to_name, ""));
        spAction.setSelection(Integer.parseInt(LibInspira.getShared(global.temppreferences, global.temp.report_doc_action_index, "0")));
        etStartDate.setText(LibInspira.getShared(global.temppreferences, global.temp.report_startdate, ""));
        etEndDate.setText(LibInspira.getShared(global.temppreferences, global.temp.report_enddate, ""));
    }

    @Override
    public void onActivityCreated(Bundle bundle){
        super.onActivityCreated(bundle);
        global = new GlobalVar(getActivity());
        getView().findViewById(R.id.btnGetReport).setOnClickListener(this);

        etSuratJalan = (EditText) getView().findViewById(R.id.etSuratJalan);
        etFrom = (EditText) getView().findViewById(R.id.etFrom);
        etTo = (EditText) getView().findViewById(R.id.etTo);
        spAction = (Spinner) getView().findViewById(R.id.spAction);
        etStartDate = (EditText) getView().findViewById(R.id.etStartDate);
        etEndDate = (EditText) getView().findViewById(R.id.etEndDate);
        btnClearSuratJalan = (ImageButton) getView().findViewById(R.id.btnClearSuratjalan);
        btnClearFrom = (ImageButton) getView().findViewById(R.id.btnClearFrom);
        btnClearTo = (ImageButton) getView().findViewById(R.id.btnClearTo);
        btnClearAction = (ImageButton) getView().findViewById(R.id.btnClearAction);
        btnClearStartDate = (ImageButton) getView().findViewById(R.id.btnClearStartDate);
        btnClearEndDate = (ImageButton) getView().findViewById(R.id.btnClearEndDate);

        etSuratJalan.setOnClickListener(this);
        etFrom.setOnClickListener(this);
        etTo.setOnClickListener(this);
//        spAction.setOnClickListener(this);
        etStartDate.setOnClickListener(this);
        etEndDate.setOnClickListener(this);
        btnClearSuratJalan.setOnClickListener(this);
        btnClearFrom.setOnClickListener(this);
        btnClearTo.setOnClickListener(this);
        btnClearAction.setOnClickListener(this);
        btnClearStartDate.setOnClickListener(this);
        btnClearEndDate.setOnClickListener(this);

        ArrayAdapter<String> adapter;
        List<String> list;

        list = new ArrayList<>();
        list.add("ALL");
        list.add("SUBMIT");
        list.add("ACCEPT");
        list.add("REJECT");
        adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAction.setAdapter(adapter);

        spAction.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LibInspira.setShared(global.temppreferences, global.temp.report_doc_action_index, String.valueOf(position));
                LibInspira.setShared(global.temppreferences, global.temp.report_doc_action, spAction.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                LibInspira.setShared(global.temppreferences, global.temp.report_doc_action_index, "0");
                LibInspira.setShared(global.temppreferences, global.temp.report_doc_action, spAction.getSelectedItem().toString());
            }
        });

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
            setActionUrl("Report/GetReportDocDistribution/");
            new PdfParentFragment.getData().execute( getActionUrl() );
        }
        else if(id == R.id.etFrom){
            LibInspira.setShared(global.sharedpreferences, global.shared.position, "report doc_from");
            LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new ChooseUserFragment());
        }
        else if(id == R.id.etTo){
            LibInspira.setShared(global.sharedpreferences, global.shared.position, "report doc_to");
            LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new ChooseUserFragment());
        }
        else if(id == R.id.spAction){
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
        else if(id == R.id.btnClearFrom){
            etFrom.setText("");
            LibInspira.setShared(global.temppreferences, global.temp.report_user_from, "");
            LibInspira.setShared(global.temppreferences, global.temp.report_user_from_name, "");
        }
        else if(id == R.id.btnClearTo){
            etTo.setText("");
            LibInspira.setShared(global.temppreferences, global.temp.report_user_to, "");
            LibInspira.setShared(global.temppreferences, global.temp.report_user_to_name, "");
        }
        else if(id == R.id.btnClearTo){
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
            jsonObject.put("nomormhadmin_from", LibInspira.getShared(global.temppreferences, global.temp.report_user_from, ""));
            jsonObject.put("nomormhadmin_to", LibInspira.getShared(global.temppreferences, global.temp.report_user_to, ""));
            if(LibInspira.getShared(global.temppreferences, global.temp.report_doc_action, "").equals("ALL")){
                jsonObject.put("action", "");
            }else{
                jsonObject.put("action", LibInspira.getShared(global.temppreferences, global.temp.report_doc_action, ""));
            }
            jsonObject.put("startdate", LibInspira.getShared(global.temppreferences, global.temp.report_startdate, ""));
            jsonObject.put("enddate", LibInspira.getShared(global.temppreferences, global.temp.report_enddate, ""));
            jsonObject.put("nomorcabang", LibInspira.getShared(global.userpreferences, global.user.cabang, ""));
            jsonObject.put("nomormhadmin", LibInspira.getShared(global.userpreferences, global.user.nomor, ""));
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

                    new LibPDF(getActivity()).createPDF_docdistribution(result, format.format(cal.getTime()));
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