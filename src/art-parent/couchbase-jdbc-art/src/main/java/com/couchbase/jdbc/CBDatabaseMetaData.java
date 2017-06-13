
/*
 * //  Copyright (c) 2015 Couchbase, Inc.
 * //  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * //  except in compliance with the License. You may obtain a copy of the License at
 * //    http://www.apache.org/licenses/LICENSE-2.0
 * //  Unless required by applicable law or agreed to in writing, software distributed under the
 * //  License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * //  either express or implied. See the License for the specific language governing permissions
 * //  and limitations under the License.
 */

package com.couchbase.jdbc;

import com.couchbase.jdbc.core.CouchMetrics;
import com.couchbase.jdbc.core.CouchResponse;
import com.couchbase.jdbc.util.StringUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by davec on 2015-02-20.
 */
public class CBDatabaseMetaData implements DatabaseMetaData
{
    final CBConnection connection;

    public CBDatabaseMetaData(CBConnection connection)
    {
        this.connection=connection;
    }
    /**
     * Retrieves whether the current user can call all the procedures
     * returned by the method <code>getProcedures</code>.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean allProceduresAreCallable() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves whether the current user can use all the tables returned
     * by the method <code>getTables</code> in a <code>SELECT</code>
     * statement.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean allTablesAreSelectable() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves the URL for this DBMS.
     *
     * @return the URL for this DBMS or <code>null</code> if it cannot be
     * generated
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public String getURL() throws SQLException
    {
        return connection.getURL();
    }

    /**
     * Retrieves the user name as known to this database.
     *
     * @return the database user name
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public String getUserName() throws SQLException
    {
        return connection.getUserName();
    }

    /**
     * Retrieves whether this database is in read-only mode.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean isReadOnly() throws SQLException
    {
        return connection.isReadOnly();
    }

    /**
     * Retrieves whether <code>NULL</code> values are sorted high.
     * Sorted high means that <code>NULL</code> values
     * sort higher than any other value in a domain.  In an ascending order,
     * if this method returns <code>true</code>,  <code>NULL</code> values
     * will appear at the end. By contrast, the method
     * <code>nullsAreSortedAtEnd</code> indicates whether <code>NULL</code> values
     * are sorted at the end regardless of sort order.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean nullsAreSortedHigh() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether <code>NULL</code> values are sorted low.
     * Sorted low means that <code>NULL</code> values
     * sort lower than any other value in a domain.  In an ascending order,
     * if this method returns <code>true</code>,  <code>NULL</code> values
     * will appear at the beginning. By contrast, the method
     * <code>nullsAreSortedAtStart</code> indicates whether <code>NULL</code> values
     * are sorted at the beginning regardless of sort order.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean nullsAreSortedLow() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves whether <code>NULL</code> values are sorted at the start regardless
     * of sort order.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean nullsAreSortedAtStart() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether <code>NULL</code> values are sorted at the end regardless of
     * sort order.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean nullsAreSortedAtEnd() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves the name of this database product.
     *
     * @return database product name
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public String getDatabaseProductName() throws SQLException
    {
        return "Couchbase";
    }

    /**
     * Retrieves the version number of this database product.
     *
     * @return database version number
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public String getDatabaseProductVersion() throws SQLException
    {

        ResultSet rs = connection.createStatement().executeQuery("select version()");
        if(rs.next())
        {
            return rs.getString(1);
        }
        return null;
    }

    /**
     * Retrieves the name of this JDBC driver.
     *
     * @return JDBC driver name
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public String getDriverName() throws SQLException
    {
        return CBDriver.DRIVER_NAME;
    }

    /**
     * Retrieves the version number of this JDBC driver as a <code>String</code>.
     *
     * @return JDBC driver version
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public String getDriverVersion() throws SQLException
    {
        return ""+CBDriver.MAJOR_VERSION+'.'+CBDriver.MINOR_VERSION;
    }

    /**
     * Retrieves this JDBC driver's major version number.
     *
     * @return JDBC driver major version
     */
    @Override
    public int getDriverMajorVersion()
    {
        return CBDriver.MAJOR_VERSION;
    }

    /**
     * Retrieves this JDBC driver's minor version number.
     *
     * @return JDBC driver minor version number
     */
    @Override
    public int getDriverMinorVersion()
    {
        return CBDriver.MINOR_VERSION;
    }

    /**
     * Retrieves whether this database stores tables in a local file.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean usesLocalFiles() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether this database uses a file for each table.
     *
     * @return <code>true</code> if this database uses a local file for each table;
     * <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean usesLocalFilePerTable() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether this database treats mixed case unquoted SQL identifiers as
     * case sensitive and as a result stores them in mixed case.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsMixedCaseIdentifiers() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves whether this database treats mixed case unquoted SQL identifiers as
     * case insensitive and stores them in upper case.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean storesUpperCaseIdentifiers() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether this database treats mixed case unquoted SQL identifiers as
     * case insensitive and stores them in lower case.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean storesLowerCaseIdentifiers() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether this database treats mixed case unquoted SQL identifiers as
     * case insensitive and stores them in mixed case.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean storesMixedCaseIdentifiers() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether this database treats mixed case quoted SQL identifiers as
     * case sensitive and as a result stores them in mixed case.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves whether this database treats mixed case quoted SQL identifiers as
     * case insensitive and stores them in upper case.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether this database treats mixed case quoted SQL identifiers as
     * case insensitive and stores them in lower case.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether this database treats mixed case quoted SQL identifiers as
     * case insensitive and stores them in mixed case.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves the string used to quote SQL identifiers.
     * This method returns a space " " if identifier quoting is not supported.
     *
     * @return the quoting string or a space if quoting is not supported
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public String getIdentifierQuoteString() throws SQLException
    {
        return "`";
    }

    /**
     * Retrieves a comma-separated list of all of this database's SQL keywords
     * that are NOT also SQL:2003 keywords.
     *
     * @return the list of this database's keywords that are not also
     * SQL:2003 keywords
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public String getSQLKeywords() throws SQLException
    {
        return null;
    }

    /**
     * Retrieves a comma-separated list of math functions available with
     * this database.  These are the Open /Open CLI math function names used in
     * the JDBC function escape clause.
     *
     * @return the list of math functions supported by this database
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public String getNumericFunctions() throws SQLException
    {
        return "add,div,mod,mult,neg,sub,abs,acos,asin,atan,atan2,ceil,cos,deg,degrees,e,exp,ln,log,floor,inf,nan,neginf,pi,posinf,power,rad,radians,random,round,sign,sin,sqrt,tan,trunc";
    }

    /**
     * Retrieves a comma-separated list of string functions available with
     * this database.  These are the  Open Group CLI string function names used
     * in the JDBC function escape clause.
     *
     * @return the list of string functions supported by this database
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public String getStringFunctions() throws SQLException
    {
        return "contains,initcap,length,lower,ltrim,position,pos,regex_contains,regex_like,regex_position,regex_pos,regex_replace,repeat,replace,rtrim,split,substr,title,trim,upper";
    }

    /**
     * Retrieves a comma-separated list of system functions available with
     * this database.  These are the  Open Group CLI system function names used
     * in the JDBC function escape clause.
     *
     * @return a list of system functions supported by this database
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public String getSystemFunctions() throws SQLException
    {
        return "";
    }

    /**
     * Retrieves a comma-separated list of the time and date functions available
     * with this database.
     *
     * @return the list of time and date functions supported by this database
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public String getTimeDateFunctions() throws SQLException
    {
        return "clock_millis,clock_str,date_add_millis,date_add_str,date_diff_millis,date_diff_str,date_part_millis,date_part_str,date_trunc_millis,date_trunc_str,millis,millis_to_str,millis_to_utc,millis_to_zone_name,now_millis,now_str,str_to_millis,str_to_utc,str_to_zone_name";
    }

    /**
     * Retrieves the string that can be used to escape wildcard characters.
     * This is the string that can be used to escape '_' or '%' in
     * the catalog search parameters that are a pattern (and therefore use one
     * of the wildcard characters).
     * 
     * <P>The '_' character represents any single character;
     * the '%' character represents any sequence of zero or
     * more characters.
     *
     * @return the string used to escape wildcard characters
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public String getSearchStringEscape() throws SQLException
    {
        return "\\";
    }

    /**
     * Retrieves all the "extra" characters that can be used in unquoted
     * identifier names (those beyond a-z, A-Z, 0-9 and _).
     *
     * @return the string containing the extra characters
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public String getExtraNameCharacters() throws SQLException
    {
        return "";
    }

    /**
     * Retrieves whether this database supports <code>ALTER TABLE</code>
     * with add column.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsAlterTableWithAddColumn() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether this database supports <code>ALTER TABLE</code>
     * with drop column.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsAlterTableWithDropColumn() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether this database supports column aliasing.
     * 
     * <P>If so, the SQL AS clause can be used to provide names for
     * computed columns or to provide alias names for columns as
     * required.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsColumnAliasing() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves whether this database supports concatenations between
     * <code>NULL</code> and non-<code>NULL</code> values being
     * <code>NULL</code>.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean nullPlusNonNullIsNull() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether this database supports the JDBC scalar function
     * <code>CONVERT</code> for the conversion of one JDBC type to another.
     * The JDBC types are the generic SQL data types defined
     * in <code>java.sql.Types</code>.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsConvert() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves whether this database supports the JDBC scalar function
     * <code>CONVERT</code> for conversions between the JDBC types <i>fromType</i>
     * and <i>toType</i>.  The JDBC types are the generic SQL data types defined
     * in <code>java.sql.Types</code>.
     *
     * @param fromType the type to convert from; one of the type codes from
     *                 the class <code>java.sql.Types</code>
     * @param toType   the type to convert to; one of the type codes from
     *                 the class <code>java.sql.Types</code>
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     * @see Types
     */
    @Override
    public boolean supportsConvert(int fromType, int toType) throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether this database supports table correlation names.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsTableCorrelationNames() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves whether, when table correlation names are supported, they
     * are restricted to being different from the names of the tables.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsDifferentTableCorrelationNames() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves whether this database supports expressions in
     * <code>ORDER BY</code> lists.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsExpressionsInOrderBy() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves whether this database supports using a column that is
     * not in the <code>SELECT</code> statement in an
     * <code>ORDER BY</code> clause.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsOrderByUnrelated() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether this database supports some form of
     * <code>GROUP BY</code> clause.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsGroupBy() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves whether this database supports using a column that is
     * not in the <code>SELECT</code> statement in a
     * <code>GROUP BY</code> clause.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsGroupByUnrelated() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves whether this database supports using columns not included in
     * the <code>SELECT</code> statement in a <code>GROUP BY</code> clause
     * provided that all of the columns in the <code>SELECT</code> statement
     * are included in the <code>GROUP BY</code> clause.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsGroupByBeyondSelect() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves whether this database supports specifying a
     * <code>LIKE</code> escape clause.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsLikeEscapeClause() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether this database supports getting multiple
     * <code>ResultSet</code> objects from a single call to the
     * method <code>execute</code>.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsMultipleResultSets() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether this database allows having multiple
     * transactions open at once (on different connections).
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsMultipleTransactions() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves whether columns in this database may be defined as non-nullable.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsNonNullableColumns() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether this database supports the ODBC Minimum SQL grammar.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsMinimumSQLGrammar() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves whether this database supports the ODBC Core SQL grammar.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsCoreSQLGrammar() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves whether this database supports the ODBC Extended SQL grammar.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsExtendedSQLGrammar() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether this database supports the ANSI92 entry level SQL
     * grammar.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsANSI92EntryLevelSQL() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves whether this database supports the ANSI92 intermediate SQL grammar supported.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsANSI92IntermediateSQL() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether this database supports the ANSI92 full SQL grammar supported.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsANSI92FullSQL() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether this database supports the SQL Integrity
     * Enhancement Facility.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsIntegrityEnhancementFacility() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether this database supports some form of outer join.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsOuterJoins() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves whether this database supports full nested outer joins.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsFullOuterJoins() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether this database provides limited support for outer
     * joins.  (This will be <code>true</code> if the method
     * <code>supportsFullOuterJoins</code> returns <code>true</code>).
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsLimitedOuterJoins() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves the database vendor's preferred term for "schema".
     *
     * @return the vendor term for "schema"
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public String getSchemaTerm() throws SQLException
    {
        return "NAMESPACE";
    }

    /**
     * Retrieves the database vendor's preferred term for "procedure".
     *
     * @return the vendor term for "procedure"
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public String getProcedureTerm() throws SQLException
    {
        return "procedure";
    }

    /**
     * Retrieves the database vendor's preferred term for "catalog".
     *
     * @return the vendor term for "catalog"
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public String getCatalogTerm() throws SQLException
    {
        return "namespace";
    }

    /**
     * Retrieves whether a catalog appears at the start of a fully qualified
     * table name.  If not, the catalog appears at the end.
     *
     * @return <code>true</code> if the catalog name appears at the beginning
     * of a fully qualified table name; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean isCatalogAtStart() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves the <code>String</code> that this database uses as the
     * separator between a catalog and table name.
     *
     * @return the separator string
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public String getCatalogSeparator() throws SQLException
    {
        return ":";
    }

    /**
     * Retrieves whether a schema name can be used in a data manipulation statement.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsSchemasInDataManipulation() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves whether a schema name can be used in a procedure call statement.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsSchemasInProcedureCalls() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether a schema name can be used in a table definition statement.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsSchemasInTableDefinitions() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether a schema name can be used in an index definition statement.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsSchemasInIndexDefinitions() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves whether a schema name can be used in a privilege definition statement.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether a catalog name can be used in a data manipulation statement.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsCatalogsInDataManipulation() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves whether a catalog name can be used in a procedure call statement.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsCatalogsInProcedureCalls() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether a catalog name can be used in a table definition statement.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsCatalogsInTableDefinitions() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether a catalog name can be used in an index definition statement.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsCatalogsInIndexDefinitions() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves whether a catalog name can be used in a privilege definition statement.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether this database supports positioned <code>DELETE</code>
     * statements.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsPositionedDelete() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether this database supports positioned <code>UPDATE</code>
     * statements.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsPositionedUpdate() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether this database supports <code>SELECT FOR UPDATE</code>
     * statements.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsSelectForUpdate() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether this database supports stored procedure calls
     * that use the stored procedure escape syntax.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsStoredProcedures() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether this database supports subqueries in comparison
     * expressions.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsSubqueriesInComparisons() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves whether this database supports subqueries in
     * <code>EXISTS</code> expressions.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsSubqueriesInExists() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves whether this database supports subqueries in
     * <code>IN</code> expressions.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsSubqueriesInIns() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves whether this database supports subqueries in quantified
     * expressions.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsSubqueriesInQuantifieds() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves whether this database supports correlated subqueries.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsCorrelatedSubqueries() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether this database supports SQL <code>UNION</code>.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsUnion() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves whether this database supports SQL <code>UNION ALL</code>.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsUnionAll() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves whether this database supports keeping cursors open
     * across commits.
     *
     * @return <code>true</code> if cursors always remain open;
     * <code>false</code> if they might not remain open
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsOpenCursorsAcrossCommit() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether this database supports keeping cursors open
     * across rollbacks.
     *
     * @return <code>true</code> if cursors always remain open;
     * <code>false</code> if they might not remain open
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsOpenCursorsAcrossRollback() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether this database supports keeping statements open
     * across commits.
     *
     * @return <code>true</code> if statements always remain open;
     * <code>false</code> if they might not remain open
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsOpenStatementsAcrossCommit() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves whether this database supports keeping statements open
     * across rollbacks.
     *
     * @return <code>true</code> if statements always remain open;
     * <code>false</code> if they might not remain open
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsOpenStatementsAcrossRollback() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves the maximum number of hex characters this database allows in an
     * inline binary literal.
     *
     * @return max the maximum length (in hex characters) for a binary literal;
     * a result of zero means that there is no limit or the limit
     * is not known
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public int getMaxBinaryLiteralLength() throws SQLException
    {
        return 0;
    }

    /**
     * Retrieves the maximum number of characters this database allows
     * for a character literal.
     *
     * @return the maximum number of characters allowed for a character literal;
     * a result of zero means that there is no limit or the limit is
     * not known
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public int getMaxCharLiteralLength() throws SQLException
    {
        return 0;
    }

    /**
     * Retrieves the maximum number of characters this database allows
     * for a column name.
     *
     * @return the maximum number of characters allowed for a column name;
     * a result of zero means that there is no limit or the limit
     * is not known
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public int getMaxColumnNameLength() throws SQLException
    {
        return 0;
    }

    /**
     * Retrieves the maximum number of columns this database allows in a
     * <code>GROUP BY</code> clause.
     *
     * @return the maximum number of columns allowed;
     * a result of zero means that there is no limit or the limit
     * is not known
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public int getMaxColumnsInGroupBy() throws SQLException
    {
        return 0;
    }

    /**
     * Retrieves the maximum number of columns this database allows in an index.
     *
     * @return the maximum number of columns allowed;
     * a result of zero means that there is no limit or the limit
     * is not known
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public int getMaxColumnsInIndex() throws SQLException
    {
        return 0;
    }

    /**
     * Retrieves the maximum number of columns this database allows in an
     * <code>ORDER BY</code> clause.
     *
     * @return the maximum number of columns allowed;
     * a result of zero means that there is no limit or the limit
     * is not known
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public int getMaxColumnsInOrderBy() throws SQLException
    {
        return 0;
    }

    /**
     * Retrieves the maximum number of columns this database allows in a
     * <code>SELECT</code> list.
     *
     * @return the maximum number of columns allowed;
     * a result of zero means that there is no limit or the limit
     * is not known
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public int getMaxColumnsInSelect() throws SQLException
    {
        return 0;
    }

    /**
     * Retrieves the maximum number of columns this database allows in a table.
     *
     * @return the maximum number of columns allowed;
     * a result of zero means that there is no limit or the limit
     * is not known
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public int getMaxColumnsInTable() throws SQLException
    {
        return 0;
    }

    /**
     * Retrieves the maximum number of concurrent connections to this
     * database that are possible.
     *
     * @return the maximum number of active connections possible at one time;
     * a result of zero means that there is no limit or the limit
     * is not known
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public int getMaxConnections() throws SQLException
    {
        return 0;
    }

    /**
     * Retrieves the maximum number of characters that this database allows in a
     * cursor name.
     *
     * @return the maximum number of characters allowed in a cursor name;
     * a result of zero means that there is no limit or the limit
     * is not known
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public int getMaxCursorNameLength() throws SQLException
    {
        return 0;
    }

    /**
     * Retrieves the maximum number of bytes this database allows for an
     * index, including all of the parts of the index.
     *
     * @return the maximum number of bytes allowed; this limit includes the
     * composite of all the constituent parts of the index;
     * a result of zero means that there is no limit or the limit
     * is not known
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public int getMaxIndexLength() throws SQLException
    {
        return 0;
    }

    /**
     * Retrieves the maximum number of characters that this database allows in a
     * schema name.
     *
     * @return the maximum number of characters allowed in a schema name;
     * a result of zero means that there is no limit or the limit
     * is not known
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public int getMaxSchemaNameLength() throws SQLException
    {
        return 0;
    }

    /**
     * Retrieves the maximum number of characters that this database allows in a
     * procedure name.
     *
     * @return the maximum number of characters allowed in a procedure name;
     * a result of zero means that there is no limit or the limit
     * is not known
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public int getMaxProcedureNameLength() throws SQLException
    {
        return 0;
    }

    /**
     * Retrieves the maximum number of characters that this database allows in a
     * catalog name.
     *
     * @return the maximum number of characters allowed in a catalog name;
     * a result of zero means that there is no limit or the limit
     * is not known
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public int getMaxCatalogNameLength() throws SQLException
    {
        return 0;
    }

    /**
     * Retrieves the maximum number of bytes this database allows in
     * a single row.
     *
     * @return the maximum number of bytes allowed for a row; a result of
     * zero means that there is no limit or the limit is not known
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public int getMaxRowSize() throws SQLException
    {
        return 0;
    }

    /**
     * Retrieves whether the return value for the method
     * <code>getMaxRowSize</code> includes the SQL data types
     * <code>LONGVARCHAR</code> and <code>LONGVARBINARY</code>.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves the maximum number of characters this database allows in
     * an SQL statement.
     *
     * @return the maximum number of characters allowed for an SQL statement;
     * a result of zero means that there is no limit or the limit
     * is not known
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public int getMaxStatementLength() throws SQLException
    {
        return 0;
    }

    /**
     * Retrieves the maximum number of active statements to this database
     * that can be open at the same time.
     *
     * @return the maximum number of statements that can be open at one time;
     * a result of zero means that there is no limit or the limit
     * is not known
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public int getMaxStatements() throws SQLException
    {
        return 0;
    }

    /**
     * Retrieves the maximum number of characters this database allows in
     * a table name.
     *
     * @return the maximum number of characters allowed for a table name;
     * a result of zero means that there is no limit or the limit
     * is not known
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public int getMaxTableNameLength() throws SQLException
    {
        return 0;
    }

    /**
     * Retrieves the maximum number of tables this database allows in a
     * <code>SELECT</code> statement.
     *
     * @return the maximum number of tables allowed in a <code>SELECT</code>
     * statement; a result of zero means that there is no limit or
     * the limit is not known
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public int getMaxTablesInSelect() throws SQLException
    {
        return 0;
    }

    /**
     * Retrieves the maximum number of characters this database allows in
     * a user name.
     *
     * @return the maximum number of characters allowed for a user name;
     * a result of zero means that there is no limit or the limit
     * is not known
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public int getMaxUserNameLength() throws SQLException
    {
        return 0;
    }

    /**
     * Retrieves this database's default transaction isolation level.  The
     * possible values are defined in <code>java.sql.Connection</code>.
     *
     * @return the default isolation level
     * @throws java.sql.SQLException if a database access error occurs
     * @see Connection
     */
    @Override
    public int getDefaultTransactionIsolation() throws SQLException
    {
        return Connection.TRANSACTION_READ_UNCOMMITTED;
    }

    /**
     * Retrieves whether this database supports transactions. If not, invoking the
     * method <code>commit</code> is a noop, and the isolation level is
     * <code>TRANSACTION_NONE</code>.
     *
     * @return <code>true</code> if transactions are supported;
     * <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsTransactions() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves whether this database supports the given transaction isolation level.
     *
     * @param level one of the transaction isolation levels defined in
     *              <code>java.sql.Connection</code>
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     * @see Connection
     */
    @Override
    public boolean supportsTransactionIsolationLevel(int level) throws SQLException
    {
        return Connection.TRANSACTION_READ_UNCOMMITTED==level;
    }

    /**
     * Retrieves whether this database supports both data definition and
     * data manipulation statements within a transaction.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves whether this database supports only data manipulation
     * statements within a transaction.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean supportsDataManipulationTransactionsOnly() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether a data definition statement within a transaction forces
     * the transaction to commit.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean dataDefinitionCausesTransactionCommit() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether this database ignores a data definition statement
     * within a transaction.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves a description of the stored procedures available in the given
     * catalog.
     * 
     * Only procedure descriptions matching the schema and
     * procedure name criteria are returned.  They are ordered by
     * <code>PROCEDURE_CAT</code>, <code>PROCEDURE_SCHEM</code>,
     * <code>PROCEDURE_NAME</code> and <code>SPECIFIC_ NAME</code>.
     * 
     * <P>Each procedure description has the the following columns:
     * <OL>
     * <LI><B>PROCEDURE_CAT</B> String =&gt; procedure catalog (may be <code>null</code>)
     * <LI><B>PROCEDURE_SCHEM</B> String =&gt; procedure schema (may be <code>null</code>)
     * <LI><B>PROCEDURE_NAME</B> String =&gt; procedure name</LI>
     * <LI> reserved for future use
     * <LI> reserved for future use
     * <LI> reserved for future use
     * <LI><B>REMARKS</B> String =&gt; explanatory comment on the procedure
     * <LI><B>PROCEDURE_TYPE</B> short =&gt; kind of procedure:
     * <UL>
     * <LI> procedureResultUnknown - Cannot determine if  a return value
     * will be returned
     * <LI> procedureNoResult - Does not return a return value
     * <LI> procedureReturnsResult - Returns a return value
     * </UL>
     * <LI><B>SPECIFIC_NAME</B> String  =&gt; The name which uniquely identifies this
     * procedure within its schema.
     * </OL>
     * 
     * A user may not have permissions to execute any of the procedures that are
     * returned by <code>getProcedures</code>
     *
     * @param catalog              a catalog name; must match the catalog name as it
     *                             is stored in the database; "" retrieves those without a catalog;
     *                             <code>null</code> means that the catalog name should not be used to narrow
     *                             the search
     * @param schemaPattern        a schema name pattern; must match the schema name
     *                             as it is stored in the database; "" retrieves those without a schema;
     *                             <code>null</code> means that the schema name should not be used to narrow
     *                             the search
     * @param procedureNamePattern a procedure name pattern; must match the
     *                             procedure name as it is stored in the database
     * @return <code>ResultSet</code> - each row is a procedure description
     * @throws java.sql.SQLException if a database access error occurs
     * @see #getSearchStringEscape
     */
    @Override
    public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern) throws SQLException
    {
        return getEmptyResultSet();
    }

    /**
     * Retrieves a description of the given catalog's stored procedure parameter
     * and result columns.
     * 
     * <P>Only descriptions matching the schema, procedure and
     * parameter name criteria are returned.  They are ordered by
     * PROCEDURE_CAT, PROCEDURE_SCHEM, PROCEDURE_NAME and SPECIFIC_NAME. Within this, the return value,
     * if any, is first. Next are the parameter descriptions in call
     * order. The column descriptions follow in column number order.
     * 
     * <P>Each row in the <code>ResultSet</code> is a parameter description or
     * column description with the following fields:
     * <OL>
     * <LI><B>PROCEDURE_CAT</B> String =&gt; procedure catalog (may be <code>null</code>)
     * <LI><B>PROCEDURE_SCHEM</B> String =&gt; procedure schema (may be <code>null</code>)
     * <LI><B>PROCEDURE_NAME</B> String =&gt; procedure name
     * <LI><B>COLUMN_NAME</B> String =&gt; column/parameter name
     * <LI><B>COLUMN_TYPE</B> Short =&gt; kind of column/parameter:
     * <UL>
     * <LI> procedureColumnUnknown - nobody knows
     * <LI> procedureColumnIn - IN parameter
     * <LI> procedureColumnInOut - INOUT parameter
     * <LI> procedureColumnOut - OUT parameter
     * <LI> procedureColumnReturn - procedure return value
     * <LI> procedureColumnResult - result column in <code>ResultSet</code>
     * </UL>
     * <LI><B>DATA_TYPE</B> int =&gt; SQL type from java.sql.Types
     * <LI><B>TYPE_NAME</B> String =&gt; SQL type name, for a UDT type the
     * type name is fully qualified
     * <LI><B>PRECISION</B> int =&gt; precision
     * <LI><B>LENGTH</B> int =&gt; length in bytes of data
     * <LI><B>SCALE</B> short =&gt; scale -  null is returned for data types where
     * SCALE is not applicable.
     * <LI><B>RADIX</B> short =&gt; radix
     * <LI><B>NULLABLE</B> short =&gt; can it contain NULL.
     * <UL>
     * <LI> procedureNoNulls - does not allow NULL values
     * <LI> procedureNullable - allows NULL values
     * <LI> procedureNullableUnknown - nullability unknown
     * </UL>
     * <LI><B>REMARKS</B> String =&gt; comment describing parameter/column
     * <LI><B>COLUMN_DEF</B> String =&gt; default value for the column, which should be interpreted as a string when the value is enclosed in single quotes (may be <code>null</code>)
     * <UL>
     * <LI> The string NULL (not enclosed in quotes) - if NULL was specified as the default value
     * <LI> TRUNCATE (not enclosed in quotes)        - if the specified default value cannot be represented without truncation
     * <LI> NULL                                     - if a default value was not specified
     * </UL>
     * <LI><B>SQL_DATA_TYPE</B> int  =&gt; reserved for future use
     * <LI><B>SQL_DATETIME_SUB</B> int  =&gt; reserved for future use
     * <LI><B>CHAR_OCTET_LENGTH</B> int  =&gt; the maximum length of binary and character based columns.  For any other datatype the returned value is a
     * NULL
     * <LI><B>ORDINAL_POSITION</B> int  =&gt; the ordinal position, starting from 1, for the input and output parameters for a procedure. A value of 0
     * is returned if this row describes the procedure's return value.  For result set columns, it is the
     * ordinal position of the column in the result set starting from 1.  If there are
     * multiple result sets, the column ordinal positions are implementation
     * defined.
     * <LI><B>IS_NULLABLE</B> String  =&gt; ISO rules are used to determine the nullability for a column.
     * <UL>
     * <LI> YES           --- if the column can include NULLs
     * <LI> NO            --- if the column cannot include NULLs
     * <LI> empty string  --- if the nullability for the
     * column is unknown
     * </UL>
     * <LI><B>SPECIFIC_NAME</B> String  =&gt; the name which uniquely identifies this procedure within its schema.
     * </OL>
     * 
     * <P><B>Note:</B> Some databases may not return the column
     * descriptions for a procedure.
     * 
     * <p>The PRECISION column represents the specified column size for the given column.
     * For numeric data, this is the maximum precision.  For character data, this is the length in characters.
     * For datetime datatypes, this is the length in characters of the String representation (assuming the
     * maximum allowed precision of the fractional seconds component). For binary data, this is the length in bytes.  For the ROWID datatype,
     * this is the length in bytes. Null is returned for data types where the
     * column size is not applicable.
     *
     * @param catalog              a catalog name; must match the catalog name as it
     *                             is stored in the database; "" retrieves those without a catalog;
     *                             <code>null</code> means that the catalog name should not be used to narrow
     *                             the search
     * @param schemaPattern        a schema name pattern; must match the schema name
     *                             as it is stored in the database; "" retrieves those without a schema;
     *                             <code>null</code> means that the schema name should not be used to narrow
     *                             the search
     * @param procedureNamePattern a procedure name pattern; must match the
     *                             procedure name as it is stored in the database
     * @param columnNamePattern    a column name pattern; must match the column name
     *                             as it is stored in the database
     * @return <code>ResultSet</code> - each row describes a stored procedure parameter or
     * column
     * @throws java.sql.SQLException if a database access error occurs
     * @see #getSearchStringEscape
     */
    @Override
    public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern) throws SQLException
    {
        return getEmptyResultSet();
    }

    /**
     * Retrieves a description of the tables available in the given catalog.
     * Only table descriptions matching the catalog, schema, table
     * name and type criteria are returned.  They are ordered by
     * <code>TABLE_TYPE</code>, <code>TABLE_CAT</code>,
     * <code>TABLE_SCHEM</code> and <code>TABLE_NAME</code>.
     * 
     * Each table description has the following columns:
     * <OL>
     * <LI><B>TABLE_CAT</B> String =&gt; table catalog (may be <code>null</code>)
     * <LI><B>TABLE_SCHEM</B> String =&gt; table schema (may be <code>null</code>)
     * <LI><B>TABLE_NAME</B> String =&gt; table name
     * <LI><B>TABLE_TYPE</B> String =&gt; table type.  Typical types are "TABLE",
     * "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY",
     * "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
     * <LI><B>REMARKS</B> String =&gt; explanatory comment on the table
     * <LI><B>TYPE_CAT</B> String =&gt; the types catalog (may be <code>null</code>)
     * <LI><B>TYPE_SCHEM</B> String =&gt; the types schema (may be <code>null</code>)
     * <LI><B>TYPE_NAME</B> String =&gt; type name (may be <code>null</code>)
     * <LI><B>SELF_REFERENCING_COL_NAME</B> String =&gt; name of the designated
     * "identifier" column of a typed table (may be <code>null</code>)
     * <LI><B>REF_GENERATION</B> String =&gt; specifies how values in
     * SELF_REFERENCING_COL_NAME are created. Values are
     * "SYSTEM", "USER", "DERIVED". (may be <code>null</code>)
     * </OL>
     * 
     * <P><B>Note:</B> Some databases may not return information for
     * all tables.
     *
     * @param catalog          a catalog name; must match the catalog name as it
     *                         is stored in the database; "" retrieves those without a catalog;
     *                         <code>null</code> means that the catalog name should not be used to narrow
     *                         the search
     * @param schemaPattern    a schema name pattern; must match the schema name
     *                         as it is stored in the database; "" retrieves those without a schema;
     *                         <code>null</code> means that the schema name should not be used to narrow
     *                         the search
     * @param tableNamePattern a table name pattern; must match the
     *                         table name as it is stored in the database
     * @param types            a list of table types, which must be from the list of table types
     *                         returned from {@link #getTableTypes},to include; <code>null</code> returns
     *                         all types
     * @return <code>ResultSet</code> - each row is a table description
     * @throws java.sql.SQLException if a database access error occurs
     * @see #getSearchStringEscape
     */
    @Override
    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException
    {
        String sql = "select null as TABLE_CAT, namespace_id as TABLE_SCHEM, name as TABLE_NAME, 'TABLE' as TABLE_TYPE, null as REMARKS, null as TYPE_CAT, null as TYPE_SCHEM," +
                "null as TYPE_NAME, null as SELF_REFERENCING_COL_NAME, null as REF_GENERATION from system:keyspaces";

        if (schemaPattern != null || tableNamePattern != null)
        {
            sql += " where ";
            if ( schemaPattern != null )
            {
                sql += "namespace_id like '" + schemaPattern + "'";
            }
            if ( schemaPattern != null && tableNamePattern != null )
            {
                sql += " and ";
            }
            if (tableNamePattern != null )
            {
                sql += "name LIKE '" + tableNamePattern +"'";
            }
        }

        try(Statement statement = connection.createStatement())
        {
            return statement.executeQuery(sql);
        }
    }

    /**
     * Retrieves the schema names available in this database.  The results
     * are ordered by <code>TABLE_CATALOG</code> and
     * <code>TABLE_SCHEM</code>.
     * 
     * <P>The schema columns are:
     * <OL>
     * <LI><B>TABLE_SCHEM</B> String =&gt; schema name
     * <LI><B>TABLE_CATALOG</B> String =&gt; catalog name (may be <code>null</code>)
     * </OL>
     *
     * @return a <code>ResultSet</code> object in which each row is a
     * schema description
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public ResultSet getSchemas() throws SQLException
    {
        return getSchemas(null, null);
    }

    /**
     * Retrieves the catalog names available in this database.  The results
     * are ordered by catalog name.
     * 
     * <P>The catalog column is:
     * <OL>
     * <LI><B>TABLE_CAT</B> String =&gt; catalog name
     * </OL>
     *
     * @return a <code>ResultSet</code> object in which each row has a
     * single <code>String</code> column that is a catalog name
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public ResultSet getCatalogs() throws SQLException
    {
        String sql = "select name as TABLE_CAT from system:namespaces";
        try( Statement statement = connection.createStatement())
        {
            return statement.executeQuery( sql );
        }
    }

    /**
     * Retrieves the table types available in this database.  The results
     * are ordered by table type.
     * 
     * <P>The table type is:
     * <OL>
     * <LI><B>TABLE_TYPE</B> String =&gt; table type.  Typical types are "TABLE",
     * "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY",
     * "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
     * </OL>
     *
     * @return a <code>ResultSet</code> object in which each row has a
     * single <code>String</code> column that is a table type
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public ResultSet getTableTypes() throws SQLException
    {
        List <Map<String,String>> catalogs = new ArrayList<Map<String,String>>(2);
        Map <String,String>table = new HashMap<String, String>();
////SYSTEM; BUCKET; GSI INDEX; VIEW INDEX
        table.put("TABLE_TYPE","SYSTEM");
        catalogs.add(table);

        table = new HashMap<>();
        table.put("TABLE_TYPE", "BUCKET");
        catalogs.add(table);

        table = new HashMap<>();
        table.put("TABLE_TYPE", "GSI INDEX");
        catalogs.add(table);

        table = new HashMap<>();
        table.put("TABLE_TYPE", "VIEW INDEX");
        catalogs.add(table);

        CouchMetrics metrics = new CouchMetrics();
        metrics.setResultCount(table.size());
        // this just has to be not 0
        metrics.setResultSize(1);

        @SuppressWarnings("unchecked")
        Map <String, String> signature = new HashMap();
        signature.put("TABLE_TYPE","string");

        CouchResponse couchResponse = new CouchResponse();
        couchResponse.setResults(catalogs);
        couchResponse.setMetrics(metrics);
        couchResponse.setSignature(signature);

        return new CBResultSet(null, couchResponse);
    }

    /**
     * Retrieves a description of table columns available in
     * the specified catalog.
     * 
     * <P>Only column descriptions matching the catalog, schema, table
     * and column name criteria are returned.  They are ordered by
     * <code>TABLE_CAT</code>,<code>TABLE_SCHEM</code>,
     * <code>TABLE_NAME</code>, and <code>ORDINAL_POSITION</code>.
     * 
     * <P>Each column description has the following columns:
     * <OL>
     * <LI><B>TABLE_CAT</B> String =&gt; table catalog (may be <code>null</code>)
     * <LI><B>TABLE_SCHEM</B> String =&gt; table schema (may be <code>null</code>)
     * <LI><B>TABLE_NAME</B> String =&gt; table name
     * <LI><B>COLUMN_NAME</B> String =&gt; column name
     * <LI><B>DATA_TYPE</B> int =&gt; SQL type from java.sql.Types
     * <LI><B>TYPE_NAME</B> String =&gt; Data source dependent type name,
     * for a UDT the type name is fully qualified
     * <LI><B>COLUMN_SIZE</B> int =&gt; column size.
     * <LI><B>BUFFER_LENGTH</B> is not used.
     * <LI><B>DECIMAL_DIGITS</B> int =&gt; the number of fractional digits. Null is returned for data types where
     * DECIMAL_DIGITS is not applicable.
     * <LI><B>NUM_PREC_RADIX</B> int =&gt; Radix (typically either 10 or 2)
     * <LI><B>NULLABLE</B> int =&gt; is NULL allowed.
     * <UL>
     * <LI> columnNoNulls - might not allow <code>NULL</code> values
     * <LI> columnNullable - definitely allows <code>NULL</code> values
     * <LI> columnNullableUnknown - nullability unknown
     * </UL>
     * <LI><B>REMARKS</B> String =&gt; comment describing column (may be <code>null</code>)
     * <LI><B>COLUMN_DEF</B> String =&gt; default value for the column, which should be interpreted as a string when the value is enclosed in single quotes (may be <code>null</code>)
     * <LI><B>SQL_DATA_TYPE</B> int =&gt; unused
     * <LI><B>SQL_DATETIME_SUB</B> int =&gt; unused
     * <LI><B>CHAR_OCTET_LENGTH</B> int =&gt; for char types the
     * maximum number of bytes in the column
     * <LI><B>ORDINAL_POSITION</B> int =&gt; index of column in table
     * (starting at 1)
     * <LI><B>IS_NULLABLE</B> String  =&gt; ISO rules are used to determine the nullability for a column.
     * <UL>
     * <LI> YES           --- if the column can include NULLs
     * <LI> NO            --- if the column cannot include NULLs
     * <LI> empty string  --- if the nullability for the
     * column is unknown
     * </UL>
     * <LI><B>SCOPE_CATALOG</B> String =&gt; catalog of table that is the scope
     * of a reference attribute (<code>null</code> if DATA_TYPE isn't REF)
     * <LI><B>SCOPE_SCHEMA</B> String =&gt; schema of table that is the scope
     * of a reference attribute (<code>null</code> if the DATA_TYPE isn't REF)
     * <LI><B>SCOPE_TABLE</B> String =&gt; table name that this the scope
     * of a reference attribute (<code>null</code> if the DATA_TYPE isn't REF)
     * <LI><B>SOURCE_DATA_TYPE</B> short =&gt; source type of a distinct type or user-generated
     * Ref type, SQL type from java.sql.Types (<code>null</code> if DATA_TYPE
     * isn't DISTINCT or user-generated REF)
     * <LI><B>IS_AUTOINCREMENT</B> String  =&gt; Indicates whether this column is auto incremented
     * <UL>
     * <LI> YES           --- if the column is auto incremented
     * <LI> NO            --- if the column is not auto incremented
     * <LI> empty string  --- if it cannot be determined whether the column is auto incremented
     * </UL>
     * <LI><B>IS_GENERATEDCOLUMN</B> String  =&gt; Indicates whether this is a generated column
     * <UL>
     * <LI> YES           --- if this a generated column
     * <LI> NO            --- if this not a generated column
     * <LI> empty string  --- if it cannot be determined whether this is a generated column
     * </UL>
     * </OL>
     * 
     * <p>The COLUMN_SIZE column specifies the column size for the given column.
     * For numeric data, this is the maximum precision.  For character data, this is the length in characters.
     * For datetime datatypes, this is the length in characters of the String representation (assuming the
     * maximum allowed precision of the fractional seconds component). For binary data, this is the length in bytes.  For the ROWID datatype,
     * this is the length in bytes. Null is returned for data types where the
     * column size is not applicable.
     *
     * @param catalog           a catalog name; must match the catalog name as it
     *                          is stored in the database; "" retrieves those without a catalog;
     *                          <code>null</code> means that the catalog name should not be used to narrow
     *                          the search
     * @param schemaPattern     a schema name pattern; must match the schema name
     *                          as it is stored in the database; "" retrieves those without a schema;
     *                          <code>null</code> means that the schema name should not be used to narrow
     *                          the search
     * @param tableNamePattern  a table name pattern; must match the
     *                          table name as it is stored in the database
     * @param columnNamePattern a column name pattern; must match the column
     *                          name as it is stored in the database
     * @return <code>ResultSet</code> - each row is a column description
     * @throws java.sql.SQLException if a database access error occurs
     * @see #getSearchStringEscape
     */
    @Override
    public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException
    {
        return getEmptyResultSet();
    }

    /**
     * Retrieves a description of the access rights for a table's columns.
     * 
     * <P>Only privileges matching the column name criteria are
     * returned.  They are ordered by COLUMN_NAME and PRIVILEGE.
     * 
     * <P>Each privilige description has the following columns:
     * <OL>
     * <LI><B>TABLE_CAT</B> String =&gt; table catalog (may be <code>null</code>)
     * <LI><B>TABLE_SCHEM</B> String =&gt; table schema (may be <code>null</code>)
     * <LI><B>TABLE_NAME</B> String =&gt; table name
     * <LI><B>COLUMN_NAME</B> String =&gt; column name
     * <LI><B>GRANTOR</B> String =&gt; grantor of access (may be <code>null</code>)
     * <LI><B>GRANTEE</B> String =&gt; grantee of access
     * <LI><B>PRIVILEGE</B> String =&gt; name of access (SELECT,
     * INSERT, UPDATE, REFRENCES, ...)
     * <LI><B>IS_GRANTABLE</B> String =&gt; "YES" if grantee is permitted
     * to grant to others; "NO" if not; <code>null</code> if unknown
     * </OL>
     *
     * @param catalog           a catalog name; must match the catalog name as it
     *                          is stored in the database; "" retrieves those without a catalog;
     *                          <code>null</code> means that the catalog name should not be used to narrow
     *                          the search
     * @param schema            a schema name; must match the schema name as it is
     *                          stored in the database; "" retrieves those without a schema;
     *                          <code>null</code> means that the schema name should not be used to narrow
     *                          the search
     * @param table             a table name; must match the table name as it is
     *                          stored in the database
     * @param columnNamePattern a column name pattern; must match the column
     *                          name as it is stored in the database
     * @return <code>ResultSet</code> - each row is a column privilege description
     * @throws java.sql.SQLException if a database access error occurs
     * @see #getSearchStringEscape
     */
    @Override
    public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern) throws SQLException
    {
        return getEmptyResultSet();
    }

    /**
     * Retrieves a description of the access rights for each table available
     * in a catalog. Note that a table privilege applies to one or
     * more columns in the table. It would be wrong to assume that
     * this privilege applies to all columns (this may be true for
     * some systems but is not true for all.)
     * 
     * <P>Only privileges matching the schema and table name
     * criteria are returned.  They are ordered by
     * <code>TABLE_CAT</code>,
     * <code>TABLE_SCHEM</code>, <code>TABLE_NAME</code>,
     * and <code>PRIVILEGE</code>.
     * 
     * <P>Each privilige description has the following columns:
     * <OL>
     * <LI><B>TABLE_CAT</B> String =&gt; table catalog (may be <code>null</code>)
     * <LI><B>TABLE_SCHEM</B> String =&gt; table schema (may be <code>null</code>)
     * <LI><B>TABLE_NAME</B> String =&gt; table name
     * <LI><B>GRANTOR</B> String =&gt; grantor of access (may be <code>null</code>)
     * <LI><B>GRANTEE</B> String =&gt; grantee of access
     * <LI><B>PRIVILEGE</B> String =&gt; name of access (SELECT,
     * INSERT, UPDATE, REFRENCES, ...)
     * <LI><B>IS_GRANTABLE</B> String =&gt; "YES" if grantee is permitted
     * to grant to others; "NO" if not; <code>null</code> if unknown
     * </OL>
     *
     * @param catalog          a catalog name; must match the catalog name as it
     *                         is stored in the database; "" retrieves those without a catalog;
     *                         <code>null</code> means that the catalog name should not be used to narrow
     *                         the search
     * @param schemaPattern    a schema name pattern; must match the schema name
     *                         as it is stored in the database; "" retrieves those without a schema;
     *                         <code>null</code> means that the schema name should not be used to narrow
     *                         the search
     * @param tableNamePattern a table name pattern; must match the
     *                         table name as it is stored in the database
     * @return <code>ResultSet</code> - each row is a table privilege description
     * @throws java.sql.SQLException if a database access error occurs
     * @see #getSearchStringEscape
     */
    @Override
    public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern) throws SQLException
    {
        return getEmptyResultSet();
    }

    /**
     * Retrieves a description of a table's optimal set of columns that
     * uniquely identifies a row. They are ordered by SCOPE.
     * 
     * <P>Each column description has the following columns:
     * <OL>
     * <LI><B>SCOPE</B> short =&gt; actual scope of result
     * <UL>
     * <LI> bestRowTemporary - very temporary, while using row
     * <LI> bestRowTransaction - valid for remainder of current transaction
     * <LI> bestRowSession - valid for remainder of current session
     * </UL>
     * <LI><B>COLUMN_NAME</B> String =&gt; column name
     * <LI><B>DATA_TYPE</B> int =&gt; SQL data type from java.sql.Types
     * <LI><B>TYPE_NAME</B> String =&gt; Data source dependent type name,
     * for a UDT the type name is fully qualified
     * <LI><B>COLUMN_SIZE</B> int =&gt; precision
     * <LI><B>BUFFER_LENGTH</B> int =&gt; not used
     * <LI><B>DECIMAL_DIGITS</B> short  =&gt; scale - Null is returned for data types where
     * DECIMAL_DIGITS is not applicable.
     * <LI><B>PSEUDO_COLUMN</B> short =&gt; is this a pseudo column
     * like an Oracle ROWID
     * <UL>
     * <LI> bestRowUnknown - may or may not be pseudo column
     * <LI> bestRowNotPseudo - is NOT a pseudo column
     * <LI> bestRowPseudo - is a pseudo column
     * </UL>
     * </OL>
     * 
     * <p>The COLUMN_SIZE column represents the specified column size for the given column.
     * For numeric data, this is the maximum precision.  For character data, this is the length in characters.
     * For datetime datatypes, this is the length in characters of the String representation (assuming the
     * maximum allowed precision of the fractional seconds component). For binary data, this is the length in bytes.  For the ROWID datatype,
     * this is the length in bytes. Null is returned for data types where the
     * column size is not applicable.
     *
     * @param catalog  a catalog name; must match the catalog name as it
     *                 is stored in the database; "" retrieves those without a catalog;
     *                 <code>null</code> means that the catalog name should not be used to narrow
     *                 the search
     * @param schema   a schema name; must match the schema name
     *                 as it is stored in the database; "" retrieves those without a schema;
     *                 <code>null</code> means that the schema name should not be used to narrow
     *                 the search
     * @param table    a table name; must match the table name as it is stored
     *                 in the database
     * @param scope    the scope of interest; use same values as SCOPE
     * @param nullable include columns that are nullable.
     * @return <code>ResultSet</code> - each row is a column description
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable) throws SQLException
    {
        return getEmptyResultSet();
    }

    /**
     * Retrieves a description of a table's columns that are automatically
     * updated when any value in a row is updated.  They are
     * unordered.
     * 
     * <P>Each column description has the following columns:
     * <OL>
     * <LI><B>SCOPE</B> short =&gt; is not used
     * <LI><B>COLUMN_NAME</B> String =&gt; column name
     * <LI><B>DATA_TYPE</B> int =&gt; SQL data type from <code>java.sql.Types</code>
     * <LI><B>TYPE_NAME</B> String =&gt; Data source-dependent type name
     * <LI><B>COLUMN_SIZE</B> int =&gt; precision
     * <LI><B>BUFFER_LENGTH</B> int =&gt; length of column value in bytes
     * <LI><B>DECIMAL_DIGITS</B> short  =&gt; scale - Null is returned for data types where
     * DECIMAL_DIGITS is not applicable.
     * <LI><B>PSEUDO_COLUMN</B> short =&gt; whether this is pseudo column
     * like an Oracle ROWID
     * <UL>
     * <LI> versionColumnUnknown - may or may not be pseudo column
     * <LI> versionColumnNotPseudo - is NOT a pseudo column
     * <LI> versionColumnPseudo - is a pseudo column
     * </UL>
     * </OL>
     * 
     * <p>The COLUMN_SIZE column represents the specified column size for the given column.
     * For numeric data, this is the maximum precision.  For character data, this is the length in characters.
     * For datetime datatypes, this is the length in characters of the String representation (assuming the
     * maximum allowed precision of the fractional seconds component). For binary data, this is the length in bytes.  For the ROWID datatype,
     * this is the length in bytes. Null is returned for data types where the
     * column size is not applicable.
     *
     * @param catalog a catalog name; must match the catalog name as it
     *                is stored in the database; "" retrieves those without a catalog;
     *                <code>null</code> means that the catalog name should not be used to narrow
     *                the search
     * @param schema  a schema name; must match the schema name
     *                as it is stored in the database; "" retrieves those without a schema;
     *                <code>null</code> means that the schema name should not be used to narrow
     *                the search
     * @param table   a table name; must match the table name as it is stored
     *                in the database
     * @return a <code>ResultSet</code> object in which each row is a
     * column description
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException
    {
        return getEmptyResultSet();
    }

    /**
     * Retrieves a description of the given table's primary key columns.  They
     * are ordered by COLUMN_NAME.
     * 
     * <P>Each primary key column description has the following columns:
     * <OL>
     * <LI><B>TABLE_CAT</B> String =&gt; table catalog (may be <code>null</code>)
     * <LI><B>TABLE_SCHEM</B> String =&gt; table schema (may be <code>null</code>)
     * <LI><B>TABLE_NAME</B> String =&gt; table name
     * <LI><B>COLUMN_NAME</B> String =&gt; column name
     * <LI><B>KEY_SEQ</B> short =&gt; sequence number within primary key( a value
     * of 1 represents the first column of the primary key, a value of 2 would
     * represent the second column within the primary key).
     * <LI><B>PK_NAME</B> String =&gt; primary key name (may be <code>null</code>)
     * </OL>
     *
     * @param catalog a catalog name; must match the catalog name as it
     *                is stored in the database; "" retrieves those without a catalog;
     *                <code>null</code> means that the catalog name should not be used to narrow
     *                the search
     * @param schema  a schema name; must match the schema name
     *                as it is stored in the database; "" retrieves those without a schema;
     *                <code>null</code> means that the schema name should not be used to narrow
     *                the search
     * @param table   a table name; must match the table name as it is stored
     *                in the database
     * @return <code>ResultSet</code> - each row is a primary key column description
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException
    {
        String sql = "select null as TABLE_CAT, namespace_id as TABLE_SCHEM, keyspace_id as TABLE_NAME, 'id' as COLUMN_NAME, 1 as KEY_SEQ, name as PK_NAME from system:indexes where is_primary = true";

        if (schema != null )
        {
            sql += " and namespace_id like " + schema;
        }
        if (table != null)
        {
            sql += " and keyspace_id like " + table;
        }

        try(Statement statement = connection.createStatement())
        {
            return statement.executeQuery(sql);
        }
    }

    /**
     * Retrieves a description of the primary key columns that are
     * referenced by the given table's foreign key columns (the primary keys
     * imported by a table).  They are ordered by PKTABLE_CAT,
     * PKTABLE_SCHEM, PKTABLE_NAME, and KEY_SEQ.
     * 
     * <P>Each primary key column description has the following columns:
     * <OL>
     * <LI><B>PKTABLE_CAT</B> String =&gt; primary key table catalog
     * being imported (may be <code>null</code>)
     * <LI><B>PKTABLE_SCHEM</B> String =&gt; primary key table schema
     * being imported (may be <code>null</code>)
     * <LI><B>PKTABLE_NAME</B> String =&gt; primary key table name
     * being imported
     * <LI><B>PKCOLUMN_NAME</B> String =&gt; primary key column name
     * being imported
     * <LI><B>FKTABLE_CAT</B> String =&gt; foreign key table catalog (may be <code>null</code>)
     * <LI><B>FKTABLE_SCHEM</B> String =&gt; foreign key table schema (may be <code>null</code>)
     * <LI><B>FKTABLE_NAME</B> String =&gt; foreign key table name
     * <LI><B>FKCOLUMN_NAME</B> String =&gt; foreign key column name
     * <LI><B>KEY_SEQ</B> short =&gt; sequence number within a foreign key( a value
     * of 1 represents the first column of the foreign key, a value of 2 would
     * represent the second column within the foreign key).
     * <LI><B>UPDATE_RULE</B> short =&gt; What happens to a
     * foreign key when the primary key is updated:
     * <UL>
     * <LI> importedNoAction - do not allow update of primary
     * key if it has been imported
     * <LI> importedKeyCascade - change imported key to agree
     * with primary key update
     * <LI> importedKeySetNull - change imported key to <code>NULL</code>
     * if its primary key has been updated
     * <LI> importedKeySetDefault - change imported key to default values
     * if its primary key has been updated
     * <LI> importedKeyRestrict - same as importedKeyNoAction
     * (for ODBC 2.x compatibility)
     * </UL>
     * <LI><B>DELETE_RULE</B> short =&gt; What happens to
     * the foreign key when primary is deleted.
     * <UL>
     * <LI> importedKeyNoAction - do not allow delete of primary
     * key if it has been imported
     * <LI> importedKeyCascade - delete rows that import a deleted key
     * <LI> importedKeySetNull - change imported key to NULL if
     * its primary key has been deleted
     * <LI> importedKeyRestrict - same as importedKeyNoAction
     * (for ODBC 2.x compatibility)
     * <LI> importedKeySetDefault - change imported key to default if
     * its primary key has been deleted
     * </UL>
     * <LI><B>FK_NAME</B> String =&gt; foreign key name (may be <code>null</code>)
     * <LI><B>PK_NAME</B> String =&gt; primary key name (may be <code>null</code>)
     * <LI><B>DEFERRABILITY</B> short =&gt; can the evaluation of foreign key
     * constraints be deferred until commit
     * <UL>
     * <LI> importedKeyInitiallyDeferred - see SQL92 for definition
     * <LI> importedKeyInitiallyImmediate - see SQL92 for definition
     * <LI> importedKeyNotDeferrable - see SQL92 for definition
     * </UL>
     * </OL>
     *
     * @param catalog a catalog name; must match the catalog name as it
     *                is stored in the database; "" retrieves those without a catalog;
     *                <code>null</code> means that the catalog name should not be used to narrow
     *                the search
     * @param schema  a schema name; must match the schema name
     *                as it is stored in the database; "" retrieves those without a schema;
     *                <code>null</code> means that the schema name should not be used to narrow
     *                the search
     * @param table   a table name; must match the table name as it is stored
     *                in the database
     * @return <code>ResultSet</code> - each row is a primary key column description
     * @throws java.sql.SQLException if a database access error occurs
     * @see #getExportedKeys
     */
    @Override
    public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException
    {
        return getEmptyResultSet();
    }

    /**
     * Retrieves a description of the foreign key columns that reference the
     * given table's primary key columns (the foreign keys exported by a
     * table).  They are ordered by FKTABLE_CAT, FKTABLE_SCHEM,
     * FKTABLE_NAME, and KEY_SEQ.
     * 
     * <P>Each foreign key column description has the following columns:
     * <OL>
     * <LI><B>PKTABLE_CAT</B> String =&gt; primary key table catalog (may be <code>null</code>)
     * <LI><B>PKTABLE_SCHEM</B> String =&gt; primary key table schema (may be <code>null</code>)
     * <LI><B>PKTABLE_NAME</B> String =&gt; primary key table name
     * <LI><B>PKCOLUMN_NAME</B> String =&gt; primary key column name
     * <LI><B>FKTABLE_CAT</B> String =&gt; foreign key table catalog (may be <code>null</code>)
     * being exported (may be <code>null</code>)
     * <LI><B>FKTABLE_SCHEM</B> String =&gt; foreign key table schema (may be <code>null</code>)
     * being exported (may be <code>null</code>)
     * <LI><B>FKTABLE_NAME</B> String =&gt; foreign key table name
     * being exported
     * <LI><B>FKCOLUMN_NAME</B> String =&gt; foreign key column name
     * being exported
     * <LI><B>KEY_SEQ</B> short =&gt; sequence number within foreign key( a value
     * of 1 represents the first column of the foreign key, a value of 2 would
     * represent the second column within the foreign key).
     * <LI><B>UPDATE_RULE</B> short =&gt; What happens to
     * foreign key when primary is updated:
     * <UL>
     * <LI> importedNoAction - do not allow update of primary
     * key if it has been imported
     * <LI> importedKeyCascade - change imported key to agree
     * with primary key update
     * <LI> importedKeySetNull - change imported key to <code>NULL</code> if
     * its primary key has been updated
     * <LI> importedKeySetDefault - change imported key to default values
     * if its primary key has been updated
     * <LI> importedKeyRestrict - same as importedKeyNoAction
     * (for ODBC 2.x compatibility)
     * </UL>
     * <LI><B>DELETE_RULE</B> short =&gt; What happens to
     * the foreign key when primary is deleted.
     * <UL>
     * <LI> importedKeyNoAction - do not allow delete of primary
     * key if it has been imported
     * <LI> importedKeyCascade - delete rows that import a deleted key
     * <LI> importedKeySetNull - change imported key to <code>NULL</code> if
     * its primary key has been deleted
     * <LI> importedKeyRestrict - same as importedKeyNoAction
     * (for ODBC 2.x compatibility)
     * <LI> importedKeySetDefault - change imported key to default if
     * its primary key has been deleted
     * </UL>
     * <LI><B>FK_NAME</B> String =&gt; foreign key name (may be <code>null</code>)
     * <LI><B>PK_NAME</B> String =&gt; primary key name (may be <code>null</code>)
     * <LI><B>DEFERRABILITY</B> short =&gt; can the evaluation of foreign key
     * constraints be deferred until commit
     * <UL>
     * <LI> importedKeyInitiallyDeferred - see SQL92 for definition
     * <LI> importedKeyInitiallyImmediate - see SQL92 for definition
     * <LI> importedKeyNotDeferrable - see SQL92 for definition
     * </UL>
     * </OL>
     *
     * @param catalog a catalog name; must match the catalog name as it
     *                is stored in this database; "" retrieves those without a catalog;
     *                <code>null</code> means that the catalog name should not be used to narrow
     *                the search
     * @param schema  a schema name; must match the schema name
     *                as it is stored in the database; "" retrieves those without a schema;
     *                <code>null</code> means that the schema name should not be used to narrow
     *                the search
     * @param table   a table name; must match the table name as it is stored
     *                in this database
     * @return a <code>ResultSet</code> object in which each row is a
     * foreign key column description
     * @throws java.sql.SQLException if a database access error occurs
     * @see #getImportedKeys
     */
    @Override
    public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException
    {
        return getEmptyResultSet();
    }

    /**
     * Retrieves a description of the foreign key columns in the given foreign key
     * table that reference the primary key or the columns representing a unique constraint of the  parent table (could be the same or a different table).
     * The number of columns returned from the parent table must match the number of
     * columns that make up the foreign key.  They
     * are ordered by FKTABLE_CAT, FKTABLE_SCHEM, FKTABLE_NAME, and
     * KEY_SEQ.
     * 
     * <P>Each foreign key column description has the following columns:
     * <OL>
     * <LI><B>PKTABLE_CAT</B> String =&gt; parent key table catalog (may be <code>null</code>)
     * <LI><B>PKTABLE_SCHEM</B> String =&gt; parent key table schema (may be <code>null</code>)
     * <LI><B>PKTABLE_NAME</B> String =&gt; parent key table name
     * <LI><B>PKCOLUMN_NAME</B> String =&gt; parent key column name
     * <LI><B>FKTABLE_CAT</B> String =&gt; foreign key table catalog (may be <code>null</code>)
     * being exported (may be <code>null</code>)
     * <LI><B>FKTABLE_SCHEM</B> String =&gt; foreign key table schema (may be <code>null</code>)
     * being exported (may be <code>null</code>)
     * <LI><B>FKTABLE_NAME</B> String =&gt; foreign key table name
     * being exported
     * <LI><B>FKCOLUMN_NAME</B> String =&gt; foreign key column name
     * being exported
     * <LI><B>KEY_SEQ</B> short =&gt; sequence number within foreign key( a value
     * of 1 represents the first column of the foreign key, a value of 2 would
     * represent the second column within the foreign key).
     * <LI><B>UPDATE_RULE</B> short =&gt; What happens to
     * foreign key when parent key is updated:
     * <UL>
     * <LI> importedNoAction - do not allow update of parent
     * key if it has been imported
     * <LI> importedKeyCascade - change imported key to agree
     * with parent key update
     * <LI> importedKeySetNull - change imported key to <code>NULL</code> if
     * its parent key has been updated
     * <LI> importedKeySetDefault - change imported key to default values
     * if its parent key has been updated
     * <LI> importedKeyRestrict - same as importedKeyNoAction
     * (for ODBC 2.x compatibility)
     * </UL>
     * <LI><B>DELETE_RULE</B> short =&gt; What happens to
     * the foreign key when parent key is deleted.
     * <UL>
     * <LI> importedKeyNoAction - do not allow delete of parent
     * key if it has been imported
     * <LI> importedKeyCascade - delete rows that import a deleted key
     * <LI> importedKeySetNull - change imported key to <code>NULL</code> if
     * its primary key has been deleted
     * <LI> importedKeyRestrict - same as importedKeyNoAction
     * (for ODBC 2.x compatibility)
     * <LI> importedKeySetDefault - change imported key to default if
     * its parent key has been deleted
     * </UL>
     * <LI><B>FK_NAME</B> String =&gt; foreign key name (may be <code>null</code>)
     * <LI><B>PK_NAME</B> String =&gt; parent key name (may be <code>null</code>)
     * <LI><B>DEFERRABILITY</B> short =&gt; can the evaluation of foreign key
     * constraints be deferred until commit
     * <UL>
     * <LI> importedKeyInitiallyDeferred - see SQL92 for definition
     * <LI> importedKeyInitiallyImmediate - see SQL92 for definition
     * <LI> importedKeyNotDeferrable - see SQL92 for definition
     * </UL>
     * </OL>
     *
     * @param parentCatalog  a catalog name; must match the catalog name
     *                       as it is stored in the database; "" retrieves those without a
     *                       catalog; <code>null</code> means drop catalog name from the selection criteria
     * @param parentSchema   a schema name; must match the schema name as
     *                       it is stored in the database; "" retrieves those without a schema;
     *                       <code>null</code> means drop schema name from the selection criteria
     * @param parentTable    the name of the table that exports the key; must match
     *                       the table name as it is stored in the database
     * @param foreignCatalog a catalog name; must match the catalog name as
     *                       it is stored in the database; "" retrieves those without a
     *                       catalog; <code>null</code> means drop catalog name from the selection criteria
     * @param foreignSchema  a schema name; must match the schema name as it
     *                       is stored in the database; "" retrieves those without a schema;
     *                       <code>null</code> means drop schema name from the selection criteria
     * @param foreignTable   the name of the table that imports the key; must match
     *                       the table name as it is stored in the database
     * @return <code>ResultSet</code> - each row is a foreign key column description
     * @throws java.sql.SQLException if a database access error occurs
     * @see #getImportedKeys
     */
    @Override
    public ResultSet getCrossReference(String parentCatalog, String parentSchema, String parentTable, String foreignCatalog, String foreignSchema, String foreignTable) throws SQLException
    {
        return getEmptyResultSet();
    }

    /**
     * Retrieves a description of all the data types supported by
     * this database. They are ordered by DATA_TYPE and then by how
     * closely the data type maps to the corresponding JDBC SQL type.
     * 
     * <P>If the database supports SQL distinct types, then getTypeInfo() will return
     * a single row with a TYPE_NAME of DISTINCT and a DATA_TYPE of Types.DISTINCT.
     * If the database supports SQL structured types, then getTypeInfo() will return
     * a single row with a TYPE_NAME of STRUCT and a DATA_TYPE of Types.STRUCT.
     * 
     * <P>If SQL distinct or structured types are supported, then information on the
     * individual types may be obtained from the getUDTs() method.
     * 
     * 
     * 
     * <P>Each type description has the following columns:
     * <OL>
     * <LI><B>TYPE_NAME</B> String =&gt; Type name
     * <LI><B>DATA_TYPE</B> int =&gt; SQL data type from java.sql.Types
     * <LI><B>PRECISION</B> int =&gt; maximum precision
     * <LI><B>LITERAL_PREFIX</B> String =&gt; prefix used to quote a literal
     * (may be <code>null</code>)
     * <LI><B>LITERAL_SUFFIX</B> String =&gt; suffix used to quote a literal
     * (may be <code>null</code>)
     * <LI><B>CREATE_PARAMS</B> String =&gt; parameters used in creating
     * the type (may be <code>null</code>)
     * <LI><B>NULLABLE</B> short =&gt; can you use NULL for this type.
     * <UL>
     * <LI> typeNoNulls - does not allow NULL values
     * <LI> typeNullable - allows NULL values
     * <LI> typeNullableUnknown - nullability unknown
     * </UL>
     * <LI><B>CASE_SENSITIVE</B> boolean=&gt; is it case sensitive.
     * <LI><B>SEARCHABLE</B> short =&gt; can you use "WHERE" based on this type:
     * <UL>
     * <LI> typePredNone - No support
     * <LI> typePredChar - Only supported with WHERE .. LIKE
     * <LI> typePredBasic - Supported except for WHERE .. LIKE
     * <LI> typeSearchable - Supported for all WHERE ..
     * </UL>
     * <LI><B>UNSIGNED_ATTRIBUTE</B> boolean =&gt; is it unsigned.
     * <LI><B>FIXED_PREC_SCALE</B> boolean =&gt; can it be a money value.
     * <LI><B>AUTO_INCREMENT</B> boolean =&gt; can it be used for an
     * auto-increment value.
     * <LI><B>LOCAL_TYPE_NAME</B> String =&gt; localized version of type name
     * (may be <code>null</code>)
     * <LI><B>MINIMUM_SCALE</B> short =&gt; minimum scale supported
     * <LI><B>MAXIMUM_SCALE</B> short =&gt; maximum scale supported
     * <LI><B>SQL_DATA_TYPE</B> int =&gt; unused
     * <LI><B>SQL_DATETIME_SUB</B> int =&gt; unused
     * <LI><B>NUM_PREC_RADIX</B> int =&gt; usually 2 or 10
     * </OL>
     * 
     * <p>The PRECISION column represents the maximum column size that the server supports for the given datatype.
     * For numeric data, this is the maximum precision.  For character data, this is the length in characters.
     * For datetime datatypes, this is the length in characters of the String representation (assuming the
     * maximum allowed precision of the fractional seconds component). For binary data, this is the length in bytes.  For the ROWID datatype,
     * this is the length in bytes. Null is returned for data types where the
     * column size is not applicable.
     *
     * @return a <code>ResultSet</code> object in which each row is an SQL
     * type description
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public ResultSet getTypeInfo() throws SQLException
    {
        /*

            TYPE_NAME: NUMERIC, DATA_TYPE: -5, PRECISION: 19, LITERAL_PREFIX: null, LITERAL_SUFFIX: null, CREATE_PARAMS: null, NULLABLE: 1, CASE_SENSITIVE: 0, SEARCHABLE: 2, UNSIGNED_ATTRIBUTE: 0, FIXED_PREC_SCALE: 0, AUTO_INCREMENT: null, LOCAL_TYPE_NAME: NUMERIC, MINIMUM_SCALE: 0, MAXIMUM_SCALE: 0, SQL_DATA_TYPE: -5, SQL_DATETIME_SUB: null, NUM_PREC_RADIX: 10,
            TYPE_NAME: NULL, DATA_TYPE: 0, PRECISION: 0, LITERAL_PREFIX: ', LITERAL_SUFFIX: ', CREATE_PARAMS: LENGTH, NULLABLE: 1, CASE_SENSITIVE: 1, SEARCHABLE: 3, UNSIGNED_ATTRIBUTE: null, FIXED_PREC_SCALE: 0, AUTO_INCREMENT: null, LOCAL_TYPE_NAME: NULL, MINIMUM_SCALE: null, MAXIMUM_SCALE: null, SQL_DATA_TYPE: 0, SQL_DATETIME_SUB: null, NUM_PREC_RADIX: null,
            TYPE_NAME: NUMERIC, DATA_TYPE: 4, PRECISION: 10, LITERAL_PREFIX: null, LITERAL_SUFFIX: null, CREATE_PARAMS: null, NULLABLE: 1, CASE_SENSITIVE: 0, SEARCHABLE: 2, UNSIGNED_ATTRIBUTE: 0, FIXED_PREC_SCALE: 0, AUTO_INCREMENT: null, LOCAL_TYPE_NAME: NUMERIC, MINIMUM_SCALE: 0, MAXIMUM_SCALE: 0, SQL_DATA_TYPE: 4, SQL_DATETIME_SUB: null, NUM_PREC_RADIX: 10,
            TYPE_NAME: NUMERIC, DATA_TYPE: 8, PRECISION: 15, LITERAL_PREFIX: null, LITERAL_SUFFIX: null, CREATE_PARAMS: null, NULLABLE: 1, CASE_SENSITIVE: 0, SEARCHABLE: 2, UNSIGNED_ATTRIBUTE: 0, FIXED_PREC_SCALE: 0, AUTO_INCREMENT: null, LOCAL_TYPE_NAME: NUMERIC, MINIMUM_SCALE: null, MAXIMUM_SCALE: null, SQL_DATA_TYPE: 8, SQL_DATETIME_SUB: null, NUM_PREC_RADIX: 2,
            TYPE_NAME: ARRAY, DATA_TYPE: 12, PRECISION: 65500, LITERAL_PREFIX: ', LITERAL_SUFFIX: ', CREATE_PARAMS: max length, NULLABLE: 1, CASE_SENSITIVE: 1, SEARCHABLE: 3, UNSIGNED_ATTRIBUTE: null, FIXED_PREC_SCALE: 0, AUTO_INCREMENT: null, LOCAL_TYPE_NAME: ARRAY, MINIMUM_SCALE: null, MAXIMUM_SCALE: null, SQL_DATA_TYPE: 12, SQL_DATETIME_SUB: null, NUM_PREC_RADIX: null,
            TYPE_NAME: MISSING, DATA_TYPE: 12, PRECISION: 255, LITERAL_PREFIX: ', LITERAL_SUFFIX: ', CREATE_PARAMS: max length, NULLABLE: 1, CASE_SENSITIVE: 1, SEARCHABLE: 3, UNSIGNED_ATTRIBUTE: null, FIXED_PREC_SCALE: 0, AUTO_INCREMENT: null, LOCAL_TYPE_NAME: MISSING, MINIMUM_SCALE: null, MAXIMUM_SCALE: null, SQL_DATA_TYPE: 12, SQL_DATETIME_SUB: null, NUM_PREC_RADIX: null,
            TYPE_NAME: OBJECT, DATA_TYPE: 12, PRECISION: 65500, LITERAL_PREFIX: ', LITERAL_SUFFIX: ', CREATE_PARAMS: max length, NULLABLE: 1, CASE_SENSITIVE: 1, SEARCHABLE: 3, UNSIGNED_ATTRIBUTE: null, FIXED_PREC_SCALE: 0, AUTO_INCREMENT: null, LOCAL_TYPE_NAME: OBJECT, MINIMUM_SCALE: null, MAXIMUM_SCALE: null, SQL_DATA_TYPE: 12, SQL_DATETIME_SUB: null, NUM_PREC_RADIX: null,
            TYPE_NAME: STRING, DATA_TYPE: 12, PRECISION: 510, LITERAL_PREFIX: ', LITERAL_SUFFIX: ', CREATE_PARAMS: max length, NULLABLE: 1, CASE_SENSITIVE: 1, SEARCHABLE: 3, UNSIGNED_ATTRIBUTE: null, FIXED_PREC_SCALE: 0, AUTO_INCREMENT: null, LOCAL_TYPE_NAME: STRING, MINIMUM_SCALE: null, MAXIMUM_SCALE: null, SQL_DATA_TYPE: 12, SQL_DATETIME_SUB: null, NUM_PREC_RADIX: null,
            TYPE_NAME: BOOLEAN, DATA_TYPE: 16, PRECISION: 1, LITERAL_PREFIX: null, LITERAL_SUFFIX: null, CREATE_PARAMS: null, NULLABLE: 1, CASE_SENSITIVE: 0, SEARCHABLE: 2, UNSIGNED_ATTRIBUTE: null, FIXED_PREC_SCALE: 0, AUTO_INCREMENT: null, LOCAL_TYPE_NAME: BOOLEAN, MINIMUM_SCALE: null, MAXIMUM_SCALE: null, SQL_DATA_TYPE: 16, SQL_DATETIME_SUB: null, NUM_PREC_RADIX: null"
            */

        List <Map<String,Object>> types = new ArrayList<>(2);
        Map <String,Object>type = new HashMap<>(32);


        types.add(type);


        // TYPE_NAME: NUMERIC, DATA_TYPE: 2, PRECISION: 38, LITERAL_PREFIX: null, LITERAL_SUFFIX: null, CREATE_PARAMS: precision,scale, NULLABLE: 1, CASE_SENSITIVE: 0,
        // SEARCHABLE: 2, UNSIGNED_ATTRIBUTE: 0, FIXED_PREC_SCALE: 0, AUTO_INCREMENT: null, LOCAL_TYPE_NAME: NUMERIC, MINIMUM_SCALE: 0, MAXIMUM_SCALE: 38, SQL_DATA_TYPE: 2,
        // SQL_DATETIME_SUB: null, NUM_PREC_RADIX: 10,

        type.put("TYPE_NAME",       "NUMERIC");
        type.put("DATA_TYPE",       Types.NUMERIC);
        type.put("PRECISION",       38);
        type.put("LITERAL_PREFIX",  null);
        type.put("LITERAL_SUFFIX",  null);
        type.put("CREATE_PARMS",    "precision,scale");
        type.put("NULLABLE",        typeNullable);
        type.put("CASE_SENSITIVE",  false);
        type.put("SEARCHABLE",      typePredBasic);
        type.put("UNSIGNED_ATTRIBUTE", false);
        type.put("FIXED_PREC_SCALE", false);
        type.put("AUTO_INCREMENT",  null);
        type.put("LOCAL_TYPE_NAME", "NUMERIC");
        type.put("MINIMUM_SCALE",   0);
        type.put("MAXIMUM_SCALE",   38);
        type.put("SQL_DATA_TYPE",   2);
        type.put("SQL_DATETIME_SUB",null);
        type.put("NUM_PREC_RADIX",  10);

        types.add(type);
        type = new HashMap<>(32);

        //TYPE_NAME: NULL, DATA_TYPE: 0, PRECISION: 0, LITERAL_PREFIX: ', LITERAL_SUFFIX: ', CREATE_PARAMS: LENGTH, NULLABLE: 1, CASE_SENSITIVE: 1,
        // SEARCHABLE: 3, UNSIGNED_ATTRIBUTE: null, FIXED_PREC_SCALE: 0, AUTO_INCREMENT: null, LOCAL_TYPE_NAME: NULL, MINIMUM_SCALE: null, MAXIMUM_SCALE: null,
        // SQL_DATA_TYPE: 0,SQL_DATETIME_SUB: null, NUM_PREC_RADIX: null,

        type.put("TYPE_NAME",       "NULL");
        type.put("DATA_TYPE",       Types.NULL);
        type.put("PRECISION",       0);
        type.put("LITERAL_PREFIX",  "'");
        type.put("LITERAL_SUFFIX",  "'");
        type.put("CREATE_PARMS",    "length");
        type.put("NULLABLE",        typeNullable);
        type.put("CASE_SENSITIVE",  true);
        type.put("SEARCHABLE",      typeSearchable);
        type.put("UNSIGNED_ATTRIBUTE", null);
        type.put("FIXED_PREC_SCALE", 0);
        type.put("AUTO_INCREMENT",  null);
        type.put("LOCAL_TYPE_NAME", "NULL");
        type.put("MINIMUM_SCALE",   null);
        type.put("MAXIMUM_SCALE",   null);
        type.put("SQL_DATA_TYPE",   0);
        type.put("SQL_DATETIME_SUB",null);
        type.put("NUM_PREC_RADIX",  null);


        //TYPE_NAME: ARRAY, DATA_TYPE: 12, PRECISION: 65500, LITERAL_PREFIX: ', LITERAL_SUFFIX: ', CREATE_PARAMS: max length, NULLABLE: 1, CASE_SENSITIVE: 1,
        // SEARCHABLE: 3, UNSIGNED_ATTRIBUTE: null, FIXED_PREC_SCALE: 0, AUTO_INCREMENT: null, LOCAL_TYPE_NAME: ARRAY, MINIMUM_SCALE: null, MAXIMUM_SCALE: null,
        // SQL_DATA_TYPE: 12, SQL_DATETIME_SUB: null, NUM_PREC_RADIX: null,

        types.add(type);
        type = new HashMap<>(32);

        type.put("TYPE_NAME",       "ARRAY");
        type.put("DATA_TYPE",       Types.ARRAY);
        type.put("PRECISION",       65500);
        type.put("LITERAL_PREFIX",  "'");
        type.put("LITERAL_SUFFIX",  "'");
        type.put("CREATE_PARMS",    "max length");
        type.put("NULLABLE",        typeNullable);
        type.put("CASE_SENSITIVE",  true);
        type.put("SEARCHABLE",      typeSearchable);
        type.put("UNSIGNED_ATTRIBUTE", null);
        type.put("FIXED_PREC_SCALE", 0);
        type.put("AUTO_INCREMENT",  null);
        type.put("LOCAL_TYPE_NAME", "ARRAY");
        type.put("MINIMUM_SCALE",   null);
        type.put("MAXIMUM_SCALE",   null);
        type.put("SQL_DATA_TYPE",   Types.VARCHAR);
        type.put("SQL_DATETIME_SUB",null);
        type.put("NUM_PREC_RADIX",  null);

        types.add(type);
        type = new HashMap<>(32);

        // TYPE_NAME: MISSING, DATA_TYPE: 12, PRECISION: 255, LITERAL_PREFIX: ', LITERAL_SUFFIX: ', CREATE_PARAMS: max length, NULLABLE: 1, CASE_SENSITIVE: 1,
        // SEARCHABLE: 3, UNSIGNED_ATTRIBUTE: null, FIXED_PREC_SCALE: 0, AUTO_INCREMENT: null, LOCAL_TYPE_NAME: MISSING, MINIMUM_SCALE: null, MAXIMUM_SCALE: null,
        // SQL_DATA_TYPE: 12, SQL_DATETIME_SUB: null, NUM_PREC_RADIX: null,

        type.put("TYPE_NAME",       "MISSING");
        type.put("DATA_TYPE",       Types.VARCHAR);
        type.put("PRECISION",       255);
        type.put("LITERAL_PREFIX",  "'");
        type.put("LITERAL_SUFFIX",  "'");
        type.put("CREATE_PARMS",    "max length");
        type.put("NULLABLE",        typeNullable);
        type.put("CASE_SENSITIVE",  true);
        type.put("SEARCHABLE",      typeSearchable);
        type.put("UNSIGNED_ATTRIBUTE", null);
        type.put("FIXED_PREC_SCALE", 0);
        type.put("AUTO_INCREMENT",  null);
        type.put("LOCAL_TYPE_NAME", "MISSING");
        type.put("MINIMUM_SCALE",   null);
        type.put("MAXIMUM_SCALE",   null);
        type.put("SQL_DATA_TYPE",   Types.VARCHAR);
        type.put("SQL_DATETIME_SUB",null);
        type.put("NUM_PREC_RADIX",  null);

        types.add(type);
        type = new HashMap<>(32);

        // TYPE_NAME: OBJECT, DATA_TYPE: 12, PRECISION: 65500, LITERAL_PREFIX: ', LITERAL_SUFFIX: ', CREATE_PARAMS: max length, NULLABLE: 1, CASE_SENSITIVE: 1,
        // SEARCHABLE: 3, UNSIGNED_ATTRIBUTE: null, FIXED_PREC_SCALE: 0, AUTO_INCREMENT: null, LOCAL_TYPE_NAME: OBJECT, MINIMUM_SCALE: null, MAXIMUM_SCALE: null,
        // SQL_DATA_TYPE: 12, SQL_DATETIME_SUB: null, NUM_PREC_RADIX: null,

        type.put("TYPE_NAME",       "OBJECT");
        type.put("DATA_TYPE",       Types.VARCHAR);
        type.put("PRECISION",       65500);
        type.put("LITERAL_PREFIX",  "'");
        type.put("LITERAL_SUFFIX",  "'");
        type.put("CREATE_PARMS",    "max length");
        type.put("NULLABLE",        typeNullable);
        type.put("CASE_SENSITIVE",  true);
        type.put("SEARCHABLE",      typeSearchable);
        type.put("UNSIGNED_ATTRIBUTE", null);
        type.put("FIXED_PREC_SCALE", 0);
        type.put("AUTO_INCREMENT",  null);
        type.put("LOCAL_TYPE_NAME", "OBJECT");
        type.put("MINIMUM_SCALE",   null);
        type.put("MAXIMUM_SCALE",   null);
        type.put("SQL_DATA_TYPE",   Types.VARCHAR);
        type.put("SQL_DATETIME_SUB",null);
        type.put("NUM_PREC_RADIX",  null);

        types.add(type);
        type = new HashMap<>(32);

        // TYPE_NAME: STRING, DATA_TYPE: 12, PRECISION: 510, LITERAL_PREFIX: ', LITERAL_SUFFIX: ', CREATE_PARAMS: max length, NULLABLE: 1, CASE_SENSITIVE: 1,
        // SEARCHABLE: 3, UNSIGNED_ATTRIBUTE: null, FIXED_PREC_SCALE: 0, AUTO_INCREMENT: null, LOCAL_TYPE_NAME: STRING, MINIMUM_SCALE: null, MAXIMUM_SCALE: null,
        // SQL_DATA_TYPE: 12, SQL_DATETIME_SUB: null, NUM_PREC_RADIX: null,

        type.put("TYPE_NAME",       "STRING");
        type.put("DATA_TYPE",       Types.VARCHAR);
        type.put("PRECISION",       510);
        type.put("LITERAL_PREFIX",  "'");
        type.put("LITERAL_SUFFIX",  "'");
        type.put("CREATE_PARMS",    "max length");
        type.put("NULLABLE",        typeNullable);
        type.put("CASE_SENSITIVE",  true);
        type.put("SEARCHABLE",      typeSearchable);
        type.put("UNSIGNED_ATTRIBUTE", null);
        type.put("FIXED_PREC_SCALE", 0);
        type.put("AUTO_INCREMENT",  null);
        type.put("LOCAL_TYPE_NAME", "STRING");
        type.put("MINIMUM_SCALE",   null);
        type.put("MAXIMUM_SCALE",   null);
        type.put("SQL_DATA_TYPE",   Types.VARCHAR);
        type.put("SQL_DATETIME_SUB",null);
        type.put("NUM_PREC_RADIX",  null);

        types.add(type);
        type = new HashMap<>(32);

        // TYPE_NAME: BOOLEAN, DATA_TYPE: 16, PRECISION: 1, LITERAL_PREFIX: null, LITERAL_SUFFIX: null, CREATE_PARAMS: null, NULLABLE: 1, CASE_SENSITIVE: 0,
        // SEARCHABLE: 2, UNSIGNED_ATTRIBUTE: null, FIXED_PREC_SCALE: 0, AUTO_INCREMENT: null, LOCAL_TYPE_NAME: BOOLEAN, MINIMUM_SCALE: null, MAXIMUM_SCALE: null,
        // SQL_DATA_TYPE: 16, SQL_DATETIME_SUB: null, NUM_PREC_RADIX: null"

        type.put("TYPE_NAME",       "BOOLEAN");
        type.put("DATA_TYPE",       Types.BOOLEAN);
        type.put("PRECISION",       1);
        type.put("LITERAL_PREFIX",  null);
        type.put("LITERAL_SUFFIX",  null);
        type.put("CREATE_PARMS",    null);
        type.put("NULLABLE",        typeNullable);
        type.put("CASE_SENSITIVE",  false);
        type.put("SEARCHABLE",      typePredBasic);
        type.put("UNSIGNED_ATTRIBUTE", null);
        type.put("FIXED_PREC_SCALE", 0);
        type.put("AUTO_INCREMENT",  null);
        type.put("LOCAL_TYPE_NAME", "BOOLEAN");
        type.put("MINIMUM_SCALE",   null);
        type.put("MAXIMUM_SCALE",   null);
        type.put("SQL_DATA_TYPE",   Types.BOOLEAN);
        type.put("SQL_DATETIME_SUB",null);
        type.put("NUM_PREC_RADIX",  null);

        types.add(type);

        @SuppressWarnings("unchecked")
        Map <String, String> signature = new <String, String> HashMap();
        signature.put("TYPE_NAME","string");
        signature.put("DATA_TYPE","numeric");
        signature.put("PRECISION","numeric");
        signature.put("LITERAL_PREFIX","string");
        signature.put("LITERAL_SUFFIX","string");
        signature.put("CREATE_PARMS","string");
        signature.put("NULLABLE","numeric");
        signature.put("CASE_SENSITIVE","boolean");
        signature.put("SEARCHABLE","numeric");
        signature.put("UNSIGNED_ATTRIBUTE","boolean");
        signature.put("FIXED_PREC_SCALE","boolean");
        signature.put("AUTO_INCREMENT","boolean");
        signature.put("LOCAL_TYPE_NAME","string");
        signature.put("MINIMUM_SCALE","numeric");
        signature.put("MAXIMUM_SCALE","numeric");
        signature.put("SQL_DATA_TYPE","numeric");
        signature.put("SQL_DATETIME_SUB","numeric");
        signature.put("NUM_PREC_RADIX","numeric");

        CouchMetrics metrics = new CouchMetrics();
        metrics.setResultCount(types.size());
        // this just has to be not 0
        metrics.setResultSize(2);

        CouchResponse couchResponse = new CouchResponse();
        couchResponse.setResults(types);
        couchResponse.setMetrics(metrics);
        couchResponse.setSignature(signature);

        return new CBResultSet(null, couchResponse);

    }

    /**
     * Retrieves a description of the given table's indices and statistics. They are
     * ordered by NON_UNIQUE, TYPE, INDEX_NAME, and ORDINAL_POSITION.
     * 
     * <P>Each index column description has the following columns:
     * <OL>
     * <LI><B>TABLE_CAT</B> String =&gt; table catalog (may be <code>null</code>)
     * <LI><B>TABLE_SCHEM</B> String =&gt; table schema (may be <code>null</code>)
     * <LI><B>TABLE_NAME</B> String =&gt; table name
     * <LI><B>NON_UNIQUE</B> boolean =&gt; Can index values be non-unique.
     * false when TYPE is tableIndexStatistic
     * <LI><B>INDEX_QUALIFIER</B> String =&gt; index catalog (may be <code>null</code>);
     * <code>null</code> when TYPE is tableIndexStatistic
     * <LI><B>INDEX_NAME</B> String =&gt; index name; <code>null</code> when TYPE is
     * tableIndexStatistic
     * <LI><B>TYPE</B> short =&gt; index type:
     * <UL>
     * <LI> tableIndexStatistic - this identifies table statistics that are
     * returned in conjuction with a table's index descriptions
     * <LI> tableIndexClustered - this is a clustered index
     * <LI> tableIndexHashed - this is a hashed index
     * <LI> tableIndexOther - this is some other style of index
     * </UL>
     * <LI><B>ORDINAL_POSITION</B> short =&gt; column sequence number
     * within index; zero when TYPE is tableIndexStatistic
     * <LI><B>COLUMN_NAME</B> String =&gt; column name; <code>null</code> when TYPE is
     * tableIndexStatistic
     * <LI><B>ASC_OR_DESC</B> String =&gt; column sort sequence, "A" =&gt; ascending,
     * "D" =&gt; descending, may be <code>null</code> if sort sequence is not supported;
     * <code>null</code> when TYPE is tableIndexStatistic
     * <LI><B>CARDINALITY</B> int =&gt; When TYPE is tableIndexStatistic, then
     * this is the number of rows in the table; otherwise, it is the
     * number of unique values in the index.
     * <LI><B>PAGES</B> int =&gt; When TYPE is  tableIndexStatisic then
     * this is the number of pages used for the table, otherwise it
     * is the number of pages used for the current index.
     * <LI><B>FILTER_CONDITION</B> String =&gt; Filter condition, if any.
     * (may be <code>null</code>)
     * </OL>
     *
     * @param catalog     a catalog name; must match the catalog name as it
     *                    is stored in this database; "" retrieves those without a catalog;
     *                    <code>null</code> means that the catalog name should not be used to narrow
     *                    the search
     * @param schema      a schema name; must match the schema name
     *                    as it is stored in this database; "" retrieves those without a schema;
     *                    <code>null</code> means that the schema name should not be used to narrow
     *                    the search
     * @param table       a table name; must match the table name as it is stored
     *                    in this database
     * @param unique      when true, return only indices for unique values;
     *                    when false, return indices regardless of whether unique or not
     * @param approximate when true, result is allowed to reflect approximate
     *                    or out of data values; when false, results are requested to be
     *                    accurate
     * @return <code>ResultSet</code> - each row is an index column description
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate)
            throws SQLException {
        // We ignore the catalog parameter entirely. There is no such thing in Couchbase.
        // The schema parameter maps to "namespace_id", and we check that in the issued query if necessary.
        // The table parameter maps to "keyspace_id", and we check that in the issued query.
        // For unique = true, we return only primary indexes. For unique = false, we return all indexes.
        // We ignore the approximate parameter entirely.
        schema = StringUtil.stripBackquotes(schema);
        table = StringUtil.stripBackquotes(table);
        String sql = "select namespace_id, keyspace_id, name, index_key, is_primary from system:indexes where keyspace_id = (?)";
        if (schema != null) {
            sql += " and namespace_id = (?)";
        }
        if (unique) {
            sql += " and is_primary = true";
        }
        sql += " order by name";

        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, table);
        if (schema != null) {
            statement.setString(2, schema);
        }
        ResultSet rs = statement.executeQuery();

        List<Map<String, Object>> indexCols = new ArrayList<>(2);
        while (rs.next()) {
            Array indexKeysArray = rs.getArray("index_key");
            Object[] indexKeys = (Object[]) indexKeysArray.getArray();
            if (indexKeys == null || indexKeys.length == 0) {
                indexCols.add(createIndexInfoCol(rs.getString("namespace_id"), rs.getString("keyspace_id"), rs.getString("name"),
                        "meta().id", (short) 1, rs.getBoolean("is_primary")));
            } else {
                short ordinal = 1;
                for (Object o: indexKeys) {
                    indexCols.add(createIndexInfoCol(rs.getString("namespace_id"), rs.getString("keyspace_id"), rs.getString("name"),
                            o.toString(), ordinal, rs.getBoolean("is_primary")));
                    ordinal++;
                }
            }
        }

        Map<String, String> signature = new HashMap<>();
        signature.put("TABLE_CAT", "string");
        signature.put("TABLE_SCHEM", "string");
        signature.put("TABLE_NAME", "string");
        signature.put("NON_UNIQUE", "boolean");
        signature.put("INDEX_QUALIFIER", "string");
        signature.put("INDEX_NAME", "string");
        signature.put("TYPE", "numeric");
        signature.put("ORDINAL_POSITION", "numeric");
        signature.put("COLUMN_NAME", "numeric");
        signature.put("ASC_OR_DESC", "string");
        signature.put("CARDINALITY", "numeric");
        signature.put("PAGES", "numeric");
        signature.put("FILTER_CONDITION", "numeric");

        CouchMetrics metrics = new CouchMetrics();
        metrics.setResultCount(indexCols.size());
        // this just has to be not 0
        metrics.setResultSize(2);

        CouchResponse couchResponse = new CouchResponse();
        couchResponse.setResults(indexCols);
        couchResponse.setMetrics(metrics);
        couchResponse.setSignature(signature);

        return new CBResultSet(null, couchResponse);
    }

    static private Map<String, Object> createIndexInfoCol(String namespaceId, String keyspaceId, String name, String columnName, short ordinal, boolean isPrimary) throws SQLException {
        Map<String, Object> indexCol = new HashMap<>(13);
        indexCol.put("TABLE_CAT", "");
        indexCol.put("TABLE_SCHEM", namespaceId);
        indexCol.put("TABLE_NAME", keyspaceId);
        indexCol.put("NON_UNIQUE", !isPrimary);
        indexCol.put("INDEX_QUALIFIER", null);
        indexCol.put("INDEX_NAME", name);
        indexCol.put("TYPE", tableIndexHashed);
        indexCol.put("ORDINAL_POSITION", ordinal);
        indexCol.put("COLUMN_NAME", StringUtil.stripBackquotes(columnName));
        indexCol.put("ASC_OR_DESC", "A");
        indexCol.put("CARDINALITY", 0);
        indexCol.put("PAGES", 0);
        indexCol.put("FILTER_CONDITION", null);
        return indexCol;
    }

    /**
     * Retrieves whether this database supports the given result set type.
     *
     * @param type defined in <code>java.sql.ResultSet</code>
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     * @see Connection
     * @since 1.2
     */
    @Override
    public boolean supportsResultSetType(int type) throws SQLException
    {
        return type == ResultSet.TYPE_FORWARD_ONLY;
    }

    /**
     * Retrieves whether this database supports the given concurrency type
     * in combination with the given result set type.
     *
     * @param type        defined in <code>java.sql.ResultSet</code>
     * @param concurrency type defined in <code>java.sql.ResultSet</code>
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     * @see Connection
     * @since 1.2
     */
    @Override
    public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether for the given type of <code>ResultSet</code> object,
     * the result set's own updates are visible.
     *
     * @param type the <code>ResultSet</code> type; one of
     *             <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *             <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *             <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return <code>true</code> if updates are visible for the given result set type;
     * <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     * @since 1.2
     */
    @Override
    public boolean ownUpdatesAreVisible(int type) throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether a result set's own deletes are visible.
     *
     * @param type the <code>ResultSet</code> type; one of
     *             <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *             <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *             <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return <code>true</code> if deletes are visible for the given result set type;
     * <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     * @since 1.2
     */
    @Override
    public boolean ownDeletesAreVisible(int type) throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether a result set's own inserts are visible.
     *
     * @param type the <code>ResultSet</code> type; one of
     *             <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *             <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *             <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return <code>true</code> if inserts are visible for the given result set type;
     * <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     * @since 1.2
     */
    @Override
    public boolean ownInsertsAreVisible(int type) throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether updates made by others are visible.
     *
     * @param type the <code>ResultSet</code> type; one of
     *             <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *             <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *             <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return <code>true</code> if updates made by others
     * are visible for the given result set type;
     * <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     * @since 1.2
     */
    @Override
    public boolean othersUpdatesAreVisible(int type) throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether deletes made by others are visible.
     *
     * @param type the <code>ResultSet</code> type; one of
     *             <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *             <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *             <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return <code>true</code> if deletes made by others
     * are visible for the given result set type;
     * <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     * @since 1.2
     */
    @Override
    public boolean othersDeletesAreVisible(int type) throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether inserts made by others are visible.
     *
     * @param type the <code>ResultSet</code> type; one of
     *             <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *             <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *             <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return <code>true</code> if inserts made by others
     * are visible for the given result set type;
     * <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     * @since 1.2
     */
    @Override
    public boolean othersInsertsAreVisible(int type) throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether or not a visible row update can be detected by
     * calling the method <code>ResultSet.rowUpdated</code>.
     *
     * @param type the <code>ResultSet</code> type; one of
     *             <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *             <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *             <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return <code>true</code> if changes are detected by the result set type;
     * <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     * @since 1.2
     */
    @Override
    public boolean updatesAreDetected(int type) throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether or not a visible row delete can be detected by
     * calling the method <code>ResultSet.rowDeleted</code>.  If the method
     * <code>deletesAreDetected</code> returns <code>false</code>, it means that
     * deleted rows are removed from the result set.
     *
     * @param type the <code>ResultSet</code> type; one of
     *             <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *             <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *             <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return <code>true</code> if deletes are detected by the given result set type;
     * <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     * @since 1.2
     */
    @Override
    public boolean deletesAreDetected(int type) throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether or not a visible row insert can be detected
     * by calling the method <code>ResultSet.rowInserted</code>.
     *
     * @param type the <code>ResultSet</code> type; one of
     *             <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *             <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *             <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return <code>true</code> if changes are detected by the specified result
     * set type; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     * @since 1.2
     */
    @Override
    public boolean insertsAreDetected(int type) throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether this database supports batch updates.
     *
     * @return <code>true</code> if this database supports batch upcates;
     * <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     * @since 1.2
     */
    @Override
    public boolean supportsBatchUpdates() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves a description of the user-defined types (UDTs) defined
     * in a particular schema.  Schema-specific UDTs may have type
     * <code>JAVA_OBJECT</code>, <code>STRUCT</code>,
     * or <code>DISTINCT</code>.
     * 
     * <P>Only types matching the catalog, schema, type name and type
     * criteria are returned.  They are ordered by <code>DATA_TYPE</code>,
     * <code>TYPE_CAT</code>, <code>TYPE_SCHEM</code>  and
     * <code>TYPE_NAME</code>.  The type name parameter may be a fully-qualified
     * name.  In this case, the catalog and schemaPattern parameters are
     * ignored.
     * 
     * <P>Each type description has the following columns:
     * <OL>
     * <LI><B>TYPE_CAT</B> String =&gt; the type's catalog (may be <code>null</code>)
     * <LI><B>TYPE_SCHEM</B> String =&gt; type's schema (may be <code>null</code>)
     * <LI><B>TYPE_NAME</B> String =&gt; type name
     * <LI><B>CLASS_NAME</B> String =&gt; Java class name
     * <LI><B>DATA_TYPE</B> int =&gt; type value defined in java.sql.Types.
     * One of JAVA_OBJECT, STRUCT, or DISTINCT
     * <LI><B>REMARKS</B> String =&gt; explanatory comment on the type
     * <LI><B>BASE_TYPE</B> short =&gt; type code of the source type of a
     * DISTINCT type or the type that implements the user-generated
     * reference type of the SELF_REFERENCING_COLUMN of a structured
     * type as defined in java.sql.Types (<code>null</code> if DATA_TYPE is not
     * DISTINCT or not STRUCT with REFERENCE_GENERATION = USER_DEFINED)
     * </OL>
     * 
     * <P><B>Note:</B> If the driver does not support UDTs, an empty
     * result set is returned.
     *
     * @param catalog         a catalog name; must match the catalog name as it
     *                        is stored in the database; "" retrieves those without a catalog;
     *                        <code>null</code> means that the catalog name should not be used to narrow
     *                        the search
     * @param schemaPattern   a schema pattern name; must match the schema name
     *                        as it is stored in the database; "" retrieves those without a schema;
     *                        <code>null</code> means that the schema name should not be used to narrow
     *                        the search
     * @param typeNamePattern a type name pattern; must match the type name
     *                        as it is stored in the database; may be a fully qualified name
     * @param types           a list of user-defined types (JAVA_OBJECT,
     *                        STRUCT, or DISTINCT) to include; <code>null</code> returns all types
     * @return <code>ResultSet</code> object in which each row describes a UDT
     * @throws java.sql.SQLException if a database access error occurs
     * @see #getSearchStringEscape
     * @since 1.2
     */
    @Override
    public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types) throws SQLException
    {
        List <Map<String,String>> udts = new ArrayList<>(2);



        CouchMetrics metrics = new CouchMetrics();
        metrics.setResultCount(udts.size());
        // this just has to be not 0
        metrics.setResultSize(1);

        @SuppressWarnings("unchecked") Map <String, String>signature = new <String,String>HashMap();


        CouchResponse couchResponse = new CouchResponse();
        couchResponse.setResults(udts);
        couchResponse.setMetrics(metrics);
        couchResponse.setSignature(signature);

        return new CBResultSet(null, couchResponse);
    }

    /**
     * Retrieves the connection that produced this metadata object.
     * 
     *
     * @return the connection that produced this metadata object
     * @throws java.sql.SQLException if a database access error occurs
     * @since 1.2
     */
    @Override
    public Connection getConnection() throws SQLException
    {
        return  connection;
    }

    /**
     * Retrieves whether this database supports savepoints.
     *
     * @return <code>true</code> if savepoints are supported;
     * <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     * @since 1.4
     */
    @Override
    public boolean supportsSavepoints() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether this database supports named parameters to callable
     * statements.
     *
     * @return <code>true</code> if named parameters are supported;
     * <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     * @since 1.4
     */
    @Override
    public boolean supportsNamedParameters() throws SQLException
    {
        return true;
    }

    /**
     * Retrieves whether it is possible to have multiple <code>ResultSet</code> objects
     * returned from a <code>CallableStatement</code> object
     * simultaneously.
     *
     * @return <code>true</code> if a <code>CallableStatement</code> object
     * can return multiple <code>ResultSet</code> objects
     * simultaneously; <code>false</code> otherwise
     * @throws java.sql.SQLException if a datanase access error occurs
     * @since 1.4
     */
    @Override
    public boolean supportsMultipleOpenResults() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether auto-generated keys can be retrieved after
     * a statement has been executed
     *
     * @return <code>true</code> if auto-generated keys can be retrieved
     * after a statement has executed; <code>false</code> otherwise
     * <p>If <code>true</code> is returned, the JDBC driver must support the
     * returning of auto-generated keys for at least SQL INSERT statements
     * 
     * @throws java.sql.SQLException if a database access error occurs
     * @since 1.4
     */
    @Override
    public boolean supportsGetGeneratedKeys() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves a description of the user-defined type (UDT) hierarchies defined in a
     * particular schema in this database. Only the immediate super type/
     * sub type relationship is modeled.
     * 
     * Only supertype information for UDTs matching the catalog,
     * schema, and type name is returned. The type name parameter
     * may be a fully-qualified name. When the UDT name supplied is a
     * fully-qualified name, the catalog and schemaPattern parameters are
     * ignored.
     * 
     * If a UDT does not have a direct super type, it is not listed here.
     * A row of the <code>ResultSet</code> object returned by this method
     * describes the designated UDT and a direct supertype. A row has the following
     * columns:
     * <OL>
     * <LI><B>TYPE_CAT</B> String =&gt; the UDT's catalog (may be <code>null</code>)
     * <LI><B>TYPE_SCHEM</B> String =&gt; UDT's schema (may be <code>null</code>)
     * <LI><B>TYPE_NAME</B> String =&gt; type name of the UDT
     * <LI><B>SUPERTYPE_CAT</B> String =&gt; the direct super type's catalog
     * (may be <code>null</code>)
     * <LI><B>SUPERTYPE_SCHEM</B> String =&gt; the direct super type's schema
     * (may be <code>null</code>)
     * <LI><B>SUPERTYPE_NAME</B> String =&gt; the direct super type's name
     * </OL>
     * 
     * <P><B>Note:</B> If the driver does not support type hierarchies, an
     * empty result set is returned.
     *
     * @param catalog         a catalog name; "" retrieves those without a catalog;
     *                        <code>null</code> means drop catalog name from the selection criteria
     * @param schemaPattern   a schema name pattern; "" retrieves those
     *                        without a schema
     * @param typeNamePattern a UDT name pattern; may be a fully-qualified
     *                        name
     * @return a <code>ResultSet</code> object in which a row gives information
     * about the designated UDT
     * @throws java.sql.SQLException if a database access error occurs
     * @see #getSearchStringEscape
     * @since 1.4
     */
    @Override
    public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern) throws SQLException
    {
        return getEmptyResultSet();
    }

    /**
     * Retrieves a description of the table hierarchies defined in a particular
     * schema in this database.
     * 
     * <P>Only supertable information for tables matching the catalog, schema
     * and table name are returned. The table name parameter may be a fully-
     * qualified name, in which case, the catalog and schemaPattern parameters
     * are ignored. If a table does not have a super table, it is not listed here.
     * Supertables have to be defined in the same catalog and schema as the
     * sub tables. Therefore, the type description does not need to include
     * this information for the supertable.
     * 
     * <P>Each type description has the following columns:
     * <OL>
     * <LI><B>TABLE_CAT</B> String =&gt; the type's catalog (may be <code>null</code>)
     * <LI><B>TABLE_SCHEM</B> String =&gt; type's schema (may be <code>null</code>)
     * <LI><B>TABLE_NAME</B> String =&gt; type name
     * <LI><B>SUPERTABLE_NAME</B> String =&gt; the direct super type's name
     * </OL>
     * 
     * <P><B>Note:</B> If the driver does not support type hierarchies, an
     * empty result set is returned.
     *
     * @param catalog          a catalog name; "" retrieves those without a catalog;
     *                         <code>null</code> means drop catalog name from the selection criteria
     * @param schemaPattern    a schema name pattern; "" retrieves those
     *                         without a schema
     * @param tableNamePattern a table name pattern; may be a fully-qualified
     *                         name
     * @return a <code>ResultSet</code> object in which each row is a type description
     * @throws java.sql.SQLException if a database access error occurs
     * @see #getSearchStringEscape
     * @since 1.4
     */
    @Override
    public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern) throws SQLException
    {
        return getEmptyResultSet();
    }

    /**
     * Retrieves a description of the given attribute of the given type
     * for a user-defined type (UDT) that is available in the given schema
     * and catalog.
     * 
     * Descriptions are returned only for attributes of UDTs matching the
     * catalog, schema, type, and attribute name criteria. They are ordered by
     * <code>TYPE_CAT</code>, <code>TYPE_SCHEM</code>,
     * <code>TYPE_NAME</code> and <code>ORDINAL_POSITION</code>. This description
     * does not contain inherited attributes.
     * 
     * The <code>ResultSet</code> object that is returned has the following
     * columns:
     * <OL>
     * <LI><B>TYPE_CAT</B> String =&gt; type catalog (may be <code>null</code>)
     * <LI><B>TYPE_SCHEM</B> String =&gt; type schema (may be <code>null</code>)
     * <LI><B>TYPE_NAME</B> String =&gt; type name
     * <LI><B>ATTR_NAME</B> String =&gt; attribute name
     * <LI><B>DATA_TYPE</B> int =&gt; attribute type SQL type from java.sql.Types
     * <LI><B>ATTR_TYPE_NAME</B> String =&gt; Data source dependent type name.
     * For a UDT, the type name is fully qualified. For a REF, the type name is
     * fully qualified and represents the target type of the reference type.
     * <LI><B>ATTR_SIZE</B> int =&gt; column size.  For char or date
     * types this is the maximum number of characters; for numeric or
     * decimal types this is precision.
     * <LI><B>DECIMAL_DIGITS</B> int =&gt; the number of fractional digits. Null is returned for data types where
     * DECIMAL_DIGITS is not applicable.
     * <LI><B>NUM_PREC_RADIX</B> int =&gt; Radix (typically either 10 or 2)
     * <LI><B>NULLABLE</B> int =&gt; whether NULL is allowed
     * <UL>
     * <LI> attributeNoNulls - might not allow NULL values
     * <LI> attributeNullable - definitely allows NULL values
     * <LI> attributeNullableUnknown - nullability unknown
     * </UL>
     * <LI><B>REMARKS</B> String =&gt; comment describing column (may be <code>null</code>)
     * <LI><B>ATTR_DEF</B> String =&gt; default value (may be <code>null</code>)
     * <LI><B>SQL_DATA_TYPE</B> int =&gt; unused
     * <LI><B>SQL_DATETIME_SUB</B> int =&gt; unused
     * <LI><B>CHAR_OCTET_LENGTH</B> int =&gt; for char types the
     * maximum number of bytes in the column
     * <LI><B>ORDINAL_POSITION</B> int =&gt; index of the attribute in the UDT
     * (starting at 1)
     * <LI><B>IS_NULLABLE</B> String  =&gt; ISO rules are used to determine
     * the nullability for a attribute.
     * <UL>
     * <LI> YES           --- if the attribute can include NULLs
     * <LI> NO            --- if the attribute cannot include NULLs
     * <LI> empty string  --- if the nullability for the
     * attribute is unknown
     * </UL>
     * <LI><B>SCOPE_CATALOG</B> String =&gt; catalog of table that is the
     * scope of a reference attribute (<code>null</code> if DATA_TYPE isn't REF)
     * <LI><B>SCOPE_SCHEMA</B> String =&gt; schema of table that is the
     * scope of a reference attribute (<code>null</code> if DATA_TYPE isn't REF)
     * <LI><B>SCOPE_TABLE</B> String =&gt; table name that is the scope of a
     * reference attribute (<code>null</code> if the DATA_TYPE isn't REF)
     * <LI><B>SOURCE_DATA_TYPE</B> short =&gt; source type of a distinct type or user-generated
     * Ref type,SQL type from java.sql.Types (<code>null</code> if DATA_TYPE
     * isn't DISTINCT or user-generated REF)
     * </OL>
     *
     * @param catalog              a catalog name; must match the catalog name as it
     *                             is stored in the database; "" retrieves those without a catalog;
     *                             <code>null</code> means that the catalog name should not be used to narrow
     *                             the search
     * @param schemaPattern        a schema name pattern; must match the schema name
     *                             as it is stored in the database; "" retrieves those without a schema;
     *                             <code>null</code> means that the schema name should not be used to narrow
     *                             the search
     * @param typeNamePattern      a type name pattern; must match the
     *                             type name as it is stored in the database
     * @param attributeNamePattern an attribute name pattern; must match the attribute
     *                             name as it is declared in the database
     * @return a <code>ResultSet</code> object in which each row is an
     * attribute description
     * @throws java.sql.SQLException if a database access error occurs
     * @see #getSearchStringEscape
     * @since 1.4
     */
    @Override
    public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern, String attributeNamePattern) throws SQLException
    {
        return getEmptyResultSet();
    }

    /**
     * Retrieves whether this database supports the given result set holdability.
     *
     * @param holdability one of the following constants:
     *                    <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code> or
     *                    <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     * @see java.sql.Connection
     * @since 1.4
     */
    @Override
    public boolean supportsResultSetHoldability(int holdability) throws SQLException
    {
        return false;
    }

    /**
     * Retrieves this database's default holdability for <code>ResultSet</code>
     * objects.
     *
     * @return the default holdability; either
     * <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code> or
     * <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
     * @throws java.sql.SQLException if a database access error occurs
     * @since 1.4
     */
    @Override
    public int getResultSetHoldability() throws SQLException
    {
        return ResultSet.HOLD_CURSORS_OVER_COMMIT;
    }

    /**
     * Retrieves the major version number of the underlying database.
     *
     * @return the underlying database's major version
     * @throws java.sql.SQLException if a database access error occurs
     * @since 1.4
     */
    @Override
    public int getDatabaseMajorVersion() throws SQLException
    {
        return 1;
    }

    /**
     * Retrieves the minor version number of the underlying database.
     *
     * @return underlying database's minor version
     * @throws java.sql.SQLException if a database access error occurs
     * @since 1.4
     */
    @Override
    public int getDatabaseMinorVersion() throws SQLException
    {
        return 1;
    }

    /**
     * Retrieves the major JDBC version number for this
     * driver.
     *
     * @return JDBC version major number
     * @throws java.sql.SQLException if a database access error occurs
     * @since 1.4
     */
    @Override
    public int getJDBCMajorVersion() throws SQLException
    {
        return 4;
    }

    /**
     * Retrieves the minor JDBC version number for this
     * driver.
     *
     * @return JDBC version minor number
     * @throws java.sql.SQLException if a database access error occurs
     * @since 1.4
     */
    @Override
    public int getJDBCMinorVersion() throws SQLException
    {
        return (Integer.parseInt(System.getProperty("java.specification.version").split("\\.")[1]) == 8) ? 2:1;
    }

    /**
     * Indicates whether the SQLSTATE returned by <code>SQLException.getSQLState</code>
     * is X/Open (now known as Open Group) SQL CLI or SQL:2003.
     *
     * @return the type of SQLSTATE; one of:
     * sqlStateXOpen or
     * sqlStateSQL
     * @throws java.sql.SQLException if a database access error occurs
     * @since 1.4
     */
    @Override
    public int getSQLStateType() throws SQLException
    {
        return sqlStateSQL;
    }

    /**
     * Indicates whether updates made to a LOB are made on a copy or directly
     * to the LOB.
     *
     * @return <code>true</code> if updates are made to a copy of the LOB;
     * <code>false</code> if updates are made directly to the LOB
     * @throws java.sql.SQLException if a database access error occurs
     * @since 1.4
     */
    @Override
    public boolean locatorsUpdateCopy() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether this database supports statement pooling.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     * @since 1.4
     */
    @Override
    public boolean supportsStatementPooling() throws SQLException
    {
        return false;
    }

    /**
     * Indicates whether or not this data source supports the SQL <code>ROWID</code> type,
     * and if so  the lifetime for which a <code>RowId</code> object remains valid.
     * 
     * The returned int values have the following relationship:
     * <pre>
     *     ROWID_UNSUPPORTED &lt; ROWID_VALID_OTHER &lt; ROWID_VALID_TRANSACTION
     *         &lt; ROWID_VALID_SESSION &lt; ROWID_VALID_FOREVER
     * </pre>
     * so conditional logic such as
     * <pre>
     *     if (metadata.getRowIdLifetime() &gt; DatabaseMetaData.ROWID_VALID_TRANSACTION)
     * </pre>
     * can be used. Valid Forever means valid across all Sessions, and valid for
     * a Session means valid across all its contained Transactions.
     *
     * @return the status indicating the lifetime of a <code>RowId</code>
     * @throws java.sql.SQLException if a database access error occurs
     * @since 1.6
     */
    @Override
    public RowIdLifetime getRowIdLifetime() throws SQLException
    {
        return RowIdLifetime.ROWID_UNSUPPORTED;
    }

    /**
     * Retrieves the schema names available in this database.  The results
     * are ordered by <code>TABLE_CATALOG</code> and
     * <code>TABLE_SCHEM</code>.
     * 
     * <P>The schema columns are:
     * <OL>
     * <LI><B>TABLE_SCHEM</B> String =&gt; schema name
     * <LI><B>TABLE_CATALOG</B> String =&gt; catalog name (may be <code>null</code>)
     * </OL>
     *
     * @param catalog       a catalog name; must match the catalog name as it is stored
     *                      in the database;"" retrieves those without a catalog; null means catalog
     *                      name should not be used to narrow down the search.
     * @param schemaPattern a schema name; must match the schema name as it is
     *                      stored in the database; null means
     *                      schema name should not be used to narrow down the search.
     * @return a <code>ResultSet</code> object in which each row is a
     * schema description
     * @throws java.sql.SQLException if a database access error occurs
     * @see #getSearchStringEscape
     * @since 1.6
     */
    @Override
    public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException
    {

        String sql = "select name as TABLE_SCHEM, 'Couchbase' as TABLE_CATALOG from system:namespaces";
        if (schemaPattern != null)
        {
            sql += " where name like '" + schemaPattern +"'";
        }
        try( Statement statement = connection.createStatement())
        {
            return statement.executeQuery( sql );
        }
    }

    /**
     * Retrieves whether this database supports invoking user-defined or vendor functions
     * using the stored procedure escape syntax.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     * @since 1.6
     */
    @Override
    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves whether a <code>SQLException</code> while autoCommit is <code>true</code> inidcates
     * that all open ResultSets are closed, even ones that are holdable.  When a <code>SQLException</code> occurs while
     * autocommit is <code>true</code>, it is vendor specific whether the JDBC driver responds with a commit operation, a
     * rollback operation, or by doing neither a commit nor a rollback.  A potential result of this difference
     * is in whether or not holdable ResultSets are closed.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     * @since 1.6
     */
    @Override
    public boolean autoCommitFailureClosesAllResultSets() throws SQLException
    {
        return false;
    }

    /**
     * Retrieves a list of the client info properties
     * that the driver supports.  The result set contains the following columns
     * 
     * <ol>
     * <li><b>NAME</b> String=&gt; The name of the client info property<br>
     * <li><b>MAX_LEN</b> int=&gt; The maximum length of the value for the property<br>
     * <li><b>DEFAULT_VALUE</b> String=&gt; The default value of the property<br>
     * <li><b>DESCRIPTION</b> String=&gt; A description of the property.  This will typically
     * contain information as to where this property is
     * stored in the database.
     * </ol>
     * 
     * The <code>ResultSet</code> is sorted by the NAME column
     * 
     *
     * @return A <code>ResultSet</code> object; each row is a supported client info
     * property
     * 
     * @throws java.sql.SQLException if a database access error occurs
     *                               
     * @since 1.6
     */
    @Override
    public ResultSet getClientInfoProperties() throws SQLException
    {
        return getEmptyResultSet();
    }

    /**
     * Retrieves a description of the  system and user functions available
     * in the given catalog.
     * 
     * Only system and user function descriptions matching the schema and
     * function name criteria are returned.  They are ordered by
     * <code>FUNCTION_CAT</code>, <code>FUNCTION_SCHEM</code>,
     * <code>FUNCTION_NAME</code> and
     * <code>SPECIFIC_ NAME</code>.
     * 
     * <P>Each function description has the the following columns:
     * <OL>
     * <LI><B>FUNCTION_CAT</B> String =&gt; function catalog (may be <code>null</code>)
     * <LI><B>FUNCTION_SCHEM</B> String =&gt; function schema (may be <code>null</code>)
     * <LI><B>FUNCTION_NAME</B> String =&gt; function name.  This is the name
     * used to invoke the function
     * <LI><B>REMARKS</B> String =&gt; explanatory comment on the function
     * <LI><B>FUNCTION_TYPE</B> short =&gt; kind of function:
     * <UL>
     * <LI>functionResultUnknown - Cannot determine if a return value
     * or table will be returned
     * <LI> functionNoTable- Does not return a table
     * <LI> functionReturnsTable - Returns a table
     * </UL>
     * <LI><B>SPECIFIC_NAME</B> String  =&gt; the name which uniquely identifies
     * this function within its schema.  This is a user specified, or DBMS
     * generated, name that may be different then the <code>FUNCTION_NAME</code>
     * for example with overload functions
     * </OL>
     * 
     * A user may not have permission to execute any of the functions that are
     * returned by <code>getFunctions</code>
     *
     * @param catalog             a catalog name; must match the catalog name as it
     *                            is stored in the database; "" retrieves those without a catalog;
     *                            <code>null</code> means that the catalog name should not be used to narrow
     *                            the search
     * @param schemaPattern       a schema name pattern; must match the schema name
     *                            as it is stored in the database; "" retrieves those without a schema;
     *                            <code>null</code> means that the schema name should not be used to narrow
     *                            the search
     * @param functionNamePattern a function name pattern; must match the
     *                            function name as it is stored in the database
     * @return <code>ResultSet</code> - each row is a function description
     * @throws java.sql.SQLException if a database access error occurs
     * @see #getSearchStringEscape
     * @since 1.6
     */
    @Override
    public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern) throws SQLException
    {
        return getEmptyResultSet();
    }

    /**
     * Retrieves a description of the given catalog's system or user
     * function parameters and return type.
     * 
     * <P>Only descriptions matching the schema,  function and
     * parameter name criteria are returned. They are ordered by
     * <code>FUNCTION_CAT</code>, <code>FUNCTION_SCHEM</code>,
     * <code>FUNCTION_NAME</code> and
     * <code>SPECIFIC_ NAME</code>. Within this, the return value,
     * if any, is first. Next are the parameter descriptions in call
     * order. The column descriptions follow in column number order.
     * 
     * <P>Each row in the <code>ResultSet</code>
     * is a parameter description, column description or
     * return type description with the following fields:
     * <OL>
     * <LI><B>FUNCTION_CAT</B> String =&gt; function catalog (may be <code>null</code>)
     * <LI><B>FUNCTION_SCHEM</B> String =&gt; function schema (may be <code>null</code>)
     * <LI><B>FUNCTION_NAME</B> String =&gt; function name.  This is the name
     * used to invoke the function
     * <LI><B>COLUMN_NAME</B> String =&gt; column/parameter name
     * <LI><B>COLUMN_TYPE</B> Short =&gt; kind of column/parameter:
     * <UL>
     * <LI> functionColumnUnknown - nobody knows
     * <LI> functionColumnIn - IN parameter
     * <LI> functionColumnInOut - INOUT parameter
     * <LI> functionColumnOut - OUT parameter
     * <LI> functionColumnReturn - function return value
     * <LI> functionColumnResult - Indicates that the parameter or column
     * is a column in the <code>ResultSet</code>
     * </UL>
     * <LI><B>DATA_TYPE</B> int =&gt; SQL type from java.sql.Types
     * <LI><B>TYPE_NAME</B> String =&gt; SQL type name, for a UDT type the
     * type name is fully qualified
     * <LI><B>PRECISION</B> int =&gt; precision
     * <LI><B>LENGTH</B> int =&gt; length in bytes of data
     * <LI><B>SCALE</B> short =&gt; scale -  null is returned for data types where
     * SCALE is not applicable.
     * <LI><B>RADIX</B> short =&gt; radix
     * <LI><B>NULLABLE</B> short =&gt; can it contain NULL.
     * <UL>
     * <LI> functionNoNulls - does not allow NULL values
     * <LI> functionNullable - allows NULL values
     * <LI> functionNullableUnknown - nullability unknown
     * </UL>
     * <LI><B>REMARKS</B> String =&gt; comment describing column/parameter
     * <LI><B>CHAR_OCTET_LENGTH</B> int  =&gt; the maximum length of binary
     * and character based parameters or columns.  For any other datatype the returned value
     * is a NULL
     * <LI><B>ORDINAL_POSITION</B> int  =&gt; the ordinal position, starting
     * from 1, for the input and output parameters. A value of 0
     * is returned if this row describes the function's return value.
     * For result set columns, it is the
     * ordinal position of the column in the result set starting from 1.
     * <LI><B>IS_NULLABLE</B> String  =&gt; ISO rules are used to determine
     * the nullability for a parameter or column.
     * <UL>
     * <LI> YES           --- if the parameter or column can include NULLs
     * <LI> NO            --- if the parameter or column  cannot include NULLs
     * <LI> empty string  --- if the nullability for the
     * parameter  or column is unknown
     * </UL>
     * <LI><B>SPECIFIC_NAME</B> String  =&gt; the name which uniquely identifies
     * this function within its schema.  This is a user specified, or DBMS
     * generated, name that may be different then the <code>FUNCTION_NAME</code>
     * for example with overload functions
     * </OL>
     * 
     * <p>The PRECISION column represents the specified column size for the given
     * parameter or column.
     * For numeric data, this is the maximum precision.  For character data, this is the length in characters.
     * For datetime datatypes, this is the length in characters of the String representation (assuming the
     * maximum allowed precision of the fractional seconds component). For binary data, this is the length in bytes.  For the ROWID datatype,
     * this is the length in bytes. Null is returned for data types where the
     * column size is not applicable.
     *
     * @param catalog             a catalog name; must match the catalog name as it
     *                            is stored in the database; "" retrieves those without a catalog;
     *                            <code>null</code> means that the catalog name should not be used to narrow
     *                            the search
     * @param schemaPattern       a schema name pattern; must match the schema name
     *                            as it is stored in the database; "" retrieves those without a schema;
     *                            <code>null</code> means that the schema name should not be used to narrow
     *                            the search
     * @param functionNamePattern a procedure name pattern; must match the
     *                            function name as it is stored in the database
     * @param columnNamePattern   a parameter name pattern; must match the
     *                            parameter or column name as it is stored in the database
     * @return <code>ResultSet</code> - each row describes a
     * user function parameter, column  or return type
     * @throws java.sql.SQLException if a database access error occurs
     * @see #getSearchStringEscape
     * @since 1.6
     */
    @Override
    public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern, String columnNamePattern) throws SQLException
    {
        return getEmptyResultSet();
    }

    /**
     * Retrieves a description of the pseudo or hidden columns available
     * in a given table within the specified catalog and schema.
     * Pseudo or hidden columns may not always be stored within
     * a table and are not visible in a ResultSet unless they are
     * specified in the query's outermost SELECT list. Pseudo or hidden
     * columns may not necessarily be able to be modified. If there are
     * no pseudo or hidden columns, an empty ResultSet is returned.
     * 
     * <P>Only column descriptions matching the catalog, schema, table
     * and column name criteria are returned.  They are ordered by
     * <code>TABLE_CAT</code>,<code>TABLE_SCHEM</code>, <code>TABLE_NAME</code>
     * and <code>COLUMN_NAME</code>.
     * 
     * <P>Each column description has the following columns:
     * <OL>
     * <LI><B>TABLE_CAT</B> String =&gt; table catalog (may be <code>null</code>)
     * <LI><B>TABLE_SCHEM</B> String =&gt; table schema (may be <code>null</code>)
     * <LI><B>TABLE_NAME</B> String =&gt; table name
     * <LI><B>COLUMN_NAME</B> String =&gt; column name
     * <LI><B>DATA_TYPE</B> int =&gt; SQL type from java.sql.Types
     * <LI><B>COLUMN_SIZE</B> int =&gt; column size.
     * <LI><B>DECIMAL_DIGITS</B> int =&gt; the number of fractional digits. Null is returned for data types where
     * DECIMAL_DIGITS is not applicable.
     * <LI><B>NUM_PREC_RADIX</B> int =&gt; Radix (typically either 10 or 2)
     * <LI><B>COLUMN_USAGE</B> String =&gt; The allowed usage for the column.  The
     * value returned will correspond to the enum name returned by {@link java.sql.PseudoColumnUsage#name PseudoColumnUsage.name()}
     * <LI><B>REMARKS</B> String =&gt; comment describing column (may be <code>null</code>)
     * <LI><B>CHAR_OCTET_LENGTH</B> int =&gt; for char types the
     * maximum number of bytes in the column
     * <LI><B>IS_NULLABLE</B> String  =&gt; ISO rules are used to determine the nullability for a column.
     * <UL>
     * <LI> YES           --- if the column can include NULLs
     * <LI> NO            --- if the column cannot include NULLs
     * <LI> empty string  --- if the nullability for the column is unknown
     * </UL>
     * </OL>
     * 
     * <p>The COLUMN_SIZE column specifies the column size for the given column.
     * For numeric data, this is the maximum precision.  For character data, this is the length in characters.
     * For datetime datatypes, this is the length in characters of the String representation (assuming the
     * maximum allowed precision of the fractional seconds component). For binary data, this is the length in bytes.  For the ROWID datatype,
     * this is the length in bytes. Null is returned for data types where the
     * column size is not applicable.
     *
     * @param catalog           a catalog name; must match the catalog name as it
     *                          is stored in the database; "" retrieves those without a catalog;
     *                          <code>null</code> means that the catalog name should not be used to narrow
     *                          the search
     * @param schemaPattern     a schema name pattern; must match the schema name
     *                          as it is stored in the database; "" retrieves those without a schema;
     *                          <code>null</code> means that the schema name should not be used to narrow
     *                          the search
     * @param tableNamePattern  a table name pattern; must match the
     *                          table name as it is stored in the database
     * @param columnNamePattern a column name pattern; must match the column
     *                          name as it is stored in the database
     * @return <code>ResultSet</code> - each row is a column description
     * @throws java.sql.SQLException if a database access error occurs
     * @see java.sql.PseudoColumnUsage
     * @since 1.7
     */
    @Override
    public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException
    {
        return getEmptyResultSet();
    }

    /**
     * Retrieves whether a generated key will always be returned if the column
     * name(s) or index(es) specified for the auto generated key column(s)
     * are valid and the statement succeeds.  The key that is returned may or
     * may not be based on the column(s) for the auto generated key.
     * Consult your JDBC driver documentation for additional details.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws java.sql.SQLException if a database access error occurs
     * @since 1.7
     */
    @Override
    public boolean generatedKeyAlwaysReturned() throws SQLException
    {
        return false;
    }

    /**
     * Returns an object that implements the given interface to allow access to
     * non-standard methods, or standard methods not exposed by the proxy.
     * 
     * If the receiver implements the interface then the result is the receiver
     * or a proxy for the receiver. If the receiver is a wrapper
     * and the wrapped object implements the interface then the result is the
     * wrapped object or a proxy for the wrapped object. Otherwise return the
     * the result of calling <code>unwrap</code> recursively on the wrapped object
     * or a proxy for that result. If the receiver is not a
     * wrapper and does not implement the interface, then an <code>SQLException</code> is thrown.
     *
     * @param iface A Class defining an interface that the result must implement.
     * @return an object that implements the interface. May be a proxy for the actual implementing object.
     * @throws java.sql.SQLException If no object found that implements the interface
     * @since 1.6
     */
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        if (iface.isAssignableFrom(getClass()))
        {
            return iface.cast(this);
        }
        throw new SQLException("Cannot unwrap to " + iface.getName());
    }

    /**
     * Returns true if this either implements the interface argument or is directly or indirectly a wrapper
     * for an object that does. Returns false otherwise. If this implements the interface then return true,
     * else if this is a wrapper then return the result of recursively calling <code>isWrapperFor</code> on the wrapped
     * object. If this does not implement the interface and is not a wrapper, return false.
     * This method should be implemented as a low-cost operation compared to <code>unwrap</code> so that
     * callers can use this method to avoid expensive <code>unwrap</code> calls that may fail. If this method
     * returns true then calling <code>unwrap</code> with the same argument should succeed.
     *
     * @param iface a Class defining an interface.
     * @return true if this implements the interface or directly or indirectly wraps an object that does.
     * @throws java.sql.SQLException if an error occurs while determining whether this is a wrapper
     *                               for an object with the given interface.
     * @since 1.6
     */
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        return iface.isAssignableFrom(getClass());
    }
    private ResultSet getEmptyResultSet()
    {
        List types = new ArrayList();
        Map signature = new HashMap();
        CouchMetrics metrics = new CouchMetrics();
        metrics.setResultCount(types.size());
        // this just has to be not 0
        metrics.setResultSize(2);

        CouchResponse couchResponse = new CouchResponse();
        couchResponse.setResults(types);
        couchResponse.setMetrics(metrics);
        //noinspection unchecked
        couchResponse.setSignature(signature);

        return new CBResultSet(null, couchResponse);
    }
}
