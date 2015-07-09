/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ivli.roim;

import java.io.Serializable;

/**
 *
 * @author likhachev
 */
public class Window implements Serializable, Cloneable {      

    private static final long serialVersionUID = 42L;

    static final double WINDOW_WIDTH_MINIMUM = 1.;
    
    private double iLevel = .5;
    private double iWidth = WINDOW_WIDTH_MINIMUM;


    private Window(){}
    public Window(Window aW) {iLevel = aW.iLevel; iWidth = aW.iWidth;}
    public Window(double aL, double aW) {
        iLevel = aL; 
        iWidth = Math.max(WINDOW_WIDTH_MINIMUM, aW);   
    }

    public double getLevel()  {return iLevel;}
    public double getWidth()  {return iWidth;}

    public void setLevel(double aC) {iLevel = aC;}
    public void setWidth(double aW) {iWidth = Math.max(aW, WINDOW_WIDTH_MINIMUM);}   

    public double getTop()    {return iLevel + iWidth / 2.;}
    public double getBottom() {return iLevel - iWidth / 2.;}

    public void setTop(double aT) { 
        final double oldTop = getTop();
        final double oldBottom = getBottom();
        
        iWidth = aT - oldBottom;
        iLevel = oldBottom + iWidth / 2.0;        
    }
    
    public void setBottom(double aB) { 
        final double oldTop = getTop();
        final double oldBottom = getBottom();
        
        iWidth = oldTop - aB;
        iLevel = oldTop - iWidth / 2.0;       
    }
    
    public void setWindow(double aL, double aW) {
        setLevel(aL);
        setWidth(aW);
    }

    public boolean inside(double aV) {return aV > getBottom() && aV < getTop();}

    public boolean compare(Window aW) {
        return  aW.iLevel == this.iLevel && aW.iWidth == this.iWidth;
    }
    
    public boolean contains(Window aW) {
        return  aW.getTop() <= this.getTop() && aW.getBottom() >= this.getBottom();
    }

    @Override
    public String toString(){return super.toString() + String.format("%.1f, %.1f",getBottom(), getTop());} //NOI18N
}