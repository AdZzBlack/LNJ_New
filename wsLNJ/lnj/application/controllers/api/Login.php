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

        $user = (isset($jsonObject["username"]) ? $this->clean($jsonObject["username"])     : "adi");
        $pass = md5((isset($jsonObject["password"]) ? $this->clean($jsonObject["password"]) : "admin"));
        $token = (isset($jsonObject["token"]) ? $jsonObject["token"]     : "a");

//        $interval  = $this->db->query("SELECT intnilai FROM whsetting_mobile WHERE intNomor = 1 LIMIT 1")->row()->intnilai;
		
		$query1 = "	UPDATE mhadmin a
					SET hash = UUID(),
					gcm_id = '$token'
					WHERE a.status_aktif = 1
					AND a.kode = '$user'
                    AND BINARY a.sandi = '$pass'";
        $this->db->query($query1);
		
        $query = "	SELECT 
						a.nomor AS nomor,
						a.sandi AS `password`,
						a.nomormhpegawai AS nomor_pegawai,
						d.kode AS kode_pegawai,
						a.nama AS nama,
						a.role_android AS role,
						a.hash AS `hash`,
						a.nomormhcabang AS cabang,
						e.nama AS namacabang,
						b.isdriver AS isdriver,
						b.qrcodereader AS qrcodereader,
						b.checkin AS checkin
					FROM mhadmin a
					JOIN whrole_mobile b ON a.role_android = b.nomor
					LEFT JOIN mhpegawai d ON a.nomormhpegawai = d.nomor
					JOIN mhcabang e ON a.nomormhcabang = e.nomor
					WHERE a.status_aktif = 1
					AND a.kode = '$user'
					AND BINARY a.sandi = '$pass'";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                								'user_nomor'    	    		=> $r['nomor'],
												'user_password'					=> $r['password'],
                                                'user_nomor_pegawai'         	=> $r['nomor_pegawai'],
                                                'user_kode_pegawai'        		=> $r['kode_pegawai'],
                								'user_nama' 					=> $r['nama'],
												'user_role' 					=> $r['role'], 
												'user_hash' 					=> $r['hash'],
												'user_cabang' 					=> $r['cabang'],
												'user_nama_cabang' 				=> $r['namacabang'],
												'role_isdriver'					=> $r['isdriver'],
												'role_qrcodereader' 			=> $r['qrcodereader'],
												'role_checkin'					=> $r['checkin'],
                								)
               	);
            }
        }else{		
			array_push($data['data'], array( 'query' => $this->error($query1) ));
		}  
	
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

        $query = "	SELECT 
                        a.nomor AS nomor,
                        a.sandi AS `password`,
                        a.nomormhpegawai AS nomor_pegawai,
                        d.kode AS kode_pegawai,
                        a.nama AS nama,
                        a.role_android AS role,
                        a.hash AS `hash`,
                        a.nomormhcabang AS cabang,
                        e.nama AS namacabang,
                        b.isdriver AS isdriver,
                        b.qrcodereader AS qrcodereader,
                        b.checkin AS checkin
                    FROM mhadmin a
                    JOIN whrole_mobile b ON a.role_android = b.nomor
                    LEFT JOIN mhpegawai d ON a.nomormhpegawai = d.nomor
                    JOIN mhcabang e ON a.nomormhcabang = e.nomor
                    WHERE a.status_aktif = 1
					AND hash = '$hash'";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0)
		{
			foreach ($result->result_array() as $r)
			{
				array_push($data['data'], array(
													'success'						=> "true",
													'user_nomor'    	    		=> $r['nomor'],
                                                    'user_password'					=> $r['password'],
                                                    'user_nomor_pegawai'         	=> $r['nomor_pegawai'],
                                                    'user_kode_pegawai'        		=> $r['kode_pegawai'],
                                                    'user_nama' 					=> $r['nama'],
                                                    'user_role' 					=> $r['role'],
                                                    'user_hash' 					=> $r['hash'],
                                                    'user_cabang' 					=> $r['cabang'],
                                                    'user_nama_cabang' 				=> $r['namacabang'],
                                                    'role_isdriver'					=> $r['isdriver'],
                                                    'role_qrcodereader' 			=> $r['qrcodereader'],
                                                    'role_checkin'					=> $r['checkin'],
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
