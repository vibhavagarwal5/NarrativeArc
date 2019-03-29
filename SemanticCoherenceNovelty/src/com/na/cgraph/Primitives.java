package com.na.cgraph;

import java.util.*;
import java.io.File;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import com.na.sc.SemanticCoherence.Pair;

//./neo4j-shell -path Project/NarrativeArc/cooccurrencegraph.db

public class Primitives {

	public static GraphDatabaseService graphDb = new GraphDatabaseFactory()
			.newEmbeddedDatabase(new File(Config.DB_PATH));
	
	
	public static double[][] graphMerge(ArrayList<Node> nodesCombined, Node[] nodes_1, Node[] nodes_2) 
	{
		// In this Method, the default value is the value in the original graph.
		// If a edge occurs in both the subgraphs then that edge
		// weight is doubled.
		int n = nodesCombined.size();
		double newgraph[][] = new double[n][n];
		double mark[][] = new double[n][n];
			
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++)
				mark[i][j] = 0.0;
		
		for (int i = 0; i < nodes_1.length; i++)
			for (int j = 0; j < nodes_1.length; j++)
				mark[i][j]++;
		
		for (int i = 0; i < nodes_2.length; i++)
			for (int j = 0; j < nodes_2.length; j++)
				mark[nodesCombined.indexOf((nodes_2[i]))][nodesCombined.indexOf((nodes_2[j]))]++;
		
		for (int i = 0; i < mark.length; i++)
			for (int j = 0; j < mark.length; j++) 
			{
//				if (mark[i][j] < 2.0)
//					mark[i][j] = 1.0;
//				else
//					mark[i][j] = 2.0;

				if ( mark[i][j] <= 2.0 && mark[i][j] > 0 )
						mark[i][j] = 1.0;

			}
		for (int i = 0; i < n; i++) 
		{			
			try(Transaction tx = graphDb.beginTx();)
			{
				Iterator<Relationship> iterator = nodesCombined.get(i).getRelationships().iterator();
				while (iterator.hasNext()) 
				{
					Relationship rel = iterator.next(); 
					
					Node start = rel.getStartNode();
					Node end = rel.getEndNode();
					if (nodesCombined.contains(end)&& nodesCombined.contains(start)) 
					{
						Long weight_multiplier = (long) rel.getProperty("weight");
						newgraph[nodesCombined.indexOf(end)][nodesCombined.indexOf(start)] *= weight_multiplier;
						newgraph[nodesCombined.indexOf(start)][nodesCombined.indexOf(end)] *= weight_multiplier;
					}
				}
				tx.success();
			} 
		}
		return newgraph;
	}

	public static String[] getNodeNames(ArrayList<Node> nodesCombined) 
	{
		int n = nodesCombined.size();
		String nodeNames[] = new String[n];
		for (int i = 0; i < n; i++) 
		{			
			try(Transaction tx = graphDb.beginTx();)
			{
				nodeNames[i] = (String) (nodesCombined.get(i).getProperty("name"));
				tx.success();
			}
		}
		return nodeNames;
	}
	
	public static Node[] getClosure(ArrayList<String> focii) 
	{
		Set<Node> ans = new HashSet<Node>();
		String str = "MATCH (focus)-[e:COOCCURS]-(neighbor)"
				+ "WHERE focus.name in {focii} or neighbor.name in {focii} "
				+ "return DISTINCT(neighbor)";
		Map<String, Object> params = new HashMap<>();
		params.put("focii", focii);
		Result r = graphDb.execute(str, params);
		//System.out.println(r.resultAsString());
		Iterator<Object> p = r.columnAs("neighbor");
		
		while (p.hasNext())
		{
			Node e = (Node) p.next();
			ans.add(e);
		}
		
		return ans.toArray(new Node[0]);
	}

	public static Node[] getContext(ArrayList<String> focii) {
		Set<Node> ans = new HashSet<Node>();
		String str = "MATCH (focus)-[e:COOCCURS]-(neighbor)"
				+ "WHERE focus.name in {focii} or neighbor.name in {focii} "
				+ "return DISTINCT(neighbor)";
		Map<String, Object> params = new HashMap<>();
		params.put("focii", focii);
		Result r = graphDb.execute(str, params);
		Iterator<Object> p = r.columnAs("neighbor");
		while (p.hasNext()) {
			Node e = (Node) p.next();
			ans.add(e);
		}
		return ans.toArray(new Node[0]);
	}

	public static long[][] getSemanticContext(Node nodes[]) {
		long Pans[][] = new long[1 + nodes.length][nodes.length + 1];
		Map<Node, Integer> mp = new HashMap<Node, Integer>();
		for (int i = 0; i < nodes.length; i++)
			mp.put(nodes[i], i);
		for (Node val : nodes) {
			Transaction tx = graphDb.beginTx();
			try {
				Iterator<Relationship> relIt = val.getRelationships()
						.iterator();
				while (relIt.hasNext()) {
					Relationship rel = relIt.next();
					if (mp.containsKey(rel.getEndNode())
							&& mp.containsKey(rel.getStartNode())) {
						Pans[mp.get(rel.getEndNode())][mp.get(rel
								.getStartNode())] = (long) rel
								.getProperty("weight");
						Pans[mp.get(rel.getStartNode())][mp.get(rel
								.getEndNode())] = (long) rel
								.getProperty("weight");
					}
				}
				tx.success();
			} finally {
				tx.close();
			}
		}
		return Pans;
	}

	// Please ensure A and B are square matrix of same dimension
	public static double[][] MatMult(double[][] A, double[][] B) {
		int n = A.length;
		double[][] ans = new double[n][n];
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++)
				for (int k = 0; k < n; k++)
					ans[i][j] += (A[i][k] * B[k][j]);
		return ans;
	}

	public static double[] MatMult(double[][] A, double[] B) {
		int n = A.length;
		double[] ans = new double[n];
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++)
				ans[i] += (A[i][j] * B[j]);
		return ans;
	}

	public static double[] MatMult(double[] A, double[][] B) {
		int n = A.length;
		double[] ans = new double[n];
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++)
				ans[i] += (A[i] * B[i][j]);
		return ans;
	}

	public static double[] VectorAdd(double[] A, double[] B) {
		int n = A.length;
		double[] ans = new double[n];
		for (int i = 0; i < n; i++)
			ans[i] = A[i] + B[i];
		return ans;
	}

	public static double MatMult(double[] A, double[] B) {
		int n = A.length;
		double ans = 0.0;
		for (int i = 0; i < n; i++)
			ans += (A[i] * B[i]);
		return ans;
	}

	public static double[][] graphMerge(Node[] nodesCombined, long[][] graph_1,
			long[][] graph_2, Node[] nodes_1, Node[] nodes_2) {
		// In this Method, the default value is the value in the original graph.
		// If a edge occurs in both the subgraphs then that edge
		// weight is doubled.

		double newgraph[][] = new double[nodesCombined.length][nodesCombined.length];
		double mark[][] = new double[nodesCombined.length][nodesCombined.length];
		HashMap<Node, Integer> n1 = new HashMap<Node, Integer>();
		for (int i = 0; i < nodesCombined.length; i++)
			n1.put(nodesCombined[i], i);
		for (int i = 0; i < nodesCombined.length; i++)
			for (int j = 0; j < nodesCombined.length; j++)
				mark[i][j] = 0.0;
		for (int i = 0; i < nodes_1.length; i++)
			for (int j = 0; j < nodes_1.length; j++)
				mark[i][j]++;
		for (int i = 0; i < nodes_2.length; i++)
			for (int j = 0; j < nodes_2.length; j++)
				mark[n1.get(nodes_2[i])][n1.get(nodes_2[j])]++;
		for (int i = 0; i < mark.length; i++)
			for (int j = 0; j < mark.length; j++) {
				if (mark[i][j] < 2.0)
					mark[i][j] = 1.0;
				else
					mark[i][j] = 2.0;
			}
		for (int i = 0; i < nodesCombined.length; i++) {
			Transaction tx = graphDb.beginTx();
			try {
				Iterator<Relationship> relIt = nodesCombined[i]
						.getRelationships().iterator();
				while (relIt.hasNext()) {
					Relationship rel = relIt.next();
					if (n1.containsKey(rel.getEndNode())
							&& n1.containsKey(rel.getStartNode())) {
						newgraph[n1.get(rel.getEndNode())][n1.get(rel
								.getStartNode())] = mark[n1.get(rel
								.getEndNode())][n1.get(rel.getStartNode())]
								* (long) rel.getProperty("weight");
						newgraph[n1.get(rel.getStartNode())][n1.get(rel
								.getEndNode())] = mark[n1.get(rel
								.getStartNode())][n1.get(rel.getEndNode())]
								* (long) rel.getProperty("weight");
					}
				}
				tx.success();
			} finally {
				tx.close();
			}
		}
		return newgraph;
	}

	public static String[] getNodeNames(Node[] nodesCombined) {
		String nodeNames[] = new String[nodesCombined.length];
		for (int i = 0; i < nodesCombined.length; i++) {
			Transaction tx = graphDb.beginTx();
			try {
				nodeNames[i] = (String) (nodesCombined[i].getProperty("name"));
				tx.success();
			} finally {
				tx.close();
			}
		}
		return nodeNames;
	}

	public static void print(ArrayList<Node> nodesCombined2) {
		for (Node n : nodesCombined2) {
			Transaction tx = graphDb.beginTx();
			try {
				System.out.println(n.getProperty("name"));
				tx.success();
			} finally {
				tx.close();
			}
		}

	}

	public static double[][] graphMultiplyNormalized(Pair[] nodesCombined,
			long[][] graph_1, long[][] graph_2, Map<String, Integer> g1_nodes,
			Map<String, Integer> g2_nodes) {
		System.out.println("length : " + nodesCombined.length);
		double newgraph[][] = new double[nodesCombined.length][nodesCombined.length];
		for (int i = 0; i < nodesCombined.length; i++)
			for (int j = 0; j < nodesCombined.length; j++) {
				String n1_1 = nodesCombined[i].first, n1_2 = nodesCombined[i].second, n2_1 = nodesCombined[j].first, n2_2 = nodesCombined[j].second;
				newgraph[i][j] = Config.NormalizationOffset
						+ (double) (graph_1[g1_nodes.get(n1_1)][g1_nodes
								.get(n2_1)] * graph_2[g2_nodes.get(n1_2)][g2_nodes
								.get(n2_2)]);
			}
		return newgraph;
	}

	public static Map<String, Integer> getMappingToNumbers(Node[] nodes_1) {
		HashMap<String, Integer> g1_nodes = new HashMap<String, Integer>();
		Transaction tx = graphDb.beginTx();
		try {
			for (int c = 0; c < nodes_1.length; c++)
				g1_nodes.put((String) nodes_1[c].getProperty("name"), c);
			tx.success();
		} finally {
			tx.close();
		}
		return g1_nodes;
	}

	public static ArrayList<Pair> CombineNodesToPair(Node[] nodes_1,
			Node[] nodes_2) {
		ArrayList<Pair> nodesCombinedTemp = new ArrayList<Pair>();
		Transaction tx = graphDb.beginTx();
		try {
			for (Node i : nodes_1)
				for (Node j : nodes_2)
					if (!(i.equals(j)))
						nodesCombinedTemp.add(new Pair(i.getProperty("name"), j
								.getProperty("name")));
			tx.success();
		} finally {
			tx.close();
		}
		return nodesCombinedTemp;
	}
}
