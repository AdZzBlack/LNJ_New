/******************************************************************************
    Author           : Tonny
    Description      : List document pending
    History          :

******************************************************************************/
package layout;

import android.app.Activity;
import android.content.Context;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
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

public class ChooseJobFragment extends Fragment implements View.OnClickListener{
    private EditText etSearch;
    private ImageButton ibtnSearch;

    private TextView tvInformation, tvNoData;
    private ListView lvSearch;
    private ItemListAdapter itemadapter;
    private ArrayList<ItemAdapter> list;
    private GetData getData;

    public ChooseJobFragment() {
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
        getActivity().setTitle("Container Load Report");
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

        getView().findViewById(R.id.rlSearch).setVisibility(View.VISIBLE);
        tvInformation = (TextView) getView().findViewById(R.id.tvInformation);
        tvNoData = (TextView) getView().findViewById(R.id.tvNoData);
        etSearch = (EditText) getView().findViewById(R.id.etSearch);

        itemadapter = new ItemListAdapter(getActivity(), R.layout.list_item, new ArrayList<ItemAdapter>());
        itemadapter.clear();
        lvSearch = (ListView) getView().findViewById(R.id.lvChoose);
        lvSearch.setAdapter(itemadapter);

        //added by Tonny @17-Jan-2018
        ibtnSearch = (ImageButton) getView().findViewById(R.id.ibtnSearch);
        ibtnSearch.setOnClickListener(this);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //remarked by Tonny @17-Jan-2018
//                if(etSearch.getText().toString().length()>=4)
//                {
//                    String actionUrl = "Master/getJob/";
//                    getData = new GetData(etSearch.getText().toString());
//                    getData.execute( actionUrl );
//                }
//                search();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        refreshList();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(getData != null){
            getData.cancel(true);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if(id==R.id.ibtnSearch)
        {
//            search();
            //added by Tonny @17-Jan-2018
            String actionUrl = "Master/getJob/";
            getData = new GetData(etSearch.getText().toString());
            getData.execute( actionUrl );
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
                if(LibInspira.contains(list.get(ctr).getKode(),etSearch.getText().toString() ))
                {
                    itemadapter.add(list.get(ctr));
                    itemadapter.notifyDataSetChanged();
                }
            }
        }
    }

    protected void refreshList()
    {
        itemadapter.clear();
        list.clear();

        String data = LibInspira.getShared(global.datapreferences, global.data.job, "");
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
                    String[] parts = pieces[i].trim().split("~");

                    if(parts.length==6)
                    {
                        String nomor = parts[0];
                        String kode = parts[1];
                        String stuffingdate = parts[2];
                        String invoice = parts[3];
                        String pol = parts[4];
                        String pod = parts[5];

                        if(nomor.equals("")) nomor = "null";
                        if(kode.equals("")) kode = "null";
                        if(stuffingdate.equals("")) stuffingdate = "null";
                        if(invoice.equals("")) invoice = "null";
                        if(pol.equals("")) pol = "null";
                        if(pod.equals("")) pod = "null";

                        ItemAdapter dataItem = new ItemAdapter();
                        dataItem.setNomor(nomor);
                        dataItem.setKode(kode);
                        dataItem.setStuffingdate(stuffingdate);
                        dataItem.setInvoice(invoice);
                        dataItem.setPol(pol);
                        dataItem.setPod(pod);
                        list.add(dataItem);

                        itemadapter.add(dataItem);
                        itemadapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }

    private class GetData extends AsyncTask<String, Void, String> {
        String keyword;
        public GetData(String _keyword)
        {
            keyword = _keyword;
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                jsonObject = new JSONObject();
                jsonObject.put("keyword", keyword);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return LibInspira.executePost(getContext(), urls[0], jsonObject);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("resultQuery", result);
            try
            {
                String tempData= "";
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if(!obj.has("query")){
                            String nomor = (obj.getString("nomor"));
                            String kode = (obj.getString("kode"));
                            String stuffingdate = (obj.getString("stuffingdate"));
                            String invoice = (obj.getString("invoice"));
                            String pol = (obj.getString("pol"));
                            String pod = (obj.getString("pod"));

                            if(nomor.equals("")) nomor = "null";
                            if(kode.equals("")) kode = "null";
                            if(stuffingdate.equals("")) stuffingdate = "null";
                            if(invoice.equals("")) invoice = "null";
                            if(pol.equals("")) pol = "null";
                            if(pod.equals("")) pod = "null";

                            tempData = tempData + nomor + "~" + kode + "~" + stuffingdate+ "~" + invoice + "~" + pol + "~" + pod + "|";
                        }
                    }
                    if(!tempData.equals(LibInspira.getShared(global.datapreferences, global.data.job, "")))
                    {
                        LibInspira.setShared(
                                global.datapreferences,
                                global.data.job,
                                tempData
                        );
                        refreshList();
                    }
                }
                tvInformation.animate().translationYBy(-80);
                LibInspira.hideLoading();
            }
            catch(Exception e)
            {
                e.printStackTrace();
//                tvInformation.animate().translationYBy(-80);
                LibInspira.hideLoading();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            tvInformation.setVisibility(View.VISIBLE);
            LibInspira.showLoading(getContext(), "Searching", "Loading");
        }
    }

    public class ItemAdapter {

        private String nomor;
        private String kode;
        private String stuffingdate;
        private String invoice;
        private String pol;
        private String pod;


        public ItemAdapter() {}

        public String getNomor() {return nomor;}
        public void setNomor(String _param) {this.nomor = _param;}

        public String getKode() {return kode;}
        public void setKode(String _param) {this.kode = _param;}

        public String getStuffingdate() {return stuffingdate;}
        public void setStuffingdate(String _param) {this.stuffingdate = _param;}

        public String getInvoice() {return invoice;}
        public void setInvoice(String _param) {this.invoice = _param;}

        public String getPol() {return pol;}
        public void setPol(String _param) {this.pol = _param;}

        public String getPod() {return pod;}
        public void setPod(String _param) {this.pod = _param;}
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
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //remarked by Tonny @09-Jan-2018  untuk mengatasi bug pada saat scroll
//            View row = convertView;
            View row = null;
            Holder holder = null;

            if(row==null)
            {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);
            }

            holder = new Holder();
            holder.adapterItem = items.get(position);

            holder.tvNama = (TextView) row.findViewById(R.id.tvName);

            row.setTag(holder);
            setupItem(holder, row);
            final Holder finalHolder = holder;
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(LibInspira.getShared(global.sharedpreferences, global.shared.position, "").equals("container loading"))
                    {
                        String nomor = finalHolder.adapterItem.getNomor();
                        String kode = finalHolder.adapterItem.getKode();
                        String stuffingdate = finalHolder.adapterItem.getStuffingdate();
                        String invoice = finalHolder.adapterItem.getInvoice();
                        String pol = finalHolder.adapterItem.getPol();
                        String pod = finalHolder.adapterItem.getPod();

                        if(nomor.equals("null")) nomor = "";
                        if(kode.equals("null")) kode = "";
                        if(stuffingdate.equals("null")) stuffingdate = "";
                        if(invoice.equals("null")) invoice = "";
                        if(pol.equals("null")) pol = "";
                        if(pod.equals("null")) pod = "";

                        LibInspira.setShared(global.temppreferences, global.temp.selected_job_nomor, nomor);
                        LibInspira.setShared(global.temppreferences, global.temp.selected_job_kode, kode);
                        LibInspira.setShared(global.temppreferences, global.temp.selected_job_stuffingdate, stuffingdate);
                        LibInspira.setShared(global.temppreferences, global.temp.selected_job_invoice, invoice);
                        LibInspira.setShared(global.temppreferences, global.temp.selected_job_pol, pol);
                        LibInspira.setShared(global.temppreferences, global.temp.selected_job_pod, pod);

                        LibInspira.BackFragment(getActivity().getSupportFragmentManager());
                    }
                }
            });
            return row;
        }

        private void setupItem(final Holder holder, final View row) {
            holder.tvNama.setText(holder.adapterItem.getKode().toUpperCase());
        }
    }

}
