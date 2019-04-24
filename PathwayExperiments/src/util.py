import pandas as pd
import numpy as np
import re
import random
from nltk.corpus import stopwords
from sklearn.model_selection import train_test_split

collection_topics = ["collection_id", "sequence_id", "resource_id", "title", "description", "Summarization", "lda_topics", "avg_word_emb", "doc_emb"]

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
	
def process_summarization(df, without_stop_words = False):
	# df = get_data(path)
	# #Keeping rows with 'f' as is_deleted value
	# df = df[(df.is_deleted == "f")]
	
	#Copying title into description column when description is null
	# df.loc[df.description.isnull(),"description"] = df.loc[df.description.isnull(),"title"]
	
	df.description.fillna('', inplace=True)
	df.narration.fillna('', inplace=True)
	df.Summarization.fillna(df.title + ". " + df.description, inplace=True)
	df.Summarization = df.apply(lambda x: x['Summarization'] + '. ' + x['narration']  if x['Summarization'][-1]!='.' else x['Summarization'] + x['narration'],axis=1)

	#Cleaning up html descriptions
	df.Summarization = df.Summarization.apply(clean_html)
	
	#Removing rows with null (Only title has null values now in test dataset)
	df.dropna(axis = 'rows')
	
	#Sorting collections
	df = df.sort_values(["collection_id"])
	df.reset_index(drop = True, inplace = True)
	
	#Adding text column to df
	# df.loc[:,"summarizations"] = get_text_from_df(df, without_stop_words)
	# df = df.loc[df.text!="",:]

	#Removing rows with null (Only title has null values now in test dataset)
	df.dropna(axis = 'rows')
	
	#Sorting collections
	df = df.sort_values(["collection_id"])
	df.reset_index(drop = True, inplace = True)

	return df

def get_collection_data(df,i):
	collection = {}
	coll_df = df.loc[(df.collection_id == i),:]
	coll_df = coll_df.sort_values(["sequence_id"])
	collection_topics = ["sequence_id", "resource_id", "title", "description", "Summarization", "lda_topics", "avg_word_emb", "doc_emb"]
	for col in collection_topics:
		collection[col+'s'] = []
	collection["collection_ids"] = i

	for ind, row in coll_df.iterrows():
		for col in collection_topics:
			 collection[col + 's'].append(row[col])

	for col in collection_topics:
		collection[col + 's'] = np.array(collection[col + 's'])

	collection['label'] = np.array([1])
	return collection

def generate_collections(df):
	collection_ids = df.collection_id.unique()
	collections = []
	for i in collection_ids:
		collections.append(get_collection_data(df,i))
	return collections

# def get_random_collections(collections):
# 	n = len(collections)
# 	avg_size = sum([len(col) for col in collections])/n
	
# 	random_collections = []
# 	for i in range(n):
# 		rand_col = []
# #         rand_size = random.randint(int(avg_size/2), int(avg_size*3/2))
# 		first_pick = random.randint(0,n-1)
# 		pick = first_pick
# 		selection = random.randint(0,len(collections[pick])-1)
# 		rand_col.append(collections[pick][selection])
# 		for j in range(1,int(avg_size)):
# 			rand_pick = random.randint(0,n-1)
# 			while(rand_pick == pick):
# 				rand_pick = random.randint(0,n-1)
# 			pick = rand_pick
# 			selection = random.randint(0,len(collections[pick])-1)
# 			rand_col.append(collections[pick][selection])
# 		random_collections.append(rand_col)
# 	return random_collections

def generate_random_collections(df, size, avg_col_length):
	total_df_size = df.shape[0]
	
	collections = []
	for i in range(size):
		collection = {}
		for col in collection_topics:
			collection[col+'s'] = []
		
		for j in range(avg_col_length):
			pick = random.randint(0,total_df_size-1)
			row = df.iloc[pick,:]
			while(row["collection_id"] in collection["collection_ids"]):
				pick = random.randint(0,total_df_size-1)
				row = df.iloc[pick,:]
			for col in collection_topics:
				collection[col+'s'].append(row[col])
			
		for col in collection_topics:
			collection[col+'s'] = np.array(collection[col+'s'])

		collection['label'] = np.array([0])
		collections.append(collection)
	
	return collections

#check
def get_train_test(df, train_ratio = 0.7):
	valid_collection = generate_collections(df)
	random_collection = generate_random_collections(df,df.shape[0],7)
	all_collections = valid_collection.append(random_collection,ignore_index=True)
	train_data,test_data = train_test_split(all_collections,train_size = train_ratio, random_state = 42)
	return train_data,test_data
	
def generate_valid_collections_df(collections):
	df = pd.DataFrame.from_dict(data = collections)
	df['label'] = 1
	return df

def generate_random_collections_df(rand_col):
	rand_df = pd.DataFrame.from_dict(data = rand_col)
	rand_df['label'] = 0
	return rand_df

def get_pairs(collections, rand = False):
	data=[]

	for col in collections:
		ran = min([len(col[col_topic + "s"]) for col_topic in collection_topics])
		for i in range(0,ran-1):
			new_data=[]
			for col_topic in collection_topics:
				if col_topic == "collection_id":
					if rand:
						new_data.append(col[col_topic + "s"][i])
						new_data.append(col[col_topic + "s"][i+1])
					else:
						new_data.append(col[col_topic + "s"])
						new_data.append(col[col_topic + "s"])
				else:
					new_data.append(col[col_topic + "s"][i])
					new_data.append(col[col_topic + "s"][i+1])
			data.append(new_data)
	return data

def generate_pairs_df(collections, rand = False):
	columns = [col+"s"+str(i) for col in collection_topics for i in range(1,3)]
	return pd.DataFrame(data = get_pairs(collections, rand), columns=columns)

