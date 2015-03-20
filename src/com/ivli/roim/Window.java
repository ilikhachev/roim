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

    //public Window(double aL, double aW) {
    //    iLevel = aL; 
    //    iWidth = Math.max(WINDOW_WIDTH_MINIMUM, aW);
    //}

    public Window(double aL, double aW) {
        iLevel = aL; 
        iWidth = Math.max(WINDOW_WIDTH_MINIMUM, aW);   
    }

    public final double getLevel()  {return iLevel;}
    public final double getWidth()  {return iWidth;}

    public final void setLevel(double aC) {iLevel = aC;}
    public final void setWidth(double aW) {iWidth = Math.max(aW, WINDOW_WIDTH_MINIMUM);}   

    public final double getTop()    {return iLevel + iWidth / 2.;}
    public final double getBottom() {return iLevel - iWidth / 2.;}

    public final void setWindow(double aL, double aW) {
        setLevel(aL);
        setWidth(aW);
    }

    public final void setTop(double aT) {
        final double delta = aT - getTop();
        setWidth(getWidth() + delta);
        setLevel(getLevel() + delta / 2.);
    }

    public final void setBottom(double aB) {
        final double delta = aB - getBottom();
        setWidth(getWidth() + delta);
        setLevel(getLevel() + delta / 2.);
    }

    public final boolean inside(double aV) {return aV > getBottom() && aV < getTop();}

    public final boolean equals(Window aW) {
        return  aW.iLevel == this.iLevel && aW.iWidth == this.iWidth;
    }
}