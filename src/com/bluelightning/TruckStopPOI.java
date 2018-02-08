package com.bluelightning;

import java.io.FileReader;
import java.util.ArrayList;

import com.opencsv.CSVReader;

public class TruckStopPOI extends POIBase {
	
	protected String locationId;
	protected ArrayList<String> attributes = new ArrayList<String>();
	protected String  address;
	protected String  city;
	protected String  state;
	protected String  zip;
	protected String  exit;
	protected String  phone;
	
	public String toString() {
		StringBuffer attrStr = new StringBuffer();
		for (String attr : attributes) {
			attrStr.append(attr);
			attrStr.append('/');
		}
		return String.format("(%10.6f, %10.6f) %s: %s; %s; %s; %s; %s; %s; %s: %s", 
				latitude, longitude, name, locationId,
				address, city, state, zip, exit, phone, attrStr.toString() );
	}

	public TruckStopPOI() {
	}

	public TruckStopPOI(String[] fields) {
		super(fields);
		String[] columnC = name.split(";");
		name = columnC[0];
		if (columnC.length > 1) {
			parseColumnC( columnC );
		}
		for (int c = 3; c < fields.length; c++) {
			switch (c) {
			case 3:
				phone = fields[c].trim();
				break;
			case 4:
				String[] parts = fields[c].trim().split(";");
				if (parts.length == 1) {
					address = parts[0];
					exit = "";
				} else {
					exit = parts[0];
					address = parts[1];
				}
				break;
			case 5:
				city = fields[c].trim();
				break;
			case 6:
				state = fields[c].trim();
				break;
			case 7:
				zip = fields[c].trim();
				break;
			default: break;
			}
		}
	}

	private void parseColumnC(String[] columnC) {
		for (int i = 1; i < columnC.length; i++) {
			String field = columnC[i].trim();
			if (field.startsWith("#")) {
				int j = field.indexOf(' ');
				if (j == -1) {
					locationId = field;
				} else {
					locationId = field.substring(0, j);
					attributes.add( field.substring(j+1).trim());
				}
			} else {
				String[] fields = field.split("/");
				for (String f : fields) {
					attributes.add( f.trim() );
				}
			}
		}
	}

	public static POISet factory( String filePath ) {
		POISet list = new POISet();
		try {
		     CSVReader reader = new CSVReader(new FileReader(filePath));
		     String [] nextLine;
		     while ((nextLine = reader.readNext()) != null) {
		    	if (nextLine[2].startsWith("AMBEST"))
		    		continue;
		        POI poi = new TruckStopPOI( nextLine );
		        list.add(poi);
		     }
			 reader.close();
		} catch (Exception e) {
			list.clear();;
		}
		 return list;
	}
	
	public static void main(String[] args) {
    	POISet pset = TruckStopPOI.factory("C:\\Users\\NOOK\\GIT\\default\\RobautoFX\\POI\\Truck_Stops.csv");
    	for (int i = 0; i < pset.size(); i++)
    		System.out.println( pset.get(i).toString() );
	}

}
