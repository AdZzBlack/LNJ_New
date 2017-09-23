/******************************************************************************
    Author           : ADI
    Description      : untuk mengisi header sales order
    History          :

******************************************************************************/
package layout;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.inspira.gms.GlobalVar;
import com.inspira.gms.LibInspira;
import com.inspira.gms.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.inspira.gms.IndexInternal.global;

//import android.app.Fragment;

public class FormSalesOrderHeaderFragment extends Fragment implements View.OnClickListener{

    private TextView tvDate, tvCustomer, tvBroker, tvValuta, tvProyek;
    private Button btnNext;
    private DatePickerDialog dp;
    private CheckBox chkBarangImport;
    private Spinner spJenis, spPerhitunganBarangCustom;

    public FormSalesOrderHeaderFragment() {
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
        View v = inflater.inflate(R.layout.fragment_sales_order_header, container, false);
        getActivity().setTitle("Header Sales Order");
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
        tvCustomer = (TextView) getView().findViewById(R.id.tvCustomer);
        tvBroker = (TextView) getView().findViewById(R.id.tvBroker);
        tvValuta = (TextView) getView().findViewById(R.id.tvValuta);
        tvProyek = (TextView) getView().findViewById(R.id.tvProyek);
        tvDate = (TextView) getView().findViewById(R.id.tvDate); //added by Tonny @30-Aug-2017
        btnNext = (Button) getView().findViewById(R.id.btnNext);
        chkBarangImport = (CheckBox) getView().findViewById(R.id.chkBarangImport);
        spJenis = (Spinner) getView().findViewById(R.id.spJenis);
        spPerhitunganBarangCustom = (Spinner) getView().findViewById(R.id.spPerhitunganBarangCustom);

        tvCustomer.setOnClickListener(this);
        tvBroker.setOnClickListener(this);
        tvValuta.setOnClickListener(this);
        tvProyek.setOnClickListener(this);
        tvDate.setOnClickListener(this);  //added by Tonny @30-Aug-2017
        btnNext.setOnClickListener(this);
        chkBarangImport.setOnClickListener(this);  //added by Tonny @07-Sep-2017

        tvCustomer.setText(LibInspira.getShared(global.temppreferences, global.temp.salesorder_customer_nama, "").toUpperCase());
        tvValuta.setText(LibInspira.getShared(global.temppreferences, global.temp.salesorder_valuta_nama, "").toUpperCase());
        tvBroker.setText(LibInspira.getShared(global.temppreferences, global.temp.salesorder_broker_nama, "").toUpperCase());
        tvProyek.setText(LibInspira.getShared(global.temppreferences, global.temp.salesorder_proyek_nama, "").toUpperCase());

        //added by Tonny @08-Sep-2017 jika preferences import, maka centang chkBarangImport
        if(LibInspira.getShared(global.temppreferences, global.temp.salesorder_import, "0").equals("1")){
            chkBarangImport.setChecked(true);
        }else{
            chkBarangImport.setChecked(false);
        }

        if (!LibInspira.getShared(global.temppreferences, global.temp.salesorder_date, "").equals("")){
            tvDate.setText(LibInspira.getShared(global.temppreferences, global.temp.salesorder_date, ""));
        }
        // Declare DatePicker
        Calendar newCalendar = Calendar.getInstance();
        dp = new DatePickerDialog(getActivity(), R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                try {
                    String date = year + "-" + (monthOfYear+1) + "-" + dayOfMonth;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date newdate = sdf.parse(date);
                    date = sdf.format(newdate);

                    tvDate.setText(date);
                    LibInspira.setShared(global.temppreferences, global.temp.salesorder_date, tvDate.getText().toString());
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        if(LibInspira.getShared(global.temppreferences, global.temp.salesorder_type_proyek, "").equals("proyek"))
        {
            getView().findViewById(R.id.trProyek).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trJenis).setVisibility(View.GONE);
            getView().findViewById(R.id.trImport).setVisibility(View.GONE);

            LibInspira.setShared(global.temppreferences, global.temp.salesorder_jenis, "0");
            LibInspira.setShared(global.temppreferences, global.temp.salesorder_import, "0");
            LibInspira.setShared(global.temppreferences, global.temp.salesorder_pekerjaan, "");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View view) {
        view.startAnimation(GlobalVar.buttoneffect);
        int id = view.getId();

        LibInspira.setShared(global.sharedpreferences, global.shared.position, "salesorder");

        if(id==R.id.tvCustomer)
        {
            LibInspira.ReplaceFragment(getActivity().getSupportFragmentManager(), R.id.fragment_container, new ChooseCustomerFragment());
        }
        else if(id==R.id.tvTarget)
        {
            LibInspira.ReplaceFragment(getActivity().getSupportFragmentManager(), R.id.fragment_container, new ChooseSalesmanFragment());
        }
        else if(id==R.id.tvBroker)
        {
            LibInspira.ReplaceFragment(getActivity().getSupportFragmentManager(), R.id.fragment_container, new ChooseBrokerFragment());
        }
        else if(id==R.id.tvValuta)
        {
            LibInspira.ReplaceFragment(getActivity().getSupportFragmentManager(), R.id.fragment_container, new ChooseValutaFragment());
        }
        else if(id==R.id.tvProyek)
        {
            LibInspira.ReplaceFragment(getActivity().getSupportFragmentManager(), R.id.fragment_container, new ChooseProyekFragment());
        }
        else if(id==R.id.tvDate) {  //added by Tonny @30-Aug-2017
            dp.show();
        }
        else if(id==R.id.chkBarangImport)
        {
            //jika user menekan button chkBarangImport, maka hapus cache / preferences dari salesorder item yang telah dibuat sebelumnya
            LibInspira.setShared(global.temppreferences, global.temp.salesorder_item, "");
        }
        else if(id==R.id.btnNext)
        {
            if(LibInspira.getShared(global.temppreferences, global.temp.salesorder_customer_nomor, "").equals("") ||
                    LibInspira.getShared(global.temppreferences, global.temp.salesorder_broker_nomor, "").equals("") ||
                    LibInspira.getShared(global.temppreferences, global.temp.salesorder_valuta_nomor, "").equals("")
                    )
            {
                LibInspira.ShowShortToast(getContext(), "All Field Required");
            }
            else
            {
                if (chkBarangImport.isChecked()){
                    if(!LibInspira.getShared(global.temppreferences, global.temp.salesorder_import, "").equals("1"))
                    {
                        LibInspira.setShared(global.temppreferences, global.temp.salesorder_item, "");
                        LibInspira.setShared(global.temppreferences, global.temp.salesorder_import, "1");
                    }
                }else{
                    if(!LibInspira.getShared(global.temppreferences, global.temp.salesorder_import, "").equals("0"))
                    {
                        LibInspira.setShared(global.temppreferences, global.temp.salesorder_item, "");
                        LibInspira.setShared(global.temppreferences, global.temp.salesorder_import, "0");
                    }
                }

                if(!LibInspira.getShared(global.temppreferences, global.temp.salesorder_jenis, "").equals(spJenis.getSelectedItemPosition()))
                {
                    LibInspira.setShared(global.temppreferences, global.temp.salesorder_item, "");
                    LibInspira.setShared(global.temppreferences, global.temp.salesorder_jenis, String.valueOf(spJenis.getSelectedItemPosition()));
                }

                LibInspira.setShared(global.temppreferences, global.temp.salesorder_perhitungan_barang_custom, String.valueOf(spPerhitunganBarangCustom.getSelectedItemPosition()));

                LibInspira.ReplaceFragment(getActivity().getSupportFragmentManager(), R.id.fragment_container, new FormSalesOrderDetailItemListFragment());
            }
        }
    }
}
