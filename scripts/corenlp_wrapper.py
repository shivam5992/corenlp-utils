wrds = [u'Guangdong', u'University', u'of', u'Foreign', u'Studies', u'is', u'located', u'in', u'Guangzhou', u'.']
wrds = [(u'Guangdong', u'NNP'), (u'University', u'NNP'), (u'of', u'IN'), (u'Foreign', u'NNP'), (u'Studies', u'NNPS'), (u'is', u'VBZ'), (u'located', u'JJ'), (u'in', u'IN'), (u'Guangzhou', u'NNP'), (u'.', u'.')]
relns = [(u'ROOT', 0, 7), (u'compound', 2, 1), (u'nsubjpass', 7, 2), (u'case', 5, 3), (u'compound', 5, 4), (u'nmod', 2, 5), (u'auxpass', 7, 6), (u'case', 9, 8), (u'nmod', 7, 9), (u'punct', 7, 10)]

mapp = {}
for i, wrd in enumerate(wrds):
	mapp[str(i)] = {'wrd' : wrd[0] , 'pos' : wrd[1]}

basicdependencies = [{
          "dep": "ROOT",
          "governor": "0",
          "governorGloss": "ROOT",
          "dependent": "7",
          "dependentGloss": "located"
        },
        {
          "dep": "compound",
          "governor": "2",
          "governorGloss": "University",
          "dependent": "1",
          "dependentGloss": "Guangdong"
        },
        {
          "dep": "nsubj",
          "governor": "7",
          "governorGloss": "located",
          "dependent": "2",
          "dependentGloss": "University"
        },
        {
          "dep": "case",
          "governor": "5",
          "governorGloss": "Studies",
          "dependent": "3",
          "dependentGloss": "of"
        },
        {
          "dep": "compound",
          "governor": "5",
          "governorGloss": "Studies",
          "dependent": "4",
          "dependentGloss": "Foreign"
        },
        {
          "dep": "nmod",
          "governor": "2",
          "governorGloss": "University",
          "dependent": "5",
          "dependentGloss": "Studies"
        },
        {
          "dep": "cop",
          "governor": "7",
          "governorGloss": "located",
          "dependent": "6",
          "dependentGloss": "is"
        },
        {
          "dep": "case",
          "governor": "9",
          "governorGloss": "Guangzhou",
          "dependent": "8",
          "dependentGloss": "in"
        },
        {
          "dep": "nmod",
          "governor": "7",
          "governorGloss": "located",
          "dependent": "9",
          "dependentGloss": "Guangzhou"
        }
      ]

tree_map = {}
visited = []
for each in basicdependencies:
	parent_index = each['dependent']
	leaf_index = each['governor']
	parent = each['dependentGloss']
	leaf = each['governorGloss']
	reln = each['dep']

	node = "-> " + parent + " ("+reln.lower()+")"
	tree_map[node] = []

	for each in basicdependencies:
		if each['governor'] == parent_index:
			node1 = "  -> " + each['dependentGloss'] + " ("+each['dep'].lower()+")"
			visited.append(each['dependentGloss'])
			tree_map[node].append(node1)
	print 

for k, v in tree_map.iteritems():
	if v:
		print k, v 
# -> located-JJ (root)
#   -> University-NNP (nsubj)
#     -> Guangdong-NNP (nn)
#     -> of-IN (prep)
#       -> Studies-NNPS (pobj)
#         -> Foreign-NNP (nn)
#   -> is-VBZ (cop)
#   -> in-IN (prep)
#     -> Guangzhou-NNP (pobj)

# -> signed-VBN (root)
  # -> Allseas-NNP (nsubj)
exit(0)

# print mapp
mains = {}
for each in relns:
	try:
		root = mapp[str(each[1])]['wrd']
		root_pos = mapp[str(each[1])]['pos']
		leaf = mapp[str(each[2])]['wrd']
		leaf_pos = mapp[str(each[2])]['pos']
		reln = each[0]
		

		key= str(each[1])#+str(each[1])+str(each[2])
		rt = "-> "+ root + "-" + root_pos + " ("+reln+")"
		lf = "  -> "+ leaf + "-" + leaf_pos + " ("+reln+")"
		if key not in mains:
			mains[key] = []

		mains[key].append(lf)
	except:
		pass

for k,v in mains.iteritems():
	print mapp[k]['wrd'], v 