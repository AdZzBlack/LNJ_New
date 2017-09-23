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
class GCM extends REST_Controller { 

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
	function updateGCM_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
		$jsonObject = (json_decode($value , true));

        $user_nomor = (isset($jsonObject["user_nomor"]) ? $this->clean($jsonObject["user_nomor"]) : "1");
        $gcmid      = (isset($jsonObject["gcmid"])      ? $jsonObject["gcmid"]                    : "f_Y0uVfgmQQ:APA91bH0jNhTBwCNqReZtaGu8TdF3ZX92l2Lj6cn3-qHxzJJI4S6phj-MMH31E2F9cv6uAYmJJmmEIA2wwWJTVA__qg8GrPuvj_aIcvoZcFWOTvosKZtYidHCAJfhmeoPMskC31PVkXe");

        $data  = array( 'gcmid'       => $gcmid );
        $where = array( 'nomor' 	  => $user_nomor );

        $this->db->update('whuser_mobile', $data, $where);
    }
}
