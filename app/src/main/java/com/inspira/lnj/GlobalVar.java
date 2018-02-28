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
    public static SharedPreferences chatPreferences;

    public static User user;
    public static Sales sales;  //added by Tonny @01-Aug-2017
    public static Data data;
    public static Shared shared;
    public static Settings settings;  //added by Tonny @03-Aug-2017
    public static Temp temp; //added by ADI @20-Aug-2017
    public static TempMaps tempMaps;  //added by Tonny @16-Nov-2017
    public static Chat chat;

    public static AlphaAnimation buttoneffect = new AlphaAnimation(1F, 0.8F);
    public static AlphaAnimation listeffect = new AlphaAnimation(1F, 0.5F);

    public static String webserviceURL = "/wsLNJ/lnj/index.php/api/";

    public static String folder = "/LNJ"; //added by ADI @01-Sep-2017
    public static String folderPDF = folder + "/PDF"; //added by ADI @01-Sep-2017

    public static final String LOCAL_SERVER_URL = "http://192.168.8.101";
    public static final String CHAT_SERVER_URL = "http://vpn.inspiraworld.com"+":3001";
    public static final String URL_SERVER_PICTURE_PATH = "http://vpn.inspiraworld.com"+":3001/";

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
        chatPreferences  = context.getSharedPreferences("chat", Context.MODE_PRIVATE);

        data = new Data();
        user = new User();
        sales = new Sales();  //added by Tonny @01-Aug-2017
        shared = new Shared();
        settings = new Settings();  //added by Tonny @03-Aug-2017
        temp = new Temp(); //added by ADI @20-Aug-2017
        tempMaps = new TempMaps();  //added by Tonny @16-Nov-2017
        chat = new Chat();
    }

    public static void clearDataUser()
    {
        LibInspira.clearShared(userpreferences);
        LibInspira.clearShared(rolepreferences);
        LibInspira.clearShared(notifpreferences);
        LibInspira.clearShared(settingpreferences); //added by Tonny @03-Aug-2017
        LibInspira.clearShared(datapreferences); //added by Tonny @02-Jan-2018
    }

    public static String getUploadURL(int _urltype)
    {
        String result;
        String url = LibInspira.getShared(sharedpreferences, shared.server, "");
        if(_urltype==1) result = "https://" + url + "/uploads/lnj/CONTAINER%20EMPTY/upload.php";
        else if(_urltype==2) result = "https://" + url + "/uploads/lnj/CONTAINER%20SEALED/upload.php";
        else if(_urltype==3) result = "https://" + url + "/uploads/lnj/CONTAINER%20SEALED%20PORT/upload.php";
        else if(_urltype==4) result = "https://" + url + "/uploads/lnj/CONTAINER%20OTHERS/upload.php";  //modified by Tonny @23-Jan-2018 CONTAINER SEALED OTHERS --> CONTAINER OTHERS
        else  result = "";

        return result;
    }

    public class Shared
    {
        public String server = "server";
        public String position = "position";  //added by Tonny @07-Aug-2017
    }

    //untuk shared preferences yang boleh di clean tiap saat
    public class Temp
    {
        public String temp = "temp";

        public String selected_nomor_user = "selected_nomor_user";
        public String selected_kode_user = "selected_kode_user";
        public String selected_nama_user = "selected_nama_user";
        public String selected_count = "selected_count";

        //added by Tonny @16-Feb-2018 untuk choose driver
        public String selected_nomor_driver = "selected_nomor_user";
        public String selected_kode_driver = "selected_kode_user";
        public String selected_nama_driver = "selected_nama_user";

        //added by Tonny @01-Nov-2017 untuk menampung data thorderjual yg dipilih
        public String selected_nomor_doc = "selected_nomor_doc";
        public String selected_kode_doc = "selected_kode_doc";
        public String selected_nomormhadmin = "selected_nomormhadmin";
        public String selected_nomormhadmin_from = "selected_nomormhadmin_from";  //added by Tonny @30-Jan-2018
        public String selected_nomortlaporan_ref = "selected_nomortlaporan_ref";  //added by Tonny @30-Jan-2018
        public String selected_tanggal = "selected_nomormhadmin";
        public String selected_namamhadmin = "selected_namamhadmin";
        public String delete_reason = "delete_reason";  //added by Tonny @19-Feb-2018

        //added by ADI @15-Jan-2018 untuk menampung data container loading yg dipilih
        public String selected_job_nomor = "selected_job_nomor";
        public String selected_job_kode = "selected_job_kode";
        public String selected_job_stuffingdate = "selected_container_stuffingdate";
        public String selected_job_invoice = "selected_container_invoice";
        public String selected_job_pol = "selected_container_pol";
        public String selected_job_pod = "selected_container_pod";

        public String selected_container_nomor = "selected_container_nomor";
        public String selected_container_kode = "selected_container_kode";
        public String selected_container_size = "selected_container_size";
        public String selected_container_type = "selected_container_type";
        public String selected_container_seal = "selected_container_seal";

        public String photo_pathraw_empty_container = "photo_pathraw_empty_container";
        public String photo_path_empty_container = "photo_path_empty_container";
        public String photo_photoname_empty_container = "photo_photoname_empty_container";
        public String photo_pathraw_sealed_container = "photo_pathraw_sealed_container";
        public String photo_path_sealed_container = "photo_path_sealed_container";
        public String photo_photoname_sealed_container = "photo_photoname_sealed_container";
        public String photo_pathraw_sealed_condition = "Photo_pathraw_sealed_condition";
        public String photo_path_sealed_condition = "photo_path_sealed_condition";
        public String photo_photoname_sealed_condition = "photo_photoname_sealed_condition";
        public String photo_pathraw_other_picture = "Photo_pathraw_other_picture";
        public String photo_path_other_picture = "photo_path_other_picture";
        public String photo_photoname_other_picture = "photo_photoname_other_picture";

        public String nomor_doc = "nomor_doc";
        public String kode_doc = "kode_doc";

        public String report_startdate = "report_startdate";
        public String report_enddate = "report_enddate";
        public String report_job = "report_job";
        public String report_user = "report_user";
        public String report_user_name = "report_user_name";
        public String report_user_from_name = "report_user_from_name";
        public String report_user_from = "report_user_from";
        public String report_user_to_name = "report_user_to_name";
        public String report_user_to = "report_user_to";
        public String report_doc_action_index = "report_user_action_index";
        public String report_doc_action = "report_user_action";

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
        public String role_cantracking = "role_cantracking"; //role apakah user dapat menggunakan fitur track atau tidak (0/1)

        //belum ambil dari database!!!!!!!
        public String role_creategroup = "role_creategroup";

        public String checkin_nomorthsuratjalan = "checkin_nomorthsuratjalan"; //added by ADI @05-Oct-2017
        public String checkin_nomortdsuratjalan = "checkin_nomortdsuratjalan"; //added by Tonny @04-Dec-2017
        public String checkin_kodesuratjalan = "checkin_kodesuratjalan"; //added by Tonny @16-Dec-2017
        public String checkin_kodecontainer = "checkin_kodecontainer"; //added by ADI @05-Oct-2017
    }

    public class Data
    {
        public String temp = "temp";
        public String user = "user"; // nomor~nama~location~hp
        public String contact = "contact";
        public String userlist = "userlist"; // nomor~kode~nama~cantracked
        public String doclist = "doclist"; //untuk menampung data document(thorderjual) yg diberikan pada user yg login  //nomor~kode~nomormhadmin~tanggal~nama
        public String deliveryorderlist = "deliveryorderlist"; //nomor1|nomor2 //untuk menampung list nomor surat jalan yang telah discan untuk ditampilkan di ChooseSuratJalan
        public String job = "job"; //nomor~kode~stuffingdate~invoice~pol~pod
        public String groups = "groups";

        public String selectedUsers = "selectedUsers";
        public String selectedGroup = "selectedGroup";
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
        public String nomor = "nomor";
        public String event = "event";
        public String placename = "placename";
        public String radius = "radius";
        public String latitude = "latitude";
        public String longitude = "longitude";
        public String duration = "duration";
        public String notes = "notes";
        public String mode = "mode";
    }

    public class Chat
    {
        public String chat_room_list = "chat_room_list";
        public String chat_coba = "chat_coba";
        public String chat_to_id = "chat_to_id";

        public String chat_menu_position = "chat_menu_position";

        public String chat_history_room = "chat_history_room";
        public String chat_history_chat = "chat_history_chat";
        public String chat_history_all = "chat_history_all";
    }
}
