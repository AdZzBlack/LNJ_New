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
class Omzettarget extends REST_Controller { 

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
    }

    function getOmzetSales_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $user_nomor  = (isset($jsonObject["user_nomor"])  ? $this->clean($jsonObject["user_nomor"])  : "2");
        $bex_nomor   = (isset($jsonObject["bex_nomor"])   ? $this->clean($jsonObject["bex_nomor"])   : "%");
        // $tgl_awal    = (isset($jsonObject["tgl_awal"])    ? $this->clean($jsonObject["tgl_awal"])    : strtotime(date("Y-m-d").' -1 months'));
        $tgl_awal    = (isset($jsonObject["tgl_awal"])    ? $this->clean($jsonObject["tgl_awal"])    : date('Y-m-01'));
        $tgl_akhir   = (isset($jsonObject["tgl_akhir"])   ? $this->clean($jsonObject["tgl_akhir"])   : date("Y-m-d"));

        if($user_nomor == ""){ $user_nomor = "2"; }
        if($tgl_awal == ""){ $tgl_awal = strtotime(date("Y-m-d").' -1 months'); }
        if($tgl_akhir == ""){ $tgl_akhir = date("Y-m-d"); }

        $area_nomor  = (isset($jsonObject["area_nomor"])  ? $this->clean($jsonObject["area_nomor"])  : "");
        if($area_nomor != ""){ $area_nomor = " AND a.intNomor = $area_nomor "; }
        
        $brand_nomor = (isset($jsonObject["brand_nomor"]) ? $this->clean($jsonObject["brand_nomor"]) : "");
        if($brand_nomor != ""){ $brand_nomor = " AND a.intNomor = $brand_nomor "; }

        if($bex_nomor != "%"){
			$bex_kode   = $this->db->query("SELECT vcKode FROM msales where intNomor ='". $bex_nomor."'")->row()->vcKode;
		}

        $area_kode  = "%"; 
        if($area_nomor != ""){
            $area_kode  = $this->db->query("SELECT vcNama FROM marea where intNomor ='". $area_nomor."'")->row()->vcNama;  
        }
        $brand_kode = "%"; 
        if($brand_nomor != ""){
            $brand_kode = $this->db->query("SELECT vcNama FROM mbrand where intNomor ='". $brand_nomor."'")->row()->vcNama;  
        }

        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "0");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

        $query = "CALL RP_Sales_ByBEX_20120618(2, '$bex_kode', '$area_kode', '$brand_kode', '$tgl_awal', '$tgl_akhir', $user_nomor);";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'vcNamaBrand'     => $r['vcNamaBrand'], 
                                                'vcNamaArea'      => $r['vcNamaArea'], 
                                                'vcNamaCustomer'  => $r['vcNamaCustomer'],
                                                'decDPP'          => $r['decDPP']
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

    function getOmzetSalesTotal_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $user_nomor  = (isset($jsonObject["user_nomor"])  ? $this->clean($jsonObject["user_nomor"])  : "2");
        $bex_nomor   = (isset($jsonObject["bex_nomor"])   ? $this->clean($jsonObject["bex_nomor"])   : "%");
        // $tgl_awal    = (isset($jsonObject["tgl_awal"])    ? $this->clean($jsonObject["tgl_awal"])    : date('Y-m-d', strtotime(date("Y-m-d").' -1 months') ) );
        $tgl_awal    = (isset($jsonObject["tgl_awal"])    ? $this->clean($jsonObject["tgl_awal"])    : date('Y-m-01'));
        $tgl_akhir   = (isset($jsonObject["tgl_akhir"])   ? $this->clean($jsonObject["tgl_akhir"])   : date("Y-m-d"));
        $reminder    = (isset($jsonObject["reminder"])    ? $this->clean($jsonObject["reminder"])    : "false");

        if($user_nomor == ""){ $user_nomor = "2"; }
        if($tgl_awal == ""){ $tgl_awal = date('Y-m-d', strtotime(date("Y-m-d").' -1 months') ); }
        if($tgl_akhir == ""){ $tgl_akhir = date("Y-m-d"); }

        $area_nomor  = (isset($jsonObject["area_nomor"])  ? $this->clean($jsonObject["area_nomor"])  : "");
        if($area_nomor != ""){ $area_nomor = " AND a.intNomor = $area_nomor "; }
        
        $brand_nomor = (isset($jsonObject["brand_nomor"]) ? $this->clean($jsonObject["brand_nomor"]) : "");
        if($brand_nomor != ""){ $brand_nomor = " AND a.intNomor = $brand_nomor "; }

		
        if($bex_nomor != "%"){
			$bex_kode   = $this->db->query("SELECT vcKode FROM msales where intNomor ='". $bex_nomor."'")->row()->vcKode;
			$vcGCMId    = $this->getGCMId($bex_nomor);
		}

        $area_kode  = "%"; 
        if($area_nomor != ""){
            $area_kode  = $this->db->query("SELECT vcNama FROM marea where intNomor ='". $area_nomor."'")->row()->vcNama;  
        }
        $brand_kode = "%"; 
        if($brand_nomor != ""){
            $brand_kode = $this->db->query("SELECT vcNama FROM mbrand where intNomor ='". $brand_nomor."'")->row()->vcNama;  
        }

        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "0");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

        $total_omzet = 0;

        $query = "CALL RP_Sales_ByBEX_20120618(2, '$bex_kode', '$area_kode', '$brand_kode', '$tgl_awal', '$tgl_akhir', $user_nomor);";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                $total_omzet += floatval($r['decDPP']);
            }
            array_push($data['data'], array( 'total_omzet' => $total_omzet ));
        }else{      
            array_push($data['data'], array( 'total_omzet' => $total_omzet ));
        }  

        if($reminder == "true"){
			
			if($bex_nomor != ""){ $bex_nomor = " AND b.intNomorMSales = $bex_nomor "; }

			$query = "  SELECT a.intPeriode, a.intTahun, SUM(a.decTarget) AS decTarget
						FROM thtarget a 
						JOIN muser_android b ON a.intNomorMSales = b.intNomorMSales $bex_nomor 
						WHERE a.intStatus = 1 
								AND a.intPeriode = MONTH(CURDATE()) 
								AND a.intTahun = YEAR(CURDATE()) 
						GROUP BY a.intPeriode, a.intTahun ";
			$result = $this->db->query($query);

			$total_target = 0;
			
			if( $result && $result->num_rows() > 0){
				foreach ($result->result_array() as $r){
					$total_target += floatval($r['decTarget']);
				}
			}
			
            if( $vcGCMId != "null" ){      
                $this->send_gcm($vcGCMId, $this->ellipsis("Reminder Omset"),'Omset : Rp.' . number_format($total_omzet,2) . ' | Target " Rp.' . number_format($total_target,2),'Dashboard',$bex_nomor,"tes");   
            }
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

}
