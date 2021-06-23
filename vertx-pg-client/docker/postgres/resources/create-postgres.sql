DROP TYPE IF EXISTS weather CASCADE;
DROP TYPE IF EXISTS mood CASCADE;
DROP TYPE IF EXISTS full_address CASCADE;

CREATE TYPE weather AS ENUM ('sunny', 'cloudy', 'rainy');
CREATE TYPE mood AS ENUM ('unhappy', 'ok', 'happy');

CREATE TYPE full_address AS (city TEXT, street TEXT, home BOOLEAN);

-- scram-sha-256 is only available for versions >= 10
DO $$ BEGIN
  IF (select Substr(setting, 1, 1) from pg_settings where name = 'server_version_num') <> '9' THEN
    DROP USER IF EXISTS saslscram;
    set password_encryption = 'scram-sha-256';
    create user saslscram with SUPERUSER encrypted password 'saslscrampwd';
    alter role saslscram set password_encryption = 'scram-sha-256';
    alter role saslscram with password 'saslscrampwd';
    grant all privileges on database postgres to saslscram;
    GRANT ALL ON ALL TABLES IN SCHEMA public TO saslscram;
    GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO saslscram;
  END IF;
END $$;

-- World table
DROP TABLE IF EXISTS World;
CREATE TABLE  World (
  id integer NOT NULL,
  randomNumber integer NOT NULL default 0,
  PRIMARY KEY  (id)
);

INSERT INTO World (id, randomnumber)
SELECT x.id, random() * 10000 + 1 FROM generate_series(1,10000) as x(id);

-- Fortune table
DROP TABLE IF EXISTS Fortune;
CREATE TABLE Fortune (
  id integer NOT NULL,
  message varchar(2048) NOT NULL,
  PRIMARY KEY  (id)
);

INSERT INTO Fortune (id, message) VALUES (1, 'fortune: No such file or directory');
INSERT INTO Fortune (id, message) VALUES (2, 'A computer scientist is someone who fixes things that aren''t broken.');
INSERT INTO Fortune (id, message) VALUES (3, 'After enough decimal places, nobody gives a damn.');
INSERT INTO Fortune (id, message) VALUES (4, 'A bad random number generator: 1, 1, 1, 1, 1, 4.33e+67, 1, 1, 1');
INSERT INTO Fortune (id, message) VALUES (5, 'A computer program does what you tell it to do, not what you want it to do.');
INSERT INTO Fortune (id, message) VALUES (6, 'Emacs is a nice operating system, but I prefer UNIX. — Tom Christaensen');
INSERT INTO Fortune (id, message) VALUES (7, 'Any program that runs right is obsolete.');
INSERT INTO Fortune (id, message) VALUES (8, 'A list is only as strong as its weakest link. — Donald Knuth');
INSERT INTO Fortune (id, message) VALUES (9, 'Feature: A bug with seniority.');
INSERT INTO Fortune (id, message) VALUES (10, 'Computers make very fast, very accurate mistakes.');
INSERT INTO Fortune (id, message) VALUES (11, '<script>alert("This should not be displayed in a browser alert box.");</script>');
INSERT INTO Fortune (id, message) VALUES (12, 'フレームワークのベンチマーク');

-- All purpose testing table
DROP TABLE IF EXISTS Test;
CREATE TABLE Test (
  id integer NOT NULL,
  val varchar(2048) NOT NULL,
  PRIMARY KEY  (id)
);

DROP TABLE IF EXISTS "NumericDataType";
CREATE TABLE "NumericDataType" (
  "id" INTEGER NOT NULL PRIMARY KEY,
  "Short" INT2,
  "Integer" INT4,
  "Long" INT8,
  "Float" FLOAT4,
  "Double" FLOAT8,
  "BigDecimal" NUMERIC,
  "Boolean" BOOLEAN,
  "SmallSerial" SERIAL2,
  "Serial" SERIAL4,
  "BigSerial" SERIAL8
);

INSERT INTO "NumericDataType" ("id", "Short", "Integer", "Long", "Float", "Double", "BigDecimal", "Boolean")
VALUES (1, 32767, 2147483647, 9223372036854775807, 3.4028235E38, 1.7976931348623157E308, '9.99999999999999999999999999999999999', true);
INSERT INTO "NumericDataType" ("id", "Short", "Integer", "Long", "Float", "Double", "BigDecimal", "Boolean")
VALUES (2, 32767, 2147483647, 9223372036854775807, 3.4028235E38, 1.7976931348623157E308, '9.99999999999999999999999999999999999', true);

DROP TABLE IF EXISTS "TemporalDataType";
CREATE TABLE "TemporalDataType" ("id" INTEGER NOT NULL PRIMARY KEY, "Date" date, "Time" time without time zone, "TimeTz" time with time zone, "Timestamp" timestamp without time zone, "TimestampTz" timestamp with time zone, "Interval" interval);
INSERT INTO "TemporalDataType" ("id" ,"Date", "Time", "TimeTz", "Timestamp", "TimestampTz", "Interval") VALUES (1 ,'1981-05-30', '17:55:04.90512', '17:55:04.90512+03:07', '2017-05-14 19:35:58.237666', '2017-05-14 23:59:59.237666-03', '10 years 3 months 332 days 20 hours 20 minutes 20.999999 seconds');
INSERT INTO "TemporalDataType" ("id" ,"Date", "Time", "TimeTz", "Timestamp", "TimestampTz", "Interval") VALUES (2 ,'2017-05-30', '12:55:04.90512', '02:55:04.90512+03:07', '1909-05-14 19:35:58.237666', '1909-05-14 22:35:58.237666-03', '02:01:33');
INSERT INTO "TemporalDataType" ("id" ,"Date", "Time", "TimeTz", "Timestamp", "TimestampTz", "Interval") VALUES (3 ,'1900-01-01', '23:59:04.90512', '08:08:03.90512+03:07', '1800-01-01 23:57:53.237666', '1800-01-01 23:59:59.237666-03', '04:33:59');
INSERT INTO "TemporalDataType" ("id" ,"Date", "Time", "TimeTz", "Timestamp", "TimestampTz", "Interval") VALUES (4 ,'1900-01-01', '23:59:04.90512', '08:08:03.90512+03:07', '1800-01-01 23:57:53.237666', '1800-01-01 23:59:59.237666-03', '04:33:59');

DROP TABLE IF EXISTS "CharacterDataType";
CREATE TABLE "CharacterDataType" (
  "id" INTEGER NOT NULL PRIMARY KEY,
  "Name" NAME,
  "SingleChar" CHAR,
  "FixedChar" CHAR(3),
  "Text" TEXT,
  "VarCharacter" VARCHAR,
  "uuid" UUID
);
INSERT INTO "CharacterDataType" ("id" ,"Name", "SingleChar", "FixedChar", "Text", "VarCharacter", "uuid") VALUES (1, 'What is my name ?', 'A', 'YES', 'Hello World', 'Great!', '6f790482-b5bd-438b-a8b7-4a0bed747011');
INSERT INTO "CharacterDataType" ("id" ,"Name", "SingleChar", "FixedChar", "Text", "VarCharacter", "uuid") VALUES (2, 'What is my name ?', 'A', 'YES', 'Hello World', 'Great!', '6f790482-b5bd-438b-a8b7-4a0bed747011');

DROP TABLE IF EXISTS "JsonDataType";
CREATE TABLE "JsonDataType" (
  "id" INTEGER NOT NULL PRIMARY KEY,
  "JsonObject" JSON,
  "JsonArray" JSON,
  "Number" JSON,
  "String" JSON,
  "BooleanTrue" JSON,
  "BooleanFalse" JSON,
  "NullValue" JSON,
  "Null" JSON
);
INSERT INTO "JsonDataType" ("id" ,"JsonObject", "JsonArray", "Number", "String", "BooleanTrue", "BooleanFalse", "NullValue", "Null") VALUES (1, '  {"str":"blah", "int" : 1, "float" : 3.5, "object": {}, "array" : []   }', '[1,true,null,9.5,"Hi"]', '4', '"Hello World"', 'true', 'false', 'null', NULL);
INSERT INTO "JsonDataType" ("id" ,"JsonObject", "JsonArray", "Number", "String", "BooleanTrue", "BooleanFalse", "NullValue", "Null") VALUES (2, '  {"str":"blah", "int" : 1, "float" : 3.5, "object": {}, "array" : []   }', '[1,true,null,9.5,"Hi"]', '4', '"Hello World"', 'true', 'false', 'null', NULL);

DROP TABLE IF EXISTS "JsonbDataType";
CREATE TABLE "JsonbDataType" (
  "id" INTEGER NOT NULL PRIMARY KEY,
  "JsonObject" JSONB,
  "JsonArray" JSONB,
  "Number" JSONB,
  "String" JSONB,
  "BooleanTrue" JSONB,
  "BooleanFalse" JSONB,
  "NullValue" JSONB,
  "Null" JSONB
);
INSERT INTO "JsonbDataType" ("id" ,"JsonObject", "JsonArray", "Number", "String", "BooleanTrue", "BooleanFalse", "NullValue", "Null") VALUES (1, '  {"str":"blah", "int" : 1, "float" : 3.5, "object": {}, "array" : []   }', '[1,true,null,9.5,"Hi"]', '4', '"Hello World"', 'true', 'false', 'null', NULL);
INSERT INTO "JsonbDataType" ("id" ,"JsonObject", "JsonArray", "Number", "String", "BooleanTrue", "BooleanFalse", "NullValue", "Null") VALUES (2, '  {"str":"blah", "int" : 1, "float" : 3.5, "object": {}, "array" : []   }', '[1,true,null,9.5,"Hi"]', '4', '"Hello World"', 'true', 'false', 'null', NULL);

-- Geometric table
DROP TABLE IF EXISTS "GeometricDataType";
CREATE TABLE "GeometricDataType" (
  "id" INTEGER NOT NULL PRIMARY KEY,
  "Point" POINT,
  "Line" LINE,
  "Lseg" LSEG,
  "Box" BOX,
  "ClosedPath" PATH,
  "OpenPath" PATH,
  "Polygon" POLYGON,
  "Circle" CIRCLE
);

INSERT INTO "GeometricDataType" ("id", "Point", "Line", "Lseg", "Box", "ClosedPath", "OpenPath", "Polygon", "Circle")
VALUES (1, '(1.0,2.0)':: POINT, '{1.0,2.0,3.0}':: LINE, '((1.0,1.0),(2.0,2.0))':: LSEG, '((2.0,2.0),(1.0,1.0))':: BOX, '((1.0,1.0),(2.0,1.0),(2.0,2.0),(2.0,1.0))':: PATH,'[(1.0,1.0),(2.0,1.0),(2.0,2.0),(2.0,1.0)]':: PATH, '((1.0,1.0),(2.0,2.0),(3.0,1.0))':: POLYGON, '<(1.0,1.0),1.0>':: CIRCLE);
INSERT INTO "GeometricDataType" ("id", "Point", "Line", "Lseg", "Box", "ClosedPath", "OpenPath", "Polygon", "Circle")
VALUES (2, '(1.0,2.0)':: POINT, '{1.0,2.0,3.0}':: LINE, '((1.0,1.0),(2.0,2.0))':: LSEG, '((2.0,2.0),(1.0,1.0))':: BOX, '((1.0,1.0),(2.0,1.0),(2.0,2.0),(2.0,1.0))':: PATH,'[(1.0,1.0),(2.0,1.0),(2.0,2.0),(2.0,1.0)]':: PATH, '((1.0,1.0),(2.0,2.0),(3.0,1.0))':: POLYGON, '<(1.0,1.0),1.0>':: CIRCLE);


DROP TABLE IF EXISTS "ArrayDataType";
CREATE TABLE "ArrayDataType" (
  "id"             INTEGER NOT NULL PRIMARY KEY,
  "Boolean"        BOOLEAN [],
  "Short"          INT2 [],
  "Integer"        INT4 [],
  "Long"           INT8 [],
  "Float"          FLOAT4 [],
  "Double"         FLOAT8 [],
  "Char"           CHAR(8) [],
  "Varchar"        VARCHAR [],
  "Text"           TEXT [],
  "Name"           NAME [],
  "LocalDate"      DATE [],
  "LocalTime"      TIME WITHOUT TIME ZONE [],
  "OffsetTime"     TIME WITH TIME ZONE [],
  "LocalDateTime"  TIMESTAMP WITHOUT TIME ZONE [],
  "OffsetDateTime" TIMESTAMP WITH TIME ZONE [],
  "UUID"           UUID [],
  "Numeric"        NUMERIC [],
  "Bytea"          BYTEA[],
  "JSON"           JSON[],
  "JSONB"          JSONB[],
  "Point"          POINT[],
  "Line"           LINE[],
  "Lseg"           LSEG[],
  "Box"            BOX[],
  "ClosedPath"     PATH[],
  "OpenPath"       PATH[],
  "Polygon"        POLYGON[],
  "Circle"         CIRCLE[],
  "Enum"           mood[],
  "Interval"       INTERVAL [],
  "CustomType"     full_address[]
);
INSERT INTO "ArrayDataType" VALUES (1, ARRAY [TRUE],
                                       ARRAY [1],
                                       ARRAY [2],
                                       ARRAY [3],
                                       ARRAY [4.1],
                                       ARRAY [5.2],
                                       ARRAY ['01234567'],
                                       ARRAY ['Knock, knock.Who’s there?very long pause….Java.'],
                                       ARRAY ['Knock, knock.Who’s there?very long pause….Java.'],
                                       ARRAY ['Knock, knock.Who’s there?very long pause….Java.'],
                                       ARRAY ['1998-05-11' :: DATE, '1998-05-11' :: DATE],
                                       ARRAY ['17:55:04.90512' :: TIME WITHOUT TIME ZONE],
                                       ARRAY ['17:55:04.90512+03' :: TIME WITH TIME ZONE],
                                       ARRAY ['2017-05-14 19:35:58.237666' :: TIMESTAMP WITHOUT TIME ZONE],
                                       ARRAY ['2017-05-14 23:59:59.237666-03' :: TIMESTAMP WITH TIME ZONE],
                                       ARRAY ['6f790482-b5bd-438b-a8b7-4a0bed747011' :: UUID],
                                       ARRAY [0,1,2,3],
                                       ARRAY [decode('48454c4c4f', 'hex')],
                                       ARRAY ['  {"str":"blah", "int" : 1, "float" : 3.5, "object": {}, "array" : []   }' :: JSON, '[1,true,null,9.5,"Hi"]' :: JSON, '4' :: JSON, '"Hello World"' :: JSON, 'true' :: JSON, 'false' :: JSON, 'null' :: JSON],
                                       ARRAY ['  {"str":"blah", "int" : 1, "float" : 3.5, "object": {}, "array" : []   }' :: JSON, '[1,true,null,9.5,"Hi"]' :: JSON, '4' :: JSON, '"Hello World"' :: JSON, 'true' :: JSON, 'false' :: JSON, 'null' :: JSON],
                                       ARRAY ['(1.0,1.0)':: POINT, '(2.0,2.0)' :: POINT],
                                       ARRAY ['{1.0,2.0,3.0}':: LINE, '{2.0,3.0,4.0}':: LINE],
                                       ARRAY ['((1.0,1.0),(2.0,2.0))':: LSEG, '((2.0,2.0),(3.0,3.0))':: LSEG],
                                       ARRAY ['((2.0,2.0),(1.0,1.0))':: BOX, '((3.0,3.0),(2.0,2.0))':: BOX],
                                       ARRAY ['((1.0,1.0),(2.0,1.0),(2.0,2.0),(2.0,1.0))':: PATH, '((2.0,2.0),(3.0,2.0),(3.0,3.0),(3.0,2.0))':: PATH],
                                       ARRAY ['[(1.0,1.0),(2.0,1.0),(2.0,2.0),(2.0,1.0)]':: PATH, '[(2.0,2.0),(3.0,2.0),(3.0,3.0),(3.0,2.0)]':: PATH],
                                       ARRAY ['((1.0,1.0),(2.0,2.0),(3.0,1.0))':: POLYGON, '((0.0,0.0),(0.0,1.0),(1.0,2.0),(2.0,1.0),(2.0,0.0))':: POLYGON],
                                       ARRAY ['<(1.0,1.0),1.0>':: CIRCLE, '<(0.0,0.0),2.0>':: CIRCLE],
                                       ARRAY['ok'::mood,'unhappy'::mood, 'happy'::mood],
                                       ARRAY['10 years 3 months 332 days 20 hours 20 minutes 20.999991 seconds'::INTERVAL, '20 minutes 20.123456 seconds'::INTERVAL, '30 months ago'::INTERVAL],
                                       ARRAY [ROW('Anytown', 'Main St', true)::full_address, ('Anytown', 'First St', false)::full_address]);
INSERT INTO "ArrayDataType" VALUES (2, ARRAY [TRUE],
                                       ARRAY [1],
                                       ARRAY [2],
                                       ARRAY [3],
                                       ARRAY [4.1],
                                       ARRAY [5.2],
                                       ARRAY ['01234567'],
                                       ARRAY ['Knock, knock.Who’s there?very long pause….Java.'],
                                       ARRAY ['Knock, knock.Who’s there?very long pause….Java.'],
                                       ARRAY ['Knock, knock.Who’s there?very long pause….Java.'],
                                       ARRAY ['1998-05-11' :: DATE], ARRAY ['17:55:04.90512' :: TIME WITHOUT TIME ZONE],
                                       ARRAY ['17:55:04.90512+03' :: TIME WITH TIME ZONE],
                                       ARRAY ['2017-05-14 19:35:58.237666' :: TIMESTAMP WITHOUT TIME ZONE],
                                       ARRAY ['2017-05-14 23:59:59.237666-03' :: TIMESTAMP WITH TIME ZONE],
                                       ARRAY ['6f790482-b5bd-438b-a8b7-4a0bed747011' :: UUID],
                                       ARRAY [0,1,2,3],
                                       ARRAY [decode('48454c4c4f', 'hex')],
                                       ARRAY ['  {"str":"blah", "int" : 1, "float" : 3.5, "object": {}, "array" : []   }' :: JSON, '[1,true,null,9.5,"Hi"]' :: JSON, '4' :: JSON, '"Hello World"' :: JSON, 'true' :: JSON, 'false' :: JSON, 'null' :: JSON],
                                       ARRAY ['  {"str":"blah", "int" : 1, "float" : 3.5, "object": {}, "array" : []   }' :: JSON, '[1,true,null,9.5,"Hi"]' :: JSON, '4' :: JSON, '"Hello World"' :: JSON, 'true' :: JSON, 'false' :: JSON, 'null' :: JSON],
                                       ARRAY ['(1.0,1.0)':: POINT, '(2.0,2.0)' :: POINT],
                                       ARRAY ['{1.0,2.0,3.0}':: LINE, '{2.0,3.0,4.0}':: LINE],
                                       ARRAY ['((1.0,1.0),(2.0,2.0))':: LSEG, '((2.0,2.0),(3.0,3.0))':: LSEG],
                                       ARRAY ['((2.0,2.0),(1.0,1.0))':: BOX, '((3.0,3.0),(2.0,2.0))':: BOX],
                                       ARRAY ['((1.0,1.0),(2.0,1.0),(2.0,2.0),(2.0,1.0))':: PATH, '((2.0,2.0),(3.0,2.0),(3.0,3.0),(3.0,2.0))':: PATH],
                                       ARRAY ['[(1.0,1.0),(2.0,1.0),(2.0,2.0),(2.0,1.0)]':: PATH, '[(2.0,2.0),(3.0,2.0),(3.0,3.0),(3.0,2.0)]':: PATH],
                                       ARRAY ['((1.0,1.0),(2.0,2.0),(3.0,1.0))':: POLYGON, '((0.0,0.0),(0.0,1.0),(1.0,2.0),(2.0,1.0),(2.0,0.0))':: POLYGON],
                                       ARRAY ['<(1.0,1.0),1.0>':: CIRCLE, '<(0.0,0.0),2.0>':: CIRCLE],
                                       ARRAY['unhappy'::mood, 'happy'::mood],
                                       ARRAY['0 years 0 months 0 days 0 hours 0 minutes 0 seconds'::INTERVAL],
                                       ARRAY [ROW('Anytown', 'Main St', true)::full_address, ('Anytown', 'First St', false)::full_address]);

DROP TABLE IF EXISTS "EnumDataType";
CREATE TABLE "EnumDataType" (
  "id" INTEGER NOT NULL PRIMARY KEY,
  "currentMood" mood,
  "currentWeather" weather
);
INSERT INTO "EnumDataType" ("id", "currentMood", "currentWeather") VALUES (1, 'ok', 'sunny');
INSERT INTO "EnumDataType" ("id", "currentMood", "currentWeather") VALUES (2, 'unhappy', 'cloudy');
INSERT INTO "EnumDataType" ("id", "currentMood", "currentWeather") VALUES (3, 'happy', 'rainy');
INSERT INTO "EnumDataType" ("id", "currentMood", "currentWeather") VALUES (4, null, null);
INSERT INTO "EnumDataType" ("id", "currentMood", "currentWeather") VALUES (5, 'ok', 'sunny');

DROP TABLE IF EXISTS "CustomDataType";
CREATE TABLE "CustomDataType" (
  "id" INTEGER NOT NULL PRIMARY KEY,
  "address" full_address
);
INSERT INTO "CustomDataType" ("id", "address") VALUES (1, ('Anytown', 'Main St', true));
INSERT INTO "CustomDataType" ("id", "address") VALUES (2, ('Anytown', 'First St', false));

CREATE TABLE "AllDataTypes"
(
  boolean     BOOLEAN,
  int2        INT2,
  int4        INT4,
  int8        INT8,
  float4      FLOAT4,
  float8      FLOAT8,
  char        CHAR,
  varchar     VARCHAR,
  text        TEXT,
  enum        mood,
  name        NAME,
  numeric     NUMERIC,
  uuid        UUID,
  date        DATE,
  time        TIME,
  timetz      TIMETZ,
  timestamp   TIMESTAMP,
  timestamptz TIMESTAMPTZ,
  interval    INTERVAL,
  bytea       BYTEA,
  json        JSON,
  jsonb       JSONB,
  point       POINT,
  line        LINE,
  lseg        LSEG,
  box         BOX,
  path        PATH,
  polygon     POLYGON,
  circle      CIRCLE
);

-- TCK usage --
-- immutable for select query testing --
DROP TABLE IF EXISTS immutable;
CREATE TABLE immutable
(
  id      integer       NOT NULL,
  message varchar(2048) NOT NULL,
  PRIMARY KEY (id)
);

INSERT INTO immutable (id, message)
VALUES (1, 'fortune: No such file or directory');
INSERT INTO immutable (id, message)
VALUES (2, 'A computer scientist is someone who fixes things that aren''t broken.');
INSERT INTO immutable (id, message)
VALUES (3, 'After enough decimal places, nobody gives a damn.');
INSERT INTO immutable (id, message)
VALUES (4, 'A bad random number generator: 1, 1, 1, 1, 1, 4.33e+67, 1, 1, 1');
INSERT INTO immutable (id, message)
VALUES (5, 'A computer program does what you tell it to do, not what you want it to do.');
INSERT INTO immutable (id, message)
VALUES (6, 'Emacs is a nice operating system, but I prefer UNIX. — Tom Christaensen');
INSERT INTO immutable (id, message)
VALUES (7, 'Any program that runs right is obsolete.');
INSERT INTO immutable (id, message)
VALUES (8, 'A list is only as strong as its weakest link. — Donald Knuth');
INSERT INTO immutable (id, message)
VALUES (9, 'Feature: A bug with seniority.');
INSERT INTO immutable (id, message)
VALUES (10, 'Computers make very fast, very accurate mistakes.');
INSERT INTO immutable (id, message)
VALUES (11, '<script>alert("This should not be displayed in a browser alert box.");</script>');
INSERT INTO immutable (id, message)
VALUES (12, 'フレームワークのベンチマーク');
-- immutable for select query testing --

-- mutable for insert,update,delete query testing
DROP TABLE IF EXISTS mutable;
CREATE TABLE mutable
(
  id  integer       NOT NULL,
  val varchar(2048) NOT NULL,
  PRIMARY KEY (id)
);
-- mutable for insert,update,delete query testing

-- unstable table for table schema changing testing
DROP TABLE IF EXISTS unstable;
CREATE TABLE unstable
(
  id      integer       NOT NULL,
  message varchar(2048) NOT NULL,
  PRIMARY KEY (id)
);

INSERT INTO unstable (id, message)
VALUES (1, 'fortune: No such file or directory');
-- unstable table for table schema changing testing

-- test transaction with constraint check deferred until commit
DROP TABLE IF EXISTS deferred_constraints;
CREATE TABLE deferred_constraints (name varchar(50) primary key, parent varchar(50));
ALTER TABLE deferred_constraints ADD CONSTRAINT the_parent_key FOREIGN KEY (parent) REFERENCES deferred_constraints(name) DEFERRABLE INITIALLY DEFERRED;
--

-- table for test ANSI SQL data type codecs
DROP TABLE IF EXISTS basicdatatype;
CREATE TABLE basicdatatype
(
    id           INTEGER,
    test_int_2   SMALLINT,
    test_int_4   INTEGER,
    test_int_8   BIGINT,
    test_float_4 REAL,
    test_float_8 DOUBLE PRECISION,
    test_numeric NUMERIC(5, 2),
    test_decimal DECIMAL,
    test_boolean BOOLEAN,
    test_char    CHAR(8),
    test_varchar VARCHAR(20),
    test_date    DATE,
    test_time    TIME
);
INSERT INTO basicdatatype(id, test_int_2, test_int_4, test_int_8, test_float_4, test_float_8, test_numeric,
                          test_decimal, test_boolean, test_char, test_varchar, test_date, test_time)
VALUES (1, 32767, 2147483647, 9223372036854775807, 3.40282E38, 1.7976931348623157E308, 999.99,
        12345, TRUE, 'testchar', 'testvarchar', '2019-01-01', '18:45:02');
INSERT INTO basicdatatype(id, test_int_2, test_int_4, test_int_8, test_float_4, test_float_8, test_numeric,
                          test_decimal, test_boolean, test_char, test_varchar, test_date, test_time)
VALUES (2, 32767, 2147483647, 9223372036854775807, 3.40282E38, 1.7976931348623157E308, 999.99,
        12345, TRUE, 'testchar', 'testvarchar', '2019-01-01', '18:45:02');
INSERT INTO basicdatatype(id, test_int_2, test_int_4, test_int_8, test_float_4, test_float_8, test_numeric,
                          test_decimal, test_boolean, test_char, test_varchar, test_date, test_time)
VALUES (3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
-- table for test ANSI SQL data type codecs

-- Collector API testing
DROP TABLE IF EXISTS collector_test;
CREATE TABLE collector_test
(
    id           INT4,
    test_int_2   INT2,
    test_int_4   INT4,
    test_int_8   INT8,
    test_float   FLOAT4,
    test_double  FLOAT8,
    test_varchar VARCHAR(20)
);

INSERT INTO collector_test
VALUES (1, 32767, 2147483647, 9223372036854775807, 123.456, 1.234567, 'HELLO,WORLD');
INSERT INTO collector_test
VALUES (2, 32767, 2147483647, 9223372036854775807, 123.456, 1.234567, 'hello,world');

-- TCK usage --

