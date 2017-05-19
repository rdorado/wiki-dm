from nltk.corpus import wordnet
from sortedcontainers import SortedDict
#import pickle

targets =["animal","horse","dog","cat","religion","god","jehovah","computer","cpu","random-access_memory","operating_system","planet","country","canada","japan","colombia","music","sport","baseball","soccer"]
d = [SortedDict() for x in range(len(targets))]
k = 200

# Examples of similarities
#print "Path: ",net1[0].path_similarity(net2[0])
#print "LCH: ",net1[0].lch_similarity(net2[0])
#print "WUP: ",net1[0].wup_similarity(net2[0])
#print "RES: ",net1[0].res_similarity(net2[0])
#print "JCN: ",net1[0].jcn_similarity(net2[0])
#print "Lin: ",net1[0].lin_similarity(net2[0])

# Load the list of synsets given in the vector of targets. Warning! It crashes if no synset can be found!
syntargets = []
for target in targets:
  print "Finding synset",target,"in WordNet"
  syntargets.append( wordnet.synsets(target, pos='n')[0] )

# Accumulates the k most important synsets
result = []
for word in wordnet.all_synsets(pos='n'):
  for i in range(len(syntargets)):
     sim = word.wup_similarity(syntargets[i])
     #sim = word.lch_similarity(syntargets[i])
     if sim == None: continue
     d[i][-sim] = word
     if len(d[i]) > k: d[i].popitem()

# Write the output
with open('train.dat', 'w') as f:
  for i in range(len(syntargets)):
    f.write(targets[i]+":")   
    for item in d[i].items():
      f.write(" "+item[1].lemmas()[0].name()+","+str(-item[0]))  
    f.write("\n")


