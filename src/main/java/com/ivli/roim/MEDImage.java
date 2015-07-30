/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ivli.roim;


import java.io.IOException;
import java.util.ArrayList;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class MEDImage extends MEDImageBase {
              
    private String iFile; 
   
    private class FrameWithStats {
        RoiStats iStats;
        Raster   iRaster;      
    }    
    
    private int iIndex;    
    private final ArrayList<FrameWithStats> iFrames = new ArrayList();    
    
    private MEDImage() {}
    
    static MEDImage New(final String aFile) throws IOException {
        MEDImage self = new MEDImage();
        self.open(aFile);
        return self;
    }
                  
    private Raster getFrame() { 
        return iFrames.get(iIndex).iRaster;
    }
    
    public int getWidth() {return getFrame().getWidth();}
    
    public int getHeight() {return getFrame().getHeight();}            
            
    public void open(String aFile) throws IOException {           
        iLoader.open(aFile);
         
        iFrames.clear();
        iFrames.ensureCapacity(getNoOfFrames());
       
        loadFrame(0);
    }
           
    public RoiStats getImageStats() {return iFrames.get(iIndex).iStats;}
       
    public BufferedImage getBufferedImage() {
        return convert((WritableRaster)getFrame());
    }     
    
    public void loadFrame(int anIndex) throws IndexOutOfBoundsException{
        try{
            doLoadFrame(anIndex);
           
            logger.info("Frame -"+iIndex+", MIN" + iFrames.get(iIndex).iStats.iMin + // NOI18N
                        ", MAX" + iFrames.get(iIndex).iStats.iMax +   // NOI18N
                        ", DEN" + iFrames.get(iIndex).iStats.iIden);  // NOI18N
        } catch (IOException ex) {
            logger.error(ex); 
        } 
    }
                   
    private void doLoadFrame(int anIndex) throws IOException, IndexOutOfBoundsException {
    
        if (anIndex > getNoOfFrames()-1 || anIndex < 0)
            throw new IndexOutOfBoundsException();
        
         // load and cache image if it is not yet in cache
        if ((iIndex = anIndex) >= iFrames.size() || null == iFrames.get(iIndex)) { 
            FrameWithStats r = new FrameWithStats();
            r.iRaster = iLoader.readRaster(iIndex);
            ROIExtractor ex=new ROIExtractor(new ROI(r.iRaster.getBounds(), null, null));
            ex.apply(r.iRaster);
            r.iStats = ex.iRoi.getStats();                            

            iFrames.add(iIndex, r);
        }    
    }
    
    private BufferedImage convert(WritableRaster raster) {
        ColorModel cm ;
       // if (pmi.isMonochrome()) {
           
            cm = createColorModel(8, DataBuffer.TYPE_USHORT);//TYPE_BYTE);
          //  SampleModel sm = createSampleModel(DataBuffer.TYPE_BYTE, false);
          //  raster = applyLUTs(raster, frameIndex, param, sm, 8);
          //  for (int i = 0; i < overlayGroupOffsets.length; i++) {
          //      applyOverlay(overlayGroupOffsets[i], 
          //              raster, frameIndex, param, 8, overlayData[i]);
       //     }
      //  } else {
      //      cm = createColorModel(bitsStored, dataType);
      //  }
        //WritableRaster r = raster.createCompatibleWritableRaster();
        return new BufferedImage(cm, raster , false, null);
    }
    
    static ColorModel createColorModel(int bits, int dataType) {
        return new ComponentColorModel(
                ColorSpace.getInstance(ColorSpace.CS_GRAY),
                new int[] { bits },
                false, // hasAlpha
                false, // isAlphaPremultiplied
                Transparency.OPAQUE,
                dataType);
    }
    
    public BufferedImage makeCompositeFrame(int aFrom, int aTo) throws IOException {
        if (-1 == aTo)
            aTo = getNoOfFrames();

        assert (aFrom >= 0 && aFrom < getNoOfFrames() || aTo > aFrom || aFrom < getNoOfFrames());  
        
        WritableRaster composite = iFrames.get(0).iRaster.createCompatibleWritableRaster();
        
        final int cols = iFrames.get(0).iRaster.getWidth();
        final int rows = iFrames.get(0).iRaster.getHeight();
        
        for (int n = aFrom; n < aTo; ++n) {
            final Raster r = iFrames.get(n).iRaster;
            for (int i = 0; i < cols; ++i)
               for (int j = 0; j < rows; ++j) 
                   composite.setSample(i, j, 0, composite.getSample(i, j, 0) + r.getSample(i, j, 0));           
        }
        
        return convert(composite);
    }    
    
    public void extract(Extractor aEx) {
        aEx.apply(getFrame());
    }
      
    
    private static final Logger logger = LogManager.getLogger(MEDImage.class);    
}

