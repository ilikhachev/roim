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
    protected static int SCNdx = 0;
    
    static final Color [] iCols = new Color[]{Color.BLACK, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.ORANGE, Color.RED, Color.PINK, Color.YELLOW};
    
    private ROI (Shape aS, Color aC) {iShape = aS; iColor = aC;}
    ROI (Shape aS) {iShape = aS; iColor = SCNdx < iCols.length -1 ? iCols[++SCNdx] : Color.BLACK;}
    
    final ROI createTransformedROI(AffineTransform aT) {
        ROI self = new ROI(aT.createTransformedShape(iShape), iColor);
        self.iAnnotation = this.iAnnotation;
        return self;    
        }
    
     public Color  getColor() {return iColor;}
}
