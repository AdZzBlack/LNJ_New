/******************************************************************************
    Author           : ADI
    Description      : dashboard untuk internal
    History          :

******************************************************************************/
package layout;

import android.app.DatePickerDialog;
import android.content.Context;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
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

//import android.app.Fragment;

public class FilterSalesOmzetFragment extends Fragment implements View.OnClickListener{
    TextView tvSalesman, tvEndDate;
    RadioButton rbtnBulan, rbtnTahun;
    DatePickerDialog dp;
    Button btnSearch;
    ImageButton ibtnClearSales;
    private String nomorsales;
    public FilterSalesOmzetFragment() {
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
        View v = inflater.inflate(R.layout.fragment_filter_sales_omzet, container, false);
        getActivity().setTitle("Filter Sales Omzet");
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
        tvSalesman = (TextView) getView().findViewById(R.id.tvSalesman);
        tvSalesman.setOnClickListener(this);
        tvEndDate = (TextView) getView().findViewById(R.id.tvEndDate);
        tvEndDate.setOnClickListener(this);
        if (LibInspira.getShared(global.omzetpreferences, global.omzet.enddate, "").equals("")){
            tvEndDate.setText(LibInspira.getCurrentDate());
        }else{
            tvEndDate.setText(LibInspira.getShared(global.omzetpreferences, global.omzet.enddate, ""));
        }
        rbtnBulan = (RadioButton) getView().findViewById(R.id.rbtnBulan);
        rbtnTahun = (RadioButton) getView().findViewById(R.id.rbtnTahun);
        btnSearch = (Button) getView().findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(this);
        nomorsales = LibInspira.getShared(global.sharedpreferences, global.shared.nomorsales, "");
        tvSalesman.setText(LibInspira.getShared(global.sharedpreferences, global.shared.namasales, ""));
        ibtnClearSales = (ImageButton) getView().findViewById(R.id.ibtnClearSales);
        ibtnClearSales.setOnClickListener(this);
        // Define DatePicker
        Calendar newCalendar = Calendar.getInstance();
        dp = new DatePickerDialog(getActivity(), R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                try {
                    String date = year + "-" + (monthOfYear+1) + "-" + dayOfMonth;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date newdate = sdf.parse(date);
                    date = sdf.format(newdate);

                    tvEndDate.setText(date);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ibtnClearSales){
            LibInspira.setShared(global.sharedpreferences, global.shared.nomorsales, "");
            LibInspira.setShared(global.sharedpreferences, global.shared.namasales, "");
            LibInspira.setShared(global.omzetpreferences, global.omzet.nomorsales, "");
            nomorsales = "";
            tvSalesman.setText("");
        }else if(id == R.id.tvSalesman){
            LibInspira.setShared(global.sharedpreferences, global.shared.position, "filter omzet");
            LibInspira.ReplaceFragment(getActivity().getSupportFragmentManager(), R.id.fragment_container, new ChooseSalesmanFragment());
        }else if(id == R.id.tvEndDate){
            dp.show();
        }else if(id == R.id.btnSearch){
            LibInspira.setShared(global.omzetpreferences, global.omzet.nomorsales, nomorsales);
            LibInspira.setShared(global.omzetpreferences, global.omzet.enddate, tvEndDate.getText().toString());
            if (rbtnBulan.isChecked()){
                LibInspira.setShared(global.omzetpreferences, global.omzet.bulantahun, "bulan");
            }else{
                LibInspira.setShared(global.omzetpreferences, global.omzet.bulantahun, "tahun");
            }
            LibInspira.setShared(global.datapreferences, global.data.salesmanomzet, "");
            LibInspira.ReplaceFragment(getActivity().getSupportFragmentManager(), R.id.fragment_container, new SalesOmzetFragment());
        }
    }
}
