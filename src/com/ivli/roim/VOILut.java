/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ivli.roim;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ByteLookupTable;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.LookupOp;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;


class  VOILut implements LUTTransform {
    private LookupOp iLok;
    private ComponentColorModel iCMdl;
    private final PValueTransform iPVt = new PValueTransform();
    private final Window iWin;
    private boolean iInverted;            
    private boolean iLog;
    private final LutBuffer iBuffer;
  
    public VOILut(MedImage2D aI) {
        iWin  = new Window(aI.getMinimum(), aI.getMaximum());
        iCMdl = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY),
                                        null, //new int[] {8},
                                        false,		// has alpha
                                        false,		// alpha premultipled
                                        Transparency.OPAQUE,
                                        DataBuffer.TYPE_BYTE);
        
        iBuffer = new LutBuffer(aI.getBufferedImage().getSampleModel().getDataType(), aI.isSigned()); 
    }

    public ComponentColorModel getComponentColorModel(){return iCMdl;}

    //public void setTransform(PValueTransform aPVt) {iPVt = aPVt;}     

    public Window getWindow() {return iWin;}

    public boolean isInverted() {return iInverted;}

    public boolean setInverted(boolean aI) {
        if (aI != isInverted()) {
            iInverted = aI;    
            makeLUT();
            return true;
        }
        return false;
    }

    public boolean isLinear() {return true!=iLog;}
    
    public boolean setLinear(boolean aL) {
        if (iLog != aL) {
            iLog = aL; 
            makeLUT();
            return true;
        }
        return false;
    }
    
    private static final double LUT_MIN   = .0;
    private static final double LUT_MAX   = 255.;
    private static final double LUT_RANGE = LUT_MAX - LUT_MIN;

    private final void makeLogarithmic() {   
        
       // LutBuffer lut = new LutBuffer(iImg.getBufferedImage().getSampleModel().getDataType(), iImg.isSigned());  

        for (int i = 0; i < iBuffer.length; ++i) {
            double y = Ranger.range(LUT_RANGE / (1 + Math.exp(-4*(iPVt.transform(iBuffer.min + i) - iWin.getLevel())/iWin.getWidth())) + LUT_MIN + 0.5, LUT_MIN, LUT_MAX);
            iBuffer.bytes[i]=(byte)(isInverted() ? (LUT_MAX - y) : y);
        }

        iLok  = new LookupOp(new ByteLookupTable(0, iBuffer.bytes), null);	
    }

    private final void makeLinear() {  
        final double m = 255 / iWin.getWidth();
        final double b = -m*iWin.getBottom();
        final byte max = (byte)(isInverted() ? 0x00:0xff);
        final byte min = (byte)(isInverted() ? 0xff:0x00);
        
        for (int x=0; x < iBuffer.length; ++x) {
            
            //final double y = iPVt.transform(x)*m+b;
            
            if (x <= iWin.getBottom()) 
                iBuffer.bytes[x] = 0;
            else if (x > iWin.getTop()) 
                iBuffer.bytes[x] = (byte)255;
            else
                iBuffer.bytes[x] = (byte)(x*m+b);
        }

        iLok = new LookupOp(new ByteLookupTable(0, iBuffer.bytes), null);
    }
    
    private final void makeLinear2() {  	
        for (int i = 0; i < iBuffer.length; ++i) {
            double y = iPVt.transform(i-iBuffer.min);

            if (y <= iWin.getBottom()) y = LUT_MIN;
            else if (y > iWin.getTop()) y = LUT_MAX;
            else {
                y = (((y - iWin.getLevel())/iWin.getWidth() + .5) * LUT_RANGE + LUT_MIN);
            }

            iBuffer.bytes[i] = (byte)(isInverted() ? LUT_MAX - y : y);
        }

        iLok = new LookupOp(new ByteLookupTable(0, iBuffer.bytes), null);	
    }

    public final void makeLUT() {   
        
        logger.info("make LUT " + (iLog ? "logarithmic":"linear") + ", level=" + iWin.getLevel() + ", width=" + iWin.getWidth());
        
        if (isLinear())
            makeLinear();   
        else 
            makeLogarithmic();     
    }
  
    public BufferedImage transform(BufferedImage aSrc, BufferedImage aDst) {
        if (null == iLok)
            makeLUT();
        return iLok.filter(aSrc, null == aDst ? iLok.createCompatibleDestImage(aSrc, iCMdl):aDst);	
    }
    
    private static final Logger logger = LogManager.getLogger(VOILut.class);
} 

final class LutBuffer {
    final byte [] bytes;
    final int  length;
    final int  min;
    final int  max;

    public LutBuffer(int Type, boolean Signed) {
        switch (Type) {
            case DataBuffer.TYPE_SHORT:
            case DataBuffer.TYPE_USHORT:
                min = Signed ? -32768 : 0;
                max = Signed ?  32768 : 65536;
                bytes = new byte[length = 65536];
                break;
            case DataBuffer.TYPE_BYTE:
                min = Signed ? -128 : 0;
                max = Signed ?  128 : 256;
                bytes = new byte[length = 256]; 
                break;
            default:
                throw new IllegalArgumentException();                  
        }                
    }
}