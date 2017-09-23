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
import android.widget.Button;

import com.inspira.gms.GlobalVar;
import com.inspira.gms.LibInspira;
import com.inspira.gms.R;

//import android.app.Fragment;

public class PenjualanSubFragment extends Fragment implements View.OnClickListener{
    private Button btnSalesOrder, btnDeliveryOrder;

    public PenjualanSubFragment() {
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
        View v = inflater.inflate(R.layout.fragment_penjualan_sub, container, false);
        getActivity().setTitle("Sales Order");
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

        btnSalesOrder = (Button) getView().findViewById(R.id.btnSalesOrder);
        btnDeliveryOrder = (Button) getView().findViewById(R.id.btnDeliveryOrder);

        btnSalesOrder.setOnClickListener(this);
        btnDeliveryOrder.setOnClickListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        view.startAnimation(GlobalVar.buttoneffect);
        if(id==R.id.btnSalesOrder)
        {
            LibInspira.ReplaceFragment(getActivity().getSupportFragmentManager(), R.id.fragment_container, new FormSalesOrderHeaderFragment());
        }
        else if(id==R.id.btnDeliveryOrder)
        {
            //remarked by Tonny @04-Sep-2017
            //LibInspira.ReplaceFragment(getActivity().getSupportFragmentManager(), R.id.fragment_container, new FormScheduleTaskFragment());
        }
    }
}
