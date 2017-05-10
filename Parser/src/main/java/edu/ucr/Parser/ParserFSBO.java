package edu.ucr.Parser;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;


class Coordinates
{
    double longitude; 
    double latitude; 
    Coordinates(double m, double d)
    {
    	longitude = m;
    	latitude = d;
    }
}

public class ParserFSBO {
	
	
	
	 public static void main( String[] args ) throws IOException, ParserConfigurationException, SAXException, SQLException
	    {
		 
		 Connection conn = null;
		 Statement stat = null;
		 ResultSet rs = null;
		 PreparedStatement pstat = null;
		 String dbName = null;
		 
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
    	for(String state:states){
		 String directoryPath = state+"/";
			File directory = new File(directoryPath);

			if (directory != null) {
				File[] textFiles = directory.listFiles();

				if (textFiles != null) {
					System.out.println("Number of files to parse: " + textFiles.length);

					for (File textFile : textFiles) {
						if (textFile.isFile() && textFile.getName().endsWith(".txt")) {
							parseAndSave(textFile);
						}
					}
				}
			}
    	}
    	System.out.println("All done");
		} 
	    
	   
		public static void parseAndSave(File file) throws IOException, SQLException{
		 	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String owner=null;
			String phoneNumber1=null;
			String phoneNumber2=null;
			String emailAddress=null;
			String address=null;
			String city=null;
			String state=null;
			String zipcode=null;
			int listingId=-1;
			String bedrooms=null;
			String bathrooms=null;
			String garage=null;
			String type=null;
			String subtype=null;
			String lotSize=null;
			int salePrice=-1;
			String squareFeet=null;
			int yearBuilt=-1;
			String schoolDistrict=null;
			String subdivision=null;
			String pageSource=null;
			String description=null;
			String amenities=null;
			Timestamp crawlTime=null;
			double latitude=0.0;
			double longitude=0.0;
			crawlTime = Timestamp.valueOf(sdf.format(file.lastModified()));
			String regex = "(.)*(\\d)(.)*";
			Pattern pattern = Pattern.compile(regex);
			Document doc = Jsoup.parse(file, "UTF-8");
			
			Elements ownerdetails=doc.getElementsByClass("modal-body");
			
			if(doc.getElementsByClass("price")!=null){
				Elements price=doc.getElementsByClass("price");
				String sale=price.text();
				String sales[]= sale.split("\\s");
				salePrice= Integer.parseInt(sales[0].replace("$","").replace(",","").trim());
			}
			 Elements myin = doc.getElementsByClass("address");
			
			JSONObject geoResponse=getGeoResponse(myin.text());
			try {
				Coordinates c=getCoordinate(geoResponse);
				latitude=c.latitude;
				longitude=c.longitude;
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("inside catch");
			}
			
			BigDecimal lat = new BigDecimal(Float.toString((float) latitude));
			BigDecimal longi = new BigDecimal(Float.toString((float) longitude));
			Elements addressMobileSpan = doc.select("div.address-copy span.address-mobile");
			if (addressMobileSpan.text().length() > 0) {
				Elements aHrefs = addressMobileSpan.select("a[href]");
				String homeAddress = "";

				if (aHrefs.size() > 0) {
					// googleMapUrl exists
					Element aHref = aHrefs.first();
					homeAddress = aHref.html();
				} else {
					// googleMapUrl does not exist
					homeAddress = addressMobileSpan.html();
				}

				if ((homeAddress != null) && (!homeAddress.isEmpty())) {
					String[] homeAddressParts = homeAddress.split("<br>");

					if (homeAddressParts != null) {
						if (homeAddressParts.length > 1) {
							address = homeAddressParts[0].trim();
						}

						String cityStateZipcode = homeAddressParts[homeAddressParts.length - 1].trim();
						String[] cityStateZipcodeParts = cityStateZipcode.split(",");
						city = cityStateZipcodeParts[0].trim();

						String[] stateZipcodeParts = cityStateZipcodeParts[cityStateZipcodeParts.length - 1].trim().split(" ");
						state = stateZipcodeParts[0].trim();
						zipcode = stateZipcodeParts[1].trim();
					}}
			}

			
			Elements tables = doc.select("table tr");
			for(Element table : tables)
			{
			     //System.out.println(table.text().toString());
			     String temp=table.text();
			     if(table.text().contains("Listing ID:")){
			    	 listingId=Integer.parseInt(temp.replace("Listing ID:", "").trim());
			    	//System.out.println(listingId);
			     }
			     if(table.text().contains("Bedrooms:")){
			    	 bedrooms=temp.replace("Bedrooms:", "").trim();
			    	//System.out.println(bedrooms);
			     }
			     if(table.text().contains("Bathrooms:")){
			    	 bathrooms=temp.replace("Bathrooms:", "").trim();
			    	//System.out.println(bathrooms);
			     }
			     if(table.text().contains("Garage:")){
			    	 garage=(temp.replace("Garage:", "").trim());
			    	//System.out.println(garage);
			     }
			     if(table.text().contains("Type:")){
			    	 type=temp.replace("Type:", "").trim();
			    	//System.out.println(type);
			     }
			     if(table.text().contains("Subtype:")){
			    	 subtype=temp.replace("Subtype:", "").trim();
			    	//System.out.println(subtype);
			     }
			     if(table.text().contains("Lot Size:")){
			    	 lotSize=temp.replace("Lot Size:", "").trim();
			    	//System.out.println(lotSize);
			     }
			     if(table.text().contains("Sq. Feet:")){
			    	 squareFeet=temp.replace("Sq. Feet:", "").trim();
			    	//System.out.println(squareFeet);
			     }
			     if(table.text().contains("Year Built:")){
			    	 yearBuilt=Integer.parseInt(temp.replace("Year Built:", "").trim());
			    	//System.out.println(yearBuilt);
			     }
			     if(table.text().contains("School District:")){
			    	 schoolDistrict=temp.replace("School District:", "").trim();
			    	//System.out.println(schoolDistrict);
			     }
			     if(table.text().contains("Subdivision:")){
			    	 subdivision=temp.replace("Subdivision:", "").trim();
			    	//System.out.println(subdivision);
			     }
			    	 
			}
			
			pageSource = "http://fsbo.com/listings/listings/show/id/" + listingId + "/";
			
			
			if(doc.getElementsByClass("description")!=null){
				Elements desc=doc.select("[class=hidden-xs property-description]");
				description=desc.text();
			}
			
			if(doc.getElementsByClass("amenities")!=null){
				Elements amen=doc.select("[class=hidden-xs more-amenities]");
				amenities=amen.text();
			}
			
			
			Element contactModalIn = doc.select("div#sellerModal").first();

			if (!contactModalIn.text().isEmpty()) {
				Element contactModalBody = contactModalIn.select("div.modal-dialog div.modal-content div.modal-body")
						.first();
				if (!contactModalBody.text().isEmpty()) {
					Element contactModalBodyDiv = contactModalBody.select("div").first();
					Elements divs = contactModalBodyDiv.select("div");
					for (int i = 0; i < divs.size(); ++i) {
						String message = divs.get(i).text().trim();
						String value = "";
						if (message.equalsIgnoreCase("Contact:")) {
							value = divs.get(++i).text().trim();
							owner=value;
						} else if (message.equalsIgnoreCase("Phone:")) {
							value = divs.get(++i).text().trim();
							String filteredValue = value.replaceAll("[^0-9]", "");

							if ((filteredValue.length()) >= 10 && (filteredValue.length() <= 11)) {
								
								if (phoneNumber1==null) {
									phoneNumber1=filteredValue;
								} else {
									phoneNumber2=filteredValue;
								}
							} else {
								String temp = getEmailAddressFromText(value);

								if (temp.length() > 0) {
									emailAddress=temp;
								}
							}
						} else if (message.equalsIgnoreCase("Email:")) {
							value = divs.get(++i).text().trim();
							String temp = getEmailAddressFromText(value);

							if (temp.length() > 0) {
								emailAddress=temp;
							}
						} else if (message.equalsIgnoreCase("Email Address:")) {
							value = divs.get(++i).text().trim();
							String temp = getEmailAddressFromText(value);

							if (temp.length() > 0) {
								emailAddress=temp;
							}
						}
					}

				}}
			
		
			String myDB= "homeDB";
			String strDb = "jdbc:mysql://localhost/myDB?useUnicode=true&characterEncoding=Big5";
	        strDb = strDb.replaceAll("myDB", myDB);
	        String strUsr = "root";
	        String strPwd = "home123";
	        Connection conn = null;
	        try
	        {
	            if(conn!=null)
	                conn.close();
	            
	            Class.forName("com.mysql.jdbc.Driver");
	            conn = DriverManager.getConnection("jdbc:mysql://localhost/homeDB?user=root&password=home123");
}
	        catch(ClassNotFoundException e)
	        {
	            System.out.println("DriverClassNotFound :"+e.getMessage());
	            e.printStackTrace();
	        }
	        catch(SQLException e)
	        {
	            System.out.println("SQL Exception :"+e.getMessage());
	            e.printStackTrace();
	        } catch (Exception e) {
	            System.out.println("Exception :"+e.getMessage());
	            e.printStackTrace();
	        }
	        String sqlStatement = "INSERT INTO FSBOhomes(owner, phone_number1, phone_number2, email_address, url, sale_price, street_address, city, state, zipcode, latitude, longitude, listingId, num_bedrooms, num_bathrooms, garage, type, subtype, lot_size, square_feet, year_built, school_district, subdivision, description, amenities, crawl_time) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			PreparedStatement preparedStatement = conn.prepareStatement(sqlStatement);

			// 1
			if (owner!=null) {
				preparedStatement.setString(1, owner);
			} else {
				preparedStatement.setString(1, null);
			}
			// 2
			if (phoneNumber1!=null) {
				preparedStatement.setString(2, phoneNumber1);
			} else {
				preparedStatement.setString(2, null);
			}
			// 3
			if (phoneNumber2!= null) {
				preparedStatement.setString(3, phoneNumber2);
			} else {
				preparedStatement.setString(3, null);
			}
			// 4
			if (emailAddress!=null) {
				preparedStatement.setString(4, emailAddress);
			} else {
				preparedStatement.setString(4, null);
			}
			// 5
			preparedStatement.setString(5, pageSource);
			// 6
			preparedStatement.setInt(6, salePrice);
			// 7
			if (address!=null) {
				preparedStatement.setString(7, address);
			} else {
				preparedStatement.setString(7, null);
			}
			// 8
			
				preparedStatement.setString(8,city);
			
			// 9
			
				preparedStatement.setString(9, state);
			
			// 10
			preparedStatement.setString(10, zipcode);
			// 11
			preparedStatement.setBigDecimal(11, lat);
			// 12
			preparedStatement.setBigDecimal(12, longi);
			// 13
			preparedStatement.setInt(13, listingId);
			// 14
			preparedStatement.setString(14, bedrooms);
			// 15
			preparedStatement.setString(15, bathrooms);
			// 16
			preparedStatement.setString(16, garage);
			// 17
				preparedStatement.setString(17, type);
			//18
				preparedStatement.setString(18, subtype);
			
			// 19
				preparedStatement.setString(19, lotSize);
			// 20
				preparedStatement.setString(20, squareFeet);
			
			// 21
			preparedStatement.setInt(21, yearBuilt);
			// 22\
				preparedStatement.setString(22, schoolDistrict);
			
			// 23
				preparedStatement.setString(23, subdivision);
		
			// 24
				preparedStatement.setString(24,description);
			
			// 25
				preparedStatement.setString(25, amenities);
			
			// 26
			preparedStatement.setTimestamp(26, crawlTime);
			preparedStatement.executeUpdate();
			preparedStatement.close();
		
	    }
	 
			
			public static String getEmailAddressFromText(String text) {
				String emailAddress = "";
				Matcher m = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+").matcher(text);

				while (m.find()) {
					emailAddress = m.group();

					if (!emailAddress.isEmpty()) {
						break;
					}
				}

				return emailAddress;
			}
			
			
	 public static JSONObject getGeoResponse(String address) {
		 int numLimitFail=0;
			String strJSON = "";
			String key="AIzaSyBtuZUaRxj9FMBuiKnXRUG935uM2BAqvAE";

			try {
				int responseCode = 0;
				String strURL = "https://maps.googleapis.com/maps/api/geocode/json?address="
						+ URLEncoder.encode(address, "UTF-8") + "&key=" + key;
				// System.out.println(strURL);
				URL url = new URL(strURL);
				HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
				httpConnection.connect();
				responseCode = httpConnection.getResponseCode();
				if (responseCode == 200) {
					// Read response result from the httpConnection via the
					// bufferedReader
					BufferedReader bufferedReader = new BufferedReader(
							new InputStreamReader(httpConnection.getInputStream()));
					String line;
					while ((line = bufferedReader.readLine()) != null) {
						strJSON += line;
					}
					bufferedReader.close();

					// Check if the response object is valid
					JSONObject jsonObj = new JSONObject(strJSON);
					String status = (String) jsonObj.get("status");
					if (status.equals("OK")) {
						numLimitFail = 0;
						return jsonObj;
					} else if (status.equals("OVER_QUERY_LIMIT")) {
						System.out.println("Method getGeoResponse");
						System.out.println("Geocoding API error - response status: " + status);
						if (numLimitFail < 10) // Access too frequently per second
						{
							numLimitFail += 1;
							Thread.sleep(1000);
							return getGeoResponse(address);
						} else // Over 2500 limit per day
						{
							numLimitFail = 0;
							//switchKey();
							return getGeoResponse(address);
						}
					} else {
						System.out.println("Method getGeoResponse");
						System.out.println("Geocoding API error - response status: " + status);
						numLimitFail = 0;
						return null;
					}
				} else {
					System.out.println("Geocoding API connection fails.");
					numLimitFail = 0;
					return null;
				}
			} catch (Exception ex) {
				// System.out.println("Geocoding API error occurs.");
				return null;
			}
		}

		public static Coordinates getCoordinate(JSONObject responseJSONObj) throws Exception {
			
			if (responseJSONObj == null) {
				System.out.println("Method getCoordinate : response null");
				System.out.println("Geocoding API error occurs.");

			}
			JSONArray jsonArray = (JSONArray) responseJSONObj.get("results");
			JSONObject jsonObject = (JSONObject) jsonArray.get(0);
			JSONObject geometryObj = (JSONObject) jsonObject.get("geometry");
			JSONObject locationObj = (JSONObject) geometryObj.get("location");
			double latitude = Double.parseDouble(locationObj.get("lat").toString());
			double longitude = Double.parseDouble(locationObj.get("lng").toString());
			return new Coordinates(latitude, longitude);
		}


}
