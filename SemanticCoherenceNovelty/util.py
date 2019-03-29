import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import re
import random
from nltk.corpus import stopwords
import nltk

def get_data(path):
    
    df = pd.read_csv(path)
    return df
    

def clean_html(string):
    new_line_removed_string = re.sub(r'\n','',string)
    return re.sub(r'<.*?>',' ',new_line_removed_string)

def get_alphanumeral(string):
    return re.sub(r"[^a-zA-Z0-9]+"," ",string).strip()

def get_alpha(string):
    return re.sub(r"[^a-zA-Z]+"," ",string).strip()

def remove_stop_words(text):
    stop_words = stopwords.words('english')
    processed_text = ""
    for word in text.split():
        if word.lower() not in stop_words:
            processed_text+= word+" "
    return processed_text.strip()

def get_text_from_df(df, without_stop_words = False):
    #returns text concat(title,description) keeping only alphabets
    text_list = []
    for t,d in zip(df.loc[:,"title"], df.loc[:,"description"]):
        text = str(t)+" "+str(d)
        #removing all non-alphabets
        text = get_alpha(text)
        if (without_stop_words == True):
            text = remove_stop_words(text)
        text_list.append(text)
    return np.array(text_list)
    
def get_processed_data(path, without_stop_words = False):
    df = get_data(path)
    #Keeping rows with 'f' as is_deleted value
    df = df[(df.is_deleted == "f")]
    
    #Copying title into description column when description is null
    df.loc[df.description.isnull(),"description"] = df.loc[df.description.isnull(),"title"]
    
    #Cleaning up html descriptions
    df.loc[df.description.apply(lambda x: "<" in x), "description"] = df.loc[df.description.apply(lambda x: "<" in x), "description"].apply(lambda x: clean_html(x))
    
    #Adding text column to df
    df.loc[:,"text"] = get_text_from_df(df, without_stop_words)
    
    df = df.loc[df.text!="",:]


    #Removing rows with null (Only title has null values now in test dataset)
    df.dropna(axis = 'rows')
    
    #Dropping is_deleted column
    df = df.drop(columns = ["is_deleted"])
    
    
    #Sorting collections
    df = df.sort_values(["collection_id"])
    
    #Resetting indexes
    df.reset_index(drop = True, inplace = True)

    return df


def get_collection_data(df,i):
    collection = {}
    coll_df = df.loc[(df.collection_id == i),:]
    coll_df = coll_df.sort_values(["sequence_id"])
    collection["id"] = i
    collection["sequence_ids"] = []
    collection["resource_ids"] = []
    collection["titles"] = []
    collection["descriptions"] = []
    collection["texts"] = []

    for ind, row in coll_df.iterrows():
        collection["sequence_ids"].append(row["sequence_id"])
        collection["resource_ids"].append(row["resource_id"])
        collection["titles"].append(row["title"])
        collection["descriptions"].append(row["description"])
        collection["texts"].append(row["text"])

    collection["sequence_ids"] = np.array(collection["sequence_ids"])
    collection["resource_ids"] = np.array(collection["resource_ids"])
    collection["titles"] = np.array(collection["titles"])
    collection["descriptions"] = np.array(collection["descriptions"])
    collection["texts"] = np.array(collection["texts"])


    return collection

def get_collections(df):
    collection_ids = df.collection_id.unique()
    collections = []
    for i in collection_ids:
        collections.append(get_collection_data(df,i))
    return collections

def get_random_collections(collections):
    n = len(collections)
    avg_size = sum([len(col) for col in collections])/n
    
    random_collections = []
    for i in range(n):
        rand_col = []
#         rand_size = random.randint(int(avg_size/2), int(avg_size*3/2))
        first_pick = random.randint(0,n-1)
        pick = first_pick
        selection = random.randint(0,len(collections[pick])-1)
        rand_col.append(collections[pick][selection])
        for j in range(1,int(avg_size)):
            rand_pick = random.randint(0,n-1)
            while(rand_pick== pick):
                rand_pick = random.randint(0,n-1)
            pick = rand_pick
            selection = random.randint(0,len(collections[pick])-1)
            rand_col.append(collections[pick][selection])
        random_collections.append(rand_col)
    return random_collections


def get_train_test(collections, train_ratio = 0.7):
    #Assuming collections is a 2-d list of learning resource text
    n = len(collections)
    
    if n==0:
        return [],[]
    if n==1:
        return collections[0], collections[0] 
    
    random.shuffle(collections)

    train_data = []
    test_data = []
    
    train_collections = collections[:int(n*train_ratio)]
    train_random_collections = get_random_collections(train_collections)
    
    for col in train_collections:
        train_data.append((col,1))
        
    for col in train_random_collections:
        train_data.append((col,0))
    
    
    test_collections = collections[int(n*train_ratio):]
    test_random_collections = get_random_collections(test_collections)
   
    for col in test_collections:
        test_data.append((col,1))
    
    for col in test_random_collections:
        test_data.append((col,0))
    
    random.shuffle(train_data)
    random.shuffle(test_data)
    
    return np.array(train_data), np.array(test_data)
    
