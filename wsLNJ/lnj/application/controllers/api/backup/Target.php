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
class Target extends REST_Controller { 

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

    function getMaxID($primerykey,$table,$database){
        if($database == "android"){
            $db_android = $this->load->database('db_android', TRUE);
            $db_android->select_max($primerykey);
            $result = $db_android->get($table)->row_array();
            return $result[$primerykey];
        }else{
            $this->db->select_max($primerykey);
            $result = $this->db->get($table)->row_array();
            return $result[$primerykey];
        }
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
        return str_replace( array("\t", "\n", "\r") , "", $string);
    }

    function getGCMId($user_nomor){
        $query = "  SELECT 
                    a.intNomorMUser, 
                    a.vcGCMId 
                    FROM muser_android a 
                    WHERE a.intStatus > 0 AND (a.vcGCMId <> '' AND a.vcGCMId IS NOT NULL) AND a.intNomorMSales = $user_nomor ";
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

        /*if ($this->gcm->send())
            echo 'Success for all messages';
        else
            echo 'Some messages have errors';

        print_r($this->gcm->status);
        print_r($this->gcm->messagesStatuses);

        die(' Worked.');*/
    }

    function getAllPeriode_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $periode = (isset($jsonObject["periode"]) ? $this->clean($jsonObject["periode"]) : "");
        if($periode != ""){ $periode = " AND a.intPeriode = $periode "; }

        $tahun = (isset($jsonObject["tahun"]) ? $this->clean($jsonObject["tahun"]) : "");
        if($tahun != ""){ $tahun = " AND a.intTahun = $tahun "; }

        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

        $query = "  SELECT DISTINCT a.intPeriode, a.intTahun 
                    FROM thtarget a 
                    WHERE a.intStatus = 1 $periode $tahun 
                        AND STR_TO_DATE(CONCAT(intTahun,'-',intPeriode), '%Y-%m') >= STR_TO_DATE(CONCAT(YEAR(CURDATE()),'-',MONTH(CURDATE())), '%Y-%m') 
                    ORDER BY intTahun, intPeriode 
                    $limit ";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'intPeriode' => $r['intPeriode'], 
                                                'intTahun'   => $r['intTahun']
                                                )
                );
            }
        }else{      
            // array_push($data['data'], array( 'success' => "false" ));
        }  

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }

    }

    function getAllTarget_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $periode = (isset($jsonObject["periode"]) ? $this->clean($jsonObject["periode"]) : "");
        if($periode != ""){ $periode = " AND a.intPeriode = $periode "; }

        $tahun = (isset($jsonObject["tahun"]) ? $this->clean($jsonObject["tahun"]) : "");
        if($tahun != ""){ $tahun = " AND a.intTahun = $tahun "; }

        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

        $query = "  SELECT a.*, b.vcNama  
                    FROM thtarget a 
                    JOIN msales b ON a.intNomorMSales = b.intNomor  
                    WHERE a.intStatus = 1 $periode $tahun 
                        AND STR_TO_DATE(CONCAT(intTahun,'-',intPeriode), '%Y-%m') >= STR_TO_DATE(CONCAT(YEAR(CURDATE()),'-',MONTH(CURDATE())), '%Y-%m') 
                    ORDER BY intTahun, intPeriode 
                    $limit ";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'intNomor'   => $r['intNomor'], 
                                                'vcNama'     => $r['vcNama'], 
                                                'intPeriode' => $r['intPeriode'], 
                                                'intTahun'   => $r['intTahun'], 
                                                'decTarget'  => $r['decTarget']
                                                )
                );
            }
        }else{      
            // array_push($data['data'], array( 'success' => "false" ));
        }  

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }

    }

	// --- POST Customer --- //
    function getDataTarget_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $bex_nomor = (isset($jsonObject["bex_nomor"]) ? $this->clean($jsonObject["bex_nomor"]) : "");
        if($bex_nomor != ""){ $bex_nomor = " AND b.intNomorMSales = $bex_nomor "; }

        $target_nomor = (isset($jsonObject["target_nomor"]) ? $this->clean($jsonObject["target_nomor"]) : "");
        if($target_nomor != ""){ $target_nomor = " AND a.intNomor = $target_nomor "; }

        $query = "  SELECT a.*, c.vcNama  
                    FROM thtarget a 
                    JOIN muser_android b ON a.intNomorMSales = b.intNomorMSales $bex_nomor
                    JOIN msales c ON a.intNomorMSales = c.intNomor  
                    WHERE a.intStatus = 1  $target_nomor ";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'vcNama'     => $r['vcNama'], 
                                                'intPeriode' => $r['intPeriode'], 
                                                'intTahun'   => $r['intTahun'], 
                                                'decTarget'  => $r['decTarget']
                                                )
                );
            }
        }else{      
            array_push($data['data'], array( 'success' => "false" ));
        }  

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }

    }

    function getDataTargetThisMonth_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $bex_nomor = (isset($jsonObject["bex_nomor"]) ? $this->clean($jsonObject["bex_nomor"]) : "");
        if($bex_nomor != ""){ $bex_nomor = " AND b.intNomorMSales = $bex_nomor "; }

        $query = "  SELECT a.intPeriode, a.intTahun, SUM(a.decTarget) AS decTarget
                    FROM thtarget a 
                    JOIN muser_android b ON a.intNomorMSales = b.intNomorMSales $bex_nomor 
                    WHERE a.intStatus = 1 
                            AND a.intPeriode = MONTH(CURDATE()) 
                            AND a.intTahun = YEAR(CURDATE()) 
                    GROUP BY a.intPeriode, a.intTahun ";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array( 'decTarget'  => $r['decTarget'] ));
            }
        }else{      
            array_push($data['data'], array( 'decTarget' => "0" ));
        }  

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }

    }

     // --- POST Login --- //
    function createTarget_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $user_nomor  = (isset($jsonObject["user_nomor"])  ? $this->clean($jsonObject["user_nomor"])  : "");
        $bulan  = (isset($jsonObject["bulan"])  ? $this->clean($jsonObject["bulan"])  : "");
        $tahun  = (isset($jsonObject["tahun"])  ? $this->clean($jsonObject["tahun"])  : "");
        $datalisttarget = (isset($jsonObject["datalisttarget"]) ? $jsonObject["datalisttarget"] : "");

        $monthName = date("F", mktime(0, 0, 0, $bulan, 10));

        $user_nama = $this->db->query("SELECT vcNama FROM muser where intNomor ='". $user_nomor."'")->row()->vcNama;  

        $regisID = array();
        $regisID_Header = array();
        $regisID_Keterangan = array();

        $resultdata_value = "1";

        $this->db->trans_begin();

        if($datalisttarget != ""){
            $pieces = explode("|", $datalisttarget);
            foreach ($pieces as $arr) {
                $valuedata = explode("~", $arr);

                if( $valuedata[0] != ""){

                    $valuedata[0] = $this->db->query("SELECT intNomorMSales FROM muser_android where intNomorMUser ='". $valuedata[0]."'")->row()->intNomorMSales;  

                    $bex_nomor = $this->db->query(" SELECT IFNULL(intNomorMSales, 0) AS intNomorMSales
                                                    FROM ( 
                                                        SELECT a.intNomorMSales
                                                        FROM thtarget a 
                                                        JOIN muser_android b ON a.intNomorMSales = b.intNomorMSales 
                                                        WHERE a.intStatus = 1 
                                                                AND a.intNomorMSales = $valuedata[0]  
                                                                AND a.intPeriode = $bulan 
                                                                AND a.intTahun = $tahun 
                                                    ) a "
                                                )->row()->intNomorMSales; 

                    if($bex_nomor == $valuedata[0]){

                        // $resultdata_value = "3";

                    }else{

                        $query_detail_target = $this->db->insert_string('thtarget', array(
                                                                                          'intNomorMSales'  => $valuedata[0], 
                                                                                          'intPeriode'      => $bulan, 
                                                                                          'intTahun'        => $tahun, 
                                                                                          'decTarget'       => $valuedata[2], 
                                                                                          'intInsertUserID' => $user_nomor
                                                                                        )
                                                                        );
                        $this->db->query($query_detail_target);
                        // array_push($data['data'], array( 'success' => $query_detail_barang ));

                        $header_nomor = $this->getMaxID("intNomor","thtarget","default");  
                        array_push($regisID_Header, $header_nomor);  

                        array_push($regisID_Keterangan, $monthName . " " . $tahun . " : Rp." . number_format($valuedata[2],2)); 

                        $vcGCMId = $this->getGCMId($valuedata[0]);   
                        array_push($regisID, $vcGCMId);        

                    }
                }
            }
        }

        if ($this->db->trans_status() === FALSE){
            $this->db->trans_rollback();
            array_push($data['data'], array( 'success' => "false" ));
        }else{
            $this->db->trans_commit();
            array_push($data['data'], array( 'success' => "true" ));

            for($i = 0; $i < count($regisID); $i++){

                if($regisID[$i] != null){
                    // START SEND NOTIFICATION
                    // array_push($data['data'], array( '$regisID[$i]' => $regisID[$i] ));
                    $this->send_gcm($regisID[$i], $this->ellipsis($regisID_Keterangan[$i]),'New Target From : ' . $user_nama,'Dashboard',$regisID_Header[$i],$user_nama);   
                }
                
            }
        }  

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }

    }

    function updateTarget_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $resultValue = "0";

        $target_nomor  = (isset($jsonObject["target_nomor"])  ? $this->clean($jsonObject["target_nomor"])  : "");
        $decTarget     = (isset($jsonObject["decTarget"])     ? $jsonObject["decTarget"]                   : "");

        $this->db->trans_begin();

        $this->db->query("UPDATE thtarget SET decTarget = $decTarget WHERE intNomor = $target_nomor");

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
