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
    //protected static Queue<String> HTMLdocuments = new LinkedList<>();

    protected static Set<String> visitedUrls = new HashSet<String>();
    
    protected static Set<String> compactString = new HashSet<String>();
    
    //protected static Integer visitedUrlsCount = 0;

    final static int stoppingCriteria = 5000;
    
    static boolean crawlerFinished = false;
    
    static int isInterrupted = 0;
    
    /**
     * @param args the command line arguments
     * @throws IOException 
     */
    public static void main(String[] args) throws InterruptedException, IOException {
        Thread crawling = new Thread(new CrawlerController());
 
        
        checkPreviousState();
        if(isInterrupted == 1) {
            loadState();
        }
        else {
            readSeeds(seedsList);
        }
      
        System.out.println(visitedUrls);
        System.out.println(seedsList);
        
        
        crawling.start();
              
        crawling.join();
        
        System.out.println("Crawling finished");
         
       
        Indexer.indexer(visitedUrls);

        for (String item: seedsList) {
            System.out.println(item);
        }
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
                /*
                if (seedListFile.createNewFile()) {
                    System.out.println("Seeds list file is created!");
                } else {
                    System.out.println("Seeds list file already exists.");
                }
                */
                //Converting Queue to Array
                String[] array = null;
                array = seedsList.toArray(new String[seedsList.size()]);
                System.out.println(Arrays.toString(array));
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
                /*
                if (visitedUrlsFile.createNewFile()) {
                    System.out.println("Visited urls file is created!");
                } else {
                    System.out.println("Visited urls file already exists.");
                }
                */
                System.out.println(visitedUrls);

                //Converting Set to Array
                String[] array = null;
                array = visitedUrls.toArray(new String[visitedUrls.size()]);
                System.out.println(Arrays.toString(array));
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

    public static void loadState()  {  

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
            System.out.println("An error occurred while loading seeds from file!");
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
            System.out.println("An error occurred while loading visitedUrls from file!");
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

