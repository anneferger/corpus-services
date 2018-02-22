# Introduction

The HZSK Corpus Services project bundles functionality used for maintenance, curation, conversion, and visualization of corpus data at the HZSK.  

# Gitlab artifacts

The latest compiled .jar can be found here: 
https://gitlab.rrz.uni-hamburg.de/hzsk/hzsk-corpus-services/-/jobs/artifacts/master/browse?job=compile_withmaven



Additional info on the Corpus services can be found here:
https://lab.multilingua.uni-hamburg.de/redmine/projects/redmine/wiki/How_to_use_the_Corpus_Validator

# functions

To see the currently available functions, check the Doxygen files.

You can get a rough idea by listing java classes, e.g. `ls src/main/java/de/uni_hamburg/corpora/validation/` for validator tools.

# Compilation

To use the validator for HZSK corpora, compile it using `mvn clean compile assembly:single`.
(See https://stackoverflow.com/questions/574594/how-can-i-create-an-executable-jar-with-dependencies-using-maven)

# ATTENTION: THIS WILL BE DEPRECATED SOON AND NEEDS TO BE ADAPTED

# Usage

## All validation checks

Use it on Linux or on Windows connecting to the server via ssh, because some Checks (FileCoverageChecker) don't work on windows yet.

Add the generated .jar to the Folder /Korpora/HZSK and execute something like:

```
java -jar hzsk-corpus-services-0.1-jar-with-dependencies.jar {corpusfoldername} > {corpusfoldername}/output_corpus-services.txt
```

{corpusfoldername} should be the name of the folder you want to validate. The class in the example runs different checks on the coma file in the specified corpus and on all the exbs and and other files.

Make sure to have only one coma file in your corpus folder.

Your output can be found in the corpus folder in output_corpus-services.txt

## Single corpus checks

There are other checks available too, check the doxygen documentation and change the command to the corresponding class.

```
java -cp hzsk-corpus-services-0.1-jar-with-dependencies.jar de.uni_hamburg.corpora.validation.FilenameChecker {corpusfoldername} 
```

Some checks may require exb or coma file paths instead of corpus directory. If you've some form of bash_completion installed you can get a neat list of check by just typing this:
```
java -cp hzsk-corpus-services-0.1-jar-with-dependencies.jar de.uni_hamburg.corpora.validation.
```
and hitting tab twice.