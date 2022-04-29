--
-- PostgreSQL database dump
--

-- Dumped from database version 14.1
-- Dumped by pg_dump version 14.1

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
-- Name: documentos; Type: TABLE; Schema: g5earch; Owner: postgres
--

CREATE TABLE g5earch.documentos (
    "ID" integer NOT NULL,
    titulo character varying NOT NULL,
    "URI" character varying NOT NULL
);


ALTER TABLE g5earch.documentos OWNER TO postgres;

--
-- Name: documentos_ID_seq; Type: SEQUENCE; Schema: g5earch; Owner: postgres
--

ALTER TABLE g5earch.documentos ALTER COLUMN "ID" ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME g5earch."documentos_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: terminos; Type: TABLE; Schema: g5earch; Owner: postgres
--

CREATE TABLE g5earch.terminos (
    nombre character varying NOT NULL,
    "IDDocumento" integer NOT NULL,
    frecuencia integer NOT NULL
);


ALTER TABLE g5earch.terminos OWNER TO postgres;

--
-- Name: documentos documentos_pkey; Type: CONSTRAINT; Schema: g5earch; Owner: postgres
--

ALTER TABLE ONLY g5earch.documentos
    ADD CONSTRAINT documentos_pkey PRIMARY KEY ("ID");


--
-- Name: terminos terminos_pkey; Type: CONSTRAINT; Schema: g5earch; Owner: postgres
--

ALTER TABLE ONLY g5earch.terminos
    ADD CONSTRAINT terminos_pkey PRIMARY KEY ("IDDocumento", nombre);


--
-- Name: fki_IDDocumento_FK_Terminos; Type: INDEX; Schema: g5earch; Owner: postgres
--

CREATE INDEX "fki_IDDocumento_FK_Terminos" ON g5earch.terminos USING btree ("IDDocumento");


--
-- Name: terminos IDDocumento_FK_Terminos; Type: FK CONSTRAINT; Schema: g5earch; Owner: postgres
--

ALTER TABLE ONLY g5earch.terminos
    ADD CONSTRAINT "IDDocumento_FK_Terminos" FOREIGN KEY ("IDDocumento") REFERENCES g5earch.documentos("ID") NOT VALID;