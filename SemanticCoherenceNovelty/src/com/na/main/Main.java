package com.na.main;

//not useful

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;

import javax.swing.JOptionPane;

import com.na.cgraph.Config;
import com.na.cgraph.CreateGraph;
import com.na.cgraph.PreProcessText;
import com.na.sc.SemanticCoherence;

import au.com.bytecode.opencsv.CSVReader;


public class Main {

	private static String path = "./src/main/resources/";

	public static void main(String[] args) throws IOException {

		Config.loadConfigs();
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("AllPairs")){
        		//reading collection and random collection files
				 //reading collection and random collection files
				
	            BufferedReader br1 = new BufferedReader(new FileReader("collection/Collection1"));
	            BufferedReader br2 = new BufferedReader(new FileReader("collection/Collection2"));
	            BufferedReader br3 = new BufferedReader(new FileReader("collection/Collection3"));
	            BufferedReader br4 = new BufferedReader(new FileReader("collection/Collection4"));
	            BufferedReader br5 = new BufferedReader(new FileReader( "collection/Collection5"));
	            BufferedReader br6 = new BufferedReader(new FileReader("collection/Collection6"));
	            BufferedReader br7 = new BufferedReader(new FileReader("collection/Collection7"));
	            BufferedReader br8 = new BufferedReader(new FileReader("collection/Collection8"));
	            BufferedReader rc1 = new BufferedReader(new FileReader("collection/RandomCollection1"));

	            //Collection StringBuilder array
	            StringBuilder[] sb1 = new StringBuilder[br1.lines().toArray().length];
	            br1 = new BufferedReader( new FileReader("collection/Collection1") );
	            StringBuilder[] sb2 = new StringBuilder[br2.lines().toArray().length];
	            br2 = new BufferedReader( new FileReader( "collection/Collection2") );
	            StringBuilder[] sb3 = new StringBuilder[br3.lines().toArray().length];
	            br3 = new BufferedReader( new FileReader( "collection/Collection3") );
	            StringBuilder[] sb4 = new StringBuilder[br4.lines().toArray().length];
	            br4 = new BufferedReader( new FileReader( "collection/Collection4") );
	            StringBuilder[] sb5 = new StringBuilder[br5.lines().toArray().length];
	            br5 = new BufferedReader( new FileReader( "collection/Collection5") );
	            StringBuilder[] sb6 = new StringBuilder[br6.lines().toArray().length];
	            br6 = new BufferedReader( new FileReader( "collection/Collection6") );
	            StringBuilder[] sb7 = new StringBuilder[br7.lines().toArray().length];
	            br7 = new BufferedReader( new FileReader( "collection/Collection7") );
	            StringBuilder[] sb8 = new StringBuilder[br8.lines().toArray().length];
	            br8 = new BufferedReader( new FileReader( "collection/Collection8") );

                //Random Collection StringBuilder array
                StringBuilder[] rndm1 = new StringBuilder[rc1.lines().toArray().length];
                rc1 =  new BufferedReader( new FileReader( "collection/RandomCollection1") );

                //separating each learning resources of each collection
                for(int j = 0; j < sb1.length; j++)
                    sb1[j] = new StringBuilder(br1.readLine());
                for(int j = 0; j < sb2.length; j++)
                    sb2[j] = new StringBuilder(br2.readLine());
                for(int j = 0; j < sb3.length; j++)
                    sb3[j] = new StringBuilder(br3.readLine());
                for(int j = 0; j < sb4.length; j++)
                    sb4[j] = new StringBuilder(br4.readLine());
                for(int j = 0; j < sb5.length; j++)
                    sb5[j] = new StringBuilder(br5.readLine());
                for(int j = 0; j < sb6.length; j++)
                    sb6[j] = new StringBuilder(br6.readLine());
                for(int j = 0; j < sb7.length; j++)
                    sb7[j] = new StringBuilder(br7.readLine());
                for(int j = 0; j < sb8.length; j++)
                    sb8[j] = new StringBuilder(br8.readLine());


//                pairCoherenceALL(sb1);
//                pairCoherenceALL(sb2);
				pairCoherenceAndNovelty(sb1);
				pairCoherenceAndNovelty(sb2);
				pairCoherenceAndNovelty(sb3);
				pairCoherenceAndNovelty(sb4);
				pairCoherenceAndNovelty(sb5);
				pairCoherenceAndNovelty(sb6);
				pairCoherenceAndNovelty(sb7);
				pairCoherenceAndNovelty(sb8);




        	}else if(args[0].equalsIgnoreCase("pairCoherenceAndNovelty")){
        		System.out.println("in coherence and novelty");
				/* BufferedReader br1 = new BufferedReader(new FileReader("collection/Collection2"));
		            

		            //Collection StringBuilder array
		            StringBuilder[] sb1 = new StringBuilder[br1.lines().toArray().length];
		            br1 = new BufferedReader( new FileReader("collection/Collection2") );
		            for(int j = 0; j < sb1.length; j++)
		                sb1[j] = new StringBuilder(br1.readLine());*/
		            pairCoherenceAndNovelty();
				
			} else if (args[0].equalsIgnoreCase("create")) {
				System.out.println("Cerating Term Co-occurence graph");
				// Text preprocessing
//				 PreProcessText.readJson();
				 PreProcessText.cleanData();
				PreProcessText.keyWordExtraction();
				// Creating co-occurence graph
				CreateGraph.createGraph(Config.KEYWORDS_PATH);

			} else if (args[0].equalsIgnoreCase("outOfSequence")){
				
	            //reading collection and random collection files
	            BufferedReader br1 = new BufferedReader(new FileReader("collection/Collection1"));
	            BufferedReader br2 = new BufferedReader(new FileReader("collection/Collection2"));
	            BufferedReader br3 = new BufferedReader(new FileReader("collection/Collection3"));
	            BufferedReader br4 = new BufferedReader(new FileReader("collection/Collection4"));
	            BufferedReader br5 = new BufferedReader(new FileReader( "collection/Collection5"));
	            BufferedReader br6 = new BufferedReader(new FileReader("collection/Collection6"));
	            BufferedReader br7 = new BufferedReader(new FileReader("collection/Collection7"));
	            BufferedReader br8 = new BufferedReader(new FileReader("collection/Collection8"));
	           BufferedReader rc1 = new BufferedReader(new FileReader("collection/RandomCollection1"));

	            //Collection StringBuilder array
	            StringBuilder[] sb1 = new StringBuilder[br1.lines().toArray().length];
	            br1 = new BufferedReader( new FileReader("collection/Collection1") );
	            StringBuilder[] sb2 = new StringBuilder[br2.lines().toArray().length];
	            br2 = new BufferedReader( new FileReader( "collection/Collection2") );
	            StringBuilder[] sb3 = new StringBuilder[br3.lines().toArray().length];
	            br3 = new BufferedReader( new FileReader( "collection/Collection3") );
	            StringBuilder[] sb4 = new StringBuilder[br4.lines().toArray().length];
	            br4 = new BufferedReader( new FileReader( "collection/Collection4") );
	            StringBuilder[] sb5 = new StringBuilder[br5.lines().toArray().length];
	            br5 = new BufferedReader( new FileReader( "collection/Collection5") );
	            StringBuilder[] sb6 = new StringBuilder[br6.lines().toArray().length];
	            br6 = new BufferedReader( new FileReader( "collection/Collection6") );
	            StringBuilder[] sb7 = new StringBuilder[br7.lines().toArray().length];
	            br7 = new BufferedReader( new FileReader( "collection/Collection7") );
	            StringBuilder[] sb8 = new StringBuilder[br8.lines().toArray().length];
	            br8 = new BufferedReader( new FileReader( "collection/Collection8") );

	            //Random Collection StringBuilder array
	            StringBuilder[] rndm1 = new StringBuilder[rc1.lines().toArray().length];
	            rc1 =  new BufferedReader( new FileReader( "collection/RandomCollection1") );

	            //separating each learning resources of each collection
	            for(int j = 0; j < sb1.length; j++)
	                sb1[j] = new StringBuilder(br1.readLine());
	            for(int j = 0; j < sb2.length; j++)
	                sb2[j] = new StringBuilder(br2.readLine());
	            for(int j = 0; j < sb3.length; j++)
	                sb3[j] = new StringBuilder(br3.readLine());
	            for(int j = 0; j < sb4.length; j++)
	                sb4[j] = new StringBuilder(br4.readLine());
	            for(int j = 0; j < sb5.length; j++)
	                sb5[j] = new StringBuilder(br5.readLine());
	            for(int j = 0; j < sb6.length; j++)
	                sb6[j] = new StringBuilder(br6.readLine());
	            for(int j = 0; j < sb7.length; j++)
	                sb7[j] = new StringBuilder(br7.readLine());
	            for(int j = 0; j < sb8.length; j++)
	                sb8[j] = new StringBuilder(br8.readLine());

	            //separating each learning resources of random collection
	            for(int j = 0; j < rndm1.length; j++)
	                rndm1[j] = new StringBuilder(rc1.readLine());

	            /* pairCoherence(sb) is a function that calculates the semantic coherence score between consecutive
	               learning resources pairs (e.g. (LA1,LA2),(LA2,LA3),.....)  using Graph Kernel method
	            */

	            //For getting output of pairwise semantic score for each collection
	           // pairCoherenceOutOfSync(sb1);
	          /*  pairCoherenceOutOfSync(sb2);
	            pairCoherenceOutOfSync(sb3);
	            pairCoherenceOutOfSync(sb4);
	            pairCoherenceOutOfSync(sb5);
	            pairCoherenceOutOfSync(sb6);
	            pairCoherenceOutOfSync(sb7);
	            pairCoherenceOutOfSync(sb8);*/
	            
	            pairCoherenceALL(sb1);
			}
			
			else if (args[0].equalsIgnoreCase("add")) {
				System.out.println("Adding entity to co-occurence graph");

				CreateGraph.cypherTest();
			} else if (args[0].equalsIgnoreCase("coherence")) {
				/*
				 * The learning activities need to be in JSON format. Please
				 * refer to the sample input for details. Strictly follow the
				 * format.
				 */
				System.out.println("Checking semantic coherence");
				String string1 = JOptionPane.showInputDialog(null,
						"Enter the first String");
				String string2 = JOptionPane.showInputDialog(null,
						"Enter the Second String");
				double SC = SemanticCoherence.getSemanticCoherence(string1,
						string2);
				JOptionPane.showMessageDialog(null,
						"The Semantic Coherence is :" + SC);
			} else if (args[0].equalsIgnoreCase("debug")) {
				/*
				 * debug mode takes two or three file names as input. The last
				 * filename is the name of the output file generated. If only
				 * one file name is given, Debug reads learning activities from
				 * that file and calculated SC between every two *consecutive*
				 * Learning Activities. If two input files are given, Debug
				 * generates SC between every two corresponding learning
				 * activity taken in order from the two input files. The
				 * learning activities need to be in JSON format. Please refer
				 * to the sample input for details. Strictly follow the format.
				 */
				// TODO Evaluate for all pairs of learning activities instead of
				// the sequential pairing.
				FileWriter writer = new FileWriter(args[3]);
				try (BufferedReader br = new BufferedReader(new FileReader(
						args[1]))) {
					String string1, string2;
					while ((string1 = br.readLine()) != null
							&& string1.length() > 0) {
						BufferedReader br2 = new BufferedReader(new FileReader(
								args[2]));
						while ((string2 = br2.readLine()) != null
								&& string2.length() > 0) {
							// input string to be a JSON format. This is to
							// verify
							// that its encompassed within []
							if (string1.charAt(0) != '[')
								string1 = "[" + string1 + "]";
							if (string2.charAt(0) != '[')
								string2 = "[" + string2 + "]";
							
							if(!string1.equals(string2)){
							double SC = SemanticCoherence.getSemanticCoherence(
									string1, string2);
							

							
								ArrayList<String> K1 = SemanticCoherence
										.extractKeyPhrases(string1);
								ArrayList<String> K2 = SemanticCoherence
										.extractKeyPhrases(string2);

								String s1 = "", s2 = "";
								for (String s : K1)
									s1 += (s + " ");
								for (String s : K2)
									s2 += (s + " ");
								writer.write("\n" + s1 + "\n" + s2
										+ "\n---->The Semantic Coherence is : "
										+ SC);
								System.out.println(s1 + "\n" + s2
										+ "\n---->The Semantic Coherence is : "
										+ SC);
							
						}
						}
						br2.close();
					}
					br.close();
				}
				writer.close();
		} 
			else if (args[0].equalsIgnoreCase("SinglePair")){
				try(FileWriter fw = new FileWriter("./data/pairwise/singlepairwise", false);
			           BufferedWriter bw = new BufferedWriter(fw);
        			   PrintWriter out = new PrintWriter(bw)){
					String str1 =args[1];
					String str2 = args[2];
					double[] scores;
					System.out.println(str1);
					System.out.println(str2);
					scores = SemanticCoherence.getSemanticCoherenceNovelty(str1.toString(),str2.toString());
					out.write(" "+scores[0]+"\n");
					out.write(" "+scores[1]+"\n");
					System.out.println(scores[0]);
					System.out.println(scores[1]);
				}catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} 
	}
			//else if (args[0].equalsIgnoreCase("scg")) {
//
//				//Option scg outputs semantic context graph given list of LA's
//				/*
//				 * Analyze mode takes two file names as input. Reads the first
//				 * LA only from the first filename and prints the analysis in
//				 * the second filename (output file) Analysis is nodes printed
//				 * in decreasing order of number of times visited in the Entity
//				 * Term Graph after Random Walks.
//				 */
//				FileWriter writer = new FileWriter(args[2]);
//				try (BufferedReader br = new BufferedReader(new FileReader(
//						args[1]))) {
//					String string1;
//					while ((string1 = br.readLine()) != null){
//					
//					List<Entry<String, Integer>> list = SemanticCoherence_original.getSemanticCoherenceAnalysis(string1);
//
//					for (Entry<String, Integer> i : list) {
//						if(i.getValue()>20){
//						//writer.write(i.getKey() + " - " + i.getValue());
//						//System.out.println(i.getKey() + " - " + i.getValue());
//						writer.write(i.getKey() +" ");
//						System.out.println(i.getKey() +" ");
//						}
//						
//					}
//						writer.write("\n");
//					}
//						writer.close();
//				}}
//				else if (args[0].equalsIgnoreCase("analyze")) {
//
//					/*
//					 * Analyze mode takes two file names as input. Reads the first
//					 * LA only from the first filename and prints the analysis in
//					 * the second filename (output file) Analysis is nodes printed
//					 * in decreasing order of number of times visited in the Entity
//					 * Term Graph after Random Walks.
//					 */
//					
//					try (BufferedReader br = new BufferedReader(new FileReader(
//							args[1]))) {
//						FileWriter writer = null;
//					    String string1;
//					    String filename;
//						while ((string1 = br.readLine()) != null){
//							int count=1;
//							filename = args[2]+count;
//							writer = new FileWriter(filename);
//							System.out.println(filename);
//							count++;
//						//String string1 = br.readLine();
//						List<Entry<String, Integer>> list = SemanticCoherence
//								.getSemanticCoherenceAnalysis(string1);
//
//						for (Entry<String, Integer> i : list) {
//							writer.write("\n" + i.getKey() + " - " + i.getValue());
//							System.out.println(i.getKey() + " - " + i.getValue());
//						}
//						writer.close();
//					}
//					
//					}
//				} 
			else {
				System.out
						.println("Please enter one of these options - \n create, cohereence, debug, analyze, scg");
			}
			
			
		}
	}
	
	private static void pairCoherenceALL(StringBuilder[] sb1) {
    	/* This function calculates the coherence score between consecutive
        learning resources pairs (e.g. (LA1,LA2),(LA2,LA3),.....)

        SemanticCoherence.getSemanticCoherence(LA1,LA2) finds coherence score between LA1 and LA2
        using Graph Kernel approach
     */

     //writing each coherence score in CollectionOutput file
     try(FileWriter fw = new FileWriter("data/output/AllPairsGraphKernel", true);
         BufferedWriter bw = new BufferedWriter(fw);
         PrintWriter out = new PrintWriter(bw))
     {
         double SC;
         int count = 0;
         double sum = 0;
         int n=sb1.length - 1;
         for(int i = 0; i < n; i++)
         {
        	 for(int j = 0; j < n; j++)
             {
        		 if(i!=j){
	             SC = SemanticCoherence.getSemanticCoherence(sb1[i].toString(), sb1[j].toString());
	             out.write(" "+i+" "+j+" "+SC+"\n");
	             System.out.println(SC);
	             count++;
	             sum+=SC;
        		 }
             }
         }
        // out.println("\nMean is "+ sum/count +"\n"+"*******************************"+"\n");
     }
     catch (IOException e) {
         //exception handling
     }
 
    }
    
	
	private static void pairCoherenceOutOfSync(StringBuilder[] sb1) {
    	/* This function calculates the coherence score between consecutive
        learning resources pairs (e.g. (LA1,LA2),(LA2,LA3),.....)

        SemanticCoherence.getSemanticCoherence(LA1,LA2) finds coherence score between LA1 and LA2
        using Graph Kernel approach
     */

     //writing each coherence score in CollectionOutput file
     try(FileWriter fw = new FileWriter("data/output/OutOfSyncGraphKernel", true);
         BufferedWriter bw = new BufferedWriter(fw);
         PrintWriter out = new PrintWriter(bw))
     {
         double SC;
         int count = 0;
         double sum = 0;
         int n=sb1.length - 1;
         for(int i = 0; i < 3; i++)
         {
             SC = SemanticCoherence.getSemanticCoherence(sb1[i].toString(), sb1[n-i].toString());
             out.write(" "+SC+"\n");
             System.out.println(SC);
             count++;
             sum+=SC;
         }
         //out.println("\nMean is "+ sum/count +"\n"+"*******************************"+"\n");
     }
     catch (IOException e) {
         //exception handling
     }
 
    }
	
	private static void pairCoherenceAndNovelty(){
		String strFile = "data/pairwise/total_0_2500.csv";
		//can use opencsv
		CSVReader reader = null;
		BufferedWriter writer =null;
		double[] scores;
		try {
			reader = new CSVReader(new FileReader(strFile));
			writer = new BufferedWriter(new FileWriter("data/pairwise/results_0_2500.csv", true));
			String [] nextLine;
		    int lineNumber = 0;
		    String[] header= reader.readNext();
		    System.out.println(header);
		    //writer.write(header[0].toString() +","+ header[1].toString() + ","+ "Exposition Coherence"+","+"Novelty");
		   // writer.write(System.getProperty( "line.separator"));
		    int count =0;
			    while ((nextLine = reader.readNext()) != null) {
			      lineNumber++;
			     
			      writer.write(System.getProperty( "line.separator"));
			      System.out.println(nextLine[6].toString());
			      System.out.println(nextLine[7].toString());
			      scores = SemanticCoherence.getSemanticCoherenceNovelty(nextLine[6].toString(), nextLine[7].toString());
			      writer.write(nextLine[0].toString() +","+ nextLine[1].toString()+ ","
			    		  + nextLine[2].toString()+"," + nextLine[3].toString()+","+
			    		  nextLine[4].toString() +","+nextLine[5].toString() +","+
			    		  nextLine[6].toString() +","+nextLine[7].toString() +","+
			    		  +scores[0] +","+scores[1]);
			     
			      // if(lineNumber==2) {
			    	 // break;
			      // }
			      }
			    
    } catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} finally{
		if(writer!=null)
			try {
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	}
	
	private static void pairCoherenceAndNovelty(StringBuilder[] sb1) {

        /* This function calculates the coherence score between consecutive
          learning resources pairs (e.g. (LA1,LA2),(LA2,LA3),.....)

          SemanticCoherence.getSemanticCoherence(LA1,LA2) finds coherence score between LA1 and LA2
          using Graph Kernel approach
       */
		
		System.out.println("string is "+sb1[0].toString());
		System.out.println("Hai");
		System.out.println("sb1.length "+sb1.length);
       //writing each coherence score in CollectionOutput file
       try(FileWriter fw = new FileWriter("collection/CollectionOutput2", true);

           BufferedWriter bw = new BufferedWriter(fw);
           PrintWriter out = new PrintWriter(bw))
		{
			 double[] scores;
           int count = 0;
           double sum = 0;
           System.out.println("sb1.length "+sb1.length);
           //for(int i = 0; i <= sb1.length; i++)
           for(int i = 0; i < sb1.length -1; i++)
           {
           	System.out.println(sb1[i].toString());
           	System.out.println(sb1[i+1].toString());
        	   scores = SemanticCoherence.getSemanticCoherenceNovelty(sb1[i].toString(), sb1[i+1].toString());
               out.write(" "+scores[0]+"\n");
               System.out.println("ec = "+scores[0]);
               count++;
               sum+=scores[0];
               out.write(" "+scores[1]+"\n");
               System.out.println("novelty ="+scores[1]);
           }
           out.println("\nMean is "+ sum/count +"\n"+"*******************************"+"\n");
       }
	
       catch (Exception e) {
           //exception handling
    	   e.printStackTrace();
       }
   }

}
