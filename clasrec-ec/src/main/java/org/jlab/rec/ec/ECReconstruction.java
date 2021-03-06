/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.rec.ec;

import org.jlab.clasrec.main.DetectorReconstruction;
import org.jlab.clasrec.utils.ServiceConfiguration;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.evio.clas12.EvioDataBank;
import org.jlab.evio.clas12.EvioDataEvent;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author gavalian
 */
public class ECReconstruction extends DetectorReconstruction {

    public Map<String,double[][][][]> CCDB = new HashMap<String,double[][][][]>();
    
    double atten[][][][] = new double [6][3][3][68];
    double  gain[][][][] = new double [6][3][3][68];
    double   ped[][][][] = new double [6][3][3][68];

    String tbatt[][] = {{"energyU","energyV","energyW"},
			            {"attenuation","attenuation","attenuation"},
			            {"attenuation","attenuation","attenuation"}};
    String tbgai[][] = {{"energyU","energyV","energyW"},
			            {"gain","gain","gain"},
		  	            {"gain","gain","gain"}};
    String tbped[][] = {{"energyU","energyV","energyW"},
			            {"pedestal","pedestal","pedestal"},
			            {"pedestal","pedestal","pedestal"}};
    
    String itatt[][] = {{"atten2U","atten2V","atten2W"},
			            {"innU","innV","innW"},
			            {"outU","outV","outW"}};
    String itgai[][] = {{"gain","gain","gain"},
			            {"innU","innV","innW"},
			            {"outU","outV","outW"}};
    String itped[][] = {{"pedestal","pedestal","pedestal"},
                        {"innU","innV","innW"},
			            {"outU","outV","outW"}};
	    
    String slnam[]   = {"pcal","ec","ec"};

    int[][] ipsl = {{68,62,62},{36,36,36},{36,36,36}};
    
    public ECReconstruction() {
        super("EC", "gavalian", "1.0");
    }

    @Override
    public void processEvent(EvioDataEvent event) {
        try {
            ECStore store = new ECStore();
            store.initECHits(event, this.getGeometry("EC"), CCDB);
            //store.showHits();
            store.initECPeaks();
            //store.showPeaks();
            store.initECPeakClusters();
            //store.showClusters();
            this.writeOutput(event, store);
        } catch (Exception e) {
            System.err.println("[EC-REC] >>>>>> something went wrong with this event");
            e.printStackTrace();
        }
    }
    
    public void writeOutput(EvioDataEvent event, ECStore store){
        EvioDataBank bankHits  = store.getBankHits(event);
        EvioDataBank bankPeaks = store.getBankPeaks(event);
        EvioDataBank bankClusters = store.getBankClusters(event);
        if(bankHits.rows()>0&&bankPeaks.rows()>0&&bankClusters.rows()>0)
            event.appendBanks(bankHits,bankPeaks,bankClusters);
    }
    
    @Override
    public void init() {
        int ind;
        ConstantProvider cp1,cp2,cp3;
        this.requireGeometry("EC");
        this.setCalibrationRun(1);
        this.setCalibrationVariation("default");
        this.requireCalibration("/calibration/ec/energy/attenuation");
        this.requireCalibration("/calibration/ec/energy/gain");
        this.requireCalibration("/calibration/ec/energy/pedestal");
        this.requireCalibration("/calibration/pcal/energy/energyU");
        this.requireCalibration("/calibration/pcal/energy/energyV");
        this.requireCalibration("/calibration/pcal/energy/energyW");
	
	    for (int sl=0;sl<3;sl++) {
	    for (int il=0;il<3;il++) {
		cp1=this.getConstants("/calibration/"+slnam[sl]+"/energy/"+tbatt[sl][il]);
		cp2=this.getConstants("/calibration/"+slnam[sl]+"/energy/"+tbgai[sl][il]);
		cp3=this.getConstants("/calibration/"+slnam[sl]+"/energy/"+tbped[sl][il]);
		for (int sec=0;sec<6;sec++) {
        for (int ip=0;ip<ipsl[sl][il];ip++) {
        ind = sec*ipsl[sl][il]+ip;
        atten[sec][sl][il][ip] = cp1.getDouble("/calibration/"+slnam[sl]+"/energy/"+tbatt[sl][il]+"/"+itatt[sl][il],ind);
         gain[sec][sl][il][ip] = cp2.getDouble("/calibration/"+slnam[sl]+"/energy/"+tbgai[sl][il]+"/"+itgai[sl][il],ind);
          ped[sec][sl][il][ip] = cp3.getDouble("/calibration/"+slnam[sl]+"/energy/"+tbped[sl][il]+"/"+itped[sl][il],ind);
                    }
                }
            }
        }

    CCDB.put("atten",atten);
	CCDB.put("gain",gain);
	CCDB.put("ped",ped);
 	
    }

    @Override
    public void configure(ServiceConfiguration c) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public static void main(String[] args){        
    }
    
}
