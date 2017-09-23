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
class Salestracking extends REST_Controller { 

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
	function sendLocationBEX_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
		$jsonObject = (json_decode($value , true));

        $user_nomor = (isset($jsonObject["user_nomor"]) ? $this->clean($jsonObject["user_nomor"])   : "");
        $latitude   = (isset($jsonObject["latitude"])   ? $jsonObject["latitude"]                   : "");
        $longitude  = (isset($jsonObject["longitude"])  ? $jsonObject["longitude"]                  : "");

        $db_android = $this->load->database('db_android', TRUE);

        $db_android->trans_begin();
        
        $radius   = $db_android->query("SELECT a.intNomor, a.vcNama, a.intNilai FROM tsetting a WHERE a.intStatus > 0 AND a.vcNama = 'radius'")->row()->intNilai;

        $query = "  SELECT distance 
                    FROM( 
                        SELECT a.*, 
                        ACOS( SIN( RADIANS( a.decLatitude ) ) * SIN( RADIANS( $latitude ) ) + COS( RADIANS( a.decLatitude ) ) 
                        * COS( RADIANS( $latitude )) * COS( RADIANS( a.decLongitude ) - RADIANS( $longitude )) ) * 6380 AS `distance` 
                        FROM ttracking a 
                        WHERE a.intStatus = 1 AND a.intNomorMUser = $user_nomor 
                        ORDER BY a.dtInsertDate DESC 
                        LIMIT 1 
                         ) b 
                    WHERE b.distance >= " . ( $radius / 1000 );
        $result = $db_android->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                $query = $db_android->insert_string('ttracking', array(
                                                                      'intNomorMUser'=>$user_nomor, 
                                                                      'decLatitude'=>$latitude, 
                                                                      'decLongitude'=>$longitude, 
                                                                      'intNilaiRadius'=>$radius
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
            }
        }else{      
            $querynow  = "  SELECT * 
                            FROM ttracking a 
                            WHERE a.intStatus > 0 AND a.intNomorMUser = $user_nomor ";
            $resultnow = $db_android->query($querynow);
            if( $resultnow && $resultnow->num_rows() > 0){
                array_push($data['data'], array( 'query' => "In Radius" ));
            }else{

                $query = $db_android->insert_string('ttracking', array(
                                                                      'intNomorMUser'=>$user_nomor, 
                                                                      'decLatitude'=>$latitude, 
                                                                      'decLongitude'=>$longitude, 
                                                                      'intNilaiRadius'=>$radius
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
            }
        }       

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }else{
            array_push($data['data'], array( 'user_nomor' => $user_nomor, 'latitude' => $latitude, 'longitude' => $longitude ));
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    // --- GET Message Group --- //
    function getLocationBEX_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $user_nomor = (isset($jsonObject["user_nomor"]) ? $this->clean($jsonObject["user_nomor"]) 	: "");
        if($user_nomor != ""){ $user_nomor = " AND intNomorMUser = $user_nomor "; }

        $range_date = "";
        $start_date = (isset($jsonObject["start_date"]) ? $this->clean($jsonObject["start_date"]) 	: "");
        $end_date   = (isset($jsonObject["end_date"]) 	? $this->clean($jsonObject["end_date"]) 	: "");
        if($start_date != "" && $end_date == ""){ 
            $tommorow = $tomorrow = date('Y-m-d', strtotime($start_date) + 86400);
        	$range_date = " AND dtInsertDate >= '$start_date' AND dtInsertDate <= '$tomorrow' "; 
        }
        if($start_date != "" && $end_date != ""){ $range_date = " AND dtInsertDate >= '$start_date' AND dtInsertDate <= '$end_date' "; }

        $db_android = $this->load->database('db_android', TRUE);

        $query = "  SELECT 
                        a.intNomorMUser, 
                        a.declatitude, 
                        a.declongitude,
                        a.dtInsertDate
                    FROM ttracking a 
                    WHERE a.intStatus = 1 $user_nomor $range_date 
                    ORDER BY a.dtInsertDate";
        $result = $db_android->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'intNomorMUser' => $r['intNomorMUser'], 
                                                'declatitude'   => $r['declatitude'], 
                                                'declongitude'  => $r['declongitude'], 
                                                'dtInsertDate'  => $r['dtInsertDate']
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
