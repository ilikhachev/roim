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
        
        
	private static final Logger logger = LogManager.getLogger(JMedImagePane.class);
                
        class  WindowMgmt {
            private LookupOp            iLok = null;
            private ComponentColorModel iCMdl = null;
            private PValueTransform     iPVt = new PValueTransform();
            private Window              iWin = null;//new Window();
            private boolean             iInverted = false;            
            private boolean             iLog = false;
            
            private WindowMgmt() {}
            public WindowMgmt(Window aW) {
                iWin = aW;
                iCMdl = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY),
                                                null, //new int[] {8},
                                                false,		// has alpha
                                                false,		// alpha premultipled
                                                Transparency.OPAQUE,
                                                DataBuffer.TYPE_BYTE);
            }
            
            public void setTransform(PValueTransform aPVt) {iPVt = aPVt;}     
            
            public Window getWindow() {return iWin;}
        
            public boolean isInverted() {return iInverted;}
            
            public void setInverted(boolean aI) {
                if (aI != isInverted()) {
                    iInverted = aI;    
                    makeLUT();
                    updateBufferedImage();
                    notifyWindowChanged(false);
                }
            }
            
            public boolean isLinear() {return true!=iLog;}
            public void setLinear(boolean aL) {
                if (iLog != aL) {
                    iLog = aL; 
                    makeLUT();
                    updateBufferedImage();
                    notifyWindowChanged(false);
                }
            }
            
            private class LutBuffer {
                byte [] bytes = null;
                int  length = 0;
                int  min = 0;
                int  max = 0;
                
                public LutBuffer(int Type, boolean Signed) {
                    switch (Type) {
                        case DataBuffer.TYPE_SHORT:
                        case DataBuffer.TYPE_USHORT:
                            min = Signed ? -32768 : 0;
                            max = Signed ?  32768 : 65536;
                            bytes=new byte[length = 65536];
                            break;
                        case DataBuffer.TYPE_BYTE:
                            min = Signed ? -128 : 0;
                            max = Signed ?  128 : 256;
                            bytes=new byte[length = 256]; 
                            break;
                        default:
                            throw new IllegalArgumentException();                  
                    }                
                }
            }
                
            private static final double LUT_MIN   = .0;
	    private static final double LUT_MAX   = 255.;
            private static final double LUT_RANGE = LUT_MAX - LUT_MIN;
            
            private final void makeLogLUT() {                
                LutBuffer lut = new LutBuffer(iImg.getBufferedImage(0).getSampleModel().getDataType(), iImg.isSigned());  
                
		for (int i = 0; i < lut.length; ++i) {
                    double y = Ranger.range(LUT_RANGE / (1 + Math.exp(-4*(iPVt.transform(lut.min + i) - iWin.getLevel())/iWin.getWidth())) + LUT_MIN + 0.5, LUT_MIN, LUT_MAX);
                    lut.bytes[i]=(byte)(isInverted() ? (LUT_MAX - y) : y);
		}
            
		iLok  = new LookupOp(new ByteLookupTable(0, lut.bytes), null);	
            }
           
            private final void makeLinearLUT() {  		
                LutBuffer lut = new LutBuffer(iImg.getBufferedImage().getSampleModel().getDataType(), iImg.isSigned()); 
                
                for (int i = 0; i < lut.length; ++i) {
                    double y = iPVt.transform(i-lut.min);
                    
                    if (y <= iWin.getBottom()) y = LUT_MIN;
                    else if (y > iWin.getTop()) y = LUT_MAX;
                    else {
                            y = (((y - iWin.getLevel())/iWin.getWidth() + .5) * LUT_RANGE + LUT_MIN);
                    }

                    lut.bytes[i] = (byte)(isInverted() ? LUT_MAX - y : y);
                }

                iLok = new LookupOp(new ByteLookupTable(0, lut.bytes), null);	
            }
                        
        public final void makeLUT() {    
                logger.info("make LUT " + (iLog ? "logarithmic":"linear") + ", level=" + iWin.getLevel() + ", width=" + iWin.getWidth());
                if (iLog)
                    makeLogLUT(); 
                else 
                    makeLinearLUT();    
            }
            
        public BufferedImage transform(BufferedImage aI) {
                if (null == iLok)
                    makeLUT();
                return iLok.filter(aI, iLok.createCompatibleDestImage(aI, iCMdl));	
            }
        } 
        
        //TODO: it's only temporary hook
        public BufferedImage transform(BufferedImage aI) {
            return iWM.transform(aI);
        }
                
        private final HashSet<WindowChangeListener> iWinListeners = new HashSet();
        
        public void addWindowChangeListener(WindowChangeListener aL) {
            iWinListeners.add(aL);
            aL.windowChanged(new WindowChangeEvent(this, iWM.getWindow(), getMinimum(), getMaximum(), true));
            }
        
        private void notifyWindowChanged(boolean aRC) {
            final WindowChangeEvent wce = new WindowChangeEvent(this, iWM.getWindow(), getMinimum(), getMaximum(), aRC);
            for (WindowChangeListener l:iWinListeners)
                l.windowChanged(wce);
        }
        
        private final MedImage2D iImg;
        private final Controller iController;
        private final WindowMgmt iWM; //new WindowMgmt();
        private final List<ROI>  iRoi = new LinkedList();
        
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
                iWM.getWindow().setWindow(aW.getLevel(), aW.getWidth()); 
                iWM.makeLUT();
                updateBufferedImage();
                notifyWindowChanged(false);
            }
        }
                /**Git tets **/
        boolean isInverted() {return iWM.isInverted();}
        void    setInverted(boolean aI) {iWM.setInverted(aI);} 
        boolean isLinear() {return iWM.isLinear();}
        void    setLinear(boolean aI) {iWM.setLinear(aI);}
        double  getMinimum() {return iImg.getMinimum();}
        double  getMaximum() {return iImg.getMaximum();}    
       
        public JMedImagePane(String aFileName) throws IOException {
            iImg = new MedImage2D();      
            iImg.open(aFileName);
            iImg.getBufferedImage(0);  
            iWM = new WindowMgmt(new Window(getMinimum(), getMaximum()));
            iController = new Controller(this);
            }
        
	public JMedImagePane(MedImage2D anImg) {
            iImg = anImg;
            iImg.getBufferedImage(0);   
            iWM = new WindowMgmt(new Window(getMinimum(), getMaximum()));
            iController = new Controller(this);            
            }
   
    AffineTransform iZoom   = AffineTransform.getScaleInstance(DEFAULT_SCALE_X, DEFAULT_SCALE_Y);
    Point           iOrigin = new Point(0,0);    
    BufferedImage   iBuf;
        
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
        RenderingHints hts = new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        AffineTransformOp z = new AffineTransformOp(iZoom, hts);
        BufferedImage src = iWM.transform(iImg.getBufferedImage(0));
        BufferedImage dst = z.createCompatibleDestImage(src, iWM.iCMdl);
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
}




