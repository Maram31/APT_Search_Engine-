import org.jsoup.Jsoup;

import com.mongodb.*;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.WriteModel;
import com.mongodb.client.result.*;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Set;
import java.util.Arrays;
import java.util.Hashtable;
import java.io.IOException;
import java.util.ArrayList;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;



public class Indexer {
	public static void indexer(Set<String> visitedUrls) throws IOException {
		
		Preprocessor.prepareStopWords();
		
		String[] allUrls = new String[visitedUrls.size()];
		visitedUrls.toArray(allUrls);
		
		MongoClient client = MongoClients.create("mongodb://localhost:27017");
		MongoDatabase db = client.getDatabase("search_engine");
		MongoCollection<Document> collection = db.getCollection("indexer");
		
		Hashtable<String, Integer> wordsPerDocDict = new Hashtable<String, Integer>();
	
		int countDocuments= allUrls.length;
		System.out.println(countDocuments);
		int u=0;
		for(String url : allUrls) {
		   
		   wordsPerDocDict.clear();
		   
		   org.jsoup.nodes.Document doc = Jsoup.connect(url).get();
		   String allWords =  doc.text();
//		   System.out.println(allWords);
		   
		   int val = 0;
		   String words[] = allWords.split(" ");
		   double lengthDocument = words.length;
	
		   System.out.println("Url no. "+u + " : "+ url + " #words: " + lengthDocument );
		   u = u+1; 
		   
		   // Split words and add them in a hashtable with its frequency
		   for(String token : words) {
			   if(token!="") {
				   String processedToken = Preprocessor.preprocess(token);//porterStemmer.stemWord(token);
				   if(!wordsPerDocDict.containsKey(processedToken))
					   wordsPerDocDict.put(processedToken,1);
				   else {
					   val = wordsPerDocDict.get(processedToken);
					   wordsPerDocDict.replace(processedToken, val+1);
				   }  
			   }
		    }
	   

		   List<WriteModel<Document>> writes = new ArrayList<WriteModel<Document>>();
		   
		   // Put all words in hashtable in db
		   Set<String> setOfWords = wordsPerDocDict.keySet();
	       for(String key : setOfWords) {
	    	   Document query = new Document("word",key);
	    	   long count = collection.countDocuments(query);
	    	   if(count== 0) {
	    		    // First time to add word
	    		    Document entry = new Document().append("word",key).append("IDF", Math.log((double)countDocuments)/ Math.log(2));
	    		  	List<Document> urls = new ArrayList<Document>();
	    		  	urls.add(new Document("url",url).append("tf", (double)wordsPerDocDict.get(key)/lengthDocument));
	    		  	entry.append("urls", urls);
	    		  	writes.add(new InsertOneModel<Document>(entry));
	    	    } else {	
	    		    // word already exists -> ADD url of the other document and its corresponding TF & UPDATE IDF of the word 
	    		    @SuppressWarnings("unchecked")
					List<Document> oldUrls = (List<Document>) collection.find(Filters.eq("word", key)).first().get("urls");
	    		    oldUrls.add(new Document("url",url).append("tf", (double)wordsPerDocDict.get(key)/lengthDocument));
	    		    double size = oldUrls.size();
	    		    writes.add(
	    		    	    new UpdateOneModel<Document>(
	    		    	        new Document("word", key), // filter
	    		    	        new Document("$set", new Document().append("urls", oldUrls).append("IDF", Math.log((double)(countDocuments/size))/ Math.log(2)))) // update
	    		    	);	    		
	    	    }	   
	       }
	       BulkWriteResult bulkWriteResult = collection.bulkWrite(writes);
	   }	
		client.close();
    }
}	   