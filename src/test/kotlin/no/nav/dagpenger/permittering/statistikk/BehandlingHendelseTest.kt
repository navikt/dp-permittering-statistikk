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
import java.time.LocalDateTime
import java.util.UUID

class BehandlingHendelseTest {
    private val testIdent = "12345678910"
    private val søknadId = UUID.randomUUID()
    private val idag = LocalDateTime.now()
    private val behandlingId = UUID.randomUUID()

    @Test
    fun `Konstruering av BehandlingOpprettet og BehandlingFerdigfra JsonMessage`() {
        newMessage(
            eventNavn = "behandling_opprettet",
            skjedde = idag.toLocalDate(),
        ).toBehandlingHendelse() shouldBe
            BehandlingHendelse.BehandlingOpprettet(
                behandlingId = behandlingId,
                søknadId = søknadId,
                dato = idag.toLocalDate(),
                ident = testIdent,
            )

        newMessage(
            eventNavn = "behandlingsresultat",
            sistendret = idag,
        ).toBehandlingHendelse() shouldBe
            BehandlingHendelse.BehandlingFerdig(
                behandlingId = behandlingId,
                søknadId = søknadId,
                dato = idag.toLocalDate(),
                ident = testIdent,
            )
    }

    @Test
    fun `Konstruering av BehandlingAvbrutt fra JsonMessage`() {
        newMessage(
            eventNavn = "behandling_avbrutt",
            opprettet = idag,
            årsak = "test",
        ).toBehandlingHendelse().let {
            it.shouldBeInstanceOf<BehandlingHendelse.BehandlingAvbrutt>()
            it.behandlingId shouldBe behandlingId
            it.søknadId shouldBe søknadId
            it.dato shouldBe idag.toLocalDate()
            it.ident shouldBe testIdent
            it.hendelseType shouldBe BehandlingHendelse.HendelseType.AVBRUTT
        }

        newMessage(
            eventNavn = "behandling_avbrutt",
            opprettet = idag,
            årsak = "Oppgaven er endret i Arena",
        ).toBehandlingHendelse().let {
            it.shouldBeInstanceOf<BehandlingHendelse.BehandlingAvbrutt>()
            it.behandlingId shouldBe behandlingId
            it.søknadId shouldBe søknadId
            it.dato shouldBe idag.toLocalDate()
            it.ident shouldBe testIdent
            it.hendelseType shouldBe BehandlingHendelse.HendelseType.AVBRUTT_BEHANDLES_ARENA
        }
    }

    private fun newMessage(
        eventNavn: String,
        søknadId: UUID = this.søknadId,
        behandlingId: UUID = this.behandlingId,
        skjedde: LocalDate? = null,
        opprettet: LocalDateTime? = null,
        sistendret: LocalDateTime? = null,
        ident: String = this.testIdent,
        årsak: String? = null,
    ): JsonMessage =
        JsonMessage
            .newMessage(
                buildMap {
                    put("@event_name", eventNavn)
                    put(
                        "behandletHendelse",
                        buildMap {
                            put("id", søknadId)
                            skjedde?.let { put("skjedde", it) }
                        },
                    )
                    put("behandlingId", behandlingId)
                    put("ident", ident)
                    opprettet?.let { put("@opprettet", it) }
                    sistendret?.let { put("sistEndret", it) }
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
