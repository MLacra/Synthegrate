-- Final SQL mapping: 
SELECT attribute_0,attribute_1,attribute_2,attribute_3,attribute_4 FROM (SELECT attribute_0,attribute_1,attribute_2,attribute_3,attribute_4 FROM schema_1.relation_2 AS relation_2 JOIN schema_1.relation_1 AS relation_1 ON relation_2.attribute_6 = relation_1.attribute_5) as relation_0 ORDER BY attribute_0,attribute_1,attribute_2,attribute_3,attribute_4
/*SELECT attribute_0,attribute_1,attribute_2,attribute_3,attribute_4 FROM 
	(
		schema_1.relation_2 AS relation_2
	JOIN
		schema_1.relation_1 AS relation_1
	ON relation_2.attribute_6 = relation_1.attribute_5
	) 
 as relation_0
ORDER BY attribute_0,attribute_1,attribute_2,attribute_3,attribute_4*/