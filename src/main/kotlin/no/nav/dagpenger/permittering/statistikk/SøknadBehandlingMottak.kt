package no.nav.dagpenger.permittering.statistikk

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.permittering.statistikk.BehandlingHendelse.Companion.toBehandlingHendelse

private val logger = KotlinLogging.logger {}

internal class SøknadBehandlingMottak(
    rapidsConnection: RapidsConnection,
    private val mediator: Mediator,
) : River.PacketListener {
    companion object {

        fun JsonMessage.preconditions() {
            this.requireAny("@event_name", eventNavn)
            this.requireValue("behandletHendelse.type", "Søknad")
        }

        fun JsonMessage.requiredKeys() {
            this.requireKey("ident", "behandlingId", "behandletHendelse")
        }

        fun JsonMessage.interestedKeys() {
            this.interestedIn("årsak")
        }

        val eventNavn =
            listOf(
                "behandling_opprettet",
                "behandling_avbrutt",
                "behandlingsresultat",
            )

        val rapidFilter: River.() -> Unit = {
            precondition { it.preconditions() }
            validate {
                it.requiredKeys()
                it.interestedKeys()
            }
        }
    }

    init {
        River(rapidsConnection).apply(rapidFilter).register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
        metadata: MessageMetadata,
        meterRegistry: MeterRegistry,
    ) {
        runBlocking {
            val behandlingHendelse = packet.toBehandlingHendelse()
            logger.info { "Mottok behandling hendelse: $behandlingHendelse" }
            mediator.håndter(behandlingHendelse, context)
        }
    }
}
