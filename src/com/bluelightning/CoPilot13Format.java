package com.bluelightning;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jxmapviewer.viewer.GeoPosition;

/*
 * Based on https://github.com/cpesch/RouteConverter
 */
public class CoPilot13Format  {
	
	public static final String UTF16LE_ENCODING = "UTF-16LE";
	
    protected static final int BYTE_ORDER_MARK = 65279; // '\ufeff';

    protected static final String DATA_VERSION = "Data Version";
    private static final String START_TRIP = "Start Trip";
    private static final String END_TRIP = "End Trip";
    private static final String START_STOP = "Start Stop";
    private static final String END_STOP = "End Stop";
    private static final String START_STOP_OPT = "Start StopOpt";
    private static final String END_STOP_OPT = "End StopOpt";
    private static final String STOP = "Stop";

    protected static final char NAME_VALUE_SEPARATOR = '=';
    protected static final Pattern NAME_VALUE_PATTERN = Pattern.compile("(.+?)" + NAME_VALUE_SEPARATOR + "(.+|)");
    protected static final double INTEGER_FACTOR = 1000000.0;

    private static final String CREATOR = "Creator";
    private static final String LONGITUDE = "Longitude";
    private static final String LATITUDE = "Latitude";
    private static final String STATE = "State";
    private static final String ZIP = "Zip";
    private static final String CITY = "City";
    private static final String COUNTY = "County";
    private static final String ADDRESS = "Address"; // houseNumber<space>street
    private static final String SHOW = "Show";
    private static final String SEQUENCE = "Sequence";

    protected  Map<String, String> tripMap = new HashMap<>();
    protected  List<String> tripMapKeys = new ArrayList<>();
    
    public static String trim(String string) {
        if (string == null)
            return null;
        string = string.trim();
        if (string.length() == 0)
            return null;
        else
            return string;
    }

    public static String trim(String string, int length) {
        string = trim(string);
        if (string == null)
            return null;
        return string.substring(0, Math.min(string.length(), length));
    }

    public static String formatIntAsString(Integer anInteger) {
        if (anInteger == null)
            return "0";
        return Integer.toString(anInteger);
    }

    public static String formatIntAsString(Integer anInteger, int exactDigitCount) {
        StringBuilder buffer = new StringBuilder(formatIntAsString(anInteger));
        while (buffer.length() < exactDigitCount)
            buffer.insert(0, "0");
        return buffer.toString();
    }

    public static Integer parseInteger(String string) {
        String trimmed = trim(string);
        if (trimmed != null) {
            if (trimmed.startsWith("+"))
                trimmed = trimmed.substring(1);
            return Integer.parseInt(trimmed);
        } else
            return null;
    }

    public static int parseInt(String string) {
        Integer integer = parseInteger(string);
        return integer != null ? integer : -1;
    }


    
	protected boolean isDataVersion(String line) {
        String data = line.charAt(0) == BYTE_ORDER_MARK ? line.substring(1) : line;
		return data.startsWith("Data Version:13");
	}

//    public void read(InputStream source, ParserContext<Wgs84Route> context) throws Exception {
//        read(source, UTF16LE_ENCODING, context);
//    }
//
//    public void write(Wgs84Route route, OutputStream target, int startIndex, int endIndex) throws IOException {
//        write(route, target, UTF16LE_ENCODING, startIndex, endIndex);
//    }

    public void write(String routeName, List<GeoPosition> positions, PrintWriter writer, int startIndex, int endIndex) {
        // with UTF-16LE no BOM is written, UnicodeLittle would write one by is not supported
        // (see http://java.sun.com/j2se/1.4.2/docs/guide/intl/encoding.doc.html)
        // but the fix from http://mindprod.com/jgloss/encoding.html helped me
        writer.write(BYTE_ORDER_MARK);
        writer.println(DATA_VERSION + ":13.4.1.3");
        writeHeader(routeName, writer);
        writePositions(positions, writer, startIndex, endIndex);
    }

    public String getExtension() {
        return ".trp";
    }

//    public BaseNavigationPosition getDuplicateFirstPosition(BaseRoute<BaseNavigationPosition, BaseNavigationFormat> route) {
//        List<BaseNavigationPosition> positions = route.getPositions();
//        NavigationPosition first = positions.get(0);
//        return asGeoPosition(first.getLongitude(), first.getLatitude(), "Start:" + first.getDescription());
//    }

    public List<GeoPosition> read(BufferedReader reader) throws IOException {
        List<GeoPosition> positions = new ArrayList<>();
        
        Map<String, String> map = null;
        String routeName = null;

    	reader.mark(4);
    	int first = reader.read();
    	if (first != BYTE_ORDER_MARK) {
    		reader.reset();
    	}
        while (true) {
            String line = reader.readLine();
            if (line == null)
                break;
            if (trim(line) == null)
                continue;

            //noinspection StatementWithEmptyBody
            if (isDataVersion(line) || line.startsWith(END_TRIP) || line.startsWith(END_STOP_OPT)) {
            } else if (line.startsWith(START_TRIP)) {
                routeName = trim(parseValue(line));
                map = tripMap;
            } else if (line.startsWith(START_STOP) || line.startsWith(START_STOP_OPT)) {
                map = new HashMap<>();
            } else if (line.startsWith(END_STOP)) {
                GeoPosition position = parsePosition(map);
                positions.add(position);
            } else if (isNameValue(line)) {
                String name = parseName(line);
                String value = parseValue(line);
                map.put(name, value);
                if (map == tripMap) {
                	tripMapKeys.add(name);
                }
            } else {
                return null;
            }
        }
        return positions;
    }


    boolean isNameValue(String line) {
        Matcher matcher = NAME_VALUE_PATTERN.matcher(line);
        return matcher.matches();
    }

    private String parseName(String line) {
        Matcher matcher = NAME_VALUE_PATTERN.matcher(line);
        if (!matcher.matches())
            throw new IllegalArgumentException("'" + line + "' does not match");
        return matcher.group(1);
    }

    private String parseValue(String line) {
        Matcher matcher = NAME_VALUE_PATTERN.matcher(line);
        if (!matcher.matches())
            throw new IllegalArgumentException("'" + line + "' does not match");
        return matcher.group(2);
    }

    GeoPosition parsePosition(Map<String, String> map) {
        Integer latitude = parseInteger(map.get(LATITUDE));
        Integer longitude = parseInteger(map.get(LONGITUDE));
        String state = trim(map.get(STATE));
        String zip = trim(map.get(ZIP));
        String city = trim(map.get(CITY));
        String county = trim(map.get(COUNTY));
        String address = trim(map.get(ADDRESS));
        String description = (state != null ? state + (zip != null ? "-" : " ") : "") +
                (zip != null ? zip + " " : "") + (city != null ? city : "") +
                (county != null ? ", " + county : "") + (address != null ? ", " + address : "");
        return new GeoPosition( 
                latitude / INTEGER_FACTOR,
        		longitude / INTEGER_FACTOR);
    }
    
    protected String GENERATED_BY = "Robauto";

    protected void writeHeader(String routeName, PrintWriter writer) {
        tripMap.put(START_TRIP, routeName);
        tripMap.put(CREATOR, GENERATED_BY);
        tripMapKeys.forEach( key -> {
        	writer.println( key + NAME_VALUE_SEPARATOR + tripMap.get(key));
        });
        writer.println(END_TRIP); 
        writer.println();
    }

    protected void writePositions(List<GeoPosition> positions, PrintWriter writer, int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex; i++) {
            GeoPosition position = positions.get(i);
            writer.println(START_STOP + NAME_VALUE_SEPARATOR + STOP + " " + i);
            String longitude = formatIntAsString((int) (position.getLongitude() * INTEGER_FACTOR));
            writer.println(LONGITUDE + NAME_VALUE_SEPARATOR + longitude);
            String latitude = formatIntAsString((int) (position.getLatitude() * INTEGER_FACTOR));
            writer.println(LATITUDE + NAME_VALUE_SEPARATOR + latitude);

            // TODO write decomposed description
            // Name=
            // Address=11 Veilchenstrasse
            // City=Gladbeck
            // State=DE
            // County=Recklinghausen
            // Zip=47853

//            String description = position.getDescription();
//            int index = description.indexOf(',');
//            String city = index != -1 ? description.substring(0, index) : description;
//            city = trim(city);
//            String address = index != -1 ? description.substring(index + 1) : description;
//            address = trim(address);
//            boolean first = i == startIndex;
//            boolean last = i == endIndex - 1;
//
//            // only store address if there was a comma in the description
//            writer.println(ADDRESS + NAME_VALUE_SEPARATOR + (index != -1 ? address : ""));
//            // otherwise store description als city
//            writer.println(CITY + NAME_VALUE_SEPARATOR + city);
//            if (first || last /* TODO || preferences.getBoolean("writeTargets", false)*/)
//                writer.println(SHOW + NAME_VALUE_SEPARATOR + "1"); // Target/Stop target
//            else
                writer.println(SHOW + NAME_VALUE_SEPARATOR + "1"); // Waypoint TODO
            writer.println(SEQUENCE + NAME_VALUE_SEPARATOR + i);
            writer.println(END_STOP);
            writer.println();

            writer.println(START_STOP_OPT + NAME_VALUE_SEPARATOR + STOP + " " + i);
            writer.println("Loaded=1");
            writer.println(END_STOP_OPT);
            writer.println();
        }
    }
}
