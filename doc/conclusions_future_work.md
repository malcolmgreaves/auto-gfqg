# Conclusions, Results, and Future Work

Ultimately, this project is a successful demonstration of an end-to-end gap-fill question generation system. Interestingly, it does not directly rely upon supervised machine learning. Rather, it uses many different unsupervised learning techniques and some well thought out, intuitive herusitics. Advantageously, at a bare minimum, the system only relies upon a fact-filled corpus within the desired domain. As our experiments show, however, it is possible to include more knowledge in the system via additional corpuses.

The system used in the experiments had information from a vareity of sources. For example, the word vectors used in the experiments were built from a corpus that was completely different from the one used in question generation. While we did observe a qualatative improvement in the selected gap-words and distractors when we eliminated out-of-vocabulary word vectors, our overall impression is that this corpus difference does not greatly impact the system's effectiveness. Further experiments would be able to drill-down into this potential line of fruitful transfer learning in the system.

### Results

For experimentation, we used the text of Campbel's Biology, 9th edition. Every sentence of this book is located in the [biology.txt](../data/from_authors/biology.txt) file.

The learned BTM information is [here](../output/author_biology-BTM_topic_modeling/output). This directory contains the unique words in the corpus as well as all of the topic model parameters (located in the subdirectory `model/`).

The selected sentences we generated in our experiments are [here](../output/author_biology-BTM_topic_modeling/selected_sentences-k25_topwords20_threshold0.4).

Our experimental results, i.e. generated questions from this biology corpus, are here:
* [Distractors can be any word from the larger word2vec corpus.](../output/author_biology-BTM_topic_modeling/baseline-no_nlp-no_vocab_filter/ORIGINAL-complete_questions-gap_and_distractors)
* [Distractors must be words from the original biology corpus.](../output/author_biology-BTM_topic_modeling/vocab_filter/VOCAB_FILTERED-complete_questions-gap_and_distractors)
* [Distractors must also have their part-of-speech tags align with the chosen gap word, which must be a noun or verb.](../output/author_biology-BTM_topic_modeling/nlp_and_vocab_filter/NLP-complete_questions-gap_and_distractors)


### Future Work

We would like to extend this work in a few key areas. In no particular order:

* **Implementing DATM** We could experiment with the Deep Autoencoder Topic Model (DATM) described in the RevUp paper. Importantly, it would be interesting to see what, if any, differences in question generation there is between BTM and DATM.

* **Supervised Gap Filling** We could use the Microsoft Research [mind the gap](../data/msr-mind_the_gap_question_gen_corpus) and the [QGSTEC2010](../data/QGSTEC2010) data resources to train supervised gap-selection models. This data mirrors the procedure used by the RevUp authors. Additionally, we could use this data as a basis of more rigorous, quantative evaluation of our topic-weighted word vector method for gap-word selection.

* **Simultaneous, Coupled Learning** Instead of treating sentence scoring, gap-phrase selection, and distractor generation as separate, independent tasks, we could combine them into a single, learnable multi-part task. Combining supervised signals for which words make good gaps in particular, specific sentences would probably achieve a better overall question generation performance. Likewise, if we learn what makes a good gap phrase at the same time as we learn what makes good distractors, we would likely observe higher quality questions.

* **More Relevant Distractors** Currently, our system does a less-than-optimal job at ensuring the generated distractors are relevant and fit-well with the chosen gap-word and selected sentence. A low hanging fruit would be to ensure that the matching distractor and gap-word morphology. In an expanded sense, our qualatiative observations are that the distractors are often not relevant to the sentence's context. Ensuring that the distractors make sense is vital for overall question quality.

* **Corpus-Specific Word Vectors** We could re-train word vectors on the biology corpus data only, instead of using the pre-trained word vectors from the opensource word2vec project. We might find that these word vectors are more appropriate for our unspervised technique for gap-word selection.

* **NLP Tool Improvement** We could be more dilligent and thorough in our NLP pipeline. Specifically, we could use constituency parsing and named entity recognition (NER) in order to expand our system's limitation of _gap word_ finding to _gap phrase_ finding. Additionally, this would mean that we'd be able to find _distractor phrases_ too. This ability would likely allow us to create more appropriate question, answer, and distractor triples from text.

* **Syntax in Sentence Scoring and Selection** The core idea is to further experiment with different topic word-vector creation and weighting schemes in sentence scoring.  Currently, we perform a simple summation of all associated word vectors in a given sentence where each vector is weighted by conditional word distributions of the sentence's top three latent topics. We do not even, for example, weight these probabilities by the original topic weights that we use for sentence scoring. On the more sophisticated end of the spectrum, we could use the graphs produced by dependency parsing to re-shape and re-prioritize how we perform topic weighting via word vectors. For instance, if a word or phrase is the root of the sentence, we could choose to give its associated word vector more weight. We could additionally add-in a weight decay that would be inversely proprotional to the length of the word's path to the root. 
