/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.ArrayList;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;


public class RobotReader {
    static protected HashMap<String, ArrayList<String>> allowedUrls = new HashMap<String, ArrayList<String>>();
    static protected HashMap<String, ArrayList<String>> disallowedUrls = new HashMap<String, ArrayList<String>>();

    
    protected URL getRobotFileURL(String url) throws MalformedURLException{  
        String domain = null;
        URL urlAddress = new URL(url);
        domain = urlAddress.getProtocol() + "://"  + urlAddress.getHost();
        String robotFileURL = domain + "/robots.txt";
        //System.out.println("Robot file URL is: "+ robotFileURL);
        return new URL(robotFileURL);          
    }
    
    protected boolean checkRobotFile(String url) {
        URL Roboturl = null;
        URL checkURL = null;   
        
        try {
            checkURL = new URL(url);
            String protocol = checkURL.getProtocol();
                     
            if (!protocol.equals("http") && !protocol.equals("https")) {
                return false;
            }
            else {
                Roboturl = getRobotFileURL(url);
            }
            
        } catch (MalformedURLException m) {
            System.out.println(Thread.currentThread().getName() + ": Invalid robot file URL");
            return false;
        }
        
        boolean inAllowed = allowedUrls.containsKey(Roboturl.getHost());
        boolean inDisallowed = disallowedUrls.containsKey(Roboturl.getHost());

        if (!inAllowed && !inDisallowed)   //checking that robot file wasn't downloaded before
        {
            //System.out.println(Thread.currentThread().getName() + " Allowed: " + allowedUrls.keys());
            //System.out.println(Thread.currentThread().getName() + " Disallowed: " + disallowedUrls.keys());
  
            if (addtoRobotList(Roboturl)) {
                System.out.println("Added to robot list");
                //System.out.println("Allowed: " + allowedUrls);
                //System.out.println("Disallowed: " + disallowedUrls);
                return true;                         
            }
        }
 
        String file = checkURL.getFile();           // gets the directory 
        //System.out.println("String file: "+ file);
        try {
            for (String s : allowedUrls.get(Roboturl.getHost())) {
                //System.out.println("String allowed: " + s);
                if ((file.compareToIgnoreCase(s) == 0)) {
                    System.out.println(Thread.currentThread().getName() + ": Visiting URL " + url + " is allowed!");
                    return true;
                }
            }

            for (String s : disallowedUrls.get(Roboturl.getHost())) {
                //System.out.println("String disallowed: " + s);
                if (file.startsWith(s)) {
                    System.out.println(Thread.currentThread().getName() + ": Visiting URL " + url + " is disallowed!");
                    return false;
                }
            }
        } catch (NullPointerException ex) {
            return false;
        }
        return true;
    }
    

    protected boolean addtoRobotList(URL urlRobot) 
    {
        HttpURLConnection connect;
        //System.out.println("Adding to robot");        
        
        try {
            connect = (HttpURLConnection) urlRobot.openConnection();
            connect.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36" + "(KHTML, like Gecko) Chrome/91.0.4472.77 Safari/537.36");
            //System.out.println("Robot connected");
        } catch (IOException e) {
            System.out.println(Thread.currentThread().getName() + ": Connection error when retrieving robot file!");
            return false;
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connect.getInputStream()));
            String robotText = new String();
            String path;
            ArrayList<String> disallowed = new ArrayList<String>();
            ArrayList<String> allowed = new ArrayList<String>();
            

            while ((robotText = reader.readLine()) != null)             //loop until we reach the desired user agent
            {
                if (robotText.startsWith("User-agent: *")) {
                    break;
                }
            }

            while ((robotText = reader.readLine()) != null)
            {
                if (robotText.startsWith("User")) {
                    break;
                }

                if (robotText.indexOf("Disallow:") == 0) {
                    path = robotText.substring("Disallow:".length());       

                    int commentIndex;
                    commentIndex = path.indexOf("#");
                    if (commentIndex != - 1) {
                        path = path.substring(0, commentIndex);
                        System.out.println(path);
                    }
        
                    path = path.trim();                                 // remove any trailing or leading spaces from path

                    //System.out.println(path);
                    
                    if (path.isEmpty() || path == null)                 // if empty then it allows access to all content
                    {
                        System.out.println(Thread.currentThread().getName() + " finds that everything is allowed in url: " + urlRobot);
                        return true;
                    }
                    
                    disallowed.add(path);    

                } else if (robotText.indexOf("Allow:") == 0) {
                    path = robotText.substring("Allow:".length());
                    
                    int commentIndex;
                    commentIndex = path.indexOf("#");
                    if (commentIndex != - 1) {
                        path = path.substring(0, commentIndex);
                    }
                    
                    path = path.trim();
                    allowed.add(path);
                }
            }
            
            reader.close(); //Closes buffer and releases memory resources
            allowedUrls.put(urlRobot.getHost(), allowed);
            disallowedUrls.put(urlRobot.getHost(), disallowed);
            
            //System.out.println("Allowed:" + RobotReader.allowedUrls);
            //System.out.println("Disallowed:" + RobotReader.disallowedUrls);            
            
            allowed = null;
            disallowed = null;
            
        } catch (IOException ex) {
            System.out.println(Thread.currentThread().getName() + " couldn't find robot file for " + urlRobot); /* Assume crawling is allowed since robot file doesn't exist. */ 
            return true;
        }
        return false;
    }
   
}
 