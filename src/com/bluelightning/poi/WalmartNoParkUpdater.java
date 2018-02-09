/**
 * 
 */
package com.bluelightning.poi;

import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.TreeMap;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.opencsv.CSVReader;

/**
 * @author NOOK
 *
 */
public class WalmartNoParkUpdater extends WalmartPOI {
	
	protected static TreeMap<String, ArrayList<WalmartPOI>> byState = new TreeMap<>();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String csvPath = "POI/Walmart_United States & Canada.csv"; 
		String outPath = WalmartPOI.csvPath;
		try {
			PrintWriter out = new PrintWriter( outPath );
			POISet pset = WalmartPOI.factory(csvPath);
			for (POI p : pset) {
				WalmartPOI walmart = (WalmartPOI) p;
				State state = State.valueOfAbbreviation(walmart.state);
				if (!state.toString().equals("Unknown")) {
					ArrayList<WalmartPOI> list = byState.get(state.toString());
					if (list == null)
						list = new ArrayList<>();
					list.add( walmart );
					byState.put( state.toString(), list );
				}
			}
			for (String state : byState.keySet()) {
				ArrayList<WalmartPOI> list = byState.get(state);
				System.out.println( state + " " + list.size() );
				String url = String.format("http://www.walmartlocator.com/no-park-walmarts-in-%s/", state.toLowerCase()); 
				try {
					Connection conn = Jsoup.connect(url).followRedirects(true).timeout(10000);
					
					Document doc = conn.get();
					Elements container = doc.getElementsByClass("entry-content");
					Elements lis = container.first().getElementsByTag("LI");
					String addresses = lis.text();
					for (WalmartPOI poi : list) {
						if (poi.zip.equals("35022") || addresses.contains(poi.zip)) {
							poi.isNoOvernight = true;
							System.out.println(poi.toCSV());
						}
						out.println(poi.toCSV());
					}
					Thread.sleep(10000);
				} catch (Exception x) {
					x.printStackTrace();
				}
			} // for each state
			out.close();
			POISet p1 = WalmartPOI.factory(outPath);
			p1.forEach(System.out::println);
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

}
