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
class Report extends REST_Controller {

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

    // --- Report Live Tracking--- //
    function GetReportLiveTracking_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $driver = (isset($jsonObject["driver"]) ? $this->clean($jsonObject["driver"])     : "%");
        $job_nomor = (isset($jsonObject["job_nomor"]) ? $this->clean($jsonObject["job_nomor"])     : "%");
        $startdate = (isset($jsonObject["startdate"]) ? $this->clean($jsonObject["startdate"])     : "2000-01-01");
        $enddate = (isset($jsonObject["enddate"]) ? $this->clean($jsonObject["enddate"])     : "3000-01-01");

        if($driver=="") $driver = "%";
        if($job_nomor=="") $job_nomor = "%";
        if($startdate=="") $startdate = "2000-01-01";
        if($enddate=="") $enddate = "3000-01-01";

        $query = "CALL RP_HISTORY_TRACKING('$job_nomor', '$driver', '$startdate', '$enddate') ";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                                                'job'					  => $r['job'],
                                                'tgldelivery'             => $r['tgldelivery'],
                                                'vehicleid'			      => $r['vehicleid'],
                                                'kodecontainer'           => $r['kodecontainer'],
                                                'weight'                  => $r['weight'],
                                                'driver'                  => $r['driver'],
                                                'latitude'                => $r['latitude'],
                                                'longitude'               => $r['longitude'],
                                                'datetracking'            => $r['datetracking']
                                                )
                );
            }
        }else{
            array_push($data['data'], array( 'error' => $this->error($query) ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    // --- Report Deviation--- //
    function GetReportDeviationTracking_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $driver = (isset($jsonObject["driver"]) ? $this->clean($jsonObject["driver"])     : "%");
        $job_nomor = (isset($jsonObject["job_nomor"]) ? $this->clean($jsonObject["job_nomor"])     : "%");
        $startdate = (isset($jsonObject["startdate"]) ? $this->clean($jsonObject["startdate"])     : "2000-01-01");
        $enddate = (isset($jsonObject["enddate"]) ? $this->clean($jsonObject["enddate"])     : "3000-01-01");

        if($driver=="") $driver = "%";
        if($job_nomor=="") $job_nomor = "%";
        if($startdate=="") $startdate = "2000-01-01";
        if($enddate=="") $enddate = "3000-01-01";

        $query = "CALL RP_DEVIATION_TRACKING('$job_nomor', '$driver', '$startdate', '$enddate') ";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                                                'job'					  => $r['job'],
                                                'tgldelivery'             => $r['tgldelivery'],
                                                'vehicleid'			      => $r['vehicleid'],
                                                'kodecontainer'           => $r['kodecontainer'],
                                                'weight'                  => $r['weight'],
                                                'driver'                  => $r['driver'],
                                                'latitude'                => $r['latitude'],
                                                'longitude'               => $r['longitude'],
                                                'cp_latitude'             => $r['cp_latitude'],
                                                'cp_longitude'            => $r['cp_longitude'],
                                                'radius_km'               => $r['radius(km)'],
                                                'distance_km'             => $r['distance(km)'],
                                                'datetracking'            => $r['datetracking']
                                                )
                );
            }
        }else{
            array_push($data['data'], array( 'error' => $this->error($query) ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    // --- Report Document Distribution--- //
    function GetReportDocDistribution_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $nomormhadmin_from = (isset($jsonObject["nomormhadmin_from"]) ? $this->clean($jsonObject["nomormhadmin_from"])     : "%");
        $nomormhadmin_to = (isset($jsonObject["nomormhadmin_to"]) ? $this->clean($jsonObject["nomormhadmin_to"])     : "%");
        $action = (isset($jsonObject["action"]) ? $this->clean($jsonObject["action"])     : "%");
        $startdate = (isset($jsonObject["startdate"]) ? $this->clean($jsonObject["startdate"])     : "2000-01-01");
        $enddate = (isset($jsonObject["enddate"]) ? $this->clean($jsonObject["enddate"])     : "3000-01-01");
        $nomorcabang = (isset($jsonObject["nomorcabang"]) ? $this->clean($jsonObject["nomorcabang"])     : "0");
        $nomormhadmin = (isset($jsonObject["nomormhadmin"]) ? $this->clean($jsonObject["nomormhadmin"])     : "%");

        if($nomormhadmin_from == "") $nomormhadmin_from = "%";
        if($nomormhadmin_to == "") $nomormhadmin_to = "%";
        if($action == "") $action = "%";
        if($startdate == "") $startdate = "2000-01-01";
        if($enddate == "") $enddate = "3000-01-01";
        if($nomorcabang == "") $nomorcabang = "0";
        if($nomormhadmin == "") $nomormhadmin = "%";

        $query = "CALL RP_DOC_DIST_20180210('$nomormhadmin_from', '$nomormhadmin_to', '$action', '$startdate', '$enddate', '$nomorcabang', '$nomormhadmin') ";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                                                'kode'					  => $r['kode'],
                                                'job'					  => $r['job'],
                                                'ref'					  => $r['ref'],
                                                'nama_from'			      => $r['nama_from'],
                                                'tanggal'				  => $r['tanggal'],
                                                'action'				  => $r['action'],
                                                'nama_to'			      => $r['nama_to'],
                                                'keterangan'              => $r['keterangan']
                                                )
                );
            }
        }else{
            array_push($data['data'], array( 'error' => $this->error($query) ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }
}