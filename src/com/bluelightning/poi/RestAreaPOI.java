/**
 * 
 */
package com.bluelightning.poi;

import java.io.FileReader;

import com.bluelightning.Here2;
import com.bluelightning.LatLon;
import com.opencsv.CSVReader;

import seedu.addressbook.data.tag.UniqueTagList;

/**
 * @author NOOK
 *
 */
public class RestAreaPOI extends POIBase {
	
	protected String  address;
	protected String  state;
	protected String  highway;
	protected String  direction;
	protected String  mileMarker;
	protected boolean hasRvDump;
	protected boolean hasGas;
	protected boolean hasDiesel;
	protected boolean hasPetArea;
	protected boolean hasRestrooms;
	protected boolean hasPicnicTables;
	
	@Override
	public String toString() {
		StringBuffer attributes = new StringBuffer();
		if (hasRvDump) attributes.append("RV Dump,");
		if (hasGas) attributes.append("Gas,");
		if (hasDiesel) attributes.append("Diesel,");
		if (hasPetArea) attributes.append("Pets,");
		if (hasRestrooms) attributes.append("Restrooms,");
		if (hasPicnicTables) attributes.append("Picnic,");
		return String.format("%s, %s, [%s]", super.toString(), address, attributes.toString() );
	}

	
	public RestAreaPOI() {}
	
	public RestAreaPOI(String[] fields) {
		super(fields);
		String[] columnC = fields[2].split(","); //AL,I-10,WB,BALDWIN COUNTY WELCOME CENTER, MM65.8
		parseColumnC( columnC );
		String[] columnD = fields[3].split("\\|"); //[RR,PT,VM,Pets,HF]|RV Dump
		parseColumnD( columnD );
		// TODO tagList
	}

	private void parseColumnC(String[] columnC) {
		state = columnC[0].trim();
		highway = columnC[1].trim();
		direction = columnC[2].trim();
		name = columnC[3].trim();
		if (columnC.length > 4)
			mileMarker = columnC[4].trim();
		else
			mileMarker = "";
		address = String.format("%s %s %s %s", state, highway, direction, mileMarker).trim();
	}

	private void parseColumnD(String[] columnD) {
		if (columnD == null || columnD.length == 0)
			return;
		if (columnD.length > 1) {
			parseColumnD1( columnD[1]);
			
		}
		if (columnD[0].length() < 2)
			return;
		columnD = columnD[0].substring(1, columnD[0].length()-2).split(",");
		// where : RR=Rest Rooms, PT=Picnic Tables, VM=Vending Machines, Pets=Pet Walking Area, HF=Handicapped Facilities
		for (String field : columnD) {
			switch (field.toUpperCase().trim()) {
			case "RR": this.hasRestrooms = true; break;
			case "PT": this.hasPicnicTables = true; break;
			case "PETS": this.hasPetArea = true; break;
			}
		}
	}
	
	private void parseColumnD1(String columnD1) {
		String[] fields = columnD1.split(",");
		for (String field : fields) {
			switch (field.toUpperCase().trim()) {
			case "RV DUMP": this.hasRvDump = true;
			case "GAS": this.hasGas = true;
			case "DIESEL": this.hasDiesel = true;
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.bluelightning.poi.POI#getAddress()
	 */
	@Override
	public String getAddress() {
		return address;
	}

	/* (non-Javadoc)
	 * @see com.bluelightning.poi.POI#getTags()
	 */
	@Override
	public UniqueTagList getTags() {
		return tagList;
	}


	public static POISet factory( String filePath ) {
		POISet list = new POISet();
		try {
		     CSVReader reader = new CSVReader(new FileReader(filePath));
		     String [] nextLine;
		     while ((nextLine = reader.readNext()) != null) {
		        POI poi = new RestAreaPOI( nextLine );
		        if (!poi.getName().toUpperCase().contains("CLOSED")) {
		        	list.add(poi);
		        }
		     }
			 reader.close();
		} catch (Exception e) {
			list.clear();;
		}
		 return list;
	}
	
	public static POISet factory() {
		return factory("POI/RestAreasCombined_USA.csv");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
    	POISet pset = RestAreaPOI.factory("C:\\Users\\NOOK\\GIT\\default\\Robauto\\POI\\RestAreasCombined_USA.csv");
    	for (int i = 0; i < pset.size(); i++)
    		System.out.println( pset.get(i).toString() );
	}


	public String getState() {
		return state;
	}


	public String getHighway() {
		return highway;
	}


	public String getDirection() {
		return direction;
	}


	public String getMileMarker() {
		return mileMarker;
	}


	public boolean isHasRvDump() {
		return hasRvDump;
	}


	public boolean isHasGas() {
		return hasGas;
	}


	public boolean isHasDiesel() {
		return hasDiesel;
	}


	public boolean isHasPetArea() {
		return hasPetArea;
	}


	public boolean isHasRestrooms() {
		return hasRestrooms;
	}


	public boolean isHasPicnicTables() {
		return hasPicnicTables;
	}

}
