/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ivli.roim;

import java.awt.Shape;

/**
 *
 * @author likhachev
 */
public abstract class Overlay {
    private final boolean iSelectable;
    private final boolean iMovable;
    private boolean iSelected;
    
    Overlay(){iSelectable=false; iMovable=false;}
    Overlay(boolean aS, boolean aM) {iSelectable = aS; iMovable = aM;}
    boolean isSelectable(){return iSelectable;}
    boolean isMovable(){return iMovable;}
    
    void select(boolean aS) {
        assert(iSelected == true);
        iSelected = aS;
    }
    boolean isSelected() {return iSelected;}
    
    abstract void move(double adX, double adY); 
    
    //boolean intersects(Rectangle2D aD)
    abstract Shape getShape();
}
