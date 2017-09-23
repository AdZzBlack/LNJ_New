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
class Pricelist extends REST_Controller { 

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

    function getPriceList_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

        $filter = (isset($jsonObject["filter"]) ? "'%".$this->clean($jsonObject["filter"])."%'" : "'%'");
        if($filter == "''"){ $filter = "'%'"; }

        $query = "  SELECT a.intNomor, a.vcKode, a.vcNamaBeli, 
                          F_Barang_GetNamaJual(a.vcNamaBeli, c.vcNama, e.vcNama, f.vcNama, g.vcNama, a.decP, a.decL, a.decT) AS vcNamaJual, 
                          a.decLuas, 
                          a.intPLApproved, 
                          a.decHargaPL1, a.decHargaPL1USD, a.decHargaPL1RMB, 
                          IFNULL(CONCAT('/ ',h1.vcNama),'') AS satuanPL1, 
                          a.decHargaPL2, a.decHargaPL2USD, a.decHargaPL2RMB, 
                          IFNULL(CONCAT('/ ',h2.vcNama),'') AS satuanPL2, 
                          a.decHargaPL3, a.decHargaPL3USD, a.decHargaPL3RMB, 
                          IFNULL(CONCAT('/ ',h3.vcNama),'') AS satuanPL3,
                           i.Satuan_1,
                           i.INVCOL AS Konversi_1,
                           i.Satuan_2,
                           i.Konversi_2,
                           i.Satuan_3,
                           i.Konversi_3
                   FROM mbarang a  
                        JOIN mkategori b ON b.intNomor = a.intNomorMKategori  
                        JOIN mbrand    c ON c.intNomor = a.intNomorMBrand  
                        JOIN mgroup    d ON d.intNomor = a.intNomorMGroup  
                        JOIN mtipe     e ON e.intNomor = a.intNomorMTipe  
                        JOIN mgrade    f ON f.intNomor = a.intNomorMGrade  
                        JOIN msurface  g ON g.intNomor = a.intNomorMSurface  
                        JOIN msatuan   h1 ON h1.intNomor = a.intNomorMSatuan1  
                        JOIN msatuan   h2 ON h2.intNomor = a.intNomorMSatuan2  
                        JOIN msatuan   h3 ON h3.intNomor = a.intNomorMSatuan3 
                        LEFT JOIN  (
                                    SELECT a.intNomor,
                                         a.intNomorMSatuan1,
                                         a.intNomorMSatuan2,
                                         a.intNomorMSatuan3, 
                                         a.vcKode AS `Kode Barang`, 
                                           a.vcNamaBeli AS `Nama Beli`, 
                                           F_Barang_GetNamaJual (a.vcNamaBeli, a1.vcNama, a2.vcNama, a3.vcNama, a4.vcNama, a.decP, a.decL, a.decT) AS `Nama Jual`,
                                         a5.vcNama AS `Satuan_1`,
                                         1 AS INVCOL,
                                         a6.vcNama AS `Satuan_2`,
                                         a.decKonversi2 AS `Konversi_2`,
                                         a7.vcNama AS `Satuan_3`,
                                         a.decKonversi3 AS `Konversi_3`
                                    FROM mbarang a 
                                         JOIN mbrand   a1 ON a1.intNomor = a.intNomorMBrand 
                                         JOIN mtipe    a2 ON a2.intNomor = a.intNomorMTipe 
                                         JOIN mgrade   a3 ON a3.intNomor = a.intNomorMGrade 
                                         JOIN msurface a4 ON a4.intNomor = a.intNomorMSurface 
                                         JOIN msatuan  a5 ON a5.intNomor = a.intNomorMSatuan1
                                         JOIN msatuan  a6 ON a6.intNomor = a.intNomorMSatuan2
                                         JOIN msatuan  a7 ON a7.intNomor = a.intNomorMSatuan3
                                    WHERE a.intNomor > 0 AND a.intStatus > 0 AND a.intAktif > 0 AND a.intKomoditi = 1 
                        ) i ON a.intNomor = i.intNomor 
                   WHERE a.intPLApproved =   1   AND  
                         a.intStatus = 1  AND  
                         (a.vcKode LIKE $filter OR  
                          a.vcNamaBeli LIKE $filter OR  
                          b.vcNama LIKE $filter OR  
                          c.vcNama LIKE $filter OR  
                          d.vcNama LIKE $filter OR  
                          e.vcNama LIKE $filter OR  
                          f.vcNama LIKE $filter OR  
                          g.vcNama LIKE $filter OR  
                          h1.vcNama LIKE $filter OR  
                          h2.vcNama LIKE $filter OR  
                          h3.vcNama LIKE $filter )  
                   ORDER BY vcNamaJual, a.vcNamaBeli 
                   $limit";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'intNomor'   	   => $r['intNomor'], 
                                                'vcKode'     	   => $r['vcKode'], 
                                                'vcNamaBeli'     => $r['vcNamaBeli'], 
                                                'vcNamaJual'  	 => $r['vcNamaJual'], 
                                                'decLuas'  		   => $r['decLuas'], 
                                                'intPLApproved'  => $r['intPLApproved'], 
                                                'decHargaPL1'    => $r['decHargaPL1'], 
                                                'decHargaPL1USD' => $r['decHargaPL1USD'], 
                                                'decHargaPL1RMB' => $r['decHargaPL1RMB'], 
                                                'decHargaPL2'    => $r['decHargaPL2'], 
                                                'decHargaPL2USD' => $r['decHargaPL2USD'], 
                                                'decHargaPL2RMB' => $r['decHargaPL2RMB'], 
                                                'decHargaPL3'    => $r['decHargaPL3'], 
                                                'decHargaPL3USD' => $r['decHargaPL3USD'], 
                                                'decHargaPL3RMB' => $r['decHargaPL3RMB'] , 
                                                'satuanPL1'      => $r['satuanPL1'] , 
                                                'satuanPL2'      => $r['satuanPL2'] , 
                                                'satuanPL3'      => $r['satuanPL3'] ,
                                                'Satuan_1'       => $r['Satuan_1'] ,
                                                'Satuan_2'       => $r['Satuan_2'] ,
                                                'Satuan_3'       => $r['Satuan_3'] ,
                                                'Konversi_1'     => $r['Konversi_1'] ,
                                                'Konversi_2'     => $r['Konversi_2'] ,
                                                'Konversi_3'     => $r['Konversi_3'] 
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
