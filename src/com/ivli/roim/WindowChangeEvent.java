/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ivli.roim;

import java.util.EventObject;
/**
 *
 * @author likhachev
 */
public class WindowChangeEvent extends EventObject {
   
    public WindowChangeEvent(Object aO, Window aW, double aMin, double aMax, boolean aRC) {
        super(aO); 
        iWindow = aW;
        iMin = aMin;
        iMax = aMax;
        iRangeChanged = aRC;
    }
    
    public final Window getWindow() {return iWindow;}   
    public final double getMin() {return iMin;}
    public final double getMax() {return iMax;}
    public final boolean isRangeChanged() {return iRangeChanged;}
     
    private final boolean iRangeChanged;
    private final double  iMin;
    private final double  iMax;
    private final Window  iWindow; 
}
