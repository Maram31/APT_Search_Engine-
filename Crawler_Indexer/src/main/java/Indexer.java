import org.jsoup.Jsoup;

//import org.bson.Document;
//
//import com.mongodb.MongoClient;
//import com.mongodb.MongoClientURI;
//import com.mongodb.client.MongoClients;
//import com.mongodb.client.MongoCollection;
//import com.mongodb.client.MongoDatabase;
//import com.mongodb.client.model.Filters;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Hashtable;
//import java.util.List;
//import java.util.Set;

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
		
		Preprocessor.prepareStopWords("stop_words_array.txt");
		
		String[] allUrls = new String[visitedUrls.size()];
		visitedUrls.toArray(allUrls);
//	    String [] allUrls = {"https://www.w3schools.com/js/","https://docs.mongodb.com/manual/reference/operator/query/type/","https://www.tutorialspoint.com/javascript/index.htm"};
		
		//Connect to db

//		MongoClient mongoClient = (MongoClient) MongoClients.create(
//		    "mongodb://taskmanager:taskmanager@cluster0-shard-00-00.kbkfp.mongodb.net:27017,cluster0-shard-00-01.kbkfp.mongodb.net:27017,cluster0-shard-00-02.kbkfp.mongodb.net:27017/search_engine?ssl=true&replicaSet=Cluster0-shard-0&authSource=admin&retryWrites=true&w=majority");
//		MongoDatabase db = mongoClient.getDatabase("search_engine");
		
//		MongoClientURI uri = new  MongoClientURI("mongodb+srv://taskmanager:taskmanager@cluster0.kbkfp.mongodb.net/search_engine?retryWrites=true&w=majority");
//		
//		MongoClientURI uri = new  MongoClientURI("mongodb+srv://taskmanager:taskmanager@cluster0.kbkfp.mongodb.net/search_engine?retryWrites=true&w=majority");//&readPreference=secondary&ssl=true");
//		MongoClient mongoClient = new MongoClient(uri);
//		MongoDatabase db=mongoClient.getDatabase("search_engine");
		
//		MongoClient client = MongoClients.create("mongodb+srv://taskmanager:taskmanager@cluster0.kbkfp.mongodb.net/search_engine?retryWrites=true&w=majority&ssl=false");
		MongoClient client = MongoClients.create("mongodb://localhost:27017");
//		MongoClient client = MongoClients.create("mongodb://localhost:27017/search_engine?socketTimeout=360000");

//		ConnectionString connString = new ConnectionString(
//				"mongodb://localhost:27017"
//			);
//		MongoClientSettings settings = MongoClientSettings.builder()
//		    .applyConnectionString(connString)
//		    .retryWrites(true)
//		    .socketTimeout(30000)
//		    .build();
//		MongoClient client = MongoClients.create(settings);
//		
//		MongoClientOptions.Builder options = MongoClientOptions.builder();
//		options.socketKeepAlive(true);
//		MongoClient mongoClient = new MongoClient("mongodb://localhost:27017", options.build());
//		
		MongoDatabase db = client.getDatabase("search_engine");
		MongoCollection<Document> collection = db.getCollection("indexer");
		
		
//		MongoClient mongoClient = new MongoClient("localhost",27017);
//		MongoDatabase db=mongoClient.getDatabase("search_engine");
//		MongoCollection<Document> collection = db.getCollection("indexer");	
//		
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
	   
//		   System.out.println(wordsPerDocDict);
		   List<WriteModel<Document>> writes = new ArrayList<WriteModel<Document>>();
//		   List<Document> newDocs = new ArrayList<Document>();
//		   
		   
		   // Put all words in hashtable in db
		   Set<String> setOfWords = wordsPerDocDict.keySet();
	       for(String key : setOfWords) {
	    	   Document query = new Document("word",key);
	    	   long count = collection.countDocuments(query);
//	    	   long count =0;
	    	   if(count== 0) {
	    		    // First time to add word
	    		    Document entry = new Document().append("word",key).append("IDF", Math.log((double)countDocuments)/ Math.log(2));
	    		  	List<Document> urls = new ArrayList<Document>();
	    		  	urls.add(new Document("url",url).append("tf", (double)wordsPerDocDict.get(key)/lengthDocument));
//	    		  	System.out.println(urls.size());
	    		  	entry.append("urls", urls);
//	    	   		Document urls = new Document(url2,wordsPerDocDict.get(key));
//	    	   		entry.put("urls",urls);
//	    	   		db.getCollection("indexer").insertOne(entry);	
	    		  	writes.add(new InsertOneModel<Document>(entry));
//	    		  	newDocs.add(entry);
	    	    } else {	
	    		    // word already exists -> ADD url of the other document and its corresponding TF & UPDATE IDF of the word 
//	    		    double oldIDF = collection.find(Filters.eq("word", key)).first().getDouble("IDF");
	    		    @SuppressWarnings("unchecked")
					List<Document> oldUrls = (List<Document>) collection.find(Filters.eq("word", key)).first().get("urls");
	    		    oldUrls.add(new Document("url",url).append("tf", (double)wordsPerDocDict.get(key)/lengthDocument));
	    		    double size = oldUrls.size();
//	    		    db.getCollection("indexer").updateOne(Filters.eq("word", key), new Document("$set", new Document("urls."+url, wordsPerDocDict.get(key)).append("IDF", (double)(countDocuments/(countDocuments/oldIDF+1)))));
//	    		    db.getCollection("indexer").updateOne(Filters.eq("word", key), new Document("$set", new Document().append("urls", oldUrls).append("IDF", Math.log((double)(countDocuments/size)))));
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
  
	    
//	    Element body = doc.body();
//	    Elements paragraphs = body.getElementsByTag("p");
//	    
//	    String stopWords = "I|its|with|but|a|and|be|if|in|it|of|on|or|so|the|they|there|this|which|why";
	    
//	    for (Element paragraph : paragraphs) {
//	    System.out.println(paragraph.text());
////	       System.out.println(paragraph.text().length());
////	       Remove stopwords from string before dividing them into the array
//	       String afterStopWords1 = paragraph.text().replaceAll(" (" + stopWords +  ") ", " ");
//	       String afterStopWords = afterStopWords1.replaceAll(" (" + stopWords +  ") ", " ");
//	       System.out.println(afterStopWords);
////	       System.out.println(afterStopWords.length());
////	       String words[] = afterStopWords.split(" ");
////		      for(String token : words) {
////		         System.out.println(token);
////		      }
////		    System.out.println("-----------------------------------------");   
//	    }
//	    
	    

 

