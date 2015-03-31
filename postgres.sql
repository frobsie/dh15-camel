CREATE TABLE public.feedentryqueue
(
  id serial NOT NULL,
  title character varying(255),
  link character varying(255),
  description text,
  pubdate timestamp with time zone,
  processed boolean DEFAULT false,
  externalid character varying(255) DEFAULT NULL,
  CONSTRAINT feedentryqueue_pkey PRIMARY KEY (id)
);