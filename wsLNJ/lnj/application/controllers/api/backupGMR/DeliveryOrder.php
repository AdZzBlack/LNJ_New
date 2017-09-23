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
class DeliveryOrder extends REST_Controller { 

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
        return str_replace( array("\t", "\n", "\r") , "", $string);
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

		/*
        if ($this->gcm->send())
            echo 'Success for all messages';
        else
            echo 'Some messages have errors';

        print_r($this->gcm->status);
        print_r($this->gcm->messagesStatuses);

        die(' Worked.');
		*/
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
	
	function test_get()
	{
		
		$regisID = array();
				
				$query_getuser = " SELECT 
									a.gcmid
									FROM whuser_mobile a 
									JOIN whrole_mobile b ON a.nomorrole = b.nomor
									WHERE a.status_aktif > 0 AND (a.gcmid <> '' AND a.gcmid IS NOT NULL) AND b.approvedeliveryorder = 1 ";
				$result_getuser = $this->db->query($query_getuser);

				if( $result_getuser && $result_getuser->num_rows() > 0){
					foreach ($result_getuser->result_array() as $r_user){

						// START SEND NOTIFICATION
						$vcGCMId = $r_user['gcmid'];
						if( $vcGCMId != "null" ){      
							array_push($regisID, $vcGCMId);       
						}
						
					}
					$count = $this->db->query("SELECT COUNT(1) AS order_baru FROM thdeliveryorder a WHERE a.status_disetujui = 0")->row()->order_baru; 
					$this->send_gcm_group($regisID, $this->ellipsis($count . ' pending order'),'Delivery Order','ChooseApprovalDelivery','','');
				} 
	}

    function alldatarabdetail_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $intNomorMRAB = (isset($jsonObject["nomor_rab"]) ? $this->clean($jsonObject["nomor_rab"]) : "");
        if($intNomorMRAB != ""){ $intNomorMRAB = " AND a.nomormdrab = " . $intNomorMRAB; }
		
        $search = (isset($jsonObject["search"]) ? $this->clean($jsonObject["search"]) : "");
        if($search != ""){ $search = " AND (b.nama  LIKE '%$search%') "; }

		$query = "  SELECT  
						a.nomor AS nomor,
                        b.nomor AS nomorbarang,
						b.nama  AS namabarang,
						c.nama  AS satuan,
						a.jumlah AS jumlah,
						f.harga AS harga,
						a.jumlahterorder AS do,
						a.perkiraanpersenwaste AS waste
                    FROM mdrabdetail a
					JOIN mhbarang b ON a.nomormhbarang = b.nomor
					JOIN mhsatuan c ON b.nomormhsatuan = c.nomor
					JOIN mdpekerjaan f ON a.nomormhpekerjaan = f.nomormhpekerjaan AND b.nomor = f.nomormhbarang
					WHERE 1 = 1 
						AND b.status_aktif = 1
					$intNomorMRAB $search";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
				array_push($data['data'], array(
												'nomor'    		=> $r['nomor'], 
												'nomorbarang'   => $r['nomorbarang'],
												'namabarang'    => $r['namabarang'],
												'satuan'       	=> $r['satuan'],
												'jumlah'       	=> $r['jumlah'],
												'harga'       	=> $r['harga'],
												'do'       		=> $r['do'],
												'waste'       		=> $r['waste'],
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
	
	function createDeliveryOrder_post(){     
		$data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
		
		$userNomor          = (isset($jsonObject["userNomor"])  	? $jsonObject["userNomor"]                     : "");
		$tanggal            = (isset($jsonObject["tanggal"]) 		? $jsonObject["tanggal"]                       : "");
		$bangunan_nomor     = (isset($jsonObject["nomor_bangunan"]) ? $jsonObject["nomor_bangunan"]                : "");
		$dataDO             = (isset($jsonObject["dataDO"])  		? $jsonObject["dataDO"]                        : "");
		
		$this->db->trans_begin();
		
		$query = "INSERT INTO thdeliveryorder(`nomormhbangunan`, `dibuat_pada`, `dibuat_oleh`, `kode`) VALUES($bangunan_nomor, '$tanggal', $userNomor, FC_GENERATE_DELIVERY_ORDER_KODE())";
		
        $this->db->query($query);

        $header_nomor = $this->db->insert_id();
		
		$approve = 1;
		
		if($dataDO != "")
		{
			$pieces = explode("|", $dataDO);
			foreach ($pieces as $arr) {
                $valuedata = explode("~", $arr);

                if( $valuedata[0] != ""){
					
					if($valuedata[5]==0) $approve = 0;
					
					$catatan = "";
					if($valuedata[7]!="0") $catatan = $valuedata[7];
					
                    $query_detail_do = $this->db->insert_string('tddeliveryorder', array(
                                                                          'nomorthdeliveryorder'=>$header_nomor,
																		  'nomormdrabdetail'	=>$valuedata[6], 
                                                                          'nomormhbarang'     	=>$valuedata[0], 
                                                                          'jumlahorder' 		=>$valuedata[3], 
                                                                          'harga' 				=>$valuedata[4],
																		  'status_disetujui'	=>$valuedata[5],
																		  'dibuat_oleh'			=>$userNomor,
																		  'catatan'				=>$catatan
                                                                        )
                                                    );
                    $this->db->query($query_detail_do);
					
                }
            }
		}
		
		
		if ($this->db->trans_status() === FALSE){
            $this->db->trans_rollback();
            array_push($data['data'], array( 'failed' => $query ));
        }else{
            $this->db->trans_commit();
			
			$query = "SELECT kode FROM thdeliveryorder where nomor = $header_nomor";
			$result = $this->db->query($query)->row()->kode;
			
            array_push($data['data'], array( 
										'nomor'	=> $header_nomor,
										'success' => $this->clean($result)
								));
			
			if($approve==0)
			{
				$regisID = array();
				
				$query_getuser = " SELECT 
									a.gcmid
									FROM whuser_mobile a 
									JOIN whrole_mobile b ON a.nomorrole = b.nomor
									WHERE a.status_aktif > 0 AND (a.gcmid <> '' AND a.gcmid IS NOT NULL) AND b.approvedeliveryorder = 1 ";
				$result_getuser = $this->db->query($query_getuser);

				if( $result_getuser && $result_getuser->num_rows() > 0){
					foreach ($result_getuser->result_array() as $r_user){

						// START SEND NOTIFICATION
						$vcGCMId = $r_user['gcmid'];
						if( $vcGCMId != "null" ){      
							array_push($regisID, $vcGCMId);       
						}
						
					}
					$count = $this->db->query("SELECT COUNT(1) AS order_baru FROM thdeliveryorder a WHERE a.status_disetujui = 0")->row()->order_baru; 
					$this->send_gcm_group($regisID, $this->ellipsis($count . ' pending order'),'Delivery Order','ChooseApprovalDelivery','','');
				} 
			}
			
			$query1 = "CALL SP_CHECK_DELIVERY_ORDER($header_nomor, 0, 0)";
			$this->db->query($query1);
        } 
		
        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
	}
	
	function alldataneedprint_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
		
        $search = (isset($jsonObject["search"]) ? $this->clean($jsonObject["search"]) : "");
        if($search != ""){ $search = " AND (a.kode LIKE '%$search%') "; }

		$nomor = (isset($jsonObject["nomor"]) ? $this->clean($jsonObject["nomor"]) : "");
        if($nomor != ""){ $nomor = " AND (a.nomor = '$nomor') "; }
		
		$user_nomor = (isset($jsonObject["user_nomor"]) ? $this->clean($jsonObject["user_nomor"]) : "1");
        if($user_nomor != ""){ $user_nomor = " AND (a.dibuat_oleh = '$user_nomor') "; }
		
        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

		$query = "  SELECT  
                        a.nomor,
						a.kode
					FROM thdeliveryorder a
					WHERE 1 = 1
					AND a.status_print = 0
					AND a.status_disetujui = 1 $user_nomor $search $nomor $limit";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'nomor'    			=> $r['nomor'], 
												'nama'       		=> $r['kode']
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
	
	function alldatadetailneedprint_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

		$nomor = (isset($jsonObject["nomor"]) ? $this->clean($jsonObject["nomor"]) : "");
        if($nomor != ""){ $nomor = " AND (c.nomor = '$nomor') "; }

		$query = "  SELECT  
						e.nama AS item,
						a.jumlahorder AS jumlahorder,
						a.catatan AS catatan,
						f.nama AS satuan
					FROM tddeliveryorder a
					JOIN mdrabdetail b ON a.nomormdrabdetail = b.nomor
					JOIN thdeliveryorder c ON a.nomorthdeliveryorder = c.nomor
					JOIN mhbangunan_view d ON c.nomormhbangunan = d.nomor
					JOIN mhbarang e ON a.nomormhbarang = e.nomor
					JOIN mhsatuan f ON e.nomormhsatuan = f.nomor
					WHERE 1 = 1 $nomor";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
												'item'    				=> $r['item'], 
												'jumlahorder' 			=> $r['jumlahorder'],
												'catatan'    			=> $r['catatan'], 
												'satuan'  				=> $r['satuan']
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
	
	function alldataneededit_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
		
        $search = (isset($jsonObject["search"]) ? $this->clean($jsonObject["search"]) : "");
        if($search != ""){ $search = " AND (a.kode LIKE '%$search%') "; }

		$nomor = (isset($jsonObject["nomor"]) ? $this->clean($jsonObject["nomor"]) : "");
        if($nomor != ""){ $nomor = " AND (a.nomor = '$nomor') "; }
		
		$user_nomor = (isset($jsonObject["user_nomor"]) ? $this->clean($jsonObject["user_nomor"]) : "1");
        if($user_nomor != ""){ $user_nomor = " AND (a.dibuat_oleh = '$user_nomor') "; }
		
        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

		$query = "  SELECT  
                        a.nomor,
						a.kode
					FROM thdeliveryorder a
					WHERE 1 = 1
					AND a.status_disetujui = 2 $user_nomor $search $nomor $limit";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'nomor'    			=> $r['nomor'], 
												'nama'       		=> $r['kode']
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
	
	function alldatadetailneededit_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

		$nomor = (isset($jsonObject["nomor"]) ? $this->clean($jsonObject["nomor"]) : "");
        if($nomor != ""){ $nomor = " AND (c.nomor = '$nomor') "; }

		$query = "  SELECT  
                        a.nomor AS nomor,
						e.nama AS item,
						a.nomorthdeliveryorder AS nomorthdeliveryorder,
						b.perkiraanpersenwaste AS waste,
						b.jumlah AS jumlah,
						b.jumlahterorder AS do,
						a.jumlahorder AS jumlahorder,
						a.catatan AS catatan,
						d.namalengkap AS namalengkap,
						f.nama AS satuan
					FROM tddeliveryorder a
					JOIN mdrabdetail b ON a.nomormdrabdetail = b.nomor
					JOIN thdeliveryorder c ON a.nomorthdeliveryorder = c.nomor
					JOIN mhbangunan_view d ON c.nomormhbangunan = d.nomor
					JOIN mhbarang e ON a.nomormhbarang = e.nomor
					JOIN mhsatuan f ON e.nomormhsatuan = f.nomor
					WHERE 1 = 1
					AND a.status_disetujui = 2 $nomor";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'nomor'    				=> $r['nomor'], 
												'item'    				=> $r['item'], 
												'nomorthdeliveryorder'  => $r['nomorthdeliveryorder'],
												'waste'    				=> $r['waste'], 
												'jumlah'  				=> $r['jumlah'],
												'do'    				=> $r['do'], 
												'jumlahorder' 			=> $r['jumlahorder'],
												'catatan'    			=> $r['catatan'], 
												'namalengkap'  			=> $r['namalengkap'],
												'satuan'  				=> $r['satuan']
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
	
	function alldataneedapproval_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
		
        $search = (isset($jsonObject["search"]) ? $this->clean($jsonObject["search"]) : "");
        if($search != ""){ $search = " AND (a.kode LIKE '%$search%') "; }

		$nomor = (isset($jsonObject["nomor"]) ? $this->clean($jsonObject["nomor"]) : "");
        if($nomor != ""){ $nomor = " AND (a.nomor = '$nomor') "; }
		
        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

		$query = "  SELECT  
                        a.nomor,
						a.kode
					FROM thdeliveryorder a
					WHERE 1 = 1
					AND a.status_disetujui = 0 $search $nomor $limit";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'nomor'    			=> $r['nomor'], 
												'nama'       		=> $r['kode']
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
	
	function alldatadetailneedapproval_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

		$nomor = (isset($jsonObject["nomor"]) ? $this->clean($jsonObject["nomor"]) : "");
        if($nomor != ""){ $nomor = " AND (c.nomor = '$nomor') "; }

		$query = "  SELECT  
                        a.nomor AS nomor,
						e.nama AS item,
						a.nomorthdeliveryorder AS nomorthdeliveryorder,
						b.perkiraanpersenwaste AS waste,
						b.jumlah AS jumlah,
						b.jumlahterorder AS do,
						a.jumlahorder AS jumlahorder,
						a.catatan AS catatan,
						d.namalengkap AS namalengkap,
						f.nama AS satuan
					FROM tddeliveryorder a
					JOIN mdrabdetail b ON a.nomormdrabdetail = b.nomor
					JOIN thdeliveryorder c ON a.nomorthdeliveryorder = c.nomor
					JOIN mhbangunan_view d ON c.nomormhbangunan = d.nomor
					JOIN mhbarang e ON a.nomormhbarang = e.nomor
					JOIN mhsatuan f ON e.nomormhsatuan = f.nomor
					WHERE 1 = 1
					AND a.status_disetujui = 0 $nomor";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'nomor'    				=> $r['nomor'], 
												'item'    				=> $r['item'], 
												'nomorthdeliveryorder'  => $r['nomorthdeliveryorder'],
												'waste'    				=> $r['waste'], 
												'jumlah'  				=> $r['jumlah'],
												'do'    				=> $r['do'], 
												'jumlahorder' 			=> $r['jumlahorder'],
												'catatan'    			=> $r['catatan'], 
												'namalengkap'  			=> $r['namalengkap'],
												'satuan'  				=> $r['satuan']
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
	
	function print_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
		
		$nomorth = (isset($jsonObject["nomorth"]) ? $this->clean($jsonObject["nomorth"]) : "");

		$this->db->trans_begin();
		
		$query = "  UPDATE thdeliveryorder
                    SET status_print = 1
					WHERE 1 = 1 AND nomor= " . $nomorth;
        $this->db->query($query);
		
        if ($this->db->trans_status() === FALSE){
            $this->db->trans_rollback();
            array_push($data['data'], array( 'success' => $query ));
        }else{
            $this->db->trans_commit();
            array_push($data['data'], array( 'success' => 'true' ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }
	
	function approveall_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
		
		$nomorth = (isset($jsonObject["nomorth"]) ? $this->clean($jsonObject["nomorth"]) : "");

		$this->db->trans_begin();
		
		$query = "  UPDATE tddeliveryorder
                    SET status_disetujui = 1
					WHERE 1 = 1 AND nomorthdeliveryorder = " . $nomorth;
        $this->db->query($query);
		
        if ($this->db->trans_status() === FALSE){
            $this->db->trans_rollback();
            array_push($data['data'], array( 'success' => $query1 ));
        }else{
            $this->db->trans_commit();
            array_push($data['data'], array( 'success' => 'true' ));
			$query1 = "CALL SP_CHECK_TDDELIVERYORDER($nomorth, 0, 0)";
			$this->db->query($query1);
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }
	
	function approveselected_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
		
		$nomorth = (isset($jsonObject["nomorth"]) ? $this->clean($jsonObject["nomorth"]) : "");
		$list = (isset($jsonObject["list"]) ? $jsonObject["list"] : "");

		$this->db->trans_begin();
		
		if($list != "")
		{
			$pieces = explode("|", $list);
			foreach ($pieces as $arr) {
				if($arr!="")
				{
					$query = "  UPDATE tddeliveryorder
								SET status_disetujui = 1
								WHERE 1 = 1 AND nomor = " . $arr;
					$this->db->query($query);
				}
			}
		}
		
		$query1 = "  UPDATE tddeliveryorder
                    SET status_disetujui = 2
					WHERE 1 = 1 
						AND status_disetujui = 0
						AND nomorthdeliveryorder = " . $nomorth;
        $this->db->query($query1);
		
        if ($this->db->trans_status() === FALSE){
            $this->db->trans_rollback();
            array_push($data['data'], array( 'success' => $query ));
        }else{
            $this->db->trans_commit();
            array_push($data['data'], array( 'success' => 'true' ));
			$query1 = "CALL SP_CHECK_TDDELIVERYORDER($nomorth, 0, 0)";
			$this->db->query($query1);
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }
	
	function disapproveall_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
		
		$nomorth = (isset($jsonObject["nomorth"]) ? $this->clean($jsonObject["nomorth"]) : "");

		$this->db->trans_begin();
		
		$query = "  UPDATE tddeliveryorder
                    SET status_disetujui = 2
					WHERE 1 = 1 AND nomorthdeliveryorder = " . $nomorth;
        $this->db->query($query);
		
		$query = "  UPDATE thdeliveryorder
                    SET status_disetujui = 2
					WHERE 1 = 1 AND nomor = " . $nomorth;
        $this->db->query($query);
		
        if ($this->db->trans_status() === FALSE){
            $this->db->trans_rollback();
            array_push($data['data'], array( 'success' => $query ));
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
