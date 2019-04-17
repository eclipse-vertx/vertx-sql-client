# @Deprecated--- This part is only for mysql tests and should be moved out of TCK tests ---
DROP TABLE IF EXISTS collectorTest;
CREATE TABLE collectorTest
(
  id        INT NOT NULL PRIMARY KEY,
  `Int2`    SMALLINT,
  `Int3`    MEDIUMINT,
  `Int4`    INT,
  `Int8`    BIGINT,
  `Float`   FLOAT,
  `Double`  DOUBLE,
  `Varchar` VARCHAR(20)
);


INSERT INTO collectorTest
VALUES (1, 32767, 8388607, 2147483647, 9223372036854775807, 123.456, 1.234567, 'HELLO,WORLD');
INSERT INTO collectorTest
VALUES (2, 32767, 8388607, 2147483647, 9223372036854775807, 123.456, 1.234567, 'hello,world');

# datatype testing table
DROP TABLE IF EXISTS datatype;
CREATE TABLE datatype
(
  id          INT NOT NULL PRIMARY KEY,
  `Binary`    BINARY(5),
  `VarBinary` VARBINARY(20)
);

INSERT INTO datatype
VALUES (1, 'HELLO', 'HELLO, WORLD');
INSERT INTO datatype
VALUES (2, 'hello', 'hello, world');

# @Deprecated--- This part is only for mysql tests and should be moved out of TCK tests ---

# TFB tables

# To maintain consistency across servers and fix a problem with the jdbc per
# http://stackoverflow.com/questions/37719818/the-server-time-zone-value-aest-is-unrecognized-or-represents-more-than-one-ti
SET GLOBAL time_zone = '+00:00';

# modified from SO answer http://stackoverflow.com/questions/5125096/for-loop-in-mysql
CREATE DATABASE hello_world;
USE testschema;

-- world table
DROP TABLE IF EXISTS world;
CREATE TABLE world (
  id int(10) unsigned NOT NULL auto_increment,
  randomNumber int NOT NULL default 0,
  PRIMARY KEY  (id)
)
ENGINE=INNODB;
GRANT SELECT, UPDATE ON world TO 'benchmarkdbuser'@'%' IDENTIFIED BY 'benchmarkdbpass';
GRANT SELECT, UPDATE ON world TO 'benchmarkdbuser'@'localhost' IDENTIFIED BY 'benchmarkdbpass';

DELIMITER #
CREATE PROCEDURE load_data()
BEGIN

declare v_max int unsigned default 10000;
declare v_counter int unsigned default 0;

  TRUNCATE TABLE world;
  START TRANSACTION;
  while v_counter < v_max do
    INSERT INTO world (randomNumber) VALUES ( floor(0 + (rand() * 10000)) );
    SET v_counter=v_counter+1;
  end while;
  commit;
END #

DELIMITER ;

CALL load_data();

-- Fortune table
DROP TABLE IF EXISTS Fortune;
CREATE TABLE Fortune (
  id int(10) unsigned NOT NULL auto_increment,
  message varchar(2048) CHARACTER SET 'utf8' NOT NULL,
  PRIMARY KEY  (id)
)
ENGINE=INNODB;
GRANT SELECT ON Fortune TO 'mysql'@'%' IDENTIFIED BY 'password';
GRANT SELECT ON Fortune TO 'mysql'@'localhost' IDENTIFIED BY 'password';

INSERT INTO Fortune (message) VALUES ('fortune: No such file or directory');
INSERT INTO Fortune (message) VALUES ('A computer scientist is someone who fixes things that aren''t broken.');
INSERT INTO Fortune (message) VALUES ('After enough decimal places, nobody gives a damn.');
INSERT INTO Fortune (message) VALUES ('A bad random number generator: 1, 1, 1, 1, 1, 4.33e+67, 1, 1, 1');
INSERT INTO Fortune (message) VALUES ('A computer program does what you tell it to do, not what you want it to do.');
INSERT INTO Fortune (message) VALUES ('Emacs is a nice operating system, but I prefer UNIX. — Tom Christaensen');
INSERT INTO Fortune (message) VALUES ('Any program that runs right is obsolete.');
INSERT INTO Fortune (message) VALUES ('A list is only as strong as its weakest link. — Donald Knuth');
INSERT INTO Fortune (message) VALUES ('Feature: A bug with seniority.');
INSERT INTO Fortune (message) VALUES ('Computers make very fast, very accurate mistakes.');
INSERT INTO Fortune (message) VALUES ('<script>alert("This should not be displayed in a browser alert box.");</script>');
INSERT INTO Fortune (message) VALUES ('フレームワークのベンチマーク');

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

-- table for test ANSI SQL data type codecs
-- MySQL specific so that it works well with TCK, see https://dev.mysql.com/doc/refman/8.0/en/sql-mode.html#sqlmode_ansi
SET sql_mode = 'ANSI';
-- MySQL specific
DROP TABLE IF EXISTS basicdatatype;
CREATE TABLE basicdatatype
(
  id           INTEGER          NOT NULL,
  test_int_2   SMALLINT         NOT NULL,
  test_int_4   INTEGER          NOT NULL,
  test_int_8   BIGINT           NOT NULL,
  test_float_4 REAL             NOT NULL,
  test_float_8 DOUBLE PRECISION NOT NULL,
  test_numeric NUMERIC(5, 2)    NOT NULL,
  test_decimal DECIMAL          NOT NULL,
  test_boolean BOOLEAN          NOT NULL,
  test_char    CHAR(8)          NOT NULL,
  test_varchar VARCHAR(20)      NOT NULL,
  test_date    DATE             NOT NULL,
  test_time    TIME             NOT NULL
);
INSERT INTO basicdatatype(id, test_int_2, test_int_4, test_int_8, test_float_4, test_float_8, test_numeric,
                          test_decimal, test_boolean, test_char, test_varchar, test_date, test_time)
VALUES ('1', '32767', '2147483647', '9223372036854775807', '3.40282E38', '1.7976931348623157E308', '999.99',
        '12345', TRUE, 'testchar', 'testvarchar', '2019-01-01', '18:45:02');
INSERT INTO basicdatatype(id, test_int_2, test_int_4, test_int_8, test_float_4, test_float_8, test_numeric,
                          test_decimal, test_boolean, test_char, test_varchar, test_date, test_time)
VALUES ('2', '32767', '2147483647', '9223372036854775807', '3.40282E38', '1.7976931348623157E308', '999.99',
        '12345', TRUE, 'testchar', 'testvarchar', '2019-01-01', '18:45:02');
-- table for test ANSI SQL data type codecs

-- TCK usage --
