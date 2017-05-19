import re
from os import listdir
from os.path import isfile, join
from nltk.corpus import wordnet


path = "/home/rdorado/project/wiki-dm-project/data/wikipedia/articles/Docs"
def getWNWord(term):
  resp = []
  df = {}
  getWNWordPOS(term, 'n', df)
  getWNWordPOS(term, 'a', df)
  getWNWordPOS(term, 'v', df)

  for key, value in df.items():
    resp.append(key)
 
  return resp

  
def getWNWordPOS(term, pos, df):
  wnterms = wordnet.synsets(term, pos=pos)
  wn_nouns = wordnet.all_synsets(pos=pos)
  for wn_word in wn_nouns:
    for wnterm in wnterms:
     sim = wnterm.wup_similarity(wn_word)
     if sim != None and sim > 0.95:
       try:
         df[wn_word.lemma_names()[0]]+=1
       except KeyError:
         df[wn_word.lemma_names()[0]]=1



onlyfiles = [f for f in listdir(path) if isfile(join(path, f))]
with open(path+'/../../../wikitextwn095.dat', 'w') as outf:     
 terms = []
 strings = []
 i=0
 for filename in onlyfiles:
  if filename.endswith(".xml"):
    term=filename[:-4].lower()
    df = {}
    with open(path+"/"+filename) as f:
      lines = f.readlines()      
      for line in lines[4:-3]:
          line = re.sub(r'\W+', ' ', line)  
          wordlist = line.split()
          for word in wordlist:
             try:
                df[word] = df[word]+1
             except KeyError:
                df[word] = 1    

    print(term)
    

    string = ""
    #for wrd in wn_words:
    #  string+=wrd+","
    wn_words = getWNWord(term)
    for key, value in df.items():
      if key in wn_words:
        string+=" "+key

    terms.append(term)
    strings.append(string)
    i+=1
    outf.write(term+":"+string+"\n")
    #print(term+":"+string)      
#"  </text>";


     #sim = word.lch_similarity(syntargets[i])




