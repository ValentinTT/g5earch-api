SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: g5earch; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA g5earch;

ALTER SCHEMA g5earch OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: documents; Type: TABLE; Schema: g5earch; Owner: postgres
--

CREATE TABLE g5earch.documents (
    "ID" integer NOT NULL,
    title character varying NOT NULL,
    "URI" character varying NOT NULL
);


ALTER TABLE g5earch.documents OWNER TO postgres;

--
-- Name: documentos_ID_seq; Type: SEQUENCE; Schema: g5earch; Owner: postgres
--

ALTER TABLE g5earch.documents ALTER COLUMN "ID" ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME g5earch."documentos_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
);


--
-- Name: terms; Type: TABLE; Schema: g5earch; Owner: postgres
--

CREATE TABLE g5earch.terms (
    name character varying NOT NULL,
    "DocumentID" integer NOT NULL,
    frequency integer NOT NULL
);


ALTER TABLE g5earch.terms OWNER TO postgres;

--
-- Name: documents documentos_pkey; Type: CONSTRAINT; Schema: g5earch; Owner: postgres
--

ALTER TABLE ONLY g5earch.documents
    ADD CONSTRAINT documentos_pkey PRIMARY KEY ("ID");


--
-- Name: terms terminos_pkey; Type: CONSTRAINT; Schema: g5earch; Owner: postgres
--

ALTER TABLE ONLY g5earch.terms
    ADD CONSTRAINT terminos_pkey PRIMARY KEY ("DocumentID", name);


--
-- Name: fki_IDDocumento_FK_Terminos; Type: INDEX; Schema: g5earch; Owner: postgres
--

CREATE INDEX "fki_IDDocumento_FK_Terminos" ON g5earch.terms USING btree ("DocumentID");


--
-- Name: terms IDDocumento_FK_Terminos; Type: FK CONSTRAINT; Schema: g5earch; Owner: postgres
--

ALTER TABLE ONLY g5earch.terms
    ADD CONSTRAINT "IDDocumento_FK_Terminos" FOREIGN KEY ("DocumentID") REFERENCES g5earch.documents("ID") NOT VALID;