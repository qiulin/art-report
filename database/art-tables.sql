-- Create the ART database

-- IMPORTANT:
-- after running this script, ALSO RUN the tables_xxx.sql script for your database
-- (found in the quartz directory)


-- NOTES:
-- for sql server, mysql replace TIMESTAMP with DATETIME

-- for sql server, replace CLOB with VARCHAR(MAX)
-- for mysql, replace CLOB with LONGTEXT
-- for postgresql, replace CLOB with TEXT
-- for cubrid, replace CLOB with STRING


-- UPGRADING:
-- if you are upgrading, don't use this script. run the scripts available in the
-- upgrade directory run the scripts one at a time to upgrade to newer versions.
-- e.g. from 2.0 to 2.1, then 2.1 to 2.2 etc.

-- sql reserved words checker - https://www.petefreitag.com/tools/sql_reserved_words_checker/

-- ------------------------------------------------


-- ART_DATABASE_VERSION
-- stores the version of the ART database

CREATE TABLE ART_DATABASE_VERSION
(
	DATABASE_VERSION VARCHAR(50)
);
-- insert database version
INSERT INTO ART_DATABASE_VERSION VALUES('3.1-snapshot');


-- ART_CUSTOM_UPGRADES
-- stores indications of custom upgrades that have been performed

CREATE TABLE ART_CUSTOM_UPGRADES
(
	DATABASE_VERSION VARCHAR(50),
	UPGRADED INTEGER
);


-- ART_USERS 
-- Stores user info

-- ACCESS_LEVEL: 0 = normal user, 5 = normal user who can schedule jobs
-- 10 = junior admin, 30 = mid admin, 40 = standard admin, 80 = senior admin
-- 100 = super admin
-- ACTIVE: boolean value. 0=false, 1=true
-- CAN_CHANGE_PASSWORD: boolean value. 0=false, 1=true

CREATE TABLE ART_USERS
(
	USER_ID INTEGER NOT NULL,
	USERNAME VARCHAR(50) NOT NULL,
	PASSWORD VARCHAR(200) NOT NULL,
	PASSWORD_ALGORITHM VARCHAR(20),
	FULL_NAME VARCHAR(100),  
	EMAIL VARCHAR(100),    
	ACCESS_LEVEL INTEGER,
	DEFAULT_QUERY_GROUP INTEGER,
	START_QUERY VARCHAR(500),
	CAN_CHANGE_PASSWORD INTEGER, 
	ACTIVE INTEGER, 
	CREATION_DATE TIMESTAMP,
	CREATED_BY VARCHAR(50),
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50),
	CONSTRAINT au_pk PRIMARY KEY(USERNAME)	
);


-- ART_ACCESS_LEVELS
-- Reference table for user access levels

CREATE TABLE ART_ACCESS_LEVELS
(
	ACCESS_LEVEL INTEGER NOT NULL,
	DESCRIPTION VARCHAR(50),
	CONSTRAINT aal_pk PRIMARY KEY(ACCESS_LEVEL)
);
-- insert access levels
INSERT INTO ART_ACCESS_LEVELS VALUES (0,'Normal User');
INSERT INTO ART_ACCESS_LEVELS VALUES (5,'Schedule User');
INSERT INTO ART_ACCESS_LEVELS VALUES (10,'Junior Admin');
INSERT INTO ART_ACCESS_LEVELS VALUES (30,'Mid Admin');
INSERT INTO ART_ACCESS_LEVELS VALUES (40,'Standard Admin');
INSERT INTO ART_ACCESS_LEVELS VALUES (80,'Senior Admin');
INSERT INTO ART_ACCESS_LEVELS VALUES (100,'Super Admin');


-- ART_DATABASES
-- Stores Target Database definitions

-- ACTIVE: boolean. 0=false, 1=true
-- JNDI: boolean. 0=false, 1=true

CREATE TABLE ART_DATABASES
(
	DATABASE_ID INTEGER NOT NULL,
	NAME	          VARCHAR(50) NOT NULL,
	DESCRIPTION VARCHAR(200),
	DATASOURCE_TYPE VARCHAR(20),
	JNDI INTEGER,
	DRIVER            VARCHAR(200) NOT NULL,
	URL               VARCHAR(2000) NOT NULL,
	USERNAME          VARCHAR(50) NOT NULL,
	PASSWORD          VARCHAR(100) NOT NULL,
	PASSWORD_ALGORITHM VARCHAR(20),
	POOL_TIMEOUT      INTEGER,  
	TEST_SQL          VARCHAR(60),
	ACTIVE    INTEGER,
	CREATION_DATE TIMESTAMP,
	CREATED_BY VARCHAR(50),
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50),
	CONSTRAINT ad_pk PRIMARY KEY(DATABASE_ID),
	CONSTRAINT ad_name_uq UNIQUE(NAME)
);


-- ART_QUERY_GROUPS
-- Stores name and description of query groups

CREATE TABLE ART_QUERY_GROUPS
(
	QUERY_GROUP_ID  INTEGER  NOT NULL,  
	NAME            VARCHAR(50) NOT NULL,
	DESCRIPTION     VARCHAR(100),
	CREATION_DATE TIMESTAMP,
	CREATED_BY VARCHAR(50),
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50),
	CONSTRAINT aqg_pk PRIMARY KEY(QUERY_GROUP_ID),
	CONSTRAINT aqg_name_uq UNIQUE(NAME)	
);


-- ART_QUERIES
-- Stores query definitions 

-- USES_RULES: boolean. 0=false, 1=true
-- PARAMETERS_IN_OUTPUT: boolean. 0=false, 1=true. indicates whether
-- selected report parameters will be shown in the report output
-- ACTIVE: boolean
-- HIDDEN: boolean
-- OMIT_TITLE_ROW: boolean
-- LOV_USE_DYNAMIC_DATASOURCE: boolean

CREATE TABLE ART_QUERIES
(
	QUERY_ID    INTEGER NOT NULL,	
	NAME              VARCHAR(50) NOT NULL,
	SHORT_DESCRIPTION VARCHAR(254) NOT NULL,
	DESCRIPTION       VARCHAR(2000) NOT NULL,
	QUERY_TYPE        INTEGER,
	GROUP_COLUMN INTEGER,
	QUERY_GROUP_ID  INTEGER NOT NULL,	
	DATABASE_ID	    INTEGER NOT NULL,
	CONTACT_PERSON        VARCHAR(100), 
	USES_RULES  INTEGER,	 
	ACTIVE INTEGER,
	HIDDEN INTEGER,
	PARAMETERS_IN_OUTPUT INTEGER,
	X_AXIS_LABEL VARCHAR(50),
	Y_AXIS_LABEL VARCHAR(50),
	GRAPH_OPTIONS VARCHAR(200),
	SECONDARY_CHARTS VARCHAR(100),
	TEMPLATE VARCHAR(100),
	DISPLAY_RESULTSET INTEGER,	
	XMLA_DATASOURCE VARCHAR(50),
	XMLA_CATALOG VARCHAR(50),
	DEFAULT_REPORT_FORMAT VARCHAR(50),
	OMIT_TITLE_ROW INTEGER,
	HIDDEN_COLUMNS VARCHAR(500),
	TOTAL_COLUMNS VARCHAR(500),
	DATE_COLUMN_FORMAT VARCHAR(100),
	NUMBER_COLUMN_FORMAT VARCHAR(50),
	COLUMN_FORMATS VARCHAR(2000),
	LOCALE VARCHAR(50),
	NULL_NUMBER_DISPLAY VARCHAR(50),
	NULL_STRING_DISPLAY VARCHAR(50),
	FETCH_SIZE INTEGER,
	REPORT_OPTIONS VARCHAR(4000),
	PAGE_ORIENTATION VARCHAR(20),
	LOV_USE_DYNAMIC_DATASOURCE INTEGER,
	OPEN_PASSWORD VARCHAR(100),
	MODIFY_PASSWORD VARCHAR(100),
	ENCRYPTOR_ID INTEGER,
	CREATION_DATE TIMESTAMP,
	CREATED_BY VARCHAR(50),
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50),
	CONSTRAINT aq_pk PRIMARY KEY(QUERY_ID),
	CONSTRAINT aq_name_uq UNIQUE(NAME)
);


-- ART_REPORT_TYPES
-- Reference table for report types

CREATE TABLE ART_REPORT_TYPES
(
	REPORT_TYPE INTEGER NOT NULL,
	DESCRIPTION VARCHAR(100),
	CONSTRAINT art_pk PRIMARY KEY(REPORT_TYPE)
);
-- insert report types
INSERT INTO ART_REPORT_TYPES VALUES (0,'Tabular');
INSERT INTO ART_REPORT_TYPES VALUES (1,'Group');
INSERT INTO ART_REPORT_TYPES VALUES (100,'Update Statement');
INSERT INTO ART_REPORT_TYPES VALUES (101,'Crosstab');
INSERT INTO ART_REPORT_TYPES VALUES (102,'Crosstab (html only)');
INSERT INTO ART_REPORT_TYPES VALUES (103,'Tabular (html only)');
INSERT INTO ART_REPORT_TYPES VALUES (110,'Dashboard');
INSERT INTO ART_REPORT_TYPES VALUES (111,'Text');
INSERT INTO ART_REPORT_TYPES VALUES (112,'JPivot: Mondrian');
INSERT INTO ART_REPORT_TYPES VALUES (113,'JPivot: Mondrian XMLA');
INSERT INTO ART_REPORT_TYPES VALUES (114,'JPivot: SQL Server XMLA');
INSERT INTO ART_REPORT_TYPES VALUES (115,'JasperReports: Template Query');
INSERT INTO ART_REPORT_TYPES VALUES (116,'JasperReports: ART Query');
INSERT INTO ART_REPORT_TYPES VALUES (117,'Jxls: Template Query');
INSERT INTO ART_REPORT_TYPES VALUES (118,'Jxls: ART Query');
INSERT INTO ART_REPORT_TYPES VALUES (119,'LOV: Dynamic');
INSERT INTO ART_REPORT_TYPES VALUES (120,'LOV: Static');
INSERT INTO ART_REPORT_TYPES VALUES (121,'Dynamic Job Recipients');
INSERT INTO ART_REPORT_TYPES VALUES (122,'FreeMarker');
INSERT INTO ART_REPORT_TYPES VALUES (123,'XDocReport: FreeMarker engine - Docx');
INSERT INTO ART_REPORT_TYPES VALUES (124,'XDocReport: Velocity engine - Docx');
INSERT INTO ART_REPORT_TYPES VALUES (125,'XDocReport: FreeMarker engine - ODT');
INSERT INTO ART_REPORT_TYPES VALUES (126,'XDocReport: Velocity engine - ODT');
INSERT INTO ART_REPORT_TYPES VALUES (127,'XDocReport: FreeMarker engine - PPTX');
INSERT INTO ART_REPORT_TYPES VALUES (128,'XDocReport: Velocity engine - PPTX');
INSERT INTO ART_REPORT_TYPES VALUES (129,'Dashboard: Gridstack');
INSERT INTO ART_REPORT_TYPES VALUES (130,'ReactPivot');
INSERT INTO ART_REPORT_TYPES VALUES (131,'Thymeleaf');
INSERT INTO ART_REPORT_TYPES VALUES (132,'PivotTable.js');
INSERT INTO ART_REPORT_TYPES VALUES (133,'PivotTable.js: CSV Local');
INSERT INTO ART_REPORT_TYPES VALUES (134,'PivotTable.js: CSV Server');
INSERT INTO ART_REPORT_TYPES VALUES (135,'Dygraphs');
INSERT INTO ART_REPORT_TYPES VALUES (136,'Dygraphs: CSV Local');
INSERT INTO ART_REPORT_TYPES VALUES (137,'Dygraphs: CSV Server');
INSERT INTO ART_REPORT_TYPES VALUES (138,'DataTables');
INSERT INTO ART_REPORT_TYPES VALUES (139,'DataTables: CSV Local');
INSERT INTO ART_REPORT_TYPES VALUES (140,'DataTables: CSV Server');
INSERT INTO ART_REPORT_TYPES VALUES (141,'Fixed Width');
INSERT INTO ART_REPORT_TYPES VALUES (142,'C3.js');
INSERT INTO ART_REPORT_TYPES VALUES (143,'Chart.js');
INSERT INTO ART_REPORT_TYPES VALUES (144,'Datamaps');
INSERT INTO ART_REPORT_TYPES VALUES (145,'Datamaps: File');
INSERT INTO ART_REPORT_TYPES VALUES (146,'Leaflet');
INSERT INTO ART_REPORT_TYPES VALUES (147,'OpenLayers');
INSERT INTO ART_REPORT_TYPES VALUES (148,'Tabular: Heatmap');
INSERT INTO ART_REPORT_TYPES VALUES (149,'Saiku: Report');
INSERT INTO ART_REPORT_TYPES VALUES (150,'Saiku: Connection');
INSERT INTO ART_REPORT_TYPES VALUES (151,'MongoDB');
INSERT INTO ART_REPORT_TYPES VALUES (152,'CSV');
INSERT INTO ART_REPORT_TYPES VALUES (-1,'Chart: XY');
INSERT INTO ART_REPORT_TYPES VALUES (-2,'Chart: Pie 3D');
INSERT INTO ART_REPORT_TYPES VALUES (-3,'Chart: Horizontal Bar 3D');
INSERT INTO ART_REPORT_TYPES VALUES (-4,'Chart: Vertical Bar 3D');
INSERT INTO ART_REPORT_TYPES VALUES (-5,'Chart: Line');
INSERT INTO ART_REPORT_TYPES VALUES (-6,'Chart: Time Series');
INSERT INTO ART_REPORT_TYPES VALUES (-7,'Chart: Date Series');
INSERT INTO ART_REPORT_TYPES VALUES (-8,'Chart: Stacked Vertical Bar 3D');
INSERT INTO ART_REPORT_TYPES VALUES (-9,'Chart: Stacked Horizontal Bar 3D');
INSERT INTO ART_REPORT_TYPES VALUES (-10,'Chart: Speedometer');
INSERT INTO ART_REPORT_TYPES VALUES (-11,'Chart: Bubble Chart');
INSERT INTO ART_REPORT_TYPES VALUES (-12,'Chart: Heat Map');
INSERT INTO ART_REPORT_TYPES VALUES (-13,'Chart: Pie 2D');
INSERT INTO ART_REPORT_TYPES VALUES (-14,'Chart: Vertical Bar 2D');
INSERT INTO ART_REPORT_TYPES VALUES (-15,'Chart: Stacked Vertical Bar 2D');
INSERT INTO ART_REPORT_TYPES VALUES (-16,'Chart: Horizontal Bar 2D');
INSERT INTO ART_REPORT_TYPES VALUES (-17,'Chart: Stacked Horizontal Bar 2D');


-- ART_ADMIN_PRIVILEGES
-- stores privileges for Junior and Mid Admin (Admin Level <=30)
-- this table is used to limit data extraction for these admins when
-- viewing available groups and datasources

-- PRIVILEGE can be either "DB" (datasource) or "GRP" (query group)
-- VALUE_ID is the datasource id or query group id

CREATE TABLE ART_ADMIN_PRIVILEGES
(	
	USER_ID INTEGER,
	USERNAME    VARCHAR(50) NOT NULL,
	PRIVILEGE   VARCHAR(4) NOT NULL,
	VALUE_ID    INTEGER NOT NULL,
	CONSTRAINT aap_pk PRIMARY KEY(USERNAME, PRIVILEGE, VALUE_ID)	
);


-- ART_USER_QUERIES
-- Stores the queries a user can execute

CREATE TABLE ART_USER_QUERIES
(
	USER_ID INTEGER,
	USERNAME    VARCHAR(50) NOT NULL,
	QUERY_ID    INTEGER     NOT NULL,	 
	CONSTRAINT auq_pk PRIMARY KEY(USERNAME, QUERY_ID)	
);


-- ART_USER_QUERY_GROUPS
-- Stores query groups a user can deal with

CREATE TABLE ART_USER_QUERY_GROUPS
(
	USER_ID INTEGER,
	USERNAME       VARCHAR(50) NOT NULL,
	QUERY_GROUP_ID INTEGER     NOT NULL,        
	CONSTRAINT auqg_pk PRIMARY KEY(USERNAME, QUERY_GROUP_ID)	
);


-- ART_PARAMETERS
-- Stores parameter definitions, holding core parameter attributes

-- HIDDEN: boolean
-- SHARED: boolean
-- USE_LOV: boolean
-- USE_RULES_IN_LOV: boolean
-- USE_DIRECT_SUBSTITUTION: boolean
-- DRILLDOWN_COLUMN_INDEX - if used in a drilldown report, refers to the column in
-- the parent report on which the parameter will be applied (index starts from 1)

CREATE TABLE ART_PARAMETERS
(	
	PARAMETER_ID INTEGER NOT NULL,		
	NAME  VARCHAR(60),
	DESCRIPTION VARCHAR(50),
	PARAMETER_TYPE VARCHAR(30),           
	PARAMETER_LABEL     VARCHAR(50),
	HELP_TEXT            VARCHAR(500),
	DATA_TYPE         VARCHAR(30),
	DEFAULT_VALUE     VARCHAR(4000),
	DEFAULT_VALUE_REPORT_ID INTEGER,
	HIDDEN INTEGER,
	SHARED INTEGER,
	USE_LOV INTEGER, 
	LOV_REPORT_ID  INTEGER,
	USE_RULES_IN_LOV INTEGER,
	DRILLDOWN_COLUMN_INDEX INTEGER,
	USE_DIRECT_SUBSTITUTION INTEGER,
	PARAMETER_OPTIONS VARCHAR(4000),
	PARAMETER_DATE_FORMAT VARCHAR(100),
	PLACEHOLDER_TEXT VARCHAR(100),
	CREATION_DATE TIMESTAMP,
	CREATED_BY VARCHAR(50),
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50),
	CONSTRAINT ap_pk PRIMARY KEY(PARAMETER_ID)	
);


-- ART_REPORT_PARAMETERS
-- Stores parameters used in reports, holding additional parameter attributes

CREATE TABLE ART_REPORT_PARAMETERS
(	
	REPORT_PARAMETER_ID INTEGER NOT NULL,
	REPORT_ID INTEGER NOT NULL,	
	PARAMETER_ID INTEGER NOT NULL,	
	PARAMETER_POSITION INTEGER NOT NULL,
	CHAINED_PARENTS  VARCHAR(200),              
	CHAINED_DEPENDS VARCHAR(200),
	CONSTRAINT arp_pk PRIMARY KEY(REPORT_PARAMETER_ID)	
);


-- ART_QUERY_FIELDS
-- Stores query parameters

-- FIELD_POSITION is the order the parameter is displayed to users
-- FIELD_CLASS stores the data type of the parameter
-- PARAM_TYPE: M for MULTI param, I for INLINE param 
-- PARAM_LABEL stores the column name for non-labelled MULTI params
-- or the parameter label for INLINE params or labelled multi params
-- USE_LOV is set to Y if the param values are provided by an LOV query
-- CHAINED_PARAM_POSITION is the position of the chained param 
-- CHAINED_VALUE_POSITION - allow chained parameter value to come from
-- a different parameter from the previous one in the chained parameter sequence
-- DRILLDOWN_COLUMN - if used in a drill down report, refers to the column in
-- the parent report on which the parameter will be applied 

CREATE TABLE ART_QUERY_FIELDS
(	
	QUERY_ID                INTEGER     NOT NULL,
	FIELD_POSITION          INTEGER     NOT NULL, 
	NAME                    VARCHAR(25),
	SHORT_DESCRIPTION       VARCHAR(40),
	DESCRIPTION             VARCHAR(120),
	PARAM_TYPE VARCHAR(1) NOT NULL,           
	PARAM_LABEL     VARCHAR(55),  
	PARAM_DATA_TYPE         VARCHAR(15) NOT NULL,
	DEFAULT_VALUE           VARCHAR(80),	        
	USE_LOV       VARCHAR(1), 		
	APPLY_RULES_TO_LOV        VARCHAR(1),
	LOV_QUERY_ID  INTEGER,
	CHAINED_PARAM_POSITION  INTEGER,              
	CHAINED_VALUE_POSITION INTEGER,
	DRILLDOWN_COLUMN INTEGER,
	DIRECT_SUBSTITUTION VARCHAR(1),
	MIGRATED INTEGER,
	UPDATE_DATE TIMESTAMP,	
	CONSTRAINT aqf_pk PRIMARY KEY(QUERY_ID, FIELD_POSITION)	
);


-- ART_ALL_SOURCES
-- Stores source code for queries (sql, mdx, xml, html, text)

CREATE TABLE ART_ALL_SOURCES
(
	OBJECT_ID              INTEGER      NOT NULL,	
	LINE_NUMBER            INTEGER      NOT NULL,
	SOURCE_INFO              VARCHAR(4000),
	CONSTRAINT aas_pk PRIMARY KEY(OBJECT_ID, LINE_NUMBER)	
);


-- ART_RULES
-- Stores Rule definitions
 
CREATE TABLE ART_RULES
(
	RULE_ID INTEGER NOT NULL,
	RULE_NAME VARCHAR(50) NOT NULL,
	SHORT_DESCRIPTION VARCHAR(100),
	DATA_TYPE VARCHAR(30),
	CREATION_DATE TIMESTAMP,
	CREATED_BY VARCHAR(50),
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50),
	CONSTRAINT ar_pk PRIMARY KEY(RULE_ID),
	CONSTRAINT ar_rname_uq UNIQUE(RULE_NAME)
);


-- ART_QUERY_RULES
-- Stores rules-query relationships 

CREATE TABLE ART_QUERY_RULES
(
	QUERY_RULE_ID INTEGER NOT NULL,
	QUERY_ID INTEGER NOT NULL,
	RULE_ID INTEGER,
	RULE_NAME VARCHAR(50) NOT NULL,
	FIELD_NAME VARCHAR(100) NOT NULL,
	FIELD_DATA_TYPE VARCHAR(15), 
	CONSTRAINT aqr_pk PRIMARY KEY(QUERY_ID, RULE_NAME)	
);


-- ART_USER_RULES
-- Stores rule values for users
-- RULE_TYPE can be EXACT or LOOKUP
 
CREATE TABLE ART_USER_RULES
(  
	RULE_VALUE_KEY VARCHAR(50) NOT NULL,
	USER_ID INTEGER NOT NULL,
	USERNAME VARCHAR(50) NOT NULL,
	RULE_ID INTEGER,
	RULE_NAME VARCHAR(50) NOT NULL, 
	RULE_VALUE VARCHAR(100) NOT NULL,
	RULE_TYPE VARCHAR(6)	
);

-- ART_USER_GROUP_RULES
-- Stores rule values for user groups
-- RULE_TYPE can be EXACT or LOOKUP
 
CREATE TABLE ART_USER_GROUP_RULES
(  
	RULE_VALUE_KEY VARCHAR(50) NOT NULL,
	USER_GROUP_ID INTEGER NOT NULL,
	RULE_ID INTEGER,
	RULE_NAME VARCHAR(50) NOT NULL, 
	RULE_VALUE VARCHAR(100) NOT NULL,
	RULE_TYPE VARCHAR(6)	
);


-- ART_JOBS
-- Stores scheduled jobs

-- OUTPUT_FORMAT: html, pdf, xls etc
-- LAST_FILE_NAME: Contains result of last job execution
-- MIGRATED_TO_QUARTZ is present to allow seamless migration of jobs when
-- upgrading from ART versions before 1.11 (before quartz was used as the scheduling engine)
-- ACTIVE: boolean. 0=false, 1=true

CREATE TABLE ART_JOBS
(
	JOB_ID INTEGER NOT NULL,
	JOB_NAME VARCHAR(50),
	QUERY_ID	    INTEGER NOT NULL,
	USER_ID INTEGER,
	USERNAME          VARCHAR(50) NOT NULL,
	OUTPUT_FORMAT            VARCHAR(50) NOT NULL, 
	JOB_TYPE VARCHAR(50),      
	JOB_MINUTE	    VARCHAR(100),               
	JOB_HOUR		    VARCHAR(100),               
	JOB_DAY		    VARCHAR(100),               
	JOB_WEEKDAY	    VARCHAR(100),               
	JOB_MONTH		    VARCHAR(100),               
	MAIL_TOS          VARCHAR(254),
	MAIL_FROM         VARCHAR(80),
	MAIL_CC VARCHAR(254),
	MAIL_BCC VARCHAR(254),
	SUBJECT	    VARCHAR(1000),
	MESSAGE           VARCHAR(4000),
	CACHED_DATASOURCE_ID INTEGER,
	CACHED_TABLE_NAME VARCHAR(30),	
	START_DATE TIMESTAMP,
	END_DATE TIMESTAMP,
	NEXT_RUN_DATE TIMESTAMP NULL,		
	LAST_FILE_NAME    VARCHAR(4000),
	LAST_RUN_MESSAGE VARCHAR(100),
	LAST_RUN_DETAILS    VARCHAR(4000),
	LAST_START_DATE   TIMESTAMP NULL,
	LAST_END_DATE     TIMESTAMP NULL,
	ACTIVE INTEGER,
	ENABLE_AUDIT INTEGER,				
	ALLOW_SHARING INTEGER,
	ALLOW_SPLITTING INTEGER,
	RECIPIENTS_QUERY_ID INTEGER,
	RUNS_TO_ARCHIVE INTEGER,
	MIGRATED_TO_QUARTZ VARCHAR(1),
	FIXED_FILE_NAME VARCHAR(1000),
	BATCH_FILE VARCHAR(50),
	FTP_SERVER_ID INTEGER,
	EMAIL_TEMPLATE VARCHAR(100),
	EXTRA_SCHEDULES CLOB,	
	CREATION_DATE TIMESTAMP,
	CREATED_BY VARCHAR(50),
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50),
	CONSTRAINT aj_pk PRIMARY KEY(JOB_ID)
);


-- ART_JOBS_PARAMETERS
-- store jobs parameters

-- PARAM_TYPE: M = multi, I = inline 
-- PARAM_NAME: the html element name of the parameter

CREATE TABLE ART_JOBS_PARAMETERS
(
	JOB_ID        INTEGER NOT NULL,
	PARAM_TYPE	VARCHAR(1) NOT NULL,   
	PARAM_NAME		    VARCHAR(60),
	PARAM_VALUE		    VARCHAR(4000)	
);


-- ART_JOBS_AUDIT
-- stores logs of every job execution when job auditing is enabled

-- USERNAME: user for whom the job is run
-- JOB_AUDIT_KEY: unique identifier for a job audit record
-- ACTION: S = job started, E = job ended, X = Error occurred while running job

CREATE TABLE ART_JOBS_AUDIT
(
	JOB_ID            INTEGER NOT NULL,
	USER_ID INTEGER,
	USERNAME VARCHAR(50),
	JOB_AUDIT_KEY VARCHAR(100),
	JOB_ACTION   VARCHAR(1),             
	START_DATE TIMESTAMP NULL,
	END_DATE TIMESTAMP NULL	
);

		
-- ART_LOGS
-- Stores log information e.g. logins and report execution

-- LOG_TYPE: login = successful login, loginerr = unsuccessful login attempt
-- report = interactive report execution, upload = template file uploaded when
-- creating query that uses a template file
-- TOTAL_TIME: total execution time in secs, including fetch time and display time
-- FETCH_TIME: time elapsed from when the query is submitted to when the
-- database returns 1st row

CREATE TABLE ART_LOGS
(
	LOG_DATE TIMESTAMP NOT NULL,	
	USERNAME VARCHAR(50) NOT NULL,
	LOG_TYPE VARCHAR(50) NOT NULL, 
	IP VARCHAR(50), 
	QUERY_ID INTEGER,
	TOTAL_TIME INTEGER, 
	FETCH_TIME INTEGER, 
	MESSAGE VARCHAR(500) 
);


-- ART_USER_JOBS
-- Stores users who have been given access to a job's output

-- USER_GROUP_ID: used to indicate if job was shared via user group. To enable
-- deletion of split job records where access was granted via user group,
-- when a user is removed from a group.
-- LAST_FILE_NAME: contains file name for individualized output (split job),
-- or NULL if file name to use comes from ART_JOBS table

CREATE TABLE ART_USER_JOBS
(
	JOB_ID INTEGER NOT NULL,
	USER_ID INTEGER,
	USERNAME VARCHAR(50) NOT NULL,
	USER_GROUP_ID INTEGER,
	LAST_FILE_NAME VARCHAR(4000),
	LAST_RUN_MESSAGE VARCHAR(100),
	LAST_RUN_DETAILS VARCHAR(4000),
	LAST_START_DATE TIMESTAMP NULL,
	LAST_END_DATE TIMESTAMP NULL,
	CONSTRAINT auj_pk PRIMARY KEY(JOB_ID, USERNAME)	
);


-- ART_JOB_SCHEDULES
-- Stores job schedules to enable re-use of schedules when creating jobs

CREATE TABLE ART_JOB_SCHEDULES
(
	SCHEDULE_ID INTEGER NOT NULL,
	SCHEDULE_NAME VARCHAR(50) NOT NULL,
	DESCRIPTION VARCHAR(200),
	JOB_MINUTE	    VARCHAR(100),               
	JOB_HOUR		    VARCHAR(100),               
	JOB_DAY		    VARCHAR(100), 
	JOB_MONTH		    VARCHAR(100),   	
	JOB_WEEKDAY	    VARCHAR(100),
	EXTRA_SCHEDULES CLOB,	
	CREATION_DATE TIMESTAMP,
	CREATED_BY VARCHAR(50),
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50),
	CONSTRAINT ajs_pk PRIMARY KEY(SCHEDULE_ID),
	CONSTRAINT ajs_sname_uq UNIQUE(SCHEDULE_NAME)
);


-- ART_USER_GROUPS
-- Stores user group definitions

CREATE TABLE ART_USER_GROUPS
(
	USER_GROUP_ID INTEGER NOT NULL,
	NAME VARCHAR(50) NOT NULL,
	DESCRIPTION VARCHAR(100),
	DEFAULT_QUERY_GROUP INTEGER,
	START_QUERY VARCHAR(500),
	CREATION_DATE TIMESTAMP,
	CREATED_BY VARCHAR(50),
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50),
	CONSTRAINT aug_pk PRIMARY KEY(USER_GROUP_ID),
	CONSTRAINT aug_name_uq UNIQUE(NAME)
);


-- ART_USER_GROUP_ASSIGNEMENT
-- Stores details of which users belong to which user groups

CREATE TABLE ART_USER_GROUP_ASSIGNMENT
(
	USER_ID INTEGER NOT NULL,
	USERNAME VARCHAR(50) NOT NULL,
	USER_GROUP_ID INTEGER NOT NULL,
	CONSTRAINT auga_pk PRIMARY KEY(USERNAME, USER_GROUP_ID)	
);


-- ART_USER_GROUP_QUERIES
-- Stores which queries certain user groups can access (users who are members of 
-- the group can access the queries)

CREATE TABLE ART_USER_GROUP_QUERIES
(
	USER_GROUP_ID INTEGER NOT NULL,
	QUERY_ID INTEGER NOT NULL,
	CONSTRAINT augq_pk PRIMARY KEY(USER_GROUP_ID, QUERY_ID)	
);


-- ART_USER_GROUP_GROUPS
-- Stores which query groups certain user groups can access (users who are members
-- of the group can access the query groups)

CREATE TABLE ART_USER_GROUP_GROUPS
(
	USER_GROUP_ID INTEGER NOT NULL,
	QUERY_GROUP_ID INTEGER NOT NULL,
	CONSTRAINT augg_pk PRIMARY KEY(USER_GROUP_ID, QUERY_GROUP_ID)	
);


-- ART_USER_GROUP_JOBS
-- Stores which jobs have been shared with certain user groups (users who are
-- members of the group can access the job output)

CREATE TABLE ART_USER_GROUP_JOBS
(
	USER_GROUP_ID INTEGER NOT NULL,
	JOB_ID INTEGER NOT NULL,
	CONSTRAINT augj_pk PRIMARY KEY(USER_GROUP_ID, JOB_ID)	
);


-- ART_DRILLDOWN_QUERIES
-- Stores details of drill down queries

-- OPEN_IN_NEW_WINDOW: boolean

CREATE TABLE ART_DRILLDOWN_QUERIES
(
	DRILLDOWN_ID INTEGER NOT NULL,
	QUERY_ID INTEGER NOT NULL,
	DRILLDOWN_QUERY_ID INTEGER NOT NULL,
	DRILLDOWN_QUERY_POSITION INTEGER NOT NULL,
	DRILLDOWN_TITLE VARCHAR(50),
	DRILLDOWN_TEXT VARCHAR(50),
	OUTPUT_FORMAT VARCHAR(50),
	OPEN_IN_NEW_WINDOW INTEGER,
	CONSTRAINT adq_pk PRIMARY KEY(QUERY_ID, DRILLDOWN_QUERY_POSITION)	
);


-- ART_JOB_ARCHIVES
-- Stored details of past runs for publish jobs

-- JOB_SHARED: N = job not shared, Y = job shared, S = split job

CREATE TABLE ART_JOB_ARCHIVES
(
	ARCHIVE_ID VARCHAR(100) NOT NULL,
	JOB_ID INTEGER NOT NULL,
	USER_ID INTEGER,
	USERNAME VARCHAR(50) NOT NULL,	
	ARCHIVE_FILE_NAME VARCHAR(4000),
	START_DATE TIMESTAMP NULL,
	END_DATE TIMESTAMP NULL,
	JOB_SHARED VARCHAR(1),
	CONSTRAINT aja_pk PRIMARY KEY(ARCHIVE_ID)
);


-- ART_LOGGED_IN_USERS
-- Stores approximate indication of the currently logged in users

CREATE TABLE ART_LOGGED_IN_USERS
(
	LOGGED_IN_USERS_ID VARCHAR(100) NOT NULL,
	USER_ID INTEGER NOT NULL,
	USERNAME VARCHAR(50) NOT NULL,
	LOGIN_DATE TIMESTAMP NULL,
	IP_ADDRESS VARCHAR(50),
	CONSTRAINT alu_pk PRIMARY KEY(LOGGED_IN_USERS_ID)
);


-- ART_FTP_SERVERS
-- Stores configurations for ftp servers

-- ACTIVE: boolean

CREATE TABLE ART_FTP_SERVERS
(
	FTP_SERVER_ID INTEGER NOT NULL,
	NAME VARCHAR(50),
	DESCRIPTION VARCHAR(200),
	ACTIVE INTEGER,
	CONNECTION_TYPE VARCHAR(20),
	SERVER VARCHAR(100),
	PORT INTEGER,
	FTP_USER VARCHAR(50),
	PASSWORD VARCHAR(100),
	REMOTE_DIRECTORY VARCHAR(200),
	CREATION_DATE TIMESTAMP,
	CREATED_BY VARCHAR(50),
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50),
	CONSTRAINT afs_pk PRIMARY KEY(FTP_SERVER_ID),
	CONSTRAINT afs_name_uq UNIQUE(NAME)
);

-- ART_ENCRYPTORS
-- Stores configurations for file encryptors

CREATE TABLE ART_ENCRYPTORS
(
	ENCRYPTOR_ID INTEGER NOT NULL,
	NAME VARCHAR(50),
	DESCRIPTION VARCHAR(200),
	ACTIVE INTEGER,
	ENCRYPTOR_TYPE VARCHAR(50),
	AESCRYPT_PASSWORD VARCHAR(100),
	OPENPGP_PUBLIC_KEY_FILE VARCHAR(100),
	OPENPGP_PUBLIC_KEY_STRING VARCHAR(4000),
	OPENPGP_SIGNING_KEY_FILE VARCHAR(100),
	OPENPGP_SIGNING_KEY_PASSPHRASE VARCHAR(1000),
	CREATION_DATE TIMESTAMP,
	CREATED_BY VARCHAR(50),
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50),
	CONSTRAINT ae_pk PRIMARY KEY(ENCRYPTOR_ID),
	CONSTRAINT ae_name_uq UNIQUE(NAME)
);


