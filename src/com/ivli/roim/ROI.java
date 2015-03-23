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
    
    public Color getColor() {return iColor;}
    public Shape getShape() {return iShape;}        
    
    void Move(int adX, int adY) {
        AffineTransform trans = AffineTransform.getTranslateInstance(adX, adY);    
        iShape = trans.createTransformedShape(iShape);
    }  
    
    static ROI createTransformedRoi(ROI aSrc, AffineTransform aT) {
        ROI self = new ROI(aT.createTransformedShape(aSrc.iShape), aSrc.iColor);
        self.iAnnotation = aSrc.iAnnotation;
        return self;    
    }
}
