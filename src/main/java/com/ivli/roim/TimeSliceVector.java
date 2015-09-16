
package com.ivli.roim;

import java.util.ArrayList;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 *
 * @author likhachev
 */
public class TimeSliceVector implements java.io.Serializable {

    private static final long serialVersionUID = 42L;
                    
    ArrayList<PhaseInformation> iPhases; 
      //frame start time in millisecons from series begin
    ArrayList<Long>             iSlices; 
        
    public TimeSliceVector(Attributes aAttr) {        
        iPhases = new ArrayList();
        iSlices = new ArrayList();        
        
        Sequence pid = (Sequence)aAttr.getValue(Tag.PhaseInformationSequence);
        
        if (null != pid) {
        
            for (Attributes a : pid) {
                int fd = a.getInt(Tag.ActualFrameDuration, 1);     
                int nf = a.getInt(Tag.NumberOfFramesInPhase, 1);  
                iPhases.add(new PhaseInformation(nf, fd));
            }  
        }
        
        fillSlicesArray();
        
        logger.debug(iSlices);
    }
    
       
    public TimeSliceVector(ArrayList<PhaseInformation> aP) {                
        iPhases =  new ArrayList();
        
        for (PhaseInformation p: aP)
            iPhases.add(new PhaseInformation(p));
        
        ///iPhases.
        iSlices = new ArrayList(); 
        
        fillSlicesArray();
    }
  
    private void fillSlicesArray() {
        long n = 0L;
       
        iSlices.clear();
       
        for (PhaseInformation p : iPhases) {            
            for (int j = 0; j < p.iNumberOfFrames; ++j) 
                iSlices.add(n += p.iFrameDuration);                 
        } 
        
        assert (noOfFrames() == iSlices.size());    
    }
    
    boolean isValidFrameNumber(int aFrame) {
        return aFrame >=0 && aFrame < iSlices.size();
    }
         
    public int noOfFrames() {
        int ret = 0;
        for (PhaseInformation p : iPhases) 
            ret += p.iNumberOfFrames;
        return ret;   
    }
    
    public int noOfPhases() {     
        return iPhases.size();   
    }
    
    public long phaseDuration(int aPhase) {
        return iPhases.get(aPhase).iFrameDuration * iPhases.get(aPhase).iNumberOfFrames;
    }
    
    public int phaseFrame(int aFrameNumber) {
        int ctr = 0, phase = 1;
        
        for (PhaseInformation p:iPhases) {
            if (aFrameNumber > ctr && aFrameNumber < (ctr += p.iNumberOfFrames))
                return phase;
            ++phase;
        }
        
        return 0;
    }
    
    public long phaseStarts(int aPhaseNumber) {
        long ret = 0L;
        
        for (int i = 0; i < iPhases.size() && i <= aPhaseNumber; ++i)             
            ret += phaseDuration(i);    
        
        return ret;
    }
            
    public long frameLapse(int aStart, int aEnd) {    
        return frameStarts(aEnd) - frameStarts(aStart);                 
    } 
    
    public long frameStarts(int aFrameNumber) {
        if (0 == aFrameNumber)
            return 0L;
        else {
            long ret   = 0L;
            int  frame = 0;   
            int  phase = 1, ctr = 0;
            
            for (PhaseInformation p : iPhases) {
                if (aFrameNumber > ctr &&  aFrameNumber < (ctr += p.iNumberOfFrames))                                          
                    return ret += (aFrameNumber - ctr) * p.iFrameDuration;
                
                ret += p.iNumberOfFrames * p.iFrameDuration;                        
            }
  
            return ret;
        }
    }
    
    public long duration() { // does it make sense to cache this field
        long ret = 0L;
        for (PhaseInformation p : iPhases)
            ret += p.duration();
        return ret;
    }
    
      //0, 1, 2 etc
    public int phaseNumber(long uSecFromStart) {        
        assert (uSecFromStart >= 0L && uSecFromStart < duration());
           
        long elapsed = 0L;
        
        int n = 0;
        
        for (; n < iPhases.size() ; ++n)
            if (uSecFromStart < elapsed)
                break;
            else
                elapsed += iPhases.get(n).duration();
                
        return n-1;
    }
    
    public int frameNumber(long uSecFromStart) {        
        if (0 == uSecFromStart)
            return 0;
        else
            return 0;
    }
    
    private void resamplePhase(PhaseInformation aP, int newFrameDuration) {
        if (newFrameDuration > aP.duration())
            throw new IllegalArgumentException("cannot step over phase boundary");
        
        int newNoOfFrames = aP.iNumberOfFrames * aP.iFrameDuration / newFrameDuration;
        aP.iNumberOfFrames = newNoOfFrames;
        aP.iFrameDuration = newFrameDuration;
    }
    
    public void resample(int newFrameDuration) {        
        iPhases.stream().forEach((i) -> {
            resamplePhase(i, newFrameDuration);
        });
            
        fillSlicesArray();
    }
    
    
              
    public void resample(int aPhase, int newFrameDuration) {
        resamplePhase(iPhases.get(aPhase), newFrameDuration);        
        fillSlicesArray();
    }
    
    
    
    private static final Logger logger = LogManager.getLogger(TimeSliceVector.class);
}
