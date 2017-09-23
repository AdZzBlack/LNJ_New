package layout;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.inspira.gms.GlobalVar;
import com.inspira.gms.LibInspira;
import com.inspira.gms.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static com.inspira.gms.IndexInternal.global;

/**
 * Created by shoma on 06/09/17.
 */

public class FormGroupFragment extends Fragment implements View.OnClickListener {
    private TextView tvStatus;
    private Button btnSave, btnInvite;
    private Switch switchStatus;
    private EditText etName, etNames;
    private String registeredUsers;
    private String tempTextUsers;
    private String[] selectedGroup;
    private String actionUrl;
    private String notif;

    public FormGroupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tempTextUsers = "";
        actionUrl = "Group/newGroup/";
        notif = "Group Created";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_form_group, container, false);
        getActivity().setTitle("New Group");
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
        tvStatus = (TextView) getView().findViewById(R.id.tvStatus);
        btnSave = (Button) getView().findViewById(R.id.btnSave);
        switchStatus = (Switch) getView().findViewById(R.id.statusSwitch);
        etName = (EditText) getView().findViewById(R.id.tvGroupName);
        etNames = (EditText) getView().findViewById(R.id.etNames);
        btnInvite = (Button) getView().findViewById(R.id.btnInvite);

        btnSave.setOnClickListener(this);
        btnInvite.setOnClickListener(this);
        switchStatus.setOnClickListener(this);

        selectedGroup = LibInspira.getShared(global.datapreferences, global.data.selectedGroup, "").split("~");
        if (selectedGroup.length == 2) {
            actionUrl = "Group/updateGroup/";
            etName.setText(selectedGroup[1]);
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
                    LibInspira.ShowLongToast(getContext(), "Please fill in Group Name");
                else {
                    new group().execute(actionUrl);
                }
                break;
            case R.id.btnInvite:
                LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new ChooseUserFragment());
                break;
            case R.id.statusSwitch:
                tvStatus.setText((switchStatus.isChecked()) ? "Active" : "Not Active");
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
                jsonObject.put("creator", LibInspira.getShared(global.userpreferences, global.user.nomor_android, ""));
                if (selectedGroup.length == 2)
                    jsonObject.put("nomor", selectedGroup[0]);
                jsonObject.put("nama", etName.getText());
                jsonObject.put("status", switchStatus.isChecked());
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
            LibInspira.ShowLongToast(getContext(), notif);
            LibInspira.BackFragment(getActivity().getSupportFragmentManager());
        }
    }
}
