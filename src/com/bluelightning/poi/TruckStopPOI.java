package com.bluelightning.poi;

import java.awt.Dimension;
import java.awt.Image;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import com.bluelightning.map.ButtonWaypoint;
import com.opencsv.CSVReader;

import seedu.addressbook.data.tag.UniqueTagList;

public class TruckStopPOI extends POIBase {

	protected String locationId;
	protected ArrayList<String> attributes = new ArrayList<String>();
	protected String address;
	protected String city;
	protected String state;
	protected String zip;
	protected String exit;
	protected String phone;
	protected Image image;

	protected static Image defaultImage;
	protected static TreeMap<String, Image> imagesByName = new TreeMap<>();

	static {
		Dimension size = ButtonWaypoint.getImageSize();
		try {
			defaultImage = ImageIO.read(new File("images/Truck_Stop.png")).getScaledInstance((int) size.getWidth(),
					(int) size.getHeight(), Image.SCALE_SMOOTH);
			imagesByName.put("Flying J", ImageIO.read(new File("images/FlyingJ.png"))
					.getScaledInstance((int) size.getWidth(), (int) size.getHeight(), Image.SCALE_SMOOTH));
			imagesByName.put("Love's", ImageIO.read(new File("images/Loves.png"))
					.getScaledInstance((int) size.getWidth(), (int) size.getHeight(), Image.SCALE_SMOOTH));
			imagesByName.put("Petro", ImageIO.read(new File("images/Petro.png"))
					.getScaledInstance((int) size.getWidth(), (int) size.getHeight(), Image.SCALE_SMOOTH));
			imagesByName.put("Pilot", ImageIO.read(new File("images/Pilot.png"))
					.getScaledInstance((int) size.getWidth(), (int) size.getHeight(), Image.SCALE_SMOOTH));
			imagesByName.put("Sapp TC", ImageIO.read(new File("images/Sapp_TC.png"))
					.getScaledInstance((int) size.getWidth(), (int) size.getHeight(), Image.SCALE_SMOOTH));
			imagesByName.put("TA", ImageIO.read(new File("images/TA.png"))
					.getScaledInstance((int) size.getWidth(), (int) size.getHeight(), Image.SCALE_SMOOTH));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String toString() {
		StringBuffer attrStr = new StringBuffer();
		for (String attr : attributes) {
			attrStr.append(attr);
			attrStr.append('/');
		}
		return String.format("%s: %s; %s; %s; %s; %s; %s; %s: [%s]", super.toString(), locationId, address, city, state,
				zip, exit, phone, attrStr.toString());
	}

	public TruckStopPOI() {
		image = defaultImage;
	}

	public TruckStopPOI(String[] fields) {
		super(fields);
		String[] columnC = name.split(";");
		name = columnC[0];
		if (columnC.length > 1) {
			parseColumnC(columnC);
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
			default:
				break;
			}
		}
		// TODO tagList
		image = defaultImage;
		for (String prefix : imagesByName.keySet()) {
			if (name.toUpperCase().startsWith(prefix.toUpperCase())) {
				image = imagesByName.get(prefix);
				break;
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
					attributes.add(field.substring(j + 1).trim());
				}
			} else {
				String[] fields = field.split("/");
				for (String f : fields) {
					attributes.add(f.trim());
				}
			}
		}
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
	public Image getImage() {
		return image;
	}

	public static POISet factory(String filePath) {
		POISet list = new POISet();
		try {
			CSVReader reader = new CSVReader(new FileReader(filePath));
			String[] nextLine;
			while ((nextLine = reader.readNext()) != null) {
				if (nextLine[2].startsWith("AMBEST"))
					continue;
				POI poi = new TruckStopPOI(nextLine);
				list.add(poi);
			}
			reader.close();
		} catch (Exception e) {
			list.clear();
			;
		}
		return list;
	}

	public static POISet factory() {
		return factory("POI/Truck_Stops.csv");
	}

	public static void main(String[] args) {
		POISet pset = TruckStopPOI.factory("C:\\Users\\NOOK\\GIT\\default\\Robauto\\POI\\Truck_Stops.csv");
		TreeSet<String> names = new TreeSet<>();
		for (int i = 0; i < pset.size(); i++) {
			System.out.println(pset.get(i).toString());
			names.add(pset.get(i).getName());
		}
		for (String name : names) {
			System.out.println(name);
		}
	}

}
