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


    public Crawler() {
    }
    
    protected String getNextUrl() {                               // get next url to crawl from seed list   
        synchronized(Main.seedsList) {
            if (Main.seedsList.isEmpty()) {
                System.out.println(Thread.currentThread().getName() + "Seedlist is empty");
                return null;                                      // no more seeds in list   
            }
            else {
                return Main.seedsList.remove();                   // return next url to crawl
            }
        }
    }
    
    protected boolean addHTMLDocument(Document HTMLDocument) {
        boolean isAdded;
        synchronized(Main.HTMLdocuments) {
            isAdded = Main.HTMLdocuments.add(HTMLDocument);
        }
        if (isAdded) {
            System.out.println(Thread.currentThread().getName() + " added a document");
            return true;
        }
        else
            return false;
    }    
    /*
    protected boolean getHTMLDocument(String url) {
        Document HTMLDocument = null;
        try {
            Connection connection = Jsoup.connect(url).userAgent(UserAgent);
            Connection.Response r = connection.url(url).timeout(10000).execute();
            
            if (!r.contentType().contains("text/html")) {
                return false;
            }
            HTMLDocument = connection.get();
        } catch (HttpStatusException e) {
            Main.restrictedSites.add(url);
            System.out.println("Restricted url: " + url);

            return false;
        } catch (IOException | IllegalArgumentException | NullPointerException ex) {
            return false;
        }
 
        return (addHTMLDocument(HTMLDocument));
    }
    
   */

    @Override
    public void run() {
        //System.out.println("Starting crawling process");
        crawl();
    }
    
    public void crawl() {
        String url;
        //boolean retrieved = false;
        boolean allowedByRobot = false;
        
        while(true) {
            if(Main.visitedUrls.size() > Main.stoppingCriteria) {
                System.out.println("Reached stopping criteria");
                break;
            }
            
            url = null;
            String nextUrl;
            //synchronized(Main.visitedUrls) {
                nextUrl = getNextUrl();
            //}
            //System.out.println(Thread.currentThread().getName() +" will crawl next url: " + nextUrl);

            if(nextUrl == null)
                break;
              
            synchronized(Main.visitedUrls) {
                url = Main.visitedUrls.contains(nextUrl) ? null : nextUrl;
            }
            
            if(url != null) {
                //allowedByRobot = RobotReader.checkRobot(url);
                                
                if(!readRobotFiles.checkRobotFile(url)) {
                    continue;
                }
                
                else {
                    //System.out.println("Allowed:" + RobotReader.allowedUrls);
                    //System.out.println("Disallowed:" + RobotReader.disallowedUrls);
                    extractUrls(url);
                }
                //Main.seedsList.forEach(System.out::println);
            }
        //}
            
        }
    }
                
            
        
    
                
                
                
/*
            while (!CrawlerController.isInterrupted) {
                url = null;
                //retrieved = false;
                //allowedByRobot = false;

                url = getNextUrl();

                if (url != null) {                  // there is a url to visit
                    System.out.println(Thread.currentThread().getName() + " Retrieved the following URL: " + url);
                    extractUrls(url);
                    //allowedByRobot = readRobotFiles.checkRobotFile(url);
                    //System.out.println(allowedByRobot);

                    //if (!allowedByRobot) {
                    //}
                }
                //if (url != null && allowedByRobot) {

                    //retrieved = getHTMLDocument(url);

                //}*/
 
    

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


                //some checks and modifications to avoid duplicate links
          		
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
                
                //if(extractedURL.contains("#")) {
                //    int indexEnd = extractedURL.indexOf("#");
                //    extractedURL = extractedURL.substring(0,indexEnd);    		
                //}

                //if(extractedURL.contains("=")) {
                //    int indexEnd = extractedURL.indexOf("?");
                //    if(indexEnd > 1) {
                //        extractedURL = extractedURL.substring(0, indexEnd);
                //    }  		
                //}
         
                if (extractedURL.endsWith("/")) {
                    extractedURL = extractedURL.substring(0, extractedURL.length() - 1);
               }

                
                //Ensure that the Content is Html 	
                if(extractedURL.contains("png") || extractedURL.contains("jpg") || extractedURL.contains("svg") || extractedURL.contains("jpeg") || extractedURL.contains("pdf")) {
                    continue;				
                }
                                        
                if(!Main.seedsList.contains(extractedURL) && !Main.visitedUrls.contains(extractedURL)) {
                    System.out.println(Thread.currentThread().getName() + " extracted url: " + extractedURL);
                    //Main.visitedUrls.forEach(System.out::println);
                    Main.seedsList.add(extractedURL);   
                    Main.visitedUrls.add(extractedURL);   
                    //.visitedUrls.size();
                    System.out.println(Main.visitedUrls.size());
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
        
            //System.out.println("Document title:" + t);

            //System.out.println("Document content:" + compact);
            
            if(!connection.response().contentType().contains("text/html"))
            {
                System.out.println("Retrieved something other than HTML");
                return null;
            }
            if(connection.response().statusCode() == 200) {
                System.out.println("Recieved webpage at: " + url);

                synchronized(Main.compactString){
                    if(Main.compactString.contains(compact)) {
                       System.out.println("Document at: " + url + " was visited before!");
                       return null; 
                    }
                    else {
                        Main.compactString.add(compact);   
                    }
                }
                                       
                synchronized(Main.visitedUrls){
                    Main.visitedUrls.add(url);  
                    //Main.visitedUrlsCount++;
                    System.out.println(Main.visitedUrls.size());
                }
            }
            
            //System.out.println("Recieved document: " + htmlDocument.body().text());
            return htmlDocument;
            
        } catch(IOException e) {
            System.out.println("Couldn't retrieve the webpage at: " + url);
            return null;
        }
    }
    
    public String createCompactString(String s) {
        int length = s.length();
        
        int readFrom = length/2;
        
        String compactString = "";     //substring containing 1000 characters
        
        if ((length-readFrom) > 1000) 
        {
            compactString = s.substring(readFrom, readFrom + 1000);
        } 
        else
        {
            compactString = s.substring(readFrom, length);
        }

        //System.out.println(compactString); 
        return compactString;
    }
    


    
    private String downloadPage(URL pageUrl) {
        try {
            // Open connection to URL for reading.
            BufferedReader reader = new BufferedReader(new InputStreamReader(pageUrl.openStream()));
            // Read page into buffer.
            String line;
            StringBuffer pageBuffer = new StringBuffer();
            
            while ((line = reader.readLine()) != null) {
            pageBuffer.append(line);
            }
            return pageBuffer.toString();
        } catch (Exception e) {
        }
        return null;
    }


   
}