# auto-gfqg

This **Automatic Gap-Fill Question Generation** system creates multiple choice, fill-in-the-blank questions from text corpora. Textbooks, factoid archives, news articles, reports, lecture notes, legal proceedings -- the minimum viable input is a small to moderate sized collection of coherent, well-formed english.

This work is a proof-of-concept reimplementation of the ideas behind [RevUp](http://oa.upm.es/42192/1/INVE_MEM_2015_226779.pdf). The ideas implemented here are largely the same as those in the paper. There are two notable differences. First, we the use a [biterm topic model](https://github.com/xiaohuiyan/BTM) instead of the [deep autoencoder topic model](https://www.prhlt.upv.es/workshops/iwes15/pdf/iwes15-kumar-d'haro.pdf). Second, we use topic-weighted word vectors to perform the gap-phrase selection. In contrast, RevUp uses a supervised model trained on human judegements via Mechanical Turk.  

The [conclusions and future work](doc/conclusions_future_work.md) file summarizes thoughts and findings of this proof-of-concept (poc).

Before attempting to run and programs here, please read through the documentation and ensure that your machine has the [necessary pre-reqs](doc/software_prereqs.md).

### Overview of Information Flow

This gap-fill question generation system consists of a series of different programs and data resources. It is hacked-togeher research code that, in its current form, is unsuitable for production work. It does, however, demonstrate a question generation system from end-to-end.

The following numbered list roughly describes the system's sequential operation:

1. Use NLP tools to pre-process text. Includes sentence splitting, tokenization, and word stemming over all corpus text. See [NLP process with CoreNLP](doc/nlp_process_with_corenlp.md) for more.

2. Use word2vec to create word vectors over a larger, different corpus of text. See [create word vectors](doc/create_word_vectors.md) for more.

3. Use biterm topic modelling (BTM) to discover latent topics that are expressed on a per-sentence basis within the corpus. See [train BTM](doc/train_biterm_topic_model-btm.md) for more.

4. Use the learned BTM word-topic conditional probabilites and intuitive heuristics to score all sentences from the corpus. Then, threshold and eliminate low-scoring sentences, creating gap-fill question candidates. See [score and generate gap fill question candidates](doc/score_and_generate_gap_fill_questions_candidates.md) for more.

5. For each candidate sentence, choose a gap word. Removing the gap word from the sentence creates the fill-in-the-blank question (i.e. the gap word is the correct answer). Additionally, discover appropriate distractors for the chosen gap word. Distractors are semantically related, but ultimately different from the gap phrase (i.e. these are the incorrect answers). See [finding gap words and distractors](doc/find_gaps_and_distractors.md) for more.

All of the Scala programs have built-in help support. Invoke them with "-h" or "--help" to see information about how to use each program.




