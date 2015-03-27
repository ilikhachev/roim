/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ivli.roim;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Transparency;
import java.awt.color.*;
import java.awt.image.*;
import java.awt.Point;
import java.awt.geom.AffineTransform;

import javax.swing.JComponent;

/**
 *
 * @author likhachev
 */
public class LutPanel extends JComponent implements WindowChangeListener {
    private final JMedImagePane iPanel; 
    private BufferedImage       iBuf;

    public LutPanel(JMedImagePane aP) {iPanel = aP;}
    
    final static int NUMBER_OF_SHADES = 255;
    ComponentSampleModel iSm;
    ComponentColorModel iCm;
    
    void init2() {
        final int w = getWidth();
        final int h = getHeight();
        iCm = new ComponentColorModel(
			ColorSpace.getInstance(ColorSpace.CS_GRAY),
			new int[] {8},
			false,		// has alpha
			false,		// alpha premultipled
			Transparency.OPAQUE,
			DataBuffer.TYPE_USHORT);
		
        iSm = new ComponentSampleModel(
                DataBuffer.TYPE_USHORT,
                w,
                NUMBER_OF_SHADES,
                1,
                w,
                new int[] {0});
		         
        //final double delta = (iPanel.getMaximum() - iPanel.getMinimum())/getHeight();
        final int stepy = (int)Math.floor((double)h / (double)NUMBER_OF_SHADES);
             
        short data [] = new short[w*h];

        for (int i = 0; i < NUMBER_OF_SHADES; ++i) {            
            for (int j = 0; j < w; ++j)
                data[i*w+j] =(short)(NUMBER_OF_SHADES - i);
        }

        DataBuffer buf = new DataBufferUShort(data, w, 0);
        WritableRaster wr = Raster.createWritableRaster(iSm, buf, new Point(0,0));
        iBuf = new BufferedImage(iCm, wr, true, null);	// no properties hash table
    }
    
    public Dimension getMinimumSize() {return new Dimension(20, NUMBER_OF_SHADES);}
    public Dimension getPreferredSize() {return new Dimension(20, getParent().getHeight());}
    public Dimension getMaximumSize() {return new Dimension(Short.MAX_VALUE,
                                                            Short.MAX_VALUE);}
        
    public void windowChanged(WindowChangeEvent anEvt) {
        repaint();
    }    

    public void paintComponent(Graphics g) {  
        //if (null == iBuf)
        init2(); //TODO: optimize it
        //AffineTransform trans = AffineTransform.getScaleInstance(1.0, 1.0);//(double)getHeight()/(double)NUMBER_OF_SHADES);
        //AffineTransformOp z = new AffineTransformOp(trans, null);
        BufferedImage temp = iPanel.transform(iBuf);
        //BufferedImage temp2 = z.createCompatibleDestImage(temp, iCm);
        BufferedImage temp2 = iPanel.createIndexedCopy(temp, LutLoader.open("fire"));
        g.drawImage(temp2, 0, 0, getWidth(), getHeight(), null);
    }
}
