
import static RobotReader.allowedUrls;
import static RobotReader.disallowedUrls;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Maram
 */
public class Trial {
    static RobotReader readRobotFiles = new RobotReader();        //read robot files

    static protected HashMap<String, ArrayList<String>> allowedUrls = new HashMap<String, ArrayList<String>>();
    static protected HashMap<String, ArrayList<String>> disallowedUrls = new HashMap<String, ArrayList<String>>();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws MalformedURLException {
        // TODO code application logic here
        String visit = "https://twitter.com";
        readRobotFiles.checkRobotFile(visit);
    }
    
    
}
