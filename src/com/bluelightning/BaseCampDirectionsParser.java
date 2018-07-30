/**
 * 
 */
package com.bluelightning;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;

/**
 * @author lintondf
 *
 */
public class BaseCampDirectionsParser {
	
	public static class ParsedDirections {
		public enum Kind {ERROR, START, ARRIVE, TURN};
		public Kind kind;
		public String target;
		public String road;
		public String exit;
		public enum Direction {NONE, LEFT, RIGHT, NORTH, SOUTH, EAST, WEST};
		public Direction direction;
		
		public ParsedDirections() {
			kind = Kind.ERROR;
			this.direction = Direction.NONE;
		}
		
		public ParsedDirections( Kind kind ) {
			this.kind = kind;
			this.direction = Direction.NONE;
		}
		
		public void setDirection( String word ) {
			switch (word) {
			case "LEFT":
				direction = Direction.LEFT;
				break;
			case "RIGHT":
				direction = Direction.RIGHT;
				break;
			case "N":
			case "NORTH":
				direction = Direction.NORTH;
				break;
			case "S":
			case "SOUTH":
				direction = Direction.SOUTH;
				break;
			case "E":
			case "EAST":
				direction = Direction.EAST;
				break;
			case "W":
			case "WEST":
				direction = Direction.WEST;
				break;
			default:
				direction = Direction.NONE;
			}
		}
		
		public String toString() {
			return String.format("%s/%s/%s/%s/%s", kind.toString(), road, exit, direction.toString(), target);
		}
	}
	
	static protected class ParserBase {
		String keyword;
		
		public ParserBase( String keyword) {
			this.keyword = keyword;
		}
		
		public String getKeyword() {
			return keyword;
		}
		
		public ParsedDirections parse(String[] words) {
			System.err.println("NIY: " + words[0]);
			return new ParsedDirections();
		}
	}
	
	protected static String join( String[] words, int start, int last ) {
		StringBuffer sb = new StringBuffer();
		for (int i = start; i < last && i < words.length; i++) {
			sb.append(words[i]);
			sb.append(' ');
		}
		return sb.toString().trim();
	}
	
	static protected class ParseArrive extends ParserBase {
		public ParseArrive() {
			super("ARRIVE");
		}
		
		@Override
		public ParsedDirections parse(String[] words) {
			ParsedDirections parsed = new ParsedDirections(ParsedDirections.Kind.ARRIVE);
			if (words.length < 3 || ! words[1].equals("AT")) {
				return new ParsedDirections(ParsedDirections.Kind.ERROR);
			}
			parsed.target = join( words, 2, words.length);
			return parsed;
		}
	}

	static protected class ParseBear extends ParserBase {
		public ParseBear() {
			super("BEAR");
		}
		
		@Override
		public ParsedDirections parse(String[] words) {
			ParsedDirections parsed = new ParsedDirections(ParsedDirections.Kind.TURN);
			if (words.length < 4) {
				return new ParsedDirections();
			}
			parsed.setDirection(words[1]);
			if (! words[2].equals("ONTO")) {
				return new ParsedDirections();
			}
			parsed.road = join( words, 3, words.length);
			return parsed;
		}
	}
	
	static protected int findKeyword( String[] words, int iStart, String keyword) {
		for (int i = iStart; i < words.length; i++) {
			if (words[i].equals(keyword))
				return i;
		}
		return -1;
	}

	static protected class ParseContinue extends ParserBase {
		public ParseContinue() {
			super("CONTINUE");
		}

		@Override
		public ParsedDirections parse(String[] words) {
			ParsedDirections parsed = new ParsedDirections(ParsedDirections.Kind.TURN);
			if (words.length < 3) {
				return new ParsedDirections();
			}
			switch (words[1]) {
			case "ON":
				int i = findKeyword( words, 2, "TOWARDS");
				if (i >= 0) {
					parsed.road = join( words, 2, i );
					parsed.target = join( words, i+1, words.length);
				} else {
					parsed.road = join(words, 2, words.length );
				}
				break;
			case "TOWARDS":
				parsed.target = join( words, 2, words.length);
				break;
			default:
				return new ParsedDirections();
			}
			return parsed;
		}
	}
	

	static protected class ParseExit extends ParserBase {
		public ParseExit() {
			super("EXIT");
		}

		@Override
		public ParsedDirections parse(String[] words) {
			ParsedDirections parsed = new ParsedDirections(ParsedDirections.Kind.TURN);
			if (words.length < 3) {
				return new ParsedDirections();
			}
			parsed.setDirection(words[1]);
			if (! words[2].equals("ONTO")) {
				return new ParsedDirections();
			}
			// ONTO <road> | ONTO RAMP ONTO <road>
			int index = findKeyword( words, 3, "ONTO");
			if (index < 0) {
				parsed.road = join( words, 3, words.length);
			} else {
				parsed.road = join( words, index+1, words.length );
			}
			return parsed;
		}
	}

	static protected class ParseGet extends ParserBase {
		public ParseGet() {
			super("GET");
		}

		@Override
		public ParsedDirections parse(String[] words) {
			ParsedDirections parsed = new ParsedDirections(ParsedDirections.Kind.TURN);
			if (words.length < 3 || ! words[1].equals("ON")) {
				return new ParsedDirections(ParsedDirections.Kind.ERROR);
			}
			int i = findKeyword(words, 2, "AND");
			if (i < 0) {
				return new ParsedDirections(ParsedDirections.Kind.ERROR);				
			}
			parsed.road = join( words, 2, i);
			if ( (i+2) >= words.length || !words[i+1].equals("DRIVE")) {
				return new ParsedDirections(ParsedDirections.Kind.ERROR);
			}
			parsed.setDirection( words[i+2] );
			return parsed;
		}
	}

	static protected class ParseKeep extends ParserBase {
		public ParseKeep() {
			super("KEEP");
		}

		@Override
		public ParsedDirections parse(String[] words) {
			ParsedDirections parsed = new ParsedDirections(ParsedDirections.Kind.TURN);
			if (words.length < 3) {
				return new ParsedDirections();
			}
			parsed.setDirection(words[1]);
			if (! words[2].equals("ONTO")) {
				return new ParsedDirections();
			}
			// KEEP <direction> ONTO <road> {TOWARDS <target>}
			int index = findKeyword( words, 3, "TOWARDS");
			if (index < 0) {
				parsed.road = join( words, 3, words.length);
			} else {
				parsed.road = join( words, 3, index );
				parsed.target = join( words, index+1, words.length);
			}
			return parsed;
		}
	}

	static protected class ParseStart extends ParserBase {
		public ParseStart() {
			super("START");
		}
		
		@Override
		public ParsedDirections parse(String[] words) {
			ParsedDirections parsed = new ParsedDirections(ParsedDirections.Kind.START);
			if (words.length < 3 || ! words[1].equals("AT")) {
				return new ParsedDirections(ParsedDirections.Kind.ERROR);
			}
			parsed.target = join( words, 2, words.length);
			return parsed;
		}
	}

	static protected class ParseTake extends ParserBase {
		public ParseTake() {
			super("TAKE");
		}

		@Override
		public ParsedDirections parse(String[] words) {
			ParsedDirections parsed = new ParsedDirections(ParsedDirections.Kind.TURN);
			if (words.length < 3) {
				return new ParsedDirections();
			}
			switch (words[1]) {
			case "EXIT": // TAKE EXIT <exit> TO THE <direction> ONTO <road> TOWARDS <target> | TAKE EXIT <exit> ONTO <road>
				int iTo = findKeyword( words, 2, "TO");
				int jTowards = findKeyword( words, 2, "TOWARDS");
				int iOnto = findKeyword( words, 2, "ONTO");
				if (iTo > 0) { // TAKE EXIT <exit> TO THE <direction> ONTO <road> TOWARDS <target>
					parsed.exit = join( words, 2, iTo );
					if ((iTo+2) > words.length || !words[iTo+1].equals("THE")) {
						return new ParsedDirections();
					}
					parsed.setDirection( words[iTo+2] );
					if (iOnto > 0) {
						if (jTowards < 0) {
							parsed.road = join( words, iOnto+1, words.length);
						} else {
							parsed.road = join( words, iOnto+1, jTowards);
							parsed.target = join( words, jTowards+1, words.length );
						}
					} else if (jTowards > 0) {
						parsed.target = join(words, jTowards+1, words.length);
					}
				} else if (iOnto > 0) { // TAKE EXIT <exit> ONTO <road> {TOWARDS <target}>
					parsed.exit = join( words, 2, iOnto );
					if (jTowards < 0) {
						parsed.road = join( words, iOnto+1, words.length);
					} else {
						parsed.road = join( words, iOnto+1, jTowards );
						parsed.target = join( words, jTowards+1, words.length );
					}
				}
				break;
			case "RAMP": // TAKE RAMP TO THE <direction> TOWARDS <target>
				int iTowards = findKeyword( words, 2, "TOWARDS");
				if (words.length < 5) {
					return new ParsedDirections();
				}
				if (! (words[2].equals("TO") && words[3].equals("THE")) ) {
					return new ParsedDirections();
				}
				parsed.setDirection( words[4] );
				if (iTowards > 0) {
					parsed.target = join( words, iTowards+1, words.length);
				}
				break;
			case "THE":  // TAKE THE <road> RAMP TO THE <direction>
				int iRamp = findKeyword( words, 2, "RAMP");
				if (iRamp < 0) {
					return new ParsedDirections();					
				}
				parsed.road = join( words, 2, iRamp+1); // include RAMP
				if (iRamp+3 > words.length || ! words[iRamp+1].equals("TO") || ! words[iRamp+2].equals("THE")) {
					return new ParsedDirections();										
				}
				parsed.setDirection( words[iRamp+3 ]);
				break;
			default:
				return new ParsedDirections();
			}
			return parsed;
		}
	}

	static protected class ParseTurn extends ParserBase {
		public ParseTurn() {
			super("TURN");
		}

		@Override
		public ParsedDirections parse(String[] words) {
			ParsedDirections parsed = new ParsedDirections(ParsedDirections.Kind.TURN);
			if (words.length < 3) {
				return new ParsedDirections();
			}
			parsed.setDirection(words[1]);
			if (! words[2].equals("ONTO")) {
				return new ParsedDirections();
			}
			parsed.road = join( words, 3, words.length);
			return parsed;
		}
	}
	
	protected HashMap<String, ParserBase> map = new HashMap<>();
	
	public BaseCampDirectionsParser() {
		ParserBase[] parsers = {
				new ParseArrive(),
				new ParseBear(),
				new ParseContinue(),
				new ParseExit(),
				new ParseGet(),
				new ParseKeep(),
				new ParseStart(),
				new ParseTake(),
				new ParseTurn(),
			};
		
		for (ParserBase parser : parsers) {
			map.put(parser.getKeyword(),  parser);
		}
	}
	
	public ParsedDirections parse( String directions) {
		String[] words = directions.trim().toUpperCase().split(" ");
		if (map.containsKey(words[0])) {
			ParsedDirections parsed = map.get(words[0]).parse(words);
			return parsed;
		} else {
			return null;
		}		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File src = new File("/Users/lintondf/GIT/Robauto/turns.txt");
		BaseCampDirectionsParser parser = new BaseCampDirectionsParser();
		try {
			List<String> lines = IOUtils.readLines(new FileReader(src));
			for (String line : lines) {
				System.out.println(line);
				line = line.trim().toUpperCase();
				if (! line.isEmpty()) {
					String[] words = line.split(" ");
					if (words.length > 0) {
						ParsedDirections  parsed = parser.parse( line );
						if (parsed != null && parsed.kind != ParsedDirections.Kind.ERROR ) {
							System.out.println( parsed );
						} else {
							System.err.println( line );
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
