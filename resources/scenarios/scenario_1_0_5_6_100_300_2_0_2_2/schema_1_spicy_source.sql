CREATE SCHEMA IF NOT EXISTS public; 
DROP TABLE IF EXISTS public.relation_1 CASCADE ;
CREATE TABLE public.relation_1 (
attribute_5 text, 
attribute_0 text, -- target attribute = relation_0.attribute_0
attribute_2 text, -- target attribute = relation_0.attribute_2
attribute_3 text, -- target attribute = relation_0.attribute_3
attribute_4 text, -- target attribute = relation_0.attribute_4
attribute_11 text);

DROP TABLE IF EXISTS public.relation_2 CASCADE ;
CREATE TABLE public.relation_2 (
attribute_6 text UNIQUE, 
attribute_1 text UNIQUE, -- target attribute = relation_0.attribute_1
attribute_7 text, 
attribute_8 text, 
attribute_9 text, 
attribute_10 text);

