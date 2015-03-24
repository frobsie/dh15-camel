CREATE TABLE feedentry
(
  id serial NOT NULL,
  title character varying(255),
  link character varying(255),
  description text,
  pubdate timestamp with time zone,
  CONSTRAINT feedentry_pkey PRIMARY KEY (id)
)
