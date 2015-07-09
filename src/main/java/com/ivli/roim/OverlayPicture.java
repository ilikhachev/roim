/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ivli.roim;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.AffineTransformOp;

/**
 *
 * @author likhachev
 */
public class OverlayPicture extends Overlay {
    
    BufferedImage iPicture;
    //Point iOrigin=new Point(0,0);
    
    OverlayPicture(BufferedImage aP, String aN) { 
        super(aP.getRaster().getBounds(), null != aN? aN:java.util.ResourceBundle.getBundle("com/ivli/roim/Bundle").getString("PICTURE"));
        iPicture = aP;
    }
    
    int getCaps(){return CANFLIP|CANROTATE|HASCUSTOMMNU|PERMANENT;}
    
    void draw(Graphics2D aGC, AffineTransform aTrans) {
        
        RenderingHints hts  = new RenderingHints(RenderingHints.KEY_INTERPOLATION, Settings.INTERPOLATION_METHOD);
        AffineTransformOp z = new AffineTransformOp(aTrans, hts);
                    
        //BufferedImage src = iWM.transform(true != SUMMED_FRAME_PANE ? iImg.getBufferedImage():iImg.makeCompositeFrame(0, -1), null);
        
        BufferedImage iBuf = z.filter(iPicture, null);   
        Rectangle r = getShape().getBounds();
        //aTrans.
        AffineTransform trans = AffineTransform.getTranslateInstance(aTrans.getTranslateX(), aTrans.getTranslateY());
        
        //aGC.drawImage(iBuf, 0, 0, r.width, r.height, null);
        aGC.drawImage(iPicture, trans, null);        
    } 
     
    void update(){
    }
     
    void move(double adX, double adY) {
         
        AffineTransform trans = AffineTransform.getTranslateInstance(adX, adY);    
        iShape = trans.createTransformedShape(iShape);
        
       // AffineTransform trans = AffineTransform.getTranslateInstance(adX, adY);    
       // iShape = trans.createTransformedShape(iShape);
        //iOrigin.x += adX;
        //iOrigin.y += adY;
    }
}