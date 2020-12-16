package com.bluelightning;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

public class Configuration {

	public static class Abbreviation {
		public String from;
		public String to;
	}

	// JSON Serialized Elements
	public double milesPerGallon;
	public double fuelInGallons;
	public int mapCyclesPerMapUpdate;
	public int mapCyclesPerMapRotation;
	
	public double minimumDriveMinutes;
	public double maximumDriveMinutes;
	public double driveMinutesFuzz;
	
	public List<Abbreviation> abbreviations;
	
	private static Configuration singleton;
	
	private Configuration() {
		abbreviations = new Vector<>();
	}
	
	protected void dump() {
		for (int i = 0; i < abbreviations.size(); i++) {
			System.out.println( abbreviations.get(i).from + "->" + abbreviations.get(i).to);
		}
		
	}
	
	/**
	 * @return the singleton
	 */
	public static Configuration getSingleton() {
		if (singleton == null) {
			java.io.InputStream is = Thread.currentThread().getClass().getResourceAsStream("/resources/robauto.json");
			//String appConfigPath = Thread.currentThread().getContextClassLoader().getResource("resources/robauto.json").getPath();
			//String appConfigPath = rootPath + "robauto.json";
			Gson gson = new Gson();
			try (Reader reader = new InputStreamReader(is)) { //new FileReader(appConfigPath)) {
				singleton = gson.fromJson(reader, Configuration.class);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return singleton;
	}

	public static void main(String[] args) {
		Configuration.getSingleton().dump();
		System.exit(0);
	}

}
