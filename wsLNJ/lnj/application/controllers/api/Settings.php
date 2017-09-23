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
class Settings extends REST_Controller { 

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
        return str_replace( array("\t", "\n") , "", $string);
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
        $vcGCMId = $this->getGCMId(3);
		
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

	//by Tonny (03-08-2017) untuk mendapatkan value pada tabel settings
	function getSettings_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $interval  = $this->db->query("SELECT intnilai FROM whsetting_mobile WHERE intNomor = 1 LIMIT 1")->row()->intnilai;
        $radius    = $this->db->query("SELECT intnilai FROM whsetting_mobile WHERE intNomor = 2 LIMIT 1")->row()->intnilai;
		$tracking  = $this->db->query("SELECT intnilai FROM whsetting_mobile WHERE intNomor = 3 LIMIT 1")->row()->intnilai;
		$jam_awal  = $this->db->query("SELECT intnilai FROM whsetting_mobile WHERE intNomor = 6 LIMIT 1")->row()->intnilai;
		$jam_akhir = $this->db->query("SELECT intnilai FROM whsetting_mobile WHERE intNomor = 7 LIMIT 1")->row()->intnilai;
        
        array_push($data['data'], array( 
										'success'	=> "true",
    									'interval' 	=> $interval, 
    									'radius'	=> $radius,
										'tracking'	=> $tracking,
										'jam_awal'	=> $jam_awal,
										'jam_akhir'	=> $jam_akhir
        								)
        );

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }
	
	//by Tonny (03-08-2017) untuk melakukan update pada tabel settings
	function setSettings_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $interval  = (isset($jsonObject["interval"]) ? $this->clean($jsonObject["interval"])       : "a");
        $radius    = (isset($jsonObject["radius"]) ? $this->clean($jsonObject["radius"])           : "a");
		$tracking  = (isset($jsonObject["tracking"]) ? $this->clean($jsonObject["tracking"])       : "a");
		$jam_awal  = (isset($jsonObject["jam_awal"]) ? $this->clean($jsonObject["jam_awal"])       : "a");
		$jam_akhir = (isset($jsonObject["jam_akhir"]) ? $this->clean($jsonObject["jam_akhir"])     : "a");
		
		$this->db->trans_begin();			
		$query = "	UPDATE whsetting_mobile 
					SET intNilai = '$interval'
					WHERE intStatus > 0 
					AND intNomor = 1 ";
		$this->db->query($query);
		$query = "	UPDATE whsetting_mobile 
					SET intNilai = '$radius'
					WHERE intStatus > 0 
					AND intNomor = 2 ";
		$this->db->query($query);
		$query = "	UPDATE whsetting_mobile 
					SET intNilai = '$tracking'
					WHERE intStatus > 0 
					AND intNomor = 3 ";
		$this->db->query($query);
		$query = "	UPDATE whsetting_mobile 
					SET intNilai = CONCAT(SUBSTRING('$jam_awal', 1, 2), ':', SUBSTRING('$jam_awal', 3, 2))
					WHERE intStatus > 0 
					AND intNomor = 6 ";
		$this->db->query($query);
		$query = "	UPDATE whsetting_mobile 
					SET intNilai = CONCAT(SUBSTRING('$jam_akhir', 1, 2), ':', SUBSTRING('$jam_akhir', 3, 2))
					WHERE intStatus > 0 
					AND intNomor = 7 ";
		$this->db->query($query);
		if ($this->db->trans_status() === FALSE)
			$this->db->trans_rollback();
		else
			$this->db->trans_commit();
		
        if ($this->db->trans_status() === FALSE){
            $this->db->trans_rollback();
            array_push($data['data'], array( 'success' => $query ));
        }else{
            $this->db->trans_commit();
            array_push($data['data'], array( 'success' => "true" ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }
}
