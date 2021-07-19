package com.hunt.app.stopwords;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

public class Stopwords {
	
	public static Set<String> stopWordSet = new HashSet<String>();
	public static Set<String> stemmedStopWordSet = null;
	
	public static boolean isStopword(String word) {
		if(word.length() < 2) return true;
		if(word.charAt(0) >= '0' && word.charAt(0) <= '9') return true; //remove numbers, "25th", etc
		if(stopWordSet.contains(word)) return true;
		else return false;
	}
	
	public static boolean isStemmedStopword(String word) {
		if(word.length() < 2) return true;
		if(word.charAt(0) >= '0' && word.charAt(0) <= '9') return true; //remove numbers, "25th", etc
		//String stemmed = stemString(word);
		if(stopWordSet.contains(word)) return true;
		//if(stemmedStopWordSet.contains(word)) return true;
		//if(stopWordSet.contains(word)) return true;
		//if(stemmedStopWordSet.contains(word)) return true;
		else return false;
	}
	
	public static String removeStopWords(String string, String path) {
		String result = "";
		String[] words = string.split("\\s+");
		
		try{
			// stopWordSet = Files.lines(Paths.get(path)).collect(Collectors.toSet());
			Stream<String> stream = Files.lines(Paths.get(path));
			Iterator<String> iterator = stream.iterator();
			while(iterator.hasNext()) {
				stopWordSet.add(iterator.next());
			}
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		stemmedStopWordSet = stemStringSet(stopWordSet);		
		for(String word : words) {
			if(word.isEmpty()) continue;
			if(isStopword(string)) continue; //remove stopwords
			result += (word+" ");
		}
		return result;
	}
	
	public static String removeStemmedStopWords(String string) {
		String result = "";
		String[] words = string.split("\\s+");
		for(String word : words) {
			if(word.isEmpty()) continue;
			if(isStemmedStopword(word)) continue;
			if(word.charAt(0) >= '0' && word.charAt(0) <= '9') continue; //remove numbers, "25th", etc
			result += (word+" ");
		}
		return result;
	}
	
	public static String stemString(String string) {
		return new Stemmer().stem(string);
	}
	
	public static Set<String> stemStringSet(Set<String> stringSet) {
		Stemmer stemmer = new Stemmer();
		Set<String> results = new HashSet<String>();
		for(String string : stringSet) {
			results.add(stemmer.stem(string));
		}
		return results;
	}
	public static String removeNoicedata(String document){
		try{
		document = document.replaceAll("\u00A0", " ");
		document = document.replaceAll("[^\\x20-\\x7E]", " ");
		document = document.replaceAll("http.*?\\s", "");
		document = document.replaceAll("Http.*?\\s", "");
		document = document.replaceAll("WWW.*?\\s", "");
		document = document.replaceAll("www.*?\\s", " ");
		/*document = document.replaceAll("-", " ");
		document = document.replaceAll("—", " ");
		document = document.replaceAll("— ", "");*/
		document = document.replaceAll("…………………………", "");
		//document = document.replaceAll("([^.@\\s]+)(\\.[^.@\\s]+)*@([^.@\\s]+\\.)+([^.@\\s]+)", " ");
		document = document.replaceAll("\\p{Punct}", " ");
		document = document.replaceAll("\\d", " ");
		document = document.replaceAll("“", "");
		document = document.replaceAll("”", "");
		document = document.replaceAll("”", "");
		document = document.replaceAll("’", "");
		//document = document.replaceAll("(?m)^\\s*$[\n\r]{1,}", " ");
		document = document.replaceAll(" +", " ").toLowerCase();
		}catch(Exception e){
			e.printStackTrace();
		}
		return document;
	}
}

