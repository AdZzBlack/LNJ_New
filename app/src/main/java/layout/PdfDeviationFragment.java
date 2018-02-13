/******************************************************************************
    Author           : Tonny
    Description      : PDF converter for deviasi report
    History          :

******************************************************************************/
package layout;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inspira.lnj.GlobalVar;
import com.inspira.lnj.LibInspira;
import com.inspira.lnj.LibPDF;
import com.inspira.lnj.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PdfDeviationFragment extends PdfParentFragment implements View.OnClickListener{
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
    public void onActivityCreated(Bundle bundle){
        super.onActivityCreated(bundle);
        global = new GlobalVar(getActivity());
        getView().findViewById(R.id.btnGetReport).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if(id==R.id.btnGetReport)
        {
            setActionUrl("Report/GetReportDeviationTracking/");
            new getData().execute( getActionUrl() );
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