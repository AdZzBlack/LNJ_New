/******************************************************************************
    Author           : Tonny
    Description      : list document yang sudah selesai
    History          :

******************************************************************************/
package layout;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inspira.lnj.LibInspira;
import com.inspira.lnj.R;

import static com.inspira.lnj.IndexInternal.global;

//import android.app.Fragment;

public class ChooseCompletedDocumentFragment extends ChoosePendingDocumentFragment implements View.OnClickListener{
    public ChooseCompletedDocumentFragment() {
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
        View v = inflater.inflate(R.layout.fragment_choose, container, false);
        getActivity().setTitle("Accepted Document");
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
        status = "finish";
        LibInspira.setShared(global.datapreferences, global.data.doclist, "");
        super.onActivityCreated(bundle);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
    }

    @Override
    protected void refreshList()
    {
        super.refreshList();
    }

}
