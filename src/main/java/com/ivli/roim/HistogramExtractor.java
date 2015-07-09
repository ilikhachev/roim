/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ivli.roim;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.Raster;
import org.jfree.data.xy.XYSeries;

/**
 *
 * @author likhachev
 */
public class HistogramExtractor implements Extractor {
    final ROI iRoi;
    public final XYSeries iHist = new XYSeries(java.util.ResourceBundle.getBundle("com/ivli/roim/Bundle").getString("HISTOGRAMEXTRACTOR.IHISTOGRAM"));
    
    public HistogramExtractor(ROI aRoi){iRoi = aRoi;}
    
    @Override
    public void extract(Raster aRaster) throws ArrayIndexOutOfBoundsException {
        extractOne(aRaster);
    }
    
    void extractOne(Raster aRaster) throws ArrayIndexOutOfBoundsException {
        final Shape shape = (null != iRoi) ? iRoi.getShape() : aRaster.getBounds();
        final Rectangle bnds = shape.getBounds();
        double temp[] = new double [aRaster.getNumBands()];
        
        for (int i=bnds.x; i < (bnds.x + bnds.width); ++i)
            for (int j=bnds.y; j < (bnds.y + bnds.height); ++j) 
                if (shape.contains(i, j)) {                   
                    temp = aRaster.getPixel(i, j, temp);
                    /**/
                    final int ndx = iHist.indexOf(temp[0]);
                    if (ndx < 0) 
                        iHist.add(temp[0], 1);
                    else {                   
                        final Number val = iHist.getY(ndx);
                        iHist.update((Number)temp[0], ((Double)val)+1); 
                    } 
                }
    }

    
    void extractBinned256(Raster aRaster) throws ArrayIndexOutOfBoundsException {
    
        final Shape shape = (null != iRoi) ? iRoi.getShape() : aRaster.getBounds();
        final Rectangle bnds = shape.getBounds();
        double temp [] = new double [aRaster.getNumBands()];
        
        final ROIExtractor rex = new ROIExtractor(iRoi);
        rex.extract(aRaster);
        final double step = (rex.iStats.iMax - rex.iStats.iMin ) / 256.0;
                                
        for (int i=bnds.x; i < (bnds.x + bnds.width); ++i)
            for (int j=bnds.y; j < (bnds.y + bnds.height); ++j) 
                if (shape.contains(i, j)) {                   
                    temp = (aRaster.getPixel(i, j, temp));
                    /**/
                    final Double binNo = Math.floor(temp[0]/step);
                    final int ndx = iHist.indexOf(binNo);
                    if (ndx < 0) 
                        iHist.add(binNo, (Double)1.0);
                    else {                   
                        final Number val = iHist.getY(ndx);
                        iHist.update(binNo, ((Double)val)+1); 
                    } 
                }
        
    }
}