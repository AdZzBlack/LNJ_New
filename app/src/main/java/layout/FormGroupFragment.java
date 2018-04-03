package layout;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.inspira.lnj.BackgroundTask;
import com.inspira.lnj.GlobalVar;
import com.inspira.lnj.LibInspira;
import com.inspira.lnj.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.inspira.lnj.IndexInternal.global;
import static com.inspira.lnj.IndexInternal.jsonObject;

/**
 * Created by shoma on 06/09/17.
 */

public class FormGroupFragment extends Fragment implements View.OnClickListener {
    private Button btnSave, btnInvite;
    private EditText etName, etNames;
    private String registeredUsers;
    private String tempTextUsers;
    private String[] selectedGroup;
    private String actionUrl;
    private String notif;
    private Boolean isCreateNew;  //added by Tonny @03-Apr-2018

    //remarked by Tonny @03-Apr-2018  tidak terpakai
//    private String status;
    private DeleteGroup deleteGroup;
    private String nomorgroup, namagroup;

    public FormGroupFragment() {
        // Required empty public constructor
    }

    //added by Tonny @02-Apr-2018 untuk mengganti layout menu pada form ini
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //super.onCreateOptionsMenu(menu, inflater);
        if(!isCreateNew) {
            inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.group_menu, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete) {  //added by Tonny @30-Jul-2017
            //added by Tonny @02-Apr-2018  untuk hapus group chat
            LibInspira.alertbox("Delete Group " + namagroup, "Are you sure want to delete this group? ", getActivity(),
                new Runnable() {
                    @Override
                    public void run() {
                        deleteGroup = new DeleteGroup();
                        String actionUrl = "Group/deleteGroup/";
                        deleteGroup.execute(actionUrl);
                    }
                }
                , new Runnable() {
                    @Override
                    public void run() {
                        //do nothing
                    }
                });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tempTextUsers = "";
        actionUrl = "Group/newGroup/";
        notif = "Group Created";
        setHasOptionsMenu(true);  //added by Tonny @02-Apr-2018 supaya onCreateOptionsMenu dijalankan
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_form_group, container, false);
        isCreateNew = this.getArguments().getBoolean("isCreateNew");  //added by Tonny @03-Apr-2018
        if(isCreateNew) {
            getActivity().setTitle("New Group");
        }else{
            getActivity().setTitle("Edit Group");
        }
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
        btnSave = getView().findViewById(R.id.btnSave);
        etName = getView().findViewById(R.id.etGroupName);
        etNames = getView().findViewById(R.id.etNames);
        btnInvite = getView().findViewById(R.id.btnInvite);

        btnSave.setOnClickListener(this);
        btnInvite.setOnClickListener(this);

        selectedGroup = LibInspira.getShared(global.datapreferences, global.data.selectedGroup, "").split("~");
        if (selectedGroup.length == 2) {
            nomorgroup = selectedGroup[0];
            namagroup = selectedGroup[1];
//            actionUrl = "Group/updateGroup/";  //remarked by Tonny @02-Apr-2018
//            etName.setText(selectedGroup[1]);  //remarked by Tonny @02-Apr-2018
            etName.setText(namagroup);
            notif = "Group Updated";
        }

        refreshList();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshList();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        view.startAnimation(GlobalVar.buttoneffect);

        switch (id) {
            case R.id.btnSave:
                if (etName.getText().toString().equals(""))
                    LibInspira.showLongToast(getContext(), "Please fill in Group Name");
                else if(isCreateNew) {  //modified by Tonny @03-Apr-2018
                    actionUrl = "Group/newGroup/";
                    new group().execute(actionUrl);
                }else{
                    actionUrl = "Group/updateGroup/";
                    new group().execute(actionUrl);
                }
                break;
            case R.id.btnInvite:
                LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new ChooseUserFragment());
                break;
        }
    }

    private void refreshList()
    {
        String data = LibInspira.getShared(global.datapreferences, global.data.selectedUsers, "");
        String[] pieces = data.trim().split("\\|");
        if(pieces.length==1 && pieces[0].equals("")) {
            etNames.setText("-");
        } else {
            tempTextUsers = "";
            registeredUsers = "";
            for(int i=0 ; i < pieces.length ; i++){
                if(!pieces[i].equals(""))
                {
                    String[] parts = pieces[i].trim().split("\\~");

                    String nomor = parts[0];
                    String nama = parts[1];

                    registeredUsers += nomor + ((i == pieces.length - 1) ? "" : "|");
                    tempTextUsers += (i+1) + ". " + nama + "\n";
                }
            }
            etNames.setText(tempTextUsers);
        }
    }

    private class group extends AsyncTask<String, Void, String> {
        JSONObject jsonObject;
        @Override
        protected String doInBackground(String... urls) {
            try {
                jsonObject = new JSONObject();
                jsonObject.put("creator", LibInspira.getShared(global.userpreferences, global.user.nomor, ""));
                if (selectedGroup.length == 2 && !isCreateNew)
                    jsonObject.put("nomor", selectedGroup[0]);
                jsonObject.put("nama", etName.getText());
//                jsonObject.put("status", status);
                jsonObject.put("users", registeredUsers);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return LibInspira.executePost(getContext(), urls[0], jsonObject);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("resultQuery", result);
            LibInspira.showLongToast(getContext(), notif);
            LibInspira.BackFragment(getActivity().getSupportFragmentManager());
            BackgroundTask.mSocket.emit("loadAllRoom",LibInspira.getShared(global.userpreferences, global.user.nomor, ""));
        }
    }

    //added by Tonny @02-Apr-2018 class yg dijalankan untuk menghapus group
    private class DeleteGroup extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            jsonObject = new JSONObject();
            try {
                jsonObject.put("nomorgroup", nomorgroup);
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
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    Log.d("jsonarray length: ", Integer.toString(jsonarray.length()));
                    for (int i = jsonarray.length() - 1; i >= 0; i--) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if (!obj.has("query")) {
                            LibInspira.showLongToast(getContext(), obj.getString("message"));
                        } else {
                            Log.d("Error: ", obj.getString("query"));
                            LibInspira.showShortToast(getContext(), obj.getString("message"));
                        }
                    }
                    LibInspira.BackFragment(getFragmentManager());
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }
}
