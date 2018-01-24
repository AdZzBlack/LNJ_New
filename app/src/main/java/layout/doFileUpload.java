package layout;

import android.os.AsyncTask;
import android.util.Log;

import com.inspira.lnj.GlobalVar;
import com.inspira.lnj.IndexInternal;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by ADI on 4/4/2017.
 */

public class doFileUpload extends AsyncTask<String, Void, Void> {

    public static GlobalVar global;
    private AsyncListener listener;
    private int urltype;

    public doFileUpload(AsyncListener _listener, int _urltype)
    {
        this.listener = _listener;
        this.urltype = _urltype;
    }

    @Override
    protected Void doInBackground(String... params) {
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        DataInputStream inStream = null;
        BufferedReader bufferedReader;
        String existingFileName = params[0];
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        String responseFromServer = "";
        String urlString = IndexInternal.global.getUploadURL(urltype);
        Log.d("tes", urlString);
        try {

            //------------------ CLIENT REQUEST
            FileInputStream fileInputStream = new FileInputStream(new File(existingFileName));
            // open a URL connection to the Servlet
            URL url = new URL(urlString);
            // Open a HTTP connection to the URL
            conn = (HttpURLConnection) url.openConnection();
            // Allow Inputs
            conn.setDoInput(true);
            // Allow Outputs
            conn.setDoOutput(true);
            // Don't use a cached copy.
            conn.setUseCaches(false);
            // Use a post method.
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            dos = new DataOutputStream(conn.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + existingFileName + "\"" + lineEnd);
            dos.writeBytes(lineEnd);
            // create a buffer of maximum size
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];
            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {

                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            }

            // send multipart form data necesssary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            // close streams
            Log.e("Debug", "File is written");
            fileInputStream.close();
            dos.flush();
            dos.close();

        } catch (MalformedURLException ex) {
            Log.e("Debug", "error: " + ex.getMessage(), ex);
        } catch (IOException ioe) {
            Log.e("Debug", "error: " + ioe.getMessage(), ioe);
        }

        //------------------ read the SERVER RESPONSE
        try {

            //inStream = new DataInputStream(conn.getInputStream());
            bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String str;

            //remarked by Tonny @23-Jan-2018 inStream.readLine() is deprecated
//            while ((str = inStream.readLine()) != null) {
//                if(!str.contains("error"))
//                {
//                    listener.postTaskMethod();
//                }
//                Log.e("Debug", "Server Response " + str);
//            }
            while ((str = bufferedReader.readLine()) != null) {
                if(!str.contains("error"))
                {
                    listener.postTaskMethod();
                }
                Log.e("Debug", "Server Response " + str);
            }

            inStream.close();

        } catch (IOException ioex) {
            Log.e("Debug", "error: " + ioex.getMessage(), ioex);
        }

        return null;
    }
}