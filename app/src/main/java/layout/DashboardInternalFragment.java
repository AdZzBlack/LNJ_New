/******************************************************************************
    Author           : ADI
    Description      : dashboard untuk internal
    History          :

******************************************************************************/
package layout;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inspira.lnj.LibInspira;
import com.inspira.lnj.R;

import static com.inspira.lnj.IndexInternal.RefreshUserData;
import static com.inspira.lnj.IndexInternal.global;

//import android.app.Fragment;

public class DashboardInternalFragment extends Fragment implements View.OnClickListener{
    public DashboardInternalFragment() {
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
        View v = inflater.inflate(R.layout.fragment_dashboard_internal, container, false);
        getActivity().setTitle("Dashboard");
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
        getView().findViewById(R.id.btnCheckIn).setOnClickListener(this);
        getView().findViewById(R.id.btnQRCode).setOnClickListener(this);
        getView().findViewById(R.id.btnDocument).setOnClickListener(this);
        getView().findViewById(R.id.btnPendingDocs).setOnClickListener(this);
        getView().findViewById(R.id.btnAcceptedDocs).setOnClickListener(this);
        getView().findViewById(R.id.btnNewWayPoint).setOnClickListener(this);
        getView().findViewById(R.id.btnContainerLoadingReport).setOnClickListener(this);

        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                RefreshUserData();
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 3000);
            }
        });

        //remarked by Tonny @01-Nov-2017  diremark supaya user dapat kembali ke dashboard
//        if(!LibInspira.getShared(global.userpreferences, global.user.checkin_nomorthsuratjalan, "").equals(""))
//        {
//            LibInspira.AddFragment(getFragmentManager(), R.id.fragment_container, new FormTrackingFragment());
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        LibInspira.clearShared(global.temppreferences);

        if(id==R.id.btnCheckIn)
        {
            //LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new BarCodeCheckinFragment());
            LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new ChooseSuratJalanFragment());
        }
        else if (id == R.id.btnNewWayPoint){
            LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new WaypointFragment());
        }
        else if (id == R.id.btnQRCode){
            LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new QRCodeFragment());
        }
        else if (id == R.id.btnDocument){
            LibInspira.setShared(global.sharedpreferences, global.shared.position, "document");
            LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new ChooseUserFragment());
        }
        else if (id == R.id.btnPendingDocs){
            LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new ChoosePendingDocumentFragment());
        }
        else if (id == R.id.btnAcceptedDocs){
            LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new ChooseCompletedDocumentFragment());
        }
        else if (id == R.id.btnContainerLoadingReport){
            LibInspira.setShared(global.sharedpreferences, global.shared.position, "container loading");
            LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new FormContainerLoadingHeaderFragment());
        }
    }
}
