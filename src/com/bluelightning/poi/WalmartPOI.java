package com.bluelightning.poi;

import java.awt.Image;
import java.io.File;
import java.io.FileReader;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.jxmapviewer.viewer.GeoPosition;

import com.bluelightning.map.SwingMarker;
import com.opencsv.CSVReader;


public class WalmartPOI extends POIBase {
	
	protected String  storeId;
	protected boolean hasGas;
	protected boolean hasDiesel;
	protected String  address;
	protected String  city;
	protected String  state;
	protected String  zip;
	protected String  exit;
	protected String  phone;
	

	public WalmartPOI() {
	}

	public WalmartPOI(String[] fields) {
		super(fields);
		if (fields.length > 2) {
			parseColumnC(fields[2]);
			if (fields.length > 3) {
				parseColumnD(fields[3]);
			}
		}
	}
	
	public String formatAddress() {
		return String.format("%s, %s, %s, %s %s",
				address, city, state, zip,
				exit );
	}
	
	public String toString() {
		return String.format("%s: %s %s/%s %s %s",
				super.toString(), ""+storeId,
				(hasGas)?"gas":"", (hasDiesel)?"diesel":"",
						formatAddress(), ""+phone );
	}
	
	protected void parseColumnC(String column) {
		String[] fields = column.split(";");
		name = fields[0];
		if (fields.length < 2) {
			return;
		}
		fields = fields[1].trim().split(",");
		int i = 0;
		if (fields[i].startsWith("#")) {
			storeId = fields[i];
			i++;
		}
		if (i < fields.length) {
			if (fields[i].toLowerCase().contains("gas"))
				hasGas = true;
			if (fields[i].toLowerCase().contains("diesel"))
				hasDiesel = true;
		}
	}

	protected void parseColumnD(String column) {
		String[] fields = column.split(",");
		address = "";
		exit = "";
		city = "";
		state = "";
		zip = "";
		phone = "";
		int i = fields[0].indexOf(';');
		if (i == -1) {
			address = fields[0];
		} else {
			address = fields[0].substring(0, i);
			exit = fields[0].substring(i+1);
		}
		if (fields.length < 2) return;
		city = fields[1];
		if (fields.length < 3) return;
		state = fields[2];
		if (fields.length < 4) return;
		zip = fields[3];
		if (fields.length < 5) return;
		phone = fields[4];
	}
	
	public static POISet factory( String filePath ) {
		POISet list = new POISet();
		try {
		     CSVReader reader = new CSVReader(new FileReader(filePath));
		     String [] nextLine;
		     while ((nextLine = reader.readNext()) != null) {
		        POI poi = new WalmartPOI( nextLine );
		        list.add(poi);
		     }
			 reader.close();
		} catch (Exception e) {
			list.clear();;
		}
		 return list;
	}
	
	
	public static POISet factory() {
		return factory("POI/SamsClubs_USA.csv");
	}
	
	@Override
	public SwingMarker getMarker(String report) {
		try {
			Image img = ImageIO.read(new File("images/samsclub.jpg")); //getClass().getResource("images/costco.png"));
			return new SwingMarker( img, new GeoPosition(latitude, longitude), getName(), report );
		} catch (Exception x) {
			return new SwingMarker( new GeoPosition(latitude, longitude), getName(), report );
		}
	}
	
	
	public static void main(String[] args) {
    	POISet pset = WalmartPOI.factory("C:\\Users\\NOOK\\GIT\\default\\RobautoFX\\POI\\SamsClubs_USA.csv");
    	for (int i = 0; i < 15; i++)
    		System.out.println( pset.get(i).toString() );
	}

}
