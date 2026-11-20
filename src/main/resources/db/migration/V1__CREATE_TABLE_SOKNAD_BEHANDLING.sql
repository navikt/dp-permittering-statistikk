CREATE OR REPLACE FUNCTION oppdater_endret_tidspunkt()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.endret_tidspunkt = current_timestamp;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE IF NOT EXISTS soknad_behandling_v1 (
    soknad_id               UUID PRIMARY KEY,
    behandling_id           UUID NOT NULL,
    mottatt_dato            DATE NOT NULL,
    avsluttet_dato          DATE,
    avsluttet_type          VARCHAR(100),
    registrert_tidspunkt    TIMESTAMP WITHOUT TIME ZONE DEFAULT timezone('Europe/Oslo'::text, current_timestamp),
    endret_tidspunkt        TIMESTAMP WITHOUT TIME ZONE DEFAULT timezone('Europe/Oslo'::text, current_timestamp)
);

CREATE TRIGGER oppdater_endret_tidspunkt
    BEFORE UPDATE ON soknad_behandling_v1
    FOR EACH ROW EXECUTE FUNCTION oppdater_endret_tidspunkt();

DO
$$
    BEGIN
        IF EXISTS
            (SELECT 1 FROM pg_roles WHERE rolname = 'cloudsqliamuser')
        THEN
            GRANT SELECT ON TABLE soknad_behandling_v1 TO cloudsqliamuser;
        END IF;
    END
$$;
