#!/usr/bin/env python
# coding: utf-8

# In[16]:


# importing modules

import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import os
import re
import util
import ast
import pickle
import math
from scipy.spatial.distance import cosine
import subprocess

from nltk.corpus import stopwords 
from nltk.stem.wordnet import WordNetLemmatizer
import string
import gensim
import pyLDAvis.gensim
from numpy import linalg as LA

from sklearn.metrics import log_loss
from sklearn.metrics import accuracy_score
from sklearn.model_selection import train_test_split
from sklearn import svm
from gensim.models.doc2vec import Doc2Vec, TaggedDocument
from gensim.models import Word2Vec
from nltk.tokenize import word_tokenize
from sklearn.multiclass import OneVsRestClassifier


# In[14]:


df = util.get_processed_data("./../data/collections_math.csv", False)
collections = util.get_collections(df)
df.text[0]


# In[7]:


lda = gensim.models.ldamodel.LdaModel
fileObject = open('./../PickelFiles/lda_dictionary.model','rb')  
dictionary = pickle.load(fileObject)
ldamodel = lda.load('./../PickelFiles/lda_25_sc.model')


# In[8]:


stop = set(stopwords.words('english'))
exclude = set(string.punctuation) 
lemma = WordNetLemmatizer()
def clean(doc):
    stop_free = " ".join([i for i in doc.lower().split() if i not in stop])
    punc_free = ''.join(ch for ch in stop_free if ch not in exclude)
    normalized = " ".join(lemma.lemmatize(word) for word in punc_free.split())
    return normalized


# In[9]:


df.info()


# In[10]:


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


# In[10]:


# loading doc2vec ad word2vec models and functions for return embeddingsfor a paticular text

d2v= Doc2Vec.load("./../doc2vec_100dim.model")

def document_embeddings(doc):
    test_data = word_tokenize(doc.lower())
    return d2v.infer_vector(test_data)        


word2vec = Word2Vec.load('./../Word2Vec_100dim.bin')

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


# In[12]:


# loading svm model

pickle_in = open("./../data/svm_model_cosine_embed_novelty_SC.sav","rb")
svm_fun = pickle.load(pickle_in)


# In[13]:


# function to calculate cosin similarity for two embedding

def cosin(v1,v2):
    if(LA.norm(v1)!=0 and LA.norm(v2)!=0):
        return (np.dot(np.array(v1),np.array(v2))/(LA.norm(v1) * LA.norm(v2)))
    else:
        return 1  


# In[17]:


# java -jar NoveltySemanticCoherence.jar  SinglePair

def getNoveltySC(text1,text2):
    cmd =['java','-jar','NoveltySemanticCoherence.jar','SinglePair',text1,text2]
    subprocess.call(cmd)
    f = open("./data/pairwise/singlepairwise", "r")
    sc =float(f.readline()) 
    nv = float(f.readline())
    return sc,nv


# In[28]:


# ldamodel.get_document_topics(["solve"],per_word_topics=True)


# In[37]:


# function to predict similarity between two text using svm model

def text_similarity_svm(text1, text2):
    
    dv1 = document_embeddings(text1)
    dv2 = document_embeddings(text2)
    
    wd1 = Word2doc(text1)
    wd2 = Word2doc(text1)
    
    ds = cosin(dv1,dv2)
    ws = cosin(wd1,wd2)

    clean_matrix1 = clean(text1).split()
    clean_matrix2 = clean(text2).split() 

    clean_matrix1 = dictionary.doc2bow(clean_matrix1)  
    clean_matrix2 = dictionary.doc2bow(clean_matrix2) 
    
    lda_1 = ldamodel.get_document_topics(clean_matrix1,per_word_topics=True)[0] 
    lda_2 = ldamodel.get_document_topics(clean_matrix2,per_word_topics=True)[0] 

    print(lda_1)
#     lda_1 = clean_matrix1.apply(lambda x: ldamodel.get_document_topics(x,per_word_topics=True)[0])
#     lda_2 = clean_matrix1.apply(lambda x: ldamodel.get_document_topics(x,per_word_topics=True)[0])

    a_list=[]
    b_list=[]
    for i,j in lda_1:
        a_list.append(j)

    for i,j in lda_2:
        b_list.append(j)
    
    a_list=np.array(a_list)
    b_list=np.array(b_list)

    kl = np.sum(np.where(a_list!=0,a_list*np.log(a_list/b_list),0))    
    sc,nv =getNoveltySC(text1,text2)
    
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
    data.append(sc)
    data.append(nv)
    data.append(kl)
    
    
    similarity = svm_fun.predict_proba(np.array(data).reshape(1,5))[0][1]

    
    return similarity


# In[40]:


# function to get mean text similarity score of given corpus(data)

def get_mean_score_pair(data, score_function):
    score_sum = 0.0
    for lr1, lr2 in data:
        score_sum += score_function(lr1, lr2)
    return score_sum/len(data)


# In[41]:


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


# In[42]:


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


# In[43]:


# all possible pairs of consecutive resources

lr_pairs = []

for col in collections:
    for i in range(len(col["texts"]) -1):
        lr1 = col["texts"][i]
        lr2 = col["texts"][i+1]
        lr_pairs.append([lr1, lr2])


# In[44]:


all_cosine = []

for pair in lr_pairs:
    wv1=Word2doc(pair[0])
    wv2=Word2doc(pair[1])
    all_cosine.append(cosin(wv1,wv2))

# calculating mean of all consecutive learning resources
# mean = get_mean_score_pair(lr_pairs, text_similarity_svm)


# In[45]:


plt.hist(all_cosine,bins=30)
plt.ylabel('Cosine Similarity');    
plt.show()  


# In[46]:


def sortSecond(val): 
    return val[1]


# In[47]:


# list of all learning resources 

lr_list = []
col_list = [] 
count = 0
for col in collections:
    if(len(col["texts"])>6): col_list.append(col["id"])
    for lr in col["texts"]:
        if(len(lr)>0):
            lr_list.append(lr)


# In[48]:


def generating_collection(seed_val,threshold):
    random.seed(seed_val)
    start_lr = lr_list[random.randint(0,len(lr_list))]
    print(start_lr)
    temp_list = [] 
    wd1 = Word2doc(start_lr)
    for i in lr_list:
        wd2 = Word2doc(i)
        ws = cosin(wd1,wd2)
        temp_list.append([i,ws])
    temp_list.sort(key = sortSecond ,reverse = True)

    hist = []
    for i in temp_list:
        hist.append(i[1])
    plt.hist(hist,bins=60)
    plt.ylabel('Cosine Similarity');    
    plt.show()    
    
    neig_list = [] 
    for i in range(0,min(10,len(temp_list))):
        if(temp_list[i][1]!=1 and temp_list[i][0] not in neig_list):
            neig_list.append(temp_list[i][0])
    
    
    new_collection = create_collection(8, start_lr, neig_list, threshold, text_similarity_svm)

    # print(new_collection)
    f = open("./data/generated_collection/collection_KL"+str(seed_val)+"_"+str(threshold),"w")
    for i in new_collection:
        f.write(i+"\n")
        f.write("--------------------------------------------------------------\n")
        print(i)
        print("----------------------------------------------------------------")


# In[ ]:


generating_collection(2019,0.5)


# In[ ]:


generating_collection(2016,0.5)


# In[ ]:


generating_collection(1,0.5)


# In[8]:


text1= "Solve the Linear equation"
text2 = "Lesson I ll say an addition or subtraction sentence You say the answer sevenths seventh"


# In[18]:


sc1,nv1 = getNoveltySC(text1,text1)
sc2,nv2 = getNoveltySC(text1,text2)
sc3,nv3 = getNoveltySC(text2,text1)


# In[19]:


sc1,nv1


# In[20]:


sc2,nv2


# In[22]:


sc3,nv3


# In[ ]:




