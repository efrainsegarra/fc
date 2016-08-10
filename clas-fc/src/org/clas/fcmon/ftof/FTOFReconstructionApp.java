package org.clas.fcmon.ftof;

import static java.lang.System.out;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

//clas12
import org.clas.fcmon.tools.ECPixels;
import org.clas.fcmon.tools.FADCFitter;
import org.clas.fcmon.tools.FCApplication;
import org.jlab.clas.detector.DetectorCollection;
import org.jlab.clas12.detector.FADCConfigLoader;
import org.root.histogram.H1D;
import org.root.histogram.H2D;

//clas12rec
import org.jlab.detector.decode.CodaEventDecoder;
import org.jlab.detector.decode.DetectorDataDgtz;
import org.jlab.detector.decode.DetectorEventDecoder;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioDataBank;

import org.clas.fcmon.jroot.*;

public class FTOFReconstructionApp extends FCApplication {
    
   FADCConfigLoader fadc  = new FADCConfigLoader();
   String          mondet ;
   Boolean           inMC ;
   int              detID=0 ;
   FADCFitter     fitter  = new FADCFitter(1,15);
   String BankType        ;

   CodaEventDecoder            newdecoder = new CodaEventDecoder();
   DetectorEventDecoder   detectorDecoder = new DetectorEventDecoder();
   List<DetectorDataDgtz>  detectorData   = new ArrayList<DetectorDataDgtz>();
  
   DetectorCollection<H1D> H1_a_Sevd = new DetectorCollection<H1D>();
   DetectorCollection<H1D> H1_t_Sevd = new DetectorCollection<H1D>();
   DetectorCollection<H2D> H2_a_Hist = new DetectorCollection<H2D>();
   DetectorCollection<H2D> H2_t_Hist = new DetectorCollection<H2D>();
   DetectorCollection<H2D> H2_a_Sevd = new DetectorCollection<H2D>();
   
   public DetectorCollection<TreeMap<Integer,Object>> Lmap_a = new DetectorCollection<TreeMap<Integer,Object>>();
   public DetectorCollection<TreeMap<Integer,Object>> Lmap_t = new DetectorCollection<TreeMap<Integer,Object>>();
   
   double[]                sed7=null,sed8=null;
   TreeMap<Integer,Object> map7=null,map8=null; 
   
   int nstr = ftofPix[0].nstr;
   
   int        nha[][] = new    int[6][2];
   int        nht[][] = new    int[6][2];
   int    strra[][][] = new    int[6][2][nstr]; 
   int    strrt[][][] = new    int[6][2][nstr]; 
   int     adcr[][][] = new    int[6][2][nstr];      
   double  tdcr[][][] = new double[6][2][nstr];  
     
   int nsa,nsb,tet,pedref;     
   int     thrcc = 20;
   short[] pulse = new short[100]; 
    
   public FTOFReconstructionApp(String name, FTOFPixels[] ftofPix) {
       super(name,ftofPix);
   }
   
   public void init() {
       System.out.println("CCReconstruction.init()");
       fadc.load("/daq/fadc/ltcc",10,"default");
       mondet =           (String) mon.getGlob().get("mondet");
       inMC   =          (Boolean) mon.getGlob().get("inMC");
//       detID  =              (int) mon.getGlob().get("detID"); 
   }
   
   public void clearHistograms() {
       
       for (int is=1 ; is<7 ; is++) {
           for (int il=1 ; il<3 ; il++) {
                H2_a_Hist.get(is,il,0).reset();
                H2_a_Hist.get(is,il,3).reset();
                H2_a_Hist.get(is,il,5).reset();
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
   
   public void addEvent(EvioDataEvent event) {
      
      if(event.hasBank("EC"+"::dgtz")==true) {
          this.updateSimulatedData(event);
      } else {
          this.updateRealData(event);         
      }
      
      if (app.isSingleEvent()) {
         findPixels();     // Process all pixels for SED
         processSED();
      } else {
         processPixels();  // Process only single pixels 
      }
   }
   
   public String detID(int layer) {
       return "FTOF";
   }
   
   public void updateRealData(EvioDataEvent event){

      int adc,ped,npk;
      double tdc=0,tdcf=0;
      String AdcType ;
      
      List<DetectorDataDgtz>  dataSet = newdecoder.getDataEntries((EvioDataEvent) event);
      
      detectorDecoder.translate(dataSet);   
      detectorDecoder.fitPulses(dataSet);
      this.detectorData.clear();
      this.detectorData.addAll(dataSet);
      
      clear();
      int nsum=0;
      
      for (DetectorDataDgtz strip : detectorData) {
         if(strip.getDescriptor().getType().getName()=="FTOF") {
            adc=ped=pedref=npk=0 ; tdc=tdcf=0;
            int icr = strip.getDescriptor().getCrate(); 
            int isl = strip.getDescriptor().getSlot(); 
            int ich = strip.getDescriptor().getChannel(); 
            int is  = strip.getDescriptor().getSector();
            int il  = strip.getDescriptor().getLayer();  
            int ip  = strip.getDescriptor().getComponent();
            int iord= strip.getDescriptor().getOrder(); 
            
            if (detID(il)==mondet) {
                
            if (strip.getTDCSize()>0) {
                tdc = strip.getTDCData(0).getTime()*24./1000.;
            }
            
            if (strip.getADCSize()>0) {     
              
               AdcType = strip.getADCData(0).getPulseSize()>0 ? "ADCPULSE":"ADCFPGA";
               
               if(AdcType=="ADCFPGA") { // FADC MODE 7
                  adc = strip.getADCData(0).getIntegral();
                  ped = strip.getADCData(0).getPedestal();
                  npk = strip.getADCData(0).getHeight();
                 tdcf = strip.getADCData(0).getTime();            
                  getMode7(icr,isl,ich);
                  if (app.mode7Emulation.User_pedref==0) adc = (adc-ped*(this.nsa+this.nsb));
                  if (app.mode7Emulation.User_pedref==1) adc = (adc-this.pedref*(this.nsa+this.nsb));
               }   
               
               if (AdcType=="ADCPULSE") { // FADC MODE 1
                  for (int i=0 ; i<strip.getADCData(0).getPulseSize();i++) {               
                     pulse[i] = (short) strip.getADCData(0).getPulseValue(i);
                  }              
                  getMode7(icr,isl,ich);
                  if (app.mode7Emulation.User_pedref==0) fitter.fit(this.nsa,this.nsb,this.tet,0,pulse);                  
                  if (app.mode7Emulation.User_pedref==1) fitter.fit(this.nsa,this.nsb,this.tet,pedref,pulse);                    
                  adc = fitter.adc;
                  ped = fitter.pedsum;
                  for (int i=0 ; i< pulse.length ; i++) {
                     ftofPix[0].strips.hmap2.get("H2_a_Hist").get(is,il,5).fill(i,ip,pulse[i]-this.pedref);
                     if (app.isSingleEvent()) {
                        ftofPix[0].strips.hmap2.get("H2_a_Sevd").get(is,il,0).fill(i,ip,pulse[i]-this.pedref);
                        int w1 = fitter.t0-this.nsb ; int w2 = fitter.t0+this.nsa;
                        if (fitter.adc>0&&i>=w1&&i<=w2) ftofPix[0].strips.hmap2.get("H2_a_Sevd").get(is,il,1).fill(i,ip,pulse[i]-this.pedref);                     
                     }
                  }
               }               
               if (ped>0) ftofPix[0].strips.hmap2.get("H2_a_Hist").get(is,il,3).fill(this.pedref-ped, ip);
             }           
             fill(is, il, ip, adc, tdc, tdcf);    
            }
         }
      }
   }
   
   public void updateSimulatedData(EvioDataEvent event) {
       
      float tdcmax=100000;
      int nrows, adc, fac;
      double mc_t=0.,tdc=0,tdcf=0;
      Boolean bypass=false;
         
      if(event.hasBank(mondet+"::true")==true) {
         EvioDataBank bank  = (EvioDataBank) event.getBank(mondet+"::true");      
         for(int i=0; i < bank.rows(); i++) mc_t = bank.getDouble("avgT",i);
         fac = 1;
      } else {
         mc_t = 0;
         fac = 6;
      }
                
      inMC = true; mon.putGlob("inMC",true); 
             
      clear();
        
      EvioDataBank bank = (EvioDataBank) event.getBank(mondet+"::dgtz");
      nrows = bank.rows();

      // Use latest hit time for time reference (tdcmax).
      
      for(int i = 0; i < nrows; i++){
         float dum = (float)bank.getInt("TDC",i)-(float)mc_t*1000;
         if (dum<tdcmax) tdcmax=dum;
      }      
       
      for(int i = 0; i < nrows; i++){
         int is  = bank.getInt("sector",i);
         int ip  = bank.getInt("strip",i);
         int ic  = bank.getInt("stack",i);     
         int il  = bank.getInt("view",i);  
             adc = bank.getInt("ADC",i)/fac;
        int tdcc = bank.getInt("TDC",i);
            tdcf = tdcc;
              il = il+(ic-1)*3;
             tdc = (((float)tdcc-(float)mc_t*1000)-tdcmax+1340000)/1000; 
             bypass = false;
        if((ic==1||ic==2)&&bypass==false) fill(is, il, ip, adc, tdc, tdcf); 
      }
         
   }
   
   public void clear() {
       
       for (int is=0 ; is<6 ; is++) {
           for (int il=0 ; il<2 ; il++) {
               nha[is][il] = 0;
               nht[is][il] = 0;
               for (int ip=0 ; ip<nstr ; ip++) {
                   strra[is][il][ip] = 0;
                   strrt[is][il][ip] = 0;
                    adcr[is][il][ip] = 0;
                    tdcr[is][il][ip] = 0;
               }
           }
       }
       
       if (app.isSingleEvent()) {
           for (int is=0 ; is<6 ; is++) {
               for (int il=0 ; il<2 ; il++) {
                    ftofPix[0].strips.hmap1.get("H1_a_Sevd").get(is+1,il+1,0).reset();
                    ftofPix[0].strips.hmap2.get("H2_a_Sevd").get(is+1,il+1,0).reset();
                    ftofPix[0].strips.hmap2.get("H2_a_Sevd").get(is+1,il+1,1).reset();
               }
           }
       }   
   }
   
   public void fill(int is, int il, int ip, int adc, double tdc, double tdcf) {
           
       if(tdc>1200&&tdc<1500){
            nht[is-1][il-1]++; int inh = nht[is-1][il-1];
           tdcr[is-1][il-1][inh-1] = tdc;
          strrt[is-1][il-1][inh-1] = ip;
             ftofPix[0].strips.hmap2.get("H2_t_Hist").get(is,il,0).fill(tdc,ip,1.0);
             }
       if(adc>thrcc){
            nha[is-1][il-1]++; int inh = nha[is-1][il-1];
           adcr[is-1][il-1][inh-1] = adc;
          strra[is-1][il-1][inh-1] = ip;
             ftofPix[0].strips.hmap2.get("H2_a_Hist").get(is,il,0).fill(adc,ip,1.0);
             } 
   }
   
   public void processSED() {
       
       for (int is=0; is<6; is++) {
          for (int il=0; il<2; il++ ){;
              for (int n=0 ; n<nha[is][il] ; n++) {
                  int ip=strra[is][il][n]; int ad=adcr[is][il][n];
                  ftofPix[0].strips.hmap1.get("H1_a_Sevd").get(is+1,il+1,0).fill(ip,ad);
              }
          }
       }           
   } 
   
   public void findPixels() {
       
   }
   
   public void processPixels() {
       
   }

   public TreeMap<Integer, Object> toTreeMap(double dat[]) {
       TreeMap<Integer, Object> hcontainer = new TreeMap<Integer, Object>();
       hcontainer.put(1, dat);
       double[] b = Arrays.copyOf(dat, dat.length);
       double min=100000,max=0;
       for (int i =0 ; i < b.length; i++){
           if (b[i] !=0 && b[i] < min) min=b[i];
           if (b[i] !=0 && b[i] > max) max=b[i];
       }
       // Arrays.sort(b);
       // double min = b[0]; double max=b[b.length-1];
       if (min<=0) min=0.01;
       hcontainer.put(2, min);
       hcontainer.put(3, max);
       return hcontainer;        
   }

   public void makeMaps() {

       H2_a_Hist = ftofPix[0].strips.hmap2.get("H2_a_Hist");
       H1_a_Sevd = ftofPix[0].strips.hmap1.get("H1_a_Sevd");
       
       for (int is=1;is<7;is++) {
           for (int il=1 ; il<3 ; il++) {
               if (!app.isSingleEvent()) Lmap_a.add(is,il,0, toTreeMap(H2_a_Hist.get(is,il,0).projectionY().getData())); //Strip View ADC 
               if  (app.isSingleEvent()) Lmap_a.add(is,il,0, toTreeMap(H1_a_Sevd.get(is,il,0).getData()));           
           }
       }   
   }    
}
    
    


