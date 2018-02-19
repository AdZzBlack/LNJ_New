/******************************************************************************
    Author           : Tonny
    Description      : choose driver list
    History          :

******************************************************************************/
package layout;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.inspira.lnj.LibInspira;
import com.inspira.lnj.R;

import java.util.List;

import static com.inspira.lnj.IndexInternal.global;

public class ChooseDriverFragment extends ChooseUserFragment implements View.OnClickListener{
    public ChooseDriverFragment() {
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
        getActivity().setTitle("Choose Driver");
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
        LibInspira.setShared(global.datapreferences, global.data.userlist, "");
        setActionUrl("Master/getDriver/");
        super.onActivityCreated(bundle);
    }

    private class ItemAdapter {

        private String nomor;
        private String nama;
        private String kode;
        private String cantracked;
        private Boolean isChoosen;

        public ItemAdapter() {}

        public String getNomor() {return nomor;}
        public void setNomor(String _param) {this.nomor = _param;}

        public String getNama() {return nama;}
        public void setNama(String _param) {this.nama = _param;}

        public String getKode() {return kode;}
        public void setKode(String _param) {this.kode = _param;}

        public String getCantracked() {return cantracked;}
        public void setCantracked(String _param) {this.cantracked = _param;}

        public Boolean getIsChoosen() {return isChoosen;}
        public void setIsChoosen(Boolean _param) {this.isChoosen = _param;}
    }

    private class ItemListAdapter extends ArrayAdapter<ItemAdapter> {

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
            setupItem(holder, row);

            final Holder finalHolder = holder;
            final View finalRow = row;
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(LibInspira.getShared(global.sharedpreferences, global.shared.position, "").equals("document"))
                    {
                        LibInspira.setShared(global.temppreferences, global.temp.selected_nomor_driver, finalHolder.adapterItem.getNomor());
                        LibInspira.setShared(global.temppreferences, global.temp.selected_kode_driver, finalHolder.adapterItem.getKode());
                        LibInspira.setShared(global.temppreferences, global.temp.selected_nama_driver, finalHolder.adapterItem.getNama());
                        LibInspira.BackFragment(getFragmentManager());
                    }
                }
            });

            return row;
        }

        private void setupItem(final Holder holder, final View row) {
            holder.tvNama.setText(holder.adapterItem.getNama().toUpperCase());
            if(holder.adapterItem.isChoosen)
            {
                row.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            }
            else
            {
                row.setBackgroundColor(getResources().getColor(R.color.colorBackground));
            }
        }
    }
}
