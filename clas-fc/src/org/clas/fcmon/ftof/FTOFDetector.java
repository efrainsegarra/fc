package org.clas.fcmon.ftof;

import org.clas.fcmon.detector.view.DetectorShape2D;
import org.clas.fcmon.tools.FCDetector;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Path3D;

public class FTOFDetector extends FCDetector {
    
    public FTOFDetector(String name , FTOFPixels[] ftofPix) {
        super(name, ftofPix);       
     } 
    
    public void init(int is1, int is2) {
        initDetector(is1,is2);
   }
    
    public void initButtons() {
        
        System.out.println("FTOFDetector.initButtons()");
        
        initMapButtons(0, 0);
        initMapButtons(1, 0);
        initViewButtons(0, 0);
        app.getDetectorView().setFPS(10);
        app.setSelectedTab(1); 
        
    }   
    
    public void initDetector(int is1, int is2) {
        
        for(int id=0; id<1; id++){
        for(int is=is1; is<is2; is++) {
            for(int ip=0; ip<ftofPix[id].nstr ; ip++) app.getDetectorView().getView().addShape("LR",getPaddle(id,is,1,ip));
            for(int ip=0; ip<ftofPix[id].nstr ; ip++) app.getDetectorView().getView().addShape("LR",getPaddle(id,is,2,ip));
            for(int ip=0; ip<ftofPix[id].nstr ; ip++) app.getDetectorView().getView().addShape("L",getPaddle(id,is,1,ip));
            for(int ip=0; ip<ftofPix[id].nstr ; ip++) app.getDetectorView().getView().addShape("R",getPaddle(id,is,2,ip));
        }   
        }
        
        app.getDetectorView().getView().addDetectorListener(mon);
        
        for(String layer : app.getDetectorView().getView().getLayerNames()){
            app.getDetectorView().getView().setDetectorListener(layer,mon);
         }
        
        addButtons("DET","View","PANEL1A.0.PANEL1B.1.PANEL2.2");
        addButtons("LAY","View","LR.0.L.1.R.2");
        addButtons("PMT","Map","EVT.0.ADC.1.TDC.2.STATUS.3");
        addButtons("PIX","Map","EVT.0.ADC.1.TDC.2.STATUS.3");
        
        app.getDetectorView().addMapButtons();
        app.getDetectorView().addViewButtons(); 
        
    }    
    
    public DetectorShape2D getPaddle(int det, int sector, int layer, int paddle) {
        
        DetectorShape2D shape = new DetectorShape2D(DetectorType.FTOF,sector,layer,paddle);     
        Path3D shapePath = shape.getShapePath();
        
        int off = (layer-1)*ftofPix[det].nstr;
        
        for(int j = 0; j < 4; j++){
            shapePath.addPoint(ftofPix[det].ftof_xpix[j][paddle+off][sector-1],ftofPix[det].ftof_ypix[j][paddle+off][sector-1],0.0);
        }
        return shape;       
    }
    
}
