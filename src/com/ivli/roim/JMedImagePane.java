package com.ivli.roim;

import java.awt.Graphics;
import java.awt.Graphics2D;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.Shape;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.BufferedImage;
import java.awt.image.ByteLookupTable;
import java.awt.image.LookupOp;
import java.awt.image.AffineTransformOp;
import java.awt.RenderingHints;

import java.awt.Point;
import java.awt.event.*;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenu;        
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;
import java.util.HashSet;

import java.awt.geom.Ellipse2D;
import java.awt.Rectangle;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;


public class JMedImagePane extends JComponent {

    final static private boolean DRAW_OVERLAYS_ON_BUFFERED_IMAGE = false; //i cry ther's no #ifdef 
    final static private double DEFAULT_ELLIPSE_WIDTH  = 3.;
    final static private double DEFAULT_ELLIPSE_HEIGHT = 3.;
    final static private double DEFAULT_SCALE_X = 1.;
    final static private double DEFAULT_SCALE_Y = 1.;

    private final MedImage2D iImg;
    private final Controller iController;
    private final VOILut     iWM; 
    private final List<ROI>  iRoi = new LinkedList();
    private final AffineTransform  iZoom = AffineTransform.getScaleInstance(DEFAULT_SCALE_X, DEFAULT_SCALE_Y);
    private final Point      iOrigin = new Point(0,0);    
    private BufferedImage    iBuf;
    private final HashSet<WindowChangeListener> iWinListeners = new HashSet();    
        
    public JMedImagePane(String aFileName) throws IOException {
        iImg = new MedImage2D();      
        iImg.open(aFileName);
        iImg.first();  
        iWM = new VOILut(iImg);
        iController = new Controller(this);
    }

    public JMedImagePane(MedImage2D anImg) {
        iImg = anImg;
        iImg.first();   
        iWM = new VOILut(iImg);
        iController = new Controller(this);            
    }
//TODO: it's only temporary hook
    public BufferedImage transform(BufferedImage aI) {
        return iWM.transform(aI, null);
    }

    public void addWindowChangeListener(WindowChangeListener aL) {
        iWinListeners.add(aL);
        aL.windowChanged(new WindowChangeEvent(this, iWM.getWindow(), getMinimum(), getMaximum(), true));
    }

    private void notifyWindowChanged(boolean aRC) {
        final WindowChangeEvent wce = new WindowChangeEvent(this, iWM.getWindow(), getMinimum(), getMaximum(), aRC);
        for (WindowChangeListener l:iWinListeners)
            l.windowChanged(wce);
    }

    public void addRoi(ROI aR) {
        AffineTransform trans = AffineTransform.getTranslateInstance(iOrigin.x, iOrigin.y); 
        trans.concatenate(iZoom);
        try { 
            trans.invert(); 
        } catch (Exception e){ 
        //try to do anything useful if u can
        }

        iRoi.add(aR.createTransformedROI(trans));
    }

    public void cloneRoi(ROI aR) {
        iRoi.add(new ROI(aR));        
    }

    private Rectangle point2shape(Point aP) {
        Rectangle r = new Rectangle(aP.x, aP.y, 3, 3);
        AffineTransform trans = AffineTransform.getTranslateInstance(iOrigin.x, iOrigin.y); 
        trans.concatenate(iZoom); 
        try { 
            trans.invert(); 
        } catch (Exception e){ }
        Shape ret = trans.createTransformedShape(r);
        return ret.getBounds();
    }

    ROI findRoi(Point aP) {           
        for (ROI r:iRoi) {
           if (r.iShape.intersects(point2shape(new Point(aP.x, aP.y)))) 
                return r;                        
        }
        return null;
    }
        
    ROI deleteRoi(ROI aR) {           
        if (iRoi.remove(aR)) {
            AffineTransform trans = AffineTransform.getTranslateInstance(iOrigin.x, iOrigin.y); 
            trans.concatenate(iZoom);  
            return aR.createTransformedROI(trans); 
        }
        return null;
    }

    public Window getWindow() {return iWM.getWindow();}
              
    public void setWindow (Window aW) {
        if (!iWM.getWindow().equals(aW)) {
            if (aW.getLevel()  > getMinimum()  && aW.getLevel() < getMaximum()) {

                iWM.getWindow().setWindow(aW.getLevel(), aW.getWidth()); 

                iWM.makeLUT();
                updateBufferedImage();
                notifyWindowChanged(false);
            }
        }
    }
                /**Git tets **/
    boolean isInverted() {
        return iWM.isInverted();
    }

    void  setInverted(boolean aI) {
        if (iWM.setInverted(aI)) {              
            updateBufferedImage();
            notifyWindowChanged(false);
        }
    } 
    
    boolean isLinear() {return iWM.isLinear();}
    void    setLinear(boolean aI) {
        if(iWM.setLinear(aI)) {
            updateBufferedImage();
            notifyWindowChanged(false);
        }    
    }

    double  getMinimum() {return iImg.getMinimum();}
    double  getMaximum() {return iImg.getMaximum();}    

    
        
    public void zoom(double aFactor, int aX, int aY) {
        iZoom.setToScale(iZoom.getScaleX() + aFactor, iZoom.getScaleY() + aFactor);
        updateBufferedImage();
        repaint();
    }
     
    public void pan(int adX, int adY) {
        iOrigin.x += adX;
        iOrigin.y += adY;
        repaint();
    }
             
    void resetView() {
        iOrigin.x = iOrigin.y = 0;
        iZoom.setToScale(DEFAULT_SCALE_X, DEFAULT_SCALE_Y);  
        updateBufferedImage();
    }
     
    public void updateBufferedImage() {
        iBuf = null; ///TODO: get optimized - separate zoom and windowing operations 
        RenderingHints hts  = new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        AffineTransformOp z = new AffineTransformOp(iZoom, hts);
        BufferedImage src = iWM.transform(iImg.getBufferedImage(), null);
        BufferedImage dst = z.createCompatibleDestImage(src, iWM.getComponentColorModel());
        iBuf = z.filter(src, dst);     
    }
    
    public void paintComponent(Graphics g) {           
        if (null == iBuf) /// do we actually need this harness - optimize here
            updateBufferedImage();
        
        if (DRAW_OVERLAYS_ON_BUFFERED_IMAGE) {
            Graphics gc = iBuf.createGraphics();
           // for (ROI r : iRoi) 
            //    r.drawZoomed((Graphics2D)gc, iOrigin, iZoom.getScaleX(), iZoom.getScaleY());
         
            gc.dispose();// do i have to
        }       
        
        g.drawImage(iBuf, iOrigin.x, iOrigin.y, iBuf.getWidth(), iBuf.getHeight(), null);
        
        if (!DRAW_OVERLAYS_ON_BUFFERED_IMAGE) {
            AffineTransform trans = AffineTransform.getTranslateInstance(iOrigin.x, iOrigin.y); 
            trans.concatenate(iZoom);
            Color clr = g.getColor();
            for (ROI r : iRoi) {
                g.setColor(r.getColor());
                ((Graphics2D)g).draw(r.createTransformedROI(trans).iShape);
            }
            g.setColor(clr);
        }
        
        iController.paint(g); //must paint the last   
    }
     
    private static final Logger logger = LogManager.getLogger(JMedImagePane.class);
}




