package com.bluelightning.poi;

import java.awt.Dimension;
import java.awt.Image;
import java.io.File;
import java.io.FileReader;

import javax.imageio.ImageIO;
import com.bluelightning.map.ButtonWaypoint;
import com.opencsv.CSVReader;

import seedu.addressbook.data.exception.IllegalValueException;
import seedu.addressbook.data.tag.Tag;
import seedu.addressbook.data.tag.UniqueTagList;


public class MurphyPOI extends POIBase {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected boolean hasGas;
	protected boolean hasDiesel;
	protected String  address;
	protected String  city;
	protected String  state;
	protected String  zip;
	protected String  exit;
	protected String  phone;
	
	
	protected static  Image  image;

	static {
		if (image == null) try {
			Dimension size = ButtonWaypoint.getImageSize();
			image = ImageIO.read(new File("images/murphy.jpg"))
					.getScaledInstance((int)size.getWidth(), (int)size.getHeight(), Image.SCALE_SMOOTH);
		} catch (Exception x) {
		}		
	}


	public MurphyPOI() {
	}

	public MurphyPOI(String[] fields) {
		super(fields);
		if (fields.length > 2) {
			parseColumnC(fields[2]);
			if (fields.length > 3) {
				parseColumnD(fields[3]);
			}
		}
		try {
			tagList.add( new Tag("Murphy") );
			if (hasDiesel) tagList.add( new Tag("Diesel") );
			if (hasGas) tagList.add( new Tag("Gas") );
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
		return String.format("%s %s/%s %s %s",
				/*super.toString(),*/ ""+name,
				(hasGas)?"gas":"", (hasDiesel)?"diesel":"",
						formatAddress(), ""+phone);
	}
	
	public String toCSV() {
		StringBuffer columnC = new StringBuffer();
		columnC.append(name);
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

		return String.format("%f,%f,\"%s\",\"%s\"", longitude, latitude, columnC.toString(), columnD.toString() );
	}
	
	protected void parseColumnC(String column) {
		String[] fields = column.split(";");
		name = fields[0];
		if (fields.length < 2) {
			return;
		}
		fields = fields[1].trim().split(",");
		int i = 0;
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
		        POI poi = new MurphyPOI( nextLine );
		        if (poi.getName().startsWith("Murphy"))
		        	list.add(poi);
		     }
			 reader.close();
		} catch (Exception e) {
			list.clear();;
		}
		 return list;
	}
	
	protected static String csvPath = "POI/Walmart_United States & Canada.csv"; 
	
	public static POISet factory() {
		return factory(csvPath);
	}
	
	
	@Override
	public Image getImage() {
		return image;
	}
	
	
	@Override
	public String getAddress() {
		return formatAddress();
	}

	@Override
	public UniqueTagList getTags() {
		return tagList;
	}

	@Override
	public FuelAvailable getFuelAvailable() {
		FuelAvailable fa = new FuelAvailable();
		if (hasGas) 
			fa.add( FuelAvailable.HAS_GAS );
		if (hasDiesel)
			fa.add( FuelAvailable.HAS_DIESEL );
		return fa;
	}
	
	public static void main(String[] args) {
    	POISet pset = MurphyPOI.factory("C:\\Users\\NOOK\\GIT\\default\\RobautoFX\\POI\\Walmart_United States & Canada.csv");
    	for (int i = 0; i < 15; i++)
    		System.out.println( pset.get(i).toString() );
	}

}
