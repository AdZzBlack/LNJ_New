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
class Fakegps extends REST_Controller { 

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

	// --- Send Message Group --- //
	function createFakegpsReport_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
		$jsonObject = (json_decode($value , true));

        $user_nomor = (isset($jsonObject["user_nomor"])  ? $this->clean($jsonObject["user_nomor"]) : "");

        $db_android = $this->load->database('db_android', TRUE);

        $db_android->trans_begin();

        $query = $db_android->insert_string('tfakegps', array(
                                                              'intNomorMUser' => $user_nomor
                                                            )
                                        );
        $db_android->query($query);

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

    // --- GET Message Group --- //
    function getFakegps_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $user_nomor = (isset($jsonObject["user_nomor"])  ? $this->clean($jsonObject["user_nomor"]) : "");
        if($user_nomor != ""){ $user_nomor = " AND intNomorMUser = $user_nomor "; }

        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

        $db_android = $this->load->database('db_android', TRUE);

        $query = "  SELECT 
                    a.intNomorMUser AS user_nomor,
                    a.dtTanggal
                    FROM tfakegps a
                    WHERE a.intStatus = 1 $user_nomor
                    ORDER BY a.dtInsertDate 
                    $limit";
        $result = $db_android->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                $user_nama = $this->db->query("SELECT vcNama FROM muser where intNomor ='". $r['user_nomor']."'")->row()->vcNama;
                array_push($data['data'], array(
                                                'user_nomor'  => $r['user_nomor'], 
                                                'dtTanggal'   => $r['dtTanggal'], 
                                                'user_nama'   => $user_nama
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
