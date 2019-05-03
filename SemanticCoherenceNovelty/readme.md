Narrative arc finds a coherent sequence of resources for a given subject and given learner.

Semantic Coherence

Here, the overall structure of the project is same as that of web development project where all java files are under src->main->java and resources files of data under src->main->resources.

./run is the necessary run script
(in order to run the "./run.sh" on terminal (without quotes), always add following arguments at place of "<arguments>")
Arguments are:
(1) analyze
(2) debug
(3) coherence
(4) create
(5) testGraphKernel
(6) testWord2Vec
(7) testDoc2Vec
(8) rankingResources

Apply any one of the above arguments at time of editing run.sh file.


see below for the parameters to be passed.
----------------------------------------------------------------------------------------------------
Requirements: neo4j-community-3.2.2+, java (neo4j 3.2.3, jdk8) with 9k keywords (small dataset)
----------------------------------------------------------------------------------------------------

External Variables are set in config.properties
Variables and Constants are set in src/com/na/cgraph/Config.java

--------------------------------------------------
edu/iiitb/na/main/Main.java

(Analyze mode)
analyze mode is to get the semantic context graph and the count of number times each node was visited.
It takes two file names as input. Reads the first LA only from the first filename and prints the analysis in  the second filename (output file) Analysis is nodes printed in decreasing order of number of times visited in the Entity Term Graph after Random Walks.
Ex:
analyze data/InputSameCollection.txt  data/output.txt

(Debug mode)
debug mode takes two or three file names as input. The last filename is the name of the output file generated. If only one file name is given, Debug reads learning activities from that file and calculated SC between every two *consecutive* Learning Activities. If two input files are given, Debug generates SC between every two corresponding learning activity taken in order from the two input files. The learning activities need to be in JSON format. Please refer to the following sample input for details. Strictly follow the format.
Ex:
debug data/InputSameCollection.txt data/InputSameCollection.txt  data/output.txt
				 
(Coherence mode)
coherence mode opens a GUI where the user can input two String objects into two text windows and the semantic coherence will be printed.

(Create)
create mode create the term cooccurrence graph for the necessary input file mentioned in config.properties

(Testing Graph Kernel (TCG) )
testGraphKernel mode will work on the Learning Activites (Collections and Random Collections that have sequential resources and random resources respectively) to calculate the semantic coherence score between consecutive learning resource pairs using graph-kernel method.

(Testing Word2Vec)
testWord2Vec mode will work on calculating semantic coherence score between same Learning Activities mentioned in above case, by building Word Vector, using DL4J libraries's Word2Vec methods and Random Walk algorithm.

(Testing Doc2Vec)
testDoc2Vec mode will work on similar approach that of Word2Vec but additionally it will maintain n-dimensional document vector (check for Para2Vec libraries in DL4J), which will provide inferVector giving word-embeddings of words in that document.

(Ranking resources)
rankingResources will deal with forming clusters out of Random Resources using k-means clustering algorithm (refer DL4J library). After forming clusters, the GraphKernel approach (as mentioned above) will be used to calculate semantic score between each possible pairs.

(d2vCoherence method)
This function calculates the coherence score between consecutive learning resources pairs (e.g. (LA1,LA2),(LA2,LA3),.....) using doc2vec method:
           1.for a pair (LA1 , LA2)
           2. find inferVector of LA1 and LA2 learning resource
           3. coherence score is the cosine similarilty between their inferVectors
	   
(w2vCoherence method)
 This function calculates the coherence score between consecutive learning resources pairs (e.g. (LA1,LA2),(LA2,LA3),.....) SemanticCoherence.getWord2VecSemanticCoherence(LA1,LA2) finds coherence score between LA1 and LA2 using word2vec method
        
(pairCoherence method)
This function calculates the coherence score between consecutive learning resources pairs (e.g. (LA1,LA2),(LA2,LA3),.....) SemanticCoherence.getSemanticCoherence(LA1,LA2) finds coherence score between LA1 and LA2 using Graph
(resourceCoherence method)
This function calculates the coherence score between every possible learning resource pair. SemanticCoherence.getSemanticCoherence(LA1,LA2) finds coherence score between LA1 and LA2 using Graph Kernel approach.
	   
--------------------------------------------------
edu/iiitb/na/sc/SemanticCoherence.java

(getWord2VecSemanticCoherence method)
For first string and second string find the keyphrases e.g. Numbers become Number, brought becomes bring. Get the semantic context using keyphrases of each string by finding the 10 nearest words/keyphrases of each keyphrase in keywordSet1 and keywordSet2 Calculating Semantic Coherence Value using Random Walks at the start of each new random walk , we pick a new keyword from the combined context with implemented Stabilisation and Generalisation.

(getSemanticCoherence method)
The main function is the getSemanticCoherence function which when passed two String objects, returns the SC score of the same. For this, we run Random walks the parameters of which are set in Config.java (implemented Stabilisation and Generalisation)

(getSemanticCoherenceAnalysis method)
getSemanticCoherenceAnalysis is to analysis the SemanticContext graph of a single learning activity passed as a JSON object. it returns a list of keywords or strings and the corresponding number of times that keyword was visited while running the Random Walks.

--------------------------------------------------
edu/iiitb/na/cgraph/Primitives.java

All graph theory primitives are implemented in this file. Some of the functions for example are getSematicContext, MatrixMultiplication, GraphMerge etc.
--------------------------------------------------
edu/iiitb/na/cgraph/OldCodes

This file contains all outdated implementations of methods of calculating the SC score. Some of which are Running Random Walks using a random Start Probability, Stop Probability and Infinite series summation, etc.
--------------------------------------------------
edu/iiitb/na/cgraph/CreateGraph.java

Using Cypher queries, we create the term Cooccurrence graph by creating the entity graph for each learning activity and merging it with the existing graph.
--------------------------------------------------
Neo4j Graph database created will be stored in cooccurrencegraph.db

Keywords generated from the given set of learning activities wil be stored in data/Keywords.txt

Its good to maintain all the input output files in the IO/ folder.

stopWords.txt contains all the words found while cleaning the learning activities but which are to be omitted while forming the Keywords.

All files pertaining to cleaning and processing of the input file while creating the term cooccurrence graph, goes into the data/ folder.

------------------------------------------------------
Included cluster_schema.sql file inside resources folder in order to create database. For ranking the resources, need to use Order By clause in sql query.
------------------------------------------------------


Note:

**The file generate_collection_SVM_LSTM_Science.ipynb is used to generate the collections. It is place here as it is accessing the Java code on backend.**

**To run `generate_collection_SVM_LSTM_Science.ipynb` you need a Temp folder which has all the models (Doc2Vec, Word2Vec, Lda etc.) required to run. You can collect the folder from mentor and place it in this foler.**
