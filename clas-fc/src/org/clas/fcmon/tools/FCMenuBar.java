package org.clas.fcmon.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.clas.fcmon.tools.EventControl;

@SuppressWarnings("serial")

public class FCMenuBar extends JMenuBar  {
	
    public MonitorApp      app = null; 
    
    MenuFile        menu1 = new MenuFile();
    MenuTriggerBits menu2 = new MenuTriggerBits();
    
	public FCMenuBar() {		
		this.add(menu1.menu);
		this.add(menu2.menu);
	}
	
    public void setApplicationClass(MonitorApp app) {
        this.app = app;
    }	
    
	public class MenuFile extends JMenu implements ActionListener {
		
	    public MenuFile() {
	    	    createMenu();
	    }
	    			
        JMenu             menu = new JMenu("File");
        JMenu          ET_open = new JMenu("Attach to ET");
        JMenu          XM_open = new JMenu("xMsg Ring");
    
        JMenuItem    file_open = new JMenuItem("Load EVIO or HIPO File");    
        JMenuItem           s1 = new JMenuItem("Sector 1");
        JMenuItem           s2 = new JMenuItem("Sector 2");
        JMenuItem           s3 = new JMenuItem("Sector 3");
        JMenuItem           s4 = new JMenuItem("Sector 4");
        JMenuItem           s5 = new JMenuItem("Sector 5");
        JMenuItem           s6 = new JMenuItem("Sector 6");
        JMenuItem         ctof = new JMenuItem("CTOF");
        JMenuItem          cnd = new JMenuItem("CND");
        JMenuItem          sd5 = new JMenuItem("clondaq3");
        JMenuItem          sd6 = new JMenuItem("clondaq6");
        JMenuItem           x0 = new JMenuItem("EVIO");
        JMenuItem           x1 = new JMenuItem("HIPO");
    
        String          ethost = null;
        String          etfile = null;
    
        String      fileformat = null;
        File          eviofile = null;
  
        public JMenu getMenu() {
        	    return menu;
        }
    
        public void createMenu() {
    	
            menu.add(file_open);
            menu.add(ET_open);
            menu.add(XM_open);
           
            ET_open.add(s1);
            ET_open.add(s2);
            ET_open.add(s3);
            ET_open.add(s4);
            ET_open.add(s5);
            ET_open.add(s6);
            ET_open.add(ctof);
            ET_open.add(cnd);
            ET_open.add(sd5);
            ET_open.add(sd6);
     
            XM_open.add(x0);
            XM_open.add(x1);
   
            file_open.addActionListener(this);   
        
            s1.addActionListener(this);
            s2.addActionListener(this);
            s3.addActionListener(this);
            s4.addActionListener(this);
            s5.addActionListener(this);
            s6.addActionListener(this);   
          ctof.addActionListener(this);
           cnd.addActionListener(this);
           sd5.addActionListener(this);     
           sd6.addActionListener(this);     
            x0.addActionListener(this);      
            x1.addActionListener(this);   
          
        }
    
	@Override
	    public void actionPerformed(ActionEvent e) {
    	        if(e.getActionCommand().compareTo("Sector 1")==0) {ethost="adcecal1";etfile="/tmp/et_sys_clasprod";}
    	        if(e.getActionCommand().compareTo("Sector 2")==0) {ethost="adcecal2";etfile="/tmp/et_sys_clasprod";}
    	        if(e.getActionCommand().compareTo("Sector 3")==0) {ethost="adcecal3";etfile="/tmp/et_sys_clasprod";}
    	        if(e.getActionCommand().compareTo("Sector 4")==0) {ethost="adcecal4";etfile="/tmp/et_sys_clasprod";}
    	        if(e.getActionCommand().compareTo("Sector 5")==0) {ethost="adcecal5";etfile="/tmp/et_sys_clasprod";}
            if(e.getActionCommand().compareTo("Sector 6")==0) {ethost="adcecal6";etfile="/tmp/et_sys_clasprod";}      
            if(e.getActionCommand().compareTo("CTOF")==0)     {ethost="adcctof1";etfile="/tmp/et_sys_clasltcc";}      
            if(e.getActionCommand().compareTo("CND")==0)      {ethost="adccnd1" ;etfile="/tmp/et_sys_cndtest";}      
            if(e.getActionCommand().compareTo("clondaq3")==0) {ethost="clondaq3";etfile="/tmp/et_sys_clasprod";}       
            if(e.getActionCommand().compareTo("clondaq6")==0) {ethost="clondaq6";etfile="/tmp/et_sys_clasprod";}       
    	        if(ethost!=null) app.eventControl.openEtFile(ethost,etfile);    	
            if(e.getActionCommand().compareTo("Load EVIO or HIPO File")==0) this.chooseEvioFile();
            if(e.getActionCommand().compareTo("EVIO")==0) app.eventControl.openXEvioRing();
            if(e.getActionCommand().compareTo("HIPO")==0) app.eventControl.openXHipoRing();
	    }
	
        public void chooseEvioFile() {
            	final JFileChooser fc = new JFileChooser();
    	
    	        fc.setFileFilter(new javax.swing.filechooser.FileFilter(){
    		
    	    	    public boolean accept(File f) {
    			    return f.getName().toLowerCase().endsWith(".evio") || 
    			           f.getName().toLowerCase().endsWith(".hipo") || 
    			           f.isDirectory();
    		    }
    	    	
            public String getDescription() {
                    return "EVIO CLAS data format";
            }
            });
    	
    	        String currentDir = System.getenv("PWD");
            if(currentDir!=null) fc.setCurrentDirectory(new File(currentDir));
            int returnVal = fc.showOpenDialog(this);
        
            if (returnVal == JFileChooser.APPROVE_OPTION) {
          	    eviofile     =       fc.getSelectedFile();
                app.eventControl.openEvioFile(eviofile);
            }
        }
        
	}
	
	public class MenuTriggerBits extends JMenu implements ActionListener {
        
        public MenuTriggerBits() {
            createMenu();
		}
		
        JMenu menu = new JMenu("TriggerBits");
		
        String TriggerDef[] = { "Electron",
		        "Electron S1","Electron S2","Electron S3","Electron S4","Electron S5","Electron S6",
		        "HTCC S1","HTCC S2","HTCC S3","HTCC S4","HTCC S5","HTCC S6",
		        "PCAL S1","PCAL S2","PCAL S3","PCAL S4","PCAL S5","PCAL S6",
		        "ECAL S1","ECAL S2","ECAL S3","ECAL S4","ECAL S5","ECAL S6",
		        "HT.PC","HT.EC","PC.EC","FTOF.PC","Unused","Unused",
		        "1K Pulser"};
        
        public JMenu getMenu() {
        	    return menu;
        }	
        
        public void createMenu() {
        	
            for (int i=0; i<32; i++) {
	
               JCheckBoxMenuItem bb = new JCheckBoxMenuItem(TriggerDef[i]);  
               final Integer bit = new Integer(i);
               bb.addItemListener(new ItemListener() {
               public void itemStateChanged(ItemEvent e) {        	
                   if(e.getStateChange() == ItemEvent.SELECTED) {
            	          app.setTriggerMask(bit);
                   } else {
            	          app.clearTriggerMask(bit);
                   };
               }
               });    
               menu.add(bb); 
            }
        }

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			
		}
	}
             
}

