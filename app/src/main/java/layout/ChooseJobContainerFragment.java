/******************************************************************************
    Author           : Tonny
    Description      : List document pending
    History          :

******************************************************************************/
package layout;

import android.app.Activity;
import android.content.Context;
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

public class ChooseJobContainerFragment extends Fragment implements View.OnClickListener{
    private EditText etSearch;
    private ImageButton ibtnSearch;

    private TextView tvInformation, tvNoData;
    private ListView lvSearch;
    private ItemListAdapter itemadapter;
    private ArrayList<ItemAdapter> list;
    private GetData getData;

    public ChooseJobContainerFragment() {
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
        getActivity().setTitle("Container");
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

        itemadapter = new ItemListAdapter(getActivity(), R.layout.list_container, new ArrayList<ItemAdapter>());
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

        refreshList();

        String actionUrl = "Master/getJobContainer/";
        getData = new GetData();
        getData.execute( actionUrl );
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
            search();
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

        String data = LibInspira.getShared(global.temppreferences, global.temp.temp, "");
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

                    if(parts.length==5)
                    {
                        String nomor = parts[0];
                        String kode = parts[1];
                        String type = parts[2];
                        String size = parts[3];
                        String seal = parts[4];

                        if(nomor.equals("")) nomor = "null";
                        if(kode.equals("")) kode = "null";
                        if(type.equals("")) type = "null";
                        if(size.equals("")) size = "null";
                        if(seal.equals("")) seal = "null";

                        ItemAdapter dataItem = new ItemAdapter();
                        dataItem.setNomor(nomor);
                        dataItem.setKode(kode);
                        dataItem.setType(type);
                        dataItem.setSize(size);
                        dataItem.setSeal(seal);
                        list.add(dataItem);

                        itemadapter.add(dataItem);
                        itemadapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }

    private class GetData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                jsonObject = new JSONObject();
                jsonObject.put("nomor", LibInspira.getShared(global.temppreferences,global.temp.selected_job_nomor,""));
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
                            String type = (obj.getString("type"));
                            String size = (obj.getString("size"));
                            String seal = (obj.getString("seal"));

                            if(nomor.equals("")) nomor = "null";
                            if(kode.equals("")) kode = "null";
                            if(type.equals("")) type = "null";
                            if(size.equals("")) size = "null";
                            if(seal.equals("")) seal = "null";

                            tempData = tempData + nomor + "~" + kode + "~" + type+ "~" + size + "~" + seal + "|";
                        }
                    }
                    if(!tempData.equals(LibInspira.getShared(global.temppreferences, global.temp.temp, "")))
                    {
                        LibInspira.setShared(
                                global.temppreferences,
                                global.temp.temp,
                                tempData
                        );
                        refreshList();
                    }
                }
                tvInformation.animate().translationYBy(-80);
            }
            catch(Exception e)
            {
                e.printStackTrace();
                tvInformation.animate().translationYBy(-80);
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tvInformation.setVisibility(View.VISIBLE);
        }
    }

    public class ItemAdapter {

        private String nomor;
        private String kode;
        private String size;
        private String type;
        private String seal;


        public ItemAdapter() {}

        public String getNomor() {return nomor;}
        public void setNomor(String _param) {this.nomor = _param;}

        public String getKode() {return kode;}
        public void setKode(String _param) {this.kode = _param;}

        public String getSize() {return size;}
        public void setSize(String _param) {this.size = _param;}

        public String getType() {return type;}
        public void setType(String _param) {this.type = _param;}

        public String getSeal() {return seal;}
        public void setSeal(String _param) {this.seal = _param;}
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
            TextView tvKode, tvType, tvSize, tvSeal;
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

            holder.tvKode = (TextView) row.findViewById(R.id.tvKode);
            holder.tvType = (TextView) row.findViewById(R.id.tvType);
            holder.tvSize = (TextView) row.findViewById(R.id.tvSize);
            holder.tvSeal = (TextView) row.findViewById(R.id.tvSeal);

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
                        String size = finalHolder.adapterItem.getSize();
                        String type = finalHolder.adapterItem.getType();
                        String seal = finalHolder.adapterItem.getSeal();

                        if(nomor.equals("null")) nomor = "";
                        if(kode.equals("null")) kode = "";
                        if(size.equals("null")) size = "";
                        if(type.equals("null")) type = "";
                        if(seal.equals("null")) seal = "";

                        LibInspira.setShared(global.temppreferences, global.temp.selected_container_nomor, nomor);
                        LibInspira.setShared(global.temppreferences, global.temp.selected_container_kode, kode);
                        LibInspira.setShared(global.temppreferences, global.temp.selected_container_size, size);
                        LibInspira.setShared(global.temppreferences, global.temp.selected_container_type, type);
                        LibInspira.setShared(global.temppreferences, global.temp.selected_container_seal, seal);

                        LibInspira.BackFragment(getActivity().getSupportFragmentManager());
                    }
                }
            });
            return row;
        }

        private void setupItem(final Holder holder, final View row) {
            holder.tvKode.setText(holder.adapterItem.getKode().toUpperCase());
            holder.tvType.setText(holder.adapterItem.getType().toUpperCase());
            holder.tvSize.setText(holder.adapterItem.getSize().toUpperCase());
            holder.tvSeal.setText(holder.adapterItem.getSeal().toUpperCase());
        }
    }

}
