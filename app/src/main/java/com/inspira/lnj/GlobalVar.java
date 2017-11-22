package com.inspira.lnj;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.animation.AlphaAnimation;

/**
 * Created by Tonny on 7/24/2017.
 */

public class GlobalVar {
    public static SharedPreferences sharedpreferences;
    public static SharedPreferences temppreferences;  //added by ADI @19-Aug-2017 //buat data-data yang cuma sementara  //boleh di clear kapan aja
    public static SharedPreferences userpreferences;
    public static SharedPreferences rolepreferences;
    public static SharedPreferences notifpreferences;
    public static SharedPreferences datapreferences;
    public static SharedPreferences settingpreferences;  //added by Tonny @03-Aug-2017
    public static SharedPreferences tempmapspreferences;  //added by Tonny @16-Nov-2017

    public static User user;
    public static Sales sales;  //added by Tonny @01-Aug-2017
    public static Data data;
    public static Shared shared;
    public static Settings settings;  //added by Tonny @03-Aug-2017
    public static Temp temp; //added by ADI @20-Aug-2017
    public static TempMaps tempMaps;  //added by Tonny @16-Nov-2017

    public static AlphaAnimation buttoneffect = new AlphaAnimation(1F, 0.8F);
    public static AlphaAnimation listeffect = new AlphaAnimation(1F, 0.5F);

    public static String webserviceURL = "/wsLNJ/lnj/index.php/api/";

    public static String folder = "/LNJ"; //added by ADI @01-Sep-2017
    public static String folderPDF = folder + "/PDF"; //added by ADI @01-Sep-2017

        public GlobalVar(Context context)
        {
            sharedpreferences = context.getSharedPreferences("global", Context.MODE_PRIVATE);
            temppreferences = context.getSharedPreferences("temp", Context.MODE_PRIVATE); //added by ADI @20-Aug-2017
            userpreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);
            rolepreferences = context.getSharedPreferences("role", Context.MODE_PRIVATE);
            notifpreferences = context.getSharedPreferences("notif", Context.MODE_PRIVATE);
            datapreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);
            settingpreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);  //added by Tonny @03-Aug-2017
            tempmapspreferences = context.getSharedPreferences("maps", Context.MODE_PRIVATE);

            data = new Data();
            user = new User();
            sales = new Sales();  //added by Tonny @01-Aug-2017
            shared = new Shared();
            settings = new Settings();  //added by Tonny @03-Aug-2017
            temp = new Temp(); //added by ADI @20-Aug-2017
            tempMaps = new TempMaps();  //added by Tonny @16-Nov-2017
        }

        public static void clearDataUser()
        {
            LibInspira.clearShared(userpreferences);
            LibInspira.clearShared(rolepreferences);
            LibInspira.clearShared(notifpreferences);
            LibInspira.clearShared(settingpreferences); //added by Tonny @03-Aug-2017
        }

        public class Shared
        {
            public String server = "server";
            public String position = "position";  //added by Tonny @07-Aug-2017
        }

        //untuk shared preferences yang boleh di clean tiap saat
        public class Temp
        {
            public String selected_nomor_user = "selected_nomor_user";
            public String selected_kode_user = "selected_kode_user";
            public String selected_nama_user = "selected_nama_user";

            //added by Tonny @01-Nov-2017 untuk menampung data thorderjual yg dipilih
            public String selected_nomor_doc = "selected_nomor_doc";
            public String selected_kode_doc = "selected_kode_doc";
            public String selected_nomormhadmin = "selected_nomormhadmin";
            public String selected_tanggal = "selected_nomormhadmin";
            public String selected_namamhadmin = "selected_namamhadmin";

            public String nomor_doc = "nomor_doc";
            public String kode_doc = "kode_doc";

        }

        public class User
        {
            public String nomor = "nomor";
            public String password = "password";
            public String nomor_pegawai = "nomor_pegawai";
            public String kode_pegawai = "kode_pegawai";
            public String nama = "nama";
            public String role = "role";
            public String hash = "hash";
            public String token = "token";
            public String cabang = "cabang";
            public String namacabang = "namacabang";
            public String role_isdriver = "role_isdriver"; //role untuk mengetahui user termasuk driver atau tidak (0/1)
            public String role_qrcodereader = "role_qrcodereader"; //role untuk mengetahui user dapat melakukan qrcodereader atau tidak (0/1)
            public String role_checkin = "role_checkin"; //role apakah user dapat melakukan checkin atau tidak (0/1)
            public String role_cantracked = "role_cantracked"; //role apakah user dapat ditrack atau tidak (0/1)

            public String checkin_nomorth = "checkin_nomorth"; //added by ADI @05-Oct-2017
            public String checkin_kodecontainer = "checkin_kodecontainer"; //added by ADI @05-Oct-2017
        }

        public class Data
        {
            public String user = "user"; // nomor~nama~location~hp
            public String userlist = "userlist"; // nomor~kode~nama
            public String doclist = "doclist"; //untuk menampung data document(thorderjual) yg diberikan pada user yg login  //nomor~kode~nomormhadmin~tanggal~nama
        }

        public class Sales  //added by Tonny @01-Aug-2017
        {
            public String target = "target";
            public String omzet = "omzet";
        }

        public class Settings  //added by Tonny @03-Aug-2017
        {

        }

        public class TempMaps //added by Tonny @16-Nov-2017
        {
            public String event = "event";
            public String placename = "placename";
            public String radius = "radius";
            public String latitude = "latitude";
            public String longitude = "longitude";
            public String duration = "duration";
            public String notes = "notes";
        }
}
