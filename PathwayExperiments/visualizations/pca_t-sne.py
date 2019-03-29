#!/usr/bin/env python
# coding: utf-8

# In[6]:


# importing required modules

import pandas as pd
import numpy as np
import random
from pandas import Timestamp
get_ipython().run_line_magic('matplotlib', 'inline')
import matplotlib.pyplot as plt
from matplotlib import pyplot
from mpl_toolkits.mplot3d import Axes3D

import time
from sklearn.decomposition import PCA 
from sklearn.manifold import TSNE


# In[7]:


# Loading Document Embeddings data

df = pd.read_csv('./data/embeddings_gensim.csv')
df.info()


# In[8]:


# Droping unnecessary columns

df.columns
df.drop(['Unnamed: 0'],inplace = True,axis = 1)


# In[9]:


# valid resources
# 0008d66a-753f-4639-8634-81bb3abb3269 ->linear equation
# 001bf2c6-8ede-478a-9b8b-d7750488cb1b  -> addition and subtraction 
# 0025274f-8826-472b-9ea5-16b55bad990d ->constructing box whisker plot
# 00b3d9ad-8df6-4144-ad09-a469144b9075 -> pythagorean theorem
# 015de589-5d6d-4678-8424-5711d4836bf2 -> homework


# In[10]:


# valid resources list
collection_list =['0008d66a-753f-4639-8634-81bb3abb3269' ,'001bf2c6-8ede-478a-9b8b-d7750488cb1b' ,
                  '0025274f-8826-472b-9ea5-16b55bad990d','00b3d9ad-8df6-4144-ad09-a469144b9075',
                  '015de589-5d6d-4678-8424-5711d4836bf2',
                 ]


# In[11]:



# no_of_learning_resources = 4
# seed =123

# #  list of collections with more than minimum no of resources
# minimum_no_resources = 8
# collection_list = df[df.sequence_id > minimum_no_resources].collection_id.unique()

# # generating a random number to choose starting point for required number of collection from collection list
# random.seed(seed)
# random_start = random.randint(1, len(collection_list) - no_of_learning_resources)

# # cretaing a list of collection id to plot
# collection_list = collection_list[random_start:random_start+no_of_learning_resources]
# collection_list


# In[12]:


# verifying where resourse is there in collection list 

def isinCollectionList(col_id):
    if col_id in collection_list:
        return True
    else:
        return False
    
df["isinCollectionList"] = df.collection_id.apply(lambda x:isinCollectionList(x))    

# extractiong resoures which are in collection list
# data = df[df["isinCollectionList"]]
# data.info()


# In[13]:


# Dimension reduction using PCA

# required number of dimensions
n_components=3

# coloumns list to apply dimension reduction
feat_cols = ["vec_" + str(i) for i in range(1,51)]

# dimenstion reduction
pca = PCA(n_components)
pca_result = pca.fit_transform(df[feat_cols].values)

# results of pca in 3 dimensions
df['pca-one'] = pca_result[:,0]
df['pca-two'] = pca_result[:,1] 
df['pca-three'] = pca_result[:,2]

# variation in all the 3 dimensions
print ('Explained variation per principal component: {}'.format(pca.explained_variance_ratio_))
print ('Cumulative variation {}'.format(np.sum(pca.explained_variance_ratio_)))


# In[14]:


# extractiong resoures which are in collection list
data = df[df["isinCollectionList"]]
data.info()


# In[15]:


# import ggplot
from ggplot import *

chart = ggplot( data.loc[:,:], aes(x='pca-one', y='pca-two', color='collection_id') )         + geom_point(size=75,alpha=1)         + ggtitle("First and Second Principal Components colored by digit")

chart.show()


# In[16]:


# dimension reduction using t_sne

# number of epochs
n_iter = 300

# no of dimensions to reduce to
n_components = 3

# start time of pca to estimate time calculation
time_start = time.time()

# tsne dimension reduction
tsne = TSNE(n_components, verbose=1, perplexity=40,n_iter = n_iter )
tsne_results = tsne.fit_transform(df.loc[:,feat_cols].values)

#time elapsed to compute tsne
print ('t-SNE done! Time elapsed: {} seconds'.format(time.time()-time_start))


# In[17]:


# extractiong resoures which are in collection list
data = df[df["isinCollectionList"]]
data.info()


# In[18]:


# plotting the tsne results in 2d space

data_tsne = data.loc[:,:].copy()
df['x-tsne'] = tsne_results[:,0]
df['y-tsne'] = tsne_results[:,1]
df['z-tsne'] = tsne_results[:,2]

# chart.save("tsne.png")


# In[19]:


# extractiong resoures which are in collection list
data = df[df["isinCollectionList"]]
data.info()

chart = ggplot( data.loc[:,:], aes(x='x-tsne', y='y-tsne', color='collection_id') )         + geom_point(size=70,alpha=1)         + ggtitle("tSNE dimensions colored by digit")

chart.show()


# In[20]:


# # plotting of pca results in 3 dimension space

# fig = pyplot.figure(figsize=[10 ,10])
# ax = fig.add_subplot(111,projection='3d')

# sequence_containing_x_vals = data['pca-one']
# sequence_containing_y_vals = data['pca-two']
# sequence_containing_z_vals = data['pca-three']

# ax.set_xlabel('pca-one')
# ax.set_ylabel('pca-two')
# ax.set_zlabel('pca-three')



# ax.scatter(sequence_containing_x_vals, sequence_containing_y_vals, sequence_containing_z_vals ,c ='r',marker ='o')
# pyplot.show()


# In[21]:


# # plotting of two results in 3 dimension space


# fig = pyplot.figure(figsize=[10 ,10])
# ax = fig.add_subplot(111,projection='3d')

# sequence_containing_x_vals = data_tsne['x-tsne']
# sequence_containing_y_vals = data_tsne['y-tsne']
# sequence_containing_z_vals = data_tsne['z-tsne']

# ax.set_xlabel('x-tsne')
# ax.set_ylabel('y-tsne')
# ax.set_zlabel('z-tsne')


# ax.scatter(sequence_containing_x_vals, sequence_containing_y_vals, sequence_containing_z_vals ,c ='r',marker ='o')
# pyplot.show()


# In[22]:


df.info()


# In[23]:


# savaing the data
df.to_csv('plotting_data1.csv')


# In[ ]:




