# Finding Candidate Sentences for Gap-Fill Question Creation

The first step in the question generation process is to find sentences within the corpus that can be transformed into a multiple-choice, fill-in-the-blank style question. To this end, the idea is to use the BTM topic model that we [trained earlier](doc/train_biterm_topic_model-btm.md) to find such sentences within the corpus.

First, we assign a score to every sentence in the corpus. For this step, we compute the top three most probable topics for each sentence, weight each posterior probabiltiy by 0.5, 0.3, and 0.2, respectively, and sum the resulting values. We use the [`ScoreSentences`](../src/main/scala/agfqg/ScoreSentences.scala) program to perform this computation.

Next, we filter out all low-scoring sentences. This step is arbitrary, as in one must manually specify a threshold value. In our experiments with the biology textbook corpus, we use a value of 0.4. The [`SelectSentencesForQuestions`](../src/main/scala/agfqg/SelectSentencesForQuestions.scala) program performs this step.

#### Note

Before using any of the following programs, ensure that:
* the sentence data is present and in the correct format.
* the BTM topic model has been trained on the relevant data.
* all of the code has been compiled and executable scripts have been created using `sbt pack`.