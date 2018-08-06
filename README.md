# Synthegrate

Synthegrate is a tool that can be used to generate integration scenarios with synthetic data. It was created with the purpose of evaluating mapping generation algorithms, but it can be used in various kinds of integration tests.

## Getting Started
To run Synthegrate, you can simply download Synthegrate.jar and synthegrate.properties files.

### Prerequisites

1. For generating the scenario files: Java 8

2. [Optional] For materializing the data: 
   - PostgreSQL + a database management tool (such as *pgAdmin*)
   - Python (for running ```datafiller.py```)

### Running

- To create the synthetic scenario run the jar file:

  ```java -jar Synthegrate.jar -p synthegrate.properties```

- To materialize the synthetic scenario:

  1. create the postgres database. Note: the name needs to be the same as the one set in the properties file (`database.name` parameter). 
  2. in the parent folder of the generated scenario folder run: 

  ``` ./create_db_schemas <database_name>``` 

Note: Make sure that all the files in **runnable_jar/to_materialize** folder are in the same folder as the scenario **folder**.
E.g.

- working_directory
  - scenario_1_0_5_6_100_300_2_0_2_2 (synthetic scenario folder)
  - build_spicy_target_db
  - create_db_schemas
  - datafiller.py
  - dataFillerRunner

## Output files

The generated output files are all contained within a separate folder for each scenario:
 - 1 *schema_schema_X.sql* file for each source schema (can be more than 1 - depends on the ```scenario.schemas_number``` parameter)
 - 1 *schema_schema_X.sql* file for the target schema (the number of target schemas is fixed to 1)
 - 1 *datafiller_schema_X.sql* file for each source schema (used by [Datafiller](https://www.cri.ensmp.fr/people/coelho/datafiller.html) to populate the instances)
 - 1 file for matches (which source attribute matches which target attribute). The format of a match is:
   - Match(<source_schema_name>.<source_relation_name>.<source_attribute_name>,<target_schema_name>.<target_relation_name>.<target_attribute_name>,<confidence>).
 - 1 SQL file for the ground truth (expected) mapping. This file is not executable as it contains two parts:
   - the SQL script (that needs to be separated if run)
   - the short description of the mapping - which becomes handy when the mapping tasks become very complicated.
 - 1 file for linking the schema names to the corresponding database. An entry has the following format:
   - datasource(<name_used_in_other_files>,localhost:5432/<database_name>,JDBC_POSTGRES,null).
   - this will be generated according to the set database parameters in the properties file.
 - 1 file for declaring which source schema needs to map which target schema. One entry in the file corresponds to each source schema that needs to be mapped:
   - map_schemas(<source_schema_name>,<target_schema_name>).
   - the names are the <name_used_in_other_files> declared in the *datasource* file
 - 1 XML file containing a mapping task for [++Spicy](http://www.db.unibas.it/projects/spicy/) (a mapping generation tool)
 - separate schema_X_spicy_source/target.sql files for creating the same tables in the *public* schema as ++Spicy does not recognize other schemas.
 - 1 XML file for profiling data. This file contains information about (partial) inclusion dependencies and candidate keys.
 
## synthegrate.properties file 

### Output files 
1. Set the output folder:
   - output.folder = <where to create the scenario folder>

2. PROFILE DATA filename (**not** full path)
   - output.profile = <what is the name of the xml file that will contain profile data information>

3. MATCHES filename (**not** full path)
   - output.match = <the name of the matches file>

4. DATASOURCE filename (**not** full path)
   - output.sources = <the name of the file that will contain information about where in the database can the source be found>

5. SOURCE-TARGET filename
   - output.source_target = <the name of the file that will contain information about which source schema needs mapping to which target schema>

6. EXPECTED MAPPING filename
   - output.expected_mapping = <the name of the SQL file that will contain the ground truth (expected) mapping>

7. [++Spicy](http://www.db.unibas.it/projects/spicy/) mapping task filename
   - output.spicy_maptask = <filename>
   
### Input files
1. The path to the folder where the dictionaries can be found (or created if not found suitable). The dictionaries are used to create the tuple instances with *datafiller.py*. 
   - input.dictionaries_folder = <folder_path>

### Scenario parameters

1. scenario.max_source_relations = 1000
2. scenario.min_cardinality = 100
3. scenario.max_cardinality = 300 
4. scenario.min_arity = 5 
5. scenario.max_arity = 6 

6. scenario.tries = 1000 
7. scenario.schemas_number = 1 
8. scenario.target_relations = 1 
9. if the target has more than one table, then set how many tables have a declared primary key.
   - scenario.target_pks_number = 1 
	
10. Number of maximum join operations:
    - scenario.max_joins_number = 1 
11. Number of maximum join operations:
    - scenario.max_unions_number = 0 
    
12. Explicit foreign keys ratio out of the total FK opportunities that are created. The FK opportunities can be created just by creating proper candidate keys and inclusion dependencies in the profile data file.
    - scenario.fks_ratio = <number between 0.0 and 1.0>
13. Disjoint unions ratio out of the total created (i.e., if the instances of the unionable relations are disjoint):
    - scenario.disjoint_unions = <number between 0.0 and 1.0>
14. Reuse already created attribute to split a relation in 2 relations linked through (explicit) FK.
    - scenario.reuse_join_attributes = <number between 0.0 and 1.0>
15. If the scenario should be created with the such that the union operations are expected at the end of the mapping.
    - scenario.create_unions_first = <**true** if unions are at the end, **false** if the unions are expected to be performed first>
16. The same as 15 but for join operations:
	- scenario.create_joins_first = <true or false> 

17. When creating join opportunities, the join can be of several types:
    - scenario.star_join = <true or false> 
    - scenario.chain_join = <true or false> 
    - scenario.balanced_join = <true or false>  


### Database parameters
database.user=<username> (default value: postgres)
database.password=<password for username> (default value: postgres)
database.location = <ip> (default value: localhost)
database.port = <port> (default value: 5432)
database.name = <database name>
database.jdbc_driver = JDBC_POSTGRES (currently, POSTGRES is the only supported database, so it will not work with other parameters)
database.jdbc_prefix = jdbc:postgresql://


## License

This project is licensed under the Apache 2.0 License - see the [LICENSE.md](LICENSE.md) file for details


