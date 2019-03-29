package com.na.cgraph;

import org.neo4j.cypher.internal.ExecutionResult;
import org.neo4j.cypher.internal.javacompat.ExecutionEngine;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.Result;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.io.fs.FileUtils;

import static org.neo4j.graphdb.RelationshipType.withName;

@SuppressWarnings("unused")
public class CreateGraph {

	static GraphDatabaseService graphDb;

	@SuppressWarnings({ "resource" })
	public static void createGraph(String fileName) {
		try {
			FileUtils.deleteRecursively(new File(Config.DB_PATH));

			graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(new File(
					Config.DB_PATH));
			BufferedReader br = new BufferedReader(new FileReader(fileName));

			final RelationshipType COOCCURS = withName("cooccurs");
			Label l = Label.label("word");

			// Get all the possible words and put them in a string
			Set<String> tempWords = new HashSet<String>();
			int counter1 = 1;
			String line = "";
			while ((line = br.readLine()) != null) {
				String[] splited = line.split("\\s+");
				for (String s : splited)
					tempWords.add(s);
			}

			ArrayList<Node> nodes = new ArrayList<Node>();
			ArrayList<String> words = new ArrayList<String>(tempWords);
			// Populate the above two arraylists
			Transaction tx = graphDb.beginTx();
			try {
				for (String s : words) {
					Node n = graphDb.createNode(l);
					nodes.add(n);
					n.setProperty("name", s);
				}
				tx.success();
			} finally {
				tx.close();
			}

			System.out.println("Going for TC graph");
			br = new BufferedReader(new FileReader(fileName));
			while ((line = br.readLine()) != null) {
				String[] splited = line.split("\\s+");
				tx = graphDb.beginTx();
				try {
					String str = "MATCH (A), (B) "
							+ "WHERE A.name IN {names1} AND B.name IN {names2} AND A.name > B.name "
							+ "CREATE UNIQUE (A)-[r:COOCCURS]-(B) "
							+ "SET r.weight = coalesce(r.weight, 0) + 1";
					Map<String, Object> params = new HashMap<>();
					// add some names to the names list
					params.put("names1", splited);
					params.put("names2", splited);
					graphDb.execute(str, params);
					tx.success();
				} finally {
					tx.close();
				}
			}
			// test();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "resource" })
	public static void createGraph_WithoutCypher(String fileName) {
		try {
			FileUtils.deleteRecursively(new File(Config.DB_PATH));

			graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(new File(
					Config.DB_PATH));
			BufferedReader br = new BufferedReader(new FileReader(fileName));

			final RelationshipType COOCCURS = withName("cooccurs");
			Label l = Label.label("word");

			// Get all the possible words and put them in a string
			Set<String> tempWords = new HashSet<String>();
			int counter1 = 1;
			String line = "";
			while ((line = br.readLine()) != null) {
				String[] splited = line.split("\\s+");
				for (String s : splited)
					tempWords.add(s);
			}

			ArrayList<Node> nodes = new ArrayList<Node>();
			ArrayList<String> words = new ArrayList<String>(tempWords);
			// Populate the above two arraylists
			Transaction tx = graphDb.beginTx();
			try {
				for (String s : words) {
					Node n = graphDb.createNode(l);
					nodes.add(n);
					n.setProperty("name", s);
				}
				tx.success();
			} finally {
				tx.close();
			}

			// Creates mapping between index and word
			Map<String, Integer> table = new HashMap<String, Integer>();
			for (int c = 0; c < words.size(); c++)
				table.put(words.get(c), c);

			// Update Relationships
			System.out.println("Comes here");
			br = new BufferedReader(new FileReader(fileName));
			while ((line = br.readLine()) != null) {
				String[] splited = line.split("\\s+");
				Set<String> toAdd = new HashSet<String>();
				Set<String> left = new HashSet<String>();
				for (String v : splited)
					toAdd.add(v);

				for (String s : splited) {
					tx = graphDb.beginTx();
					try {
						Node start = nodes.get(table.get(s));
						String name1 = (String) start.getProperty("name");
						for (Relationship r : start.getRelationships()) {
							String name2 = (String) r.getOtherNode(start)
									.getProperty("name");
							if ((name1.compareTo(name2) < 0)
									&& toAdd.contains(name2)) {
								r.setProperty("weight",
										((Integer) r.getProperty("weight")) + 1);
								toAdd.remove(name2);
								left.add(name2);
							}
						}
						for (String v : toAdd) {
							if (s.compareTo(v) < 0)// s comes lexicographically
													// before v
							{
								Relationship rep = start.createRelationshipTo(
										nodes.get(table.get(v)), COOCCURS);
								rep.setProperty("weight", 1);
							}
						}
						toAdd.addAll(left);
						left.clear();
						tx.success();
					} finally {
						tx.close();
					}
				}
			}

			test();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void test() {
		Transaction tx = graphDb.beginTx();
		try {
			for (Relationship r : graphDb.getAllRelationships()) {
				System.out.println(r.getStartNode().getProperty("name") + " : "
						+ r.getEndNode().getProperty("name") + " "
						+ r.getProperty("weight"));
			}
			tx.success();
		} finally {
			tx.close();
		}
	}

	public static void cypherTest() {
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(new File(
				Config.DB_PATH));

		// String relationStr =
		// "MATCH (a{name: \"text\"})-[r:COOCCURS]-(b{name: \"exponent\"}) return r"
		// ;
		// String relationStr =
		// "MATCH (a{name: \"text\"})-[r:COOCCURS]-(b) return b" ;
		String relationStr = "MATCH (a{name: \"text\"}) return a";
		// Result execResult =
		// graphDb.execute("MATCH (n) where n.name = \"relation\" RETURN count(n)");
		int rweight = 0;
		// System.out.println(relationStr);
		try (Transaction tx = graphDb.beginTx()) {
			Result execResult = graphDb.execute(relationStr);
			// System.out.println(execResult.getQueryStatistics());

			Iterator<Object> iter = execResult.columnAs("a");
			while (iter.hasNext()) {

				// rweight= Integer.valueOf(nnode.next().toString());
				Node nnode = (Node) iter.next();
				Iterable<Relationship> rel = nnode.getRelationships();
				// System.out.println( rel.toString());

			}
			tx.close();
		}
		// CreateGraph.shutDown();

	}

	static void shutDown() {
		System.out.println();
		System.out.println("Shutting down database ...");
		// START SNIPPET: shutdownServer
		graphDb.shutdown();
		// END SNIPPET: shutdownServer
	}
}
