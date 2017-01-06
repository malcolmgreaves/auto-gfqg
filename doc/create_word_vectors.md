# Command for Re-creating Word Vectors

The word vectors used in this project come from the popular [`word2vec`](https://github.com/dav/word2vec) tool. The steps necessary to re-create these vectors is in the [modified demo-word script from the word2vec repository](../src/main/script/modified_demo-word.sh). Make sure that `$DEV` is properly set before executing this script.

After creating the word vectors, they'll be in `$VECTOR_DATA`. The first line has the number of vectors and their dimensionality; space-separated. The following lines are the word vector data; formatted as `<word> <vector>`.
