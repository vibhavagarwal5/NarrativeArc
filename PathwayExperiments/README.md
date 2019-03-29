# NarrativeArc
The goal of this project is to create a coherent narrative arc to acquire given competency.

## About
The code generates a new collection using pair-wise score learnt from given collections. Currently, the scoring function uses GloVe embeddings weighted by TFIDF values.

## Software installation
Use "pip3 -r requirements.txt" to install the required packages for python3

## Usage
* Download and place glove.6B.50d.txt (https://nlp.stanford.edu/projects/glove/ )file in data directory.
* Use "python generate_collection.py" from src directory to create a new collection