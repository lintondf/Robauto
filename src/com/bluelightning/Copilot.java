/**
 * 
 */
package com.bluelightning;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;

import seedu.addressbook.data.place.VisitedPlace;


/**
 * @author lintondf
 */
public class Copilot {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CoPilot13Format format = new CoPilot13Format();
		String path = "\\\\Surfacepro3\\na\\save\\gpstrip.trp";
		String opath = "/Users/lintondf/GIT/RobautoFX/adb/NA_save/gpstrip2.trp";
		try {
			List<VisitedPlace> positions = format.read( new BufferedReader(new InputStreamReader( new FileInputStream(path), CoPilot13Format.UTF16LE_ENCODING)));//, );
			positions.forEach(System.out::println);
//		    PrintWriter stream = new PrintWriter(opath, CoPilot13Format.UTF16LE_ENCODING);
			PrintWriter stream = new PrintWriter(System.out);
			format.write("MyRoute", positions, stream, 0, positions.size() );
			stream.close();
		} catch (Exception x ) {
			x.printStackTrace();
		}
	}

}
