/**
 * 
 */
package com.bluelightning;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;

import org.jxmapviewer.viewer.GeoPosition;


/**
 * @author lintondf
 */
public class Copilot {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CoPilot13Format format = new CoPilot13Format();
		String path = "/Users/lintondf/GIT/RobautoFX/adb/NA_save/gpstrip.trp";
		String opath = "/Users/lintondf/GIT/RobautoFX/adb/NA_save/gpstrip2.trp";
		try {
			List<GeoPosition> positions = format.read( new BufferedReader(new InputStreamReader( new FileInputStream(path), CoPilot13Format.UTF16LE_ENCODING)));//, );
		    PrintWriter stream = new PrintWriter(opath, CoPilot13Format.UTF16LE_ENCODING);
			format.write("MyRoute", positions, stream, 0, positions.size() );
			stream.close();
		} catch (Exception x ) {
			x.printStackTrace();
		}
	}

}
