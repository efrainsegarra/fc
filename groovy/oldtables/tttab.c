#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
 
// https://github.com/forcar/fc/blob/master/et2rzh/ttfc.h
#include "ttfc.h"

int main(int argc, char **argv) 
  {
    int ind=1;
    int tag=500;
    int stat=0;
    int nchan[2]={16,128};
    int type,sector,slot,chan,roc;
    int detector=0;


    if (argc == 1) {printf("Usage: tttab 1(ECAL),2(PCAL),3(FTOF1A),4(FTOF1B)\n"); exit(1) ;}
    if (argc == 2) {detector=atoi(argv[1]);}

    printf("#-----------------------------------------------------------------------------\n");
    printf("# TRANSLATION TABLE\n");
    printf("#-----------------------------------------------------------------------------\n");
    printf("# Detector - Sector - Layer - Component - CRATE - SLOT - CHANNEL\n");
    printf("#-----------------------------------------------------------------------------\n");

    if (detector==1) 
      {


    for(type=0;type<2;type++)
      {
	for(sector=1;sector<7;sector++)
	  {
	    roc=(sector-1)*6+type+1;
	    for(slot=0;slot<22;slot++) 
	      {
		for(chan=0;chan<nchan[type];chan++)
		  {
		    if (type==0&&adclayerecal[slot][chan]>0) 
		      {
			printf("ECAL %8d %8d %8d %8d %8d %8d\n",sector,adclayerecal[slot][chan],adcstripecal[slot][chan],roc,slot,chan);  
		      }
		    if (type==1&&tdclayerecal[slot][chan]>0) 
		      {
			printf("ECAL %8d %8d %8d %8d %8d %8d\n",sector,tdclayerecal[slot][chan],tdcstripecal[slot][chan],roc,slot,chan);  
		      }
		  }
	      }
	  }
      }
      }

    if (detector==2) 
      {

    for(type=0;type<2;type++)
      {
	for(sector=1;sector<7;sector++)
	  {
	    roc=(sector-1)*6+type+3;
	    for(slot=0;slot<22;slot++) 
	      {
		for(chan=0;chan<nchan[type];chan++)
		  {
		    if (type==0&&adclayerpcal[slot][chan]>0) 
		      {
			printf("PCAL %8d %8d %8d %8d %8d %8d\n",sector,adclayerpcal[slot][chan],adcstrippcal[slot][chan],roc,slot,chan);  
		      }
		    if (type==1&&tdclayerpcal[slot][chan]>0) 
		      {
			printf("PCAL %8d %8d %8d %8d %8d %8d\n",sector,tdclayerpcal[slot][chan],tdcstrippcal[slot][chan],roc,slot,chan);  
		      }
		  }
	      }
	  }
      }
      }

    if (detector==3) 
      {

    for(type=0;type<2;type++)
      {
	for(sector=1;sector<7;sector++)
	  {
	    roc=(sector-1)*6+type+5;
	    for(slot=0;slot<22;slot++) 
	      {
		for(chan=0;chan<nchan[type];chan++)
		  {
		    if (type==0&&adclayerftof[slot][chan]==2) 
		      {
			printf("FTOF1A %8d %8d %8d %8d %8d %8d\n",sector,adclrftof[slot][chan],adcslabftof[slot][chan],roc,slot,chan);  
		      }
		    if (type==1&&tdclayerftof[slot][chan]==2) 
		      {
			printf("FTOF1A %8d %8d %8d %8d %8d %8d\n",sector,tdclrftof[slot][chan],tdcslabftof[slot][chan],roc,slot,chan); 
		      }

		  }
	      }
	  }
      }
      }  
    if (detector==4) 
      {

    for(type=0;type<2;type++)
      {
	for(sector=1;sector<7;sector++)
	  {
	    roc=(sector-1)*6+type+5;
	    for(slot=0;slot<22;slot++) 
	      {
		for(chan=0;chan<nchan[type];chan++)
		  {
		    if (type==0&&adclayerftof[slot][chan]==1) 
		      {
			printf("FTOF1B %8d %8d %8d %8d %8d %8d\n",sector,adclrftof[slot][chan],adcslabftof[slot][chan],roc,slot,chan);  
		      }
		    if (type==1&&tdclayerftof[slot][chan]==1) 
		      {
			printf("FTOF1B %8d %8d %8d %8d %8d %8d\n",sector,tdclrftof[slot][chan],tdcslabftof[slot][chan],roc,slot,chan); 
		      }

		  }
	      }
	  }
      }
      }  

    printf("#-----------------------------------------------------------------------------\n");
    printf("# END OF Translation TABLE\n");
    printf("#-----------------------------------------------------------------------------\n");
}

