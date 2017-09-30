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
class Group extends REST_Controller { 

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
    
    //--- Added by Shodiq ---//
    function getGroups_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
        
        $user = (isset($jsonObject["user"]) 			? $jsonObject["user"]      	: "");

        $query = "select he.nomor, he.nama from whtdgroup_mobile de
		left join whgroup_mobile he on de.nomorwhgroup = he.nomor
		where de.nomorwhuser = $user and he.status_aktif = true and de.status_aktif = true";
        
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                                                'nomor'         	=> $r['nomor'],
                                                'nama' 				=> $r['nama']
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
    
    function getDataGroup_post()
    {
		$data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
        
        $nomor = (isset($jsonObject["nomor"]) 			? $jsonObject["nomor"]      	: "");

        $query = "SELECT users.nomor as nomor, users.userid as nama FROM gms_web.whuser_mobile users
		left join gms_web.whtdgroup_mobile detail on detail.nomorwhuser = users.nomor
		where detail.nomorwhgroup = $nomor and detail.status_aktif = true";
        
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                                                'nomor'         	=> $r['nomor'],
                                                'nama' 				=> $r['nama']
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
    
    function updateGroup_post()
    {
		$data['data'] = array();
		
		$value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
        
        $creator = (isset($jsonObject["creator"]) 		? $jsonObject["creator"]      	: "");
        $nomor = (isset($jsonObject["nomor"]) 			? $jsonObject["nomor"]    	  	: "");
        $nama = (isset($jsonObject["nama"]) 			? $jsonObject["nama"]    	  	: "");
        $status = (isset($jsonObject["status"]) 		? $jsonObject["status"]     	: "");
        $users = (isset($jsonObject["users"]) 			? $jsonObject["users"]			: "");
        
        $query = "UPDATE whgroup_mobile set 
			nama = '$nama', 
			status_aktif = $status 
			WHERE nomor = $nomor
        ";
        $this->db->query($query);
        
        $query = "UPDATE whtdgroup_mobile set status_aktif = false, nomorremoveby = $creator WHERE nomorwhgroup = $nomor";
        $this->db->query($query);
        
        $query = "UPDATE whtdgroup_mobile set status_aktif = true, nomorremoveby = 0 WHERE nomorwhgroup = $nomor and nomorwhuser = $creator";
		$this->db->query($query);
        
        $userArray = explode('|', $users);
        
        foreach ($userArray as $user) {
			$query = "UPDATE whtdgroup_mobile set status_aktif = true, nomorremoveby = 0 WHERE nomorwhgroup = $nomor and nomorwhuser = $user";
			$this->db->query($query);
			
			$query = "INSERT INTO whtdgroup_mobile (nomorwhgroup, nomorwhuser, nomorinsertby, tgl_buat, status_aktif) 
			SELECT * FROM (SELECT $nomor, $user, $creator, NOW(), true) AS tmp 
			WHERE NOT EXISTS (
				SELECT nomorwhuser FROM whtdgroup_mobile WHERE nomorwhuser = $user and nomorwhgroup = $nomor
			) LIMIT 1";
        
			$this->db->query($query);
		}
	}
    
    function newGroup_post()
    {
		$data['data'] = array();
		
		$value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
        
        $creator = (isset($jsonObject["creator"]) 		? $jsonObject["creator"]      	: "");
        $nama = (isset($jsonObject["nama"]) 			? $jsonObject["nama"]    	  	: "");
        $status = (isset($jsonObject["status"]) 		? $jsonObject["status"]     	: "");
        $users = (isset($jsonObject["users"]) 			? $jsonObject["users"]			: "");
        
        $this->db->trans_begin();
        
        $query = "INSERT INTO whgroup_mobile (nama, nomorwhuser, status_aktif) VALUES('$nama', $creator, $status)";
        
        $this->db->query($query);
        $id = $this->db->insert_id();
        
        $query = "INSERT INTO whtdgroup_mobile (nomorwhgroup, nomorwhuser, nomorinsertby, tgl_buat, status_aktif) 
        VALUES($id, $creator, $creator, NOW(), true)";
        
		$this->db->query($query);
        
        $userArray = explode('|', $users);
        
        foreach ($userArray as $user) {
			$query = "INSERT INTO whtdgroup_mobile (nomorwhgroup, nomorwhuser, nomorinsertby, tgl_buat, status_aktif) 
			VALUES($id, $user, $creator, NOW(), true)";
        
			$this->db->query($query);
		}
        
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
