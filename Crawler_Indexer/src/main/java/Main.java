/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Maram
 */

import java.util.*;

import java.net.*;
import java.io.*;
import org.jsoup.nodes.Document;

public class Main {
    protected static Queue<String> seedsList = new LinkedList<>();
    protected static Queue<Document> HTMLdocuments = new LinkedList<>();
    //protected static Queue<String> collectedUrls = new LinkedList<>();          //for indexer

    protected static Set<String> restrictedSites = new HashSet<>();
    protected static Set<String> visitedUrls = new HashSet<String>();
    //protected static List<String> visitedUrls = new ArrayList<String>();
    
    protected static Set<String> compactString = new HashSet<String>();
    
    protected static Integer visitedUrlsCount = 0;

    final static int stoppingCriteria = 5000;
    static boolean crawlerFinished = false;
    /**
     * @param args the command line arguments
     * @throws IOException 
     */
    public static void main(String[] args) throws InterruptedException, IOException {
        Thread crawling = new Thread(new CrawlerController());
      
        // thread for indexer
        readSeeds(seedsList);
       
        crawling.start();
        // indexer starts
              
        crawling.join();
        // indexer joins
        System.out.println("Crawling finished");
        
        Indexer.indexer(visitedUrls);

        if (!CrawlerController.isInterrupted) {
        }

        //for (String item: seedsList) {
            //System.out.println(item);
        //}
    }
    
    
    
    public static void readSeeds(Queue<String> seedsList) {
        try {
            File mySeeds = new File("seeds.txt");
            Scanner myReader = new Scanner(mySeeds);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                seedsList.add(data);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred while reading seeds from file!");
            e.printStackTrace();
        }
    } 

}
