package no.nav.dagpenger.permittering.statistikk

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import no.nav.dagpenger.permittering.statistikk.BehandlingHendelse.Companion.toBehandlingHendelse
import no.nav.dagpenger.permittering.statistikk.SøknadBehandlingMottak.Companion.interestedKeys
import no.nav.dagpenger.permittering.statistikk.SøknadBehandlingMottak.Companion.preconditions
import no.nav.dagpenger.permittering.statistikk.SøknadBehandlingMottak.Companion.requiredKeys
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class BehandlingHendelseTest {
    private val testIdent = "12345678910"
    private val søknadId = UUID.randomUUID()
    private val søknadsDato = LocalDate.now()
    private val behandlingId = UUID.randomUUID()

    @Test
    fun `Konstruering av BehandlingOpprettet og BehandlingFerdigfra JsonMessage`() {
        newMessage(
            eventNavn = "behandling_opprettet",
        ).toBehandlingHendelse() shouldBe
            BehandlingHendelse.BehandlingOpprettet(
                behandlingId = behandlingId,
                søknadId = søknadId,
                skjedde = søknadsDato,
                ident = testIdent,
            )

        newMessage(
            eventNavn = "behandlingsresultat",
        ).toBehandlingHendelse() shouldBe
            BehandlingHendelse.BehandlingFerdig(
                behandlingId = behandlingId,
                søknadId = søknadId,
                skjedde = søknadsDato,
                ident = testIdent,
            )
    }

    @Test
    fun `Konstruering av BehandlingAvbrutt fra JsonMessage`() {
        newMessage(
            eventNavn = "behandling_avbrutt",
            årsak = "test",
        ).toBehandlingHendelse().let {
            it.shouldBeInstanceOf<BehandlingHendelse.BehandlingAvbrutt>()
            it.behandlingId shouldBe behandlingId
            it.søknadId shouldBe søknadId
            it.skjedde shouldBe søknadsDato
            it.ident shouldBe testIdent
            it.hendelseType shouldBe BehandlingHendelse.HendelseType.AVBRUTT
        }

        newMessage(
            eventNavn = "behandling_avbrutt",
            årsak = "Oppgaven er endret i Arena",
        ).toBehandlingHendelse().let {
            it.shouldBeInstanceOf<BehandlingHendelse.BehandlingAvbrutt>()
            it.behandlingId shouldBe behandlingId
            it.søknadId shouldBe søknadId
            it.skjedde shouldBe søknadsDato
            it.ident shouldBe testIdent
            it.hendelseType shouldBe BehandlingHendelse.HendelseType.AVBRUTT_BEHANDLES_ARENA
        }
    }

    private fun newMessage(
        eventNavn: String,
        søknadId: UUID = this.søknadId,
        behandlingId: UUID = this.behandlingId,
        skjedde: LocalDate = this.søknadsDato,
        ident: String = this.testIdent,
        årsak: String? = null,
    ): JsonMessage =
        JsonMessage
            .newMessage(
                buildMap {
                    put("@event_name", eventNavn)
                    put(
                        "behandletHendelse",
                        mapOf(
                            "id" to søknadId,
                            "skjedde" to skjedde,
                        ),
                    )
                    put("behandlingId", behandlingId)
                    put("ident", ident)
                    årsak?.let {
                        put("årsak", it)
                    }
                },
            ).also {
                it.preconditions()
                it.requiredKeys()
                it.interestedKeys()
            }
}
