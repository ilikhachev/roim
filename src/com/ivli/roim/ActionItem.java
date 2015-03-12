/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ivli.roim;


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 *
 * @author likhachev
 */
abstract class ActionItem {
    protected int iX;
    protected int iY;

    public ActionItem(int aX, int aY){iX = aX; iY = aY;}

    public final boolean release(int aX, int aY) {return DoRelease(aX, aY);}

    public final void action(int aX, int aY) {
        DoAction(aX, aY); iX = aX; iY = aY;
    }

    public final void wheel(int aX) {
        DoWheel(aX);
    }     

    public final void paint(Graphics gc) {
        Color oc = gc.getColor();
        gc.setColor(Settings.ACTIVE_ROI_COLOR);
        DoPaint((Graphics2D)gc);
        gc.setColor(oc);
    }

    protected abstract void DoAction(int aX, int aY); 
    protected abstract boolean DoWheel(int aX);// {iControlled.zoom(-aX/10.0, 0, 0); return true;}
    // return true if action shall be continued
    protected abstract boolean DoRelease(int aX, int aY);
    protected abstract void DoPaint(Graphics2D aGC);
}

