DROP TABLE IF EXISTS BasicDataType;
CREATE TABLE BasicDataType
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

INSERT INTO BasicDataType
VALUES (1, 32767, 8388607, 2147483647, 9223372036854775807, 123.456, 1.234567, 'HELLO,WORLD');
INSERT INTO BasicDataType
VALUES (2, 32767, 8388607, 2147483647, 9223372036854775807, 123.456, 1.234567, 'hello,world');

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
