# Command for Re-creating Word Vectors

The word vectors used in this project come from the popular [`word2vec`](https://github.com/dav/word2vec) tool. The steps necessary to re-create these vectors is:
```
# DEV is the directory where all of your projects are
cd $DEV

# clone and move into project
git clone git@github.com:dav/word2vec.git
cd word2vec

# pasted modified "script/demo-word.sh" script that
# doesn't do the word distance demonstration and
# saves the word vectors in text format

DATA_DIR=./data
BIN_DIR=./bin
SRC_DIR=./src

TEXT_DATA=$DATA_DIR/text8
ZIPPED_TEXT_DATA="${TEXT_DATA}.zip"
VECTOR_DATA=$DATA_DIR/text8-vector.bin

# build all programs
pushd ${SRC_DIR} && make; popd

if [ ! -e $VECTOR_DATA ]; then

  if [ ! -e $TEXT_DATA ]; then
    if [ ! -e $ZIPPED_TEXT_DATA ]; then
            wget http://mattmahoney.net/dc/text8.zip -O $ZIPPED_TEXT_DATA
        fi
    unzip $ZIPPED_TEXT_DATA
        mv text8 $TEXT_DATA
  fi
  echo -----------------------------------------------------------------------------------------------------
  echo -- Training vectors...
  time $BIN_DIR/word2vec -train $TEXT_DATA -output $VECTOR_DATA -cbow 0 -size 200 -window 5 -negative 0 -hs 1 -sample 1e-3 -threads 12 -binary 0 # the key is `binary 0` means "save as text"

fi
```

After creating the word vectors, they'll be in `$VECTOR_DATA`. The first line has the number of vectors and their dimensionality; space-separated. The following lines are the word vector data; formatted as `<word> <vector>`.
