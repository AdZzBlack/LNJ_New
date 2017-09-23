/******************************************************************************
    Author           : ADI
    Description      : dashboard untuk internal
    History          :

******************************************************************************/
package layout;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.inspira.gms.LibInspira;
import com.inspira.gms.R;

import static com.inspira.gms.IndexInternal.global;

//import android.app.Fragment;

public class StockMonitoringFragment extends Fragment implements View.OnClickListener{
    public StockMonitoringFragment() {
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
        View v = inflater.inflate(R.layout.fragment_stock_monitoring, container, false);
        getActivity().setTitle("Stock Monitoring");
        return v;
    }

    /*****************************************************************************/
    //OnAttach dijalankan pada saat fragment ini terpasang pada Activity penampungnya
    /*****************************************************************************/
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    //untuk mapping UI pada fragment, jangan dilakukan pada OnCreate, tapi dilakukan pada onActivityCreated
    @Override
    public void onActivityCreated(Bundle bundle){
        super.onActivityCreated(bundle);
        //added by Tonny @17-Aug-2017
        getView().findViewById(R.id.btnPosisiStok).setOnClickListener(this);
        getView().findViewById(R.id.btnPosisiStokRandom).setOnClickListener(this);
        getView().findViewById(R.id.btnRandomPerBarang).setOnClickListener(this);
        getView().findViewById(R.id.btnRandomPerLokasi).setOnClickListener(this);
        getView().findViewById(R.id.btnMutasiStok).setOnClickListener(this);
        getView().findViewById(R.id.btnKartuStok).setOnClickListener(this);
        getView().findViewById(R.id.btnRekapGlobalStok).setOnClickListener(this);
        getView().findViewById(R.id.btnRekapCutSize).setOnClickListener(this);
        getView().findViewById(R.id.btnRekapUkuranVariasi).setOnClickListener(this);
        getView().findViewById(R.id.btnLaporanRekapCutSize).setOnClickListener(this);
        getView().findViewById(R.id.btnRekapStok).setOnClickListener(this);
        getView().findViewById(R.id.btnLapRekapStokPerJenisGrade).setOnClickListener(this);
        ///
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        LibInspira.clearShared(global.stockmonitoringpreferences);

        if(id==R.id.btnPosisiStok)
        {
            LibInspira.setShared(global.sharedpreferences, global.shared.position, "stockposition");
            LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new FilterStockFragment());
        }
        else if(id==R.id.btnPosisiStokRandom)
        {
            LibInspira.setShared(global.sharedpreferences, global.shared.position, "stockpositionrandom");
            LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new FilterStockFragment());
        }
        else if(id==R.id.btnRandomPerBarang)
        {
            LibInspira.setShared(global.sharedpreferences, global.shared.position, "stockrandomperbarang");
            LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new FilterStockFragment());
        }
        else if(id==R.id.btnRandomPerLokasi)
        {
            LibInspira.setShared(global.sharedpreferences, global.shared.position, "stockrandomperlokasi");
            LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new FilterStockFragment());
        }
        else if(id==R.id.btnMutasiStok)
        {
            LibInspira.setShared(global.sharedpreferences, global.shared.position, "stockmutasi");
            LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new FilterStockFragment());
        }
        else if(id==R.id.btnKartuStok)
        {
            LibInspira.setShared(global.sharedpreferences, global.shared.position, "stockkartu");
            LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new FilterStockFragment());
        }
        else if(id==R.id.btnRekapGlobalStok)
        {

        }
        else if(id==R.id.btnRekapCutSize)
        {

        }
        else if(id==R.id.btnRekapUkuranVariasi)
        {

        }
        else if(id==R.id.btnLaporanRekapCutSize)
        {

        }
        else if(id==R.id.btnRekapStok)
        {

        }
        else if(id==R.id.btnLapRekapStokPerJenisGrade)
        {

        }
    }
}
