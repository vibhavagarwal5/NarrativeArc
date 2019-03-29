package edu.iiitb.na.cgraph;

import java.io.FileInputStream;
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
	public static int noOfRandomWalks = 50;
	public static int noOfStartEndNodes = 50;
	public static int lengthOfRandomWalks = 100;
	public static int scaleFactorRandom = 100000;
	public static String JSON_INPUT;
	
	public static final void loadConfigs() {

	Properties prop = new Properties();
	InputStream input = null;

	try {

		input = new FileInputStream("config.properties");

		// load a properties file
		prop.load(input);

		// get the property value and print it out
		DB_PATH = prop.getProperty("DB_PATH");
		KEYWORDS_PATH = prop.getProperty("KEYWORDS_PATH");
		STOP_WORDS = prop.getProperty("STOP_WORDS");
		JSON_INPUT = prop.getProperty("JSON_INPUT");

	} 
	catch (Exception ex) {
		ex.printStackTrace();
	} 
  }
}
