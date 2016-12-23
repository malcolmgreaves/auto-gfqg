
# Finding Gap Words and Distractors

In order to find gap words and appropriate distractors, we use word vectors and the conditional word-topic probabilites that BTM has learned. 

### Commands

First, we use the topic model's word probabilites to create a weighted word-vector sum of the most likely words for every topic. The resulting vectors become the *topic vectors*. We use the [`CreateTopicWordVecs`](../src/main/scala/agfqg/CreateTopicWordVecs.scala) for this step.

Next, we can use one of three programs to appropriate find gap words and distractors for each high-scoring sentence. Using the topic vectors, the BTM model, the word vectors, and the scored sentences, our options are to invoke one of:

1. [`GapWordAndDistractorSelection`](../src/main/scala/agfqg/GapWordAndDistractorSelection.scala)

2.[`VocabFilterGapWordAndDistractorSelection`](../src/main/scala/agfqg/VocabFilterGapWordAndDistractorSelection.scala)

3. [`NlpAwareGapWordAndDistractorSelection`](../src/main/scala/agfqg/NlpAwareGapWordAndDistractorSelection.scala)

All programs find gap words by choosing the word in each sentence whose word vector is closest to the weighted sum of the top 3 topic vectors for the sentence. Only the third program, `NlpAwareGapWordAndDistractorSelection`, restricts gap words to have a noun or verb part-of-speech tag.

Additionally, all programs find distractors by choosing words that are close to both the gap word vector as well as to the weighted sentence topic vector. However, the 2nd program, `VocabFilterGapWordAndDistractorSelection`, as well as the 3rd program, restricts distractors to words that occur in the corpus only. The 1st program allows any word from the larger corpus of learned word vectors.

Moreover, every distractor is filled-in to the candidate question sentence and run through a language model. This serves as a final re-ranking to select only the most likely and relevant distractors to go along with the chosen gap word.