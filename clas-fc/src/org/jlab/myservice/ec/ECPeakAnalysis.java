/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.myservice.ec;

import java.util.List;

import org.jlab.myservice.ec.ECPeak;
import org.jlab.myservice.ec.ECPeakAnalysis;

/**
 *
 * @author gavalian
 */
public class ECPeakAnalysis {
    
    public static int getPeakSplitIndex(List<ECPeak> peaks){
        int index = -1;
        for(int i = 0; i < peaks.size(); i++){
            int si = peaks.get(i).getSplitStrip();
            if(si>=0) return i;
        }
        return index;
    }
    
    public static void splitPeaks(List<ECPeak> peaks){
        boolean isSplited = true;
        while(isSplited==true){
            int index = ECPeakAnalysis.getPeakSplitIndex(peaks);
            if(index<0){
                isSplited = false;
            } else {
                ECPeak  peak = peaks.get(index);
                peaks.remove(index);
                int strip = peak.getSplitStrip();
                List<ECPeak> twoPeaks = peak.splitPeak(strip);
                peaks.addAll(twoPeaks);
            }
        }
    }
    
}
