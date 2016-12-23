# Comand for NLPing Text Data

In order to perform common natural language processing (NLP) techniques on our text data, we use the Stanford CoreNLP library and associated processors.

### Recreating the Data

This section walks one though how to use CoreNLP to re-process all of the text.

To obtain this code, download the most recent CoreNLP release. You may do so using the following command:

```
wget http://nlp.stanford.edu/software/stanford-corenlp-full-2016-10-31.zip
unzip stanford-corenlp-full-2016-10-31.zip
cd stanford-corenlp-full-2016-10-31
```

Execute the following from within the recently downloaded corenlp directory (e.g. `stanford-corenlp-full-2016-10-31`):

```
TEXT_FI=$DEV/auto-gfqg/data/from_authors/biology.txt
java -cp "*" edu.stanford.nlp.pipeline.StanfordCoreNLP -annotators tokenize,ssplit,pos,lemma,ner -file $TEXT_FI -outputFormat conll
```

This program outputs its results into a file in the local, current directory called `biology.txt.conll`. 

### NOTE
Make sure that `DEV` is where you checked out this repository. Also, we recommend running with the following JVM flags: `-Xmx20g -XX:+UseG1GC -XX:+TieredCompilation -server -d64 -XX:+AggressiveOpts`. Additionally, we recommend using `time` to see how long it takes for this program to run on your machine.

