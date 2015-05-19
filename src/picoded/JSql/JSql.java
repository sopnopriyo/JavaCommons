package picoded.JSql;

import java.lang.String;
import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.lang.RuntimeException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.*;

import java.io.StringWriter;
import java.util.logging.*;
import java.io.PrintWriter;

import java.util.concurrent.ExecutionException;

import picoded.JSql.JSqlType;
import picoded.JSql.JSqlResult;
import picoded.JSql.JSqlException;

import picoded.JSql.db.BaseInterface;

/// Database intreface base class.
public class JSql extends BaseInterface {
	
	// Database connection settings
	//-------------------------------------------------------------------------

	/// database connection
	protected Connection sqlConn = null;
	
	/// Internal refrence of the current sqlType the system is running as
	/// @TODO: make a getter function, so this will be read-only?
	public JSqlType sqlType = JSqlType.invalid;
	
	/// Internal self used logger
	private static Logger logger = Logger.getLogger(JSql.class.getName());
	
	// Database connection caching (used for recreate
	//-------------------------------------------------------------------------

	/// database connection properties
	private Map<String, Object> connectionProps = null;
	
	/// store the database connection parameters for recreating the connection
	public void setConnectionProperties(String dbUrl, String dbName, String dbUser, String dbPass, Properties connProps) {
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
	
	/// SQLite static constructor, returns picoded.JSql.JSql_Sqlite
	public static JSql sqlite() {
		return new picoded.JSql.db.JSql_Sqlite();
	}
	
	/// SQLite static constructor, returns picoded.JSql.JSql_Sqlite
	public static JSql sqlite(String sqliteLoc) {
		return new picoded.JSql.db.JSql_Sqlite(sqliteLoc);
	}
	
	/// MySql static constructor, returns picoded.JSql.JSql_Mysql
	public static JSql mysql(String dbServerAddress, String dbName, String dbUser, String dbPass) {
		return new picoded.JSql.db.JSql_Mysql(dbServerAddress, dbName, dbUser, dbPass);
	}
	
	/// MySql static constructor, returns picoded.JSql.JSql_Mysql
	public static JSql mysql(String connectionUrl, Properties connectionProps) {
		return new picoded.JSql.db.JSql_Mysql(connectionUrl, connectionProps);
	}
	
	/// Mssql static constructor, returns picoded.JSql.JSql_Mssql
	public static JSql mssql(String dbUrl, String dbName, String dbUser, String dbPass) {
		return new picoded.JSql.db.JSql_Mssql(dbUrl, dbName, dbUser, dbPass);
	}
	
	/// Oracle static constructor, returns picoded.JSql.db.JSql_Oracle
	public static JSql oracle(String oraclePath, String dbUser, String dbPass) {
		return new picoded.JSql.db.JSql_Oracle(oraclePath, dbUser, dbPass);
	}
	
	/// As this is the base class varient, this funciton isnt suported
	public void recreate(boolean force) {
		if (force) {
			dispose();
		}
		if (sqlType == JSqlType.sqlite) {
			if (connectionProps.get("dbUrl") == null) {
				new picoded.JSql.db.JSql_Sqlite();
			} else {
				new picoded.JSql.db.JSql_Sqlite((String) connectionProps.get("dbUrl"));
			}
		} else if (sqlType == JSqlType.mysql) {
			if (connectionProps.get("connectionProps") == null) {
				new picoded.JSql.db.JSql_Mysql((String) connectionProps.get("dbUrl"), (String) connectionProps
					.get("dbName"), (String) connectionProps.get("dbUser"), (String) connectionProps.get("dbPass"));
			} else {
				new picoded.JSql.db.JSql_Mysql((String) connectionProps.get("dbUrl"), (Properties) connectionProps
					.get("connectionProps"));
			}
		} else if (sqlType == JSqlType.mssql) {
			new picoded.JSql.db.JSql_Mssql((String) connectionProps.get("dbUrl"), (String) connectionProps.get("dbName"),
				(String) connectionProps.get("dbUser"), (String) connectionProps.get("dbPass"));
		} else if (sqlType == JSqlType.oracle) {
			new picoded.JSql.db.JSql_Oracle((String) connectionProps.get("dbUrl"), (String) connectionProps.get("dbUser"),
				(String) connectionProps.get("dbPass"));
		}
	}
	
	/// [private] Helper function, used to prepare the sql statment in multiple situations
	protected PreparedStatement prepareSqlStatment(String qString, Object... values) throws JSqlException {
		int pt = 0;
		final Object parts[] = (values != null) ? values : (new Object[] {});
		
		Object argObj;
		PreparedStatement ps;
		
		try {
			ps = sqlConn.prepareStatement(qString);
			
			for (pt = 0; pt < parts.length; ++pt) {
				argObj = parts[pt];
				if (argObj == null) {
					ps.setNull(pt + 1, 0);
				} else if (String.class.isInstance(argObj)) {
					ps.setString(pt + 1, (String) argObj);
				} else if (Integer.class.isInstance(argObj)) {
					ps.setInt(pt + 1, (Integer) argObj);
				} else if (Long.class.isInstance(argObj)) {
					ps.setLong(pt + 1, (Long) argObj);
				} else if (Double.class.isInstance(argObj)) {
					ps.setDouble(pt + 1, (Double) argObj);
				} else {
					String argClassName = argObj.getClass().getName();
					throw new JSqlException("Unknown argument type (" + pt + ") : " + (argClassName));
				}
			}
		} catch (Exception e) {
			throw new JSqlException("Invalid statement argument/parameter (" + pt + ")", e);
		}
		return ps;
	}
	
	/// Executes the argumented query, and returns the result object *without*
	/// fetching the result data from the database. This is raw execution.
	///
	/// **Note:** Only queries starting with 'SELECT' will produce a JSqlResult object that has fetchable results
	public JSqlResult executeQuery_raw(String qString, Object... values) throws JSqlException {
		JSqlResult res = null;
		final String query = qString;
		final Object parts[] = values;
		try {
			PreparedStatement ps = prepareSqlStatment(qString, values);
			ResultSet rs = null;
			//Try and finally : prevent memory leaks
			try {
				//is a select statment
				if (qString.trim().toUpperCase().substring(0, 6).equals("SELECT")) {
					rs = ps.executeQuery();
					res = new JSqlResult(ps, rs);
					
					//let JSqlResult "close" it
					ps = null;
					rs = null;
					return res;
				} else {
					int r = ps.executeUpdate();
					if (r != -1) {
						return new JSqlResult(); //returns a blank JSqlResult, for consistency
					} else {
						return null;
					}
				}
			} finally {
				if (rs != null) {
					rs.close();
				}
				if (ps != null) {
					ps.close();
				}
			}
		} catch (Exception e) {
			throw new JSqlException("executeQuery_raw exception", e);
		}
	}
	
	/// Executes the argumented query, and immediately fetches the result from
	/// the database into the result set. This is raw execution.
	///
	/// **Note:** Only queries starting with 'SELECT' will produce a JSqlResult object that has fetchable results
	public JSqlResult query_raw(String qString, Object... values) throws JSqlException {
		JSqlResult result = executeQuery_raw(qString, values);
		if (result != null) {
			result.fetchAllRows();
		}
		return result;
	}
	
	/// Executes and dispose the sqliteResult object. Similar to executeQuery
	/// Returns false if no result object is given by the execution call. This is raw execution.
	public boolean execute_raw(String qString, Object... values) throws JSqlException {
		try {
			PreparedStatement ps = prepareSqlStatment(qString, values);
			ResultSet rs = null;
			try {
				//is a select statment
				if (qString.trim().toUpperCase().substring(0, 6).equals("SELECT")) {
					rs = ps.executeQuery();
					if (rs != null) {
						return true;
					}
				} else {
					int r = ps.executeUpdate();
					if (r != -1) {
						return true;
					}
				}
			} finally {
				if (rs != null) {
					rs.close();
				}
				if (ps != null) {
					ps.close();
				}
			}
		} catch (Exception e) {
			throw new JSqlException("execute_raw exception", e);
		}
		return false;
	}
	
	/// Throws an exception, as this functionality isnt supported in the base class
	public JSqlResult executeQuery(String qString, Object... values) throws JSqlException {
		throw new JSqlException(JSqlException.invalidDatabaseImplementationException);
	}
	
	/// Throws an exception, as this functionality isnt supported in the base class
	public JSqlResult query(String qString, Object... values) throws JSqlException {
		throw new JSqlException(JSqlException.invalidDatabaseImplementationException);
	}
	
	/// Throws an exception, as this functionality isnt supported in the base class
	public boolean execute(String qString, Object... values) throws JSqlException {
		throw new JSqlException(JSqlException.invalidDatabaseImplementationException);
	}
	
	/// Returns true, if dispose() function was called prior
	public boolean isDisposed() {
		return (sqlConn == null);
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
	
	//--------------------------------------------------------------------------
	// Utility helper functions used to prepare common complex SQL quries
	//--------------------------------------------------------------------------
	
	// Merge the 2 arrays together
	public Object[] joinArguments(Object[] arr1, Object[] arr2) {
		return org.apache.commons.lang3.ArrayUtils.addAll(arr1, arr2);
	}
	
	/// Sets the auto commit level
	public void setAutoCommit(boolean autoCommit) throws JSqlException {
		try {
			sqlConn.setAutoCommit(autoCommit);
		} catch (Exception e) {
			throw new JSqlException(e);
		}
	}
	
	/// Gets the current auto commit setting
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
	
	// Performs a roll back, this is currently useless without setting checkpoints
	//public void rollback() throws JSqlException {
	//	try {
	//		sqlConn.rollback();
	//	} catch (Exception e) {
	//		throw new JSqlException(e);
	//	}
	//}
	
	//--------------------------------------------------------------------------
	// Utility helper functions used to prepare common complex SQL quries
	//--------------------------------------------------------------------------
	
	///
	/// Helps generate an SQL SELECT request. This function was created to acommedate the various
	/// syntax differances of SELECT across the various SQL vendors (if any).
	///
	public JSqlQuerySet selectQuerySet( //
		String tableName, // Table name to select from
		String selectStatement, // The Columns to select, null means all
		
		String whereStatement, // The Columns to apply where clause, this must be sql neutral
		Object[] whereValues // Values that corresponds to the where statement
	) {
		return selectQuerySet(tableName, selectStatement, whereStatement, whereValues, null, 0, 0);
	}
	
	///
	/// Helps generate an SQL SELECT request. This function was created to acommedate the various
	/// syntax differances of SELECT across the various SQL vendors (if any).
	///
	/// Note that care should be taken to prevent SQL injection via the given statment strings.
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
	public JSqlQuerySet selectQuerySet( //
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
		
		if (tableName.length() > 30) {
			logger.warning(JSqlException.oracleNameSpaceWarning + tableName);
		}
		
		ArrayList<Object> queryArgs = new ArrayList<Object>();
		StringBuilder queryBuilder = new StringBuilder("SELECT ");
		
		// Select collumns
		if (selectStatement == null || (selectStatement = selectStatement.trim()).length() <= 0) {
			queryBuilder.append("*");
		} else {
			queryBuilder.append(selectStatement);
		}
		
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
		
		// Order By clause
		if (orderStatement != null && (orderStatement = orderStatement.trim()).length() > 3) {
			queryBuilder.append(" ORDER BY ");
			queryBuilder.append(orderStatement);
		}
		
		// Limit and offset clause
		if (limit > 0) {
			queryBuilder.append(" LIMIT " + limit);
			
			if (offset > 0) {
				queryBuilder.append(" OFFSET " + offset);
			}
		}
		
		// Create the query set
		return new JSqlQuerySet(queryBuilder.toString(), queryArgs.toArray(), this);
	}
	
	///
	/// Helps generate an SQL UPSERT request. This function was created to acommedate the various
	/// syntax differances of UPSERT across the various SQL vendors.
	///
	/// Note that care should be taken to prevent SQL injection via the given statment strings.
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
	public JSqlQuerySet upsertQuerySet( //
		String tableName, // Table name to upsert on
		//
		String[] uniqueColumns, // The unique column names
		Object[] uniqueValues, // The row unique identifier values
		//
		String[] insertColumns, // Columns names to update
		Object[] insertValues, // Values to update
		//
		String[] defaultColumns, // Columns names to apply default value, if not exists
		Object[] defaultValues, // Values to insert, that is not updated. Note that this is ignored if pre-existing values exists
		//
		// Various column names where its existing value needs to be maintained (if any),
		// this is important as some SQL implementation will fallback to default table values, if not properly handled
		String[] miscColumns //
	) throws JSqlException {
		
		if (tableName.length() > 30) {
			logger.warning(JSqlException.oracleNameSpaceWarning + tableName);
		}
		
		/// Checks that unique collumn and values length to be aligned
		if (uniqueColumns == null || uniqueValues == null || uniqueColumns.length != uniqueValues.length) {
			throw new JSqlException("Upsert query requires unique column and values to be equal length");
		}
		
		/// Preparing inner default select, this will be used repeatingly for COALESCE, DEFAULT and MISC values
		ArrayList<Object> innerSelectArgs = new ArrayList<Object>();
		StringBuilder innerSelectSB = new StringBuilder(" FROM ");
		innerSelectSB.append("`" + tableName + "`");
		innerSelectSB.append(" WHERE ");
		for (int a = 0; a < uniqueColumns.length; ++a) {
			if (a > 0) {
				innerSelectSB.append(" AND ");
			}
			innerSelectSB.append(uniqueColumns[a] + " = ?");
			innerSelectArgs.add(uniqueValues[a]);
		}
		innerSelectSB.append(")");
		
		String innerSelectPrefix = "(SELECT ";
		String innerSelectSuffix = innerSelectSB.toString();
		
		/// Building the query for INSERT OR REPLACE
		StringBuilder queryBuilder = new StringBuilder("INSERT OR REPLACE INTO `" + tableName + "` (");
		ArrayList<Object> queryArgs = new ArrayList<Object>();
		
		/// Building the query for both sides of '(...columns...) VALUE (...vars...)' clauses in upsert
		/// Note that the final trailing ", " seperator will be removed prior to final query conversion
		StringBuilder columnNames = new StringBuilder();
		StringBuilder columnValues = new StringBuilder();
		String columnSeperator = ", ";
		
		/// Setting up unique values
		for (int a = 0; a < uniqueColumns.length; ++a) {
			columnNames.append(uniqueColumns[a]);
			columnNames.append(columnSeperator);
			//
			columnValues.append("?");
			columnValues.append(columnSeperator);
			//
			queryArgs.add(uniqueValues[a]);
		}
		
		/// Inserting updated values
		if (insertColumns != null) {
			for (int a = 0; a < insertColumns.length; ++a) {
				columnNames.append(insertColumns[a]);
				columnNames.append(columnSeperator);
				//
				columnValues.append("?");
				columnValues.append(columnSeperator);
				//
				queryArgs.add((insertValues != null && insertValues.length > a) ? insertValues[a] : null);
			}
		}
		
		/// Handling default values
		if (defaultColumns != null) {
			for (int a = 0; a < defaultColumns.length; ++a) {
				columnNames.append(defaultColumns[a]);
				columnNames.append(columnSeperator);
				//
				columnValues.append("COALESCE(");
				//-
				columnValues.append(innerSelectPrefix);
				columnValues.append(defaultColumns[a]);
				columnValues.append(innerSelectSuffix);
				queryArgs.addAll(innerSelectArgs);
				//-
				columnValues.append(", ?)");
				columnValues.append(columnSeperator);
				//
				queryArgs.add((defaultValues != null && defaultValues.length > a) ? defaultValues[a] : null);
			}
		}
		
		/// Handling Misc values
		if (miscColumns != null) {
			for (int a = 0; a < miscColumns.length; ++a) {
				columnNames.append(miscColumns[a]);
				columnNames.append(columnSeperator);
				//-
				columnValues.append(innerSelectPrefix);
				columnValues.append(miscColumns[a]);
				columnValues.append(innerSelectSuffix);
				queryArgs.addAll(innerSelectArgs);
				//-
				columnValues.append(columnSeperator);
			}
		}
		
		/// Building the final query
		queryBuilder.append(columnNames.substring(0, columnNames.length() - columnSeperator.length()));
		queryBuilder.append(") VALUES (");
		queryBuilder.append(columnValues.substring(0, columnValues.length() - columnSeperator.length()));
		queryBuilder.append(")");
		
		return new JSqlQuerySet(queryBuilder.toString(), queryArgs.toArray(), this);
	}
	
	// Helper varient, without default or misc fields
	public JSqlQuerySet upsertQuerySet( //
		String tableName, // Table name to upsert on
		//
		String[] uniqueColumns, // The unique column names
		Object[] uniqueValues, // The row unique identifier values
		//
		String[] insertColumns, // Columns names to update
		Object[] insertValues // Values to update
	) throws JSqlException {
		return upsertQuerySet(tableName, uniqueColumns, uniqueValues, insertColumns, insertValues, null, null, null);
	}
	
	///
	/// Helps generate an SQL CREATE TABLE IF NOT EXISTS request. This function was created to acommedate the various
	/// syntax differances of CREATE TABLE IF NOT EXISTS across the various SQL vendors (if any).
	///
	/// Note that care should be taken to prevent SQL injection via the given statment strings.
	///
	/// The syntax below, is an example of such an CREATE TABLE IF NOT EXISTS statement for SQLITE.
	///
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.SQL}
	/// CREATE TABLE IF NOT EXISTS TABLENAME ( COLLUMNS_NAME TYPE, ... )
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	///
	public JSqlQuerySet createTableQuerySet(String tableName, // Table name to create
		//
		String[] columnName, // The column names
		String[] columnDefine // The column types
	) {
		if (columnName == null || columnDefine == null || columnDefine.length != columnName.length) {
			throw new IllegalArgumentException("Invalid columnName/Type provided: " + columnName + " : " + columnDefine);
		}
		
		StringBuilder queryBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS `");
		queryBuilder.append(tableName);
		queryBuilder.append("` ( ");
		
		for (int a = 0; a < columnName.length; ++a) {
			if (a > 0) {
				queryBuilder.append(", ");
			}
			queryBuilder.append(columnName[a]);
			queryBuilder.append(" ");
			queryBuilder.append(columnDefine[a]);
		}
		queryBuilder.append(" )");
		
		// Create the query set
		return new JSqlQuerySet(queryBuilder.toString(), null, this);
	}
	
	///
	/// Helps generate an SQL SELECT request. This function was created to acommedate the various
	/// syntax differances of SELECT across the various SQL vendors (if any).
	///
	/// Note that care should be taken to prevent SQL injection via the given statment strings.
	///
	/// The syntax below, is an example of such an SELECT statement for SQLITE.
	///
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.SQL}
	/// CREATE (UNIQUE|FULLTEXT) INDEX IF NOT EXISTS TABLENAME_SUFFIX ON TABLENAME ( COLLUMNS )
	/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	///
	public JSqlQuerySet createTableIndexQuerySet( //
		String tableName, // Table name to select from
		//
		String columnNames, // The column name to create the index on
		//
		String indexType, // The index type if given, can be null
		//
		String indexSuffix // The index name suffix, its auto generated if null
	) {
		
		if (tableName.length() > 30) {
			logger.warning(JSqlException.oracleNameSpaceWarning + tableName);
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
			indexSuffix = columnNames.replaceAll("/[^A-Za-z0-9]/", ""); //.toUpperCase()?
		}
		
		if ((tableName.length() + 1 + indexSuffix.length()) > 30) {
			logger.warning(JSqlException.oracleNameSpaceWarning + tableName + "_" + indexSuffix);
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
		return new JSqlQuerySet(queryBuilder.toString(), queryArgs.toArray(), this);
	}
	
	/// Helper varient, where indexSuffix is defaulted to auto generate (null)
	public JSqlQuerySet createTableIndexQuerySet( //
		String tableName, // Table name to select from
		String columnNames, // The column name to create the index on
		String indexType // The index type if given, can be null
	) {
		return createTableIndexQuerySet(tableName, columnNames, indexType, null);
	}
	
	/// Helper varient, where idnexType and indexSuffix is defaulted(null)
	public JSqlQuerySet createTableIndexQuerySet( //
		String tableName, // Table name to select from
		String columnNames // The column name to create the index on
	) {
		return createTableIndexQuerySet(tableName, columnNames, null, null);
	}
	
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
}
