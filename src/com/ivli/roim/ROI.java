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

class ROI extends Overlay implements Serializable {
    private Shape  iShape;
    private Color  iColor;
    private String iAnnotation;
        
    ROI(Shape aS, Color aC) {
        super(true, true); 
        iShape = aS; 
        if (null == aC)
            iColor = Colorer.getNextColor(this);
        else
            iColor = aC;
    }
          
    ROI(ROI aR) {super(true, true); iShape = aR.iShape; iColor = aR.iColor; iAnnotation = aR.iAnnotation;}
    
    String getAnnotation() {return iAnnotation;}
    
    void setAnnotation(RoiStats aS) {
        iAnnotation = String.format("pix=%d, pixels=%.1f, area=%.1f, min=%.1f, max=%.1f, iden=%.1f", aS.iPixels, aS.iBounds, aS.iArea, aS.iMin, aS.iMax, aS.iIden);
    }
    
    Color getColor() {return iColor;}
    Shape getShape() {return iShape;}        
    
    void move(double adX, double adY) {
        AffineTransform trans = AffineTransform.getTranslateInstance(adX, adY);    
        iShape = trans.createTransformedShape(iShape);
    }  
        
    static ROI createTransformedRoi(ROI aSrc, AffineTransform aT) {
        ROI self = new ROI(aT.createTransformedShape(aSrc.iShape), aSrc.iColor);
        self.iAnnotation = aSrc.iAnnotation;
        return self;    
    }
}
