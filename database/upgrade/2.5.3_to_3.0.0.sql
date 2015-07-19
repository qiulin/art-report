-- Upgrade script from ART 2.5.2 to ART 3.0

-- CHANGES:
-- update database version
-- reset x_axis_label column for non-graph queries
-- decrease size of x_axis_label column
-- allow datasources to be disabled
-- add reference table for report types
-- add reference table for job types
-- change active_status fields from varchar to integer
-- increase size of username columns
-- rename update_time column
-- increase size of log_type column
-- change update_date columns to timestamps
-- add user_id columns
-- change can_change_password field from varchar to integer
-- rename hashing_algorithm column
-- reset lov group to id -1 and update default_query_groups column values
-- add creation_date columns
-- and many others...

-- NOTES:
-- for hsqldb, sql server, replace the MODIFY keyword with ALTER COLUMN
--
-- for postgresql, replace the MODIFY keyword with ALTER COLUMN <column name> TYPE <data type>
--
-- for oracle, postgresql, replace the SUBSTRING keyword with SUBSTR
--
-- for sql server, mysql, replace TIMESTAMP with DATETIME


-- ------------------------------------------------


-- ***************
-- IMPORTANT

-- after running this upgrade script, ALSO RUN the tables_xxx.sql script for your database 
-- (found in the quartz directory) this is not the usual process for upgrades.

-- *****************


-- update database version
DROP TABLE ART_SETTINGS;
CREATE TABLE ART_DATABASE_VERSION
(
	DATABASE_VERSION VARCHAR(50)
);
-- insert database version
INSERT INTO ART_DATABASE_VERSION VALUES('3.0-alpha4');

-- reset x_axis_label column for non-graph queries
UPDATE ART_QUERIES SET X_AXIS_LABEL='' WHERE QUERY_TYPE>=0;

-- decrease size of x_axis_label column
UPDATE ART_QUERIES SET X_AXIS_LABEL=SUBSTRING(X_AXIS_LABEL,1,50);
ALTER TABLE ART_QUERIES MODIFY X_AXIS_LABEL VARCHAR(50);

-- allow datasources to be disabled
ALTER TABLE ART_DATABASES ADD ACTIVE INTEGER;
UPDATE ART_DATABASES SET ACTIVE=1;

-- add description column
ALTER TABLE ART_DATABASES ADD DESCRIPTION VARCHAR(200);

-- change active_status fields from varchar to integer
ALTER TABLE ART_USERS ADD ACTIVE INTEGER;
UPDATE ART_USERS SET ACTIVE=1 WHERE ACTIVE_STATUS='A' OR ACTIVE_STATUS IS NULL;
ALTER TABLE ART_USERS DROP COLUMN ACTIVE_STATUS;

ALTER TABLE ART_JOBS ADD ACTIVE INTEGER;
UPDATE ART_JOBS SET ACTIVE=1 WHERE ACTIVE_STATUS='A' OR ACTIVE_STATUS IS NULL;
ALTER TABLE ART_JOBS DROP COLUMN ACTIVE_STATUS;

-- rename update_time column
ALTER TABLE ART_LOGS ADD LOG_DATE TIMESTAMP;
UPDATE ART_LOGS SET LOG_DATE=UPDATE_TIME;
ALTER TABLE ART_LOGS DROP COLUMN UPDATE_TIME;

-- increase size of log_type column
ALTER TABLE ART_LOGS MODIFY LOG_TYPE VARCHAR(50);

-- increase size of username columns
ALTER TABLE ART_USERS MODIFY USERNAME VARCHAR(50);
ALTER TABLE ART_DATABASES MODIFY USERNAME VARCHAR(50);
ALTER TABLE ART_ADMIN_PRIVILEGES MODIFY USERNAME VARCHAR(50);
ALTER TABLE ART_USER_QUERIES MODIFY USERNAME VARCHAR(50);
ALTER TABLE ART_USER_QUERY_GROUPS MODIFY USERNAME VARCHAR(50);
ALTER TABLE ART_USER_RULES MODIFY USERNAME VARCHAR(50);
ALTER TABLE ART_JOBS MODIFY USERNAME VARCHAR(50);
ALTER TABLE ART_JOBS_AUDIT MODIFY USERNAME VARCHAR(50);
ALTER TABLE ART_LOGS MODIFY USERNAME VARCHAR(50);
ALTER TABLE ART_USER_JOBS MODIFY USERNAME VARCHAR(50);
ALTER TABLE ART_USER_GROUP_ASSIGNMENT MODIFY USERNAME VARCHAR(50);
ALTER TABLE ART_JOB_ARCHIVES MODIFY USERNAME VARCHAR(50);

-- change update_date columns to timestamps
 ALTER TABLE ART_USERS MODIFY UPDATE_DATE TIMESTAMP;
 ALTER TABLE ART_DATABASES MODIFY UPDATE_DATE TIMESTAMP;
 ALTER TABLE ART_QUERIES MODIFY UPDATE_DATE TIMESTAMP;
 ALTER TABLE ART_USER_QUERIES DROP COLUMN UPDATE_DATE;
 ALTER TABLE ART_QUERY_FIELDS MODIFY UPDATE_DATE TIMESTAMP;
 
-- change jobs start and end dates to timestamps
ALTER TABLE ART_JOBS MODIFY START_DATE TIMESTAMP;
ALTER TABLE ART_JOBS MODIFY END_DATE TIMESTAMP;

-- add user_id columns
ALTER TABLE ART_USERS ADD USER_ID INTEGER;
ALTER TABLE ART_ADMIN_PRIVILEGES ADD USER_ID INTEGER;
ALTER TABLE ART_USER_QUERIES ADD USER_ID INTEGER;
ALTER TABLE ART_USER_QUERY_GROUPS ADD USER_ID INTEGER;
ALTER TABLE ART_USER_RULES ADD USER_ID INTEGER;
ALTER TABLE ART_JOBS ADD USER_ID INTEGER;
ALTER TABLE ART_USER_JOBS ADD USER_ID INTEGER;
ALTER TABLE ART_USER_GROUP_ASSIGNMENT ADD USER_ID INTEGER;
ALTER TABLE ART_JOB_ARCHIVES ADD USER_ID INTEGER;

-- change can_change_password field from varchar to integer
ALTER TABLE ART_USERS ADD TMP_CAN_CHANGE_PASSWORD VARCHAR(1);
UPDATE ART_USERS SET TMP_CAN_CHANGE_PASSWORD=CAN_CHANGE_PASSWORD;
ALTER TABLE ART_USERS DROP COLUMN CAN_CHANGE_PASSWORD;
ALTER TABLE ART_USERS ADD CAN_CHANGE_PASSWORD INTEGER;
UPDATE ART_USERS SET CAN_CHANGE_PASSWORD=1 WHERE TMP_CAN_CHANGE_PASSWORD='Y' OR TMP_CAN_CHANGE_PASSWORD IS NULL;
ALTER TABLE ART_USERS DROP COLUMN TMP_CAN_CHANGE_PASSWORD;

-- rename hashing_algorithm column
ALTER TABLE ART_USERS ADD PASSWORD_ALGORITHM VARCHAR(20);
UPDATE ART_USERS SET PASSWORD_ALGORITHM=HASHING_ALGORITHM;
ALTER TABLE ART_USERS DROP COLUMN HASHING_ALGORITHM;

-- reset lov group to id -1 and update default_query_groups column values
UPDATE ART_QUERY_GROUPS SET QUERY_GROUP_ID=-1 WHERE QUERY_GROUP_ID=0;
UPDATE ART_QUERIES SET QUERY_GROUP_ID=-1 WHERE QUERY_GROUP_ID=0;
UPDATE ART_QUERIES SET QUERY_TYPE=119 WHERE QUERY_GROUP_ID=-1 AND QUERY_TYPE=0;
UPDATE ART_USERS SET DEFAULT_QUERY_GROUP=0 WHERE DEFAULT_QUERY_GROUP=-1;
UPDATE ART_USER_GROUPS SET DEFAULT_QUERY_GROUP=0 WHERE DEFAULT_QUERY_GROUP=-1;

-- add creation_date columns
ALTER TABLE ART_USERS ADD CREATION_DATE TIMESTAMP;
ALTER TABLE ART_QUERIES ADD CREATION_DATE TIMESTAMP;
ALTER TABLE ART_USER_GROUPS ADD CREATION_DATE TIMESTAMP;
ALTER TABLE ART_USER_GROUPS ADD UPDATE_DATE TIMESTAMP;
ALTER TABLE ART_DATABASES ADD CREATION_DATE TIMESTAMP;
ALTER TABLE ART_QUERY_GROUPS ADD CREATION_DATE TIMESTAMP;
ALTER TABLE ART_QUERY_GROUPS ADD UPDATE_DATE TIMESTAMP;
ALTER TABLE ART_JOB_SCHEDULES ADD CREATION_DATE TIMESTAMP;
ALTER TABLE ART_JOB_SCHEDULES ADD UPDATE_DATE TIMESTAMP;
ALTER TABLE ART_JOBS ADD CREATION_DATE TIMESTAMP;
ALTER TABLE ART_JOBS ADD UPDATE_DATE TIMESTAMP;
ALTER TABLE ART_RULES ADD CREATION_DATE TIMESTAMP;
ALTER TABLE ART_RULES ADD UPDATE_DATE TIMESTAMP;

-- add created by and updated_by columns
ALTER TABLE ART_USERS ADD CREATED_BY VARCHAR(50);
ALTER TABLE ART_USERS ADD UPDATED_BY VARCHAR(50);
ALTER TABLE ART_DATABASES ADD CREATED_BY VARCHAR(50);
ALTER TABLE ART_DATABASES ADD UPDATED_BY VARCHAR(50);
ALTER TABLE ART_QUERY_GROUPS ADD CREATED_BY VARCHAR(50);
ALTER TABLE ART_QUERY_GROUPS ADD UPDATED_BY VARCHAR(50);
ALTER TABLE ART_QUERIES ADD CREATED_BY VARCHAR(50);
ALTER TABLE ART_QUERIES ADD UPDATED_BY VARCHAR(50);
ALTER TABLE ART_RULES ADD CREATED_BY VARCHAR(50);
ALTER TABLE ART_RULES ADD UPDATED_BY VARCHAR(50);
ALTER TABLE ART_JOBS ADD CREATED_BY VARCHAR(50);
ALTER TABLE ART_JOBS ADD UPDATED_BY VARCHAR(50);
ALTER TABLE ART_JOB_SCHEDULES ADD CREATED_BY VARCHAR(50);
ALTER TABLE ART_JOB_SCHEDULES ADD UPDATED_BY VARCHAR(50);
ALTER TABLE ART_USER_GROUPS ADD CREATED_BY VARCHAR(50);
ALTER TABLE ART_USER_GROUPS ADD UPDATED_BY VARCHAR(50);

-- change uses_rules column from varchar to integer
ALTER TABLE ART_QUERIES ADD USES_FILTERS INTEGER;
UPDATE ART_QUERIES SET USES_FILTERS=1 WHERE USES_RULES='Y';
ALTER TABLE ART_QUERIES DROP COLUMN USES_RULES;

-- add report_status column
ALTER TABLE ART_QUERIES ADD REPORT_STATUS VARCHAR(50);
UPDATE ART_QUERIES SET REPORT_STATUS='Active';
UPDATE ART_QUERIES SET REPORT_STATUS='Disabled' WHERE ACTIVE_STATUS='D';
UPDATE ART_QUERIES SET REPORT_STATUS='Hidden' WHERE ACTIVE_STATUS='H';
ALTER TABLE ART_QUERIES DROP COLUMN ACTIVE_STATUS;

-- chage show_parameters column from varchar to integer
ALTER TABLE ART_QUERIES ADD PARAMETERS_IN_OUTPUT INTEGER;
UPDATE ART_QUERIES SET PARAMETERS_IN_OUTPUT=1 WHERE SHOW_PARAMETERS='Y' OR SHOW_PARAMETERS='A';
ALTER TABLE ART_QUERIES DROP COLUMN SHOW_PARAMETERS;

-- increase size of xmla_url column
ALTER TABLE ART_QUERIES MODIFY XMLA_URL VARCHAR(2000);

-- decrease size of message column
UPDATE ART_LOGS SET MESSAGE=SUBSTRING(MESSAGE,1,500);
ALTER TABLE ART_LOGS MODIFY MESSAGE VARCHAR(500);

-- add jndi column
ALTER TABLE ART_DATABASES ADD JNDI INTEGER;
UPDATE ART_DATABASES SET JNDI=1 WHERE DRIVER='' OR DRIVER IS NULL;

-- add schedule_id column
ALTER TABLE ART_JOB_SCHEDULES ADD SCHEDULE_ID INTEGER;

-- add schedule description column
ALTER TABLE ART_JOB_SCHEDULES ADD DESCRIPTION VARCHAR(200);

-- change open in new window column to integer
ALTER TABLE ART_DRILLDOWN_QUERIES ADD OLD_OPEN_IN_NEW_WINDOW VARCHAR(1);
UPDATE ART_DRILLDOWN_QUERIES SET OLD_OPEN_IN_NEW_WINDOW=OPEN_IN_NEW_WINDOW;
ALTER TABLE ART_DRILLDOWN_QUERIES DROP COLUMN OPEN_IN_NEW_WINDOW;
ALTER TABLE ART_DRILLDOWN_QUERIES ADD OPEN_IN_NEW_WINDOW INTEGER;
UPDATE ART_DRILLDOWN_QUERIES SET OPEN_IN_NEW_WINDOW=1 WHERE OLD_OPEN_IN_NEW_WINDOW='Y';
ALTER TABLE ART_DRILLDOWN_QUERIES DROP COLUMN OLD_OPEN_IN_NEW_WINDOW;

-- add drilldown id column
ALTER TABLE ART_DRILLDOWN_QUERIES ADD DRILLDOWN_ID INTEGER;

-- add rule_id columns
ALTER TABLE ART_RULES ADD RULE_ID INTEGER;
ALTER TABLE ART_QUERY_RULES ADD RULE_ID INTEGER;
ALTER TABLE ART_USER_RULES ADD RULE_ID INTEGER;
ALTER TABLE ART_USER_GROUP_RULES ADD RULE_ID INTEGER;

-- add data type column
ALTER TABLE ART_RULES ADD DATA_TYPE VARCHAR(30);
UPDATE ART_RULES SET DATA_TYPE='Varchar';

-- add query rule id column
ALTER TABLE ART_QUERY_RULES ADD QUERY_RULE_ID INTEGER;

-- add rule value fields
ALTER TABLE ART_USER_RULES ADD RULE_VALUE_KEY VARCHAR(50);
ALTER TABLE ART_USER_GROUP_RULES ADD RULE_VALUE_KEY VARCHAR(50);

-- change job_type column to varchar
ALTER TABLE ART_JOBS ADD OLD_JOB_TYPE INTEGER;
UPDATE ART_JOBS SET OLD_JOB_TYPE=JOB_TYPE;
ALTER TABLE ART_JOBS DROP COLUMN JOB_TYPE;
ALTER TABLE ART_JOBS ADD JOB_TYPE VARCHAR(50);
UPDATE ART_JOBS SET JOB_TYPE='Alert' WHERE OLD_JOB_TYPE=1;
UPDATE ART_JOBS SET JOB_TYPE='EmailAttachment' WHERE OLD_JOB_TYPE=2;
UPDATE ART_JOBS SET JOB_TYPE='Publish' WHERE OLD_JOB_TYPE=3;
UPDATE ART_JOBS SET JOB_TYPE='JustRun' WHERE OLD_JOB_TYPE=4;
UPDATE ART_JOBS SET JOB_TYPE='EmailInline' WHERE OLD_JOB_TYPE=5;
UPDATE ART_JOBS SET JOB_TYPE='CondEmailAttachment' WHERE OLD_JOB_TYPE=6;
UPDATE ART_JOBS SET JOB_TYPE='CondEmailInline' WHERE OLD_JOB_TYPE=7;
UPDATE ART_JOBS SET JOB_TYPE='CondPublish' WHERE OLD_JOB_TYPE=8;
UPDATE ART_JOBS SET JOB_TYPE='CacheAppend' WHERE OLD_JOB_TYPE=9;
UPDATE ART_JOBS SET JOB_TYPE='CacheInsert' WHERE OLD_JOB_TYPE=10;
ALTER TABLE ART_JOBS DROP COLUMN OLD_JOB_TYPE;

-- add migrated column
ALTER TABLE ART_QUERY_FIELDS ADD MIGRATED INTEGER;

-- create new parameter tables
CREATE TABLE ART_PARAMETERS
(	
	PARAMETER_ID INTEGER NOT NULL,		
	NAME  VARCHAR(60),
	DESCRIPTION VARCHAR(50),
	PARAMETER_TYPE VARCHAR(30),           
	PARAMETER_LABEL     VARCHAR(50),
	HELP_TEXT            VARCHAR(120),
	DATA_TYPE         VARCHAR(30),
	DEFAULT_VALUE     VARCHAR(80),
	HIDDEN INTEGER,
	USE_LOV INTEGER, 
	LOV_REPORT_ID  INTEGER,
	USE_FILTERS_IN_LOV INTEGER,	
	CHAINED_POSITION  INTEGER,              
	CHAINED_VALUE_POSITION INTEGER,
	DRILLDOWN_COLUMN_INDEX INTEGER,
	USE_DIRECT_SUBSTITUTION INTEGER,	
	CREATION_DATE TIMESTAMP,
	CREATED_BY VARCHAR(50),
	UPDATE_DATE TIMESTAMP,
	UPDATED_BY VARCHAR(50),
	CONSTRAINT ap_pk PRIMARY KEY (PARAMETER_ID)	
);

CREATE TABLE ART_REPORT_PARAMETERS
(	
	REPORT_PARAMETER_ID INTEGER NOT NULL,
	REPORT_ID INTEGER NOT NULL,	
	PARAMETER_ID INTEGER NOT NULL,	
	PARAMETER_POSITION INTEGER NOT NULL,
	CONSTRAINT arp_pk PRIMARY KEY (REPORT_PARAMETER_ID)	
);

-- add reference table for report types
CREATE TABLE ART_REPORT_TYPES
(
	REPORT_TYPE INTEGER NOT NULL,
	DESCRIPTION VARCHAR(100),
	CONSTRAINT art_pk PRIMARY KEY(REPORT_TYPE)
);
-- insert report types
INSERT INTO ART_REPORT_TYPES VALUES (0,'Tabular');
INSERT INTO ART_REPORT_TYPES VALUES (1,'Group: 1 column');
INSERT INTO ART_REPORT_TYPES VALUES (2,'Group: 2 columns');
INSERT INTO ART_REPORT_TYPES VALUES (3,'Group: 3 columns');
INSERT INTO ART_REPORT_TYPES VALUES (4,'Group: 4 columns');
INSERT INTO ART_REPORT_TYPES VALUES (5,'Group: 5 columns');
INSERT INTO ART_REPORT_TYPES VALUES (100,'Update Statement');
INSERT INTO ART_REPORT_TYPES VALUES (101,'Crosstab');
INSERT INTO ART_REPORT_TYPES VALUES (102,'Crosstab (html only)');
INSERT INTO ART_REPORT_TYPES VALUES (103,'Tabular (html only)');
INSERT INTO ART_REPORT_TYPES VALUES (110,'Dashboard');
INSERT INTO ART_REPORT_TYPES VALUES (111,'Text');
INSERT INTO ART_REPORT_TYPES VALUES (112,'Pivot Table: Mondrian');
INSERT INTO ART_REPORT_TYPES VALUES (113,'Pivot Table: Mondrian XMLA');
INSERT INTO ART_REPORT_TYPES VALUES (114,'Pivot Table: SQL Server XMLA');
INSERT INTO ART_REPORT_TYPES VALUES (115,'JasperReport: Template Query');
INSERT INTO ART_REPORT_TYPES VALUES (116,'JasperReport: ART Query');
INSERT INTO ART_REPORT_TYPES VALUES (117,'jXLS Spreadsheet: Template Query');
INSERT INTO ART_REPORT_TYPES VALUES (118,'jXLS Spreadsheet: ART Query');
INSERT INTO ART_REPORT_TYPES VALUES (119,'LOV: Dynamic');
INSERT INTO ART_REPORT_TYPES VALUES (120,'LOV: Static');
INSERT INTO ART_REPORT_TYPES VALUES (121,'Dynamic Job Recipients');
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

-- add reference table for job types
CREATE TABLE ART_JOB_TYPES
(
	JOB_TYPE INTEGER NOT NULL,
	DESCRIPTION VARCHAR(100),
	CONSTRAINT ajt_pk PRIMARY KEY(JOB_TYPE)
);
-- insert job types
INSERT INTO ART_JOB_TYPES VALUES(1,'Alert');
INSERT INTO ART_JOB_TYPES VALUES(2,'Email Output (Attachment)');
INSERT INTO ART_JOB_TYPES VALUES(3,'Publish');
INSERT INTO ART_JOB_TYPES VALUES(4,'Just Run It');
INSERT INTO ART_JOB_TYPES VALUES(5,'Email Output (Inline)');
INSERT INTO ART_JOB_TYPES VALUES(6,'Conditional Email Output (Attachment)');
INSERT INTO ART_JOB_TYPES VALUES(7,'Conditional Email Output (Inline)');
INSERT INTO ART_JOB_TYPES VALUES(8,'Conditional Publish');
INSERT INTO ART_JOB_TYPES VALUES(9,'Cache ResultSet (Append)');
INSERT INTO ART_JOB_TYPES VALUES(10,'Cache ResultSet (Delete & Insert)');

-- add reference table for access levels
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

-- delete quartz tables
DROP TABLE QRTZ_FIRED_TRIGGERS;
DROP TABLE QRTZ_PAUSED_TRIGGER_GRPS;
DROP TABLE QRTZ_SCHEDULER_STATE;
DROP TABLE QRTZ_LOCKS;
DROP TABLE QRTZ_SIMPLE_TRIGGERS;
DROP TABLE QRTZ_SIMPROP_TRIGGERS;
DROP TABLE QRTZ_CRON_TRIGGERS;
DROP TABLE QRTZ_BLOB_TRIGGERS;
DROP TABLE QRTZ_TRIGGERS;
DROP TABLE QRTZ_JOB_DETAILS;
DROP TABLE QRTZ_CALENDARS;

-- update job migrated to quartz status so that all jobs are recreated in the new quartz tables
UPDATE ART_JOBS SET MIGRATED_TO_QUARTZ='N';