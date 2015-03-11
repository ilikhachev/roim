package com.ivli.roim;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Ellipse2D;
import java.awt.Point;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.io.Serializable;

class ROI implements Serializable {
    protected Shape  iShape;
    protected Color  iColor;
    protected String iAnnotation;
         
    ROI (Shape aS, Color aC) {iShape = aS; iColor = aC;}
    ROI (Shape aS) {iShape = aS; iColor = Colorer.getNextColor(this);}
    ROI (ROI aR) {iShape = aR.iShape; iColor = aR.iColor; iAnnotation = aR.iAnnotation;}
       
    final ROI createTransformedROI(AffineTransform aT) {
        ROI self = new ROI(aT.createTransformedShape(iShape), iColor);
        self.iAnnotation = this.iAnnotation;
        return self;    
        }
    
     public Color  getColor() {return iColor;}
}
