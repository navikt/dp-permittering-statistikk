package no.nav.dagpenger.permittering.statistikk.db

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.dagpenger.permittering.statistikk.BehandlingHendelse
import no.nav.dagpenger.permittering.statistikk.SøknadBehandling
import java.util.UUID
import javax.sql.DataSource

class PostgresRepository(
    private val dataSource: DataSource,
) : Repository {
    override fun lagre(søknadBehandling: SøknadBehandling) {
        sessionOf(dataSource).use { session ->
            session.run(
                queryOf(
                    //language=PostgreSQL
                    statement =
                        """
                        INSERT INTO soknad_behandling_v1
                            (
                                soknad_id,
                                behandling_id,
                                mottatt_dato,
                                avsluttet_dato,
                                avsluttet_type
                            )
                        VALUES
                            (
                                :soknad_id,
                                :behandling_id,
                                :mottatt_dato,
                                :avsluttet_dato,
                                :avsluttet_type
                            )
                        ON CONFLICT(soknad_id) DO UPDATE SET
                         behandling_id = :behandling_id,
                         avsluttet_dato = :avsluttet_dato,
                         avsluttet_type = :avsluttet_type
                        """.trimIndent(),
                    paramMap =
                        mapOf(
                            "soknad_id" to søknadBehandling.søknadId,
                            "behandling_id" to søknadBehandling.behandlingId(),
                            "mottatt_dato" to søknadBehandling.mottattDato,
                            "avsluttet_dato" to søknadBehandling.avsluttetDato(),
                            "avsluttet_type" to søknadBehandling.avsluttetType()?.name,
                        ),
                ).asUpdate,
            )
        }
    }

    override fun finnSøknadBehandling(søknadId: UUID): SøknadBehandling? =
        sessionOf(dataSource).use { session ->
            session.run(
                queryOf(
                    //language=PostgreSQL
                    statement =
                        """
                        SELECT *
                        FROM   soknad_behandling_v1
                        WHERE  soknad_id = :soknad_id
                        """.trimIndent(),
                    paramMap =
                        mapOf(
                            "soknad_id" to søknadId,
                        ),
                ).map { row ->
                    val avsluttetType = row.stringOrNull("avsluttet_type")
                    SøknadBehandling(
                        søknadId = row.uuid("soknad_id"),
                        mottattDato = row.localDate("mottatt_dato"),
                        behandlingId = row.uuid("behandling_id"),
                        avsluttetDato = row.localDateOrNull("avsluttet_dato"),
                        avsluttetType = avsluttetType?.let { BehandlingHendelse.HendelseType.valueOf(it) },
                    )
                }.asSingle,
            )
        }
}
