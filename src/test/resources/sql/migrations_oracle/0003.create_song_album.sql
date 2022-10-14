create table album
(
    id           NUMBER GENERATED BY DEFAULT ON NULL AS IDENTITY,
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    name         VARCHAR(128),
    arts         JSON
)