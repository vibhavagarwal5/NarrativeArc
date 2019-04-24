from gensim.models import Word2Vec
from gensim.models.doc2vec import Doc2Vec, TaggedDocument
from gensim.test.utils import common_texts, get_tmpfile
from nltk import word_tokenize
import numpy as np
import pandas as pd

def load_glove():
    file = open("./data/glove.6B.50d.txt", 'r')
    vocab = {}
    word_vectors = [line.strip().split() for line in file.readlines()]
    for word_vector in word_vectors:
        vocab[word_vector[0]] = [float(v) for v in word_vector[1:]]
    return vocab

def train_Word2Vec(embedding_size = 100):
    path = get_tmpfile("word2vec.model")
    file = open("collections_text.txt", "r")
    text_data = []
    for line in file.readlines():
        text_data.append(word_tokenize(line))
    model = Word2Vec(text_data, size = embedding_size, window = 5, min_count = 1, workers = 4)
    model.save("word2vec.bin")

def load_local_Word2Vec():
    model = Word2Vec.load('./Word2Vec_100dim_science')
    return model

def load_local_Doc2Vec():
    model = Doc2Vec.load("./doc2vec_100dim_science.model")
    return model

# def get_doc_embedding_avg(word_dict, vocab, sentence, embedding_size):
#     #returns average of embedding values of words in the sentence
#     vec_sum = [0.0]*embedding_size
#     vec_sum = np.array(vec_sum)
#     for word in sentence:
#         if word in vocab:
#             vec_sum += model.wv[word]
#     return vec_sum/embedding_size

word2vec = load_local_Word2Vec()
def get_doc_embedding_avg(sentence,dim):
    words=sentence.strip().split()
    emb=np.zeros(dim)
    for word in words:
        if(word in word2vec.wv.vocab):
            emb = np.add(emb,word2vec[word])
    return np.array(emb/len(words))

Doc2vec_model = load_local_Doc2Vec()
def get_Doc2vec_embedding(sentence,dim):
    emb = np.zeros(dim) 
    emb += Doc2vec_model.infer_vector(word_tokenize(sentence.strip().lower()))
    return np.array(emb)

def train_doc_embedding_model(collections, vec_size = 100, max_epochs = 200, alpha = 0.025):
    data = [text for col in collections for text in col["summarization"]]
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
        # decrease the learning rate
        model.alpha -= 0.0002
        # fix the learning rate, no decay
        model.min_alpha = model.alpha

    model.save("doc2vec_" + vec_size + "dim.model")
    return model
