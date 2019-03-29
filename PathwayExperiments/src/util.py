import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import re
import random
from nltk.corpus import stopwords
import nltk
from class_util import *
from sklearn.model_selection import train_test_split

def get_data(path):
	#Returns dataframe read from path
	df = pd.read_csv(path)
	return df

def clean_html(string):
	#Removes sub-strings with html content (pattern: <anything>)
	new_line_removed_string = re.sub(r'\n','',string)
	return re.sub(r'<.*?>',' ',new_line_removed_string)

def get_alphanumeral(string):
	#Keeps only alphanumeral characters in the string
	return re.sub(r"[^a-zA-Z0-9]+"," ",string).strip()

def get_alpha(string):
	#Keeps only alphabets in the string
	return re.sub(r"[^a-zA-Z]+"," ",string).strip()

def remove_stop_words(text):
	#Removes stop words (from nltk library) from given string
	stop_words = stopwords.words('english')
	processed_text = ""
	for word in text.split():
		if word.lower() not in stop_words:
			processed_text+= word+" "
	return processed_text.strip()

def get_text_from_df(df, without_stop_words = False):
	#returns text concat(title,description) keeping only alphabets.
	#Optionally also removes stop words
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

def get_texts_from_collections(collections):
	#Retuns list of learning resource texts in the collection
	documents = []
	for col in collections:
		for lr in col.learning_resources:
			documents.append(lr.text)
	return documents

def get_collection(df,id):
   	#Returns collection object using rows in dataframe(df) with collection_id value of id
	coll_df = df.loc[(df.collection_id == id),:]
	coll_df = coll_df.sort_values(["sequence_id"])
	collection = Collection(id) 

	for ind, row in coll_df.iterrows():
		seq_id = row["sequence_id"]
		res_id = row["resource_id"]
		title = row["title"]
		description = row["description"]
		text = row["text"]

		learning_resource = LearningResource(seq_id, res_id, title, description, text)
		collection.add_learning_resource(learning_resource)    

	return collection

def get_collections(df):
	#Returns list of collection objects using data in the dataframe(df) 
	collection_ids = df.collection_id.unique()
	collections = []
	for i in collection_ids:
		collections.append(get_collection(df,i))
	
	for col in collections:
		lrs = []
		for i,text in enumerate(col["texts"]):
			new_text = ""
			for word in text.split():
				if len(word)>1:
					new_text+=word+" "
			if new_text.strip()!="":
				lrs.append(new_text.strip().lower())
		col["texts"] = lrs

	return collections

def get_random_collections(collections):
	#Using collections (each one a list of texts), returns a random collection of average size
	n = len(collections)
	avg_size = sum([len(col) for col in collections])/n
	
	random_collections = []
	for i in range(n):
		rand_col = []
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
	valid_collection = generate_valid_collections(collections)
	random_collection = generate_random_collections(collections)
	valid_collection = valid_collection.append(random_collection,ignore_index=True)
	train_data,test_data = train_test_split(valid_collection,train_size = train_ratio, random_state = 42)
	return train_data,test_data
	
def generate_valid_collections(collections):
	val_collection = []
	coll = [col["texts"] for col in collections]
	for col in coll:
		val_collection.append((col,1))
	df = pd.DataFrame(val_collection,columns=['texts','label'])
	return df

def generate_random_collections(collections):
	random_collection = []
	coll = get_random_collections([col["texts"] for col in collections])
	for col in coll:
		random_collection.append((col,0))
	df = pd.DataFrame(random_collection,columns=['texts','label'])
	return df

def get_topic_data(df,topic):
	# Generates subject wise dataframe from the taxonomy column
	df = df[df['subject'].str.contains(': "' + topic + '"') == True]
	return df
def save_dataframe(df, name):
	df.to_csv("./data/"+name, index=False)
