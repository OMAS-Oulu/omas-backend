--
-- PostgreSQL database dump
--

-- Dumped from database version 16.2
-- Dumped by pg_dump version 16.2

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

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: club; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.club (
    creation_date date,
    id_creator bigint NOT NULL,
    name character varying(255) NOT NULL,
    name_non_id character varying(255)
);


ALTER TABLE public.club OWNER TO postgres;

--
-- Name: competition; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.competition (
    creation_date date NOT NULL,
    end_date date,
    start_date date,
    competition_id character varying(255) NOT NULL,
    display_name character varying(255),
    type character varying(255)
);


ALTER TABLE public.competition OWNER TO postgres;

--
-- Name: password_reset_token; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.password_reset_token (
    expiry_date timestamp(6) without time zone,
    id bigint NOT NULL,
    reset_password_token character varying(255)
);


ALTER TABLE public.password_reset_token OWNER TO postgres;

--
-- Name: password_reset_token_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.password_reset_token_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.password_reset_token_seq OWNER TO postgres;

--
-- Name: role; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.role (
    user_id bigint NOT NULL,
    role character varying(255) NOT NULL
);


ALTER TABLE public.role OWNER TO postgres;

--
-- Name: team; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.team (
    competition_id character varying(255) NOT NULL,
    team_display_name character varying(255) NOT NULL,
    team_name character varying(255) NOT NULL
);


ALTER TABLE public.team OWNER TO postgres;

--
-- Name: team_member; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.team_member (
    user_id bigint NOT NULL,
    competition_id character varying(255) NOT NULL,
    team_name character varying(255) NOT NULL
);


ALTER TABLE public.team_member OWNER TO postgres;

--
-- Name: team_member_score; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.team_member_score (
    bulls_eye_count integer NOT NULL,
    creation_date date,
    sum double precision NOT NULL,
    user_id bigint NOT NULL,
    uuid uuid,
    score_per_shot character varying(1200),
    competition_id character varying(255) NOT NULL,
    team_name character varying(255) NOT NULL
);


ALTER TABLE public.team_member_score OWNER TO postgres;

--
-- Name: user_; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_ (
    creation_date date,
    id bigint NOT NULL,
    email character varying(255),
    legalname character varying(255),
    part_of_club character varying(255),
    password character varying(255),
    username character varying(255)
);


ALTER TABLE public.user_ OWNER TO postgres;

--
-- Name: user__id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.user__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.user__id_seq OWNER TO postgres;

--
-- Name: user__id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.user__id_seq OWNED BY public.user_.id;


--
-- Name: user_ id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_ ALTER COLUMN id SET DEFAULT nextval('public.user__id_seq'::regclass);


--
-- Name: club club_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.club
    ADD CONSTRAINT club_pkey PRIMARY KEY (name);


--
-- Name: competition competition_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.competition
    ADD CONSTRAINT competition_pkey PRIMARY KEY (competition_id);


--
-- Name: password_reset_token password_reset_token_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.password_reset_token
    ADD CONSTRAINT password_reset_token_pkey PRIMARY KEY (id);


--
-- Name: password_reset_token password_reset_token_reset_password_token_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.password_reset_token
    ADD CONSTRAINT password_reset_token_reset_password_token_key UNIQUE (reset_password_token);


--
-- Name: role role_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role
    ADD CONSTRAINT role_pkey PRIMARY KEY (user_id, role);


--
-- Name: team_member team_member_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.team_member
    ADD CONSTRAINT team_member_pkey PRIMARY KEY (user_id, competition_id, team_name);


--
-- Name: team_member_score team_member_score_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.team_member_score
    ADD CONSTRAINT team_member_score_pkey PRIMARY KEY (user_id, competition_id, team_name);


--
-- Name: team team_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.team
    ADD CONSTRAINT team_pkey PRIMARY KEY (competition_id, team_name);


--
-- Name: user_ user__email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_
    ADD CONSTRAINT user__email_key UNIQUE (email);


--
-- Name: user_ user__pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_
    ADD CONSTRAINT user__pkey PRIMARY KEY (id);


--
-- Name: user_ user__username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_
    ADD CONSTRAINT user__username_key UNIQUE (username);


--
-- Name: team_member fk3fi820e3nsm0yyd1pm6nf44ti; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.team_member
    ADD CONSTRAINT fk3fi820e3nsm0yyd1pm6nf44ti FOREIGN KEY (user_id) REFERENCES public.user_(id);


--
-- Name: team_member_score fk40uj7cbxbqebqrbmdatolafvw; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.team_member_score
    ADD CONSTRAINT fk40uj7cbxbqebqrbmdatolafvw FOREIGN KEY (user_id, competition_id, team_name) REFERENCES public.team_member(user_id, competition_id, team_name);


--
-- Name: team_member fk9ae9nuasjib5wngdti8rj2sc8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.team_member
    ADD CONSTRAINT fk9ae9nuasjib5wngdti8rj2sc8 FOREIGN KEY (competition_id, team_name) REFERENCES public.team(competition_id, team_name);


--
-- Name: team fkqhhapgh63c9yjo4tc0uf6ynt1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.team
    ADD CONSTRAINT fkqhhapgh63c9yjo4tc0uf6ynt1 FOREIGN KEY (competition_id) REFERENCES public.competition(competition_id);


--
-- Name: role fkrpeayxgcckrtucx5g5gxs7ydg; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role
    ADD CONSTRAINT fkrpeayxgcckrtucx5g5gxs7ydg FOREIGN KEY (user_id) REFERENCES public.user_(id);


--
-- PostgreSQL database dump complete
--