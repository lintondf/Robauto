/**
 * 
 */
package com.bluelightning.poi;

import java.awt.Image;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GeodeticCurve;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jxmapviewer.viewer.GeoPosition;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import seedu.addressbook.data.tag.UniqueTagList;

/**
 * @author NOOK
 *
 */
public class AtlasObscura extends POIBase {

	private static final long serialVersionUID = 1L;

	protected String placeId;
	protected String href;
	protected String city;
	protected String state;
	protected String subtitle;

	// protected Image image;
	
	public AtlasObscura() {}
	
	public AtlasObscura(String[] fields) {
		this.name = fields[0].trim();
		this.subtitle = fields[1].trim();
		this.city = fields[3].trim();
		this.state = fields[2].trim();
		this.latitude = Double.parseDouble(fields[4].trim());
		this.longitude = Double.parseDouble(fields[5].trim());
		this.placeId = fields[6].trim();
		this.href = fields[7].trim();
	}

	public String toHtml() {
		StringBuilder sb = new StringBuilder();
		sb.append("<H2>"); sb.append(name); sb.append("</H2>");
		sb.append("<P>");
		sb.append(String.format("<A TARGET='_blank' HREF='https://www.atlasobscura.com/%s'>%s</A>", href, subtitle));
		sb.append("</P>");
		sb.append("<P>");
		String placeLink = String.format("<A TARGET='_blank' HREF='https://www.google.com/maps/@%.7f,%.7f,16.29z/data=!5m1!1e1'>%s, %s</A>", latitude, longitude, city, state );
		sb.append(placeLink);
		sb.append("</P>");
		return sb.toString();
	}
	
	@Override
	public String getAddress() {
		return String.format("%s, %s; %10.6f, %10.6f", city, state, latitude, longitude );
	}

	// @Override
	// public Image getImage() {
	// return image;
	// }

	@Override
	public UniqueTagList getTags() {
		return this.tagList;
	}

	public static POISet factory( String filePath ) {
		POISet list = new POISet();
		try {
		     CSVReader reader = new CSVReader(new FileReader(filePath));
		     String [] nextLine;
		     while ((nextLine = reader.readNext()) != null) {
		        POI poi = new AtlasObscura( nextLine );
	        	list.add(poi);
		     }
			 reader.close();
		} catch (Exception e) {
			list.clear();
		}
		 return list;
	}
	
	public static POISet factory() {
		return factory("POI/AtlasObscura.csv");
	}
	
	public static void mainNormal(String[] args) {
		POISet set = factory();
		set.forEach(System.out::println);
	}
	
    // website scrapping code below
	private static State states;
	
	private static int loadStatePage(CSVWriter writer, String stateName, int page) {
		int count = 0;
		String url = String.format("https://www.atlasobscura.com/things-to-do/%s/places?page=%d",
				stateName.toLowerCase(), page);
		try {
			Connection conn = Jsoup.connect(url).followRedirects(true).timeout(10000);

			Document doc = conn.get();
			System.out.println(url + " : " + doc.title());
			Elements container = doc.getElementsByClass("index-card-wrap");
			for (Element card : container) {
				AtlasObscura atlasObscura = new AtlasObscura();
				Elements element = card.getElementsByClass("content-card-place");
				if (!element.isEmpty()) {
					atlasObscura.placeId = element.get(0).attr("data-place-id");
					atlasObscura.href = element.get(0).attr("href");
				}
				element = card.getElementsByClass("place-card-location");
				if (!element.isEmpty()) {
					String cityState = element.get(0).text();
					String[] fields = cityState.split(",");
					atlasObscura.city = "";
					atlasObscura.state = fields[0];
					if (fields.length > 1) {
						atlasObscura.city = fields[1];
					}
				}
				element = card.getElementsByClass("content-card-title");
				if (!element.isEmpty()) {
					atlasObscura.name = element.get(0).text();
				}
				element = card.getElementsByClass("content-card-subtitle");
				if (!element.isEmpty()) {
					atlasObscura.subtitle = element.get(0).text();
				}
				element = card.getElementsByClass("lat-lng");
				if (!element.isEmpty()) {
					String latLon = element.get(0).text();
					String[] fields = latLon.split(",");
					atlasObscura.latitude = Double.parseDouble(fields[0].trim());
					atlasObscura.longitude = Double.parseDouble(fields[1].trim());
				}
				//System.out.println("  " + atlasObscura.toString());
				atlasObscura.writeCSV(writer);
				count++;
			}
			Thread.sleep(1000);
		} catch (HttpStatusException hx) {
			return 0;
		} catch (Exception x) {
			x.printStackTrace();
		}
		return count;
	}

	public String toString() {
		return String.format("%s - %s, %s, %s, %10.6f, %10.6f, %s, %s", this.name, this.subtitle, this.city, this.state,
				this.latitude, this.longitude, this.placeId, this.href);
	}

	public void writeCSV(CSVWriter writer) {
		String[] fields = {
				this.name, this.subtitle, this.city, this.state,
				Double.toString(this.latitude), Double.toString(this.longitude), this.placeId, this.href
		};
		writer.writeNext(fields);
	}

	private static int loadState(CSVWriter writer, String stateName) {
		int page = 1;
		int count = 0;
		int c = 0;
		while ((c = loadStatePage(writer, stateName, page)) > 0) {
			count += c;
			c = 0;
			page++;
		}
		return count;
	}

	/**
	 * @param args
	 */
	public static void mainScrape(String[] args) {
		try {
			FileWriter out = new FileWriter("atlasobscura.csv");
			CSVWriter writer = new CSVWriter(out);
			for (State state : states.values()) {
				if (state.toString().equals("Unknown"))
					continue;
				int count = loadState( writer, state.toString() );				
				System.out.println(state.toString() + " : " + count);
			}
			writer.close();
		} catch (Exception x) {
			x.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		//mainScrape(args);
		mainNormal(args);
	}

}
