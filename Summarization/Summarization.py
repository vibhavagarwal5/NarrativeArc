from pytube import YouTube
import pandas as pd
import numpy as np
import subprocess as sp
import moviepy.editor as mp
import urllib2
import sys
import json
import textract
import PyPDF2
import textrank
import pdfminer
import os
from bs4 import BeautifulSoup
import io
import re
from selenium import webdriver
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait
from selenium.common.exceptions import TimeoutException
import time
import urlparse, os
import requests
import urllib
import speech_recognition as sr
from pydub import AudioSegment
from collections import OrderedDict
from sumy.parsers.plaintext import PlaintextParser
from sumy.nlp.tokenizers import Tokenizer 
from sumy.summarizers.lex_rank import LexRankSummarizer
import warnings
warnings.filterwarnings("ignore") 

chrome_path = '/Users/vibhavagarwal/Downloads/chromedriver'
options = webdriver.ChromeOptions()
options.add_argument('headless')
options.add_argument('--ignore-certificate-errors')
driver = webdriver.Chrome(chrome_path, options = options)

def BS4(url):
	driver.get(url)
	try:
		WebDriverWait(driver, 2).until(EC.alert_is_present())
		driver.switch_to_alert().dismiss()
	except TimeoutException:
		pass
	html = driver.page_source
	soup = BeautifulSoup(html)
	for script in soup(["script", "style"]):
		script.extract()
	text = soup.get_text()
	lines = (line.strip() for line in text.splitlines())
	chunks = (phrase.strip() for line in lines for phrase in line.split("  "))
	text = ' '.join(chunk for chunk in chunks if chunk)
	return text

def pdf2text(url):
	urllib.urlretrieve(url,'files.pdf')
	text = textract.process('files.pdf')
	return text
	
def embedded_pdf_text_by_replace_title(url):
	try:
		driver.get(url)
		try:
			WebDriverWait(driver, 2).until(EC.alert_is_present())		
			driver.switch_to_alert().dismiss()
		except TimeoutException:
			pass
		html = driver.page_source
		title = driver.title
		last_name = url.rsplit('/', 1)[-2]
		url_new = last_name +'/' + title
		urllib.urlretrieve(url_new,'files.pdf')
		text = textract.process('files.pdf')
	except Exception as e:
		print(e)
		text = ''
	return text

def embedded_pdf_text(url):
	try:
		driver.get(url)
		try:
			WebDriverWait(driver, 2).until(EC.alert_is_present())		
			driver.switch_to_alert().dismiss()
		except TimeoutException:
			pass
		html = driver.page_source
		text = ''
		pdfs = []
		elems = driver.find_elements_by_xpath("//a[@href]")
		for elem in elems:
			url2= elem.get_attribute("href")
			path = urlparse.urlparse(url2).path
			ext = os.path.splitext(path)[1]
			if(ext == '.pdf'):
				pdfs.append(url2)
		myset = set(pdfs)
		for x in myset:
			urllib.urlretrieve(x,'files.pdf')
			text = text + textract.process('files.pdf')   
	except Exception as e:
		print(e)
		text = ''  
	return text

def docToText(link):
	url = link.split('?')[0]
	extension = url.split('.')[-1]
	urllib.urlretrieve(url,'files.'+extension)
	text = textract.process('files.doc', method = 'antiword')
	return text

def download_doc(link):
	id = link.split('/')[-2]
	url = 'https://docs.google.com/document/export?format=doc&id='+id
	urllib.urlretrieve(url,'files.docx')
	text = textract.process('files.docx', method= 'python-docx2txt')
	return text
	
def docxToText(link):
	url = link.split('?')[0]
	extension = url.split('.')[-1]
	urllib.urlretrieve(url,'files.'+extension)
	text = textract.process('files.docx', method = 'python-docx2txt')
	return text 

	
def pptxToText(link):
	url = link.split('?')[0]
	extension = url.split('.')[-1]
	urllib.urlretrieve(url,'files.'+extension)
	text = textract.process('files.doc', method = 'python-pptx')
	return text

def msgToText(link):
	url = link.split('?')[0]
	extension = url.split('.')[-1]
	urllib.urlretrieve(url,'files.'+extension)
	text = textract.process('files.doc', method = 'msg-extractor')
	return text 

def audio_extractor():
	r = sr.Recognizer()
	with sr.AudioFile('files.wav') as source:
		audio = r.record(source)
	try:
		return r.recognize_sphinx(audio)
	except Exception as e:
		print(e)
		print('Not recognized audio')
		return NULL

def audioToText(link):
	text = ''
	url = link.split('?')[0]
	extension = url.split('.')[-1]
	urllib.urlretrieve(url,'files.'+extension)
	if extension == 'mp3':
		sound = AudioSegment.from_mp3('files.mp3')
		sound.export('files.wav',format='wav')
		text = audio_extractor()
	elif extension == 'wav':
		text = audio_extractor()
	return text

def embedded_audio(url):
	driver.get(url)
	html = driver.page_source
	text = ''
	reg_mp3 = re.findall(r'http[a-zA-Z\"\',?$!@$%^&.:/\\0-9_]*mp3', html)
	for x in reg_mp3:
		x = x.replace('\\', '')
		text = text + audioToText(x)
	reg_wav = re.findall(r'http[a-zA-Z\"\',?$!@$%^&.:/\\0-9_]*wav', html)
	for x in reg_wav:
		x = x.replace('\\', '')
		text = text + audioToText(x)
	return text

def pdf_imageToText(link):
	text = ''
	url = link.split('?')[0]
	extension = url.split('.')[-1]
	urllib.urlretrieve(url,'files.'+extension)
	text= textract.process('files.'+extension, method='tesseract')
	return text

def embedded_image_to_text(url):
	driver.get(url)
	html = driver.page_source
	#From here it is extraction of pdfs
	text =''
	text1 = ''
	pdfs = []
	elems = driver.find_elements_by_tag_name("img")
	for elem in elems:
		url2= elem.get_attribute("src")
		path = urlparse.urlparse(url2).path
		pdfs.append(url2)
	myset = set(pdfs)
	for x in myset:
		try:
			text1 = pdf_imageToText(x)
		except Exception as e:
			print(e)
			text1 = ''
		text = text + text1
	return text

def videoToText(link):
	url = link.split('?')[0]
	extension = url.split('.')[-1]
	if extension == 'mp4':
		mp4file = urllib2.urlopen(url)
		with open("files.mp4", "wb") as handle:
			handle.write(mp4file.read())
		clip = mp.VideoFileClip('files.mp4')
		clip.audio.write_audiofile('files.wav')
		text = audio_extractor()
	elif 'youtube' in link:
		yt = YouTube(link)
		stream = yt.streams.filter(file_extension='mp4').first().download(filename='files')
		clip = mp.VideoFileClip('files.mp4')
		clip.audio.write_audiofile('files.wav')
		text = audio_extractor()
	else:
		text = ''
	return text 

def embedded_video_to_text(url):
	driver.get(url)
	html = driver.page_source
	text = ''
	text1 = ''
	videos = []
	reg_youtube = re.findall('http[a-zA-Z\"\',?$!@$%^&.:/\\0-9_]*.mp4',html)
	
	for x in reg_youtube:
		videos.append(x)
		
	reg_youtube = re.findall(r'https://www.youtube.com/embed/[a-zA-Z\"\',?$!@$%^&.:/\\0-9_]*?autoplay=1',html)
	for x in reg_youtube:
		x = x.replace('\\', '')
		x = x.split('?')[0]
		x = x.split('/')[-1]
		x = 'https://www.youtube.com/watch?v='+n
		videos.append(x)
	myset = set(videos)
	for k in myset:
		try:
			text1 = videoToText(k)
		except Exception as e:
			print(e)
			text1 = ''
		text = text + text1
	return text

def removeNonAscii(s): 
	return "".join(filter(lambda x: ord(x)<128, s))

def summary(text):
	sentences = len(text.split('.'))
	t = ''
	#save the file
	text_file = open('temp.txt','w')
	text_file.write(text)
	text_file.close()
	#Summarize the document with 20% sentences
	parser = PlaintextParser.from_file('temp.txt', Tokenizer("english"))
	summarizer = LexRankSummarizer()
	lists =  summarizer(parser.document, 5)
	for x in lists:
		t = t + str(x)
	t=t.rstrip()
	return t

def remove_error_summ(data):
	unwanted_words = ['Access denied','401','403','404','Not Found','Unauthorized','HTTP','%PDF']
	for words in unwanted_words:
		data.Summarization[data.Summarization.str.contains(words) == True] = np.nan
	data.Summarization = data.Summarization.apply(lambda x: np.nan if len(list(str(x).split(" ")))<10 or len(str(x))<25 else x)
	return data

if __name__ == "__main__":

	csv_file_path = sys.argv[1]
	data = pd.read_csv(csv_file_path)
	data["Summarization"] = np.nan
	possible_extension = ['pdf','mp3','wav','docx','doc','csv','jpg','jpeg','gif','ogg','txt','epub','eml','rtf','png','ps','tiff','tif','msg','pptx','odt','html','htm','mp4']

	# MAIN PROGRAM FOR WEBPAGE, TEXT AND INTERACTIVE
	records = data[data.content_subformat.isin(['text_resource','webpage_resource','interactive_resource'])]
	links = records.url
	index = records.index 

	for index, url in zip(index,links):
		print("Serial no: ",index+2)
		extension = url.split('.')[-1]

		try:
			if extension in ('htm', 'html'):
				print('html')
				text = BS4(url)
				# text2 = embedded_pdf_text_by_replace_title(url)
				# text = text1 + text2
			elif extension == 'pdf':
				print('pdf')
				text = pdf2text(url)
			elif extension in ('doc', 'docx'):
				print('doc')
				text = docToText(url)
			elif extension == 'msg':
				print('msg')
				text = msgToText(url)
			elif 'docs.google.com' in url:
				print('google docs')
				text = download_doc(url)
			else:
				print('something else')
				text1 = embedded_pdf_text(url)
				text2 = BS4(url)
				text = text1 + text2

			text = removeNonAscii(text)
			text = text.rstrip()
			text = ". ".join(list(OrderedDict.fromkeys(text.split(". "))))
			summary_text = summary(text)
			data['Summarization'][index] = summary_text
			print('***URL: '+str(url))
			print("Summarization: ",summary_text)
			print('\n\n')
		except Exception as e:
			print(e)

		if index%100 == 0:
			print('Saving whatever is done till now!')
			data.to_csv(csv_file_path.replace('.csv','_out.csv'), index = False)


	# MAIN PROGRAM FOR AUDIO RESOURCE
	records = data[data.content_subformat.isin(['audio_resource'])]
	links = records.url
	for index, url in enumerate(links):
		print("Serial no: ",index+2)
		extension = url.split('.')[-1]
		try:
			if extension in ('mp3', 'wav'):
				text = audioToText(url)
			else:
				text = embedded_audio(url)
			summary_text = summary(text)
			data['Summarization'][index] = summary_text
			print('***URL: '+str(url))
			print("Summarization: ",summary_text)
			print('\n\n')
		except Exception as e:
			print(e)

	# MAIN PROGRAM FOR IMAGE RESOURCE
	records = data[data.content_subformat.isin(['image_resource'])]
	links = records.url
	for index, url in enumerate(links):
		print("Serial no: ",index+2)
		extension = url.split('.')[-1]
		try:
			if extension in ('pdf', 'jpeg', 'jpg', 'png', 'tiff', 'gif', 'tif'):
				text = pdf_imageToText(url)
			else:
				text = embedded_image_to_text(url)
			summary_text = summary(text)
			data['Summarization'][index] = summary_text
			print('***URL: '+str(url))
			print("Summarization: ",summary_text)
			print('\n\n')
		except Exception as e:
			print(e)

	# MAIN PROGRAM FOR VIDEO RESOURCE
	records = data[data.content_subformat.isin(['video_resource'])]
	links = records.url
	for index, url in enumerate(links):
		print("Serial no: ",index+2)
		extension = url.split('.')[-1]
		try:
			if extension == 'mp4' or 'youtube' in url:
				text = videoToText(url)
			else:
				text = embedded_video_to_text(url)
			summary_text = summary(text)
			data['Summarization'][index] = summary_text
			print('***URL: '+str(url))
			print("Summarization: ",summary_text)
			print('\n\n')
		except Exception as e:
			print(e)

	data = remove_error_summ(data)
	data.rename(columns={'id':'resource_id'},inplace=True)
	data.to_csv(csv_file_path.replace('.csv','_out.csv'), index = False)
	driver.close()
	
	print('\n\n\n:::Counts:::\n--------------')
	total = data.shape[0]
	broken = data.Summarization.isna().sum() + data[data.Summarization == ''].shape[0]
	print('Total Links: '+str(total)+'\n')
	print('Successful insertions (Approx): '+str(total-broken))
	print('Broken Links (Approx): '+str(broken))
