/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ivli.roim;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.util.NoSuchElementException;

import java.awt.image.Raster;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.BufferedImage;
import java.awt.image.ByteLookupTable;
import java.awt.image.LookupOp;
import java.awt.image.AffineTransformOp;

import javax.imageio.spi.IIORegistry;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.util.NoSuchElementException;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.image.PaletteColorModel;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReaderSpi;

import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.io.BulkDataDescriptor;

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
            registry.registerServiceProvider(new org.dcm4che3.imageio.plugins.dcm.DicomImageReaderSpi());  
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
    private String iFile;
    private BufferedImage iImg;
    
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
    
    BufferedImage getBufferedImage() {return iImg;}     
    
    int getNoOfFrames() {return iFrames;}
    
    MedImage2D first() {loadBufferedImage(iIndex = 0); return this; }
    MedImage2D next() {loadBufferedImage(iIndex = Math.min(iFrames-1, iIndex+1)); return this;}
    boolean hasNext() {return iIndex < iFrames - 1;} 
    
    private void calculate() {
        Raster r = iImg.getRaster();
        double min  = 65535; 
        double max  = 0; 
        double temp [] = new double [r.getNumBands()];

        for (int i=0; i<r.getWidth(); ++i)
            for (int j=0; j<r.getHeight(); ++j) {
                temp = r.getPixel(i, j, temp);
                if (temp[0] > max) max = temp[0];
                else if (temp[0] < min) min = temp[0];
            }

        iMin = min; iMax = max;  
    }
    
    private void loadBufferedImage(int aNdx) {
        try {          
             iImg = iReader.read(aNdx, readParam());
             calculate();
        } catch (Exception e) {   
            logger.error(e);                       
        }
    }
    
   private ImageReadParam readParam() {
        DicomImageReadParam param =
                (DicomImageReadParam) iReader.getDefaultReadParam();
        //param.setWindowCenter(windowCenter);
        //param.setWindowWidth(windowWidth);
        //param.setAutoWindowing(autoWindowing);
        //param.setWindowIndex(windowIndex);
        //param.setVOILUTIndex(voiLUTIndex);
        //param.setPreferWindow(preferWindow);
        //param.setPresentationState(prState);
        //param.setOverlayActivationMask(overlayActivationMask);
        //param.setOverlayGrayscaleValue(overlayGrayscaleValue);
        return param;
    }

  
 
}
