package no.nav.dagpenger.permittering.statistikk.db

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.permittering.statistikk.BehandlingHendelse
import no.nav.dagpenger.permittering.statistikk.SøknadBehandling
import no.nav.dagpenger.permittering.statistikk.db.Postgres.withMigratedDb
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class PostgresRepositoryTest {
    @Test
    fun `Skal lagre og hente og oppdatere søknadsbehandling`() {
        val søknadBehandling =
            SøknadBehandling(
                søknadId = UUID.randomUUID(),
                behandlingId = UUID.randomUUID(),
                mottattDato = LocalDate.now().minusDays(3),
            )
        val nyBehandlingId = UUID.randomUUID()
        withMigratedDb { ds ->
            val repository = PostgresRepository(ds)

            repository.lagre(søknadBehandling)

            repository.finnSøknadBehandling(søknadId = søknadBehandling.søknadId).also { dbSøknadBehandling ->
                dbSøknadBehandling shouldBe søknadBehandling
            }

            val oppdatertSøknadBehandling =
                søknadBehandling.copy(
                    avsluttetDato = LocalDate.now(),
                    avsluttetType = BehandlingHendelse.HendelseType.FERDIG_BEHANDLET,
                    behandlingId = nyBehandlingId,
                )
            repository.lagre(oppdatertSøknadBehandling)

            repository.finnSøknadBehandling(søknadId = søknadBehandling.søknadId).also { dbSøknadBehandling ->
                dbSøknadBehandling shouldBe oppdatertSøknadBehandling
            }
        }
    }
}
