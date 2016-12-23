# auto-gfqg
Automatic Gap-Fill Question Generation.

This work is a proof-of-concept reimplementation of the ideas behind [RevUp](http://oa.upm.es/42192/1/INVE_MEM_2015_226779.pdf). It was done under limited time and finnancial constraints as an intellectual exercise. The ideas implemented here are largely the same as those in the RevUp paper. Of note are two differences. The first is the the use of the biterm topic model instead of the RBM described in RevUp. Second is the use of word and latent topic vectors to perform the gap-filling and distractor search instead of using supervised machine learning model.

The [conclusions and future work](doc/conclusions_future_work.md) file summarizes thoughts and findings for this challenge.

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




