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
class Orderjual extends REST_Controller { 

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

    // --- POST List Orderjual --- //
    function alldataorderjual_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $intNomorMCabang = (isset($jsonObject["cabang_nomor"]) ? $this->clean($jsonObject["cabang_nomor"])   : "1");
        if($intNomorMCabang != ""){ $intNomorMCabang = " AND a.intNomorMCabang = " . $intNomorMCabang; }

        $intNomorMSales = (isset($jsonObject["bex_nomor"]) ? $this->clean($jsonObject["bex_nomor"])   : "");
        if($intNomorMSales != ""){ $intNomorMSales = " AND a.intNomorMSales = " . $intNomorMSales; }

        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"])   : "");
        $masterlimit = 20;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

        $query = "  SELECT 
                        a.intNomor, a.vcKode,
                        a.dtTanggal,a.dtTanggalKirim,
                        a.intNomorMCabang,b.vcNama AS cabang_nama,
                        a.intNomorMCustomer,c.vcNama AS customer_nama,
                        a.intNomorMSales,d.vcNama AS sales_nama,
                        a.intNomorMGudang,e.vcNama AS gudang_nama
                    FROM thorderjual a 
                    JOIN mcabang b ON a.intNomorMCabang = b.intNomor 
                    JOIN mcustomer c ON a.intNomorMCustomer = c.intNomor 
                    JOIN msales d ON a.intNomorMSales = d.intNomor 
                    JOIN mgudang e ON a.intNomorMGudang = e.intNomor 
                    WHERE a.intStatus > 0 AND a.intApproved = 0 $intNomorMCabang $intNomorMSales 
                    ORDER BY a.intNomor DESC 
                    $limit";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'intNomor'                => $r['intNomor'], 
                                                'vcKode'                  => $r['vcKode'], 
                                                'dtTanggal'               => $r['dtTanggal'],
                                                'dtTanggalKirim'          => $r['dtTanggalKirim'], 
                                                'intNomorMCabang'         => $r['intNomorMCabang'],
                                                'cabang_nama'             => $r['cabang_nama'], 
                                                'intNomorMCustomer'       => $r['intNomorMCustomer'],
                                                'customer_nama'           => $r['customer_nama'], 
                                                'intNomorMSales'          => $r['intNomorMSales'],
                                                'sales_nama'              => $r['sales_nama'],   
                                                'intNomorMGudang'         => $r['intNomorMGudang'],
                                                'gudang_nama'             => $r['gudang_nama'] 
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

	// --- POST Detail Header Orderjual --- //
	function alldetailheaderorderjual_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
		$jsonObject = (json_decode($value , true));

        $intNomor = (isset($jsonObject["orderjual_nomor"]) ? $this->clean($jsonObject["orderjual_nomor"])   : "");
        if($intNomor != ""){ $intNomor = " AND a.intNomor = " . $intNomor; }

        $query = "	SELECT 
                        a.intNomor, a.vcKode,
                        a.intTOP,
                        a.dtTanggal,a.dtTanggalKirim,
                        a.intJTHari,
                        a.vcKeterangan, a.vcKeteranganFJ,a.vcKeteranganKW,
                        a.intJenisOJ,a.intJenis,a.intGabungan,
                        a.intNomorMCabang,b.vcNama AS cabang_nama,
                        a.intNomorMCustomer,c.vcNama AS customer_nama,
                        a.intNomorMSales,d.vcNama AS sales_nama,
                        a.intNomorMJenisPenjualan,e.vcNama AS jenispenjualan_nama,
                        a.intNomorMValuta,f.vcNama AS valuta_nama,
                        a.intBiaya,
                        a.decSubtotal,a.decKurs,a.decDisc,
                        a.decDiscNominal,a.decPPN,a.decPPNNominal,
                        a.decTotal,a.decTotalLama,a.decDPP,
                        a.decTotalUMC,a.decSisa,a.decTotalBiaya,
                        a.decTotalBiayaInternal,a.decTotalBiayaInternal,a.decTotalBiayaEstimasi,
                        a.intNomorMUserApprovedBy,g.vcNama AS approveuser_nama,
                        a.intNomorMGudang,h.vcNama AS gudang_nama,
                        a.decUM1,a.decUM2,a.decUM3, 
                        a.intNomorMArea,i.vcNama AS area_nama
                    FROM thorderjual a 
                    JOIN mcabang b ON a.intNomorMCabang = b.intNomor 
                    JOIN mcustomer c ON a.intNomorMCustomer = c.intNomor 
                    JOIN msales d ON a.intNomorMSales = d.intNomor 
                    JOIN mjenispenjualan e ON a.intNomorMJenisPenjualan = e.intNomor 
                    JOIN mvaluta f ON a.intNomorMValuta = f.intNomor 
                    JOIN muser g ON a.intNomorMUserApprovedBy = g.intNomor 
                    JOIN mgudang h ON a.intNomorMGudang = h.intNomor 
                    JOIN marea i ON a.intNomorMArea = i.intNomor 
                    WHERE a.intStatus > 0 $intNomor ";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                								'intNomor' 		          => $r['intNomor'], 
                                                'vcKode'          		  => $r['vcKode'], 
                								'intTOP' 		          => $r['intTOP'],
                								'dtTanggal' 	          => $r['dtTanggal'],
                                                'dtTanggalKirim'          => $r['dtTanggalKirim'], 
                								'intJTHari' 	          => $r['intJTHari'],
                								'vcKeterangan' 	          => $r['vcKeterangan'],
                                                'vcKeteranganFJ'          => $r['vcKeteranganFJ'],
                                                'vcKeteranganKW'          => $r['vcKeteranganKW'], 
                                                'intJenisOJ'              => $r['intJenisOJ'],    
                                                'intJenis'                => $r['intJenis'],          
                                                'intGabungan'             => $r['intGabungan'],
                                                'intNomorMCabang'         => $r['intNomorMCabang'],
                                                'cabang_nama'             => $r['cabang_nama'], 
                                                'intNomorMCustomer'       => $r['intNomorMCustomer'],
                                                'customer_nama'           => $r['customer_nama'], 
                                                'intNomorMSales'          => $r['intNomorMSales'],
                                                'sales_nama'              => $r['sales_nama'], 
                                                'intNomorMJenisPenjualan' => $r['intNomorMJenisPenjualan'],
                                                'jenispenjualan_nama'     => $r['jenispenjualan_nama'], 
                                                'intNomorMValuta'         => $r['intNomorMValuta'],
                                                'valuta_nama'             => $r['valuta_nama'], 
                								'intBiaya' 			      => $r['intBiaya'], 
                                                'decSubtotal'             => $r['decSubtotal'],
                                                'decKurs'                 => $r['decKurs'],
                                                'decDisc'                 => $r['decDisc'],
                                                'decDiscNominal'          => $r['decDiscNominal'],
                                                'decPPN'                  => $r['decPPN'],
                                                'decPPNNominal'           => $r['decPPNNominal'],
                                                'decTotal'                => $r['decTotal'],
                                                'decTotalLama'            => $r['decTotalLama'],
                                                'decDPP'                  => $r['decDPP'],
                                                'decTotalUMC'             => $r['decTotalUMC'],
                                                'decSisa'                 => $r['decSisa'],
                                                'decTotalBiaya'           => $r['decTotalBiaya'],
                                                'decTotalBiayaInternal'   => $r['decTotalBiayaInternal'],
                                                'decTotalBiayaInternal'   => $r['decTotalBiayaInternal'],
                                                'decTotalBiayaEstimasi'   => $r['decTotalBiayaEstimasi'],
                                                'intNomorMUserApprovedBy' => $r['intNomorMUserApprovedBy'],
                                                'approveuser_nama'        => $r['approveuser_nama'], 
                                                'intNomorMGudang'         => $r['intNomorMGudang'],
                                                'gudang_nama'             => $r['gudang_nama'], 
                                                'decUM1'                  => $r['decUM1'],
                                                'decUM2'                  => $r['decUM2'],
                                                'decUM3'                  => $r['decUM3'],
                                                'intNomorMArea'           => $r['intNomorMArea'],
                                                'area_nama'               => $r['area_nama']
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

    // --- POST Detail Header Orderjual --- //
    function alldetailgridorderjual_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $intNomorHeader = (isset($jsonObject["orderjual_nomor"]) ? $this->clean($jsonObject["orderjual_nomor"])   : "");
        if($intNomorHeader != ""){ $intNomorHeader = " AND a.intNomorHeader = " . $intNomorHeader; }

        $query = "  SELECT 
                        a.intNomor, a.intNomorHeader, a.intNomorDetail, 
                        a.intNomorMBarang,b.vcNamaBeli AS barang_nama, 
                        a.intNomorMSatuan,c.vcNama AS satuan_nama, 
                        a.intNomorMSatuan1,d.vcNama AS satuan1_nama, 
                        a.intNomorMSatuanUnit,e.vcNama AS satuanunit_nama,
                        a.decMultiplier1,a.decMultiplier,
                        a.decJumlah1,a.decJumlahUnit,a.decJumlah,
                        a.decHarga,a.decDisc1,a.decDisc2,decDisc3,
                        a.decNetto,a.decSubtotal
                    FROM tdorderjual a 
                    JOIN mbarang b ON a.intNomorMBarang = b.intNomor 
                    JOIN msatuan c ON a.intNomorMSatuan = c.intNomor 
                    JOIN msatuan d ON a.intNomorMSatuan1 = d.intNomor 
                    JOIN msatuan e ON a.intNomorMSatuanUnit = e.intNomor 
                    WHERE a.intStatus > 0 $intNomorHeader ";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'intNomor'            => $r['intNomor'], 
                                                'intNomorHeader'      => $r['intNomorHeader'], 
                                                'intNomorDetail'      => $r['intNomorDetail'],
                                                'intNomorMBarang'     => $r['intNomorMBarang'],
                                                'barang_nama'         => $r['barang_nama'], 
                                                'intNomorMSatuan'     => $r['intNomorMSatuan'],
                                                'satuan_nama'         => $r['satuan_nama'],
                                                'intNomorMSatuan1'    => $r['intNomorMSatuan1'],
                                                'satuan1_nama'        => $r['satuan1_nama'], 
                                                'intNomorMSatuanUnit' => $r['intNomorMSatuanUnit'],    
                                                'satuanunit_nama'     => $r['satuanunit_nama'],          
                                                'decMultiplier1'      => $r['decMultiplier1'],
                                                'decMultiplier'       => $r['decMultiplier'],
                                                'decJumlah1'          => $r['decJumlah1'], 
                                                'decJumlahUnit'       => $r['decJumlahUnit'],
                                                'decJumlah'           => $r['decJumlah'], 
                                                'decHarga'            => $r['decHarga'],
                                                'decDisc1'            => $r['decDisc1'], 
                                                'decDisc2'            => $r['decDisc2'],
                                                'decDisc3'            => $r['decDisc3'], 
                                                'decNetto'            => $r['decNetto'],
                                                'decSubtotal'         => $r['decSubtotal']
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

    // --- POST Detail Header Orderjual --- //
    function alldetailgridbiayaorderjual_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $intNomorHeader = (isset($jsonObject["orderjual_nomor"]) ? $this->clean($jsonObject["orderjual_nomor"])   : "");
        if($intNomorHeader != ""){ $intNomorHeader = " AND a.intNomorHeader = " . $intNomorHeader; }

        $query = "  SELECT 
                        a.intNomor, a.intNomorHeader,  
                        a.intNomorMSupplier,b.vcNama AS supplier_nama, 
                        a.intNomorMJenisBiaya,c.vcNama AS jenisbiaya_nama, 
                        a.intNomorMValuta,d.vcNama AS valuta_nama,
                        a.decSubtotal,a.decPPN,a.decPPNNominal,
                        a.decTotal,a.decTerbayar,a.decKurs,a.decTotalIDR,
                        a.vcDeskripsi,
                        a.dectotalSupplier
                    FROM tdorderjualbiaya a 
                    JOIN msupplier b ON a.intNomorMSupplier = b.intNomor 
                    JOIN mjenisbiaya c ON a.intNomorMJenisBiaya = c.intNomor 
                    JOIN mvaluta d ON a.intNomorMValuta = d.intNomor 
                    WHERE a.intStatus > 0 $intNomorHeader ";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'intNomor'            => $r['intNomor'], 
                                                'intNomorHeader'      => $r['intNomorHeader'], 
                                                'intNomorMSupplier'   => $r['intNomorMSupplier'],
                                                'supplier_nama'       => $r['supplier_nama'],
                                                'intNomorMJenisBiaya' => $r['intNomorMJenisBiaya'],
                                                'jenisbiaya_nama'     => $r['jenisbiaya_nama'],
                                                'intNomorMValuta'     => $r['intNomorMValuta'],
                                                'valuta_nama'         => $r['valuta_nama'], 
                                                'decSubtotal'         => $r['decSubtotal'],    
                                                'decPPN'              => $r['decPPN'],          
                                                'decPPNNominal'       => $r['decPPNNominal'],
                                                'decTotal'            => $r['decTotal'],
                                                'decTerbayar'         => $r['decTerbayar'], 
                                                'decKurs'             => $r['decKurs'],
                                                'decTotalIDR'         => $r['decTotalIDR'],
                                                'vcDeskripsi'         => $r['vcDeskripsi'], 
                                                'dectotalSupplier'    => $r['dectotalSupplier']
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

    // --- POST Detail Header Orderjual --- //
    function alldetailgridbiayainternalorderjual_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $intNomorHeader = (isset($jsonObject["orderjual_nomor"]) ? $this->clean($jsonObject["orderjual_nomor"])   : "");
        if($intNomorHeader != ""){ $intNomorHeader = " AND a.intNomorHeader = " . $intNomorHeader; }

        $query = "  SELECT 
                        a.intNomor, a.intNomorHeader,  
                        a.intNomorMJenisBiaya,b.vcNama AS jenisbiaya_nama, 
                        a.intNomorMValuta,c.vcNama AS valuta_nama,
                        a.decSubtotal,a.decPPN,a.decPPNNominal,
                        a.decTotal,a.decTerbayar,a.decKurs,a.decTotalIDR,
                        a.vcDeskripsi
                    FROM tdorderjualbiayainternal a 
                    JOIN mjenisbiaya b ON a.intNomorMJenisBiaya = b.intNomor 
                    JOIN mvaluta c ON a.intNomorMValuta = c.intNomor 
                    WHERE a.intStatus > 0 $intNomorHeader ";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'intNomor'            => $r['intNomor'], 
                                                'intNomorHeader'      => $r['intNomorHeader'],
                                                'intNomorMJenisBiaya' => $r['intNomorMJenisBiaya'],
                                                'jenisbiaya_nama'     => $r['jenisbiaya_nama'],
                                                'intNomorMValuta'     => $r['intNomorMValuta'],
                                                'valuta_nama'         => $r['valuta_nama'], 
                                                'decSubtotal'         => $r['decSubtotal'],    
                                                'decPPN'              => $r['decPPN'],          
                                                'decPPNNominal'       => $r['decPPNNominal'],
                                                'decTotal'            => $r['decTotal'],
                                                'decTerbayar'         => $r['decTerbayar'], 
                                                'decKurs'             => $r['decKurs'],
                                                'decTotalIDR'         => $r['decTotalIDR'],
                                                'vcDeskripsi'         => $r['vcDeskripsi']
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

    // --- POST Detail Header Orderjual --- //
    function alldetailgridshadeorderjual_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $intNomorHeader = (isset($jsonObject["orderjual_nomor"]) ? $this->clean($jsonObject["orderjual_nomor"])   : "");
        if($intNomorHeader != ""){ $intNomorHeader = " AND a.intNomorHeader = " . $intNomorHeader; }

        $query = "  SELECT 
                        a.intNomor, a.intNomorHeader,  
                        a.intNomorMBarang,b.vcNamaBeli AS barang_nama, 
                        a.decJumlah
                    FROM tdorderjualshade a 
                    JOIN mbarang b ON a.intNomorMBarang = b.intNomor 
                    WHERE a.intStatus > 0 $intNomorHeader ";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'intNomor'        => $r['intNomor'], 
                                                'intNomorHeader'  => $r['intNomorHeader'],
                                                'intNomorMBarang' => $r['intNomorMBarang'],
                                                'barang_nama'     => $r['barang_nama'],
                                                'decJumlah'       => $r['decJumlah']
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


    // --- Send Message Group --- //
    function insertorderjual_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $kode           = "-";
        $top            = "0";
        $jatuhtempo     = (isset($jsonObject["jatuhtempo"]) ? $jsonObject["jatuhtempo"]         : "0");

        if($top == "CBD"){ $top = "1"; $jatuhtempo = "0"; }
        if($top == "COD"){ $top = "2"; $jatuhtempo = "0"; }
        if($top == "CREDIT"){ $top = "3"; }
        if($top == "CBD-TC"){ $top = "4"; $jatuhtempo = "0"; }

        $tanggal            = (isset($jsonObject["tanggal"]) ? $jsonObject["tanggal"]                       : "");
        $tanggalkirim       = (isset($jsonObject["tanggalkirim"]) ? $jsonObject["tanggalkirim"]             : "");
        $keterangan         = (isset($jsonObject["keterangan"]) ? $jsonObject["keterangan"]                 : "");
        $keteranganfj       = (isset($jsonObject["keteranganfj"]) ? $jsonObject["keteranganfj"]             : "");
        $keterangankw       = (isset($jsonObject["keterangankw"]) ? $jsonObject["keterangankw"]             : "");
        $gabungan           = (isset($jsonObject["gabungan"]) ? $jsonObject["gabungan"]                     : "0");
        $cabang             = (isset($jsonObject["cabang"]) ? $jsonObject["cabang"]                         : "1");
        $gudang             = (isset($jsonObject["gudang"]) ? $jsonObject["gudang"]                         : "1");
        $customer           = (isset($jsonObject["customer"]) ? $jsonObject["customer"]                     : "0");
        $sales              = (isset($jsonObject["sales"]) ? $jsonObject["sales"]                           : "0");
        $jenispenjualan     = (isset($jsonObject["jenispenjualan"]) ? $jsonObject["jenispenjualan"]         : "0");
        $valuta             = (isset($jsonObject["valuta"]) ? $jsonObject["valuta"]                         : "0");
        $biaya              = (isset($jsonObject["biaya"]) ? $jsonObject["biaya"]                           : "0");
        $subtotal           = (isset($jsonObject["subtotal"]) ? $jsonObject["subtotal"]                     : "0");
        $kurs               = (isset($jsonObject["kurs"]) ? $jsonObject["kurs"]                             : "0");
        $disc               = (isset($jsonObject["disc"]) ? $jsonObject["disc"]                             : "0");
        $discnominal        = (isset($jsonObject["discnominal"]) ? $jsonObject["discnominal"]               : "0");
        $ppn                = (isset($jsonObject["ppn"]) ? $jsonObject["ppn"]                               : "0");
        $ppnnominal         = (isset($jsonObject["ppnnominal"]) ? $jsonObject["ppnnominal"]                 : "0");
        $total              = (isset($jsonObject["total"]) ? $jsonObject["total"]                           : "0");
        $totallama          = (isset($jsonObject["totallama"]) ? $jsonObject["totallama"]                   : "0");
        $dpp                = (isset($jsonObject["dpp"]) ? $jsonObject["dpp"]                               : "0");
        $totalumc           = (isset($jsonObject["totalumc"]) ? $jsonObject["totalumc"]                     : "0");
        $sisa               = (isset($jsonObject["sisa"]) ? $jsonObject["sisa"]                             : "0");
        $totalbiaya         = (isset($jsonObject["totalbiaya"]) ? $jsonObject["totalbiaya"]                 : "0");
        $totalbiayainternal = (isset($jsonObject["totalbiayainternal"]) ? $jsonObject["totalbiayainternal"] : "0");
        $totalbiayaestimasi = (isset($jsonObject["totalbiayaestimasi"]) ? $jsonObject["totalbiayaestimasi"] : "0");
        $intuser            = (isset($jsonObject["intuser"]) ? $jsonObject["intuser"]                       : "0");

        $this->db->trans_begin();

        $query = $this->db->insert_string('thorderjual', array(
                                                              'vcKode'                  =>$kode, 
                                                              'intTOP'                  =>$top, 
                                                              'dtTanggal'               =>$tanggal, 
                                                              'dtTanggalKirim'          =>$tanggalkirim, 
                                                              'intJTHari'               =>$jatuhtempo, 
                                                              'vcKeterangan'            =>$keterangan, 
                                                              'vcKeteranganFJ'          =>$keteranganfj, 
                                                              'vcKeteranganKW'          =>$keterangankw, 
                                                              'intJenisOJ'              =>"1", 
                                                              'intJenis'                =>"1", 
                                                              'intGabungan'             =>$gabungan, 
                                                              'intNomorMGudang'         =>$gudang, 
                                                              'intNomorMCabang'         =>$cabang, 
                                                              'intNomorMCustomer'       =>$customer, 
                                                              'intNomorMSales'          =>$sales, 
                                                              'intNomorMJenisPenjualan' =>$jenispenjualan, 
                                                              'intNomorMValuta'         =>$valuta, 
                                                              'intBiaya'                =>$biaya, 
                                                              'decSubTotal'             =>$subtotal, 
                                                              'decKurs'                 =>$kurs, 
                                                              'decDisc'                 =>$disc, 
                                                              'decDiscNominal'          =>$discnominal, 
                                                              'decPPN'                  =>$ppn, 
                                                              'decPPNNominal'           =>$ppnnominal, 
                                                              'decTotal'                =>$total, 
                                                              'decTotalLama'            =>$totallama, 
                                                              'decDPP'                  =>$dpp, 
                                                              'decTotalUMC'             =>$totalumc, 
                                                              'decSisa'                 =>$sisa, 
                                                              'decTotalBiaya'           =>$totalbiaya, 
                                                              'decTotalBiayaInternal'   =>$totalbiayainternal, 
                                                              'decTotalBiayaEstimasi'   =>$totalbiayaestimasi, 
                                                              'intInsertUserID'         =>$intuser
                                                            )
                                        );
        $this->db->query($query);

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
