package no.nav.dagpenger.permittering.statistikk

import com.github.navikt.tbd_libs.rapids_and_rivers_api.FailedMessage
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.OutgoingMessage
import com.github.navikt.tbd_libs.rapids_and_rivers_api.SentMessage
import io.kotest.assertions.json.shouldEqualSpecifiedJsonIgnoringOrder
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.called
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.permittering.statistikk.db.Repository
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class MediatorTest {
    private val testIdent = "12345678910"
    private val søknadId = UUID.randomUUID()
    private val søknadDato = LocalDate.now().minusDays(1)
    private val avsluttetDato = LocalDate.now()
    private val behandlingId = UUID.randomUUID()

    @Test
    fun `Skal lagre søknadsbehandling og sende hendelse for en permitteringssøknad`() {
        val søknadOppslag =
            mockk<SøknadOppslag>().also {
                coEvery {
                    it.erPermittertUtenforFiskeindustri(
                        søknadId = søknadId,
                        ident = testIdent,
                    )
                } returns true
            }

        val slot = slot<SøknadBehandling>()
        val repository =
            mockk<Repository>().also {
                every { it.lagre(capture(slot)) } just Runs
            }

        val mediator =
            Mediator(
                søknadOppslag = søknadOppslag,
                repository = repository,
            )

        val messageContext = MockContext()
        runBlocking {
            mediator.håndter(
                behandlingHendelse =
                    BehandlingHendelse.BehandlingOpprettet(
                        søknadId = søknadId,
                        behandlingId = behandlingId,
                        dato = søknadDato,
                        ident = testIdent,
                    ),
                context = messageContext,
            )

            slot.captured shouldBe
                SøknadBehandling(
                    søknadId = søknadId,
                    behandlingId = behandlingId,
                    mottattDato = søknadDato,
                )

            messageContext.key shouldBe søknadId.toString()
            messageContext.message shouldEqualSpecifiedJsonIgnoringOrder
                """
                 {
                  "hendelse": "SØKT",
                  "ident": "$testIdent",
                  "behandlingId": "$behandlingId",
                  "søknadId": "$søknadId",
                  "dato": "$søknadDato"
                } 
                """.trimIndent()
        }
    }

    @Test
    fun `Skal sende hendelse for en permitteringssøknad som er avbrutt`() {
        val slot = slot<SøknadBehandling>()
        val repository =
            mockk<Repository>().also {
                every { it.finnSøknadBehandling(søknadId = søknadId) } returns
                    SøknadBehandling(
                        søknadId = søknadId,
                        mottattDato = søknadDato,
                        behandlingId = behandlingId,
                    )
                every { it.lagre(capture(slot)) } just Runs
            }

        val mediator =
            Mediator(
                søknadOppslag = mockk(),
                repository = repository,
            )

        val messageContext = MockContext()
        runBlocking {
            mediator.håndter(
                behandlingHendelse =
                    BehandlingHendelse.BehandlingAvbrutt(
                        søknadId = søknadId,
                        behandlingId = behandlingId,
                        dato = avsluttetDato,
                        ident = testIdent,
                        årsak = "test",
                    ),
                context = messageContext,
            )

            slot.captured shouldBe
                SøknadBehandling(
                    søknadId = søknadId,
                    behandlingId = behandlingId,
                    mottattDato = søknadDato,
                    avsluttetDato = avsluttetDato,
                    avsluttetType = BehandlingHendelse.HendelseType.AVBRUTT,
                )

            messageContext.key shouldBe søknadId.toString()
            messageContext.message shouldEqualSpecifiedJsonIgnoringOrder
                """
                 {
                  "hendelse": "AVBRUTT",
                  "ident": "$testIdent",
                  "behandlingId": "$behandlingId",
                  "søknadId": "$søknadId",
                  "dato": "$avsluttetDato"
                } 
                """.trimIndent()

            mediator.håndter(
                behandlingHendelse =
                    BehandlingHendelse.BehandlingAvbrutt(
                        søknadId = søknadId,
                        behandlingId = behandlingId,
                        dato = avsluttetDato,
                        ident = testIdent,
                        årsak = "Oppgaven er endret i Arena",
                    ),
                context = messageContext,
            )
            messageContext.key shouldBe søknadId.toString()
            messageContext.message shouldEqualSpecifiedJsonIgnoringOrder
                """
                 {
                  "hendelse": "AVBRUTT_BEHANDLES_ARENA",
                  "ident": "$testIdent",
                  "behandlingId": "$behandlingId",
                  "søknadId": "$søknadId",
                  "dato": "$avsluttetDato"
                } 
                """.trimIndent()
        }
    }

    @Test
    fun `Skal sende hendelse for en permitteringssøknad som er ferdigbehandlet`() {
        val slot = slot<SøknadBehandling>()
        val repository =
            mockk<Repository>().also {
                every { it.finnSøknadBehandling(søknadId = søknadId) } returns
                    SøknadBehandling(
                        søknadId = søknadId,
                        mottattDato = søknadDato,
                        behandlingId = behandlingId,
                    )
                every { it.lagre(capture(slot)) } just Runs
            }

        val mediator =
            Mediator(
                søknadOppslag = mockk(),
                repository = repository,
            )

        val messageContext = MockContext()
        runBlocking {
            mediator.håndter(
                behandlingHendelse =
                    BehandlingHendelse.BehandlingFerdig(
                        søknadId = søknadId,
                        behandlingId = behandlingId,
                        dato = avsluttetDato,
                        ident = testIdent,
                    ),
                context = messageContext,
            )

            slot.captured shouldBe
                SøknadBehandling(
                    søknadId = søknadId,
                    behandlingId = behandlingId,
                    mottattDato = søknadDato,
                    avsluttetDato = avsluttetDato,
                    avsluttetType = BehandlingHendelse.HendelseType.FERDIG_BEHANDLET,
                )
            messageContext.key shouldBe søknadId.toString()
            messageContext.message shouldEqualSpecifiedJsonIgnoringOrder
                """
                 {
                  "hendelse": "FERDIG_BEHANDLET",
                  "ident": "$testIdent",
                  "behandlingId": "$behandlingId",
                  "søknadId": "$søknadId",
                  "dato": "$avsluttetDato"
                } 
                """.trimIndent()
        }
    }

    @Test
    fun `Skal ignorere behandling hendelser som ikke er en permitterringssøknad`() {
        val søknadOppslag =
            mockk<SøknadOppslag>().also {
                coEvery {
                    it.erPermittertUtenforFiskeindustri(
                        søknadId = søknadId,
                        ident = testIdent,
                    )
                } returns false

                coEvery {
                    it.erPermittertFraFiskeindustri(
                        søknadId = søknadId,
                        ident = testIdent,
                    )
                } returns false
            }

        val repository =
            mockk<Repository>().also {
                every { it.finnSøknadBehandling(søknadId = søknadId) } returns null
            }

        val mediator =
            Mediator(
                søknadOppslag = søknadOppslag,
                repository = repository,
            )

        val messageContext = mockk<MessageContext>()
        runBlocking {
            mediator.håndter(
                behandlingHendelse =
                    BehandlingHendelse.BehandlingOpprettet(
                        søknadId = søknadId,
                        behandlingId = behandlingId,
                        dato = søknadDato,
                        ident = testIdent,
                    ),
                context = messageContext,
            )
            verify {
                messageContext wasNot called
            }

            mediator.håndter(
                behandlingHendelse =
                    BehandlingHendelse.BehandlingAvbrutt(
                        søknadId = søknadId,
                        behandlingId = behandlingId,
                        dato = avsluttetDato,
                        ident = testIdent,
                        årsak = "Oppgaven er endret i Arena",
                    ),
                context = messageContext,
            )
            verify {
                messageContext wasNot called
            }

            mediator.håndter(
                behandlingHendelse =
                    BehandlingHendelse.BehandlingFerdig(
                        søknadId = søknadId,
                        behandlingId = behandlingId,
                        dato = avsluttetDato,
                        ident = testIdent,
                    ),
                context = messageContext,
            )
            verify {
                messageContext wasNot called
            }
        }
    }

    private class MockContext : MessageContext {
        lateinit var key: String
        lateinit var message: String

        override fun publish(message: String) {
            TODO("Not yet implemented")
        }

        override fun publish(
            key: String,
            message: String,
        ) {
            this.key = key
            this.message = message
        }

        override fun publish(messages: List<OutgoingMessage>): Pair<List<SentMessage>, List<FailedMessage>> {
            TODO("Not yet implemented")
        }

        override fun rapidName(): String {
            TODO("Not yet implemented")
        }
    }
}
