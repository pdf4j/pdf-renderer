package com.sun.pdfview.font.cid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.pdfview.PDFObject;
import com.sun.pdfview.font.PDFCMap;

/*****************************************************************************
 * Parses a CMAP and builds a lookup table to map CMAP based codes to unicode.
 * This is not a fully functional CMAP parser but a stripped down parser
 * that should be able to parse some limited variants of CMAPs that are
 * used for the ToUnicode mapping found for some Type0 fonts.
 *
 * @author  Bernd Rosstauscher
 * @since 03.08.2011
 ****************************************************************************/

public class ToUnicodeMap extends PDFCMap {
	
	/*****************************************************************************
	 * Small helper class to define a range.
	 ****************************************************************************/

	private static class CharRangeMapping {
		char srcStart;
		char srcEnd;
		char destStart;
		
		CharRangeMapping(char srcStart, char srcEnd, char destStart) {
			this.srcStart = srcStart;
			this.srcEnd = srcEnd;
			this.destStart = destStart;
		}
		
		boolean contains(char c) {
			return this.srcStart <= c 
								&& c <= this.srcEnd;
		}
		
		char map(char src) {
			return (char) (this.destStart + (src-this.srcStart));
		}
		
	}
	
	private Map<Character, Character> singleCharMappings;
	private List<CharRangeMapping> charRangeMappings;

	/*************************************************************************
	 * Constructor
	 * @param map 
	 * @throws IOException 
	 ************************************************************************/
	
	public ToUnicodeMap(PDFObject map) throws IOException {
		super();
		this.singleCharMappings = new HashMap<Character, Character>();
		this.charRangeMappings = new ArrayList<CharRangeMapping>();
		parseMappings(map);
	}
	
	/*************************************************************************
	 * @param map
	 * @throws IOException 
	 ************************************************************************/
	
	private void parseMappings(PDFObject map) throws IOException {
		try {
			StringReader reader = new StringReader(new String(map.getStream(), "ASCII"));
			BufferedReader bf = new BufferedReader(reader);
			String line = bf.readLine();
			while (line != null) {
				if (line.contains("beginbfchar")) {
					parseSingleCharMappingSection(bf);
				}
				if (line.contains("beginbfrange")) {
					parseCharRangeMappingSection(bf);
				}
				line = bf.readLine();
			}
		} catch (UnsupportedEncodingException e) {
			throw new IOException(e);
		} 
	}

	/*************************************************************************
	 * @param bf
	 * @throws IOException 
	 ************************************************************************/
	
	private void parseCharRangeMappingSection(BufferedReader bf) throws IOException {
		String line = bf.readLine();
		while (line != null) {
			if (line.contains("endbfrange")) {
				break;
			}
			parseRangeLine(line);
			line = bf.readLine();
		}
	}

	/*************************************************************************
	 * @param line
	 * @return
	 ************************************************************************/
	
	private void parseRangeLine(String line) {
		String[] mapping = line.split(" ");
		if (mapping.length == 3) {
			Character srcStart = parseChar(mapping[0]);
			Character srcEnd = parseChar(mapping[1]);
			Character destStart = parseChar(mapping[2]);
			this.charRangeMappings.add(new CharRangeMapping(srcStart, srcEnd, destStart));
		}
	}

	/*************************************************************************
	 * @param bf
	 * @throws IOException 
	 ************************************************************************/
	
	private void parseSingleCharMappingSection(BufferedReader bf) throws IOException {
		String line = bf.readLine();
		while (line != null) {
			if (line.contains("endbfchar")) {
				break;
			}
			parseSingleCharMappingLine(line);
			line = bf.readLine();
		}
	}

	/*************************************************************************
	 * @param line
	 * @return
	 ************************************************************************/
	
	private void parseSingleCharMappingLine(String line) {
		String[] mapping = line.split(" ");
		if (mapping.length == 2 
				&& mapping[0].startsWith("<")
				&& mapping[1].startsWith("<")) {
			this.singleCharMappings.put(parseChar(mapping[0]), parseChar(mapping[1]));
		}
	}

	/*************************************************************************
	 * Parse a string of the format <0F3A> to a char.
	 * @param charDef
	 * @return
	 ************************************************************************/
	
	private Character parseChar(String charDef) {
		if (charDef.startsWith("<")) {
			charDef = charDef.substring(1);
		}
		if (charDef.endsWith(">")) {
			charDef = charDef.substring(0, charDef.length()-1);
		}
		long result = Long.decode("0x"+charDef);
		return (char) result;
	}

	/*************************************************************************
	 * map
	 * @see com.sun.pdfview.font.PDFCMap#map(char)
	 ************************************************************************/
	@Override
	public char map(char src) {
		Character mappedChar = this.singleCharMappings.get(src);
		if (mappedChar == null) {
			mappedChar = lookupInRanges(src);
		}
		if (mappedChar == null) {
			//TODO BROS 03.08.2011 Is this a good fallback?
			mappedChar = src;
		}
		return mappedChar;
	}

	/*************************************************************************
	 * @param src
	 * @return
	 ************************************************************************/
	
	private Character lookupInRanges(char src) {
		Character mappedChar = null;
		for (CharRangeMapping rangeMapping : this.charRangeMappings) {
			if (rangeMapping.contains(src)) {
				mappedChar = rangeMapping.map(src);
				break;
			}
		}
		return mappedChar;
	}

}