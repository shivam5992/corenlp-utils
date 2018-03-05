import json
import requests
import csv
import os
import json
import requests
import urllib
import urllib2

#bd for basic dependencery and pas
def general_urllibFetch_local(method_url, method, data):
   url = "http://localhost:8080/sae-1.0.0-BUILD-SNAPSHOT/analytics/"+method_url
   headers = {"Content-type": "application/json","Accept": "application/json"}
   json_d = json.dumps({"textCorpus":data})
   response = requests.post(url, data=json_d, headers=headers)
   return response.json()


def generalFetch_appengine(method_url, method, key, data):
    url = 'http://localhost:8080/sae-1.0.0-BUILD-SNAPSHOT/analytics/'+method_url
    json_data = json.dumps({key: data})
    headers = {"Content-type": "application/json","Accept": "application/json"}
    fetch_method = urlfetch.POST
    if method.lower() == "get":
        fetch_method = urlfetch.GET
    response = urlfetch.fetch(url, method=urlfetch.POST, headers=headers, payload=json_data)
    print response.content
    if response.status_code == 200:
        return response.content
    else:
        return "Bad response: "+str(response.status_code)

###Return final sentiment and bd tree
def getData1(text, delimiter="-", bd=True, sentiment=False):
    url = 'http://localhost:8080/sae-1.0.0-BUILD-SNAPSHOT/analytics/analyse'
    json_data = {"sentence":text, "bd":bd, "sentiment":sentiment, "delimiter":delimiter}
    headers = {"Content-type": "application/json","Accept": "application/json"}
    jsonData = json.dumps(json_data)
    response = requests.post(url, headers=headers, data=jsonData)
    return response.json()

#method_url= analyse:
#print general_urllibFetch_local("parse","post","I love my India")

def  requestToAppEngine():
    url = "http://localhost:9999/analyze_sentiment" #didnot work
    text = "RT @FootyHumour: So Suarez is signing for Barcelona - A player banned from football, signing for a club banned from transfer activity. http\xe2\x80\xa6"
    text=text.decode('utf-8').encode('ascii', 'ignore')
    post_data_dictionary = {"textcorpus":text}
    http_headers = {"Accept": "application/json"}
    post_data_encoded = urllib.urlencode(post_data_dictionary)
    request_object = urllib2.Request(url, post_data_encoded,http_headers)
    response = urllib2.urlopen(request_object)
    json_dict = response.read()
    print json_dict

def print_tree(tree):
	for subTree_dic in tree:
		orig_node = subTree_dic['node']
		inside_trees = subTree_dic['subTree']
		if len(inside_trees) == 0:
			print orig_node
		else:
			print orig_node
			print_tree(inside_trees)

print requestToAppEngine()
tree =  getData1("Dutch contractor Allseas have signed a contract with Norway's Statoil for the installation of three platforms on the giantJohan Sverdrup development")
print_tree(tree)