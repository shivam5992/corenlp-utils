'''
Rule Based Sentiment Analysis
Python Script which calculates Sentiment Scores of a sentence, Sub Sentiments of Subject, Object and Verb triplet.

Input: Basic Dependency Trees of sentences, BD Trees are created using Stanford Core NLP Parser.
Output: TUPLE: (Sentence, Sentiment, Subject, Subject-POS, Verb, Object, Object-POS, Sub-Sentiment)
'''

import FullNNP as fullname
import ast

''' Create Positive and Negative Bag Of Words '''
positives = open("data/positives.txt").read().split("\n")
negatives = open("data/negatives.txt").read().split("\n")

''' Utility function to get the word from the root node '''
def filter_node(word):
	word = word.strip().replace("-> ","")
	word = str(word.rsplit("-",1)[0])
	return word


''' Utility function to get the relation of word with its parent '''
def get_reln(word):
	word = word.strip()
	i = word.find("(")
	f = word.find(")")
	pos = str(word[i+1:f])
	return pos

''' Utility function to get the POS of the word'''
def get_pos(word):
	word1 = word.strip().replace("-> ","")
	word1 = str(word1.rsplit("-",1)[-1])
	word1 = word1.split(" ")[0]
	return word1

''' Function to calculate the sentiment of a word based on occurance in positive and negative list '''
def word_sentiment(word):
	word = word.lower()
	if word in positives:
		return 2
	elif word in negatives:
		return -2
	else:
		return 0

''' Function to check if root word is obj or not, Used to decide weather to calculate root sentiment or not '''
def check_for_obj(reln):
	possible_objs = ["pobj","iobj","dobj","obj"]
	if reln in possible_objs:
		return True
	else:
		return False

''' Get all scores and convert them into relative scores '''
def standardised(scores):
	new_scores = []
	mx = max(scores)
	mn = min(scores)
	for score in scores:
		scoreMax = 1
		scoreMin = 0
		K = (score - mn)/(mx - mn)
		srange = scoreMax - scoreMin
		C = 4            
		K = float(score - mn)/(mx - mn)
		size = scoreMin + (C*float(K*srange/C))
		new_scores.append(size)
	return new_scores


''' Decide if the child node is verb type or not'''
def checkvb(reln, pos):
	possible_pos = ["VB","VBD","VBG","VBN","VBP","VBZ"]
	possible_relns = ["dep"]

	if pos in possible_pos:
		return True
	elif reln in possible_relns:
		return True
	else:
		return False

		
''' Function to print and traverse the tree'''
def print_tree(tree):
	for subTree_dic in tree:
		orig_node = subTree_dic['node']
		inside_trees = subTree_dic['subTree']
		if len(inside_trees) == 0:
			print orig_node
		else:
			print orig_node
			print_tree(inside_trees)

''' Base function for parsing and printing '''
def tree_parse(tree):
	overall_sentiment = 0
	for subTree_dic in tree:
		orig_node = subTree_dic['node']
		node = filter_node(orig_node)
		inside_trees = subTree_dic['subTree'] 
		if len(inside_trees) == 0:
			print orig_node
			sentiment = word_sentiment(node)
		else:
			print orig_node
			sentiment = word_sentiment(node) + tree_parse(inside_trees)
		overall_sentiment += sentiment
	return overall_sentiment

''' Decide the factor depending upon the child parent relationship'''
def get_factor(cr):
	factor_1o5 = ["discourse","vmod","advmod"]
	factor_2 = ["quantmod", "npadvmod"]	
	
	factor = 1
	if cr in factor_1o5:
		factor = 1.5
	elif cr in factor_2:
		factor = 2

	elif cr == "neg":
		factor = -1
	return factor

''' Convert the calculated sentiment into standard values '''
def convert_sentiment(senti):
	if senti == 0:
		senti = 0
	elif senti > 0 and senti <= 2:
		senti = 1
	elif senti > 2:
		senti = 2
	elif senti < 0 and senti >= -2:
		senti = -1
	elif senti < -2:
		senti = -2
	return senti

''' Function to extract subject and object from a tree '''

def extract_subj_obj(tree):
	s1 = []
	o1 = []
	spos = []
	opos = []
	
	tree = tree[0]
	node = tree['node']
	node_list = tree['subTree']

	node = filter_node(node)
	node_reln = get_reln(tree['node'])
	pos = get_pos(tree['node'])

	strn = node + "-" + pos
	lis1 = fullname.fullnnp(tree,strn)
	
	if lis1 and lis1[0] != "":
		node = lis1[0]
		pos = lis1[1]
	
	
	s2 = []
	o2 = []
	sp = []
	op = []
	if node_reln in possible_objs:
		o1.extend(node)
		opos.extend(pos)
	if node_reln in possible_subjs:
		s1.extend(node)
		spos.extend(pos)

	for sub_node in node_list:
		leaf = sub_node['node']
		leaf = filter_node(leaf)
		leaf_reln = get_reln(leaf)
		leaf_tree = sub_node['subTree']
		leaf_pos = get_pos(leaf)
		strn = leaf + "-" + leaf_pos
		lis1 = fullname.fullnnp(tree,strn)
		# lis = lis1[0]
		# pos1 = lis1[1]
		if lis1 and lis1[0] != "":
			leaf = lis1[0]
			leaf_pos = lis1[1]
		if leaf_reln in possible_subjs:
			s1.extend(leaf)
			spos.extend(leaf_pos)
		if leaf_reln in possible_objs:
			o1.extend(leaf)
			opos.extend(leaf_pos)

		if leaf_tree:
			s2, sp, o2, op = extract_subj_obj(leaf_tree)
	
	subj1 = s1 + s2
	obj1 = o1 + o2
	sspos = spos + sp
	oopos = opos + op
	return subj1, sspos, obj1, oopos

''' Function to get sentiment tuple for a tree '''
def get_root_sentiment(tree):
	subj = []
	obj = []
	score = 0
	tot_fact = 1
	tree_dic = tree[0]
	subjpos1 = []
	objpos1 = []
	root = tree_dic['node']
	root = filter_node(root)
	root_reln = get_reln(tree_dic['node'])
	ps = word_sentiment(root)

	tree_list = tree_dic['subTree']

	for item in tree_list:

		child = filter_node(item['node'])
		child_tree = item['subTree']

		reln = get_reln(item['node'])
		pos = get_pos(item['node'])
		fact = get_factor(reln)
		sent = word_sentiment(child)

		strn = child + "-" + pos
		lis1 = fullname.fullnnp(tree,strn)
		lis = lis1[0]
		pos1 = lis1[1]
		if lis1 and lis1[0] != "":
			child = lis1[0]
			pos = lis1[1]
		if reln in possible_subjs:
			subj.extend(child)
			subjpos1.extend(pos)
			sent = 0
			fact = 1
		elif reln in possible_objs:
			obj.extend(child)
			objpos1.extend(pos)
			sent = 0
			fact = 1
		if len(child_tree) != 0:

			newdic = []
			newdic.append(item)
			final, pt, nt, ft = tree_sentiment(newdic)
			sent = final
			if child != root_node and child not in visited_verbs:
				subj1, s1, obj1, o1 = extract_subj_obj(child_tree)
				subj.extend(subj1)
				obj.extend(obj1)
				subjpos1.extend(s1)
				objpos1.extend(o1)
		tot_fact *= fact
		score += sent

	tot_sent = ps + score
	tot_sent *= tot_fact
	tple = (subj, subjpos1, root, obj, objpos1, tot_sent)
	return tple

''' For original tree get sub sentiments tuples list '''
def get_sub_sentiments(tree):
	tree_dic = tree[0]
	root = tree_dic['node']
	leafs = tree_dic['subTree']
	for leaf in leafs:
		leaf_node = leaf['node']
		filter_leaf_node = filter_node(leaf_node)
		leaf_tree = leaf['subTree']
		if len(leaf_tree) != 0:
			newdic = []
			newdic.append(leaf)
			get_sub_sentiments(newdic)
			leaf_reln = get_reln(leaf_node)
			leaf_pos = get_pos(leaf_node)
			if checkvb(leaf_reln, leaf_pos):
				visited_verbs.append(filter_leaf_node)
				tple = get_root_sentiment(newdic)
				sub_sentiments.append(tple)

''' Analysing and calculating the complete sentiment for a tree '''
def tree_sentiment(tree):
	''' Iterate the multiple root nodes '''
	poscount = 0
	negcount = 0
	final = 0
	factor_count = 0
	for subTree_dic in tree:
		subtreefi = 0

		''' Create Root Node Tuple '''
		orig_node = subTree_dic['node']
		root_node = filter_node(orig_node)
		root_reln = get_reln(orig_node)
		root_pos = get_pos(orig_node)
		parent_tuple = (root_node, root_pos, root_reln)
		inside_trees = subTree_dic['subTree']

		''' Calculate sentiment for root node '''
		if root_reln in possible_subjs or root_reln in possible_objs:
			ps = 0
			ignore = root_node
		else:
			ps = word_sentiment(root_node)
			if ps == 2:
				poscount += 1
			elif ps == -2:
				negcount += 1
			ignore = ""

		if root_node != ignore:
			''' Go Further down in its leafs '''
			leaf_xis = 0
			total_factor = 1
			for leafs in inside_trees:
				orig_node = leafs['node']
				child_node = filter_node(orig_node)
				child_reln = get_reln(orig_node)
				child_pos = get_pos(orig_node)
				
				''' Check if leaf is a tree or just a terminating point '''
				leaf_inside_trees = leafs['subTree']
			
				if len(leaf_inside_trees) == 0:	
					score = word_sentiment(child_node)
					if score == 2:
						poscount += 1
					elif score == -2:
						negcount += 1
					factor = get_factor(child_reln)
					if factor != 1:
						factor_count += 1
				else:
					dicleaf = []
					dicleaf.append(leafs)
					score, p, n, f  = tree_sentiment(dicleaf)
					factor = get_factor(child_reln)
					if factor != 1:
						factor_count += 1
					if f != 1:
						factor_count += f
					poscount += p
					negcount += n
				total_factor *= factor
				leaf_xis += score
		else:
			leaf_xis = 0
			total_factor = 1	
		subtreefi = ps + leaf_xis
		subtreefi *= total_factor
	final += subtreefi
	return final, poscount, negcount, factor_count



''' Run the sentiment analyser '''					
if __name__ == '__main__':
	finput = open("500_articles.csv").read().split("\n")
	
	fout = open("Output45K.txt","w")
	columns = "SentenceId|ArticleId|Sentence|SUBJ|SUBJ-POS|VERB|OBJ|OBJ-POS|SENTIMENT\n"
	fout.write(columns)
	ferror = open("FInal_error.txt","w")

	possible_verbs = ["VB","VBD","VBP","VBG","VBN","VBZ"]
	possible_objs = ["pobj","iobj","dobj","obj"]
	possible_subjs = ["nsubj","nsubjpass","csubjpass","subj","csubj","xsubj"]
	
	for i,line in enumerate(finput):
		if i <= 15:
		#	try:
				line_full = line.split("|")
				print i+1
				''' News parameters '''
				sentence_id = line_full[0]
				article_id = line_full[1]
			
				''' End News Parameters '''

				#''' Tweets Parameters '''
				sentence = line_full[2].decode('unicode_escape').encode('ascii','ignore')
				tree = line_full[3].decode('unicode_escape').encode('ascii','ignore')
				tree = ast.literal_eval(tree)
				
				''' End Tweet Section '''
				
				sub_sentiments = []
				visited_verbs = []
				
				tree_orig = tree[0]
				temp = str(tree[0][0])
				root_node = tree_orig[0]['node']
				root_node = filter_node(root_node)
				sentim, pc, nc, fc = tree_sentiment(tree_orig)
				freq = pc + nc + fc
				if freq != 0:
					sentim = sentim/freq
				sentim = convert_sentiment(sentim)
				sentim = str(sentim)

				root_tple = get_root_sentiment(tree_orig)
				sub_sentiments.append(root_tple)
				get_sub_sentiments(tree_orig)
				for j in range(len(sub_sentiments)):
					verb = str(sub_sentiments[j][2])
					senti = float(sub_sentiments[j][5])
					senti = str(convert_sentiment(senti))
					
					if sub_sentiments[j][3]:
						subcopy = ast.literal_eval(str(sub_sentiments[j][3]))
						ies = []
						for kkl,x in enumerate(subcopy):
							if x == "" or x == "=":
								ies.append(kkl)
					 	for iss, ess in reversed(list(enumerate(ies))):
					 		del sub_sentiments[j][3][ess]
					 		del sub_sentiments[j][4][ess]

						ob = str(",".join(sub_sentiments[j][3])).strip(",").strip("$")
						ob = ob.replace(",,",",")
					else:
						ob = ""
					
					if sub_sentiments[j][4]:
						for kkl,x in enumerate(sub_sentiments[j][4]):
							if x in possible_verbs:
								del sub_sentiments[j][4][kkl]
								del sub_sentiments[j][3][kkl]
						o1pos = str(",".join(sub_sentiments[j][4])).strip("$").strip(",")
						o1pos = o1pos.replace(",$,",",")
					else:
						o1pos = ""

					if sub_sentiments[j][0]:
						subcopy = ast.literal_eval(str(sub_sentiments[j][0]))
						ies = []
						for kkl,x in enumerate(subcopy):
							if x == "":
								ies.append(kkl)
					 	for iss, ess in reversed(list(enumerate(ies))):
					 		del sub_sentiments[j][0][ess]
					 		del sub_sentiments[j][1][ess]
						sb = str(",".join(sub_sentiments[j][0])).strip(",").strip("$")
						sb = sb.replace(",,",",")
					else:
						sb = ""

					if sub_sentiments[j][1]:
						for kkl,x in enumerate(sub_sentiments[j][1]):
							if x in possible_verbs:
								del sub_sentiments[j][0][kkl]
								del sub_sentiments[j][1][kkl]
						s1pos = str(",".join(sub_sentiments[j][1])).strip("$").strip(",")
						s1pos = s1pos.replace(",$,",",")
					else:
						s1pos = ""
					

					for kk, oob in enumerate(sub_sentiments[j][3]):
						for kkj, ssub in enumerate(sub_sentiments[j][0]):
							#result_first = sentence_id + "|" + article_id + "|" + sentence + "|" + ssub + "|" + sub_sentiments[j][1][kkj] + "|" + verb + "|" + oob + "|" + sub_sentiments[j][4][kk] + "|" + senti + "\n"
			 				result_first = sentence_id + "|" + article_id + "|" + ssub + "|" + sub_sentiments[j][1][kkj] + "|" + verb + "|" + oob + "|" + sub_sentiments[j][4][kk] + "|" + senti + "\n"
			 				fout.write(result_first)
		
			# except:
			# 	ferror.write(line + "\n")
			# 	continue