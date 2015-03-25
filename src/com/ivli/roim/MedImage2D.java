/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ivli.roim;

import java.io.File;
import java.io.IOException;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.awt.Rectangle;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import javax.imageio.spi.IIORegistry;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.util.NoSuchElementException;

import org.dcm4che2.imageio.plugins.dcm.DicomImageReadParam;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;


public class MedImage2D {

    private static final Logger logger = LogManager.getLogger(MedImage2D.class);
     
    static { //ensure dicom image reader is installed otherwise make try to install 
        ImageReader ir;
        
        try {
            ir = ImageIO.getImageReadersByFormatName("DICOM").next();
        } catch (NoSuchElementException e) {
            logger.error("It seems there's no DICOM reader available, make try to install one" + e);
            IIORegistry registry = IIORegistry.getDefaultInstance();
            //registry.registerServiceProvider(new org.dcm4che3.imageio.plugins.dcm.DicomImageWriterSpi());
            //registry.registerServiceProvider(new org.dcm4che2.imageio.plugins.dcm.DicomImageReaderSpi());  
            ir = ImageIO.getImageReadersByFormatName("DICOM").next();  
        } finally {
           iReader = ImageIO.getImageReadersByFormatName("DICOM").next(); 
        }
    }
    
    private static final ImageReader iReader;// = ImageIO.getImageReadersByFormatName("DICOM").next();
    private int    iIndex;
    private int    iFrames;
    private double iMin;
    private double iMax;
    private double iIden;
    private String iFile;
    //private BufferedImage iImg;
    private Raster iImg;
    //SourceImage (){}
    
    void open(String aFile) throws IOException {      
        //DicomInputStream is = new DicomInputStream(new FileInputStream(new File(aFile)));
        ImageInputStream iis = ImageIO.createImageInputStream(new File(iFile = aFile));
        iReader.setInput(iis);     
        iFrames = iReader.getNumImages(false);
        logger.info("-->Number of frames = " + iFrames);
    }
    
    boolean isSigned() {return false;} ///TODO
    double getMinimum() {return iMin;}
    double getMaximum() {return iMax;}
    
    RoiStats extractRoiStats(ROI aR) {
        RoiStats ret = new RoiStats();
                
        Raster    src  = iImg;//.getData();
        Rectangle bnds = aR.getShape().getBounds();
        
        double min  = 65535; 
        double max  = 0; 
        double temp [] = new double [src.getNumBands()];
        double sum = .0;
        int pix = 0;
                
        for (int i=bnds.x; i < bnds.x + bnds.width; ++i)
            for (int j=bnds.y; j < bnds.y + bnds.height; ++j) //{ 
            if (aR.getShape().contains(i, j)) {
                ++pix;
                temp = src.getPixel(i, j, temp);
                if (temp[0] > max) 
                    max = temp[0];
                else if (temp[0] < min) 
                    min = temp[0];
                sum += temp[0];
            }
        
        ret.iMin = min;
        ret.iMax = max;
        ret.iIden = sum;
        ret.iPixels = pix;
        return ret;
    }
              
    BufferedImage getBufferedImage() {return convert((WritableRaster)iImg);}     
    
    int getNoOfFrames() {return iFrames;}
    
    MedImage2D first() {loadBufferedImage(iIndex = 0); return this; }
    MedImage2D next() {loadBufferedImage(iIndex = Math.min(iFrames-1, iIndex+1)); return this;}
    boolean hasNext() {return iIndex < iFrames - 1;} 
       
    void minimax(Raster r) {
        ///Raster r = iImg.getRaster();
        double min  = 65535; 
        double max  = 0; 
        double temp [] = new double [r.getNumBands()];
        double sum = .0;
        
        for (int i=0; i<r.getWidth(); ++i)
            for (int j=0; j<r.getHeight(); ++j) {
                temp = r.getPixel(i, j, temp);
                if (temp[0] > max) 
                    max = temp[0];
                else if (temp[0] < min) 
                    min = temp[0];
                sum += temp[0];
                
            }
        
        iMin = min; 
        iMax = max;  
        iIden = sum;
    }
    
    private void loadBufferedImage(int aNdx) {
        try {          
            //iImg = iReader.read(aNdx, readParam());
            iImg = iReader.readRaster(aNdx, readParam());
           
            //iImg = convert((WritableRaster)r);//iReader.read(aNdx, readParam());
            minimax(iImg);
            
        } catch (Exception e) {   
            logger.error(e);                       
        }
        logger.info("Min=" + iMin + ", Max=" + iMax + ", Den=" + iIden);
    }
    
    private ImageReadParam readParam() {
        DicomImageReadParam param =
                (DicomImageReadParam) iReader.getDefaultReadParam();
        //param.setWindowCenter(windowCenter);
        //param.setWindowWidth(windowWidth);
        param.setAutoWindowing(false);
        //param.setWindowIndex(windowIndex);
        //param.setVOILUTIndex(voiLUTIndex);
        //param.setPreferWindow(preferWindow);
        //param.setPresentationState(prState);
        //param.setOverlayActivationMask(overlayActivationMask);
        //param.setOverlayGrayscaleValue(overlayGrayscaleValue);
        return param;
    }
    
    
    private BufferedImage convert(WritableRaster raster) {
        ColorModel cm;
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



}
