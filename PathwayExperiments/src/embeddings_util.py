from gensim.models import Word2Vec
from gensim.models.doc2vec import Doc2Vec, TaggedDocument
from gensim.test.utils import common_texts, get_tmpfile
from nltk import word_tokenize
import numpy as np
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer

def train_Word2Vec(path = "../data/collections_text.txt", embedding_size = 100):
    #Trains a word2vec model using text data
    path = get_tmpfile("word2vec.model")
    file = open(path, "r")
    text_data = []
    for line in file.readlines():
        text_data.append(word_tokenize(line))
    model = Word2Vec(text_data, size = embedding_size, window = 5, min_count = 1, workers = 4)
    model.save("../models/word2vec.bin")

def load_local_word2vec():
    #Loads a trained word2vec mdoel
    model = Word2Vec.load("../models/word2vec.bin")
    return model

def load_glove():
    #Returns a dictionary of word vectors used by Glove embeddings
    file = open("../data/glove.6B.50d.txt", 'r')
    vocab = {}
    word_vectors = [line.strip().split() for line in file.readlines()]
    for word_vector in word_vectors:
        vocab[word_vector[0]] = [float(v) for v in word_vector[1:]]
    return vocab
def get_avg_word2doc_embedding(word_dict, vocab, sentence, embedding_size):
    #returns average of embedding values of words in the sentence
    vec_sum = [0.0]*embedding_size
    vec_sum = np.array(vec_sum)
    for word in sentence:
        if word in vocab:
            vec_sum += model.wv[word]
    return vec_sum/embedding_size

def run_tfidf(documents):
    #Using documents (list of texts), runs a tfidf
    doc_to_ix = {}
    for doc in documents:
        if doc not in doc_to_ix:
            doc_to_ix[doc] = len(doc_to_ix)

    vectorizer = TfidfVectorizer()
    tfidf_matrix = vectorizer.fit_transform(documents)

    feature_names = vectorizer.get_feature_names()
    word_to_ix = {word:index for index, word in enumerate(feature_names)}

    #Returns trained tfidf matrix, document to index and word to index dictionaries
    return tfidf_matrix, doc_to_ix, word_to_ix

def train_doc_embedding_model(collections, vec_size = 100, max_epochs = 200, alpha = 0.025):
    # Trains Doc2Vec on the collections data
    data = [text for col in collections for text in col["texts"]]
    tagged_data = [TaggedDocument(words=word_tokenize(_d.lower()), tags=[str(i)]) for i, _d in enumerate(data)]

    model = Doc2Vec(size=vec_size,
                    alpha=alpha, 
                    min_alpha=0.00025,
                    min_count=1,
                    dm =1)
    
    model.build_vocab(tagged_data)

    for epoch in range(max_epochs):
        print('iteration {0}'.format(epoch))
        model.train(tagged_data,
                    total_examples=model.corpus_count,
                    epochs=model.iter)
        model.alpha -= 0.0002
        model.min_alpha = model.alpha

    model.save("doc2vec_" + vec_size + "dim.model")
    return model
