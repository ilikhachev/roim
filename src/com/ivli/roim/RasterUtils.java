/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ivli.roim;

import java.awt.image.Raster;

/**
 *
 * @author likhachev
 */
public class RasterUtils {
    
    
    static void minimax(Raster aR, Double aMin, Double aMax, Double aSum) {
        ///Raster r = iImg.getRaster();
        double min  = 65535; 
        double max  = 0; 
        double temp [] = new double [aR.getNumBands()];
        double sum = .0;
        for (int i=0; i<aR.getWidth(); ++i)
            for (int j=0; j<aR.getHeight(); ++j) {
                temp = aR.getPixel(i, j, temp);
                if (temp[0] > max) 
                    max = temp[0];
                else if (temp[0] < min) 
                    min = temp[0];
                sum += temp[0];
            }

        aMin = min; aMax = max;  
        if (null != aSum)
            aSum = sum;
    }
}
