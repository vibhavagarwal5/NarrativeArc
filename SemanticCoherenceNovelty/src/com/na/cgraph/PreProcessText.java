package com.na.cgraph;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.StemAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

@SuppressWarnings("unused")
public class PreProcessText {
	public static void readJson() {

		JSONParser parser = new JSONParser();
		BufferedWriter bw = null;
		try {

			File f = new File("data/LAText.txt");
			bw = new BufferedWriter(new FileWriter(f));

			Object obj = parser.parse(new FileReader(Config.JSON_INPUT));

			JSONArray jsonArray = (JSONArray) obj;

			@SuppressWarnings("rawtypes")
			Iterator iterator = jsonArray.iterator();
			while (iterator.hasNext()) {
				JSONObject learningActivity = (JSONObject) iterator.next();
				// System.out.println(learningActivity.get("title"));
				// System.out.println(learningActivity.get("description"));
				bw.write(learningActivity.get("title").toString());
				bw.write(" ");
				if (learningActivity.get("description") != null) {
					bw.write(learningActivity.get("description").toString());
				}
				bw.write("\n");
			}
			bw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}

	public static void keyWordExtraction() {

		// TODO: Remove proper nouns

		/*
		 * Properties props = new Properties(); props.put("annotators",
		 * "tokenize, ssplit, pos, lemma"); StanfordCoreNLP pipeline = new
		 * StanfordCoreNLP(props, false);
		 */

		@SuppressWarnings("serial")
		StanfordCoreNLP pipeline = new StanfordCoreNLP(new Properties() {
			{
				setProperty("annotators", "tokenize,ssplit,pos,lemma");
			}
		});

		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			br = new BufferedReader(new FileReader("data/CleaneddData_Science.txt"));
			// br = new BufferedReader(new
			// FileReader("data/KeywordsTrial.txt"));
			// br = new BufferedReader(new FileReader("data/LAText.txt"));
			String line;
			bw = new BufferedWriter(new FileWriter(Config.KEYWORDS_PATH));
			while ((line = br.readLine()) != null) {
				// process the line.
				// System.out.println(line);
				Annotation document = pipeline.process(line);
				// Annotation document =new Annotation(line);
				pipeline.annotate(document);
				for (CoreMap sentence : document.get(SentencesAnnotation.class)) {
					for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
						String word = token.get(TextAnnotation.class);
						String lemma = token.get(LemmaAnnotation.class);
						// System.out.println(word + " lemmatized version :" +
						// lemma);
						bw.write(lemma + " ");
					}
					bw.write("\n");
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (bw != null) {
					try {
						bw.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

	}

	public static void cleanData() {
		// Remove stop words, numericals, single letters, punctations
		BufferedReader br;
		BufferedReader brs;
		try {
			br = new BufferedReader(new FileReader("./data/Latex.txt"));
			brs = new BufferedReader(new FileReader("./data/stopWords.txt"));
			String line;
			HashMap<String, Integer> stopWords = new HashMap<String, Integer>();
			try {
				while ((line = brs.readLine()) != null) {
					stopWords.put(line.trim(), 1);
				}
				brs.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			BufferedWriter bw = new BufferedWriter(new FileWriter(
					"./data/CleaneddData_Science.txt"));
			Locale locale1 = new Locale("en");

			try {
				while ((line = br.readLine()) != null) {
					String[] temp = line.trim().split("\\W+");
					String templine = "";
					for (int i = 0; i < temp.length; i++) {
						if (stopWords.containsKey(temp[i].toString()
								.toLowerCase(locale1)) != true
								&& (isNotWord(temp[i].toString()) != true)) {
							templine = templine.concat(temp[i].toString()
									.toLowerCase() + " ");
						}
					}
					System.out.println(templine);
					bw.write(templine.trim() + "\n");

				}
				bw.close();

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static boolean isNotWord(String input) {

		Pattern pattern = Pattern.compile(".*[0-9].*");
		// Pattern pattern2 = Pattern.compile("(\\d)*");
		Pattern pattern3 = Pattern.compile("(\\W)*");
		Pattern singleLetter = Pattern.compile("\\w");
		if (pattern.matcher(input).matches()
				|| pattern3.matcher(input).matches()
				|| singleLetter.matcher(input).matches()) {
			// System.out.println(input);
			return true;
		}
		return false;
	}

}
