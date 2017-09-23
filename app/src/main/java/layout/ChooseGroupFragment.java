package layout;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
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

import com.inspira.gms.GlobalVar;
import com.inspira.gms.LibInspira;
import com.inspira.gms.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.inspira.gms.IndexInternal.global;
import static com.inspira.gms.IndexInternal.jsonObject;

/**
 * Created by shoma on 02/09/17.
 */

public class ChooseGroupFragment extends Fragment implements View.OnClickListener {
    private EditText etSearch;
    private ImageButton ibtnSearch;
    private FloatingActionButton fab;

    private TextView tvInformation, tvNoData;
    private ListView lvSearch;
    private ItemListAdapter itemadapter;
    private ArrayList<ItemAdapter> list;

    public ChooseGroupFragment() {
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
        getActivity().setTitle("Browse Groups");
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
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

        itemadapter = new ItemListAdapter(getActivity(), R.layout.list_item, new ArrayList<ItemAdapter>());
        itemadapter.clear();
        lvSearch = (ListView) getView().findViewById(R.id.lvChoose);
        lvSearch.setAdapter(itemadapter);

        if(LibInspira.getShared(global.userpreferences, global.user.role_creategroup, "").equals("1") && LibInspira.getShared(global.sharedpreferences, global.shared.position, "").equals("Conversation"))
        {
            fab = (FloatingActionButton) getView().findViewById(R.id.fab);
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(this);
            LibInspira.setShared(global.datapreferences, global.data.selectedUsers, "");
        }

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

        String actionUrl = "Group/getGroups/";
        new getData().execute( actionUrl );
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
        else if(id==R.id.fab)
        {
            LibInspira.setShared(global.sharedpreferences, global.shared.position, "New Conversation");
            LibInspira.setShared(global.datapreferences, global.data.selectedUsers, "");
            LibInspira.setShared(global.datapreferences, global.data.selectedGroup, "");
            LibInspira.ReplaceFragment(getActivity().getSupportFragmentManager(), R.id.fragment_container, new FormGroupFragment());  //added by Shodiq @08-Sep-2017
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
                if(LibInspira.contains(list.get(ctr).getNama(),etSearch.getText().toString() ))
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

        String data = LibInspira.getShared(global.datapreferences, global.data.groups, "");

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
                    String[] parts = pieces[i].trim().split("\\~");

                    String nomor = parts[0];
                    String nama = parts[1];

                    if(nomor.equals("null")) nomor = "";
                    if(nama.equals("null")) nama = "";

                    ItemAdapter dataItem = new ItemAdapter();
                    dataItem.setNomor(nomor);
                    dataItem.setNama(nama);
                    list.add(dataItem);

                    itemadapter.add(dataItem);
                    itemadapter.notifyDataSetChanged();
                }
            }
        }
    }

    private class getData extends AsyncTask<String, Void, String> {
        private boolean editMode = false;
        private String nomor;

        protected void setEditMode(String nomor) {
            editMode = true;
            this.nomor = nomor;
        }

        @Override
        protected String doInBackground(String... urls) {
            jsonObject = new JSONObject();
            try {
                if (editMode)
                    jsonObject.put("nomor", nomor);
                else
                    jsonObject.put("user", LibInspira.getShared(global.userpreferences, global.user.nomor_android, ""));
            } catch (JSONException e) {
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
                    Log.d("jsonarray length: ", Integer.toString(jsonarray.length()));
                    for (int i = jsonarray.length() - 1; i >= 0; i--) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if (!obj.has("query")) {
                            String nomor = (obj.getString("nomor"));
                            String nama = (obj.getString("nama"));

                            if (nomor.equals("")) nomor = "null";
                            if (nama.equals("")) nama = "null";

                            tempData = tempData + nomor + "~" + nama + "|";
                        } else {
                            Log.d("FAILED: ", obj.getString("query"));
                        }

                    }
                    Log.d("tempData: ", tempData);
                    if (editMode) {
                        LibInspira.setShared(
                                global.datapreferences,
                                global.data.selectedUsers,
                                tempData
                        );
                        LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new FormGroupFragment());
                    }
                    else if(!tempData.equals(LibInspira.getShared(global.datapreferences, global.data.groups, "")))
                    {
                        LibInspira.setShared(
                                global.datapreferences,
                                global.data.groups,
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
        private String nama;

        public ItemAdapter() {}

        public String getNomor() {return nomor;}
        public void setNomor(String _param) {this.nomor = _param;}

        public String getNama() {return nama;}
        public void setNama(String _param) {this.nama = _param;}
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
        public View getView(final int position, View convertView, ViewGroup parent) {
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

            row.setTag(holder);
            setupItem(holder);

            final Holder finalHolder = holder;
            if (LibInspira.getShared(global.sharedpreferences, global.shared.position, "").equals("Conversation")) {
                row.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        String actionUrl = "Group/getDataGroup/";
                        getData getdata = new getData();
                        getdata.setEditMode(finalHolder.adapterItem.getNomor());
                        getdata.execute(actionUrl);
                        LibInspira.setShared(global.datapreferences, global.data.selectedGroup, finalHolder.adapterItem.getNomor() + "~" + finalHolder.adapterItem.getNama());

                        return false;
                    }
                });
            }
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.startAnimation(GlobalVar.listeffect);
                    if(LibInspira.getShared(global.sharedpreferences, global.shared.position, "").equals("schedule")) {
                        LibInspira.setShared(global.schedulepreferences, global.schedule.groupIDsch, finalHolder.adapterItem.getNomor());
                        LibInspira.setShared(global.schedulepreferences, global.schedule.groupsch, finalHolder.adapterItem.getNama());
                        LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new SummaryScheduleFragment());
                    }
                }
            });

            return row;
        }

        private void setupItem(final ChooseGroupFragment.ItemListAdapter.Holder holder) {
            holder.tvNama.setText(holder.adapterItem.getNama().toUpperCase());
        }
    }
}
