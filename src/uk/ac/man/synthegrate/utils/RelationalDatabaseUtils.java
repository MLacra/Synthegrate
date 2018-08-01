package uk.ac.man.synthegrate.utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.ac.man.synthegrate.schema_components.Attribute;
import uk.ac.man.synthegrate.schema_components.DBSchema;
import uk.ac.man.synthegrate.schema_components.FKconstraint;
import uk.ac.man.synthegrate.schema_components.Relation;



public class RelationalDatabaseUtils {

	private static final Logger logger = Logger.getLogger(RelationalDatabaseUtils.class.getName());

	// JDBC driver name and database URL
	static String JDBC_DRIVER = "org.postgresql.Driver";
	static String JDBC_DRIVER_TYPE = "JDBC_POSTGRES";
	static String CONNECTION_PREFIX = "jdbc:postgresql://";
	static String SERVER_URL = "localhost:5432/";
	static String VIRTUAL_DATABASE_NAME = "testsdatabase";

	// Database credentials
	// TODO create config file for these
	static String USER = "lara";
	static String PASSWORD = "postgres";

	public RelationalDatabaseUtils(String jdbcDriver, String serverURL, String username, String password) {
		JDBC_DRIVER = jdbcDriver;
		SERVER_URL = serverURL;
		USER = username;
		PASSWORD = password;
	}

	public static String getCONNECTION_PREFIX() {
		return CONNECTION_PREFIX;
	}

	public static void setCONNECTION_PREFIX(String cONNECTION_PREFIX) {
		CONNECTION_PREFIX = cONNECTION_PREFIX;
	}

	public static String getJDBC_DRIVER_TYPE() {
		return JDBC_DRIVER_TYPE;
	}

	public static void setJDBC_DRIVER_TYPE(String jDBC_DRIVER_TYPE) {
		JDBC_DRIVER_TYPE = jDBC_DRIVER_TYPE;
	}

	public static String getJDBC_DRIVER() {
		return JDBC_DRIVER;
	}

	public static void setJDBC_DRIVER(String jDBC_DRIVER) {
		JDBC_DRIVER = jDBC_DRIVER;
	}

	public static String getUSER() {
		return USER;
	}

	public static void setUSER(String uSER) {
		USER = uSER;
	}

	public static String getPASSWORD() {
		return PASSWORD;
	}

	public static void setPASSWORD(String pASSWORD) {
		PASSWORD = pASSWORD;
	}

	private static Connection getServerConnection() {
		try {
			Class.forName("org.postgresql.Driver");
			logger.info("Connecting to server...");
			return DriverManager.getConnection(CONNECTION_PREFIX + SERVER_URL, USER, PASSWORD);
		} catch (ClassNotFoundException e) {
			logger.log(Level.SEVERE,"Error! - The connection to the server could not be established! ");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String get_db_url_string(String dbName){
		if (dbName == null || dbName.equals(""))
			dbName = VIRTUAL_DATABASE_NAME;
		String dbUrl = SERVER_URL + dbName;
		return dbUrl;
	}

	private static Connection getDBConnection(String dbName) {
		Connection connection = null;

		if (dbName == null || dbName.equals(""))
			dbName = VIRTUAL_DATABASE_NAME;
		String dbUrl = CONNECTION_PREFIX + SERVER_URL + dbName;

		try {
			Class.forName("org.postgresql.Driver");
			logger.info("Connecting to database...");
			connection = DriverManager.getConnection(dbUrl, USER, PASSWORD);

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			// Something went wrong and the db doesn't exist
			logger.info("The database was not found so it will be created.");
			// TODO change this after we no longer use Spicy
			createDatabase(dbName);
		}

		if (connection == null) {
			try {
				connection = DriverManager.getConnection(dbUrl, USER, PASSWORD);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return connection;
	}

	@SuppressWarnings("unused")
	private static void createDatabase() {
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = getServerConnection();
			System.out.println("Creating database...");
			stmt = conn.createStatement();

			String sql = "CREATE DATABASE " + VIRTUAL_DATABASE_NAME;
			stmt.executeUpdate(sql);
			System.out.println("Database '" + VIRTUAL_DATABASE_NAME + "' created successfully...");
		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		} // end try
	}

	/**
	 * This is just a temporary method that will be used in order to work with
	 * Spicy Spicy uses the DB name, not the schema name, so instead of creating
	 * schemas, we have to create DBs that are fed into spicy.
	 * 
	 * This will be deprecated after we will no longer work with Spicy.
	 * 
	 * @param newDBname
	 */
	private static void createDatabase(String newDBname) {
		Connection conn = getServerConnection();

		if (newDBname == null || newDBname.equals(""))
			newDBname = VIRTUAL_DATABASE_NAME;

		Statement stmt = null;
		try {
			System.out.println("Creating database...");
			stmt = conn.createStatement();

			String sql = "CREATE DATABASE " + newDBname;
			stmt.executeUpdate(sql);
			System.out.println("Database '" + newDBname + "' created successfully...");
		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			} // nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		} // end try
	}

	/**
	 * This method is not currently used because Spicy doesn't need the schema
	 * name to create its internal objects. This will be used after we will no
	 * longer work with Spicy and we will actually work with schemas, not DBs.
	 * 
	 * If dbName is default, then send dbName should be null or empty String
	 * 
	 * @param dbName,
	 *            newSchemaName
	 */
	public static DBSchema createSchema(String dbName, String newSchemaName) {
		
		DBSchema schema = new DBSchema();
		
		Connection connection = null;
		Statement stmt = null;
	
		try {
			connection = getDBConnection(dbName);
			logger.info("Opened database successfully");

			stmt = connection.createStatement();

			String sql = "DROP SCHEMA IF EXISTS "+newSchemaName+" CASCADE; CREATE SCHEMA " + newSchemaName;
			stmt.executeUpdate(sql);
			
			logger.info(
					"Schema " + newSchemaName + " created successfully in " + VIRTUAL_DATABASE_NAME + " database.");
			
			schema.setName(newSchemaName);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			} // nothing we can do
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		} // end finally try
		
		return schema;
	}

	public static Relation createTable(String dbName, DBSchema schema, String newTableName) {
		
		String schemaName = schema.getName();
		
		Relation relation = null;
		
		String tableName = newTableName;
		Connection connection = null;
		Statement stmt = null;
		try {
			
			if (tableName == null || tableName.equals(""))
				throw new SQLException();
			
			relation = new Relation();
			relation.setName(tableName);
			
			if (schemaName != null && schemaName.length() > 0)
				tableName = schemaName + "." + newTableName;
			
			connection = getDBConnection(dbName);
			logger.info("Opened database successfully");

			stmt = connection.createStatement();

			String sql = "CREATE TABLE " + tableName + "()";
			stmt.executeUpdate(sql);
			
			relation.setParentSchema(schema);
			schema.addRelation(relation);
			
			logger.info("Table " + tableName + " created successfully in " + VIRTUAL_DATABASE_NAME + " database.");
		} catch (SQLException e) {
			logger.severe("An error occurred while creating table '" + tableName + "' in " + VIRTUAL_DATABASE_NAME
					+ " database.");
			relation = null;
		}
		finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			} // nothing we can do
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		} // end finally try
		
		return relation;
	}

	public static void addAttributeToTable(String dbName, String schemaName, String tableName, String newAttribute,
			String type) {
		Connection connection = null;
		Statement stmt = null;
		try {
			connection = getDBConnection(dbName);
			logger.info("Opened database successfully");

			stmt = connection.createStatement();

			if (tableName == null || newAttribute == null || tableName.length() < 1 || newAttribute.length() < 1 )
				throw new SQLException();

			if (type == null || type.length() < 1)
				type = "text";

			if (schemaName != null && schemaName.length() > 0)
				tableName = schemaName + "." + tableName;

			String sql = "ALTER TABLE " + tableName + " ADD " + newAttribute + " " + type;
			stmt.executeUpdate(sql);

			logger.info("Attribute " + newAttribute + " created successfully in " + tableName + " table.");
		} catch (SQLException e) {
			logger.severe("Error creating " + newAttribute + " in " + tableName + " table.");
		}
		finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			} // nothing we can do
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		} // end finally try
	}
	
	public static ArrayList<Attribute> addAttributesToTable(String dbName, DBSchema schema, Relation table, HashMap<String, String> newAttributes) {
		
		String schemaName = schema.getName();
		String tableName = table.getName();
		
		ArrayList<Attribute> attributes = new ArrayList<>();
		
		Connection connection = null;
		Statement stmt = null;
		try {
			connection = getDBConnection(dbName);
			logger.info("Opened database successfully");

			stmt = connection.createStatement();

			if (tableName == null || newAttributes == null || tableName.length() < 1 || newAttributes.isEmpty())
				throw new SQLException();

			if (schemaName != null && schemaName.length() > 0)
				tableName = schemaName + "." + tableName;

			String sql = "ALTER TABLE " + tableName + " ADD COLUMN " ;
			boolean ifFirst = true;
			for (String attributeName:newAttributes.keySet())
			{
				if (ifFirst){
					sql += attributeName+" "+newAttributes.get(attributeName);
					ifFirst = false;
				}
				else
					sql += ", ADD COLUMN "+attributeName+" "+newAttributes.get(attributeName);
				
				Attribute attribute = new Attribute();
				attribute.setName(attributeName);
				attribute.setParentRelation(table);
				attribute.setExpected_in_merge(true);
				attribute.setNot_null(true);
				attribute.setType(newAttributes.get(attributeName));
				
				attributes.add(attribute);
			}
			sql+=";";
			
			stmt.executeUpdate(sql);

			logger.info("Attribute " + newAttributes.toString() + " created successfully in " + tableName + " table.");
		} catch (SQLException e) {
			logger.severe("Error creating " + newAttributes.toString() + " in " + tableName + " table.");
			logger.severe(e.getMessage());
			attributes = null;
		}
		finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			} // nothing we can do
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException se) {
				se.printStackTrace();
				logger.severe(se.getMessage());
			}
		} // end finally try
		
		return attributes;
	}
	
//	public static void insertTuples(String dbName, String schemaName, String tableName,ArrayList<Tuple> tuples)
//	{
//		Connection connection = null;
//		Statement stmt = null;
//		try {
//			connection = getDBConnection(dbName);
//			logger.info("Opened database successfully");
//
//			stmt = connection.createStatement();
//
//			if (tableName == null || tableName.length() < 1 || tuples.size() < 1 )
//				throw new SQLException();
//
//			if (schemaName != null && schemaName.length() > 0)
//				tableName = schemaName + "." + tableName;
//
//			for (Tuple tuple:tuples)
//			{
//				String sql = "INSERT INTO " + tableName + " (";
//				ArrayList<String> columns = new ArrayList<>();
//				ArrayList<String> values = new ArrayList<>();
//				for (Attribute attribute: tuple.getValues().keySet())
//				{
//					columns.add(attribute.getName());
//					values.add(tuple.getValues().get(attribute).getValue());
//				}
//				
//				if (!values.isEmpty() && !columns.isEmpty())
//				{
//					String valuesConcat = ((values.get(0) == null) ? "NULL" : "'"+values.get(0).replaceAll("'","''")+"'");
//					
//					for (int i = 1;i<values.size();i++)
//					{
//						valuesConcat += ((values.get(i) == null) ? ", NULL" : ", '"+values.get(i).replaceAll("'","''")+"'");
//					}
//					sql+= String.join(",", columns)+") VALUES ("+valuesConcat+");";
//					
//					//System.out.println(sql);
//					stmt.executeUpdate(sql);
//				}
//			}
//
//		} catch (SQLException e) {
//			logger.severe("An error occurred while inserting tuples " 
//					+ " in " + tableName + " table.");
//		}
//		finally {
//			// finally block used to close resources
//			try {
//				if (stmt != null)
//					stmt.close();
//			} catch (SQLException se2) {
//			} // nothing we can do
//			try {
//				if (connection != null)
//					connection.close();
//			} catch (SQLException se) {
//				se.printStackTrace();
//			}
//		} // end finally try
//	}

	public static void createPrimaryKey(String dbName, DBSchema schema, Relation relation,
			ArrayList<String> attributeNames, String constraintName) {
		
		String tableName = relation.getName();
		String schemaName = schema.getName();
	
		
		Connection connection = null;
		Statement stmt = null;
		try {
			connection = getDBConnection(dbName);
			logger.info("Opened database successfully");

			stmt = connection.createStatement();

			if (tableName.length() < 1 || tableName == null || attributeNames.size() < 1 || attributeNames == null)
				throw new SQLException();
			
			if (constraintName== null || constraintName.length() < 1)
				constraintName = "PK"+tableName;
			
			if (schemaName != null && schemaName.length() > 0)
				tableName = schemaName + "." + tableName;
			
			String sql = "ALTER TABLE " + tableName + " ADD PRIMARY KEY (" + String.join(",", attributeNames) + ")";
			stmt.executeUpdate(sql);
		
			
			ArrayList<Attribute> sourceAttributes = new ArrayList<>();
			for (String attributeName:attributeNames)
			{
				sourceAttributes.add(relation.getAttributeByName(attributeName));
			}
			
			logger.info("Primary key " + String.join(",", attributeNames) + " created successfully for " + tableName
					+ " table.");
		} catch (SQLException e) {
			logger.severe("An error occurred while creating the primary key " + String.join(",", attributeNames)
					+ " for " + tableName + " table.");
		}
		finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			} // nothing we can do
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException se) {
				se.printStackTrace();
				logger.severe(se.getMessage());
			}
		} // end finally try
	}

	public static void createForeignKey(String dbName, DBSchema schema, Relation referencedTable,
			ArrayList<String> referencedAttributes, Relation referenceeTable, ArrayList<String> referenceeAttributes, String fKeyName) {
		
		 String schemaName = schema.getName();
		 String referencedTableName = referencedTable.getName();
		 String referenceeTableName = referenceeTable.getName();
		
		FKconstraint fkConstraint = null;
		
		Connection connection = null;
		Statement stmt = null;
		try {
			connection = getDBConnection(dbName);
			logger.info("Opened database successfully");

			stmt = connection.createStatement();
			if (referencedTableName.length() < 1 || referencedTableName == null || referencedAttributes.size() < 1
					|| referencedAttributes == null || referenceeTableName.length() < 1 || referenceeTableName == null
					|| referenceeAttributes.size() < 1 || referenceeAttributes == null)
				throw new SQLException();
			
			fkConstraint = new FKconstraint();
			
			if (fKeyName==null || fKeyName.length()<1)
				fKeyName = "FK_"+referenceeTableName+"_PK_"+referencedTable;

			fkConstraint.setName(fKeyName);
			
			if (schemaName != null && schemaName.length() > 0) {
				referencedTableName = schemaName + "." + referencedTableName;
				referenceeTableName = schemaName + "." + referenceeTableName;
			}
			
			String sql = "ALTER TABLE " + referenceeTableName + " ADD CONSTRAINT "+fKeyName+" FOREIGN KEY ("
					+ String.join(",", referenceeAttributes) + ")" + " REFERENCES " + referencedTableName + "("
					+ String.join(",", referencedAttributes) + ")";
			stmt.executeUpdate(sql);
			
			//TODO - only works for single attribute FKs
			fkConstraint.setFkAttribute(referenceeTable.getAttributeByName(referenceeAttributes.get(0)));
			referenceeTable.getAttributeByName(referenceeAttributes.get(0)).set_foreign_key(true);
			//TODO this gets the first key - we know that ibench doesn't gen more than one PK - this is not a general rule 
			fkConstraint.setReferencedPK(referencedTable.getCandidateKeywPK());
			fkConstraint.setFKTable(referenceeTable);
			fkConstraint.setPKTable(referencedTable);
			
			schema.addFK(fkConstraint);
			
			logger.info("Foreign key (" + String.join(",", referenceeAttributes) + "->"
					+ String.join(",", referencedAttributes) + ") created successfully.");
		} catch (SQLException e) {
			logger.severe("An error occurred while creating the foreign key (" + String.join(",", referenceeAttributes)
					+ "->" + String.join(",", referencedAttributes) + ") for " + referenceeTableName + " table.");
			logger.severe(e.getMessage());
		}
		finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			} // nothing we can do
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		} // end finally try
	}

	public static void dropDatabase(String dbName) {
		Connection connection = null;
		Statement stmt = null;
		try {
			connection = getServerConnection();
			logger.info("Server connection opened successfully");

			stmt = connection.createStatement();

			String sql = "DROP DATABASE " + dbName + ";";
			stmt.executeUpdate(sql);
			stmt.close();
			connection.close();
			logger.info("Database " + dbName + " dropped successfully.");
		} catch (SQLException e) {
			logger.severe("An error occurred while dropping database " + dbName + ".");
		}
		finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			} // nothing we can do
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		} // end finally try
	}

	public static String getServerURL() {
		return SERVER_URL;
	}

	
	public static String readPrimaryKeyName(String dbName, String schemaName, String relationName) {
		String columnName ="";

		Connection connection = getDBConnection(dbName);
		
		if (connection==null)
			return null;
		
		try {
			DatabaseMetaData dbmd = connection.getMetaData();
			ResultSet pkColumns = dbmd.getPrimaryKeys(null, schemaName, relationName);
			while (pkColumns.next()) {
				columnName = pkColumns.getString("COLUMN_NAME");
//				String pkName = pkColumns.getString("PK_NAME");
			}
		} catch (SQLException e) {
			logger.severe("Error when reading primary keys for database: " + schemaName);
			e.printStackTrace();
		}finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return columnName;
	}
	
	public static ArrayList<String> readAttributesNames(String dbName, String schemaName, String relationName) {
		ArrayList<String> columnNames =new ArrayList<>();

		Connection connection = getDBConnection(dbName);
		
		if (connection==null)
			return null;
		
		try {
			DatabaseMetaData dbmd = connection.getMetaData();
			ResultSet columns = dbmd.getColumns(null, schemaName, relationName, null);
			while (columns.next()) {
				columnNames.add(columns.getString("COLUMN_NAME"));
//				String pkName = pkColumns.getString("PK_NAME");
			}
		} catch (SQLException e) {
			logger.severe("Error when reading attribute names for relation: " + schemaName+"."+relationName);
			e.printStackTrace();
		}finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return columnNames;
	}
	
	public static ArrayList<String> readAttributesNames(String dbName, String sqlQuery) {
		ArrayList<String> columnNames =new ArrayList<>();

		Connection connection = getDBConnection(dbName);
		
		if (connection==null)
			return null;
		
		try {
			Statement st = connection.createStatement();
			ResultSet rset = st.executeQuery(sqlQuery);
			ResultSetMetaData md = rset.getMetaData();
			logger.info("Reading attribute names ...");
			for (int i=1; i<=md.getColumnCount(); i++)
			{
				columnNames.add(md.getColumnLabel(i));
			}
			
		} catch (SQLException e) {
			logger.severe("Error when reading attribute names for query: " + sqlQuery);
			e.printStackTrace();
		}finally {
			try {
				logger.info("Closed database connection.");
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return columnNames;
	}
}
