/******************************************************************************
    Author           : ADI
    Description      : dashboard untuk internal
    History          :

******************************************************************************/
package layout;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inspira.lnj.GlobalVar;
import com.inspira.lnj.LibInspira;
import com.inspira.lnj.R;

import org.json.JSONObject;
import org.w3c.dom.Text;

//import android.app.Fragment;

public class FormContainerLoadingHeaderFragment extends Fragment implements View.OnClickListener{

    private GlobalVar global;
    private JSONObject jsonObject;

    private TextView tvJob, tvInvoice, tvStuffingDate, tvPortOfLoading, tvPortOfDischarge;

    public FormContainerLoadingHeaderFragment() {
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
        View v = inflater.inflate(R.layout.fragment_form_container_loading, container, false);
        getActivity().setTitle("Container Loading Report");
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

        tvJob = (TextView) getView().findViewById(R.id.tvJob);
        tvInvoice = (TextView) getView().findViewById(R.id.tvInvoice);
        tvStuffingDate = (TextView) getView().findViewById(R.id.tvStuffingDate);
        tvPortOfLoading = (TextView) getView().findViewById(R.id.tvPortOfLoading);
        tvPortOfDischarge = (TextView) getView().findViewById(R.id.tvPortOfDischarge);

        tvJob.setOnClickListener(this);
        getView().findViewById(R.id.btnNext).setOnClickListener(this);

        if(!LibInspira.getShared(global.temppreferences, global.temp.selected_job_nomor, "").equals(""))
        {
            tvJob.setText(LibInspira.getShared(global.temppreferences, global.temp.selected_job_kode, ""));
            tvInvoice.setText(LibInspira.getShared(global.temppreferences, global.temp.selected_job_invoice, ""));
            tvStuffingDate.setText(LibInspira.getShared(global.temppreferences, global.temp.selected_job_stuffingdate, ""));
            tvPortOfLoading.setText(LibInspira.getShared(global.temppreferences, global.temp.selected_job_pol, ""));
            tvPortOfDischarge.setText(LibInspira.getShared(global.temppreferences, global.temp.selected_job_pod, ""));
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id==R.id.tvJob)
        {
            LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new ChooseJobFragment());
        }
        else if(id==R.id.btnNext)
        {
            if(!LibInspira.getShared(global.temppreferences, global.temp.selected_job_nomor, "").equals(""))
            {
                LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new FormContainerLoadingDetailFragment());
            }
            else
            {
                LibInspira.showShortToast(getContext(), "Job Required");
            }
        }
    }
}
