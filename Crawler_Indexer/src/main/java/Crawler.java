/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.jsoup.*;
import org.jsoup.nodes.Document;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Maram
 */
public class Crawler implements Runnable {    
    final String UserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"  
            + "(KHTML, like Gecko) Chrome/91.0.4472.77 Safari/537.36";                      //my user agent 

    static RobotReader readRobotFiles = new RobotReader();        //read robot files

    static int stateCounter = 1; 
    
    public Crawler() {
    }
 
    @Override
    public void run() {
        crawl();
    }
    
    public void crawl() {       
        String url;
        
        while(true) {
            if(Main.visitedUrls.size() > Main.stoppingCriteria) {
                System.out.println("Reached stopping criteria!");
                
                break;
            }
            
            String nextUrl;

            nextUrl = getNextUrl();

            if(nextUrl == null) {
                System.out.println(Thread.currentThread().getName() + ": No more links to crawl!");
                break;
            }
              
            url = null;
         
            synchronized(Main.visitedUrls) {
                url = Main.visitedUrls.contains(nextUrl) ? null : nextUrl;
            }
            
            if(url != null) {                                   // if url hasn't been visited before                                
                if(!readRobotFiles.checkRobotFile(url)) {       // if not url is not allowed by robot file then move on to the next url
                    continue;
                }
                
                else {                                          // else go ahead with the url   
                    extractUrls(url);
                }
            }
            
            else {                                              // else url has been visited before
                System.out.println(Thread.currentThread().getName() + ": This url was visited before " + nextUrl);
            }
       
            synchronized(Main.visitedUrls) {
                if(Main.visitedUrls.size() >= 500 * stateCounter && stateCounter != 10) {
                {
                    if(Thread.currentThread().getName().equals("C0")) {
                        System.out.println(Thread.currentThread().getName() + ": Saving state now ");

                        Main.saveState(1);
                        stateCounter++;                  
                    }
                }
            }
            
        }
        }
    }
    
    protected String getNextUrl() {                               // get next url to crawl from seed list   
        synchronized(Main.seedsList) {
            if (Main.seedsList.isEmpty()) {
                System.out.println(Thread.currentThread().getName() + ": Seedlist is empty!");
                return null;                                      // no more seeds in list   
            }
            else {
                return Main.seedsList.remove();                   // return next url to crawl
            }
        }
    }
                 
                 
    public void extractUrls(String url) {
        Document htmlDocument = requestDocument(url);
        if(htmlDocument != null) {
            Elements linksOnPage = htmlDocument.select("a[href]");

            for (Element link : linksOnPage) {  
                if(Main.visitedUrls.size() > Main.stoppingCriteria) {
                    return;
                }
                String extractedURL = link.absUrl("href");
                //System.out.println(extractedURL);

                //Some checks and modifications to avoid duplicate links	
                // Skip empty links.
                if (extractedURL.length() < 1) {
                continue;
                }
                
                // Skip links that are just page anchors.
                if (extractedURL.charAt(0) == '#') {
                continue;
                }
                
                // Skip mailto links.
                if (extractedURL.indexOf("mailto:") != -1) {
                continue;
                }
                
                // Skip JavaScript links.
                if (extractedURL.toLowerCase().indexOf("javascript") != -1) {
                continue;
                }
   
                if (extractedURL.endsWith("/")) {
                    extractedURL = extractedURL.substring(0, extractedURL.length() - 1);
               }

               
                //Ensure that the Content is Html 	
                if(extractedURL.contains("png") || extractedURL.contains("jpg") || extractedURL.contains("svg") || extractedURL.contains("jpeg") || extractedURL.contains("pdf")) {
                    continue;				
                }
                                        
                if(!Main.seedsList.contains(extractedURL) && !Main.visitedUrls.contains(extractedURL)) {
                    System.out.println(Thread.currentThread().getName() + " extracted url: " + extractedURL);
                    Main.seedsList.add(extractedURL);   
                }
            }
        }

    }   

    public Document requestDocument(String url) {
        try {
            Connection connection = Jsoup.connect(url).userAgent(UserAgent).timeout(5000);          
            Document htmlDocument = connection.get();
                       
            String s = htmlDocument.body().text();
            String t = htmlDocument.title();
            
            String compact = createCompactString(s);
            
            if(!connection.response().contentType().contains("text/html"))
            {
                //System.out.println("Retrieved something other than HTML");
                return null;
            }
            if(connection.response().statusCode() == 200) {
                //System.out.println("Recieved webpage at: " + url);

                synchronized(Main.compactString){
                    if(Main.compactString.contains(compact)) {
                       System.out.println("Document at: " + url + " was visited before!");
                       return null; 
                    }
                    else {
                        Main.compactString.add(compact);   
                        //System.out.println("Compact string list: " + Main.compactString);

                    }
                }
                                       
                synchronized(Main.visitedUrls){
                    Main.visitedUrls.add(url);  
                    System.out.println("Visited urls: " + Main.visitedUrls.size());
                }
            }
            
            return htmlDocument;
            
        } catch(IOException e) {
            //System.out.println("Couldn't retrieve the webpage at: " + url);
            return null;
        }
    }
    

    static String createCompactString(String str) {
        String result = "";

       
        boolean v = true;                                   // Traverse the string.
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ' ') {            // If it is space, set v as true.
                v = true;
            }
             
            
            else if (str.charAt(i) != ' ' && v == true) {            // Else check if v is true or not  

                result += (str.charAt(i));                          // If true, copy character in output string and set v as false.
                v = false;
            }
        }
       return result;
    }
  
}