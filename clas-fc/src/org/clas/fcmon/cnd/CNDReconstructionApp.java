package org.clas.fcmon.cnd;

import static java.lang.System.out;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

import org.clas.fcmon.tools.FADCFitter;
import org.clas.fcmon.tools.FCApplication;

//groot
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;

//clas12rec
import org.jlab.detector.base.DetectorCollection;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.decode.CodaEventDecoder;
import org.jlab.detector.decode.DetectorDataDgtz;
import org.jlab.detector.decode.DetectorEventDecoder;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.utils.groups.IndexedList;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;

//import org.clas.fcmon.jroot.*;

public class CNDReconstructionApp extends FCApplication {
    
   FADCFitter     fitter  = new FADCFitter(1,15);
   String          mondet = null;
   
   String        BankType = null;
   int              detID = 0;
   
   CodaEventDecoder           codaDecoder = new CodaEventDecoder();
   DetectorEventDecoder   detectorDecoder = new DetectorEventDecoder();
   List<DetectorDataDgtz>        dataList = new ArrayList<DetectorDataDgtz>();
   IndexedList<List<Float>>          tdcs = new IndexedList<List<Float>>(4);
   
   CNDConstants                    ftofcc = new CNDConstants();  
   
   double[]                sed7=null,sed8=null;
   TreeMap<Integer,Object> map7=null,map8=null; 
   
   int nstr = cndPix[0].nstr;
       
   int nsa,nsb,tet,pedref;     
   int     thrcc = 20;
   short[] pulse = new short[100]; 
    
   public CNDReconstructionApp(String name, CNDPixels[] ctofPix) {
       super(name,ctofPix);
   }
   
   public void init() {
       System.out.println("CTOFReconstruction.init()");
       mondet = (String) mon.getGlob().get("mondet");
       is1 = CNDConstants.IS1;
       is2 = CNDConstants.IS2;
      iis1 = CNDConstants.IS1-1;
      iis2 = CNDConstants.IS2-1;
   } 
   
   public void clearHistograms() {
       
       for (int idet=0; idet<cndPix.length; idet++) {
           for (int is=is1 ; is<is2 ; is++) {
               cndPix[idet].strips.hmap2.get("H2_a_Hist").get(is,0,0).reset();
               cndPix[idet].strips.hmap2.get("H2_t_Hist").get(is,0,0).reset();
               for (int il=1 ; il<3 ; il++) {
                   cndPix[idet].strips.hmap2.get("H2_a_Hist").get(is,il,0).reset();
                   cndPix[idet].strips.hmap2.get("H2_a_Hist").get(is,il,3).reset();
                   cndPix[idet].strips.hmap2.get("H2_a_Hist").get(is,il,5).reset();
                   cndPix[idet].strips.hmap2.get("H2_t_Hist").get(is,il,0).reset();
               }
           }       
       } 
   }
   
   public void getMode7(int cr, int sl, int ch) {    
      app.mode7Emulation.configMode7(cr,sl,ch);
      this.nsa    = app.mode7Emulation.nsa;
      this.nsb    = app.mode7Emulation.nsb;
      this.tet    = app.mode7Emulation.tet;
      this.pedref = app.mode7Emulation.pedref;
   }
   
   public void addEvent(DataEvent event) {
       
       if(app.getDataSource()=="ET") this.updateRawData(event);
       
       if(app.getDataSource()=="EVIO") {
           if(app.isMC==true)  this.updateSimulatedData(event);
           if(app.isMC==false) this.updateRawData(event); 
       }
       
       if(app.getDataSource()=="XHIPO"||app.getDataSource()=="HIPO") this.updateHipoData(event);;
       
       if (app.isSingleEvent()) {
           findPixels();     // Process all pixels for SED
           processSED();
        } else {
           processPixels();  // Process only single pixels 
           processCalib();   // Quantities for display and calibration engine
        }
    }
   
//   public String detID(int layer) {
//       return "FTOF";
//   }
   
   public void updateHipoData(DataEvent event) {
       
       int evno;
       long phase = 0;
       int trigger = 0;
       long timestamp = 0;
       float offset = 0;
       
       clear(0); tdcs.clear();
       
       if(!app.isMC&&event.hasBank("RUN::config")){
           DataBank bank = event.getBank("RUN::config");
           timestamp = bank.getLong("timestamp",0);
           trigger   = bank.getInt("trigger",0);
           evno      = bank.getInt("event",0);         
           int phase_offset = 1;
           phase = ((timestamp%6)+phase_offset)%6;
           app.bitsec = (int) (Math.log10(trigger>>24)/0.301+1);
       }
       
       if (app.isMCB) offset=(float)124.25;
       
       if(event.hasBank("CTOF::tdc")){
           DataBank  bank = event.getBank("CTOF" + "::tdc");
           int rows = bank.rows();
           
           for(int i = 0; i < rows; i++){
               int  is = bank.getByte("sector",i);
               int  il = bank.getByte("layer",i);
               int  lr = bank.getByte("order",i);                       
               int  ip = bank.getShort("component",i);
               
               if(!tdcs.hasItem(is,il,lr-2,ip)) tdcs.add(new ArrayList<Float>(),is,il,lr-2,ip);
                   tdcs.getItem(is,il,lr-2,ip).add((float) bank.getInt("TDC",i)*24/1000+offset-phase*4);              
           }
       }
              
       if(event.hasBank("CTOF::adc")){
           DataBank  bank = event.getBank("CTOF::adc");
           int rows = bank.rows();
           for(int i = 0; i < rows; i++){
               int  is = bank.getByte("sector",i);
               int  il = bank.getByte("layer",i);
               int  lr = bank.getByte("order",i);
               int  ip = bank.getShort("component",i);
               int adc = bank.getInt("ADC",i);
               float t = bank.getFloat("time",i);               
               int ped = bank.getShort("ped", i);
               
               Float[] tdcc; float[] tdc;
               
               if (tdcs.hasItem(is,il,lr,ip)) {
                   List<Float> list = new ArrayList<Float>();
                   list = tdcs.getItem(is,il,lr,ip); tdcc=new Float[list.size()]; list.toArray(tdcc);
                   tdc  = new float[list.size()];
                   for (int ii=0; ii<tdcc.length; ii++) tdc[ii] = tdcc[ii];  
               } else {
                   tdc = new float[1];
               }
               for (int ii=0 ; ii< 100 ; ii++) {
                   float wgt = (ii==(int)(t/4)) ? adc:0;
                   cndPix[il-1].strips.hmap2.get("H2_a_Hist").get(is,lr+1,5).fill(ii,ip,wgt);
                   if (app.isSingleEvent()) {
                       cndPix[il-1].strips.hmap2.get("H2_a_Sevd").get(is,lr+1,0).fill(ii,ip,wgt);
                   }
               }
               
               if(isGoodSector(is)) fill(il-1, is, lr+1, ip, adc, tdc, t, (float) adc);    
           }
       }
       
   }  
   
   public void updateRawData(DataEvent event) {
       
       clear(0); tdcs.clear();
       
       app.decoder.initEvent(event);
       app.bitsec = app.decoder.bitsec;
       
       List<DetectorDataDgtz> adcDGTZ = app.decoder.getEntriesADC(DetectorType.CTOF);
       List<DetectorDataDgtz> tdcDGTZ = app.decoder.getEntriesTDC(DetectorType.CTOF);

       for (int i=0; i < tdcDGTZ.size(); i++) {
           DetectorDataDgtz ddd=tdcDGTZ.get(i);
           int is = ddd.getDescriptor().getSector();
           int il = ddd.getDescriptor().getLayer();
           int lr = ddd.getDescriptor().getOrder();
           int ip = ddd.getDescriptor().getComponent();
           if(!tdcs.hasItem(is,il,lr-2,ip)) tdcs.add(new ArrayList<Float>(),is,il,lr-2,ip);
               tdcs.getItem(is,il,lr-2,ip).add((float) ddd.getTDCData(0).getTime()*24/1000);              
       }
       
       for (int i=0; i < adcDGTZ.size(); i++) {
           DetectorDataDgtz ddd=adcDGTZ.get(i);
           int is = ddd.getDescriptor().getSector();
           if (isGoodSector(is)) {
           int cr = ddd.getDescriptor().getCrate();
           int sl = ddd.getDescriptor().getSlot();
           int ch = ddd.getDescriptor().getChannel();
           int il = ddd.getDescriptor().getLayer();
           int lr = ddd.getDescriptor().getOrder();
           int ip = ddd.getDescriptor().getComponent();
           int ad = ddd.getADCData(0).getADC();
           int pd = ddd.getADCData(0).getPedestal();
           int t0 = ddd.getADCData(0).getTimeCourse();  
           float tf = (float) ddd.getADCData(0).getTime();
           float ph = (float) ddd.getADCData(0).getHeight()-pd;
           short[]    pulse = ddd.getADCData(0).getPulseArray();
           
           Float[] tdcc; float[] tdc;
           
           if (tdcs.hasItem(is,il,lr,ip)) {
               List<Float> list = new ArrayList<Float>();
               list = tdcs.getItem(is,il,lr,ip); tdcc=new Float[list.size()]; list.toArray(tdcc);
               tdc  = new float[list.size()];
               for (int ii=0; ii<tdcc.length; ii++) tdc[ii] = tdcc[ii]-app.decoder.phase*4;  
           } else {
               tdc = new float[1];
           }
           
           getMode7(cr,sl,ch); 
           
           for (int ii=0 ; ii< pulse.length ; ii++) {
               cndPix[il-1].strips.hmap2.get("H2_a_Hist").get(is,lr+1,5).fill(ii,ip,pulse[ii]-pd);
               if (app.isSingleEvent()) {
                  cndPix[il-1].strips.hmap2.get("H2_a_Sevd").get(is,lr+1,0).fill(ii,ip,pulse[ii]-pd);
                  int w1 = t0-this.nsb ; int w2 = t0+this.nsa;
                  if (ad>0&&ii>=w1&&ii<=w2) cndPix[il-1].strips.hmap2.get("H2_a_Sevd").get(is,lr+1,1).fill(ii,ip,pulse[ii]-pd);                     
               }
            }
           
           if (pd>0) cndPix[il-1].strips.hmap2.get("H2_a_Hist").get(is,lr+1,3).fill(this.pedref-pd, ip);
           fill(il-1, is, lr+1, ip, ad, tdc, tf, ph);   
           
           }           
       }
       
       if (app.decoder.isHipoFileOpen) writeHipoOutput();
       
   }
   
   public void writeHipoOutput() {
       
       DataEvent  decodedEvent = app.decoder.getDataEvent();
       DataBank   header = app.decoder.createHeaderBank(decodedEvent);
       decodedEvent.appendBanks(header);
       app.decoder.writer.writeEvent(decodedEvent);
              
   } 
   
   public void updateSimulatedData(DataEvent event) {
       
      float tdcmax=100000;
      int nrows, adc, tdcc, fac;
      float mc_t=0,tdcf=0;
      float[] tdc = new float[1];
      
      String det[] = {"FTOF1A","FTOF1B","FTOF2B"}; // FTOF.xml banknames
      
      clear(0); 
      
      for (int idet=0; idet<det.length ; idet++) {
          
          if(event.hasBank(det[idet]+"::true")==true) {
              EvioDataBank bank  = (EvioDataBank) event.getBank(det[idet]+"::true"); 
              for(int i=0; i < bank.rows(); i++) mc_t = (float) bank.getDouble("avgT",i);          
          }
         
          if(event.hasBank(det[idet]+"::dgtz")==true) {            
              EvioDataBank bank = (EvioDataBank) event.getBank(det[idet]+"::dgtz");
              
              for(int i = 0; i < bank.rows(); i++){
                  float dum = (float)bank.getInt("TDC",i)-(float)mc_t*1000;
                  if (dum<tdcmax) tdcmax=dum; //Find latest hit time
              }
      
              for(int i = 0; i < bank.rows(); i++){
                  int is  = bank.getInt("sector",i);
                  int ip  = bank.getInt("paddle",i);
                      adc = bank.getInt("ADCL",i);
                     tdcc = bank.getInt("TDCL",i);
                     tdcf = tdcc;
                   tdc[0] = (((float)tdcc-(float)mc_t*1000)-tdcmax+1340000)/1000; 
                 fill(idet, is, 1, ip, adc, tdc, tdcf, tdcf); 
                      adc = bank.getInt("ADCR",i);
                     tdcc = bank.getInt("TDCR",i);
                     tdcf = tdcc;
                   tdc[0] = (((float)tdcc-(float)mc_t*1000)-tdcmax+1340000)/1000; 
                 fill(idet, is, 2, ip, adc, tdc, tdcf, tdcf); 
              }                     
          }         
       }         
   }
   
   public void clear(int idet) {
       
       for (int is=iis1 ; is<iis2 ; is++) {
           for (int il=0 ; il<2 ; il++) {
               cndPix[idet].nha[is][il] = 0;
               cndPix[idet].nht[is][il] = 0;
               for (int ip=0 ; ip<nstr ; ip++) {
                   cndPix[idet].strra[is][il][ip] = 0;
                   cndPix[idet].strrt[is][il][ip] = 0;
                   cndPix[idet].adcr[is][il][ip]  = 0;
                   cndPix[idet].tdcr[is][il][ip]  = 0;
                   cndPix[idet].tf[is][il][ip]    = 0;
                   cndPix[idet].ph[is][il][ip]    = 0;
               }
           }
       }
       
       if (app.isSingleEvent()) {
           for (int is=iis1 ; is<iis2 ; is++) {
               for (int il=0 ; il<2 ; il++) {
                    cndPix[idet].strips.hmap1.get("H1_a_Sevd").get(is+1,il+1,0).reset();
                    cndPix[idet].strips.hmap2.get("H2_a_Sevd").get(is+1,il+1,0).reset();
                    cndPix[idet].strips.hmap2.get("H2_a_Sevd").get(is+1,il+1,1).reset();
                    cndPix[idet].strips.hmap2.get("H2_a_Hist").get(is+1,il+1,5).reset();
                    cndPix[idet].strips.hmap2.get("H2_t_Sevd").get(is+1,il+1,0).reset();
               }
           }
       }   
   }

   
   public void fill(int idet, int is, int il, int ip, int adc, float[] tdc, float tdcf, float adph) {

       for (int ii=0; ii<tdc.length; ii++) {
           
       if(tdc[ii]>0&&tdc[ii]<500){
             cndPix[idet].nht[is-1][il-1]++; int inh = cndPix[idet].nht[is-1][il-1];
             if (inh>nstr) inh=nstr;
             cndPix[idet].ph[is-1][il-1][inh-1] = adph;
             cndPix[idet].tdcr[is-1][il-1][inh-1] = (float) tdc[ii];
             cndPix[idet].strrt[is-1][il-1][inh-1] = ip;
             cndPix[idet].ph[is-1][il-1][inh-1] = adph;
             cndPix[idet].strips.hmap2.get("H2_t_Hist").get(is,il,0).fill(tdc[ii],ip,1.0);
       }
       
       cndPix[idet].strips.hmap2.get("H2_a_Hist").get(is,il,1).fill(adc,tdc[ii],1.0);
          
       }
       
       if(adc>thrcc){
             cndPix[idet].nha[is-1][il-1]++; int inh = cndPix[idet].nha[is-1][il-1];
             if (inh>nstr) inh=nstr;
             cndPix[idet].adcr[is-1][il-1][inh-1] = adc;
             cndPix[idet].tf[is-1][il-1][inh-1] = tdcf;
             cndPix[idet].strra[is-1][il-1][inh-1] = ip;
             cndPix[idet].strips.hmap2.get("H2_a_Hist").get(is,il,0).fill(adc,ip,1.0);
             } 
   }
   
   public void processCalib() {
       
       int iL,iR,ipL,ipR;
       
       for (int is=is1 ; is<is2 ; is++) {
           for (int idet=0; idet<cndPix.length; idet++) {
                iL = cndPix[idet].nha[is-1][0];
                iR = cndPix[idet].nha[is-1][1];
               ipL = cndPix[idet].strra[is-1][0][0];
               ipR = cndPix[idet].strra[is-1][1][0];
               if ((iL==1&&iR==1)&&(ipL==ipR)) {
                   float gm = (float) Math.sqrt(cndPix[idet].adcr[is-1][0][0]*cndPix[idet].adcr[is-1][1][0]);
                   cndPix[idet].strips.hmap2.get("H2_a_Hist").get(is, 0, 0).fill(gm, ipL,1.0);
               }
               iL = cndPix[idet].nht[is-1][0];
               iR = cndPix[idet].nht[is-1][1];
              ipL = cndPix[idet].strrt[is-1][0][0];
              ipR = cndPix[idet].strrt[is-1][1][0];
              if ((iL==1&&iR==1)&&(ipL==ipR)) {
                  float td = cndPix[idet].tdcr[is-1][0][0]-cndPix[idet].tdcr[is-1][1][0];
                  cndPix[idet].strips.hmap2.get("H2_t_Hist").get(is, 0, 0).fill(td, ipL,1.0);
              }
           }
       }       
   }
   
   public void processSED() {
       
       for (int idet=0; idet<cndPix.length; idet++) {
       for (int is=iis1; is<iis2; is++) {
          for (int il=0; il<2; il++ ){;
          for (int n=0 ; n<cndPix[idet].nha[is][il] ; n++) {
              int ip=cndPix[idet].strra[is][il][n]; int ad=cndPix[idet].adcr[is][il][n];
              cndPix[idet].strips.hmap1.get("H1_a_Sevd").get(is+1,il+1,0).fill(ip,ad);
          }
          for (int n=0 ; n<cndPix[idet].nht[is][il] ; n++) {
              int ip=cndPix[idet].strrt[is][il][n]; float td=cndPix[idet].tdcr[is][il][n];
              double tdc = 0.25*(td-CNDConstants.TOFFSET);
              float  wgt = cndPix[idet].ph[is][il][n];
              wgt = (wgt > 0) ? wgt:1000;
              cndPix[idet].strips.hmap2.get("H2_t_Sevd").get(is+1,il+1,0).fill((float)tdc,ip,wgt);
          }
          }
       } 
       }
   } 
   
   public void findPixels() {      
   }
   
   public void processPixels() {       
   }

   public void makeMaps(int idet) {
       DetectorCollection<H2F> H2_a_Hist = new DetectorCollection<H2F>();
       DetectorCollection<H2F> H2_t_Hist = new DetectorCollection<H2F>();
       DetectorCollection<H1F> H1_a_Sevd = new DetectorCollection<H1F>();
       
       H2_a_Hist = cndPix[idet].strips.hmap2.get("H2_a_Hist");
       H2_t_Hist = cndPix[idet].strips.hmap2.get("H2_t_Hist");
       H1_a_Sevd = cndPix[idet].strips.hmap1.get("H1_a_Sevd");
       
       for (int is=is1;is<is2;is++) {
           for (int il=1 ; il<3 ; il++) {
               if (!app.isSingleEvent()) cndPix[idet].Lmap_a.add(is,il,0, toTreeMap(H2_a_Hist.get(is,il,0).projectionY().getData())); //Strip View ADC 
               if (!app.isSingleEvent()) cndPix[idet].Lmap_t.add(is,il,0, toTreeMap(H2_t_Hist.get(is,il,0).projectionY().getData())); //Strip View TDC 
               if  (app.isSingleEvent()) cndPix[idet].Lmap_a.add(is,il,0, toTreeMap(H1_a_Sevd.get(is,il,0).getData()));           
           }
       } 
       
       cndPix[idet].getLmapMinMax(is1,is2,1,0); 
       cndPix[idet].getLmapMinMax(is1,is2,2,0); 

   }  
   
   
}
    
    


