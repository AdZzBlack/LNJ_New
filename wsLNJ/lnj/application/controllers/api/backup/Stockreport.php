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
class Stockreport extends REST_Controller { 

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

    function getStockKomoditi_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

        $komoditi      = (isset($jsonObject["komoditi"])      ? $this->clean($jsonObject["komoditi"])               : "1");
        $kode          = (isset($jsonObject["kode"])          ? "'".$this->clean($jsonObject["kode"])."'"           : "'%'");
        $nama          = (isset($jsonObject["nama"])          ? "'".$this->clean($jsonObject["nama"])."'"           : "'%'");
        $kategori      = (isset($jsonObject["kategori"])      ? "'".$this->clean($jsonObject["kategori"])."'"       : "'%'");
        $brand         = (isset($jsonObject["brand"])         ? "'".$this->clean($jsonObject["brand"])."'"          : "'%'");
        $tipe          = (isset($jsonObject["tipe"])          ? "'".$this->clean($jsonObject["tipe"])."'"           : "'%'");
        $group         = (isset($jsonObject["group"])         ? "'".$this->clean($jsonObject["group"])."'"          : "'%'");
        $gudang        = (isset($jsonObject["gudang"])        ? "'".$this->clean($jsonObject["gudang"])."'"         : "''");
        $satuan        = (isset($jsonObject["satuan"])        ? "'".$this->clean($jsonObject["satuan"])."'"         : "''");
        $shade         = (isset($jsonObject["shade"])         ? "'".$this->clean($jsonObject["shade"])."'"          : "''");
        $panjang       = (isset($jsonObject["panjang"])       ? "'".$this->clean($jsonObject["panjang"])."'"        : "'%'");        
        $lebar         = (isset($jsonObject["lebar"])         ? "'".$this->clean($jsonObject["lebar"])."'"          : "'%'");
        $tebal         = (isset($jsonObject["tebal"])         ? "'".$this->clean($jsonObject["tebal"])."'"          : "'%'");
        $tanggal_awal  = (isset($jsonObject["tanggal_awal"])  ? "'".$this->clean($jsonObject["tanggal_awal"])."'"   : "'1990-1-1'");
        $tanggal_akhir = (isset($jsonObject["tanggal_akhir"]) ? "'".$this->clean($jsonObject["tanggal_akhir"])."'"  : "'".date("Y-m-d")."'");
        $user_nomor    = (isset($jsonObject["user_nomor"])    ? $this->clean($jsonObject["user_nomor"])             : "2");
        $tampilkan_no  = (isset($jsonObject["tampilkan_no"])  ? $this->clean($jsonObject["tampilkan_no"])           : "1");

        if($kode == "''"){ $kode = "'%'"; }
        if($nama == "''"){ $nama = "'%'"; }
        if($kategori == "''"){ $kategori = "'%'"; }
        if($brand == "''"){ $brand = "'%'"; }
        if($tipe == "''"){ $tipe = "'%'"; }
        if($group == "''"){ $group = "'%'"; }
        if($panjang == "''"){ $panjang = "'%'"; }
        if($lebar == "''"){ $lebar = "'%'"; }
        if($tebal == "''"){ $tebal = "'%'"; }
        if($tanggal_akhir == "''"){ $tanggal_akhir = "'".date("Y-m-d")."'"; }
        if($user_nomor == ""){ $user_nomor = "2"; }
        if($tampilkan_no == ""){ $user_nomor = "1"; }

        // $query  = "CALL RP_Stok_20120731(0, 12, 1, $kode, $nama, $kategori, $brand, $tipe, $group, $gudang, $satuan, $shade, $panjang, $lebar, $tebal, '1990-1-1', $tanggal_akhir, $user_nomor, $tampilkan_no);";

        $query = "  SELECT  MID($kode, 2, Length($kode) - 2) AS vcFilterKodebarang,
                            MID($nama  , 2, Length($nama) - 2)   AS vcFilternamabeli,
                            MID($brand     , 2, Length($brand) - 2)      AS vcFilterbrand,
                            MID($tipe      , 2, Length($tipe) - 2)       AS vcFiltertipe,
                            MID($group     , 2, Length($group) - 2)      AS vcFiltergroup,
                            MID($kategori  , 2, Length($kategori) - 2)   AS vcFilterkategori,
                            b.vcKode AS vcKodeBarang,
                        b.vcnamabeli AS vcNamaBeli,
                        F_barang_getnamaJual (b.vcnamabeli, b1.vcnama, b2.vcnama, b3.vcnama, b4.vcnama, b.decP, b.decL, b.dect) AS vcNamaJual,
                        b5.vcnama AS vcNamaSatuan1,
                        b6.vcnama AS vcNamaSatuan2,
                        b7.vcnama AS vcNamaSatuan3,
                        b8.vcnama AS vcNamaGroup,
                        b9.vcnama AS vcNamaKategori,
                            a.decjumlah1 AS decJumlah1,
                            (a.decjumlah1 / b.deckonversi2) AS decJumlah2,
                        (a.decjumlah1 / (b.deckonversi3 * b.deckonversi2)) AS decJumlah3,
                                    Round(a.decpersediaan, 2) AS decPersediaan
                        
                    FROM (select a.intnomormbarang,
                                         sum(a.decjumlah) AS decjumlah1,
                                         sum(Round(a.decjumlah * a.dechppavg,2)) AS decpersediaan
                                  from tlaporanstok a
                           JOIN (mbarang b
                             JOIN mbrand    b1 On b1.intnomor = b.intnomormbrand
                             JOIN mtipe     b2 On b2.intnomor = b.intnomormtipe
                             JOIN mgrade    b3 On b3.intnomor = b.intnomormgrade
                             JOIN msurface  b4 On b4.intnomor = b.intnomormsurface
                             JOIN mgroup    b8 On b8.intnomor = b.intnomormgroup
                             JOIN mkategori b9 On b9.intnomor = b.intnomormkategori)
                           On a.intnomormbarang = b.intnomor
                          WHERE b.intkomoditi = $komoditi AND
                                        b.vcKode like $kode AND
                            b.vcnamabeli like $nama AND
                            b1.vcnama like $brand AND
                            b2.vcnama like $tipe AND
                            
                            b8.vcnama like $group AND
                            b9.vcnama like $kategori AND
                            ((b.decP = $panjang AND $panjang > 0) OR ($panjang = 0)) AND
                            ((b.decL = $lebar   AND $lebar > 0) OR ($lebar = 0)) AND
                            ((b.dect = $tebal   AND $tebal > 0) OR ($tebal = 0)) AND
                            a.dttanggal <= $tanggal_akhir
                          GROUP BY a.intnomormbarang) a
                         JOIN (mbarang b
                               JOIN mbrand   b1 On b1.intnomor = b.intnomormbrand
                               JOIN mtipe    b2 On b2.intnomor = b.intnomormtipe
                               JOIN mgrade   b3 On b3.intnomor = b.intnomormgrade
                               JOIN msurface b4 On b4.intnomor = b.intnomormsurface
                               JOIN msatuan  b5 On b5.intnomor = b.intnomormsatuan1
                               JOIN msatuan  b6 On b6.intnomor = b.intnomormsatuan2
                               JOIN msatuan  b7 On b7.intnomor = b.intnomormsatuan3
                           JOIN mgroup    b8 On b8.intnomor = b.intnomormgroup
                           JOIN mkategori b9 On b9.intnomor = b.intnomormkategori)
                         On a.intnomormbarang = b.intnomor
                    HAVING ($tampilkan_no = 1) or
                           (($tampilkan_no = 0) and
                            (decjumlah1 <> 0 or
                             decjumlah2 <> 0 or
                             decjumlah3 <> 0))
                    ORDER BY vcnamabeli, 
                         vcnamajual $limit";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'vcKodeBarang'   => $r['vcKodeBarang'], 
                                                'vcNamaBeli'     => $r['vcNamaBeli'], 
                                                'vcNamaJual'     => $r['vcNamaJual'], 
                                                'vcNamaSatuan1'  => $r['vcNamaSatuan1'], 
                                                'vcNamaSatuan2'  => $r['vcNamaSatuan2'], 
                                                'vcNamaSatuan3'  => $r['vcNamaSatuan3'], 
                                                'vcNamaGroup'    => $r['vcNamaGroup'], 
                                                'vcNamaKategori' => $r['vcNamaKategori'], 
                                                'decJumlah1'     => $r['decJumlah1'], 
                                                'decJumlah2'     => $r['decJumlah2'], 
                                                'decJumlah3'     => $r['decJumlah3'], 
                                                'decPersediaan'  => $r['decPersediaan']
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
