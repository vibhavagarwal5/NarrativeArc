package edu.iiitb.na.main;
import java.io.*;
import java.util.*;

import edu.iiitb.na.cgraph.Config;
import edu.iiitb.na.cgraph.CreateGraph;
import edu.iiitb.na.cgraph.PreProcessText;
import edu.iiitb.na.sc.SemanticCoherence;
import org.datavec.api.util.ClassPathResource;
import org.deeplearning4j.clustering.cluster.Cluster;
import org.deeplearning4j.clustering.cluster.ClusterSet;
import org.deeplearning4j.clustering.cluster.Point;
import org.deeplearning4j.clustering.kmeans.KMeansClustering;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.models.word2vec.wordstore.inmemory.AbstractCache;
import org.deeplearning4j.text.documentiterator.LabelsSource;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;

import static edu.iiitb.na.sc.SemanticCoherence.extractKeyPhrases;

public class Main {

    //	private static Logger log = LoggerFactory.getLogger(Main.class);
    private static String path = "src/main/resources/";
    //path where collections are present

    public static void main(String[] args) throws IOException {

        Config.loadConfigs();

        if (args.length > 0)
        {
            if (args[0].equalsIgnoreCase("create")) {
                System.out.println("Creating Term Co-occurence graph");
                // Text preprocessing
                PreProcessText.readJson(); //For creating LAText.txt
                PreProcessText.cleanData();
                PreProcessText.keyWordExtraction();
                // Creating co-occurence graph
                CreateGraph.createGraph(Config.KEYWORDS_PATH);
                //CreateGraph.createGraph_WithoutCypher(Config.KEYWORDS_PATH);

            } else if (args[0].equalsIgnoreCase("add")) {
                System.out.println("Adding entity to co-occurence graph");
                CreateGraph.cypherTest();

            } else if (args[0].equalsIgnoreCase("coherence")) {
                /*
                 * The learning activities need to be in JSON format. Please
                 * refer to the sample input for details. Strictly follow the
                 * format.
                 */
                System.out.println("Coherence Graph");
//				String string1 = JOptionPane.showInputDialog(null,"Enter the first String");
//				String string2 = JOptionPane.showInputDialog(null,"Enter the Second String");

                String string1 = "Solving an Equation by Combining Like Terms";
                String string2 = "Practice problems on the properties of real numbers";

                double SC = SemanticCoherence.getSemanticCoherence(string1,string2);

//				JOptionPane.showMessageDialog(null,"The Semantic Coherence is :" + SC);
                System.out.println("The Semantic Coherence is :" + SC);
                //prints the semantic coherence score between two learning resources

            } else if (args[0].equalsIgnoreCase("debug")) {

                FileWriter writer = new FileWriter(args[3]);
                try (BufferedReader br = new BufferedReader(new FileReader(args[1])))
                {
                    BufferedReader br2 = new BufferedReader(new FileReader(args[2]));
                    String string1, string2;

                    if (args[1].compareTo(args[2]) == 0)
                    {
                        string2 = br2.readLine();
                    }

                    while ((string1 = br.readLine()) != null && (string2 = br2.readLine()) != null
                            && string1.length() > 0 && string2.length() > 0)
                    {
                        // input string to be a JSON format. This is to verify
                        // that its encompassed within []
                        if (string1.charAt(0) != '[')
                            string1 = "[" + string1 + "]";
                        if (string2.charAt(0) != '[')
                            string2 = "[" + string2 + "]";

                        double SC = SemanticCoherence.getSemanticCoherence(string1, string2);

                        ArrayList<String> K1 = extractKeyPhrases(string1);
                        ArrayList<String> K2 = extractKeyPhrases(string2);

                        String s1 = "", s2 = "";

                        for (String s : K1)
                            s1 += (s + " ");
                        for (String s : K2)
                            s2 += (s + " ");

                        writer.write("\n" + s1 + "\n" + s2 + "\n---->The Semantic Coherence is : " + SC);
                        System.out.println(s1 + "\n" + s2 + "\n---->The Semantic Coherence is : " + SC);
                    }
                }
                writer.close();

            } else if (args[0].equalsIgnoreCase("analyze")) {

                /*
                 * Analyze mode takes two file names as input. Reads the first
                 * LA only from the first filename and prints the analysis in
                 * the second filename (output file) Analysis is nodes printed
                 * in decreasing order of number of times visited in the Entity
                 * Term Graph after Random Walks.
                 */
				/*
				FileWriter writer = new FileWriter(args[2]);
				try (BufferedReader br = new BufferedReader(new FileReader(
						args[1]))) {
					String string1 = br.readLine();
					List<Entry<String, Integer>> list = SemanticCoherence
							.getSemanticCoherenceAnalysis(string1);

					for (Entry<String, Integer> i : list) {
						writer.write("\n" + i.getKey() + " - " + i.getValue());
						System.out.println(i.getKey() + " - " + i.getValue());
					}
				}
				writer.close();
				*/
            }
            else if (args[0].equalsIgnoreCase("testGraphKernel")) {

                //reading collection and random collection files
                BufferedReader br1 = new BufferedReader(new FileReader( path + "data/collection/Collection1"));
                BufferedReader br2 = new BufferedReader(new FileReader( path + "data/collection/Collection2"));
                BufferedReader br3 = new BufferedReader(new FileReader( path + "data/collection/Collection3"));
                BufferedReader br4 = new BufferedReader(new FileReader( path + "data/collection/Collection4"));
                BufferedReader br5 = new BufferedReader(new FileReader( path + "data/collection/Collection5"));
                BufferedReader br6 = new BufferedReader(new FileReader( path + "data/collection/Collection6"));
                BufferedReader br7 = new BufferedReader(new FileReader( path + "data/collection/Collection7"));
                BufferedReader br8 = new BufferedReader(new FileReader( path + "data/collection/Collection8"));
                BufferedReader rc1 = new BufferedReader(new FileReader( path + "data/collection/RandomCollection1"));

                //Collection StringBuilder array
                StringBuilder[] sb1 = new StringBuilder[br1.lines().toArray().length];
                br1 = new BufferedReader( new FileReader( path + "data/collection/Collection1") );
                StringBuilder[] sb2 = new StringBuilder[br2.lines().toArray().length];
                br2 = new BufferedReader( new FileReader( path + "data/collection/Collection2") );
                StringBuilder[] sb3 = new StringBuilder[br3.lines().toArray().length];
                br3 = new BufferedReader( new FileReader( path + "data/collection/Collection3") );
                StringBuilder[] sb4 = new StringBuilder[br4.lines().toArray().length];
                br4 = new BufferedReader( new FileReader( path + "data/collection/Collection4") );
                StringBuilder[] sb5 = new StringBuilder[br5.lines().toArray().length];
                br5 = new BufferedReader( new FileReader( path + "data/collection/Collection5") );
                StringBuilder[] sb6 = new StringBuilder[br6.lines().toArray().length];
                br6 = new BufferedReader( new FileReader( path + "data/collection/Collection6") );
                StringBuilder[] sb7 = new StringBuilder[br7.lines().toArray().length];
                br7 = new BufferedReader( new FileReader( path + "data/collection/Collection7") );
                StringBuilder[] sb8 = new StringBuilder[br8.lines().toArray().length];
                br8 = new BufferedReader( new FileReader( path + "data/collection/Collection8") );

                //Random Collection StringBuilder array
                StringBuilder[] rndm1 = new StringBuilder[rc1.lines().toArray().length];
                rc1 =  new BufferedReader( new FileReader( path + "data/collection/RandomCollection1") );

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
                pairCoherence(sb1);
                pairCoherence(sb2);
                pairCoherence(sb3);
                pairCoherence(sb4);
                pairCoherence(sb5);
                pairCoherence(sb6);
                pairCoherence(sb7);
                pairCoherence(sb8);

                //For getting output of pairwise semantic score for random collection
                pairCoherence(rndm1);
            }
            else if(args[0].equalsIgnoreCase( "testWord2Vec")) {

                //LAText.txt is a collection of 9000 records

                String filePath = new ClassPathResource("LAText.txt").getFile().getAbsolutePath();
                SentenceIterator iter = new BasicLineIterator(filePath);
                TokenizerFactory t = new DefaultTokenizerFactory();
                t.setTokenPreProcessor(new CommonPreprocessor());

                // Building word2vec vector
                Word2Vec vec = new Word2Vec.Builder()
                        .minWordFrequency(5)
                        .iterations(1)
                        .layerSize(100)
                        .seed(42)
                        .windowSize(10)
                        .iterate(iter)
                        .tokenizerFactory(t)
                        .build();

                vec.fit();


                //reading collection and random collection files
                BufferedReader br1 = new BufferedReader(new FileReader( path + "data/collection/Collection1"));
                BufferedReader br2 = new BufferedReader(new FileReader( path + "data/collection/Collection2"));
                BufferedReader br3 = new BufferedReader(new FileReader( path + "data/collection/Collection3"));
                BufferedReader br4 = new BufferedReader(new FileReader( path + "data/collection/Collection4"));
                BufferedReader br5 = new BufferedReader(new FileReader( path + "data/collection/Collection5"));
                BufferedReader br6 = new BufferedReader(new FileReader( path + "data/collection/Collection6"));
                BufferedReader br7 = new BufferedReader(new FileReader( path + "data/collection/Collection7"));
                BufferedReader br8 = new BufferedReader(new FileReader( path + "data/collection/Collection8"));
                BufferedReader rc1 = new BufferedReader(new FileReader( path + "data/collection/RandomCollection1"));

                //Collection StringBuilder array
                StringBuilder[] sb1 = new StringBuilder[br1.lines().toArray().length];
                br1 = new BufferedReader( new FileReader( path + "data/collection/Collection1") );
                StringBuilder[] sb2 = new StringBuilder[br2.lines().toArray().length];
                br2 = new BufferedReader( new FileReader( path + "data/collection/Collection2") );
                StringBuilder[] sb3 = new StringBuilder[br3.lines().toArray().length];
                br3 = new BufferedReader( new FileReader( path + "data/collection/Collection3") );
                StringBuilder[] sb4 = new StringBuilder[br4.lines().toArray().length];
                br4 = new BufferedReader( new FileReader( path + "data/collection/Collection4") );
                StringBuilder[] sb5 = new StringBuilder[br5.lines().toArray().length];
                br5 = new BufferedReader( new FileReader( path + "data/collection/Collection5") );
                StringBuilder[] sb6 = new StringBuilder[br6.lines().toArray().length];
                br6 = new BufferedReader( new FileReader( path + "data/collection/Collection6") );
                StringBuilder[] sb7 = new StringBuilder[br7.lines().toArray().length];
                br7 = new BufferedReader( new FileReader( path + "data/collection/Collection7") );
                StringBuilder[] sb8 = new StringBuilder[br8.lines().toArray().length];
                br8 = new BufferedReader( new FileReader( path + "data/collection/Collection8") );

                //Random Collection StringBuilder array
                StringBuilder[] rndm1 = new StringBuilder[rc1.lines().toArray().length];
                rc1 =  new BufferedReader( new FileReader( path + "data/collection/RandomCollection1") );

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

                /* w2vCoherence(sb, vec) is a function that calculates the coherence score between consecutive
                   learning resources pairs (e.g. (LA1,LA2),(LA2,LA3),.....) using word2vec method
                */

                //For getting output of pairwise semantic score for each collection
                w2vCoherence(sb1, vec);
                w2vCoherence(sb2, vec);
                w2vCoherence(sb3, vec);
                w2vCoherence(sb4, vec);
                w2vCoherence(sb5, vec);
                w2vCoherence(sb6, vec);
                w2vCoherence(sb7, vec);
                w2vCoherence(sb8, vec);
                //For getting output of pairwise semantic score for random collection
                w2vCoherence(rndm1, vec);

            }
            else if(args[0].equalsIgnoreCase( "testDoc2Vec")) {

                //LAText.txt is a collection of 9000 records

                ClassPathResource resource = new ClassPathResource("LAText.txt");
                File file = resource.getFile();
                SentenceIterator iter = new BasicLineIterator(file);
                AbstractCache<VocabWord> cache = new AbstractCache<>();
                TokenizerFactory t = new DefaultTokenizerFactory();
                t.setTokenPreProcessor(new CommonPreprocessor());
                LabelsSource source = new LabelsSource("DOC_");

                // Building doc2vec vector
                ParagraphVectors vec = new ParagraphVectors.Builder()
                        .minWordFrequency(1)
                        .iterations(5)
                        .epochs(1)
                        .layerSize(100)
                        .learningRate(0.025)
                        .labelsSource(source)
                        .windowSize(5)
                        .iterate(iter)
                        .trainWordVectors(false)
                        .vocabCache(cache)
                        .tokenizerFactory(t)
                        .sampling(0)
                        .build();

                vec.fit();

                //reading collection and random collection files
                BufferedReader br1 = new BufferedReader(new FileReader( path + "data/collection/Collection1"));
                BufferedReader br2 = new BufferedReader(new FileReader( path + "data/collection/Collection2"));
                BufferedReader br3 = new BufferedReader(new FileReader( path + "data/collection/Collection3"));
                BufferedReader br4 = new BufferedReader(new FileReader( path + "data/collection/Collection4"));
                BufferedReader br5 = new BufferedReader(new FileReader( path + "data/collection/Collection5"));
                BufferedReader br6 = new BufferedReader(new FileReader( path + "data/collection/Collection6"));
                BufferedReader br7 = new BufferedReader(new FileReader( path + "data/collection/Collection7"));
                BufferedReader br8 = new BufferedReader(new FileReader( path + "data/collection/Collection8"));
                BufferedReader rc1 = new BufferedReader(new FileReader( path + "data/collection/RandomCollection1"));

                //Collection StringBuilder array
                StringBuilder[] sb1 = new StringBuilder[br1.lines().toArray().length];
                br1 = new BufferedReader( new FileReader( path + "data/collection/Collection1") );
                StringBuilder[] sb2 = new StringBuilder[br2.lines().toArray().length];
                br2 = new BufferedReader( new FileReader( path + "data/collection/Collection2") );
                StringBuilder[] sb3 = new StringBuilder[br3.lines().toArray().length];
                br3 = new BufferedReader( new FileReader( path + "data/collection/Collection3") );
                StringBuilder[] sb4 = new StringBuilder[br4.lines().toArray().length];
                br4 = new BufferedReader( new FileReader( path + "data/collection/Collection4") );
                StringBuilder[] sb5 = new StringBuilder[br5.lines().toArray().length];
                br5 = new BufferedReader( new FileReader( path + "data/collection/Collection5") );
                StringBuilder[] sb6 = new StringBuilder[br6.lines().toArray().length];
                br6 = new BufferedReader( new FileReader( path + "data/collection/Collection6") );
                StringBuilder[] sb7 = new StringBuilder[br7.lines().toArray().length];
                br7 = new BufferedReader( new FileReader( path + "data/collection/Collection7") );
                StringBuilder[] sb8 = new StringBuilder[br8.lines().toArray().length];
                br8 = new BufferedReader( new FileReader( path + "data/collection/Collection8") );
                //Random Collection StringBuilder array
                StringBuilder[] rndm1 = new StringBuilder[rc1.lines().toArray().length];
                rc1 =  new BufferedReader( new FileReader( path + "data/collection/RandomCollection1") );

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

                 /* d2vCoherence(sb, vec) is a function that calculates the coherence score between consecutive
                   learning resources pairs (e.g. (LA1,LA2),(LA2,LA3),.....) using doc2vec method
                */

                //For getting output of pairwise semantic score for each collection
                d2vCoherence(sb1,vec);
                d2vCoherence(sb2,vec);
                d2vCoherence(sb3,vec);
                d2vCoherence(sb4,vec);
                d2vCoherence(sb5,vec);
                d2vCoherence(sb6,vec);
                d2vCoherence(sb7,vec);
                d2vCoherence(sb8,vec);
                //For getting output of pairwise semantic score for random collection
                d2vCoherence(rndm1,vec);

            }
            else if(args[0].equalsIgnoreCase("rankResources"))
            {

                /*Given a set of learning resources we use Doc2Vec to calculate their infervectors.
                  Clusters are formed based on their inferVectors.
                  Ranking the learning resources according to semantic coherence. */

                //LAText.txt is a collection of 9000 records

                ClassPathResource resource = new ClassPathResource("LAText.txt" );
                File file = resource.getFile();
                SentenceIterator iter = new BasicLineIterator(file);
                AbstractCache<VocabWord> cache = new AbstractCache<>();
                TokenizerFactory t = new DefaultTokenizerFactory();
                t.setTokenPreProcessor(new CommonPreprocessor());
                LabelsSource source = new LabelsSource("DOC_");

                // Building doc2vec vector
                ParagraphVectors vec = new ParagraphVectors.Builder()
                        .minWordFrequency(1)
                        .iterations(5)
                        .epochs(1)
                        .layerSize(100)
                        .learningRate(0.025)
                        .labelsSource(source)
                        .windowSize(5)
                        .iterate(iter)
                        .trainWordVectors(false)
                        .vocabCache(cache)
                        .tokenizerFactory(t)
                        .sampling(0)
                        .build();

                vec.fit();

                /*Using K-means clustering algorithm for forming clusters using a set of learning resources */

                int maxIterationCount = 5;
                int clusterCount = 10;
                String distanceFunction = "cosinesimilarity";
                KMeansClustering kmc = KMeansClustering.setup(clusterCount, maxIterationCount, distanceFunction);

                //RC-Cluster is a set of learning resources
                BufferedReader br = new BufferedReader(new FileReader(path + "data/cluster/RC-Cluster"));
                StringBuilder[] sb = new StringBuilder[br.lines().toArray().length];
                br = new BufferedReader( new FileReader( path + "data/cluster/RC-Cluster") );
                for(int i = 0; i < sb.length; i++) {
                    sb[i] = new StringBuilder(br.readLine());
                }

                //we find inferVector of each learning resource using vec.inferVector(str) method
                Map<INDArray, String> map = new HashMap<>();
                List<INDArray> vectors = new ArrayList<INDArray>();
                for (int k = 0; k < sb.length - 1 ; k++) {
                    INDArray arr = vec.inferVector(sb[k].toString());
                    vectors.add(arr);
                    map.put(arr, sb[k].toString());
                }

                System.out.println(vectors.size() + " vectors extracted to create Point list");
                List<Point> pointsLst = Point.toPoints(vectors);
                System.out.println(pointsLst.size() + " Points created out of " + vectors.size() + " vectors");

                System.out.println("Start Clustering " + pointsLst.size() + " points/docs");
                ClusterSet cs = kmc.applyTo(pointsLst);
                System.out.println("Finish  Clustering");

                //printing clusters formed
                List<Cluster> clsterLst = cs.getClusters();
                System.out.println("\nNo. of clusters :"+clsterLst.size());
                System.out.println("\nCluster List:");

                int counter=1;
                for(Cluster c: clsterLst)
                {
                    System.out.println("\n"+counter+" cluster\n");
                    List<Point> c1 = c.getPoints();
                    System.out.println("\nNo. of elements :"+c1.size());
                    Iterator<Point> iterator = c1.iterator();
                    while( iterator.hasNext())
                    {
                        Point p = iterator.next();
                        System.out.println(map.get(p.getArray()));
                    }
                    System.out.println("*****");
                    counter++;
                }

                //reading cluster files
                BufferedReader br1 = new BufferedReader(new FileReader( path + "data/cluster/Cluster1"));
                BufferedReader br2 = new BufferedReader(new FileReader( path + "data/cluster/Cluster2"));
                BufferedReader br3 = new BufferedReader(new FileReader( path + "data/cluster/Cluster3"));
                BufferedReader br4 = new BufferedReader(new FileReader( path + "data/cluster/Cluster4"));
                BufferedReader br5 = new BufferedReader(new FileReader( path + "data/cluster/Cluster5"));
                BufferedReader br6 = new BufferedReader(new FileReader( path + "data/cluster/Cluster6"));

                //Cluster StringBuilder array
                StringBuilder[] sb1 = new StringBuilder[br1.lines().toArray().length];
                br1 = new BufferedReader( new FileReader( path + "data/cluster/Cluster1") );
                StringBuilder[] sb2 = new StringBuilder[br2.lines().toArray().length];
                br2 = new BufferedReader( new FileReader( path + "data/cluster/Cluster2") );
                StringBuilder[] sb3 = new StringBuilder[br3.lines().toArray().length];
                br3 = new BufferedReader( new FileReader( path + "data/cluster/Cluster3") );
                StringBuilder[] sb4 = new StringBuilder[br4.lines().toArray().length];
                br4 = new BufferedReader( new FileReader( path + "data/cluster/Cluster4") );
                StringBuilder[] sb5 = new StringBuilder[br5.lines().toArray().length];
                br5 = new BufferedReader( new FileReader( path + "data/cluster/Cluster5") );
                StringBuilder[] sb6 = new StringBuilder[br6.lines().toArray().length];
                br6 = new BufferedReader( new FileReader( path + "data/cluster/Cluster6") );

                //separating each learning resources of each cluster
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

                /* resourceCoherence(sb) is a function that calculates the semantic coherence score between every
                possible learning resource pairs using Graph Kernel method
                */

                //Ranking the learning resources according to semantic coherence score
                resourceCoherence(sb1);
                resourceCoherence(sb2);
                resourceCoherence(sb3);
                resourceCoherence(sb4);
                resourceCoherence(sb5);
                resourceCoherence(sb6);
            }
            else {

                System.out.println("Please enter an option - \n create graph or add entity or semantic coherence");
            }
        }
    }

    private static void d2vCoherence(StringBuilder[] sb1, ParagraphVectors vec) {

        /* This function calculates the coherence score between consecutive
           learning resources pairs (e.g. (LA1,LA2),(LA2,LA3),.....) using doc2vec method
           1.for a pair (LA1 , LA2)
           2. find inferVector of LA1 and LA2 learning resource
           3. coherence score is the cosine similarilty between their inferVectors
         */
        double cosine=0.0;
        int count = 0;
        double sum = 0;

        //writing each coherence score in CollectionOutput file
        try(FileWriter fw = new FileWriter(path + "data/collection/CollectionOutput", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw);)
        {
            for (int k = 0; k < sb1.length - 1 ; k++)
            {
                INDArray arr1 = vec.inferVector(sb1[k].toString());
                INDArray arr2 = vec.inferVector(sb1[k+1].toString());

                double deno1 = 0;
                for (int i = 0; i < arr1.length(); i++)
                    deno1 += Math.pow(arr1.getDouble(i), 2);
                double deno2 = 0;
                for (int i = 0; i < arr2.length(); i++)
                    deno2 += Math.pow(arr2.getDouble(i), 2);

                double num1 = 0;
                for (int i = 0; i < (arr1.length()) && (i < arr2.length()); i++)
                {
                    num1 += arr1.getDouble(i) * arr2.getDouble(i);
                }
                cosine = num1 / Math.sqrt(deno1 * deno2);
                out.println(cosine);
                System.out.println(cosine);
                count++;
                sum+=cosine;
            }
            out.println("\nMean is "+ sum/count +"\n"+"*******************************"+"\n");

        }
        catch (IOException e) {
            //exception handling
        }

    }

    private static void w2vCoherence(StringBuilder[] sb1, Word2Vec vec) {

        /* This function calculates the coherence score between consecutive
           learning resources pairs (e.g. (LA1,LA2),(LA2,LA3),.....)

           SemanticCoherence.getWord2VecSemanticCoherence(LA1,LA2) finds coherence score between LA1 and LA2
           using word2vec method
        */

        //writing each coherence score in CollectionOutput file
        try(FileWriter fw = new FileWriter(path + "data/collection/CollectionOutput", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw);)
        {
            double SC = 0;
            int count = 0;
            double sum = 0;

            for(int i = 0; i < sb1.length - 1; i++)
            {
                SC = SemanticCoherence.getWord2VecSemanticCoherence(sb1[i].toString(), sb1[i+1].toString(), vec);
                out.write(" "+SC+"\n");
                System.out.println(SC);
                count++;
                sum+=SC;
            }
            out.println("\nMean is "+ sum/count +"\n"+"*******************************"+"\n");
        }
        catch (IOException e) {
            //exception handling
        }

    }

    private static void pairCoherence(StringBuilder[] sb1) {

         /* This function calculates the coherence score between consecutive
           learning resources pairs (e.g. (LA1,LA2),(LA2,LA3),.....)

           SemanticCoherence.getSemanticCoherence(LA1,LA2) finds coherence score between LA1 and LA2
           using Graph Kernel approach
        */

        //writing each coherence score in CollectionOutput file
        try(FileWriter fw = new FileWriter(path + "data/collection/CollectionOutput", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            double SC;
            int count = 0;
            double sum = 0;

            for(int i = 0; i < sb1.length - 1; i++)
            {
                SC = SemanticCoherence.getSemanticCoherence(sb1[i].toString(), sb1[i+1].toString());
                out.write(" "+SC+"\n");
                System.out.println(SC);
                count++;
                sum+=SC;
            }
            out.println("\nMean is "+ sum/count +"\n"+"*******************************"+"\n");
        }
        catch (IOException e) {
            //exception handling
        }
    }
    private static void resourceCoherence(StringBuilder[] sb1) {

        /* This function calculates the coherence score between every possible
           learning resource pair.

           SemanticCoherence.getSemanticCoherence(LA1,LA2) finds coherence score between LA1 and LA2
           using Graph Kernel approach
        */

        //writing each coherence score in CollectionOutput file
        try(FileWriter fw = new FileWriter(path + "data/cluster/ClusterOutput", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            double SC;
            int count = 0;
            double sum = 0;

            for(int i = 0; i < sb1.length - 1; i++)
            {
                for(int j = i+1; j < sb1.length; j++)
                {
                SC = SemanticCoherence.getSemanticCoherence(sb1[i].toString(), sb1[j].toString());
                out.write(" "+SC+"\n");
                System.out.println(SC);
                count++;
                sum+=SC;
                }
            }
            out.println("\nMean is "+ sum/count +"\n"+"*******************************"+"\n");
        }
        catch (IOException e) {
            //exception handling
        }
    }
}