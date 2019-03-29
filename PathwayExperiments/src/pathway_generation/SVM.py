#!/usr/bin/env python
# coding: utf-8

# In[ ]:


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


# In[51]:


df = pd.read_csv("./data/pairwise/results_0_2500.csv")
data = pd.read_csv("./data/pairwise/total_0_2500.csv")

data["SC_score"] = df ["SC_score"]
data["Novelty_score"] = df ["Novelty_score"]

# data.head(5)


# In[52]:


#  generating the target variable
data["vaild_resource"] = data["collection_id1"] == data['collection_id2']        


# In[53]:


# loading doc2vec and word2vec models and function to return doc embedding
model1= Doc2Vec.load("doc2vec_100dim.model")
model2 =Doc2Vec.load("doc2vec_50dim.model")

def document_embeddings(doc,Embedding_size=100):
    test_data = word_tokenize(doc.lower())
    
    if(Embedding_size==100):
        return model1.infer_vector(test_data)        
    elif (Embedding_size == 50):
        return model2.infer_vector(test_data)


# In[54]:


# generating doc embedding for pairwise resources
data["embeddings_text1"] = data.text1.apply(lambda x: document_embeddings(x))
data["embeddings_text2"] = data.text2.apply(lambda x: document_embeddings(x))


# In[55]:


# loading word2vec and function to retuen word embeddings

word2vec = Word2Vec.load('Word2Vec_100dim.bin')

def Word2doc(x):
    words=x.split()
    emb=np.zeros(100)
    for word in words:
        if(word in word2vec.wv.vocab):
            emb = np.add(emb,word2vec[word])
    return emb/len(words)   


# In[56]:


# generating word embedding for pairwise resources

data["word_emb1"]=data.text1.apply(lambda x :Word2doc(x))
data["word_emb2"]=data.text2.apply(lambda x :Word2doc(x))


# In[57]:


# initializing cosine similarity between pairs of document embedding and word embeddings
data["doc_cosine"] = 0
data["word_cosine"] =0


# In[58]:


# functions to calcualte cosine similarity between pairs of document embedding and word embeddings

def cosine_angle_word(x):
    if(LA.norm(x["word_emb1"])!=0 and LA.norm(x["word_emb2"])!=0):
        return (np.dot(np.array(x["word_emb1"]),np.array(x["word_emb2"]))/(LA.norm(x["word_emb1"]) * LA.norm(x["word_emb2"])))
    else:
        return 1
    
def cosine_angle_doc(x):
    if(LA.norm(x["embeddings_text2"])!=0 and LA.norm(x["embeddings_text1"])!=0):
        return (np.dot(np.array(x["embeddings_text1"]),np.array(x["embeddings_text2"]))/(LA.norm(x["embeddings_text1"]) * LA.norm(x["embeddings_text2"])))
    else:
        return 1  


# In[59]:


# calcualting cosine similarity between pairs of document embedding and word embeddings

data["doc_cosine"]= data.apply(cosine_angle_doc,axis=1)
data["word_cosine"]= data.apply(cosine_angle_word,axis=1)


# In[60]:


# encoding the target table 

def encoding(boolean):
    if(boolean):
        return 1
    return 0

data.vaild_resource = data.vaild_resource.apply(lambda x:encoding(x))


# In[61]:


# splitting document embeddings to send as input for svm

columns=["docEmb1_" + str(i) for i in range(1,101)]
data[columns] = pd.DataFrame(data.embeddings_text1.values.tolist(), index= data.index)


columns=["docEmb2_" + str(i) for i in range(1,101)]
data[columns] = pd.DataFrame(data.embeddings_text2.values.tolist(), index= data.index)


# In[62]:


# splitting word embeddings to send as input for svm

columns=["wordEmb1_" + str(i) for i in range(1,101)]
data[columns] = pd.DataFrame(data.word_emb1.values.tolist(), index= data.index)

columns=["wordEmb2_" + str(i) for i in range(1,101)]
data[columns] = pd.DataFrame(data.word_emb2.values.tolist(), index= data.index)


# In[63]:


lda = gensim.models.ldamodel.LdaModel
fileObject = open('./PickelFiles/lda_dictionary.model','rb')  
dictionary = pickle.load(fileObject)
ldamodel = lda.load('./PickelFiles/lda_25_sc.model')


# In[64]:


stop = set(stopwords.words('english'))
exclude = set(string.punctuation) 
lemma = WordNetLemmatizer()
def clean(doc):
    stop_free = " ".join([i for i in doc.lower().split() if i not in stop])
    punc_free = ''.join(ch for ch in stop_free if ch not in exclude)
    normalized = " ".join(lemma.lemmatize(word) for word in punc_free.split())
    return normalized


# In[65]:


data["clean1"]=data.text1.apply(lambda x: clean(x))
data["clean2"]=data.text2.apply(lambda x: clean(x))
data.clean1= data.clean1.apply(lambda x: x.split())
data.clean2 = data.clean2.apply(lambda x: x.split())


# In[66]:


data['clean_matrix1'] = data.clean1.apply(lambda x: dictionary.doc2bow(x))
data['clean_matrix2'] = data.clean2.apply(lambda x: dictionary.doc2bow(x))


# In[67]:


data['lda_topic1'] = data.clean_matrix1.apply(lambda x: ldamodel.get_document_topics(x,per_word_topics=True)[0])
data['lda_topic2'] = data.clean_matrix2.apply(lambda x: ldamodel.get_document_topics(x,per_word_topics=True)[0])


# In[72]:


def KL_Divergence(x):
    a = x["lda_topic1"]
    b = x["lda_topic2"]
    a_list=[]
    b_list=[]
    for i,j in a:
        a_list.append(j)

    for i,j in b:
        b_list.append(j)
    
    a_list=np.array(a_list)
    b_list=np.array(b_list)
    return np.sum(np.where(a_list!=0,a_list*np.log(a_list/b_list),0))

data["kl_divergence"]= 0
data["kl_divergence"]= data.apply(KL_Divergence,axis=1)


# In[76]:


# features_to_use to train svm
 
features_to_use=[]

# for i in range(1,101):
#     features_to_use.append("docEmb1_"+str(i))
#     features_to_use.append("docEmb2_"+str(i))    
#     features_to_use.append("wordEmb1_"+str(i))    
#     features_to_use.append("wordEmb2_"+str(i))

features_to_use.append("doc_cosine")    
features_to_use.append("word_cosine")    
features_to_use.append("SC_score")  
features_to_use.append("Novelty_score")
features_to_use.append("kl_divergence")


# In[77]:


# spliting train and test data
X = data[features_to_use]
y = data["vaild_resource"]

X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.33, random_state=2019 ,shuffle = True)


# In[86]:


# training svm on training data

clf =  OneVsRestClassifier(svm.SVC(probability=True),n_jobs=-1)
clf.fit(X, y)


# In[87]:


# predicting probability on test data
y_pred = clf.predict_proba(X_test)
y_pred


# In[88]:


# predcition on test data
y_pred = clf.predict(X_test)
y_pred


# In[89]:


# accuracy on test data
accuracy_score(y_test, y_pred)


# In[90]:


from sklearn.metrics import confusion_matrix
confusion_matrix(y_test, y_pred)


# In[91]:


# logloss on test data
log_loss(y_test,y_pred)


# In[92]:


# saving the svm model
filename = './data/svm_model_cosine_embed_novelty_SC_LDA.sav'
pickle.dump(clf, open(filename, 'wb'))
 


# In[85]:


# # saving the data file
# data.to_csv("./data/pairwise.csv",index=False)


# In[ ]:




