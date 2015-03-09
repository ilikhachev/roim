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

import javax.swing.JComponent;

/**
 *
 * @author likhachev
 */
public class LutPanel extends JComponent implements WindowChangeListener {
    
   ImagePanel    iPanel = null; 
   BufferedImage iBuf = null;
 
   private LutPanel(){}
   public LutPanel(ImagePanel aP) {iPanel = aP;}
           
           
   public void setSize(Dimension d) {
       super.setSize(d);
       init2();
   }
    void init2() {
        int w = getWidth();
        int h = getHeight();
        ComponentColorModel cm=new ComponentColorModel(
			ColorSpace.getInstance(ColorSpace.CS_GRAY),
			new int[] {8},
			false,		// has alpha
			false,		// alpha premultipled
			Transparency.OPAQUE,
			DataBuffer.TYPE_USHORT
		);

		ComponentSampleModel sm = new ComponentSampleModel(
			DataBuffer.TYPE_USHORT,
			w,
			h,
			1,
			w,
			new int[] {0}
		);
                
                final double delta = (iPanel.getMaximum() - iPanel.getMinimum())/getHeight();
                final int stepy = getHeight() / 255;
                short data [] = new short[w*h];
                
                for (int i = 0; i < h;) {
                    for (int m = 0; m < stepy; ++i, ++m)
                    for (int j = 0; j < w; ++j)
                        data[i*w+j] =(short)((h-i)*delta);
                }
                
		DataBuffer buf = new DataBufferUShort(data, w, 0);
                
		WritableRaster wr = Raster.createWritableRaster(sm,buf,new Point(0,0));
                
		iBuf = new BufferedImage(cm,wr,true,null);	// no properties hash table
    }
    public void windowChanged(WindowChangeEvent anEvt) {
            repaint();
    }    

    public void paintComponent(Graphics g) {  
       BufferedImage temp = iPanel.transform(iBuf);
       g.drawImage(temp, 0,0, getWidth(), getHeight(), null);
        
    }
}
