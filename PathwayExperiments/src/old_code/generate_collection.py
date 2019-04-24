import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import os
import re
import util
import random
import embeddings_util
import nltk
import random

import torch
import torch.nn as nn
import torch.nn.functional as F
import torch.optim as optim

torch.manual_seed(1)
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
EMBEDDING_DIM = 50


def get_doc_embedding(text):
	#Retuns document embedding for text
	#Doc embedding is computed as: avg(word_embedding * tfidf)
    global vocab
    global device
    global tfidf_matrix
    global word_to_ix
    global doc_to_ix
    global EMBEDDING_DIM
    embeds = []
    text = text.lower()
    for word in text.split():
        if word in vocab:
            tf_idf = tfidf_matrix[doc_to_ix[text], word_to_ix[word]]
            embed = [v * tf_idf for v in vocab[word]]
            embeds.append(torch.tensor(embed, dtype = torch.float, device = device))
        else:
            embeds.append(torch.zeros(EMBEDDING_DIM, device = device, dtype = torch.float))
    embeds = torch.cat(embeds).view(len(text.split()), -1)
    return embeds.sum(dim = 0)/embeds.shape[0]

def text_similarity(text1, text2):
	#Retuns cosine similarity of document embeddings of text1 and text2
    cos = nn.CosineSimilarity(dim = 0)
    embed1 = get_doc_embedding(text1)
    embed2 = get_doc_embedding(text2)
    
    return cos(embed1, embed2)


def get_mean_score_pair(data, score_function):
	#Returns average of score function applied of lr pairs in data
    score_sum = 0.0
    for lr1, lr2 in data:
        score_sum += score_function(lr1, lr2)
    return score_sum/len(data)

def get_next_lr(given_lr, lr_list, target_score, score_function):
    #Returns lr from lr_list with score(with given_lr) closest to target_score 
    next_lr = ""
    score_diff = 1.0
    for lr in lr_list:
        score = score_function(given_lr, lr)
        if abs(score - target_score) < score_diff:
            score_diff = abs(score - target_score)
            next_lr = lr
    return next_lr

def create_collection(collection_size, start_lr, lr_list, target_score, score_function):
    #Creates collection of size collection_size using lr_list, starting with start_lr
    collection = [start_lr]
    lr = start_lr
    for _ in range(collection_size - 1):
        next_lr = get_next_lr(lr, lr_list, target_score, score_function)
        collection.append(next_lr)
        lr = next_lr
    return collection


if __name__ == "__main__":

    #Loading glove word embeddings
    vocab = embeddings_util.load_glove()
    print("Loaded glove embeddings")

    #Loading collections
    df = util.get_processed_data("../data/collections.csv", False)
    collections = util.get_collections(df)
    documents = util.get_texts_from_collections(collections)
    print("Collections created")

    #Processing documents to remove words of length 1

    new_documents = []
    for doc in documents:
        new_doc = ""
        for word in doc.split():
            if len(word) > 1:
                new_doc = word+" "
            if new_doc.strip()!="":
                new_documents.append(new_doc.strip().lower())
    documents = new_documents

    #Runnign tftdf on documents
    tfidf_matrix, doc_to_ix, word_to_ix = embeddings_util.run_tfidf(documents)
    print("TDIDF computation complete")


    #Collecting learning resource pairs which occur consecutively in collections
    lr_pairs = []

    for i in range(len(documents)-1):
        lr1 = documents[i]
        lr2 = documents[i+1]
        lr_pairs.append((lr1, lr2))
    print("Created lr pairs from collections")

    score_function = text_similarity

    #Computing mean of such pairs. (Which is also similarity value at peak of gaussian distribution)
    mean = get_mean_score_pair(lr_pairs, text_similarity)
    print("Target score computed")

    #Creating collections using the mean computed above
    lr_list = documents

    start_lr = lr_list[random.randint(0,len(lr_list))]
    new_collection = create_collection(3, start_lr, lr_list, mean, text_similarity)
    print("New collection: ",new_collection)