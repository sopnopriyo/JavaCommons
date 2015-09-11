package picoded.JSql.db;

import java.lang.String;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.lang.RuntimeException;
import java.util.regex.Pattern;
import java.util.regex.Matcher; //import java.util.*;

import java.io.StringWriter;
import java.util.logging.*;
import java.io.PrintWriter;

import java.util.concurrent.ExecutionException;

import picoded.JSql.JSqlType;
import picoded.JSql.JSqlResult;
import picoded.JSql.JSqlException;

import picoded.JSql.JSql;
import picoded.JSql.db.BaseInterface;

/// Pure SQLite implentation of JSql
public class JSql_Sqlite extends JSql {
	
	/// Internal self used logger
	private static Logger logger = Logger.getLogger(JSql_Sqlite.class.getName());
	
	/// Runs with in memory SQLite
	public JSql_Sqlite() {
		this(":memory:");
	}
	
	/// Runs JSql with the JDBC sqlite engine
	public JSql_Sqlite(String sqliteLoc) {
		// store database connection properties
		setConnectionProperties(sqliteLoc, null, null, null, null);
		// call internal method to create the connection
		setupConnection();
	}
	
	/// Internal common reuse constructor
	private void setupConnection() {
		sqlType = JSqlType.sqlite;
		
		try {
			Class.forName("org.sqlite.JDBC");
			sqlConn = java.sql.DriverManager.getConnection("jdbc:sqlite:" + (String) connectionProps.get("dbUrl"));
		} catch (Exception e) {
			throw new RuntimeException("Failed to load sqlite connection: ", e);
		}
	}
	
	/// As this is the base class varient, this funciton isnt suported
	public void recreate(boolean force) {
		if (force) {
			dispose();
		}
		// call internal method to create the connection
		setupConnection();
	}
	
	/// Internal parser that converts some of the common sql statements to sqlite
	public String genericSqlParser(String inString) {
		final String truncateTable = "TRUNCATE TABLE";
		final String deleteFrom = "DELETE FROM";
		
		inString = inString.toUpperCase();
		inString = inString.trim()
			.replaceAll("(\\s){1}", " ")
			.replaceAll("\\s+", " ")
			.replaceAll("(?i)VARCHAR\\(MAX\\)", "VARCHAR")
			.replaceAll("(?i)BIGINT", "INTEGER");
		//System.out.println( inString );
		
		if (inString.startsWith(truncateTable)) {
		    inString = inString.replaceAll(truncateTable, deleteFrom);
		}
		return inString;
	}
	
	/// Executes the argumented query, and returns the result object *without* 
	/// fetching the result data from the database. (not fetching may not apply to all implementations)
	/// 
	/// **Note:** Only queries starting with 'SELECT' will produce a JSqlResult object that has fetchable results
	public JSqlResult executeQuery(String qString, Object... values) throws JSqlException {
		return executeQuery_raw(genericSqlParser(qString), values);
	}
	
	/// Executes the argumented query, and immediately fetches the result from
	/// the database into the result set.
	///
	/// **Note:** Only queries starting with 'SELECT' will produce a JSqlResult object that has fetchable results
	public JSqlResult query(String qString, Object... values) throws JSqlException {
		return query_raw(genericSqlParser(qString), values);
	}
	
	/// Executes and dispose the sqliteResult object.
	///
	/// Returns false if no result is given by the execution call, else true on success
	public boolean execute(String qString, Object... values) throws JSqlException {
		return execute_raw(genericSqlParser(qString), values);
	}
	
}