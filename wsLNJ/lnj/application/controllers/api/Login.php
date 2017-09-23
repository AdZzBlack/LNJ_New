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
class Login extends REST_Controller { 

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

	function ellipsis($string) {
        $cut = 30;
        $out = strlen($string) > $cut ? substr($string,0,$cut)."..." : $string;
        return $out;
    }
	
    function clean($string) {
        return preg_replace("/[^[:alnum:][:space:]]/u", '', $string); // Replaces multiple hyphens with single one.
    }

    function error($string) {
        return str_replace( array("\t", "\n", "\r") , " ", $string);
    }
	
	function getGCMId($user_nomor){
        $query = "  SELECT 
                    a.gcmid
                    FROM whuser_mobile a 
                    WHERE a.status_aktif > 0 AND (a.gcmid <> '' AND a.gcmid IS NOT NULL) AND a.nomor = $user_nomor ";
        return $this->db->query($query)->row()->gcmid;
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

       if ($this->gcm->send())
           echo 'Success for all messages';
       else
           echo 'Some messages have errors';

       print_r($this->gcm->status);
       print_r($this->gcm->messagesStatuses);

        die(' Worked.');
    }

	public function send_gcm_group($registrationId,$message,$title,$fragment,$nomor,$nama)
    {
        $this->load->library('gcm');

        $this->gcm->setMessage($message);
        $this->gcm->setTitle($title);
        $this->gcm->setFragment($fragment);
        $this->gcm->setNomor($nomor);
        $this->gcm->setNama($nama);

        foreach ($registrationId as $regisID) {
            $this->gcm->addRecepient($regisID);
        }

        $this->gcm->setTtl(500);
        $this->gcm->setTtl(false);

        $this->gcm->setGroup('Test');
        $this->gcm->setGroup(false);

        $this->gcm->send();
    }
	
	function test_get()
	{
		
		$result = "a";
		
		$data['data'] = array();
		
		// START SEND NOTIFICATION
        $vcGCMId = 'fS4WhyXLT2M:APA91bEKMcCVsHknYDrEnKXqDw6q_g7cRWsoJbTxRb-l3jMHzn2UD9RV1MiSAYhnYsWljt1ygiHlNvDb6toom35JUG9RJP8eTC-jOYQDaiD6lkCQ7M-V5LWJDDtXeVbAiDlX0sc_dqnc';
		
        $this->send_gcm($vcGCMId, $this->ellipsis('$new_message'),'New Message(s) From ','PrivateMessage','0','0');
        
		
		
		$this->response($vcGCMId);
		
		/*
		$regisID = array();
			
		$query_getuser = " SELECT 
							a.gcmid
							FROM whuser_mobile a 
							JOIN whrole_mobile b ON a.nomorrole = b.nomor
							WHERE a.status_aktif > 0 AND (a.gcmid <> '' AND a.gcmid IS NOT NULL) AND b.approveberitaacara = 1 ";
		$result_getuser = $this->db->query($query_getuser);

		if( $result_getuser && $result_getuser->num_rows() > 0){
			foreach ($result_getuser->result_array() as $r_user){

				// START SEND NOTIFICATION
				$vcGCMId = $r_user['gcmid'];
				if( $vcGCMId != "null" ){      
					array_push($regisID, $vcGCMId);       
				}
				
			}
			$count = $this->db->query("SELECT COUNT(1) AS elevasi_baru FROM mhberitaacara a WHERE a.status_disetujui = 0")->row()->elevasi_baru; 
			$this->send_gcm_group($regisID, $this->ellipsis("Berita Acara Elevasi"),$count . ' pending elevasi','ChooseApprovalElevasi','','');
		} 
		*/
	}

	// --- POST Login --- //
	function loginUser_post()
	{     
        $data['data'] = array();

        $value = file_get_contents('php://input');
		$jsonObject = (json_decode($value , true));

        $user = (isset($jsonObject["username"]) ? $this->clean($jsonObject["username"])     : "a");
        $pass = md5((isset($jsonObject["password"]) ? $this->clean($jsonObject["password"]) : "a"));
        $token = (isset($jsonObject["token"]) ? $jsonObject["token"]     : "a");

        $interval  = $this->db->query("SELECT intnilai FROM whsetting_mobile WHERE intNomor = 1 LIMIT 1")->row()->intnilai;
        $radius    = $this->db->query("SELECT intnilai FROM whsetting_mobile WHERE intNomor = 2 LIMIT 1")->row()->intnilai;
        $tracking  = $this->db->query("SELECT intnilai FROM whsetting_mobile WHERE intNomor = 3 LIMIT 1")->row()->intnilai;
        $jam_awal  = $this->db->query("SELECT intnilai FROM whsetting_mobile WHERE intNomor = 6 LIMIT 1")->row()->intnilai;
        $jam_akhir = $this->db->query("SELECT intnilai FROM whsetting_mobile WHERE intNomor = 7 LIMIT 1")->row()->intnilai;
		
		$query = "	UPDATE whuser_mobile a 
					SET hash = UUID(),
					token = '$token'
					WHERE a.status_aktif > 0 
					AND a.userid = ? 
					AND BINARY a.password = ?";
        $this->db->query($query, array($user, $pass));
		
        $query = "	SELECT 
						a.nomor AS nomor_android,
						a.nomortuser AS nomor,
						a.password AS `password`,
						a.nomorthsales AS nomor_sales,
						d.kode AS kode_sales,
						a.userid AS nama,
						a.tipeuser AS tipe,
						a.nomorrole AS role,
						a.hash AS `hash`,
						IFNULL(a.telp, '') AS telp,
						a.nomorcabang AS cabang,
						e.cabang AS namacabang,
						b.isowner AS isowner,
						b.issales AS issales,
						b.setting AS setting,
						b.settingtarget AS settingtarget,
						b.salesorder AS salesorder,
						b.stockmonitoring AS stockmonitoring,
						b.pricelist AS pricelist,
						b.addscheduletask AS addscheduletask,
						b.salestracking AS salestracking,
						b.hpp AS hpp,
						b.crossbranch AS crossbranch,
						b.creategroup AS creategroup
					FROM whuser_mobile a
					JOIN whrole_mobile b ON a.nomorrole = b.nomor
					LEFT JOIN tuser c ON a.nomortuser = c.nomor
					LEFT JOIN thsales d ON a.nomorthsales = d.nomor
					JOIN tcabang e ON a.nomorcabang = e.nomor
					WHERE a.status_aktif = 1
					AND a.userid = '$user'
					AND BINARY a.password = '$pass'";
        $result = $this->db->query($query, array($user, $pass));

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                								'user_nomor_android'			=> $r['nomor_android'],
												'user_nomor'					=> $r['nomor'],
												'user_password'					=> $r['password'],
                                                'user_nomor_sales'         		=> $r['nomor_sales'],
                                                'user_kode_sales'         		=> $r['kode_sales'],
                								'user_nama' 					=> $r['nama'], 
												'user_tipe' 					=> $r['tipe'], 
												'user_role' 					=> $r['role'], 
												'user_hash' 					=> $r['hash'],
												'user_telp' 					=> $r['telp'],
												'user_cabang' 					=> $r['cabang'],
												'user_nama_cabang' 				=> $r['namacabang'],
												'role_isowner'					=> $r['isowner'],
												'role_issales'					=> $r['issales'],
												'role_setting'					=> $r['setting'],
												'role_settingtarget'			=> $r['settingtarget'],
												'role_salesorder'				=> $r['salesorder'],
												'role_stockmonitoring'			=> $r['stockmonitoring'],
												'role_pricelist'				=> $r['pricelist'],
												'role_addscheduletask'			=> $r['addscheduletask'],
												'role_salestracking'			=> $r['salestracking'],
												'role_hpp'          			=> $r['hpp'],
                                                'role_crossbranch'  			=> $r['crossbranch'],
                                                'role_creategroup'  			=> $r['creategroup'],
                                                'setting_interval'  			=> $interval,
                                                'setting_radius'      			=> $radius,
                                                'setting_tracking'  			=> $tracking,
                                                'setting_jamawal'     			=> $jam_awal,
                                                'setting_jamakhir'  			=> $jam_akhir
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
	
	// --- POST GPS tracking system --- //
	function gpsTracking_post()
	{     
        $data['data'] = array();

        $value = file_get_contents('php://input');
		$jsonObject = (json_decode($value , true));

        array_push($data['data'], array(
			'id'				=> $r['id'],
			'latitude'			=> $r['latitude'],
			'longitude'			=> $r['longitude'], 
			)
		);
	
        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }

    }
	
	function checkUser_post()
	{     
        $data['data'] = array();

        $value = file_get_contents('php://input');
		$jsonObject = (json_decode($value , true));

        $hash = (isset($jsonObject["hash"]) ? $jsonObject["hash"]     : "");

        $interval  = $this->db->query("SELECT intnilai FROM whsetting_mobile WHERE intNomor = 1 LIMIT 1")->row()->intnilai;
        $radius    = $this->db->query("SELECT intnilai FROM whsetting_mobile WHERE intNomor = 2 LIMIT 1")->row()->intnilai;
		$tracking  = $this->db->query("SELECT intnilai FROM whsetting_mobile WHERE intNomor = 3 LIMIT 1")->row()->intnilai;
		$jam_awal  = $this->db->query("SELECT intnilai FROM whsetting_mobile WHERE intNomor = 6 LIMIT 1")->row()->intnilai;
		$jam_akhir = $this->db->query("SELECT intnilai FROM whsetting_mobile WHERE intNomor = 7 LIMIT 1")->row()->intnilai;
		
        $query = "	SELECT 
						a.nomor AS nomor_android,
                        a.nomortuser AS nomor,
                        a.password AS `password`,
                        a.nomorthsales AS nomor_sales,
                        d.kode AS kode_sales,
                        a.userid AS nama,
                        a.tipeuser AS tipe,
                        a.nomorrole AS role,
                        a.hash AS `hash`,
                        IFNULL(a.telp, '') AS telp,
                        a.nomorcabang AS cabang,
                        e.cabang AS namacabang,
                        b.isowner AS isowner,
                        b.issales AS issales,
                        b.setting AS setting,
                        b.settingtarget AS settingtarget,
                        b.salesorder AS salesorder,
                        b.stockmonitoring AS stockmonitoring,
                        b.pricelist AS pricelist,
                        b.addscheduletask AS addscheduletask,
                        b.salestracking AS salestracking,
                        b.hpp AS hpp,
                        b.crossbranch AS crossbranch,
                        b.creategroup AS creategroup
                    FROM whuser_mobile a
                    JOIN whrole_mobile b ON a.nomorrole = b.nomor
                    LEFT JOIN tuser c ON a.nomortuser = c.nomor
                    LEFT JOIN thsales d ON a.nomorthsales = d.nomor
                    JOIN tcabang e ON a.nomorcabang = e.nomor
                    WHERE a.status_aktif = 1
						AND hash = '$hash'";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0)
		{
			foreach ($result->result_array() as $r)
			{
				array_push($data['data'], array(
													'success'						=> "true",
													'user_nomor_android'			=> $r['nomor_android'],
													'user_nomor' 					=> $r['nomor'],
													'user_nomor_sales'         		=> $r['nomor_sales'],
													'user_kode_sales'         		=> $r['kode_sales'],  //added by Tonny
													'user_password'					=> $r['password'],
													'user_nama' 					=> $r['nama'], 
													'user_tipe' 					=> $r['tipe'], 
													'user_role' 					=> $r['role'], 
													'user_hash' 					=> $r['hash'],
													'user_telp' 					=> $r['telp'],
													'user_cabang' 					=> $r['cabang'],
													'user_nama_cabang' 				=> $r['namacabang'],  //added by Tonny
													'role_isowner'					=> $r['isowner'],
													'role_issales'					=> $r['issales'],
													'role_setting'					=> $r['setting'],
													'role_settingtarget'			=> $r['settingtarget'],
													'role_salesorder'				=> $r['salesorder'],
													'role_stockmonitoring'			=> $r['stockmonitoring'],
													'role_pricelist'				=> $r['pricelist'],
													'role_addscheduletask'			=> $r['addscheduletask'],
													'role_salestracking'			=> $r['salestracking'],
													'role_hpp'          			=> $r['hpp'],
                                                    'role_crossbranch'  			=> $r['crossbranch'],
                                                    'role_creategroup'  			=> $r['creategroup'],
                                                    'setting_interval'  			=> $interval,
                                                    'setting_radius'      			=> $radius,
                                                    'setting_tracking'  			=> $tracking,
                                                    'setting_jamawal'     			=> $jam_awal,
                                                    'setting_jamakhir'  			=> $jam_akhir
											)
				);
			}
        }
		else
		{		
			array_push($data['data'], array( 'success' => "false" ));
		}  

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }

    }
}
