/**
 * 
 */
package com.bluelightning;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;

import com.bluelightning.poi.SamsClubPOI;

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
		String ipath = "gpstrip.trp.base";
		String opath =  "\\\\Surfacepro3\\NA\\save\\gpstrip.trp";
		try {
			List<VisitedPlace> positions = format.read( new BufferedReader(new InputStreamReader( new FileInputStream(ipath), CoPilot13Format.UTF16LE_ENCODING)));//, );
			positions.forEach(System.out::println);
			String[] fields = {"-77.2966","38.647307",
					"Sam's Club; #6371,  Gas,",
					"14050 Worth Ave; I-95 Exit 156,Woodbridge,VA,22192,(703) 491-2662,"
			};
			SamsClubPOI poi = new SamsClubPOI( fields );
			positions.remove(1);
			positions.add( new VisitedPlace( poi ) );
		    PrintWriter stream = new PrintWriter(opath, CoPilot13Format.UTF16LE_ENCODING);
			format.write("MyRoute", positions, stream, 0, positions.size() );
			stream.close();
			stream = new PrintWriter( System.out );
			format.write("MyRoute", positions, stream, 0, positions.size() );
			stream.close();			
		} catch (Exception x ) {
			x.printStackTrace();
		}
	}

}
