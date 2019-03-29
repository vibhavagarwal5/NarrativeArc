package com.na.cgraph;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
	
	public static double NormalizationOffset = 1.0;
	public static String DB_PATH;
	public static String KEYWORDS_PATH;
	public static String STOP_WORDS;
	public static double LAMBDA = 0.6;
	public static double DELTA = 0.03;
	public static int noOfIterationsForSC = 5;
	public static int noOfRandomWalks = 100;
	public static int noOfStartEndNodes = 50;
	public static int lengthOfRandomWalks = 100;
	public static int scaleFactorRandom = 100000;
	public static String JSON_INPUT;
	
	static Properties prop = new Properties();
	  static {
		
			System.out.println("in static block initializing property files");
	        
	        try (InputStream inputStream = Config.class.getResourceAsStream("config.properties")){
	            prop.load(inputStream);
	        } catch (IOException e) {
	            throw new RuntimeException("Failed to read file " + "config.properties", e);
	        }
	    }
	
	public static final void loadConfigs() {
	try {

		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		InputStream input = loader.getResourceAsStream("config.properties");

		input = new FileInputStream("config.properties");

		//	load a properties file
		prop.load(input);

		// get the property value and print it out
		System.out.println("set db path");

		DB_PATH = prop.getProperty("DB_PATH");
		System.out.println("path is :" + DB_PATH);

		KEYWORDS_PATH = prop.getProperty("KEYWORDS_PATH");

		STOP_WORDS = prop.getProperty("STOP_WORDS");
		System.out.println(STOP_WORDS);
		JSON_INPUT = prop.getProperty("JSON_INPUT");

	} 
	catch (Exception ex) {
		System.out.println("Config properties not set");
		ex.printStackTrace();
	} 
  }
}
