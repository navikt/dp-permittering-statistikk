package no.nav.dagpenger.permittering.statistikk

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class SøknadBehandlingMottakTest {
    private val testRapid = TestRapid()
    private val behandlingId = UUID.randomUUID()
    private val søknadId = UUID.randomUUID()
    private val ident = "12345612345"

    @Test
    fun `Skal håndtere at søknadsbehandling er opprettet`() {
        val service =
            mockk<Service>().also {
                every { it.håndter(any(), any()) } just Runs
            }
        SøknadBehandlingMottak(testRapid, service)

        testRapid.sendTestMessage(
            søknadsbehandlingHendelse(hendelseNavn = "behandling_opprettet"),
        )

        verify(exactly = 1) {
            service.håndter(any<BehandlingHendelse.BehandlingOpprettet>(), any())
        }
    }

    @Test
    fun `Skal håndtere at søknadsbehandling er avbrutt`() {
        val service =
            mockk<Service>().also {
                every { it.håndter(any(), any()) } just Runs
            }
        SøknadBehandlingMottak(testRapid, service)

        testRapid.sendTestMessage(
            søknadsbehandlingHendelse(hendelseNavn = "behandling_avbrutt"),
        )

        verify(exactly = 1) {
            service.håndter(any<BehandlingHendelse.BehandlingAvbrutt>(), any())
        }
    }

    @Test
    fun `Skal håndtere at søknadsbehandling er ferdig behandlet`() {
        val service =
            mockk<Service>().also {
                every { it.håndter(any(), any()) } just Runs
            }
        SøknadBehandlingMottak(testRapid, service)

        testRapid.sendTestMessage(
            søknadsbehandlingHendelse(hendelseNavn = "behandlingsresultat"),
        )

        verify(exactly = 1) {
            service.håndter(any<BehandlingHendelse.BehandlingFerdig>(), any())
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
              "behandletHendelse": {
                "type": "Søknad",
                "id": "$søknadId",
                "skjedde": "$skjedde"
              },
              "behandlingId": "$behandlingId",
              "ident": "$ident"
            }
            """.trimIndent()
    }
}
