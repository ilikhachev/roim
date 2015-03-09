package com.ivli.roim;


import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;


public class Lookup_Tables {
	private String path = "D:\\temp\\Lookup_Tables\\";	// path to resources
	private static int nameChoice = 0;
	private static boolean first = true;
        private static IndexColorModel cm = null;
        
        public IndexColorModel getLUT() {return cm;}
	public void load(String arg) {
		//IJ.register(Lookup_Tables.class);	// remember choices
		String s = getNames();
		if(s=="") return;
		String[] namesIn = s.split("[\\n\\r\\t\\s]+");
		String[] names = new String[namesIn.length];
		for(int i=0; i<namesIn.length; i++) {
			if(namesIn[i].indexOf(".")<0)
				names[i] = namesIn[i];
			else
				names[i] = namesIn[i].substring(0, namesIn[i].indexOf("."));
			names[i] = names[i].replace('_', ' ');
			if(first)
				if(names[i].equalsIgnoreCase("gray")) nameChoice = i;
		}
                
		//java.awt.Dialog gd = new Dialog("Look Up Table (LUT)");
		//gd.addStringField("Resource:", path, 40);
		//gd.addChoice("LUT name", names, names[nameChoice]);
		//gd.showDialog();
		//if(gd.wasCanceled()) return;
		first = false;
		//path = gd.getNextString();
		nameChoice = 1;//gd.getNextChoiceIndex();
		getLUT(namesIn[nameChoice]);
		return;
	}

	private String getNames() {
		String names = "LUTnames.txt";
		String s = "";
		try {
			// get the list of names as a stream
			InputStream is = getClass().getResourceAsStream(path+names);
			if (is==null) {
				//IJ.showMessage("Lookup Tables", "LUTnames.txt not found");
				return "";
			}
			InputStreamReader isr = new InputStreamReader(is);
			StringBuffer sb = new StringBuffer();
			char[] b = new char[8192];
			int n;
			// read a block
			while((n=isr.read(b)) > 0 )
				sb.append(b,0,n);
			s = sb.toString();
		} catch(IOException e) {
				String msg = e.getMessage();
				if(msg==null || msg.equals(""))
					msg = ""+e;
				System.out.printf("Lookup Tables %s", msg);
				return "";
		}
		return s;
	}

	private void getLUT(String lutName) {
            /*
		//ImagePlus imp = WindowManager.getCurrentImage();
		IndexColorModel cm = null;
		try {
			InputStream is = getClass().getResourceAsStream(path+lutName);
			if(is==null) {
				return;
			}
			cm = LutLoader.open(is);
		} catch(IOException e) {
				String msg = e.getMessage();
				if(msg==null || msg.equals(""))
					msg = ""+e;
				System.out.printf("Lookup Tables %s", msg);
				return;
		}
 */
	}

}
