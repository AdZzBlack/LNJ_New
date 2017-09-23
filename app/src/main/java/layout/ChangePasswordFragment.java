/******************************************************************************
    Author           : Tonny
    Description      : Untuk change password
    History          :

******************************************************************************/
package layout;

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
import android.widget.EditText;
import android.widget.Toast;

import com.inspira.gms.LibInspira;
import com.inspira.gms.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.inspira.gms.IndexInternal.global;
import static com.inspira.gms.IndexInternal.jsonObject;

public class ChangePasswordFragment extends Fragment implements View.OnClickListener{
    private EditText edtOldPassword, edtNewPassword, edtConfirmation;
    private Button btnSave;

    public ChangePasswordFragment() {
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
        View v = inflater.inflate(R.layout.fragment_change_password, container, false);
        getActivity().setTitle("Change Password");
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        edtOldPassword = (EditText) getView().findViewById(R.id.edtOldPassword);
        edtNewPassword = (EditText) getView().findViewById(R.id.edtPassword);
        edtConfirmation = (EditText) getView().findViewById(R.id.edtConfirmPassword);

        btnSave = (Button) getView().findViewById(R.id.btnSave);
        btnSave.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        String currentPassword = LibInspira.getShared(global.userpreferences, global.user.password, "");
        String oldPassword = edtOldPassword.getText().toString();
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        m.reset();
        m.update(oldPassword.getBytes());
        byte[] digest = m.digest();
        //BigInteger bigInt = new BigInteger(1,digest);
        //String hashtext = digest.toString();
        StringBuffer sb = new StringBuffer();
        for (byte b : digest) {
            sb.append(String.format("%02x", b & 0xff));
        }

        if(id == R.id.btnSave){
            //cek validasi data
            if (edtOldPassword.getText().toString().equals("") || edtNewPassword.getText().toString().equals("") || edtConfirmation.getText().toString().equals(""))
            {
                LibInspira.ShowShortToast(getContext(), "Please fill in all fields");
                return;
            }else if(!sb.toString().equals(currentPassword)){
                Log.d("current pass", currentPassword);
                Log.d("old pass", sb.toString());
                LibInspira.ShowShortToast(getContext(), "The old password is invalid");
                return;
            }else if(edtOldPassword.getText().toString().equals(edtNewPassword.getText().toString())){
                LibInspira.ShowShortToast(getContext(), "You entered the old password, change password failed");
                return;
            }else if(!edtNewPassword.getText().toString().equals(edtConfirmation.getText().toString())) {
                LibInspira.ShowShortToast(getContext(), "Password doesn't match the confirm password");
                return;
            }else if(sb.toString().equals(currentPassword)){  //jika semuanya valid
                String actionUrl = "Profile/changePassword/";
                new changePassword().execute(actionUrl);
            }
        }
    }

    private class changePassword extends AsyncTask<String, Void, String> {
        String oldpass = edtOldPassword.getText().toString();
        String newpass = edtNewPassword.getText().toString();

        @Override
        protected String doInBackground(String... urls) {
            try {
                jsonObject = new JSONObject();
                jsonObject.put("oldpass", oldpass);
                jsonObject.put("newpass", newpass);
                jsonObject.put("user_id", LibInspira.getShared(global.userpreferences, global.user.nomor, ""));
                Log.d("user_id: ", LibInspira.getShared(global.userpreferences, global.user.nomor, ""));
                Log.d("oldpass: ", oldpass);
                Log.d("newpass: ", newpass);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return LibInspira.executePost(getContext(), urls[0], jsonObject);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONArray jsonarray = new JSONArray(result);
                for (int i = jsonarray.length() - 1; i >= 0; i--) {
                    JSONObject obj = jsonarray.getJSONObject(i);

                    String success = obj.getString("success");
                    String pesan = obj.getString("pesan");
                    if(success.equals("true") && pesan.equals("1")){
                        LibInspira.ShowLongToast(getContext(), "Password Changed");
                        LibInspira.ReplaceFragment(getActivity().getSupportFragmentManager(), R.id.fragment_container, new DashboardInternalFragment());
                    }
                    else {
                        Toast.makeText(getContext(), "Change Password Failed", Toast.LENGTH_LONG).show();
                    }
                }
                LibInspira.hideLoading();
            }catch(Exception e)
            {
                e.printStackTrace();
                Toast.makeText(getContext(), result, Toast.LENGTH_LONG).show();
                LibInspira.hideLoading();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LibInspira.showLoading(getContext(), "Change Password", "Loading");
        }
    }
}
