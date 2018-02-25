package com.bluelightning.poi;

import java.awt.Dimension;
import java.awt.Image;
import java.io.File;
import java.io.FileReader;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.jxmapviewer.viewer.GeoPosition;

import com.bluelightning.map.ButtonWaypoint;
import com.bluelightning.map.SwingMarker;
import com.bluelightning.poi.POI.FuelAvailable;
import com.opencsv.CSVReader;

import seedu.addressbook.data.exception.IllegalValueException;
import seedu.addressbook.data.tag.Tag;
import seedu.addressbook.data.tag.UniqueTagList;


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
	protected boolean isNoOvernight;
	
	
	protected static  Image  imageOkToPark;
	protected static  Image  imageNoParking;

	static {
		if (imageOkToPark == null) try {
			Dimension size = ButtonWaypoint.getImageSize();
			imageOkToPark = ImageIO.read(new File("images/walmart-large.png"))
					.getScaledInstance((int)size.getWidth(), (int)size.getHeight(), Image.SCALE_SMOOTH);
			imageNoParking = ImageIO.read(new File("images/walmart-nopark-large.png"))
					.getScaledInstance((int)size.getWidth(), (int)size.getHeight(), Image.SCALE_SMOOTH);
		} catch (Exception x) {
		}		
	}


	public WalmartPOI() {
	}

	public WalmartPOI(String[] fields) {
		super(fields);
		if (fields.length > 2) {
			parseColumnC(fields[2]);
			if (fields.length > 3) {
				parseColumnD(fields[3]);
				if (fields.length > 4) {
					if (fields[4] != null && fields[4].contains("No Overnight"))
						isNoOvernight = true;
				}
			}
		}
		try {
			tagList.add( new Tag("Walmart") );
			if (hasDiesel) tagList.add( new Tag("Diesel") );
			if (hasGas) tagList.add( new Tag("Gas") );
			if (!isNoOvernight) tagList.add( new Tag("Overnight") );
		} catch (IllegalValueException e) {
			e.printStackTrace();
		}
	}
	
	
	public String formatAddress() {
		return String.format("%s, %s, %s, %s %s",
				address, city, state, zip,
				exit );
	}
	
	public String toString() {
		return String.format("%s %s %s/%s %s %s %s",
				/*super.toString(),*/ ""+name, ""+storeId,
				(hasGas)?"gas":"", (hasDiesel)?"diesel":"",
						formatAddress(), ""+phone, (isNoOvernight) ? "(No Overnight)" : "" );
	}
	
	public String toCSV() {
		StringBuffer columnC = new StringBuffer();
		columnC.append(name);
		columnC.append("; ");
		columnC.append(storeId);
		columnC.append(",");
		if (hasGas && hasDiesel)
			columnC.append(" Gas/Diesel,");
		else if (hasGas)
			columnC.append(" Gas,");
		else if (hasDiesel)
			columnC.append(" /Diesel,");
		
		StringBuffer columnD = new StringBuffer();
		columnD.append(address);
		if (exit != null && !exit.isEmpty()) {
			columnD.append(';');
			columnD.append(exit);
		}
		columnD.append(',');
		columnD.append(city);
		columnD.append(',');
		columnD.append(state);
		columnD.append(',');
		columnD.append(zip);
		columnD.append(',');
		columnD.append(phone);

		String columnE = (isNoOvernight) ? "No Overnight" : "";
		return String.format("%f,%f,\"%s\",\"%s\",\"%s\"", longitude, latitude, columnC.toString(), columnD.toString(), columnE );
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
		        if (poi.getName().startsWith("Walmart"))
		        	list.add(poi);
		     }
			 reader.close();
		} catch (Exception e) {
			list.clear();;
		}
		 return list;
	}
	
	protected static String csvPath = "POI/Walmart_Parking.csv"; 
	
	public static POISet factory() {
		return factory(csvPath);
	}
	
	
	@Override
	public Image getImage() {
		return (this.isNoOvernight) ? imageNoParking : imageOkToPark;
	}
	
	
	@Override
	public String getAddress() {
		return address;
	}

	@Override
	public UniqueTagList getTags() {
		return tagList;
	}

	@Override
	public FuelAvailable getFuelAvailable() {
		if (hasGas && hasDiesel) 
			return FuelAvailable.HAS_BOTH;
		else if (hasDiesel)
			return FuelAvailable.HAS_DIESEL;
		else if (hasGas)
			return FuelAvailable.HAS_GAS;
		return FuelAvailable.NO_FUEL;
	}
	
	public static void main(String[] args) {
    	POISet pset = WalmartPOI.factory("C:\\Users\\NOOK\\GIT\\default\\RobautoFX\\POI\\Walmart_United States & Canada.csv");
    	for (int i = 0; i < 15; i++)
    		System.out.println( pset.get(i).toString() );
	}

}
