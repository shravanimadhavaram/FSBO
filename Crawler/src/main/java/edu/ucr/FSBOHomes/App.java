package edu.ucr.FSBOHomes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

public class App 
{
	static WebDriver driver=null;
    public static void main( String[] args ) throws IOException
    {    
    	List<String> states = new ArrayList<String>();
    	File input= new File("states.txt");
    	FileInputStream fis = new FileInputStream(input);
    	 
    	//Construct BufferedReader from InputStreamReader
    	BufferedReader br = new BufferedReader(new InputStreamReader(fis));
     
    	String line = null;
    	while ((line = br.readLine()) != null) {
    		states.add(line);
    	}
    	br.close();
    	try 
        {
        driver = new FirefoxDriver();
    	String mainUrl="http://fsbo.com/listings/search/searchresults/";
    	for(String state:states){
    		System.out.println(state);
    		driver.get(mainUrl);
            WebElement email = driver.findElement(By.id("searchQuery"));
            email.sendKeys(state);
            email.sendKeys(Keys.ENTER);
            System.out.println("loaded");
            List<String> urls= new ArrayList<String>();
            while(true){
            	String page = driver.getPageSource();
    			Document doc = Jsoup.parse(page, mainUrl);
    			Elements links = doc.select("a.fsbo-button");
    			for (Element link : links) 
    		    {
    				if(link.attr("abs:href").toLowerCase().contains("/listings/show/id/"))
    					urls.add(link.attr("abs:href"));
    		    }
    			try{
    				driver.findElement(By.xpath("//a[contains(.,'next')]")).click();
    			}
    			catch(Exception e){
    				break;
    			}
            }
            int count=1;
            new File(state).mkdir();
            for(String url:urls){
            	getData(url,count,state);
            	count++;
            }
            System.out.println(state+ "done");
    		
    	}
        } catch(Throwable t)
        {
            System.err.println("Caught throwable " + t);
            t.printStackTrace();
            throw new RuntimeException("Failed to create FireFoxDriver");
        }
    	driver.close();
        driver.quit();
   
    }

	private static void getData(String url, int count, String state) throws IOException {
		// TODO Auto-generated method stub
		driver.get(url);
		String page = driver.getPageSource();
		FileWriter fileWriter = new FileWriter(state+"/"+count+ ".txt");
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
	    bufferedWriter.write(page);
	    bufferedWriter.close();
	    File file=new File(state+"/"+count+ ".txt");
		
	}
}
