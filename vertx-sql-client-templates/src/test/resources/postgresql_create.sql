

create table revokedkeys
(
    keyhash         varchar(64)     unique,
    valid_until     timestamp without time zone not null
);

