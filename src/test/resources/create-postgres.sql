
DROP TABLE IF EXISTS World;
CREATE TABLE  World (
  id integer NOT NULL,
  randomNumber integer NOT NULL default 0,
  PRIMARY KEY  (id)
);
GRANT SELECT, UPDATE ON World to benchmarkdbuser;

INSERT INTO World (id, randomnumber)
SELECT x.id, random() * 10000 + 1 FROM generate_series(1,10000) as x(id);

DROP TABLE IF EXISTS Fortune;
CREATE TABLE Fortune (
  id integer NOT NULL,
  message varchar(2048) NOT NULL,
  PRIMARY KEY  (id)
);
GRANT SELECT ON Fortune to benchmarkdbuser;

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


DROP TABLE IF EXISTS "World";
CREATE TABLE  "World" (
  id integer NOT NULL,
  randomNumber integer NOT NULL default 0,
  PRIMARY KEY  (id)
);
GRANT SELECT, UPDATE ON "World" to benchmarkdbuser;

INSERT INTO "World" (id, randomnumber)
SELECT x.id, random() * 10000 + 1 FROM generate_series(1,10000) as x(id);

DROP TABLE IF EXISTS "Fortune";
CREATE TABLE "Fortune" (
  id integer NOT NULL,
  message varchar(2048) NOT NULL,
  PRIMARY KEY  (id)
);
GRANT SELECT ON "Fortune" to benchmarkdbuser;

INSERT INTO "Fortune" (id, message) VALUES (1, 'fortune: No such file or directory');
INSERT INTO "Fortune" (id, message) VALUES (2, 'A computer scientist is someone who fixes things that aren''t broken.');
INSERT INTO "Fortune" (id, message) VALUES (3, 'After enough decimal places, nobody gives a damn.');
INSERT INTO "Fortune" (id, message) VALUES (4, 'A bad random number generator: 1, 1, 1, 1, 1, 4.33e+67, 1, 1, 1');
INSERT INTO "Fortune" (id, message) VALUES (5, 'A computer program does what you tell it to do, not what you want it to do.');
INSERT INTO "Fortune" (id, message) VALUES (6, 'Emacs is a nice operating system, but I prefer UNIX. — Tom Christaensen');
INSERT INTO "Fortune" (id, message) VALUES (7, 'Any program that runs right is obsolete.');
INSERT INTO "Fortune" (id, message) VALUES (8, 'A list is only as strong as its weakest link. — Donald Knuth');
INSERT INTO "Fortune" (id, message) VALUES (9, 'Feature: A bug with seniority.');
INSERT INTO "Fortune" (id, message) VALUES (10, 'Computers make very fast, very accurate mistakes.');
INSERT INTO "Fortune" (id, message) VALUES (11, '<script>alert("This should not be displayed in a browser alert box.");</script>');
INSERT INTO "Fortune" (id, message) VALUES (12, 'フレームワークのベンチマーク');

DROP TABLE IF EXISTS "NumericDataType";
CREATE TABLE "NumericDataType" ("Short" INT2, "Integer" INT4, "Long" INT8, "Float" FLOAT4, "Double" FLOAT8, "BigDecimal" NUMERIC, "Boolean" BOOLEAN);
INSERT INTO "NumericDataType" ("Short", "Integer", "Long", "Float", "Double", "BigDecimal", "Boolean")
VALUES (32767, 2147483647, 9223372036854775807, 3.4028235E38, 1.7976931348623157E308, '9.99999999999999999999999999999999999', true);

DROP TABLE IF EXISTS "TemporalDataType";
CREATE TABLE "TemporalDataType" ("Date" date, "Time" time without time zone, "TimeTz" time with time zone, "Timestamp" timestamp without time zone, "TimestampTz" timestamp with time zone, "Interval" interval);
INSERT INTO "TemporalDataType" ("Date", "Time", "TimeTz", "Timestamp", "TimestampTz", "Interval") VALUES ('1981-05-30', '17:55:04.90512', '17:55:04.90512+03:07', '2017-05-14 19:35:58.237666', '2017-05-14 23:59:59.237666-03', '17:22:05');
INSERT INTO "TemporalDataType" ("Date", "Time", "TimeTz", "Timestamp", "TimestampTz", "Interval") VALUES ('2017-05-30', '12:55:04.90512', '02:55:04.90512+03:07', '1909-05-14 19:35:58.237666', '1909-05-14 22:35:58.237666-03', '02:01:33');
INSERT INTO "TemporalDataType" ("Date", "Time", "TimeTz", "Timestamp", "TimestampTz", "Interval") VALUES ('1900-01-01', '23:59:04.90512', '08:08:03.90512+03:07', '1800-01-01 23:57:53.237666', '1800-01-01 23:59:59.237666-03', '04:33:59');


