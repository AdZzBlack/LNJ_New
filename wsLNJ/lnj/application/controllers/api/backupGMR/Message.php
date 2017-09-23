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
class Message extends REST_Controller { 

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

       if ($this->gcm->send())
           echo 'Success for all messages';
       else
           echo 'Some messages have errors';

       print_r($this->gcm->status);
       print_r($this->gcm->messagesStatuses);

        die(' Worked.');
    }

	function test_get()
	{
		$result = "a";
		
		$data['data'] = array();
		
		// START SEND NOTIFICATION
        $vcGCMId = $this->getGCMId(18);
		
            $this->send_gcm($vcGCMId, $this->ellipsis('$new_message'),'New Message(s) From ','PrivateMessage','0','0');
        
		
		
		$this->response($vcGCMId);
	}

	// --- POST READ PRIVATE MESSAGE --- //
	function readPrivateMessage_post()
	{     
        $data['data'] = array();

        $value = file_get_contents('php://input');
		$jsonObject = (json_decode($value , true));

        $userfrom_nomor = (isset($jsonObject["userfrom_nomor"]) ? $this->clean($jsonObject["userfrom_nomor"])     : "");
		$userto_nomor = (isset($jsonObject["userto_nomor"]) ? $this->clean($jsonObject["userto_nomor"])     : "");
		
		$this->db->trans_begin();
		
		$query = "  UPDATE whchat_mobile
                    SET status_read = '2'
					WHERE 1 = 1 
						AND nomoruser_from = $userto_nomor
						AND nomoruser_to = $userfrom_nomor";
        $this->db->query($query);

        if ($this->db->trans_status() === FALSE){
            $this->db->trans_rollback();
        }else{
            $this->db->trans_commit();
        }
    }
	
		// --- POST SEND PRIVATE MESSAGE --- //
	function sendPrivateMessage_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
		$jsonObject = (json_decode($value , true));

		$userfrom_nomor = (isset($jsonObject["userfrom_nomor"]) ? $this->clean($jsonObject["userfrom_nomor"])     : "");
		$userto_nomor = (isset($jsonObject["userto_nomor"]) ? $this->clean($jsonObject["userto_nomor"])     : "");
		$new_message = (isset($jsonObject["new_message"]) ? $jsonObject["new_message"]     : "");
		$tipe_send = (isset($jsonObject["tipe_send"]) ? $this->clean($jsonObject["tipe_send"])     : "");
		$url = (isset($jsonObject["url"]) ? $jsonObject["url"]     : "");
		
        $this->db->trans_begin();

        $query = $this->db->insert_string('whchat_mobile', array(
															  'nomoruser_from'   => $userfrom_nomor, 
															  'nomoruser_to'	 => $userto_nomor, 
															  'message'        	 => $new_message, 
															  'uploadfile'   	 => $url,
															  'tipeuploadfile'   => $tipe_send,
															  'status_read'      => "1"
															)
														);
		$this->db->query($query);

        if ($this->db->trans_status() === FALSE){
            $this->db->trans_rollback();
            array_push($data['data'], array( 'success' => "false" ));
        }else{
            $this->db->trans_commit();
			$nomor = $this->db->query("SELECT nomor FROM whchat_mobile ORDER BY nomor DESC LIMIT 1")->row()->nomor;
            array_push($data['data'], array( 'success' => "true", 'nomor' => $nomor ));
        }  

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }

    }

	// --- POST GET PRIVATE MESSAGE --- //
	function getPrivateMessage_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
		$jsonObject = (json_decode($value , true));
		
		$userfrom_nomor = (isset($jsonObject["userfrom_nomor"]) ? $this->clean($jsonObject["userfrom_nomor"]) : "");
        if($userfrom_nomor != ""){ $userfrom_nomor = " AND (a.nomoruser_from = $userfrom_nomor OR a.nomoruser_to = $userfrom_nomor)  " ; }

        $userto_nomor = (isset($jsonObject["userto_nomor"]) ? $this->clean($jsonObject["userto_nomor"]) : "");
        if($userto_nomor != ""){ $userto_nomor = " AND (a.nomoruser_from = $userto_nomor OR a.nomoruser_to = $userto_nomor)  " ; }

        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }
		
        $query = "  SELECT DISTINCT 
						a.nomor,
                        a.nomoruser_from, 
                        a.nomoruser_to, 
                        a.message, 
						a.uploadfile,
						a.tipeuploadfile,
                        a.status_read, 
                        a.dibuat_pada
                    FROM whchat_mobile a 
                    WHERE 1 = 1 $userfrom_nomor $userto_nomor
                    ORDER BY a.dibuat_pada DESC
                    $limit";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
				$userfrom_nama = $this->db->query("SELECT userid FROM whuser_mobile where nomor ='". $r['nomoruser_from']."'")->row()->userid;   
                $userto_nama = $this->db->query("SELECT userid FROM whuser_mobile where nomor ='". $r['nomoruser_to']."'")->row()->userid;
                array_push($data['data'], array(
												'nomor'				=> $r['nomor'], 
												'userfrom_nomor'    => $r['nomoruser_from'], 
                                                'userto_nomor'      => $r['nomoruser_to'], 
                                                'userfrom_nama'     => $userfrom_nama, 
                                                'userto_nama'       => $userto_nama, 
                                                'message'        	=> $r['message'],
												'url'				=> $r['uploadfile'],
												'tipe'				=> $r['tipeuploadfile'],
												'waktu'          	=> $r['dibuat_pada'],
                                                'status_read'    	=> $r['status_read'], 
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
}
