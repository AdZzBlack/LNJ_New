/******************************************************************************
    Author           : ADI
    Description      : dashboard untuk internal
    History          :

******************************************************************************/
package layout;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspira.lnj.LibInspira;
import com.inspira.lnj.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.inspira.lnj.IndexInternal.global;
import static com.inspira.lnj.IndexInternal.jsonObject;

//import android.app.Fragment;

public class ChooseAllSuratJalanFragment extends Fragment implements View.OnClickListener{
    private EditText etSearch;
    private ImageButton ibtnSearch;
    private TextView tvInformation, tvNoData;
    private ListView lvSearch;
    private ItemListAdapter itemadapter;
    private ArrayList<ItemAdapter> list;
    private FloatingActionButton fab;
    private DeliveryOrder deliveryOrder;

    public ChooseAllSuratJalanFragment() {
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
        getActivity().setTitle("All Delivery Order List");
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
        list = new ArrayList<ItemAdapter>();

        ((RelativeLayout) getView().findViewById(R.id.rlSearch)).setVisibility(View.VISIBLE);
        tvInformation = (TextView) getView().findViewById(R.id.tvInformation);
        tvNoData = (TextView) getView().findViewById(R.id.tvNoData);
        etSearch = (EditText) getView().findViewById(R.id.etSearch);
        fab = (FloatingActionButton) getView().findViewById(R.id.fab);

        itemadapter = new ItemListAdapter(getActivity(), R.layout.list_item, new ArrayList<ItemAdapter>());
        itemadapter.clear();
        lvSearch = (ListView) getView().findViewById(R.id.lvChoose);
        lvSearch.setAdapter(itemadapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                search();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        LibInspira.setShared(global.datapreferences, global.data.deliveryorderlist, "");

        fab.setOnClickListener(this);
        fab.setVisibility(View.VISIBLE);
        String ActionUrl = "Master/getAllDeliveryOrder/";
        deliveryOrder = new DeliveryOrder();
        deliveryOrder.execute(ActionUrl);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (deliveryOrder != null) deliveryOrder.cancel(true);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if(id==R.id.ibtnSearch)
        {
            search();
        }else if(id==R.id.fab){
            LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new BarCodeCheckinFragment());
        }
    }

    private void search()
    {
        itemadapter.clear();
        for(int ctr=0;ctr<list.size();ctr++)
        {
            if(etSearch.getText().equals(""))
            {
                itemadapter.add(list.get(ctr));
                itemadapter.notifyDataSetChanged();
            }
            else
            {
                String strList = list.get(ctr).getNama();
                if(strList != null && LibInspira.contains(strList, etSearch.getText().toString()))
                {
                    itemadapter.add(list.get(ctr));
                    itemadapter.notifyDataSetChanged();
                }
            }
        }
    }

    private void refreshList()
    {
        itemadapter.clear();
        list.clear();

        String data = LibInspira.getShared(global.datapreferences, global.data.deliveryorderlist, "");
        String[] pieces = data.trim().split("\\|");
        if(pieces.length==1 && pieces[0].equals(""))
        {
            tvNoData.setVisibility(View.VISIBLE);
        }
        else
        {
            tvNoData.setVisibility(View.GONE);
            for(int i=0 ; i < pieces.length ; i++){
                if(!pieces[i].equals(""))
                {
                    ItemAdapter dataItem = new ItemAdapter();
                    dataItem.setKode(pieces[i]);
                    dataItem.setNama(pieces[i]);
                    list.add(dataItem);

                    itemadapter.add(dataItem);
                    itemadapter.notifyDataSetChanged();
                }
            }
        }
    }

    public class ItemAdapter {

        private String nomor;
        private String nama;
        private String kode;
        private Boolean isChoosen = false;

        public ItemAdapter() {}

        public String getNomor() {return nomor;}
        public void setNomor(String _param) {this.nomor = _param;}

        public String getNama() {return nama;}
        public void setNama(String _param) {this.nama = _param;}

        public String getKode() {return kode;}
        public void setKode(String _param) {this.kode = _param;}

        public Boolean getChoosen() {return isChoosen;}
        public void setChoosen(Boolean _param) {this.isChoosen = _param;}
    }

    public class ItemListAdapter extends ArrayAdapter<ItemAdapter> {

        private List<ItemAdapter> items;
        private int layoutResourceId;
        private Context context;

        public ItemListAdapter(Context context, int layoutResourceId, List<ItemAdapter> items) {
            super(context, layoutResourceId, items);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.items = items;
        }

        public List<ItemAdapter> getItems() {
            return items;
        }

        public class Holder {
            ItemAdapter adapterItem;
            TextView tvNama;
            ImageButton ibtnDelete;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            Holder holder = null;

            if(row==null)
            {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);
            }

            holder = new Holder();
            holder.adapterItem = items.get(position);
            holder.tvNama = (TextView)row.findViewById(R.id.tvName);
            holder.ibtnDelete = (ImageButton) row.findViewById(R.id.ibtnDelete);
            holder.ibtnDelete.setVisibility(View.VISIBLE);

            row.setTag(holder);
            setupItem(holder, row);

            final Holder finalHolder = holder;
            final View finalRow = row;
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(finalHolder.adapterItem.getChoosen())
                    {
                        finalHolder.adapterItem.setChoosen(false);
                        finalRow.setBackgroundColor(getResources().getColor(R.color.colorBackground));
                    }
                    else
                    {
                        finalHolder.adapterItem.setChoosen(true);
                        finalRow.setBackgroundColor(getResources().getColor(R.color.colorAccentDanger));
                    }
//                    LibInspira.showLongToast(context, finalHolder.adapterItem.getKode());
                    LibInspira.setShared(global.userpreferences,global.user.checkin_nomortdsuratjalan, finalHolder.adapterItem.getKode().substring(1));
                    LibInspira.setShared(global.userpreferences,global.user.checkin_kodesuratjalan, finalHolder.adapterItem.getKode());  //added by Tonny @16-Dec-2017
                    LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new FormTrackingFragment());
                }
            });

            holder.ibtnDelete.setVisibility(View.GONE);
            holder.ibtnDelete.setOnClickListener(null);

            return row;
        }

        private void setupItem(final Holder holder, final View row) {
            holder.tvNama.setText(holder.adapterItem.getKode().toUpperCase());
            if(holder.adapterItem.getChoosen())
            {
                row.setBackgroundColor(getResources().getColor(R.color.colorAccentDanger));
            }
            else
            {
                row.setBackgroundColor(getResources().getColor(R.color.colorBackground));
            }
        }
    }

    //added by Tonny @05-Dec-2017 untuk mendapatkan list document DO
    private class DeliveryOrder extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            jsonObject = new JSONObject();
            return LibInspira.executePost(getContext(), urls[0], jsonObject);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("tes", result);
            try {
                String tempData = "";
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        LibInspira.hideLoading();
                        if(!obj.has("query")){  //jika success mendapatkan data
                            if(!obj.has("message")){  // jika ada message, berarti data kosong
                                String nomor = obj.getString("nomor");
                                if(!nomor.equals("")) {
                                    String strNomor = "D" + nomor;
                                    tempData = tempData + strNomor + "|";
                                    LibInspira.setShared(global.datapreferences, global.data.deliveryorderlist, LibInspira.getShared(global.datapreferences, global.data.deliveryorderlist, "") +
                                            strNomor + "|");
                                }
                                if(!tempData.equals(LibInspira.getShared(global.datapreferences, global.data.deliveryorderlist, "")))
                                {
                                    LibInspira.setShared(global.datapreferences, global.data.deliveryorderlist, tempData);
                                }
                            }else{
                                LibInspira.setShared(global.datapreferences, global.data.deliveryorderlist, "");
                            }
                        }
                        else
                        {
                            LibInspira.showLongToast(getContext(), obj.getString("message"));
                            Log.wtf("error ", obj.getString("query"));
                        }
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
                LibInspira.showLongToast(getContext(), e.getMessage());
            }
            tvInformation.animate().translationYBy(-80);
            refreshList();
            LibInspira.hideLoading();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LibInspira.showLoading(getContext(), "Retrieving data", "Loading");
            tvInformation.setVisibility(View.VISIBLE);
        }
    }
}
