/*
 * 
 */
package com.ivli.roim;


/**
 *
 * @author likhachev
 */

public class Series extends java.util.ArrayList<Measure> {
   
    String iName;
    
    Series(String aName) {
        iName = aName;
    }
    
    public int getNumFrames() {
        return size();
    }
    
    public void fit() {
        
    }
}
