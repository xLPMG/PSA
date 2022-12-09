package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StringComparer {

	public String compare(HashMap<String, String> dataMap, String keyword) {
		String match = null;
		// direct match
		for (String key : dataMap.keySet()) {
			if (key.equals(keyword)) {
				match = key;
				return match;
			}
		}
		// direct match case insensitive
		for (String key : dataMap.keySet()) {
			if (key.equalsIgnoreCase(keyword)) {
				match = key;
				return match;
			}
		}

		// contains keyword
		for (String key : dataMap.keySet()) {
			if (key.toLowerCase().contains(keyword.toLowerCase())) {
				match = key;
				return match;
			}
		}

		// most letter matching
		for (String key : dataMap.keySet()) {
			int i = 0;
			char[] letters = keyword.toLowerCase().replaceAll("\\W", "").toCharArray();
			for (char c : letters) {
				if (key.toLowerCase().indexOf(c) != -1) {
					i++;
				}
			}
			if (i / (float) keyword.length() >= 0.8) {
				match = key;
				return match;
			}
		}

		return match;
	}

	public ArrayList<String> compareAlt(HashMap<String, String> dataMap, String match, String keyword) {
		ArrayList<String> altMatch = new ArrayList<String>();
		
		// direct match
		for (String key : dataMap.keySet()) {
			if (key.equals(keyword)) {
				if (!key.equals(match)) {
					altMatch.add(key);
				}
			}
		}
		// direct match case insensitive
		for (String key : dataMap.keySet()) {
			if (key.equalsIgnoreCase(keyword)) {
				if (!key.equals(match)&&!altMatch.contains(key)) {
					altMatch.add(key);
				}
			}
		}

		// contains keyword
		for (String key : dataMap.keySet()) {
			if (key.toLowerCase().contains(keyword.toLowerCase())) {
				if (!key.equalsIgnoreCase(match)&&!altMatch.contains(key)) {
					altMatch.add(key);
				}
			}
		}

//		// most letter matching
//		for (String key : dataMap.keySet()) {
//			int i = 0;
//			char[] letters = keyword.toLowerCase().replaceAll("\\W", "").toCharArray();
//			for (char c : letters) {
//				if (key.toLowerCase().indexOf(c) != -1) {
//					i++;
//				}
//			}
//			if (i / (float) keyword.length() >= 0.8) {
//				if (!key.equalsIgnoreCase(match) && altMatch == null) {
//					altMatch = key;
//				}
//			}
//		}
		
		return altMatch;
	}

	public String wordGuess(HashMap<String, String> dataMap, String keyword) {
		// use when compare() returns null

		String match = null;
		for (String key : dataMap.keySet()) {
			int i = 0;
			char[] letters = keyword.toLowerCase().replaceAll("\\W", "").toCharArray();
			for (char c : letters) {
				if (key.toLowerCase().indexOf(c) != -1) {
					i++;
				}
			}
			if (i / (float) keyword.length() >= 0.3) {
				if (match == null) {
					match = key;
				}
			}
		}

		return match;
	}
	
	public String findUser(HashMap<String, String> dataMap, String username) {
		String match = null;
		// direct match
		for (String key : dataMap.keySet()) {
			String[] data = dataMap.get(key).split(":");
			if (data[0].equals(username)) {
				match = key;
				return match;
			}
		}
		return match;
	}
	
	public ArrayList<String> findAltUser(HashMap<String, String> dataMap, String match, String username) {
		ArrayList<String> altMatch = new ArrayList<String>();
		// direct match
		for (String key : dataMap.keySet()) {
			if (dataMap.get(key).contains(username)&&!key.equals(match)) {
				altMatch.add(key);
			}
		}
		return altMatch;
	}
}
