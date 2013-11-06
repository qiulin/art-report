-- Upgrade script from ART 2.5.2 to ART 3.0
--
-- Purpose: create/update the tables needed to 
--          . update database version
--          . reset x_axis_label for non-graph queries
--          . allow datasources to be disabled
--          . add reference table for query types
--          . add reference table for job types
--          . change active_status fields from varchar to integer
--
-- ------------------------------------------------


-- update database version
DROP TABLE ART_SETTINGS;
CREATE TABLE ART_DATABASE_VERSION
(
	DATABASE_VERSION VARCHAR(50)
);
-- insert database version
INSERT INTO ART_DATABASE_VERSION VALUES('3.0-alpha1');

-- reset x_axis_label for non-graph queries
UPDATE ART_QUERIES SET X_AXIS_LABEL='' WHERE QUERY_TYPE>=0;

-- allow datasources to be disabled
ALTER TABLE ART_DATABASES ADD ACTIVE INTEGER;
UPDATE ART_DATABASES SET ACTIVE=1;

-- add reference table for query types
CREATE TABLE ART_QUERY_TYPES
(
	QUERY_TYPE INTEGER NOT NULL PRIMARY KEY,
	DESCRIPTION VARCHAR(50)
);
-- insert query types
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (0,'Tabular');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (1,'Group: 1 column');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (2,'Group: 2 columns');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (3,'Group: 3 columns');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (4,'Group: 4 columns');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (5,'Group: 5 columns');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (100,'Update Statement');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (101,'Crosstab');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (102,'Crosstab (html only)');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (103,'Tabular (html only)');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (110,'Dashboard');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (111,'Text');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (112,'Pivot Table: Mondrian');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (113,'Pivot Table: Mondrian XMLA');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (114,'Pivot Table: Microsoft XMLA');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (115,'Jasper Report: Template Query');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (116,'Jasper Report: ART Query');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (117,'jXLS Spreadsheet: Template Query');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (118,'jXLS Spreadsheet: ART Query');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (119,'LOV: Dynamic');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (120,'LOV: Static');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (121,'Dynamic Job Recipients');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (-1,'Chart: XY Chart');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (-2,'Chart: Pie 3D');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (-3,'Chart: Horizontal Bar 3D');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (-4,'Chart: Vertical Bar 3D');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (-5,'Chart: Line');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (-6,'Chart: Time Series');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (-7,'Chart: Date Series');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (-8,'Chart: Stacked Vertical Bar 3D');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (-9,'Chart: Stacked Horizontal Bar 3D');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (-10,'Chart: Speedometer');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (-11,'Chart: Bubble Chart');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (-12,'Chart: Heat Map');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (-13,'Chart: Pie 2D');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (-14,'Chart: Vertical Bar 2D');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (-15,'Chart: Stacked Vertical Bar 2D');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (-16,'Chart: Horizontal Bar 2D');
INSERT INTO ART_QUERY_TYPES (QUERY_TYPE, DESCRIPTION) VALUES (-17,'Chart: Stacked Horizontal Bar 2D');

-- add reference table for job types
CREATE TABLE ART_JOB_TYPES
(
	JOB_TYPE INTEGER NOT NULL PRIMARY KEY,
	DESCRIPTION VARCHAR(100)
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

-- change active_status fields from varchar to integer
ALTER TABLE ART_USERS ADD ACTIVE INTEGER;
UPDATE ART_USERS SET ACTIVE=1 WHERE ACTIVE_STATUS='A' OR ACTIVE_STATUS IS NULL;





