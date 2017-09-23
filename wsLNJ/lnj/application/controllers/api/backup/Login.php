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

    function clean($string) {
        return preg_replace("/[^[:alnum:][:space:]]/u", '', $string); // Replaces multiple hyphens with single one.
    }

    function error($string) {
        return str_replace( array("\t", "\n") , "", $string);
    }

	// --- POST Login --- //
	function loginUser_post(){     

        $interval = "";
        $db_android = $this->load->database('db_android', TRUE);
        $interval = $db_android->query("SELECT a.intNomor, a.vcNama, a.intNilai FROM tsetting a WHERE a.intStatus > 0 AND a.vcNama = 'interval'")->row()->intNilai;
        $radius   = $db_android->query("SELECT a.intNomor, a.vcNama, a.intNilai FROM tsetting a WHERE a.intStatus > 0 AND a.vcNama = 'radius'")->row()->intNilai;

        $data['data'] = array();

        $value = file_get_contents('php://input');
		$jsonObject = (json_decode($value , true));

        $user = (isset($jsonObject["username"]) ? $this->clean($jsonObject["username"])     : "");
        $pass = md5((isset($jsonObject["password"]) ? $this->clean($jsonObject["password"]) : ""));

		$db_android->query("UPDATE tsetting SET intNilai = '$interval' WHERE vcNama = 'interval'");
		$query = "	UPDATE muser a 
						JOIN mcabang b ON a.intNomorMCabang = b.intNomor 
						JOIN muser_android c ON a.intNomor = c.intNomorMUser 
						JOIN msales d ON c.intNomorMSales = d.intNomor
					SET vcHash = UUID()
					WHERE a.intStatus > 0 
					AND c.vcUserID = ? AND BINARY c.vcMD5Password = ?";
        $result = $this->db->query($query, array($user, $pass));
		
        $query = "	SELECT 
						a.intNomor AS user_nomor, 
						a.intNomorMHUserGroup AS user_usergroup, 
						c.vcUserID AS user_id, 
						c.vcMD5Password AS user_password,
						a.vcNama AS user_nama, 
                        c.intNomorMSales AS user_sales, 
                        c.vcJabatan AS user_jabatan,
                        d.vcHP AS user_hp,
						a.intCanSignApproval AS user_cansignapproval,
						a.intNomorMCabang AS cabang_nomor,
						b.vcKode AS cabang_kode,  
						b.vcNama AS cabang_nama,
						c.vcHash AS hash
					FROM muser a 
					JOIN mcabang b ON a.intNomorMCabang = b.intNomor 
                    JOIN muser_android c ON a.intNomor = c.intNomorMUser 
                    LEFT JOIN msales d ON c.intNomorMSales = d.intNomor 
					WHERE a.intStatus > 0 
					AND BINARY c.vcUserID = ? AND BINARY c.vcMD5Password = ?";
        $result = $this->db->query($query, array($user, $pass));

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                $user_lat = "0";
                $user_long = "0";
                $queryLatLong = "SELECT a.decLatitude,a.decLongitude FROM ttracking a WHERE a.intStatus > 0 AND a.intNomorMUser = '". $r['user_nomor'] ."' ORDER BY a.dtInsertDate DESC LIMIT 1";
                $resultLatLong = $db_android->query($queryLatLong);
                if( $resultLatLong && $resultLatLong->num_rows() > 0){
                    foreach ($resultLatLong->result_array() as $rs){
                        $user_lat = $rs["decLatitude"];
                        $user_long = $rs["decLongitude"];
                    }
                }

                array_push($data['data'], array(
                								'user_nomor' 			=> $r['user_nomor'], 
                								'user_usergroup' 		=> $r['user_usergroup'], 
                								'user_id' 				=> $r['user_id'],
                								'user_password' 		=> $r['user_password'], 
                                                'user_nama'             => $r['user_nama'], 
                								'user_sales' 			=> $r['user_sales'], 
                                                'user_jabatan'          => $r['user_jabatan'],
                                                'user_hp'               => $r['user_hp'],
                                                'user_lat'              => $user_lat,
                								'user_long' 			=> $user_long,
                								'user_cansignapproval' 	=> $r['user_cansignapproval'], 
                								'cabang_nomor' 			=> $r['cabang_nomor'], 
                								'cabang_kode' 			=> $r['cabang_kode'],
                								'cabang_nama' 			=> $r['cabang_nama'],
                                                'interval'              => $interval,
                                                'radius'                => $radius,
												'hash' 					=> $r['hash']
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
	
	// --- POST Check Login --- //
	function checkLogin_post(){     
        $db_android = $this->load->database('db_android', TRUE);

        $data['data'] = array();

        $value = file_get_contents('php://input');
		$jsonObject = (json_decode($value , true));

        $hash = (isset($jsonObject["hash"]) ? $this->clean($jsonObject["hash"])     : "");
		
        $query = "	SELECT 
						vcUserID AS user_id
					FROM muser_android
					WHERE REPLACE(vcHash, '-', '') = ?";
        $result = $this->db->query($query, array($hash));

        if( $result && $result->num_rows() > 0)
		{
            array_push($data['data'], array( 'success' => "true" ));
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


    function getAllSetting_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $db_android = $this->load->database('db_android', TRUE);

        $interval  = $db_android->query("SELECT a.intNomor, a.vcNama, a.intNilai FROM tsetting a WHERE a.intStatus > 0 AND a.vcNama = 'interval'")->row()->intNilai;
        $radius    = $db_android->query("SELECT a.intNomor, a.vcNama, a.intNilai FROM tsetting a WHERE a.intStatus > 0 AND a.vcNama = 'radius'")->row()->intNilai;
        $tracking  = $db_android->query("SELECT a.intNomor, a.vcNama, a.intNilai FROM tsetting a WHERE a.intStatus > 0 AND a.vcNama = 'tracking'")->row()->intNilai;
        $latitude  = $db_android->query("SELECT a.intNomor, a.vcNama, a.intNilai FROM tsetting a WHERE a.intStatus > 0 AND a.vcNama = 'latitude'")->row()->intNilai;
        $longitude = $db_android->query("SELECT a.intNomor, a.vcNama, a.intNilai FROM tsetting a WHERE a.intStatus > 0 AND a.vcNama = 'longitude'")->row()->intNilai;
        $jam_awal = $db_android->query("SELECT a.intNomor, a.vcNama, a.intNilai FROM tsetting a WHERE a.intStatus > 0 AND a.vcNama = 'jam_awal'")->row()->intNilai;
        $jam_akhir = $db_android->query("SELECT a.intNomor, a.vcNama, a.intNilai FROM tsetting a WHERE a.intStatus > 0 AND a.vcNama = 'jam_akhir'")->row()->intNilai;
        
        array_push($data['data'], array( 
    									'interval' 	=> $interval, 
    									'radius'	=> $radius, 
    									'tracking' 	=> $tracking, 
    									'latitude' 	=> $latitude, 
    									'longitude' => $longitude,  
    									'jam_awal' 	=> $jam_awal,  
    									'jam_akhir' => $jam_akhir  
        								)
        );

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    
    function updateSetting_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $interval  = (isset($jsonObject["interval"])  ? $this->clean($jsonObject["interval"])  : "");
        $radius    = (isset($jsonObject["radius"])    ? $this->clean($jsonObject["radius"])    : "");
        $tracking  = (isset($jsonObject["tracking"])  ? $this->clean($jsonObject["tracking"])  : "");
        $jam_awal  = (isset($jsonObject["jam_awal"])  ? $jsonObject["jam_awal"]  : "");
        $jam_akhir = (isset($jsonObject["jam_akhir"]) ? $jsonObject["jam_akhir"] : "");

        $db_android = $this->load->database('db_android', TRUE);
        
        $db_android->trans_begin();

        $db_android->query("UPDATE tsetting SET intNilai = '$interval' WHERE vcNama = 'interval'");
        $db_android->query("UPDATE tsetting SET intNilai = '$radius' WHERE vcNama = 'radius'");
        $db_android->query("UPDATE tsetting SET intNilai = '$tracking' WHERE vcNama = 'tracking'");
        $db_android->query("UPDATE tsetting SET intNilai = '$jam_awal' WHERE vcNama = 'jam_awal'");
        $db_android->query("UPDATE tsetting SET intNilai = '$jam_akhir' WHERE vcNama = 'jam_akhir'");

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



    function updatePassword_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $resultValue = "0";

        $user_nomor   = (isset($jsonObject["user_nomor"])  ? $this->clean($jsonObject["user_nomor"])  : "");
        $old_password = md5((isset($jsonObject["old_password"]) ? $jsonObject["old_password"] : ""));
        $new_password = md5((isset($jsonObject["new_password"]) ? $jsonObject["new_password"] : ""));

        $db_android = $this->load->database('db_android', TRUE);
        
        $this->db->trans_begin();

        $query = "	SELECT a.intNomor 
					FROM muser_android a 
					WHERE a.intStatus > 0 
					AND a.intNomorMUser = ? AND BINARY a.vcMD5Password = ?";
        $result = $this->db->query($query, array($user_nomor, $old_password));

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
        		$this->db->query("UPDATE muser_android SET vcMD5Password = '$new_password' WHERE intNomorMUser = $user_nomor");
            }
        }else{		
			$resultValue = "3";
		}  

        if ($this->db->trans_status() === FALSE){
            $this->db->trans_rollback();
            $resultValue = "2";
        }else{
            $this->db->trans_commit();
            if($resultValue != "3"){
            	$resultValue = "1";	
            }
        }

        array_push($data['data'], array( 'success' => $resultValue ));

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    function resetPassword_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $resultValue = "0";

        $user_nomor   = (isset($jsonObject["user_nomor"])  ? $this->clean($jsonObject["user_nomor"])  : "");
        $new_password = md5((isset($jsonObject["new_password"]) ? $jsonObject["new_password"] : "admin"));

        $this->db->trans_begin();

        $this->db->query("UPDATE muser_android SET vcMD5Password = '$new_password' WHERE intNomorMUser = $user_nomor");

        if ($this->db->trans_status() === FALSE){
            $this->db->trans_rollback();
            array_push($data['data'], array( 'success' => "false" ));
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
