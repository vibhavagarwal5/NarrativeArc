#!/usr/bin/env python
# coding: utf-8

# In[1]:


# importing required modules

import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import os
import re
import util
import random


# In[2]:


# loading data
df=util.get_processed_data("./data/collections_math.csv")
df.head()


# In[3]:


# generating text column using title and description
df['text'] = df[['title', 'description']].apply(lambda x: ' '.join(x), axis=1)
df.info()


# In[8]:


# generating collections
collections = util.get_collections(df)


# In[9]:


# processing the text column
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


# generating new valid paiwise learning resources data
data=[]
for col in collections:
    ran = min(len(col["sequence_ids"]),len(col["resource_ids"]),len(col["texts"])) 
    for i in range(0,ran-1):
        new_data=[]
        
        
        
        new_data.append(col["id"])
        new_data.append(col["id"])
        
        new_data.append(col["sequence_ids"][i])
        new_data.append(col["sequence_ids"][i+1])
        
        new_data.append(col["resource_ids"][i])
        new_data.append(col["resource_ids"][i+1])
        
        new_data.append(col["texts"][i])
        new_data.append(col["texts"][i+1])
        
        data.append(new_data)
    


# In[11]:


# creating a dataframe for the valid resources 
new_df=pd.DataFrame(data = data, columns=["collection_id1","collection_id2","sequence_id1","sequence_id2","resource_id1",
                                    "resource_id2","text1","text2"])
new_df.info()


# In[12]:


# generating new invalid paiwise learning resources data


invalid_data=[]
col_id=df.collection_id.unique()

for i in range(0,31397):
    new_row=[]
    
    col_1=random.choice(collections)
    col_2=random.choice(collections)
    
    min1=min(len(col_1["sequence_ids"]),len(col_1["resource_ids"]),len(col_1["texts"]))
    min2=min(len(col_2["sequence_ids"]),len(col_2["resource_ids"]),len(col_2["texts"]))
    
    
    val_1=random.randint(0,min1-1)
    val_2=random.randint(0,min2-1)
    
#     print(len(col_1["sequence_ids"]))
#     print(len(col_2["sequence_ids"]))
    
#     print("\n")
    
#     print(len(col_1["resource_ids"]))
#     print(len(col_2["resource_ids"]))

#     print("\n")
    
#     print(len(col_1["texts"]))
#     print(len(col_2["texts"]))

#     print("\n")

    
    seq_id1 = col_1["sequence_ids"][val_1]
    seq_id2 = col_2["sequence_ids"][val_2]
    
    res_id1 = col_1["resource_ids"][val_1]
    res_id2 = col_2["resource_ids"][val_2]    
    
    text1 = col_1["texts"][val_1]
    text2 = col_2["texts"][val_2]    
    
    new_row.append(col_1["id"])
    new_row.append(col_2["id"])
   
    new_row.append(seq_id1)
    new_row.append(seq_id2)
    
    new_row.append(res_id1)
    new_row.append(res_id2)
    
    new_row.append(text1)
    new_row.append(text2)
    
    invalid_data.append(new_row)


# In[13]:


# creating a dataframe for the invalide resources 

new_invalid_df=pd.DataFrame(data = invalid_data, columns=["collection_id1","collection_id2","sequence_id1","sequence_id2","resource_id1",
                                    "resource_id2","text1","text2"])
new_invalid_df.head()


# In[14]:


# appending valid and invalid dataframe 
final_df = new_df.append(new_invalid_df , ignore_index=True)


# In[15]:


final_df.info()


# In[16]:


final_df.head()


# In[17]:


# saving data into data files
new_df.to_csv("./data/math_valid_pairwise_res.csv")
new_invalid_df.to_csv("./data/math_invalid_pairwise_res.csv")
final_df.to_csv("./data/math_final_pairwise.csv")


# In[ ]:




