/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Maram
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.json.*;

public class CrawlerController {
    int numberofThreads;
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    String line;

    CrawlerController() {

    }; 
  
  
    public void run() {
        Thread.currentThread().setName("CrawlerController");
        System.out.println("Please enter the number of crawler threads: ");
        try { 
           numberofThreads= Integer.parseInt(reader.readLine());
       } catch (IOException e) {
           Logger.getLogger(CrawlerController.class.getName()).log(Level.SEVERE, null, e);
       }

        Thread[] crawlerThreads = new Thread[numberofThreads];
        
        for (int i = 0; i < numberofThreads; i++) {
            crawlerThreads[i] = new Thread(new Crawler());
  
            crawlerThreads[i].setName("C" + i);
            crawlerThreads[i].start();
        }


        for (int i = 0; i < numberofThreads; i++) {
            try {
                crawlerThreads[i].join();
            } catch (InterruptedException ex) {
                Logger.getLogger(CrawlerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        Main.crawlerFinished = true;
        Main.isInterrupted = 0;
        Main.saveState(0);

    }         
}
