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


public class SourceImage {
    private static final Logger logger = LogManager.getLogger(SourceImage.class);
    
    private BufferedImage iImg;
    private final ImageReader iReader = ImageIO.getImageReadersByFormatName("DICOM").next();
    private int    iIndex = 0;
    private double iMin;
    private double iMax;
    private String iFile;
    
    //SourceImage (){}
    
    void open(String aFile) throws IOException {      
        //DicomInputStream is = new DicomInputStream(new FileInputStream(new File(aFile)));
        ImageInputStream iis = ImageIO.createImageInputStream(new File(aFile));
        iReader.setInput(iis);       
        logger.info("-->Number of images = " + iReader.getNumImages(false));
    }
    
    boolean isSigned() {return false;} ///TODO
    double getMinimum() {return iMin;}
    double getMaximum() {return iMax;}
    
    BufferedImage getBufferedImage() {return iImg = loadBufferedImage(iIndex = 0);}
    BufferedImage getBufferedImage(int aNdx) {return first().iImg;}        
    
    int frames() {return 1;}
    SourceImage first() { iImg = loadBufferedImage(iIndex = 0); return this; }
    SourceImage next() { iImg = loadBufferedImage(iIndex = Math.min(iIndex-1, 0)); return this;}
    
    private BufferedImage loadBufferedImage(int aNdx) {
        try {          
                BufferedImage img = iReader.read(aNdx, readParam());
                
                Raster r = img.getRaster();
                double min  = 65535; 
                double max  = 0; 
                double temp [] = new double [r.getNumBands()];
                
                for (int i=0; i < r.getNumBands(); ++i) {
                    temp[i] = 0;
                }
                    
                for (int i=0; i<r.getWidth(); ++i)
                    for (int j=0; j<r.getHeight(); ++j) {
                        temp = r.getPixel(i, j, temp);
                        if (temp[0] > max) max = temp[0];
                        else if (temp[0] < min) min = temp[0];
                    }
                         
                iMin = min; iMax = max;
                return img;
            } catch (Exception e) {   
                System.out.println(e.getLocalizedMessage());                       
            }
        return null;
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
