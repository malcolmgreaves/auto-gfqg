# Conclusions, Results, and Future Work

Ultimately, this work successfully demonstrates a satisfactory end-to-end gap-fill question generation system. Moreover, it was created on a limited budget of both time and money. Interestingly, it does not directly rely upon supervised machine learning. Rather, it uses many different unsupervised learning techniques and some well thought out, intuitive herusitics.

For experimentation, we used the text of Campbel's Biology, 9th edition. Every sentence of this book is located in the [biology.txt](../data/from_authors/biology.txt) file.

The learned BTM information is [here](../output/author_biology-BTM_topic_modeling/output). This directory contains the unique words in the corpus as well as all of the topic model parameters (located in the subdirectory `model/`).

The selected sentences we generated in our experiments are [here](../output/author_biology-BTM_topic_modeling/selected_sentences-k25_topwords20_threshold0.4).

Our experimental results, i.e. generated questions from this biology corpus, are here:
* [Distractors can be any word from the larger word2vec corpus.](../output/author_biology-BTM_topic_modeling/baseline-no_nlp-no_vocab_filter/ORIGINAL-complete_questions-gap_and_distractors)
* [Distractors must be words from the original biology corpus.](../output/author_biology-BTM_topic_modeling/vocab_filter/VOCAB_FILTERED-complete_questions-gap_and_distractors)
* [Distractors must also have their part-of-speech tags align with the chosen gap word, which must be a noun or verb.](../output/author_biology-BTM_topic_modeling/nlp_and_vocab_filter/NLP-complete_questions-gap_and_distractors)

In the future, we would like to extend this work in a few key areas. In no particular order:

* We could experiment with the Deep Autoencoder Topic Model (DATM) described in the RevUp paper. Importantly, it would be interesting to see what, if any, differences in question generation there is between BTM and DATM.

* We could use the Microsoft Research [mind the gap](../data/msr-mind_the_gap_question_gen_corpus) and the [QGSTEC2010](../data/QGSTEC2010) data resources to train supervised gap-selection models. Additionally, we could use this data as a basis of more rigorous, quantative evaluation of our question generation system.

* We could re-train word vectors on the biology corpus data only, instead of using the pre-trained word vectors from the opensource word2vec project. We might find that these word vectors are more appropriate for our unspervised technique for gap-word selection.

* We could be more dilligent and thorough in our NLP pipeline. Specifically, we could use constituency parsing and named entity recognition (NER) in order to expand our system's limitation of _gap word_ finding to _gap phrase_ finding. Additionally, this would mean that we'd be able to find _distractor phrases_ too. This ability would likely allow us to create more appropriate question, answer, and distractor triples from text.
