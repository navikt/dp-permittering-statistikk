package no.nav.dagpenger.permittering.statistikk

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class SøknadBehandlingMottakTest {
    private val testRapid = TestRapid()
    private val behandlingId = UUID.randomUUID()
    private val søknadId = UUID.randomUUID()
    private val ident = "12345612345"

    @Test
    fun `Skal håndtere søknadsbehandling er opprettet`() {
        val mediator =
            mockk<Mediator>().also {
                coEvery { it.håndter(any(), any()) } just Runs
            }
        SøknadBehandlingMottak(testRapid, mediator)

        testRapid.sendTestMessage(
            søknadsbehandlingHendelse(hendelseNavn = "behandling_opprettet"),
        )

        coVerify(exactly = 1) {
            mediator.håndter(any<BehandlingHendelse.BehandlingOpprettet>(), any())
        }
    }

    @Test
    fun `Skal håndtere søknadsbehandling er avbrutt`() {
        val mediator =
            mockk<Mediator>().also {
                coEvery { it.håndter(any(), any()) } just Runs
            }
        SøknadBehandlingMottak(testRapid, mediator)

        testRapid.sendTestMessage(
            søknadsbehandlingHendelse(hendelseNavn = "behandling_avbrutt"),
        )

        coVerify(exactly = 1) {
            mediator.håndter(any<BehandlingHendelse.BehandlingAvbrutt>(), any())
        }
    }

    @Test
    fun `Skal håndtere søknadsbehandling er ferdig behandlet`() {
        val mediator =
            mockk<Mediator>().also {
                coEvery { it.håndter(any(), any()) } just Runs
            }
        SøknadBehandlingMottak(testRapid, mediator)

        testRapid.sendTestMessage(
            søknadsbehandlingHendelse(hendelseNavn = "behandlingsresultat"),
        )

        coVerify(exactly = 1) {
            mediator.håndter(any<BehandlingHendelse.BehandlingFerdig>(), any())
        }
    }

    private fun søknadsbehandlingHendelse(
        hendelseNavn: String,
        søknadId: UUID = this.søknadId,
        behandlingId: UUID = this.behandlingId,
        ident: String = this.ident,
        skjedde: LocalDate = LocalDate.now(),
    ): String {
        //language=JSON
        return """
            {
              "@event_name": "$hendelseNavn",
              "@opprettet": "${LocalDateTime.now()}",
              "behandletHendelse": {
                "type": "Søknad",
                "id": "$søknadId",
                "skjedde": "$skjedde"
              },
              "sistEndret": "${LocalDateTime.now()}",
              "behandlingId": "$behandlingId",
              "ident": "$ident"
            }
            """.trimIndent()
    }
}
