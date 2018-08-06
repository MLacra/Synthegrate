--df dictionary_0: word=/Users/lara/Documents/workspace-sts-3.9.3/Synthegrate/resources/dictionaries/apple-permutations.txt 
 --df dictionary_1: word=/Users/lara/Documents/workspace-sts-3.9.3/Synthegrate/resources/dictionaries/english3.txt 
 --df dictionary_2: word=/Users/lara/Documents/workspace-sts-3.9.3/Synthegrate/resources/dictionaries/italiano.txt 
 --df dictionary_3: word=/Users/lara/Documents/workspace-sts-3.9.3/Synthegrate/resources/dictionaries/keywords.txt 
 --df dictionary_4: word=/Users/lara/Documents/workspace-sts-3.9.3/Synthegrate/resources/dictionaries/names2017.txt 
 --df dictionary_5: word=/Users/lara/Documents/workspace-sts-3.9.3/Synthegrate/resources/dictionaries/password-permutations.txt 
 --df dictionary_6: word=/Users/lara/Documents/workspace-sts-3.9.3/Synthegrate/resources/dictionaries/passwords1.txt 
 --df dictionary_7: word=/Users/lara/Documents/workspace-sts-3.9.3/Synthegrate/resources/dictionaries/passwords2.txt 
 --df dictionary_8: word=/Users/lara/Documents/workspace-sts-3.9.3/Synthegrate/resources/dictionaries/passwords3.txt 
 --df dictionary_9: word=/Users/lara/Documents/workspace-sts-3.9.3/Synthegrate/resources/dictionaries/passwords4.txt 
 --df dictionary_10: word=/Users/lara/Documents/workspace-sts-3.9.3/Synthegrate/resources/dictionaries/passwords5.txt 
 --df dictionary_11: word=/Users/lara/Documents/workspace-sts-3.9.3/Synthegrate/resources/dictionaries/passwords6.txt 
 --df dictionary_12: word=/Users/lara/Documents/workspace-sts-3.9.3/Synthegrate/resources/dictionaries/passwords7.txt 
 --df dictionary_13: word=/Users/lara/Documents/workspace-sts-3.9.3/Synthegrate/resources/dictionaries/primes-to-100k.txt 
 --df dictionary_14: word=/Users/lara/Documents/workspace-sts-3.9.3/Synthegrate/resources/dictionaries/primes-to-200k.txt 
 --df dictionary_15: word=/Users/lara/Documents/workspace-sts-3.9.3/Synthegrate/resources/dictionaries/words_alpha_370099.txt 
 CREATE TABLE schema_0.relation_0 ( --df: size=57 
attribute_0 text UNIQUE NOT NULL,--df: use=dictionary_7 offset=0 step=1 shift=0 size=47603
attribute_1 text UNIQUE NOT NULL,--df: use=dictionary_8 offset=0 step=1 shift=0 size=9999
attribute_2 text UNIQUE NOT NULL,--df: use=dictionary_10 offset=0 step=1 shift=0 size=3630
attribute_3 text UNIQUE NOT NULL,--df: use=dictionary_6 offset=0 step=1 shift=0 size=9604
attribute_4 text UNIQUE NOT NULL--df: use=dictionary_15 offset=0 step=1 shift=0 size=370099

);

