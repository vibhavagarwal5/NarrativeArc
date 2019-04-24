# Text Summarization
The code summarizes the URL contents of all the learning resources in the dataframe.

- Install the script dependencies using
```
pip install -r requirements.txt
```

- We are using **headless chromedriver** for automated scraping. You can download the chromedriver from http://chromedriver.chromium.org/downloads.
	- Extract the zip file to get a **chromedriver** file.
	- Wherever you have extracted the chromdriver, enter that path into the script under the variable `chrome_path`.

- Run the summarization script using 
```
python Summarization.py <path to the file>
```
eg:
```
python Summarization.py ../WSL/data.csv
```


## About the Script

- The script involves an initial task of getting all the learning resources from the postgres server by signing into the AWS server and doing `scp` of the database from the server to the local machine. The data should compulsory have fields `URL` and `content_subformat` for the script to work.
- The script can also show the number of broken (not summarizable) URLs where the summarization would be `NAN`. 
- The script outputs the data in the format **[Initial name]_out.csv**.

### Note:
Not all the summrizations would be absolutely perfect. There might be some junk summarizations also.
