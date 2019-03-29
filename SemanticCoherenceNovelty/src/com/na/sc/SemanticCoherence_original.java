package com.na.sc;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.util.CoreMap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import com.na.cgraph.Config;
import com.na.cgraph.PreProcessText;
import com.na.cgraph.Primitives;

import java.util.Random;

@SuppressWarnings("unused")
public class SemanticCoherence_original {

	public static class Pair {
		public String first, second;

		public Pair(Object a, Object b) {
			first = a.toString();
			second = b.toString();
		}

		Pair(String a, String b) {
			first = a;
			second = b;
		}
	}

	public static double getSemanticCoherence(String entity1, String entity2) {
		// Manual Random Walks

		ArrayList<String> K1 = extractKeyPhrases(entity1);
		ArrayList<String> K2 = extractKeyPhrases(entity2);
		// get the semantic context of Context of keyPhrases.
		Node nodes_1[] = null;
		nodes_1 = Primitives.getContext(K1);
		long graph_1[][] = Primitives.getSemanticContext(nodes_1);
		// Returns weight of the relationship between node[i] and node[j] at
		// graph[i][j]
		Node nodes_2[] = null;
		nodes_2 = Primitives.getContext(K2);
		long graph_2[][] = Primitives.getSemanticContext(nodes_2);

		// combine nodes and remove duplicates
		ArrayList<Node> nodesCombinedTemp = (new ArrayList<Node>(
				new LinkedHashSet<Node>(new ArrayList<Node>(
						Arrays.asList(ArrayUtils.addAll(nodes_1, nodes_2))))));
		int commonNode[] = new int[nodes_1.length + nodes_2.length];
		for (Node n : nodes_2) {
			int found = 0;
			for (int i = 0; i < nodesCombinedTemp.size(); i++) {
				if (nodesCombinedTemp.get(i).equals(n)) {
					found = 1;
					commonNode[i] = 1;
				}
			}
			if (found == 0)
				nodesCombinedTemp.add(n);
		}
		Node nodesCombined[] = new Node[nodesCombinedTemp.size()];
		nodesCombined = nodesCombinedTemp.toArray(nodesCombined);
		String nodeNames[] = null;
		nodeNames = Primitives.getNodeNames(nodesCombined);

		// Semantic Coherence Context
		double transitionGraph[][] = Primitives.graphMerge(nodesCombined,
				graph_1, graph_2, nodes_1, nodes_2);
		int matrixSize = transitionGraph.length;

		// PrintFullMatrix(nodes_1, nodes_2, nodesCombined, graph_1, graph_2,
		// transitionGraph, nodeNames);

		// Convert edge weights to probabilities
		double ProbabilityMatrix[][] = getProbabilityMatrix(transitionGraph);

		// Calculating Semantic Coherence Value using Random Walks
		double ScSum = 0.0;
		Random rand = new Random();
		for (int j = 0; j < Config.noOfRandomWalks; j++) {
			int commonNodesVisited = 0, totalNodesVisited = 0;
			int currentNode = rand.nextInt(matrixSize);
			for (int i = 0; i < Config.lengthOfRandomWalks; i++) {
				if (commonNode[currentNode] == 1)
					commonNodesVisited++;
				totalNodesVisited++;
				currentNode = getNextRandomNode(currentNode, ProbabilityMatrix,
						matrixSize);
			}
			// System.out.println(commonNodesVisited + " " + totalNodesVisited);
			ScSum += (double) commonNodesVisited / (double) totalNodesVisited;
		}
		return ScSum / Config.noOfRandomWalks;
	}

	public static LinkedList<Entry<String, Integer>> getSemanticCoherenceAnalysis(
			String entity1) {
		// Manual Random Walks

		Map<String, Integer> counter = new HashMap<String, Integer>();

		ArrayList<String> K1 = extractKeyPhrases(entity1);
		// get the semantic context of Context of keyPhrases.
		Node nodes_1[] = null;
		nodes_1 = Primitives.getContext(K1);
		long graph_1[][] = Primitives.getSemanticContext(nodes_1);
		// Returns weight of the relationship between node[i] and node[j] at
		// graph[i][j]

		// combine nodes and remove duplicates
		ArrayList<Node> nodesCombinedTemp = new ArrayList<Node>(
				Arrays.asList(ArrayUtils.addAll(nodes_1)));
		int commonNode[] = new int[nodes_1.length];
		Node nodesCombined[] = new Node[nodesCombinedTemp.size()];
		nodesCombined = nodesCombinedTemp.toArray(nodesCombined);
		String nodeNames[] = null;
		nodeNames = Primitives.getNodeNames(nodesCombined);
		for (String s : nodeNames)
			counter.put(s, 0);

		// Semantic Coherence Context
		double transitionGraph[][] = Primitives.graphMerge(nodesCombined,
				graph_1, graph_1, nodes_1, nodes_1);
		int matrixSize = transitionGraph.length;

		// PrintFullMatrix(nodes_1, nodes_2, nodesCombined, graph_1, graph_2,
		// transitionGraph, nodeNames);

		// Convert edge weights to probabilities
		double ProbabilityMatrix[][] = getProbabilityMatrix(transitionGraph);

		// Calculating Semantic Coherence Value using Random Walks
		double ScSum = 0.0;
		Random rand = new Random();
		for (int j = 0; j < Config.noOfRandomWalks; j++) {
			int commonNodesVisited = 0, totalNodesVisited = 0;
			int currentNode = rand.nextInt(matrixSize);
			for (int i = 0; i < Config.lengthOfRandomWalks; i++) {
				counter.put(nodeNames[currentNode],
						counter.get(nodeNames[currentNode]) + 1);
				if (commonNode[currentNode] == 1)
					commonNodesVisited++;
				totalNodesVisited++;
				currentNode = getNextRandomNode(currentNode, ProbabilityMatrix,
						matrixSize);
			}
			// System.out.println(commonNodesVisited + " " + totalNodesVisited);
			ScSum += (double) commonNodesVisited / (double) totalNodesVisited;
		}
		// return ScSum/Config.noOfRandomWalks;
		return sortMap(counter);
	}

	private static LinkedList<Entry<String, Integer>> sortMap(
			Map<String, Integer> unsortMap) {

		LinkedList<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(
				unsortMap.entrySet());

		// Sorting the list based on values
		Collections.sort(list, new Comparator<Entry<String, Integer>>() {
			public int compare(Entry<String, Integer> o1,
					Entry<String, Integer> o2) {
				return (o2.getValue().compareTo(o1.getValue()));
			}
		});
		return list;
	}

	private static int getNextRandomNode(int currentNode,
			double[][] probabilityMatrix, int matrixSize) {
		Random rand = new Random();
		int pick = rand.nextInt(Config.scaleFactorRandom);
		int tempSum = 0;
		for (int i = 0; i < matrixSize; i++) {
			// System.out.println(probabilityMatrix[currentNode][i]);
			tempSum += (probabilityMatrix[currentNode][i] * Config.scaleFactorRandom);
			if (pick <= tempSum)
				return i;
			pick = rand.nextInt(Config.scaleFactorRandom);
		}
		System.out.println("Error " + tempSum);
		return 0;
	}

	private static void PrintFullMatrix(Map<String, Integer> g1_nodes,
			Map<String, Integer> g2_nodes, Pair[] nodesCombined,
			long[][] graph_1, long[][] graph_2, double[][] transitionGraph,
			Node[] nodes_1, Node[] nodes_2) {
		// Only for Printing Purpose
		System.out.println("Number of nodes in the combined graph : "
				+ nodesCombined.length);

		HashMap<Node, Integer> n1 = new HashMap<Node, Integer>();
		for (int i = 0; i < nodes_1.length; i++)
			n1.put(nodes_1[i], i);
		HashMap<Node, Integer> n2 = new HashMap<Node, Integer>();
		for (int i = 0; i < nodes_2.length; i++)
			n2.put(nodes_2[i], i);

		for (int i = 0; i < nodesCombined.length; i++)
			for (int j = 0; j < nodesCombined.length; j++)
			// if(transitionGraph[i][j] != 0)
			{
				String n1_1 = nodesCombined[i].first, n1_2 = nodesCombined[i].second, n2_1 = nodesCombined[j].first, n2_2 = nodesCombined[j].second;
				if (n1.containsKey(n1_1) && n1.containsKey(n2_1))
					System.out.println("Graph 1 contains: " + n1_1 + " and "
							+ n2_1 + " and the value in the 1st graph is: "
							+ graph_1[g1_nodes.get(n1_1)][g1_nodes.get(n2_1)]);
				if (n2.containsKey(n1_2) && n2.containsKey(n2_2))
					System.out.println("Graph 2 contains: " + n1_2 + " and "
							+ n2_2 + " and the value in the 2nd graph is: "
							+ graph_2[g2_nodes.get(n1_2)][g2_nodes.get(n2_2)]);
				System.out.println("Combined Value between " + n1_1 + " "
						+ n1_2 + "-" + n2_1 + " " + n2_2 + " is: "
						+ transitionGraph[i][j] + "\n");
			}
		// Printing Ends :)
	}

	private static void PrintFullMatrix(Node[] nodes_1, Node[] nodes_2,
			Node[] nodesCombined, long[][] graph_1, long[][] graph_2,
			double[][] transitionGraph, String[] nodeNames) {
		// Only for Printing Purpose
		System.out.println("Number of nodes in the combined graph : "
				+ nodesCombined.length);

		HashMap<Node, Integer> n1 = new HashMap<Node, Integer>();
		for (int i = 0; i < nodes_1.length; i++)
			n1.put(nodes_1[i], i);
		HashMap<Node, Integer> n2 = new HashMap<Node, Integer>();
		for (int i = 0; i < nodes_2.length; i++)
			n2.put(nodes_2[i], i);

		for (int i = 0; i < nodesCombined.length; i++)
			for (int j = 0; j < nodesCombined.length; j++)
			// if(transitionGraph[i][j] != 0)
			{
				if (n1.containsKey(nodesCombined[i])
						&& n1.containsKey(nodesCombined[j]))
					System.out.println("Graph 1 contains: "
							+ nodeNames[i]
							+ " and "
							+ nodeNames[j]
							+ " and the value in the 1st graph is: "
							+ graph_1[n1.get(nodesCombined[i])][n1
									.get(nodesCombined[j])]);
				if (n2.containsKey(nodesCombined[i])
						&& n2.containsKey(nodesCombined[j]))
					System.out.println("Graph 2 contains: "
							+ nodeNames[i]
							+ " and "
							+ nodeNames[j]
							+ " and the value in the 2nd graph is: "
							+ graph_2[n2.get(nodesCombined[i])][n2
									.get(nodesCombined[j])]);
				System.out.println("Combined Value between " + nodeNames[i]
						+ " and " + nodeNames[j] + " is: "
						+ transitionGraph[i][j] + "\n");
			}
		// Printing Ends :)

	}

	private static double[][] getProbabilityMatrix(double[][] transitionGraph) {
		double newgraph[][] = new double[transitionGraph.length][transitionGraph.length];
		for (int i = 0; i < transitionGraph.length; i++) {
			int c = 0;
			for (int j = 0; j < transitionGraph.length; j++)
				c += transitionGraph[i][j];
			for (int j = 0; j < transitionGraph.length; j++)
				newgraph[i][j] = (Config.DELTA + transitionGraph[i][j])
						/ (Config.DELTA * transitionGraph.length + c);// 0
																		// correction
		}
		return newgraph;
	}

	public static ArrayList<String> extractKeyPhrases(String line) {
		return keyWordExtraction(cleanData(readJson(line)));
	}

	public static String readJson(String line) {

		JSONParser parser = new JSONParser();
		BufferedWriter bw = null;
		String ans = "";
		try {

			Object obj = parser.parse(line);
			JSONArray jsonArray = (JSONArray) obj;
			@SuppressWarnings("rawtypes")
			Iterator iterator = jsonArray.iterator();
			while (iterator.hasNext()) {
				JSONObject learningActivity = (JSONObject) iterator.next();
				ans += learningActivity.get("title").toString() + " ";
				if (learningActivity.get("description") != null) {
					ans += learningActivity.get("description").toString();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ans;
	}

	public static ArrayList<String> keyWordExtraction(String line) {
		@SuppressWarnings("serial")
		StanfordCoreNLP pipeline = new StanfordCoreNLP(new Properties() {
			{
				setProperty("annotators", "tokenize,ssplit,pos,lemma");
			}
		});
		ArrayList<String> ans = new ArrayList<String>();
		Annotation document = pipeline.process(line);
		pipeline.annotate(document);
		for (CoreMap sentence : document.get(SentencesAnnotation.class)) {
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				String word = token.get(TextAnnotation.class);
				String lemma = token.get(LemmaAnnotation.class);
				ans.add(lemma);
			}
		}
		return new ArrayList<String>(new HashSet<String>(ans));
	}

	public static String cleanData(String line) {
		// Remove stop words, numericals, single letters, punctations
		BufferedReader brs = null;
		try {
			brs = new BufferedReader(new FileReader("data/stopWords.txt"));
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		HashMap<String, Integer> stopWords = new HashMap<String, Integer>();
		try {
			String lline;
			while ((lline = brs.readLine()) != null) {
				stopWords.put(lline.trim(), 1);
			}
			brs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Locale locale1 = new Locale("en");
		String[] temp = line.trim().split("\\W+");
		String templine = "";
		for (int i = 0; i < temp.length; i++) {
			if (stopWords.containsKey(temp[i].toString().toLowerCase(locale1)) != true
					&& (isNotWord(temp[i].toString()) != true)) {
				templine = templine.concat(temp[i].toString().toLowerCase()
						+ " ");
			}
		}
		return templine.trim();
	}

	public static boolean isNotWord(String input) {
		Pattern pattern = Pattern.compile(".*[0-9].*");
		Pattern pattern3 = Pattern.compile("(\\W)*");
		Pattern singleLetter = Pattern.compile("\\w");
		if (pattern.matcher(input).matches()
				|| pattern3.matcher(input).matches()
				|| singleLetter.matcher(input).matches()) {
			return true;
		}
		return false;
	}

}
