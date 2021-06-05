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
    protected static Set<String> visitedUrls = new HashSet<String>(); 
    protected static Set<String> compactString = new HashSet<String>();
    final static int stoppingCriteria = 5000;
    static boolean crawlerFinished = false; 
    static int isInterrupted = 0;
    
    /**
     * @param args the command line arguments
     * @throws IOException 
     */
    
    public static void main(String[] args) throws InterruptedException, IOException {        
        CrawlerController controller = new CrawlerController();
        checkPreviousState();
        
        if(isInterrupted == 1) {
            System.out.println("Loading previous state...");
            loadState();
        }
        else {
            readSeeds(seedsList);
        }
        controller.run();
        
        System.out.println("Crawling finished");
          
        Indexer.indexer(visitedUrls);
    }
    
    
    
    public static void readSeeds(Queue<String> seedsList) {
        try {
            File mySeeds = new File("./seeds.txt");
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
    
    
    public static void saveState(int state) {  
        //Saving seeds list
        if(state == 1) {
            File seedListFile = new File("./seedsList.txt");     
            try {
                //Converting Queue to Array
                String[] array = null;
                array = seedsList.toArray(new String[seedsList.size()]);
                String newLine = System.getProperty("line.separator");

                //Write Content
                FileWriter writer = new FileWriter(seedListFile);
                for(int i = 0; i < array.length; i++) {
                    writer.write(array[i]+ newLine);               
                }        
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }    

            //Saving visited urls list
            File visitedUrlsFile = new File("./visitedUrls.txt");     
            try {
                //Converting Set to Array
                String[] array = null;
                array = visitedUrls.toArray(new String[visitedUrls.size()]);
                String newLine = System.getProperty("line.separator");

                //Write Content
                FileWriter writer = new FileWriter(visitedUrlsFile);
                for(int i = 0; i < array.length; i++) {
                    writer.write(array[i]+ newLine);               
                }        
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }  
            
            //Saving compact string list
            File compactStringFile = new File("./compactString.txt");     
            try {
                //Converting Set to Array
                String[] array = null;
                array = compactString.toArray(new String[compactString.size()]);
                String newLine = System.getProperty("line.separator");

                //Write Content
                FileWriter writer = new FileWriter(compactStringFile);
                for(int i = 0; i < array.length; i++) {
                    writer.write(array[i]+ newLine);               
                }        
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }  
            
            //Saving state                 
            File isInterruptedFile = new File("./state.txt");
            try {    
                FileWriter writer = new FileWriter(isInterruptedFile);
                writer.write("1");               
                writer.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        
        if(state == 0) {
            File isInterruptedFile = new File("./state.txt");
            try {    
                FileWriter writer = new FileWriter(isInterruptedFile);
                writer.write("0");               
                writer.close();
            } catch(IOException e) {
                e.printStackTrace();
            }        
        }
    }

    
    public static void loadState() {  
        try {
            //Loading seeds list
            File seedsListFile = new File("./seedsList.txt");
            if(seedsListFile.exists()) {
                Scanner myReader = new Scanner(seedsListFile);

                while (myReader.hasNextLine()) {
                    String data = myReader.nextLine();
                    seedsList.add(data);
                }
                myReader.close(); 
            }
        } catch(FileNotFoundException e) {
            System.out.println("An error occurred while loading previous state for seeds from file!");
            e.printStackTrace();
        }   

        
        try {
            //Loading visited urls
            File visitedUrlsFile = new File("./visitedUrls.txt");
            if(visitedUrlsFile.exists()) {
                Scanner myReader = new Scanner(visitedUrlsFile);

                while (myReader.hasNextLine()) {
                    String data = myReader.nextLine();
                    visitedUrls.add(data);
                }
                myReader.close(); }
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred while loading previous state for visited Urls from file!");
            e.printStackTrace(); 
        } 
        
        try {
            //Loading compact strings
            File compactStringFile = new File("./compactString.txt");
            if(compactStringFile.exists()) {
                Scanner myReader = new Scanner(compactStringFile);

                while (myReader.hasNextLine()) {
                    String data = myReader.nextLine();
                    compactString.add(data);
                }
                myReader.close(); }
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred while loading previous state for compact strings from file!");
            e.printStackTrace(); 
        }             
    }

    
    public static void checkPreviousState()  {  
        try {
            File isInterruptedFile = new File("./state.txt");
            if(isInterruptedFile.exists()) {
                Scanner myReader = new Scanner(isInterruptedFile);
                String text = myReader.nextLine();
                System.out.println(text);
                isInterrupted = Integer.parseInt(text);
            }
        } catch(FileNotFoundException e) {
            isInterrupted = 0;       
        }  
    }
    
}

