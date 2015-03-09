package com.ivli.roim;

//package ij.plugin;
//import ij.*;
//import ij.io.*;
//import ij.process.*;
import java.awt.*;
import java.io.*;
import java.awt.image.*;
import java.net.*;

/** Opens NIH Image look-up tables (LUTs), 768 byte binary LUTs
	(256 reds, 256 greens and 256 blues), LUTs in text format, 
	or generates the LUT specified by the string argument 
	passed to the run() method. */
public class LutLoader {// extends ImagePlus implements PlugIn {

	
        private LutLoader(){};
        
        
	/** If 'arg'="", displays a file open dialog and opens the specified
		LUT. If 'arg' is a path, opens the LUT specified by the path. If
		'arg'="fire", "ice", etc., uses a method to generate the LUT. */
	static final public IndexColorModel open(String arg) {        
		byte [] reds = new byte[256]; 
		byte [] greens = new byte[256]; 
		byte [] blues = new byte[256];
		int lutSize = 256;
		int nColors = 0;
		                
		if (arg.equals("invert"))
			{invertLut(); 
                        System.err.print("Not supported yet");
                        }
		else if (arg.equals("fire"))
			nColors = fire(reds, greens, blues);
		else if (arg.equals("grays"))
			nColors = grays(reds, greens, blues);
		else if (arg.equals("ice"))
			nColors = ice(reds, greens, blues);
		else if (arg.equals("spectrum"))
			nColors = spectrum(reds, greens, blues);
		else if (arg.equals("3-3-2 RGB"))
			nColors = rgb332(reds, greens, blues);
		else if (arg.equals("red"))
			nColors = primaryColor(4, reds, greens, blues);
		else if (arg.equals("green"))
			nColors = primaryColor(2, reds, greens, blues);
		else if (arg.equals("blue"))
			nColors = primaryColor(1, reds, greens, blues);
		else if (arg.equals("cyan"))
			nColors = primaryColor(3, reds, greens, blues);
		else if (arg.equals("magenta"))
			nColors = primaryColor(5, reds, greens, blues);
		else if (arg.equals("yellow"))
			nColors = primaryColor(6, reds, greens, blues);
		else if (arg.equals("redgreen"))
			nColors = redGreen(reds, greens, blues);
                else if (null != arg)
                        nColors = openLut(arg, false, reds, greens, blues);
                    
		
                if (nColors>0) {
			if (nColors<256)
				extrapolate(reds, greens, blues, nColors);
			
			return new IndexColorModel(8, nColors, reds, greens, blues);
		}
                return null;
	}
	
	
	
       
	private static final void invertLut() {
            
	}

	private static final int fire(byte[] reds, byte[] greens, byte[] blues) {
		int[] r = {0,0,1,25,49,73,98,122,146,162,173,184,195,207,217,229,240,252,255,255,255,255,255,255,255,255,255,255,255,255,255,255};
		int[] g = {0,0,0,0,0,0,0,0,0,0,0,0,0,14,35,57,79,101,117,133,147,161,175,190,205,219,234,248,255,255,255,255};
		int[] b = {0,61,96,130,165,192,220,227,210,181,151,122,93,64,35,5,0,0,0,0,0,0,0,0,0,0,0,35,98,160,223,255};
		for (int i=0; i<r.length; i++) {
			reds[i] = (byte)r[i];
			greens[i] = (byte)g[i];
			blues[i] = (byte)b[i];
		}
		return r.length;
	}

	private static final int grays(byte[] reds, byte[] greens, byte[] blues) {
		for (int i=0; i<256; i++) {
			reds[i] = (byte)i;
			greens[i] = (byte)i;
			blues[i] = (byte)i;
		}
		return 256;
	}
	
	private static final int primaryColor(int color, byte[] reds, byte[] greens, byte[] blues) {
		for (int i=0; i<256; i++) {
			if ((color&4)!=0)
				reds[i] = (byte)i;
			if ((color&2)!=0)
				greens[i] = (byte)i;
			if ((color&1)!=0)
				blues[i] = (byte)i;
		}
		return 256;
	}
	
	private static final int ice(byte[] reds, byte[] greens, byte[] blues) {
		int[] r = {0,0,0,0,0,0,19,29,50,48,79,112,134,158,186,201,217,229,242,250,250,250,250,251,250,250,250,250,251,251,243,230};
		int[] g = {156,165,176,184,190,196,193,184,171,162,146,125,107,93,81,87,92,97,95,93,93,90,85,69,64,54,47,35,19,0,4,0};
		int[] b = {140,147,158,166,170,176,209,220,234,225,236,246,250,251,250,250,245,230,230,222,202,180,163,142,123,114,106,94,84,64,26,27};
		for (int i=0; i<r.length; i++) {
			reds[i] = (byte)r[i];
			greens[i] = (byte)g[i];
			blues[i] = (byte)b[i];
		}
		return r.length;
	}

	private static final int spectrum(byte[] reds, byte[] greens, byte[] blues) {
		Color c;
		for (int i=0; i<256; i++) {
			c = Color.getHSBColor(i/255f, 1f, 1f);
			reds[i] = (byte)c.getRed();
			greens[i] = (byte)c.getGreen();
			blues[i] = (byte)c.getBlue();
		}
		return 256;
	}
	
	private static final int rgb332(byte[] reds, byte[] greens, byte[] blues) {
		Color c;
		for (int i=0; i<256; i++) {
			reds[i] = (byte)(i&0xe0);
			greens[i] = (byte)((i<<3)&0xe0);
			blues[i] = (byte)((i<<6)&0xc0);
		}
		return 256;
	}

	private static final int redGreen(byte[] reds, byte[] greens, byte[] blues) {
		for (int i=0; i<128; i++) {
			reds[i] = (byte)(i*2);
			greens[i] = (byte)0;
			blues[i] = (byte)0;
		}
		for (int i=128; i<256; i++) {
			reds[i] = (byte)0;
			greens[i] = (byte)(i*2);
			blues[i] = (byte)0;
		}
		return 256;
	}

	private static final int extrapolate(byte[] reds, byte[] greens, byte[] blues, int nColors) {
		byte[] r = new byte[nColors]; 
		byte[] g = new byte[nColors]; 
		byte[] b = new byte[nColors];
		System.arraycopy(reds, 0, r, 0, nColors);
		System.arraycopy(greens, 0, g, 0, nColors);
		System.arraycopy(blues, 0, b, 0, nColors);
		double scale = nColors/256.0;
		int i1, i2;
		double fraction;
		for (int i=0; i<256; i++) {
			i1 = (int)(i*scale);
			i2 = i1+1;
			if (i2==nColors) i2 = nColors-1;
			fraction = i*scale - i1;
			//IJ.write(i+" "+i1+" "+i2+" "+fraction);
			reds[i] = (byte)((1.0-fraction)*(r[i1]&255) + fraction*(r[i2]&255));
			greens[i] = (byte)((1.0-fraction)*(g[i1]&255) + fraction*(g[i2]&255));
			blues[i] = (byte)((1.0-fraction)*(b[i1]&255) + fraction*(b[i2]&255));
		}
                return 256;
	}
	
	/** Opens an NIH Image LUT, 768 byte binary LUT or text LUT from a file or URL. */
	private static final int openLut(String fi, boolean isURL, byte[] reds, byte[] greens, byte[] blues) {
		//IJ.showStatus("Opening: " + directory + fileName);
		//boolean isURL = url!=null && !url.equals("");
		int length = 0;
		String path = fi;//isURL?url:directory+fileName;
		if (!isURL) {
			File f = new File(fi);
			length = (int)f.length();
			if (length>10000) {
				error(path);
				return -1;
			}
		}
		int size = 0;
		try {
			if (length>768)
				size = openBinaryLut(fi, false, false, reds, greens, blues); // attempt to read NIH Image LUT
			if (size==0 && (length==0||length==768||length==970))
				size = openBinaryLut(fi, false, true, reds, greens, blues); // otherwise read raw LUT
			if (size==0 && length>768)
				size = openTextLut(fi, reds, greens, blues);
			if (size==0)
				error(path);
		} catch (IOException e) {
			System.out.printf(e.getMessage());
		}
		return size;
	}
	
	private static final void error(String path) {
		System.out.printf("LUT Reader", "This is not an ImageJ or NIH Image LUT, a 768 byte \nraw LUT, or a LUT in text format.\n \n"+path);
	}

	/** Opens an NIH Image LUT or a 768 byte binary LUT. */
	private static final int openBinaryLut(String fi, boolean isURL, boolean raw, byte[] reds, byte[] greens, byte[] blues) throws IOException {
		InputStream is;
		if (isURL)
			is = new URL(fi).openStream();
		else
			is = new FileInputStream(fi);
		DataInputStream f = new DataInputStream(is);
		int nColors = 256;
		if (!raw) {
			// attempt to read 32 byte NIH Image LUT header
			int id = f.readInt();
			if (id!=1229147980) { // 'ICOL'
				f.close();
				return 0;
			}
			int version = f.readShort();
			nColors = f.readShort();
			int start = f.readShort();
			int end = f.readShort();
			long fill1 = f.readLong();
			long fill2 = f.readLong();
			int filler = f.readInt();
		}
		//IJ.write(id+" "+version+" "+nColors);
		f.read(reds, 0, nColors);
		f.read(greens, 0, nColors);
		f.read(blues, 0, nColors);
		//if (nColors<256)
		//	interpolate(reds, greens, blues, nColors);
		f.close();
		return nColors;
	}
	
	private static final int openTextLut(String fi, byte[] reds, byte[] greens, byte[] blues) throws IOException {
            /*
		TextReader tr = new TextReader();
		tr.hideErrorMessages();
		ImageProcessor ip = tr.open(directory+fileName);
		if (ip==null)
			return 0;
		int width = ip.getWidth();
		int height = ip.getHeight();
		if (width<3||width>4||height<256||height>258) 
			return 0; 
		int x = width==4?1:0; 
		int y = height>256?1:0;
		ip.setRoi(x, y, 3, 256);
		ip = ip.crop();
		for (int i=0; i<256; i++) {
			reds[i] = (byte)ip.getPixelValue(0,i);
			greens[i] = (byte)ip.getPixelValue(1,i);
			blues[i] = (byte)ip.getPixelValue(2,i);
		}
		return 256;
            */
            return 0;
	}

	
	
	/** Opens the specified ImageJ LUT and returns
		it as an IndexColorModel. Since 1.43t. */
	private static IndexColorModel openLut(String path) throws IOException {
		return openLut(new FileInputStream(path));
	}

	/** Opens an ImageJ LUT using an InputStream
		and returns it as an IndexColorModel. Since 1.43t. */
	private static IndexColorModel openLut(InputStream stream) throws IOException {
		DataInputStream f = new DataInputStream(stream);
		byte[] reds = new byte[256]; 
		byte[] greens = new byte[256]; 
		byte[] blues = new byte[256];
		f.read(reds, 0, 256);
		f.read(greens, 0, 256);
		f.read(blues, 0, 256);
		f.close();
		return new IndexColorModel(8, 256, reds, greens, blues);
	}
	
	/** Creates a 256x32 image from an IndexColorModel. Since 1.43t. */
        /*
	public static ByteProcessor createImage(IndexColorModel cm) {
		int width = 256;
		int height = 32;
		byte[] pixels = new byte[width*height];
		ByteProcessor bp = new ByteProcessor(width, height, pixels, cm);
		int[] ramp = new int[width];
		for (int i=0; i<width; i++)
			ramp[i] = i; 
		for (int y=0; y<height; y++)
			bp.putRow(0, y, ramp, width);
		return bp;
	}
	*/
}
