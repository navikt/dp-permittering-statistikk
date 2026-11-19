package no.nav.dagpenger.permittering.statistikk

import com.github.navikt.tbd_libs.rapids_and_rivers_api.FailedMessage
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.OutgoingMessage
import com.github.navikt.tbd_libs.rapids_and_rivers_api.SentMessage
import io.kotest.assertions.json.shouldEqualSpecifiedJsonIgnoringOrder
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class MediatorTest {
    private val testIdent = "12345678910"
    private val søknadId = UUID.randomUUID()
    private val søknadsDato = LocalDate.now()
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
                        skjedde = søknadsDato,
                        ident = testIdent,
                    ),
                context = messageContext,
            )

            slot.captured shouldBe
                SøknadBehandling(
                    søknadId = søknadId,
                    behandlingId = behandlingId,
                )

            messageContext.key shouldBe søknadId.toString()
            messageContext.message shouldEqualSpecifiedJsonIgnoringOrder
                """
                 {
                  "hendelse": "SØKT",
                  "ident": "$testIdent",
                  "behandlingId": "$behandlingId",
                  "søknadId": "$søknadId",
                  "dato": "$søknadsDato"
                } 
                """.trimIndent()
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
