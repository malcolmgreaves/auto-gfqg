#!/bin/bash

echo "Thresholding scored sentences..."
for i in `seq 1 9`;
do
	date
	./target/pack/bin/select-sentences-for-questions 0.4$i output/author_biology-BTM_topic_modeling/k25-scored_sentences data/from_authors/biology.txt output/high_score-author_biology-BTM_k25-wv_text8/selected_sentences-author_biology_lemma_nostop-BTM_k25-score_0.4$i
done

echo "Selecting gap-words and distractors..."
for i in `seq 1 9`;
do
	date
	./target/pack/bin/vocab-filter-gap-word-and-distractor-selection output/author_biology-BTM_topic_modeling/combined_topic_word_vectors-k25_top20words output/high_score-author_biology-BTM_k25-wv_text8/selected_sentences-author_biology_lemma_nostop-BTM_k25-score_0.4$i data/word_vectors/text8-vector.bin output/author_biology-BTM_topic_modeling/output/voca.txt output/high_score-author_biology-BTM_k25-wv_text8/vocab_restrict/generated_questions_gaps_distractors-score_0.4$i
done

echo "Done"
date
echo ""
echo "view data/from_authors/biology.txt filtered by the global indicies in seletected sentences"
echo "This lets you see the actual, original sentences."
echo ""
echo "And then view the generated questions, selected gap-word, and selected distractors"
echo ""
echo example file is: output/high_score-author_biology-BTM_k25-wv_text8/selected_sentences-author_biology_lemma_nostop-BTM_k25-score_0.41"

