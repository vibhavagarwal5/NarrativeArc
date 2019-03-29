#!/usr/bin/env python
# coding: utf-8

# In[1]:


import os
import util
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import os
import re
from numpy import linalg as LA
import random
import os.path,subprocess
import time


# In[2]:



# loading the model
df = util.get_processed_data("./../data/collections_math.csv", False)
collections = util.get_collections(df)


# In[3]:


def getNoveltySC(text1,text2):
    cmd =['java','-jar','NoveltySemanticCoherence.jar','SinglePair',text1,text2]
    subprocess.call(cmd)
    f = open("./data/pairwise/singlepairwise", "r")
    sc =float(f.readline()) 
    nv = float(f.readline())
    return sc,nv


# In[4]:


df["collection_id"].unique()


# In[5]:


def novelty_testing(col_id):
    test = df[df["collection_id"]==col_id]
    test = test.sort_values(by=["sequence_id"])
    temp = test.text
    texts = [] 
    for i in temp :
        texts.append(i)
    adjacent = []
    for i in range(0,len(texts)-1):
        adjacent.append(getNoveltySC(texts[i],texts[i+1])[1])

    non_adjacent = []    
    for i in range (0,len(texts)-2):
        non_adjacent.append(getNoveltySC(texts[i],texts[i+2])[1])
    return adjacent,non_adjacent


# In[25]:


adj,nadj = novelty_testing("001bf2c6-8ede-478a-9b8b-d7750488cb1b")
test = df[df["collection_id"]=="001bf2c6-8ede-478a-9b8b-d7750488cb1b"]
test = test.sort_values(by=["sequence_id"])
temp = test.text

for i in temp :
        print("------------------------------")
        print(i)
print("---------------------------------")
print(adj)
print("---------------------------------")
print(nadj)        


# In[26]:


adj1,nadj1 = novelty_testing("ffea9e8f-65e1-4731-bebb-140174f60cda")
test = df[df["collection_id"]=="ffea9e8f-65e1-4731-bebb-140174f60cda"]
test = test.sort_values(by=["sequence_id"])
temp = test.text

for i in temp :
        print("------------------------------")
        print(i)
print("---------------------------------")
print(adj1)
print("---------------------------------")
print(nadj1)    


# In[28]:


adj2,nadj2 = novelty_testing("fff795b2-263c-48cb-8f93-74693e8c9c58")
test = df[df["collection_id"]=="fff795b2-263c-48cb-8f93-74693e8c9c58"]
test = test.sort_values(by=["sequence_id"])
temp = test.text

for i in temp :
        print("------------------------------")
        print(i)
print("---------------------------------")
print(adj2)
print("---------------------------------")
print(nadj2)  


# In[29]:


adj3,nadj3 = novelty_testing("001bf2c6-8ede-478a-9b8b-d7750488cb1b")
test = df[df["collection_id"]=="001bf2c6-8ede-478a-9b8b-d7750488cb1b"]
test = test.sort_values(by=["sequence_id"])
temp = test.text

for i in temp :
        print("------------------------------")
        print(i)
print("---------------------------------")
print(adj3)
print("---------------------------------")
print(nadj3)  


# In[32]:


adj4,nadj4 = novelty_testing("00209d65-4e2f-4649-be25-c4731794be6f")
test = df[df["collection_id"]=="001bf2c6-8ede-478a-9b8b-d7750488cb1b"]
test = test.sort_values(by=["sequence_id"])
temp = test.text
for i in temp :
        print("------------------------------")
        print(i)
print("---------------------------------")
print(adj4)
print("---------------------------------")
print(nadj4)  


# In[33]:


random_sample = df.text.sample(n = 10)
texts = []
for i in random_sample:
    print("---------------------------------------------")
    print(i)
    texts.append(i)
adjacent = []
for i in range(0,len(texts)-1):
    adjacent.append(getNoveltySC(texts[i],texts[i+1])[1])

non_adjacent = []    
for i in range (0,len(texts)-2):
    non_adjacent.append(getNoveltySC(texts[i],texts[i+2])[1])
print("--------------------------------------------------")
print(adjacent)
print("--------------------------------------------------")
print(non_adjacent)


# In[34]:


a="Lesson I ll say an addition or subtraction sentence You say the answer sevenths seventh"
b="Lesson I ll say an addition or subtraction sentence You say the answer sevenths seventh"
c = "Adding and Subtracting Fractions with Like Units This fluency activity reviews adding and subtracting like units mentally"
sc1,nv1 = getNoveltySC(a,b)
sc2,nv2 = getNoveltySC(a,c)


# In[35]:


print(nv1,nv2)


# In[ ]:




