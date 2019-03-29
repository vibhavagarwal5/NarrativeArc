#!/usr/bin/env python
# coding: utf-8

# In[20]:


# importing required modules

import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import os
import re
import util
from numpy import linalg as LA
import pickle
import random

from sklearn.metrics import log_loss
from sklearn.metrics import accuracy_score
from sklearn.model_selection import train_test_split
from sklearn import svm
from gensim.models.doc2vec import Doc2Vec, TaggedDocument
from gensim.models import Word2Vec
from nltk.tokenize import word_tokenize
from sklearn.multiclass import OneVsRestClassifier


# In[21]:


# loading the model

df = util.get_processed_data("./data/collections_math.csv", False)
collections = util.get_collections(df)


# In[112]:


df.info()


# In[62]:


#Removing words with length 1 since tfidf does not recognize them

for col in collections:
    lrs = []
    for i,text in enumerate(col["texts"]):
        new_text = ""
        for word in text.split():
            if len(word)>2:
                new_text+=word+" "
        if new_text.strip()!="":
            lrs.append(new_text.strip().lower())
    col["texts"] = lrs


# In[63]:


# loading doc2vec ad word2vec models and functions for return embeddingsfor a paticular text

d2v= Doc2Vec.load("doc2vec_100dim.model")

def document_embeddings(doc):
    test_data = word_tokenize(doc.lower())
    return d2v.infer_vector(test_data)        


word2vec = Word2Vec.load('Word2Vec_100dim.bin')

def Word2doc(doc):
    words=doc.split()
    emb=np.zeros(100)
    for word in words:
        if(word in word2vec.wv.vocab):
            emb = np.add(emb,word2vec[word])
    if(len(doc)!=0) :       
        return emb/len(words)
    else:
        return(np.zeros(100))


# In[65]:


# loading svm model

pickle_in = open("./data/svm_model_cosine.sav","rb")
svm = pickle.load(pickle_in)


# In[66]:


# function to calculate cosin similarity for two embedding

def cosin(v1,v2):
    if(LA.norm(v1)!=0 and LA.norm(v2)!=0):
        return (np.dot(np.array(v1),np.array(v2))/(LA.norm(v1) * LA.norm(v2)))
    else:
        return 1  


# In[73]:


# function to predict similarity between two text using svm model

def text_similarity_svm(text1, text2):
    
    dv1 = document_embeddings(text1)
    dv2 = document_embeddings(text2)
    
    wd1 = Word2doc(text1)
    wd2 = Word2doc(text1)
    
    ds = cosin(dv1,dv2)
    ws = cosin(wd1,wd2)

    data=[]

#     for i in range(0,100):
#         data.append(dv1[i])
    
#     for i in range(0,100):
#         data.append(dv2[i])
    
#     for i in range(0,100):
#         data.append(wd1[i])
    
#     for i in range(0,100):
#         data.append(wd2[i])
    
    data.append(ds)
    data.append(ws)
    
    similarity = svm.predict_proba(np.array(data).reshape(1,2))[0][1]

    
    return similarity


# In[74]:


# function to get mean text similarity score of given corpus(data)

def get_mean_score_pair(data, score_function):
    score_sum = 0.0
    for lr1, lr2 in data:
        score_sum += score_function(lr1, lr2)
    return score_sum/len(data)


# In[75]:


# function to generate next learning learning resource given starting and list of learning resources to select and 
# minimum similarity between them.

def get_next_lr(given_lr, lr_list, target_score, score_function):
    next_lr = ""
    score_diff = 0
    for lr in lr_list:
        score = score_function(given_lr, lr)
        if (score - target_score) > score_diff:
            score_diff = (score - target_score)
            next_lr = lr
    return next_lr


# In[76]:


# function to create a collection/pathway 

def create_collection(collection_size, start_lr, lr_list, target_score, score_function):
    collection = [start_lr]
    lr = start_lr
    if(start_lr in lr_list): 
        lr_list.remove(start_lr)
    for _ in range(collection_size - 1):
        next_lr = get_next_lr(lr, lr_list, target_score, score_function)
        collection.append(next_lr)
        lr = next_lr
        if(next_lr in lr_list):
            lr_list.remove(next_lr)
    return collection


# In[77]:


# all possible pairs of consecutive resources

lr_pairs = []

for col in collections:
    for i in range(len(col["texts"]) -1):
        lr1 = col["texts"][i]
        lr2 = col["texts"][i+1]
        lr_pairs.append((lr1, lr2))


# In[78]:


# calculating mean of all consecutive learning resources
mean = get_mean_score_pair(lr_pairs, text_similarity_svm)


# In[79]:


# list of all learning resources 

lr_lists = []

for col in collections:
    for lr in col["texts"]:
        if(len(lr)>0):
            lr_lists.append(lr)


# In[80]:


# making every resource to repeat only once in the set
lr_lists=list(set(lr_lists))


# In[81]:


# generating a random startng learning resource
start_lr = lr_lists[random.randint(0,len(lr_lists))]
print(start_lr)


# In[88]:


# generating a collection
new_collection = create_collection(8, start_lr, lr_lists, 0.5, text_similarity_svm)


# In[89]:


# print(new_collection)

for i in new_collection:
    print(i)
    print("----------------------------------------------------------------")

