package com.na.sc;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import org.apache.commons.lang3.ArrayUtils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.neo4j.graphdb.Node;


import com.na.cgraph.Config;
import com.na.cgraph.Primitives;

@SuppressWarnings("unused")
public class SemanticCoherence {

   
    private static int getNextRandomKeyword(double[] probabilityMatrix, int matrixSize)
	{
		Random rand = new Random();
		int pick = rand.nextInt(100);
		int tempSum = 0;

		for (int i = 0; i < matrixSize; i++) {

			tempSum += (probabilityMatrix[i] * 100);
			if (pick <= tempSum) return i;
//			pick = rand.nextInt(Config.scaleFactorRandom);
		}
		return 0;
	}
    
   /* public static double[] getSemanticCoherenceNovelty(String entity1, String entity2, PrintWriter out) {
		//for first string
		//Calculating the keyphrases
		//E.g Numbers become Number, brought becomes bring
    	System.out.println("semantic coherence novelty");
		ArrayList<String> K1 = extractKeyPhrases(entity1);
		// get the semantic context of Context of keyPhrases.
		//i.e total number of neighboring nodes
		System.out.println("before getclosure");
		Node nodes_1[] = Primitives.getClosure(K1);
		System.out.println("after getclosure");
		long graph_1[][] = Primitives.getSemanticContext(nodes_1);

		//for second string
		ArrayList<String> K2 = extractKeyPhrases(entity2);

		Node nodes_2[] = Primitives.getClosure(K2);

		long graph_2[][] = Primitives.getSemanticContext(nodes_2);

		// combine nodes and remove duplicates
		ArrayList<Node> nodesCombined = new ArrayList<>(
                new LinkedHashSet<>(Arrays.asList(ArrayUtils.addAll(nodes_1, nodes_2))));

		int commonNode[] = new int[nodesCombined.size()];
		int novelNodeMarking[] = new int[nodesCombined.size()];

		for (Node n : nodes_2)
			commonNode[nodesCombined.indexOf(n)] = 1;
		
		
		
	
		//Nodes in entity 2 but not in entity 1
		ArrayList<Node> nodes1 = new ArrayList<>(
                new LinkedHashSet<>(Arrays.asList(ArrayUtils.addAll(nodes_1))));
		ArrayList<Node> nodes2 = new ArrayList<>(
                new LinkedHashSet<>(Arrays.asList(ArrayUtils.addAll(nodes_2))));    
                		
		ArrayList<Node> novelNodes = nodes2;
		for (Node n : nodes_2){
			if(nodes1.contains(n))
				novelNodeMarking[nodes2.indexOf(n)]= 0;
			else 
				novelNodeMarking[nodes2.indexOf(n)]= 1;
		}
		
		System.out.println("Printing nodes of entity 1");
		out.println("Printing nodes of entity 1");
		for(int i=0;i<nodes1.size();i++){
			out.println(nodes1.get(i).toString());
		}
		System.out.println("Printing nodes of entity 2");
		out.println("Printing nodes of entity 2");
		for(int i=0;i<nodes2.size();i++){
			
			out.println(nodes2.get(i).toString());
		}
		System.out.println("Printing novel nodes");
		out.println("Printing novel nodes");
		for(int i=0;i<novelNodes.size();i++){
			out.print(novelNodes.get(i) +" ");
		}
			
		System.out.println("Printing common nodes");
		for(int i=0;i<nodesCombined.size();i++){
			if(commonNode[nodesCombined.indexOf(i)]==1)
				System.out.println(novelNodes.get(i).toString());
		}
			

		// Semantic Coherence Context
		double transitionGraph[][] = Primitives.graphMerge(nodesCombined, nodes_1, nodes_2);
		// PrintFullMatrix(nodes_1, nodes_2, nodesCombined, graph_1, graph_2,
		// transitionGraph, nodeNames);

		// Convert edge weights to probabilities
		double ProbabilityMatrix[][] = getProbabilityMatrix(nodesCombined, transitionGraph);
		
		// Calculating Semantic Coherence Value using Random Walks
        //Implemented Stabilisation factor
        double ScSum = 0.0;
        double novelSum = 0.0;
        
        Random rand = new Random();
        boolean flag = false;
        int out1 = (Config.noOfRandomWalks/3);
        int matrixSize = transitionGraph.length;

        for (int j = 0; j < out1; j++)
        {
            int commonNodesVisited = 0, totalNodesVisited = 0, novelNodesVisited=0;
            int currentNode = rand.nextInt(matrixSize);

            for (int i = 0; i < transitionGraph.length; i++) {
                double[] aTransitionGraph = transitionGraph[i];
                if (commonNode[currentNode] == 1) 
                	commonNodesVisited++;
               
                if (novelNodeMarking[currentNode] == 1)
                	novelNodesVisited++;
                
                totalNodesVisited++;
                
                currentNode = getNextRandomNode(currentNode, ProbabilityMatrix, matrixSize);
                if (currentNode == 0) {
                    if (flag) break;
                    flag = true;
                }
            }
//             System.out.println(commonNodesVisited + " " + totalNodesVisited);
            ScSum += (double) commonNodesVisited / (double) totalNodesVisited;
            novelSum+=(double) novelNodesVisited / (double) totalNodesVisited;
        }
        System.out.println("ScSum"+ScSum);
        System.out.println("novelSum"+novelSum);
        double oldSC=ScSum / out1;
//        	System.out.println(oldSC);
        double oldNS=novelSum / out1;
        
        double newSC=0.0;
        double newNS=0.0;
        int counter=0;

        //stabilization code
        for (int j = out1; j < out1*3; j++)
        {
            int commonNodesVisited = 0, totalNodesVisited = 0, novelNodesVisited=0;
            int currentNode = rand.nextInt(matrixSize);

            for(int i = 0; i < transitionGraph.length; i++)
            {
                if (commonNode[currentNode] == 1)
                    commonNodesVisited++;
                if (novelNodeMarking[currentNode] == 1)
                	novelNodesVisited++;
                totalNodesVisited++;
                currentNode = getNextRandomNode(currentNode, ProbabilityMatrix, matrixSize);
                if(currentNode == 0)
                {
                    if(flag) break;
                    flag = true;
                }
            }

            ScSum += (double) commonNodesVisited / (double) totalNodesVisited;
            novelSum+=(double) novelNodesVisited / (double) totalNodesVisited;
            newSC=ScSum/(j+1);
//            System.out.println(newSC);
            newNS=novelSum/(j+1);
            if((newSC-oldSC)>0.01 || (oldSC-newSC)>0.01)
                counter++;
            else
                break;

            oldSC=newSC;
            oldNS=newNS;

            if(counter==5)
                break;
        }
        double[] score =new double[2];
        score[0]=newSC;
        score[1]=newNS;
        return score;
	}
*/
    
    public static double[] getSemanticCoherenceNovelty(String entity1, String entity2) {
		//for first string
		//Calculating the keyphrases
		//E.g Numbers become Number, brought becomes bring
    	System.out.println("entered semantic coherence novelty");
		ArrayList<String> K1 = extractKeyPhrases(entity1);
		// get the semantic context of Context of keyPhrases.
		//i.e total number of neighboring nodes
		System.out.println("before getclosure");
		Node nodes_1[] = Primitives.getClosure(K1);
		System.out.println("after getclosure");
		long graph_1[][] = Primitives.getSemanticContext(nodes_1);

		//for second string
		ArrayList<String> K2 = extractKeyPhrases(entity2);

		Node nodes_2[] = Primitives.getClosure(K2);

		long graph_2[][] = Primitives.getSemanticContext(nodes_2);

		// combine nodes and remove duplicates
		ArrayList<Node> nodesCombined = new ArrayList<>(
                new LinkedHashSet<>(Arrays.asList(ArrayUtils.addAll(nodes_1, nodes_2))));

		int commonNode[] = new int[nodesCombined.size()];
		int novelNodeMarking[] = new int[nodesCombined.size()];

		for (Node n : nodes_2)
			commonNode[nodesCombined.indexOf(n)] = 1;
		
		
		
	
		//Nodes in entity 2 but not in entity 1
		ArrayList<Node> nodes1 = new ArrayList<>(
                new LinkedHashSet<>(Arrays.asList(ArrayUtils.addAll(nodes_1))));
		ArrayList<Node> nodes2 = new ArrayList<>(
                new LinkedHashSet<>(Arrays.asList(ArrayUtils.addAll(nodes_2))));    
                		
		ArrayList<Node> novelNodes = nodes2;
		for (Node n : nodes_2){
			if(nodes1.contains(n))
				novelNodeMarking[nodes2.indexOf(n)]= 0;
			else 
				novelNodeMarking[nodes2.indexOf(n)]= 1;
		}
		
		/*System.out.println("Printing nodes of entity 1");
		out.println("Printing nodes of entity 1");
		for(int i=0;i<nodes1.size();i++){
			out.println(nodes1.get(i).toString());
		}
		System.out.println("Printing nodes of entity 2");
		out.println("Printing nodes of entity 2");
		for(int i=0;i<nodes2.size();i++){
			
			out.println(nodes2.get(i).toString());
		}
		System.out.println("Printing novel nodes");
		out.println("Printing novel nodes");
		for(int i=0;i<novelNodes.size();i++){
			out.print(novelNodes.get(i) +" ");
		}*/
			
		/*System.out.println("Printing common nodes");
		for(int i=0;i<nodesCombined.size();i++){
			if(commonNode[nodesCombined.indexOf(i)]==1)
				System.out.println(novelNodes.get(i).toString());
		}*/
			

		// Semantic Coherence Context
		double transitionGraph[][] = Primitives.graphMerge(nodesCombined, nodes_1, nodes_2);
		// PrintFullMatrix(nodes_1, nodes_2, nodesCombined, graph_1, graph_2,
		// transitionGraph, nodeNames);

		// Convert edge weights to probabilities
		double ProbabilityMatrix[][] = getProbabilityMatrix(nodesCombined, transitionGraph);
		
		// Calculating Semantic Coherence Value using Random Walks
        //Implemented Stabilisation factor
        double ScSum = 0.0;
        double novelSum = 0.0;
        
        Random rand = new Random();
        boolean flag = false;
        int out1 = (Config.noOfRandomWalks/3);
        int matrixSize = transitionGraph.length;
        System.out.println(matrixSize);

        for (int j = 0; j < out1; j++)
        {
            int commonNodesVisited = 0, totalNodesVisited = 0, novelNodesVisited=0;
            int currentNode = rand.nextInt(matrixSize);

            for (int i = 0; i < transitionGraph.length; i++) {
                double[] aTransitionGraph = transitionGraph[i];
                if (commonNode[currentNode] == 1) 
                	commonNodesVisited++;
               
                if (novelNodeMarking[currentNode] == 1)
                	novelNodesVisited++;
                
                totalNodesVisited++;
                
                currentNode = getNextRandomNode(currentNode, ProbabilityMatrix, matrixSize);
                if (currentNode == 0) {
                    if (flag) break;
                    flag = true;
                }
            }
//             System.out.println(commonNodesVisited + " " + totalNodesVisited);
            ScSum += (double) commonNodesVisited / (double) totalNodesVisited;
            novelSum+=(double) novelNodesVisited / (double) totalNodesVisited;
        }
        System.out.println("ScSum"+ScSum);
        System.out.println("novelSum"+novelSum);
        double oldSC=ScSum / out1;
//        	System.out.println(oldSC);
        double oldNS=novelSum / out1;
        
        double newSC=0.0;
        double newNS=0.0;
        int counter=0;

        //stabilization code
        for (int j = out1; j < out1*3; j++)
        {
            int commonNodesVisited = 0, totalNodesVisited = 0, novelNodesVisited=0;
            int currentNode = rand.nextInt(matrixSize);

            for(int i = 0; i < transitionGraph.length; i++)
            {
                if (commonNode[currentNode] == 1)
                    commonNodesVisited++;
                if (novelNodeMarking[currentNode] == 1)
                	novelNodesVisited++;
                totalNodesVisited++;
                currentNode = getNextRandomNode(currentNode, ProbabilityMatrix, matrixSize);
                if(currentNode == 0)
                {
                    if(flag) break;
                    flag = true;
                }
            }

            ScSum += (double) commonNodesVisited / (double) totalNodesVisited;
            novelSum+=(double) novelNodesVisited / (double) totalNodesVisited;
            newSC=ScSum/(j+1);
//            System.out.println(newSC);
            newNS=novelSum/(j+1);
            if((newSC-oldSC)>0.01 || (oldSC-newSC)>0.01)
                counter++;
            else
                break;

            oldSC=newSC;
            oldNS=newNS;

            if(counter==5)
                break;
        }
        double[] score =new double[2];
        score[0]=newSC;
        score[1]=newNS;
        return score;
	}


	public static double getSemanticCoherence(String entity1, String entity2) {
		//for first string
		//Calculating the keyphrases
		//E.g Numbers become Number, brought becomes bring
		ArrayList<String> K1 = extractKeyPhrases(entity1);
		// get the semantic context of Context of keyPhrases.
		//i.e total number of neighboring nodes
	//	System.out.println("before getclosure");
		Node nodes_1[] = Primitives.getClosure(K1);
	//	System.out.println("after getclosure");
		long graph_1[][] = Primitives.getSemanticContext(nodes_1);

		//for second string
		ArrayList<String> K2 = extractKeyPhrases(entity2);

		Node nodes_2[] = Primitives.getClosure(K2);

		long graph_2[][] = Primitives.getSemanticContext(nodes_2);

		// combine nodes and remove duplicates
		ArrayList<Node> nodesCombined = new ArrayList<>(
                new LinkedHashSet<>(Arrays.asList(ArrayUtils.addAll(nodes_1, nodes_2))));

		int commonNode[] = new int[nodesCombined.size()];

		for (Node n : nodes_2)
			commonNode[nodesCombined.indexOf(n)] = 1;

		// Semantic Coherence Context
		double transitionGraph[][] = Primitives.graphMerge(nodesCombined, nodes_1, nodes_2);
		// PrintFullMatrix(nodes_1, nodes_2, nodesCombined, graph_1, graph_2,
		// transitionGraph, nodeNames);

		// Convert edge weights to probabilities
		double ProbabilityMatrix[][] = getProbabilityMatrix(nodesCombined, transitionGraph);
		
		// Calculating Semantic Coherence Value using Random Walks
        //Implemented Stabilisation factor
        double ScSum = 0.0;

        Random rand = new Random();
        boolean flag = false;
        int out = (Config.noOfRandomWalks/3);
        int matrixSize = transitionGraph.length;

        for (int j = 0; j < out; j++)
        {
            int commonNodesVisited = 0, totalNodesVisited = 0;
            int currentNode = rand.nextInt(matrixSize);

            for (int i = 0; i < transitionGraph.length; i++) {
                double[] aTransitionGraph = transitionGraph[i];
                if (commonNode[currentNode] == 1) commonNodesVisited++;
                totalNodesVisited++;
                currentNode = getNextRandomNode(currentNode, ProbabilityMatrix, matrixSize);
                if (currentNode == 0) {
                    if (flag) break;
                    flag = true;
                }
            }
//             System.out.println(commonNodesVisited + " " + totalNodesVisited);
            ScSum += (double) commonNodesVisited / (double) totalNodesVisited;
        }
        double oldSC=ScSum / out;
//        	System.out.println(oldSC);

        double newSC=0.0;
        int counter=0;

        //stabilization code
        for (int j = out; j < out*3; j++)
        {
            int commonNodesVisited = 0, totalNodesVisited = 0;
            int currentNode = rand.nextInt(matrixSize);

            for(int i = 0; i < transitionGraph.length; i++)
            {
                if (commonNode[currentNode] == 1)
                    commonNodesVisited++;
                totalNodesVisited++;
                currentNode = getNextRandomNode(currentNode, ProbabilityMatrix, matrixSize);
                if(currentNode == 0)
                {
                    if(flag) break;
                    flag = true;
                }
            }

            ScSum += (double) commonNodesVisited / (double) totalNodesVisited;

            newSC=ScSum/(j+1);
//            System.out.println(newSC);

            if((newSC-oldSC)>0.01 || (oldSC-newSC)>0.01)
                counter++;
            else
                break;

            oldSC=newSC;

            if(counter==5)
                break;
        }
        return newSC;
	}

    private static int getNextRandomNode(int currentNode,double[][] probabilityMatrix, int matrixSize)
    {
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
        //System.out.println("Error " + tempSum);
        return 0;
    }

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

	private static double[][] getProbabilityMatrix(ArrayList<Node> nodesCombined, double[][] transitionGraph) 
	{
		double newgraph[][] = new double[transitionGraph.length][transitionGraph.length];
		
		String nodeNames[] = Primitives.getNodeNames(nodesCombined);
		
		for (int i = 0; i < transitionGraph.length; i++) 
		{
			double count = 0;
			long c = 0;
//			c = Primitives.floodedWeight(nodeNames[i]);
//			System.out.println(nodeNames[i] + " " + c);
			for (int j = 0; j < transitionGraph.length; j++)
				c += transitionGraph[i][j];

			for (int j = 0; j < transitionGraph.length; j++)
			{
				//System.out.println( transitionGraph[i][j] );
//				newgraph[i][j] = transitionGraph[i][j] / c;
				//Smoothing factor
				newgraph[i][j] = (Config.DELTA + transitionGraph[i][j])
						/ (Config.DELTA * transitionGraph.length + c); //0 correction
				//System.out.println(newgraph[i][j]);
				//
				count+=newgraph[i][j];
				
			}
//			System.out.println(count);
//			System.exit(0);
		}
		return newgraph;
	}

	public static ArrayList<String> extractKeyPhrases(String line)
	{
		//return keyWordExtraction(cleanData(readJson(line)));
		return keyWordExtraction(cleanData((line)));
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
		
		//Splitting of the objects of sentences into its categories
		
		StanfordCoreNLP pipeline = new StanfordCoreNLP(new Properties() {
			{
				System.out.println("In keywordExtraction 1");
				setProperty("annotators", "tokenize,ssplit,pos,lemma");
			}
		});
		System.out.println("In keywordExtraction 2");
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
		System.out.println("In keywordExtraction 3");
		return new ArrayList<String>(new HashSet<String>(ans));
	}

	public static String cleanData(String line) {
		// Remove stop words, numericals, single letters, punctations
		
		BufferedReader brs = null;
		HashMap<String, Integer> stopWords = new HashMap<String, Integer>();
		try {
			//brs = new BufferedReader(new FileReader("/Users/Chaitali/Documents/workspace/SemanticCoherenceNarrativeArc/data/stopWords.txt"));
			brs = new BufferedReader(new FileReader(Config.STOP_WORDS));
			System.out.println("file - stop words found" );
			String lline;
			while ((lline = brs.readLine()) != null) 
				stopWords.put(lline.trim(), 1);
			
			
			brs.close();
		} 
		catch (IOException e) {
			//System.out.println("error in cleandata");
			e.printStackTrace();
		}

		Locale locale1 = new Locale("en");
		String[] temp = line.trim().split("\\W+");
		
		String templine = "";
		for (int i = 0; i < temp.length; i++) {
			if (stopWords.containsKey(temp[i].toString().toLowerCase(locale1)) != true
					&& (isNotWord(temp[i].toString()) != true)) {
				templine = templine.concat(temp[i].toString().toLowerCase()	+ " ");
			}
		}
		return templine.trim();
	}

	public static boolean isNotWord(String input) {
		Pattern pattern = Pattern.compile(".*[0-9].*");
		Pattern pattern3 = Pattern.compile("(\\W)*");
		Pattern singleLetter = Pattern.compile("\\w");
		Pattern html1 = Pattern.compile("__*");
		Pattern html2 = Pattern.compile("&*;");
		Pattern html3 = Pattern.compile("\\[.*.\\]");
		if (pattern.matcher(input).matches()
				|| pattern3.matcher(input).matches()
				|| singleLetter.matcher(input).matches() || html1.matcher(input).matches() || html2.matcher(input).matches() || html3.matcher(input).matches() ) {
			return true;
		}
		return false;
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

	/*	
	public static LinkedList<Entry<String, Integer>> getSemanticCoherenceAnalysis(
			String entity1) {
		// Manual Random Walks

		Map<String, Integer> counter = new HashMap<String, Integer>();

		ArrayList<String> K1 = extractKeyPhrases(entity1);
		// get the semantic context of Context of keyPhrases.
		Node nodes_1[] = null;
		nodes_1 = Primitives.getClosure(K1);
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

//		// PrintFullMatrix(nodes_1, nodes_2, nodesCombined, graph_1, graph_2,
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

*/
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


}
