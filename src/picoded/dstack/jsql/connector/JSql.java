package picoded.dstack.jsql.connector;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import picoded.set.JSqlType;

/// JSql provides a wrapper around several common SQL implementations. Via an (almost) single set of syntax.
/// 
/// How this works is by using a core base syntax, which is based off mysql/sqlite. And writing an intermidiary
/// parser for each SQL implementation. To work around its vendor specific issue, and run its respective commands.
/// 
/// The major down side, is that there is no true multi statement or transaction support.
/// Not that most programmers actually know how to properly do so. 
///
/// SECURITY NOTE: care should ALWAYS be taken to prevent SQL injection when dealing with query strings.
/// 
/// Currently Supported SQL Databases
/// + MySQL
/// + Oracle
/// + MS-SQL
/// + Sqlite
/// 
/// Database intreface base class.
public abstract class JSql {
	
	//-------------------------------------------------------------------------
	//
	// Reusable output logging
	//
	//-------------------------------------------------------------------------
	
	/// Internal self used logger
	protected static final Logger LOGGER = Logger.getLogger(JSql.class.getName());
	
	//-------------------------------------------------------------------------
	//
	// Database specific constructors
	//
	//-------------------------------------------------------------------------
	
	/// SQLite static constructor, returns picoded.dstack.jsql.connector.db.JSql_Sqlite
	public static JSql sqlite() {
		return new picoded.dstack.jsql.connector.db.JSql_Sqlite();
	}
	
	/// SQLite static constructor, returns picoded.dstack.jsql.connector.db.JSql_Sqlite
	public static JSql sqlite(String sqliteLoc) {
		return new picoded.dstack.jsql.connector.db.JSql_Sqlite(sqliteLoc);
	}

	/*
	
	/// MySql static constructor, returns picoded.JSql.JSql_Mysql
	public static JSql mysql(String dbServerAddress, String dbName, String dbUser, String dbPass) {
		return new picoded.dstack.jsql.connector.db.JSql_Mysql(dbServerAddress, dbName, dbUser, dbPass);
	}
	
	/// MySql static constructor, returns picoded.JSql.JSql_Mysql
	public static JSql mysql(String connectionUrl, Properties connectionProps) {
		return new picoded.dstack.jsql.connector.db.JSql_Mysql(connectionUrl, connectionProps);
	}
	
	/// Mssql static constructor, returns picoded.JSql.JSql_Mssql
	public static JSql mssql(String dbUrl, String dbName, String dbUser, String dbPass) {
		return new picoded.dstack.jsql.connector.db.JSql_Mssql(dbUrl, dbName, dbUser, dbPass);
	}
	
	/// Oracle static constructor, returns picoded.dstack.jsql.connector.db.JSql_Oracle
	public static JSql oracle(String oraclePath, String dbUser, String dbPass) {
		return new picoded.dstack.jsql.connector.db.JSql_Oracle(oraclePath, dbUser, dbPass);
	}
	
	public static JSql oracle(Connection inSqlConn) {
		return new picoded.dstack.jsql.connector.db.JSql_Oracle(inSqlConn);
	}
	*/
	
	//-------------------------------------------------------------------------
	//
	// Database connection handling
	//
	//-------------------------------------------------------------------------
	
	// Database connection settings variables
	//-------------------------------------------------------------------------
	
	/// Internal refrence of the current sqlType the system is running as
	protected JSqlType sqlType = JSqlType.INVALID;
	
	/// Java standard database connection
	protected Connection sqlConn = null;
	
	/// database connection properties
	protected Map<String, Object> connectionProps = null;
	
	// Database connection settings functions
	//-------------------------------------------------------------------------
	
	/// Returns the current sql type, this is read only
	///
	/// @return JSqlType  current implmentation mode
	public JSqlType sqlType() {
		return this.sqlType;
	}

	///
	/// Store the database connection parameters for recreating the connection
	///
	/// setup the connection properties, this is normally set by the constructor
	/// and is reused via the recreate command.
	///
	/// @param  Database location
	/// @param  Database name
	/// @param  Database username
	/// @param  Database password
	/// @param  Additional connection properties
	///
	protected void setConnectionProperties(String dbUrl, String dbName, String dbUser, String dbPass,
		Properties connProps) {
		connectionProps = new HashMap<String, Object>();
		if (dbUrl != null) {
			connectionProps.put("dbUrl", dbUrl);
		}
		if (dbName != null) {
			connectionProps.put("dbName", dbName);
		}
		if (dbUser != null) {
			connectionProps.put("dbUser", dbUser);
		}
		if (dbPass != null) {
			connectionProps.put("dbPass", dbPass);
		}
		if (connProps != null) {
			connectionProps.put("connectionProps", connProps);
		}
	}
	
	/// Recreate the current SQL connection.
	/// This forcefully close any existing SQL connection, in the process if configured.
	///
	/// A common use case would be to forcefully clear and flush temporary sessions
	/// or resolve session / memory / ram related issues.
	/// 
	/// @param   Flag to indicate that the connection should be recreated even if it already exists
	public void recreate(boolean force) {
		throw new UnsupportedOperationException(JSqlException.invalidDatabaseImplementationException);
	}

	//-------------------------------------------------------------------------
	//
	// Standard raw query/execute command sets
	//
	//-------------------------------------------------------------------------
	
	/// Executes the argumented SQL query, and immediately fetches the result from
	/// the database into the result set. 
	///
	/// This is a raw execution. As such no special parsing occurs to the request
	///
	/// **Note:** Only queries starting with 'SELECT' will produce a JSqlResult object that has fetchable results
	///
	/// @param  Query strings including substituable variable "?"
	/// @param  Array of arguments to do the variable subtitution
	///
	/// @return  JSQL result set
	public JSqlResult query_raw(String qString, Object... values) throws JSqlException {
		JSqlResult result = noFetchQuery_raw(qString, values);
		if (result != null) {
			result.fetchAllRows();
		}
		return result;
	}

	/// Executes the argumented SQL query, and returns the result object *without*
	/// fetching the result data from the database. 
	///
	/// This is a raw execution. As such no special parsing occurs to the request
	///
	/// **Note:** Only queries starting with 'SELECT' will produce a JSqlResult object that has fetchable results
	///
	/// @param  Query strings including substituable variable "?"
	/// @param  Array of arguments to do the variable subtitution
	///
	/// @return  JSQL result set
	public JSqlResult noFetchQuery_raw(String qString, Object... values) throws JSqlException {
		throw new UnsupportedOperationException(JSqlException.invalidDatabaseImplementationException);
	}

	/// Executes the argumented SQL update.
	///
	/// Returns false if no result object is given by the execution call. 
	///
	/// This is a raw execution. As such no special parsing occurs to the request
	///
	/// @param  Query strings including substituable variable "?"
	/// @param  Array of arguments to do the variable subtitution
	///
	/// @return  true if operation is succesful
	public boolean update_raw(String qString, Object... values) throws JSqlException {
		return query_raw(qString, values) != null;
	}

	//-------------------------------------------------------------------------
	//
	// SQL parser
	//
	//-------------------------------------------------------------------------
	
	/// Used for refrence checks / debugging only. This represents the core
	/// generic SQL statement refactoring engine. That is currenlty used internally
	/// by query / update. Doing common regex substitutions if needed.
	///
	/// Long term plan is to convert this to a much more proprely structed AST engine.
	public String genericSqlParser(String inString) throws JSqlException {
		return inString;
	}
	
	//-------------------------------------------------------------------------
	//
	// Normalized, and parsed query/execute command sets
	//
	//-------------------------------------------------------------------------
	
	/// Executes the argumented SQL query, and immediately fetches the result from
	/// the database into the result set. 
	///
	/// Custom SQL specific parsing occurs here
	///
	/// **Note:** Only queries starting with 'SELECT' will produce a JSqlResult object that has fetchable results
	///
	/// @param  Query strings including substituable variable "?"
	/// @param  Array of arguments to do the variable subtitution
	///
	/// @return  JSQL result set
	public JSqlResult query(String qString, Object... values) throws JSqlException {
		JSqlResult result = noFetchQuery(qString, values);
		if (result != null) {
			result.fetchAllRows();
		}
		return result;
	}
	
	/// Executes the argumented SQL query, and returns the result object *without*
	/// fetching the result data from the database. 
	///
	/// Custom SQL specific parsing occurs here
	///
	/// **Note:** Only queries starting with 'SELECT' will produce a JSqlResult object that has fetchable results
	///
	/// @param  Query strings including substituable variable "?"
	/// @param  Array of arguments to do the variable subtitution
	///
	/// @return  JSQL result set
	public JSqlResult noFetchQuery(String qString, Object... values) throws JSqlException {
		throw new UnsupportedOperationException(JSqlException.invalidDatabaseImplementationException);
	}

	/// Executes the argumented SQL update.
	///
	/// Returns false if no result object is given by the execution call. 
	///
	/// Custom SQL specific parsing occurs here
	///
	/// @param  Query strings including substituable variable "?"
	/// @param  Array of arguments to do the variable subtitution
	///
	/// @return  JSQL result set
	public boolean update(String qString, Object... values) throws JSqlException {
		throw new UnsupportedOperationException(JSqlException.invalidDatabaseImplementationException);
	}

	/// Prepare an SQL statement, for execution subsequently later
	///
	/// Custom SQL specific parsing occurs here
	///
	/// @param  Query strings including substituable variable "?"
	/// @param  Array of arguments to do the variable subtitution
	///
	/// @return  Prepared statement
	public JSqlPreparedStatement prepareStatement(String qString, Object... values) {
		return new JSqlPreparedStatement(qString, values, this);
	}
	
	//-------------------------------------------------------------------------
	//
	// Connection closure / disposal
	//
	//-------------------------------------------------------------------------
	
	/// Returns true, if dispose() function was called prior
	public boolean isDisposed() {
		return sqlConn == null;
	}
	
	/// Dispose of the respective SQL driver / connection
	public void dispose() {
		// Disposes the instancce connection
		if (sqlConn != null) {
			try {
				//sqlConn.join();
				sqlConn.close();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
			sqlConn = null;
		}
	}
	
	/// Just incase a user forgets to dispose "as per normal"
	protected void finalize() throws Throwable {
		try {
			dispose(); // close open files
		} finally {
			super.finalize();
		}
	}
	
	//-------------------------------------------------------------------------
	//
	// Utility helper functions used to prepare common complex SQL quries
	//
	//-------------------------------------------------------------------------
	
	/// Merge the 2 arrays together
	/// Used to join arguments together
	///
	/// @param  Array of arguments 1
	/// @param  Array of arguments 2
	///
	/// @return  Resulting array of arguments 1 & 2
	public Object[] joinArguments(Object[] arr1, Object[] arr2) {
		return org.apache.commons.lang3.ArrayUtils.addAll(arr1, arr2);
	}
	
	/// Sets the auto commit level
	///
	/// @param  The auto commit level flag to set
	public void setAutoCommit(boolean autoCommit) throws JSqlException {
		try {
			sqlConn.setAutoCommit(autoCommit);
		} catch (Exception e) {
			throw new JSqlException(e);
		}
	}
	
	/// Gets the current auto commit setting
	///
	/// @return true if auto commit is enabled
	public boolean getAutoCommit() throws JSqlException {
		try {
			return sqlConn.getAutoCommit();
		} catch (Exception e) {
			throw new JSqlException(e);
		}
	}
	
	/// Runs the commit (use only if setAutoCommit is false)
	public void commit() throws JSqlException {
		try {
			sqlConn.commit();
		} catch (Exception e) {
			throw new JSqlException(e);
		}
	}
	
	//-------------------------------------------------------------------------
	//
	// CREATE TABLE statement builder 
	//
	//-------------------------------------------------------------------------
	
	///
	/// Helps generate an SQL CREATE TABLE IF NOT EXISTS request. This function was created to acommedate the various
	/// syntax differances of CREATE TABLE IF NOT EXISTS across the various SQL vendors (if any).
	///
	/// The syntax below, is an example of such an CREATE TABLE IF NOT EXISTS statement for SQLITE.
	///
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.SQL}
	/// CREATE TABLE IF NOT EXISTS TABLENAME ( COLLUMNS_NAME TYPE, ... )
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	///
	/// @param  Table name to query        (eg: tableName)
	/// @param  Columns to create          (eg: col1, col2)
	/// @param  Columns types              (eg: int, text)
	///
	/// @return  true, if CREATE TABLE execution is succesful
	public boolean createTable( //
		String tableName, // Table name to create
		String[] columnName, // The column names
		String[] columnTypes // The column types
	) {
		return createTableStatement(tableName, columnName, columnTypes).update();
	}
	
	///
	/// Helps generate an SQL CREATE TABLE IF NOT EXISTS request. This function was created to acommedate the various
	/// syntax differances of CREATE TABLE IF NOT EXISTS across the various SQL vendors (if any).
	///
	/// The syntax below, is an example of such an CREATE TABLE IF NOT EXISTS statement for SQLITE.
	///
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.SQL}
	/// CREATE TABLE IF NOT EXISTS TABLENAME ( COLLUMNS_NAME TYPE, ... )
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	///
	/// @param  Table name to query        (eg: tableName)
	/// @param  Columns to create          (eg: col1, col2)
	/// @param  Columns types              (eg: int, text)
	///
	/// @return  A prepared CREATE TABLE statement
	public JSqlPreparedStatement createTableStatement( //
		String tableName, // Table name to create
		String[] columnName, // The column names
		String[] columnTypes // The column types
	) {
		throw new UnsupportedOperationException(JSqlException.invalidDatabaseImplementationException);
	}
	
	//-------------------------------------------------------------------------
	//
	// SELECT statement builder 
	//
	//-------------------------------------------------------------------------
	
	/// Helps generate an SQL SELECT request. This function was created to acommedate the various
	/// syntax differances of SELECT across the various SQL vendors (if any).
	///
	/// See : selectStatement for full docs
	///
	/// @param  Table name to query        (eg: tableName)
	/// @param  Columns to select          (eg: col1, col2)
	///
	/// @return  The JSqlResult
	public JSqlResult select( //
		String tableName, // Table name to select from
		String selectStatement // The Columns to select, null means all
	) {
		return selectStatement(tableName, selectStatement, null, null, null, 0, 0).query();
	}

	/// Helps generate an SQL SELECT request. This function was created to acommedate the various
	/// syntax differances of SELECT across the various SQL vendors (if any).
	///
	/// See : selectStatement for full docs
	///
	/// @param  Table name to query        (eg: tableName)
	/// @param  Columns to select          (eg: col1, col2)
	/// @param  Where statement to filter  (eg: col1=?)
	/// @param  Where arguments value      (eg: [value/s])
	///
	/// @return  The JSqlResult
	public JSqlResult select( //
		String tableName, // Table name to select from
		String selectStatement, // The Columns to select, null means all
		String whereStatement, // The Columns to apply where clause, this must be sql neutral
		Object[] whereValues // Values that corresponds to the where statement
	) {
		return selectStatement(tableName, selectStatement, whereStatement, whereValues, null, 0, 0).query();
	}

	/// Helps generate an SQL SELECT request. This function was created to acommedate the various
	/// syntax differances of SELECT across the various SQL vendors (if any).
	///
	/// See : selectStatement for full docs
	///
	/// @param  Table name to query        (eg: tableName)
	/// @param  Columns to select          (eg: col1, col2)
	/// @param  Where statement to filter  (eg: col1=?)
	/// @param  Where arguments value      (eg: [value/s])
	/// @param  Order by statement         (eg: col2 DESC)
	/// @param  Row count limit            (eg: 2)
	/// @param  Row offset                 (eg: 3)
	///
	/// @return  The JSqlResult
	public JSqlResult select( //
		String tableName, // Table name to select from
		//
		String selectStatement, // The Columns to select, null means all
		//
		String whereStatement, // The Columns to apply where clause, this must be sql neutral
		Object[] whereValues, // Values that corresponds to the where statement
		//
		String orderStatement, // Order by statements, must be either ASC / DESC
		//
		long limit, // Limit row count to, use 0 to ignore / disable
		long offset // Offset limit by?
	) {
		return selectStatement(tableName, selectStatement, whereStatement, whereValues, orderStatement, limit, offset).query();
	}

	/// Helps generate an SQL SELECT request. This function was created to acommedate the various
	/// syntax differances of SELECT across the various SQL vendors (if any).
	///
	/// The syntax below, is an example of such an SELECT statement for SQLITE.
	///
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.SQL}
	/// SELECT
	///	col1, col2   //select collumn
	/// FROM tableName //table name to select from
	/// WHERE
	///	col1=?       //where clause
	/// ORDER BY
	///	col2 DESC    //order by clause
	/// LIMIT 2        //limit clause
	/// OFFSET 3       //offset clause
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	///
	/// @param  Table name to query        (eg: tableName)
	/// @param  Columns to select          (eg: col1, col2)
	/// @param  Where statement to filter  (eg: col1=?)
	/// @param  Where arguments value      (eg: [value/s])
	/// @param  Order by statement         (eg: col2 DESC)
	/// @param  Row count limit            (eg: 2)
	/// @param  Row offset                 (eg: 3)
	///
	/// @return  A prepared SELECT statement
	public JSqlPreparedStatement selectStatement( //
		String tableName, // Table name to select from
		//
		String selectStatement, // The Columns to select, null means all
		//
		String whereStatement, // The Columns to apply where clause, this must be sql neutral
		Object[] whereValues, // Values that corresponds to the where statement
		//
		String orderStatement, // Order by statements, must be either ASC / DESC
		//
		long limit, // Limit row count to, use 0 to ignore / disable
		long offset // Offset limit by?
	) {
		throw new UnsupportedOperationException(JSqlException.invalidDatabaseImplementationException);
	}

	//-------------------------------------------------------------------------
	//
	// INSERT statement builder 
	//
	// @TODO : While not needed for current JavaCommons use case
	//         due to the extensive GUID usage of dstack. This may
	//         have use cases outside the core JavaCommons stack.
	//
	//-------------------------------------------------------------------------
	
	//-------------------------------------------------------------------------
	//
	// UPDATE statement builder 
	//
	// @TODO : While not needed for current JavaCommons use case
	//         due to the extensive GUID usage of dstack. This may
	//         have use cases outside the core JavaCommons stack.
	//
	//-------------------------------------------------------------------------
	
	//-------------------------------------------------------------------------
	//
	// UPSERT statement builder 
	//
	//-------------------------------------------------------------------------
	
	///
	/// Helps generate an SQL UPSERT request. This function was created to acommedate the various
	/// syntax differances of UPSERT across the various SQL vendors.
	///
	/// IMPORTANT, See : upsertStatement for full docs
	///
	/// @param  Table name to query        (eg: tableName)
	/// @param  Unique column names        (eg: id)
	/// @param  Unique column values       (eg: 1)
	/// @param  Upsert column names        (eg: fname,lname)
	/// @param  Upsert column values       (eg: 'Tom','Hanks')
	///
	/// @return  true, if UPSERT statement executed succesfuly
	///
	public boolean upsert(  //
		String tableName, // Table name to upsert on
		//
		String[] uniqueColumns, // The unique column names
		Object[] uniqueValues, // The row unique identifier values
		//
		String[] insertColumns, // Columns names to update
		Object[] insertValues // Values to update
	) {
		return upsertStatement( //
			tableName, //
			uniqueColumns, uniqueValues, //
			insertColumns, insertValues, //
			null, null, //
			null //
		).update();
	}

	///
	/// Helps generate an SQL UPSERT request. This function was created to acommedate the various
	/// syntax differances of UPSERT across the various SQL vendors.
	///
	/// IMPORTANT, See : upsertStatement for full docs
	///
	/// @param  Table name to query        (eg: tableName)
	/// @param  Unique column names        (eg: id)
	/// @param  Unique column values       (eg: 1)
	/// @param  Upsert column names        (eg: fname,lname)
	/// @param  Upsert column values       (eg: 'Tom','Hanks')
	/// @param  Default column to use existing values if exists   (eg: 'role')
	/// @param  Default column values to use if not exists        (eg: 'Benchwarmer')
	/// @param  All other column names to maintain existing value (eg: 'note')
	///
	/// @return  true, if UPSERT statement executed succesfuly
	public boolean upsert(  //
		String tableName, // Table name to upsert on
		//
		String[] uniqueColumns, // The unique column names
		Object[] uniqueValues, // The row unique identifier values
		//
		String[] insertColumns, // Columns names to update
		Object[] insertValues, // Values to update
		//
		String[] defaultColumns, //
		// Columns names to apply default value, if not exists
		// Values to insert, that is not updated. Note that this is ignored if pre-existing values exists
		Object[] defaultValues, //
		// Various column names where its existing value needs to be maintained (if any),
		// this is important as some SQL implementation will fallback to default table values, if not properly handled
		String[] miscColumns //
	) {
		return upsertStatement( //
			tableName, //
			uniqueColumns, uniqueValues, //
			insertColumns, insertValues, //
			defaultColumns, defaultValues, //
			miscColumns //
		).update();
	}
	
	///
	/// Helps generate an SQL UPSERT request. This function was created to acommedate the various
	/// syntax differances of UPSERT across the various SQL vendors.
	///
	/// First note that the term UPSERT is NOT an offical SQL syntax, but an informal combination of insert/update.
	///
	/// Note that misc column, while "this sucks" to fill up is required to ensure cross DB
	/// competibility as in certain cases this is required! 
	///
	/// This query alone is one of the largest reason this whole library exists,
	/// (the other being create table if not exists) due to its high usage,
	/// and extremely non consistent SQL implmentations across systems.
	///
	/// Its such a bad topic that even within versions of a single SQL vendor,
	/// the defacto method for achieving this changes. Its also because of this complexity
	/// why it probably will not be part of the generic query parser.
	///
	/// PostgreSQL specific notes : This does an "INSERT and ON CONFLICT UPDATE",
	/// in general this has the same meaning if you only have a single unique primary key.
	/// However a "CONFLICT" can occur through other cases such as primary/foreign keys (i think).
	/// Long story short, its complicated if you have multiple unique keys.
	///
	/// See: http://stackoverflow.com/questions/17267417/how-to-upsert-merge-insert-on-duplicate-update-in-postgresql
	///
	/// The syntax below, is an example of such an UPSERT statement for SQLITE.
	///
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.SQL}
	/// INSERT OR REPLACE INTO Employee (
	///	id,      // Unique Columns to check for upsert
	///	fname,   // Insert Columns to update
	///	lname,   // Insert Columns to update
	///	role,    // Default Columns, that has default fallback value
	///   note,    // Misc Columns, which existing values are preserved (if exists)
	/// ) VALUES (
	///	1,       // Unique value
	/// 	'Tom',   // Insert value
	/// 	'Hanks', // Update value
	///	COALESCE((SELECT role FROM Employee WHERE id = 1), 'Benchwarmer'), // Values with default
	///	(SELECT note FROM Employee WHERE id = 1) // Misc values to preserve
	/// );
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	///
	/// In general, for those compliant with SQL 2003 standard, with good performance.
	///
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.SQL}
	/// MERGE INTO Employee AS TARGET USING (
	///    SELECT 1 AS id
	/// ) AS SOURCE ON (
	///   TARGET.id = SOURCE.id
	/// ) WHEN MATCHED THEN
	///   ... UPDATE statement ...
	/// WHEN NOT MATCHED THEN
	///	... INSERT statement ... 
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	///
	/// @param  Table name to query        (eg: tableName)
	/// @param  Unique column names        (eg: id)
	/// @param  Unique column values       (eg: 1)
	/// @param  Upsert column names        (eg: fname,lname)
	/// @param  Upsert column values       (eg: 'Tom','Hanks')
	/// @param  Default column to use existing values if exists   (eg: 'role')
	/// @param  Default column values to use if not exists        (eg: 'Benchwarmer')
	/// @param  All other column names to maintain existing value (eg: 'note')
	///
	/// @return  A prepared UPSERT statement
	///
	public JSqlPreparedStatement upsertStatement( //
		String tableName, // Table name to upsert on
		//
		String[] uniqueColumns, // The unique column names
		Object[] uniqueValues, // The row unique identifier values
		//
		String[] insertColumns, // Columns names to update
		Object[] insertValues, // Values to update
		//
		String[] defaultColumns, //
		// Columns names to apply default value, if not exists
		// Values to insert, that is not updated. Note that this is ignored if pre-existing values exists
		Object[] defaultValues, //
		// Various column names where its existing value needs to be maintained (if any),
		// this is important as some SQL implementation will fallback to default table values, if not properly handled
		String[] miscColumns //
	) {
		throw new UnsupportedOperationException(JSqlException.invalidDatabaseImplementationException);
	}

	//-------------------------------------------------------------------------
	//
	// DELETE statement builder 
	//
	//-------------------------------------------------------------------------
	
	///
	/// Helps generate an SQL DELETE request. This function was created to acommedate the various
	/// syntax differances of DELETE across the various SQL vendors (if any).
	///
	/// See : deleteStatement for full docs
	///
	/// @param  Table name to query        (eg: tableName)
	///
	/// @return  true, if DELETE statement executed succesfuly
	public boolean delete( //
		String tableName // Table name to select from
	) {
		return deleteStatement(tableName, null, null).update();
	}

	///
	/// Helps generate an SQL DELETE request. This function was created to acommedate the various
	/// syntax differances of DELETE across the various SQL vendors (if any).
	///
	/// See : deleteStatement for full docs
	///
	/// @param  Table name to query        (eg: tableName)
	/// @param  Where statement to filter  (eg: col1=?)
	/// @param  Where arguments value      (eg: [value/s])
	///
	/// @return  true, if DELETE statement executed succesfuly
	public boolean delete( //
		String tableName, // Table name to select from
		//
		String whereStatement, // The Columns to apply where clause, this must be sql neutral
		Object[] whereValues // Values that corresponds to the where statement
	) {
		return deleteStatement(tableName, whereStatement, whereValues).update();
	}

	///
	/// Helps generate an SQL DELETE request. This function was created to acommedate the various
	/// syntax differances of DELETE across the various SQL vendors (if any).
	///
	/// The syntax below, is an example of such an DELETE statement for SQLITE.
	///
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.SQL}
	/// DELETE
	/// FROM tableName //table name to select from
	/// WHERE
	///	col1=?       //where clause
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	///
	/// @param  Table name to query        (eg: tableName)
	/// @param  Where statement to filter  (eg: col1=?)
	/// @param  Where arguments value      (eg: [value/s])
	///
	/// @return  A prepared DELETE statement
	///
	public JSqlPreparedStatement deleteStatement( //
		String tableName, // Table name to select from
		//
		String whereStatement, // The Columns to apply where clause, this must be sql neutral
		Object[] whereValues // Values that corresponds to the where statement
	) {
		
		if (tableName.length() > 30) {
			LOGGER.warning(JSqlException.oracleNameSpaceWarning + tableName);
		}
		
		ArrayList<Object> queryArgs = new ArrayList<Object>();
		StringBuilder queryBuilder = new StringBuilder("DELETE ");
		
		// From table names
		queryBuilder.append(" FROM `" + tableName + "`");
		
		// Where clauses
		if (whereStatement != null && (whereStatement = whereStatement.trim()).length() >= 3) {
			queryBuilder.append(" WHERE ");
			queryBuilder.append(whereStatement);
			
			if (whereValues != null) {
				for (int b = 0; b < whereValues.length; ++b) {
					queryArgs.add(whereValues[b]);
				}
			}
		}
		
		// Create the query set
		return new JSqlPreparedStatement(queryBuilder.toString(), queryArgs.toArray(), this);
	}
	
	//-------------------------------------------------------------------------
	//
	// DROP TABLE statement builder 
	//
	// @TODO : While not needed for current JavaCommons use case
	//         due to the extensive GUID usage of dstack. This may
	//         have use cases outside the core JavaCommons stack.
	//
	//-------------------------------------------------------------------------
	
	//-------------------------------------------------------------------------
	//
	// CREATE INDEX statement builder 
	//
	//-------------------------------------------------------------------------
	
	///
	/// Helps generate an SQL CREATE INDEX request. This function was created to acommedate the various
	/// syntax differances of CREATE INDEX across the various SQL vendors (if any).
	///
	/// See : createIndexStatement for full docs
	///
	/// @param  Table name to query            (eg: tableName)
	/// @param  Column names to build an index (eg: col1,col2)
	///
	/// @return  A prepared CREATE INDEX statement
	///
	public boolean createIndex( //
		String tableName, // Table name to select from
		//
		String columnNames // The column name to create the index on
	) {
		return createIndexStatement(tableName, columnNames, null, null).update();
	}

	///
	/// Helps generate an SQL CREATE INDEX request. This function was created to acommedate the various
	/// syntax differances of CREATE INDEX across the various SQL vendors (if any).
	///
	/// See : createIndexStatement for full docs
	///
	/// @param  Table name to query            (eg: tableName)
	/// @param  Column names to build an index (eg: col1,col2)
	/// @param  Index type to build            (eg: UNIQUE)
	///
	/// @return  A prepared CREATE INDEX statement
	///
	public boolean createIndex( //
		String tableName, // Table name to select from
		//
		String columnNames, // The column name to create the index on
		//
		String indexType // The index type if given, can be null
	) {
		return createIndexStatement(tableName, columnNames, indexType, null).update();
	}

	///
	/// Helps generate an SQL CREATE INDEX request. This function was created to acommedate the various
	/// syntax differances of CREATE INDEX across the various SQL vendors (if any).
	///
	/// See : createIndexStatement for full docs
	///
	/// @param  Table name to query            (eg: tableName)
	/// @param  Column names to build an index (eg: col1,col2)
	/// @param  Index type to build            (eg: UNIQUE)
	/// @param  Index suffix to use            (eg: SpecialIndex)
	///
	/// @return  A prepared CREATE INDEX statement
	///
	public boolean createIndex( //
		String tableName, // Table name to select from
		//
		String columnNames, // The column name to create the index on
		//
		String indexType, // The index type if given, can be null
		//
		String indexSuffix // The index name suffix, its auto generated if null
	) {
		return createIndexStatement(tableName, columnNames, indexType, indexSuffix).update();
	}

	///
	/// Helps generate an SQL CREATE INDEX request. This function was created to acommedate the various
	/// syntax differances of CREATE INDEX across the various SQL vendors (if any).
	///
	/// The syntax below, is an example of such an CREATE INDEX statement for SQLITE.
	///
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.SQL}
	/// CREATE (UNIQUE|FULLTEXT) INDEX IF NOT EXISTS TABLENAME_SUFFIX ON TABLENAME ( COLLUMNS )
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	///
	/// @param  Table name to query            (eg: tableName)
	/// @param  Column names to build an index (eg: col1,col2)
	/// @param  Index type to build            (eg: UNIQUE)
	/// @param  Index suffix to use            (eg: SpecialIndex)
	///
	/// @return  A prepared CREATE INDEX statement
	///
	public JSqlPreparedStatement createIndexStatement( //
		String tableName, // Table name to select from
		//
		String columnNames, // The column name to create the index on
		//
		String indexType, // The index type if given, can be null
		//
		String indexSuffix // The index name suffix, its auto generated if null
	) {
		
		if (tableName.length() > 30) {
			LOGGER.warning(JSqlException.oracleNameSpaceWarning + tableName);
		}
		
		ArrayList<Object> queryArgs = new ArrayList<Object>();
		StringBuilder queryBuilder = new StringBuilder("CREATE ");
		
		if (indexType != null && indexType.length() > 0) {
			queryBuilder.append(indexType);
			queryBuilder.append(" ");
		}
		
		queryBuilder.append("INDEX IF NOT EXISTS ");
		
		// Creates a suffix, based on the collumn names
		if (indexSuffix == null || indexSuffix.length() <= 0) {
			indexSuffix = columnNames.replaceAll("/[^A-Za-z0-9]/", ""); //.toUpperCase(Locale.ENGLISH)?
		}
		
		if ((tableName.length() + 1 + indexSuffix.length()) > 30) {
			LOGGER.warning(JSqlException.oracleNameSpaceWarning + tableName + "_" + indexSuffix);
		}
		
		queryBuilder.append("`");
		queryBuilder.append(tableName);
		queryBuilder.append("_");
		queryBuilder.append(indexSuffix);
		queryBuilder.append("` ON `");
		queryBuilder.append(tableName);
		queryBuilder.append("` (");
		queryBuilder.append(columnNames);
		queryBuilder.append(")");
		
		// Create the query set
		return new JSqlPreparedStatement(queryBuilder.toString(), queryArgs.toArray(), this);
	}
	
	/*
	/// Executes the table meta data query, and returns the result object
	public JSqlResult executeQuery_metadata(String table) throws JSqlException {
		JSqlResult res = null;
		try {
			ResultSet rs = null;
			//Try and finally : prevent memory leaks
			try {
				DatabaseMetaData meta = sqlConn.getMetaData();
				rs = meta.getColumns(null, null, table, null);
				res = new JSqlResult(null, rs);
				
				//let JSqlResult "close" it
				rs = null;
				return res;
			} finally {
				if (rs != null) {
					rs.close();
				}
			}
		} catch (Exception e) {
			throw new JSqlException("executeQuery_metadata exception", e);
		}
	}
	
	/// Executes the table meta data query, and returns the result object
	public Map<String, String> getMetaData(String sql) throws JSqlException {
		Map<String, String> metaData = null;
		ResultSet rs = null;
		//Try and finally : prevent memory leaks
		try {
			try {
				Statement st = sqlConn.createStatement();
				rs = st.executeQuery(sql);
				ResultSetMetaData rsMetaData = rs.getMetaData();
				int numberOfColumns = rsMetaData.getColumnCount();
				for (int i = 1; i <= numberOfColumns; i++) {
					if (metaData == null) {
						metaData = new HashMap<String, String>();
					}
					metaData.put(rsMetaData.getColumnName(i), rsMetaData.getColumnTypeName(i));
				}
			} finally {
				if (rs != null) {
					rs.close();
				}
				rs = null;
			}
		} catch (Exception e) {
			throw new JSqlException("executeQuery_metadata exception", e);
		}
		return metaData;
	}
	*/
}