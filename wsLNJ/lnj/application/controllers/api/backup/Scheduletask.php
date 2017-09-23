<?php

defined('BASEPATH') OR exit('No direct script access allowed');

// This can be removed if you use __autoload() in config.php OR use Modular Extensions
require APPPATH . '/libraries/REST_Controller.php';

/**
 * This is an example of a few basic event interaction methods you could use
 * all done with a hardcoded array
 *
 * @package         CodeIgniter
 * @subpackage      Rest Server
 * @category        Controller
 * @author          Phil Sturgeon, Chris Kacerguis
 * @license         MIT
 * @link            https://github.com/chriskacerguis/codeigniter-restserver
 */
class Scheduletask extends REST_Controller { 

    function __construct()
    {
        // Construct the parent class
        parent::__construct();

        // Configure limits on our controller methods
        // Ensure you have created the 'limits' table and enabled 'limits' within application/config/rest.php
        $this->methods['event_post']['limit'] = 500000000; // 500 requests per hour per event/key
        // $this->methods['event_delete']['limit'] = 50; // 50 requests per hour per event/key
        $this->methods['event_get']['limit'] = 500000000; // 500 requests per hour per event/key

        header("Access-Control-Allow-Origin: *");
        header("Access-Control-Allow-Methods: GET, POST");
        header("Access-Control-Allow-Headers: Origin, Content-Type, Accept, Authorization");
    }

    function getMaxID($primerykey,$table,$database){
        if($database == "android"){
            $db_android = $this->load->database('db_android', TRUE);
            $db_android->select_max($primerykey);
            $result = $db_android->get($table)->row_array();
            return $result[$primerykey];
        }else{
            $this->db->select_max($primerykey);
            $result = $this->db->get($table)->row_array();
            return $result[$primerykey];
        }
    }

    function clean($string) {
        return preg_replace("/[^[:alnum:][:space:]]/u", '', $string); // Replaces multiple hyphens with single one.
    }

    function ellipsis($string) {
        $cut = 30;
        $out = strlen($string) > $cut ? substr($string,0,$cut)."..." : $string;
        return $out;
    }

    function error($string) {
        return str_replace( array("\t", "\n") , "", $string);
    }

    function getGCMId($user_nomor){
        $query = "  SELECT 
                    a.intNomorMUser, 
                    a.vcGCMId 
                    FROM muser_android a 
                    WHERE a.intStatus > 0 AND (a.vcGCMId <> '' AND a.vcGCMId IS NOT NULL) AND a.intNomorMUser = $user_nomor ";
        return $this->db->query($query)->row()->vcGCMId;
    }

    public function send_gcm($registrationId,$message,$title,$fragment,$nomor,$nama)
    {
        $this->load->library('gcm');

        $this->gcm->setMessage($message);
        $this->gcm->setTitle($title);
        $this->gcm->setFragment($fragment);
        $this->gcm->setNomor($nomor);
        $this->gcm->setNama($nama);

        $this->gcm->addRecepient($registrationId);

        $this->gcm->setData(array(
            'some_key' => 'some_val'
        ));

        $this->gcm->setTtl(500);
        $this->gcm->setTtl(false);

        $this->gcm->setGroup('Test');
        $this->gcm->setGroup(false);

        $this->gcm->send();
    }

	// --- Send Message Group --- //
	function createScheduleTask_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
		$jsonObject = (json_decode($value , true));

        $manager      = (isset($jsonObject["manager_nomor"])  ? $this->clean($jsonObject["manager_nomor"])    : "");
        $bex          = (isset($jsonObject["bex_nomor"])      ? $this->clean($jsonObject["bex_nomor"])        : "");
        $customer     = (isset($jsonObject["customer_nomor"]) ? $this->clean($jsonObject["customer_nomor"])   : "");
        $tipe         = (isset($jsonObject["tipe"])           ? $this->clean($jsonObject["tipe"])             : "");
        $jenisjadwal  = (isset($jsonObject["jenisjadwal"])    ? $this->clean($jsonObject["jenisjadwal"])      : "");
        $tanggal      = (isset($jsonObject["tanggal"])        ? $jsonObject["tanggal"]                        : "");
        $jam          = (isset($jsonObject["jam"])            ? $jsonObject["jam"]                            : "");
		$reminder     = (isset($jsonObject["reminder"])       ? $jsonObject["reminder"]                       : "");
        $keterangan   = (isset($jsonObject["keterangan"])     ? $jsonObject["keterangan"]                     : "");
        $proyek       = (isset($jsonObject["proyek_nomor"])   ? $jsonObject["proyek_nomor"]                   : "0");

        $db_android = $this->load->database('db_android', TRUE);

        $db_android->trans_begin();

        $query = $db_android->insert_string('tjadwal', array(
                                                              'intNomorUserManager' => $manager, 
                                                              'intNomorUserBEX'     => $bex, 
                                                              'intNomorCustomer'    => $customer, 
                                                              'intNomorMProyek'     => $proyek, 
                                                              'vcTipe'              => $tipe, 
                                                              'vcJenisJadwal'       => $jenisjadwal, 
                                                              'dtTanggal'           => $tanggal,
                                                              'tmJam'               => $jam,
															  'intReminder'         => $reminder,
                                                              'txtKeterangan'       => $keterangan
                                                            )
                                        );
        $db_android->query($query);

        if ($db_android->trans_status() === FALSE){
            $db_android->trans_rollback();
            array_push($data['data'], array( 'success' => "false" ));
        }else{
            $db_android->trans_commit();
            array_push($data['data'], array( 'success' => "true" ));

            // START SEND NOTIFICATION
            $vcGCMId = $this->getGCMId($bex);
            if( $vcGCMId != "null" ){
                $jadwal_nomor = $this->getMaxID("intNomor","tjadwal","android");
                $manager_nama = $this->db->query("SELECT vcNama FROM muser where intNomor ='". $manager."'")->row()->vcNama; 
                $this->send_gcm($vcGCMId, $this->ellipsis($keterangan),"Schedule Task From ".$manager_nama,'ScheduleTaskSalesList',$jadwal_nomor,$manager_nama);
            }
        }  

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    // --- GET Message Group --- //
    function getScheduleTask_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $manager_nomor = (isset($jsonObject["manager_nomor"]) ? $this->clean($jsonObject["manager_nomor"])   : "");
        if($manager_nomor != ""){ $manager_nomor = " AND intNomorUserManager = $manager_nomor "; }

        $except_manager_nomor = (isset($jsonObject["except_manager_nomor"]) ? $this->clean($jsonObject["except_manager_nomor"])   : "");
        if($except_manager_nomor != ""){ $except_manager_nomor = " AND intNomorUserManager <> $except_manager_nomor "; }

        $bex_nomor = (isset($jsonObject["bex_nomor"]) ? $this->clean($jsonObject["bex_nomor"])   : "");
        if($bex_nomor != ""){ 
            $bex_nomor = " AND intNomorUserBEX_new = $bex_nomor "; 
        }

        $search = (isset($jsonObject["search"]) ? $this->clean($jsonObject["search"]) : "");
        if($search != ""){ $search = " AND (a.txtKeterangan LIKE '%$search%') "; }

        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

        $db_android = $this->load->database('db_android', TRUE);

        $query = "  SELECT DISTINCT 
                        a.intNomor AS jadwal_nomor,
                        a.intNomorUserManager AS manager_nomor,
                        CASE WHEN a.vcJenisJadwal = 'Group Meeting' THEN a.vcNama 
                        ELSE a.intNomorUserBEX 
                        END AS bex_nomor,
                        a.intNomorCustomer AS customer_nomor,
                        a.intNomorMProyek AS proyek_nomor,
                        a.vcTipe AS tipe,
                        a.vcJenisJadwal AS jenisjadwal,
                        a.dtTanggal AS tanggal,
                        a.tmJam AS jam,
                        a.txtKeterangan AS keterangan,
                        a.intStatusSelesai AS jadwal_selesai
                    FROM (
                        SELECT DISTINCT 
                        a.*, 
                        CASE WHEN a.vcJenisJadwal = 'Group Meeting' THEN c.intNomorMUser 
                        ELSE a.intNomorUserBEX 
                        END AS intNomorUserBEX_new,
                        b.vcNama  
                        FROM tjadwal a 
                        LEFT JOIN tgroup_team b ON a.intNomorUserBEX = b.intNomor AND a.vcJenisJadwal = 'Group Meeting' 
                        LEFT JOIN tdgroup_team c ON a.intNomorUserBEX = c.intNomorTGroupTeam AND a.vcJenisJadwal = 'Group Meeting' 
                        WHERE a.intStatus = 1 AND a.intStatusSelesai = 0 $search 
                        ORDER BY a.dtInsertDate 
                    ) a 
                    WHERE 1 = 1 $bex_nomor $manager_nomor $except_manager_nomor 
                    $limit";
        $result = $db_android->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                $manager_nama   = $this->db->query("SELECT vcNama FROM muser where intNomor ='". $r['manager_nomor']."'")->row()->vcNama;
                $bex_nama       = $this->db->query("SELECT vcNama FROM muser where intNomor ='". $r['bex_nomor']."'")->row()->vcNama;
                if($r["jenisjadwal"] == "Group Meeting"){
                    $bex_nama   = $db_android->query("SELECT vcNama FROM tgroup_team where intNomor ='". $r['bex_nomor']."'")->row()->vcNama;   
                }
                $customer_nama  = $this->db->query("SELECT vcNama FROM mcustomer where intNomor ='". $r['customer_nomor']."'")->row()->vcNama;
                $proyek_nama    = $this->db->query("SELECT vcNama FROM mproyek where intNomor ='". $r['proyek_nomor']."'")->row()->vcNama;
                array_push($data['data'], array(
                                                'jadwal_nomor'   => $r['jadwal_nomor'], 
                                                'manager_nomor'  => $r['manager_nomor'], 
                                                'manager_nama'   => $manager_nama, 
                                                'bex_nomor'      => $r['bex_nomor'], 
                                                'bex_nama'       => $bex_nama, 
                                                'customer_nomor' => $r['customer_nomor'], 
                                                'customer_nama'  => $customer_nama, 
                                                'proyek_nomor'   => $r['proyek_nomor'], 
                                                'proyek_nama'    => $proyek_nama, 
                                                'tipe'           => $r['tipe'], 
                                                'jenisjadwal'    => $r['jenisjadwal'], 
                                                'tanggal'        => $r['tanggal'], 
                                                'jam'            => $r['jam'], 
                                                'keterangan'     => $r['keterangan'], 
                                                'jadwal_selesai' => $r['jadwal_selesai']
                                                )
                );
            }
        }else{      
            array_push($data['data'], array( 'query' => $this->error($query) ));
        }  

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    // --- GET Detail --- //
    function getDetailDataScheduleTask_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $jadwal_nomor = $this->clean($jsonObject["jadwal_nomor"]);
        if($jadwal_nomor != ""){ $jadwal_nomor = " AND intNomor = $jadwal_nomor "; }

        $db_android = $this->load->database('db_android', TRUE);

        $query = "  SELECT 
                    a.intNomor AS jadwal_nomor,
                    a.intNomorUserManager AS manager_nomor,
                    a.intNomorUserBEX AS bex_nomor,
                    a.intNomorCustomer AS customer_nomor,
                    a.vcTipe AS tipe,
                    a.vcJenisJadwal AS jenisjadwal,
                    a.dtTanggal AS tanggal,
                    a.tmJam AS jam,
                    a.txtKeterangan AS keterangan
                    FROM tjadwal a
                    WHERE a.intStatus = 1 $jadwal_nomor
                    ORDER BY a.dtInsertDate";
        $result = $db_android->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                $manager_nama   = $this->db->query("SELECT vcNama FROM muser where intNomor ='". $r['manager_nomor']."'")->row()->vcNama;
                $bex_nama       = $this->db->query("SELECT vcNama FROM muser where intNomor ='". $r['bex_nomor']."'")->row()->vcNama;
                $customer_nama  = $this->db->query("SELECT vcNama FROM mcustomer where intNomor ='". $r['customer_nomor']."'")->row()->vcNama;
                array_push($data['data'], array(
                                                'jadwal_nomor'   => $r['jadwal_nomor'], 
                                                'manager_nomor'  => $r['manager_nomor'], 
                                                'manager_nama'   => $manager_nama, 
                                                'bex_nomor'      => $r['bex_nomor'], 
                                                'bex_nama'       => $bex_nama, 
                                                'customer_nomor' => $r['customer_nomor'], 
                                                'customer_nama'  => $customer_nama, 
                                                'tipe'           => $r['tipe'], 
                                                'jenisjadwal'    => $r['jenisjadwal'], 
                                                'tanggal'        => $r['tanggal'], 
                                                'jam'            => $r['jam'], 
                                                'keterangan'     => $r['keterangan']
                                                )
                );
            }
        }else{      
            array_push($data['data'], array( 'query' => $this->error($query) ));
        }  

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

	// --- Finish Task --- //
    function finishScheduleTask_post(){     

        $data['data'] = array();
		//array_push($data['data'], array( 'success' => "1" ));
		
        $value = file_get_contents('php://input');
		$jsonObject = (json_decode($value , true));

		
        $jadwal_nomor = (isset($jsonObject["jadwal_nomor"])  ? $this->clean($jsonObject["jadwal_nomor"]) : "");
		
		
        $db_android = $this->load->database('db_android', TRUE);
		
        $db_android->trans_begin();

		
        $manager_nomor = $db_android->query("SELECT intNomorUserManager FROM tjadwal where intNomor ='". $jadwal_nomor."'")->row()->intNomorUserManager;
        $bex_nomor     = $db_android->query("SELECT intNomorUserBEX FROM tjadwal where intNomor ='". $jadwal_nomor."'")->row()->intNomorUserBEX;
        $keterangan    = $db_android->query("SELECT txtKeterangan FROM tjadwal where intNomor ='". $jadwal_nomor."'")->row()->txtKeterangan;

        $db_android->query("UPDATE tjadwal SET intStatusSelesai = 1 WHERE intNomor = " . $jadwal_nomor);

        if ($db_android->trans_status() === FALSE){
            $db_android->trans_rollback();
            array_push($data['data'], array( 'success' => "false" ));
        }else{
            $db_android->trans_commit();
            array_push($data['data'], array( 'success' => "true" ));

            // START SEND NOTIFICATION
            $vcGCMId = $this->getGCMId($manager_nomor);
            if( $vcGCMId != "null" ){
                $bex_nama = $this->db->query("SELECT vcNama FROM muser where intNomor ='". $bex_nomor."'")->row()->vcNama; 
                $this->send_gcm($vcGCMId, $this->ellipsis($keterangan),"Schedule Task Finished by ".$bex_nama,'ScheduleTaskSalesList',$jadwal_nomor,$bex_nama);
            }
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }

    }

    // --- Delete Task --- //
    function deleteScheduleTask_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $jadwal_nomor = (isset($jsonObject["jadwal_nomor"])  ? $this->clean($jsonObject["jadwal_nomor"]) : "");

        $db_android = $this->load->database('db_android', TRUE);

        $db_android->trans_begin();

        $db_android->query("UPDATE tjadwal SET intStatus = 0 WHERE intNomor = $jadwal_nomor");

        if ($db_android->trans_status() === FALSE){
            $db_android->trans_rollback();
            array_push($data['data'], array( 'success' => "false" ));
        }else{
            $db_android->trans_commit();
            array_push($data['data'], array( 'success' => "true" ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }

    }


    function createReminderScheduleTask_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $bex = (isset($jsonObject["bex_nomor"]) ? $this->clean($jsonObject["bex_nomor"])   : "");
        $bex_nomor = (isset($jsonObject["bex_nomor"]) ? $this->clean($jsonObject["bex_nomor"])   : "");
        if($bex_nomor != ""){ $bex_nomor = " AND intNomorUserBEX = $bex_nomor "; }

        $db_android = $this->load->database('db_android', TRUE);

        $query = "  SELECT 
                    a.intNomor AS jadwal_nomor,
                    a.intNomorUserManager AS manager_nomor,
                    a.intNomorUserBEX AS bex_nomor,
                    a.intNomorCustomer AS customer_nomor,
                    a.vcTipe AS tipe,
                    a.vcJenisJadwal AS jenisjadwal,
                    a.dtTanggal AS tanggal,
                    a.tmJam AS jam,
                    a.txtKeterangan AS keterangan,
                    a.intStatusSelesai AS jadwal_selesai
                    FROM tjadwal a
                    WHERE a.intStatus = 1 
                    AND a.intStatusSelesai = 0 $bex_nomor 
					AND 
						(
							CONCAT(dtTanggal,' ',tmJam) <= (NOW() + INTERVAL 2 MINUTE)  
							AND 
							CONCAT(dtTanggal,' ',tmJam) >= (NOW() - INTERVAL 1 MINUTE)
						)
					OR
						(
							(CONCAT(dtTanggal,' ',tmJam) - INTERVAL a.intReminder-2 MINUTE) >= NOW()
							AND 
							(CONCAT(dtTanggal,' ',tmJam) - INTERVAL a.intReminder+1 MINUTE) <= NOW()
						)
					OR
						(
							(CONCAT(dtTanggal,' ',tmJam) - INTERVAL 1 DAY + INTERVAL 2 MINUTE) >= NOW()
							AND 
							(CONCAT(dtTanggal,' ',tmJam) - INTERVAL 1 DAY - INTERVAL 1 MINUTE) <= NOW()
						)
                    ORDER BY a.dtInsertDate ";
        $result = $db_android->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                $manager_nama   = $this->db->query("SELECT vcNama FROM muser where intNomor ='". $r['manager_nomor']."'")->row()->vcNama;
                $bex_nama       = $this->db->query("SELECT vcNama FROM muser where intNomor ='". $r['bex_nomor']."'")->row()->vcNama;
                $customer_nama  = $this->db->query("SELECT vcNama FROM mcustomer where intNomor ='". $r['customer_nomor']."'")->row()->vcNama;
                array_push($data['data'], array(
                                                'jadwal_nomor'   => $r['jadwal_nomor'], 
                                                'manager_nomor'  => $r['manager_nomor'], 
                                                'manager_nama'   => $manager_nama, 
                                                'bex_nomor'      => $r['bex_nomor'], 
                                                'bex_nama'       => $bex_nama, 
                                                'customer_nomor' => $r['customer_nomor'], 
                                                'customer_nama'  => $customer_nama, 
                                                'tipe'           => $r['tipe'], 
                                                'jenisjadwal'    => $r['jenisjadwal'], 
                                                'tanggal'        => $r['tanggal'], 
                                                'jam'            => $r['jam'], 
                                                'keterangan'     => $r['keterangan'], 
                                                'jadwal_selesai' => $r['jadwal_selesai']
                                                )
                );

            	// START SEND NOTIFICATION
	            $vcGCMId = $this->getGCMId($bex);
	            if( $vcGCMId != "null" ){
	                $jadwal_nomor = $r['jadwal_nomor'];
	                $keterangan   = $r['keterangan'];
	                $this->send_gcm($vcGCMId, $this->ellipsis($keterangan),"Schedule Task From ".$manager_nama,'ScheduleTaskSalesList',$jadwal_nomor,$manager_nama);
	            }
            }
        }else{      
            array_push($data['data'], array( 'query' => $this->error($query) ));
        }  

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

}
