# Documentation

## class_util
* class LearningResource (attributes: sequence_id, resource_id, title, description, text)
* class Collection (attributes: id, learning_resources)


## embeddings_util
* train_Word2Vec : Trains a word2vec model using text data
* load_local_word2vec: Loads a trained word2vec mdoel
* load_glove: Returns a dictionary of word vectors used by Glove embeddings
* run_tfidf: Using documents (list of texts), runs a tfidf, returns trained tfidf matrix, document to index and word to index dictionaries

## util
* get_data: Returns dataframe read from path
* clean_html: Removes sub-strings with html content (pattern: <...>)
* get_alphanumeral: Keeps only alphanumeral characters in the string
* get_alpha: Keeps only alphabets in the string
* remove_stop_words: Removes stop words (from nltk library) from given string
* get_text_from_df: returns text concat(title,description) keeping only alphabets, optionally also removes stop words
* get_processed_data: Returns processed data frame read from path
* get_texts_from_collections: Retuns list of learning resource texts in the collection
* get_collection: Returns collection object using rows in dataframe(df) with collection_id value of id
* get_collections: Returns list of collection objects using data in the dataframe(df)
* get_random_collections: Using collections (each one a list of texts), returns a random collection of average size
* get_train_test: Using collections (each one a list of texts), returns train and test data, train and test data contain tuples of (collection, label), label is 0 for a random collection and 1 for original collection

## generate_collections
* get_doc_embedding: Retuns document embedding for text, doc embedding is computed as: avg(word_embedding * tfidf)
* text_similarity: Retuns cosine similarity of document embeddings of text1 and text2
* get_mean_score_pair: Returns average of score function applied of lr pairs in data
* get_next_lr: Returns lr from lr_list with score(with given_lr) closest to target_score 
* create_collection: Creates collection of size collection_size using lr_list, starting with start_lr
The file also has code to data, process and create new collections

