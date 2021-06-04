import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import ca.rmen.porterstemmer.PorterStemmer;

public class Preprocessor {
	
	static String [] stopWords = new String[440];
	
	public static void prepareStopWords(String filename) throws FileNotFoundException {
			File file =new File(filename);
			Scanner sc = new Scanner(file);
			int i=0;
		    while (sc.hasNextLine()) {
		    	stopWords[i] = sc.nextLine();
		    	i++;
		    }
			sc.close();	
	}
	
	static Boolean isStop(String word) {
		for(int j= 0 ; j<stopWords.length ;j++) {
			if( word.equals(stopWords[j])) {
//				System.out.println(word + " " + stopWords[j]);
				return true;
			}
				
	    }
		return false;
	}
	
	public static String preprocess(String str) throws IOException {
		PorterStemmer porterStemmer = new PorterStemmer();
		
		// Lowercase & Remove stop words & Remove Punctuation & Stemming
		str = str.replaceAll(" ","");
		if(!isStop(str)) {
			str = str.toLowerCase();
//			System.out.println(str);

			String afterPunc = str.replaceAll("\\p{Punct}","").replaceAll("[0-9]*",""); 
//		    System.out.println(afterPunc);
		    
		    String afterStemming = porterStemmer.stemWord(afterPunc);
//		    System.out.println(afterStemming);
		    
		    return afterStemming;			
		}
		return ""; 
	}
//	
//	public static void main(String[] args) throws IOException {
//		Preprocessor.prepareStopWords();
//		Preprocessor.preprocess("smartphone");
//	}

}
