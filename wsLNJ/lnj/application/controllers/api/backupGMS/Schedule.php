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
class Schedule extends REST_Controller { 

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
    
    function cancelSchedule_post()
    {
		$data['data'] = array();
		
		$value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
        
        $this->db->trans_begin();
        
        $nomor = (isset($jsonObject["nomor"]) 			? $jsonObject["nomor"]      	: "");
        
        $query = "UPDATE whschedule_mobile set status_aktif = false where nomor = $nomor";
        
        $this->db->query($query);
        
        if ($this->db->trans_status() === FALSE)
		{
			$this->db->trans_rollback();
			array_push($data['data'], array( 'query' => $this->error($query) ));	
		}
		else
		{
			$this->db->trans_commit();
			array_push($data['data'], array( 'success' => 'true' ));
		}
		
		if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
	}
    
    function getSchedules_post()
    {
		$data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
        
        $user = (isset($jsonObject["user"]) 			? $jsonObject["user"]      	: "");

        $query = "SELECT s.nomor, 
				user.userid as creator,
				u.userid as target, 
				c.nama as customer,
				p.nama as prospecting,
				g.nama as groupsch,
				s.tipejadwal,
				s.tanggal, s.jam FROM whschedule_mobile s
				left JOIN whuser_mobile user on user.nomor = s.nomorwhuser_creator
				left JOIN whuser_mobile u on u.nomor = s.nomorwhuser_tujuan
				left JOIN whgroup_mobile g on g.nomor = s.nomorwhgroup
				left JOIN tcustomer c on c.nomor = s.nomortcustomer
				left JOIN tcustomerprospecting p on p.nomor = s.nomortcustomerprospecting
				where (s.nomorwhuser_creator = $user or s.nomorwhuser_tujuan = $user) and s.status_aktif = true
				order by s.tgl_buat";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                                                'nomor'         	=> $r['nomor'],
                                                'creator' 			=> $r['creator'],
                                                'target' 			=> $r['target'],
                                                'customer'			=> $r['customer'],
                                                'prospecting' 		=> $r['prospecting'],
                                                'group' 			=> $r['groupsch'],
                                                'type' 				=> $r['tipejadwal'],
                                                'date' 				=> $r['tanggal'],
                                                'time' 				=> $r['jam']
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
    
    //--- Added by Shodiq ---//
    function setSchedule_post()
    {
		$data['data'] = array();
		
		$value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
        
        $creator = (isset($jsonObject["creator"]) 			? $jsonObject["creator"]      	: "");
        $target = (isset($jsonObject["target"]) 			? $jsonObject["target"]    	  	: "");
        $customer = (isset($jsonObject["customer"]) 		? $jsonObject["customer"]     	: "");
        $prospecting = (isset($jsonObject["prospecting"]) 	? $jsonObject["prospecting"]    : "");
        $group = (isset($jsonObject["group"]) 				? $jsonObject["group"]     		: "");
        $type = (isset($jsonObject["type"]) 				? $jsonObject["type"]        	: "");
        $reminder = (isset($jsonObject["reminder"]) 		? $jsonObject["reminder"]     	: "");
        $date = (isset($jsonObject["date"]) 				? $jsonObject["date"]         	: "");
        $time = (isset($jsonObject["time"]) 				? $jsonObject["time"]         	: "");
        $description = (isset($jsonObject["description"]) ? $this->clean($jsonObject["description"])     : "");
        
        $this->db->trans_begin();
        
        $query = "INSERT INTO `whschedule_mobile`
			(`nomorwhuser_creator`, ";
		if ($target != "")
			$query = $query . "`nomorwhuser_tujuan`, ";
		if ($customer != "")
			$query = $query . "`nomortcustomer`, ";
		if ($prospecting != "")
			$query = $query . "`nomortcustomerprospecting`, ";
		if ($group != "")
			$query = $query . "`nomorwhgroup`, ";
			
		$query = $query . "`tipejadwal`, `reminder`, `tanggal`, `jam`, `keterangan`, `status_selesai`, `status_aktif`, `tgl_buat`)
        VALUES(
			$creator,";
		
		if ($target != "")
			$query = $query . " $target,";
		if ($customer != "")
			$query = $query . " $customer,";
		if ($prospecting != "")
			$query = $query . " $prospecting,";
		if ($group != "")
			$query = $query . " $group,";
			
			$query = $query . " '$type', $reminder, '$date', '$time', '$description', false, true, NOW()
        )";
        
        $this->db->query($query);
        
        if ($this->db->trans_status() === FALSE)
		{
			$this->db->trans_rollback();
			array_push($data['data'], array( 'query' => $this->error($query) ));	
		}
		else
		{
			$this->db->trans_commit();
			array_push($data['data'], array( 'success' => 'true' ));
		}
		
		if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
	}
}
