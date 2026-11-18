package no.nav.dagpenger.permittering.statistikk

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.instrument.MeterRegistry
import no.nav.dagpenger.permittering.statistikk.BehandlingHendelse.Companion.toBehandlingHendelse

internal class SøknadBehandlingMottak(
    rapidsConnection: RapidsConnection,
    private val service: Service,
) : River.PacketListener {
    companion object {
        private val logger = KotlinLogging.logger {}

        val eventNavn =
            listOf(
                "behandling_opprettet",
                "behandling_avbrutt",
                "behandlingsresultat",
            )

        val rapidFilter: River.() -> Unit = {
            precondition {
                it.requireAny("@event_name", eventNavn)
                it.requireValue("behandletHendelse.type", "Søknad")
            }
            validate { it.requireKey("ident", "behandlingId", "behandletHendelse") }
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
        service.håndter(packet.toBehandlingHendelse(), context)
    }
}

sealed class BehandlingHendelse(
    protected val packet: JsonMessage,
) {
    companion object {
        fun JsonMessage.toBehandlingHendelse(): BehandlingHendelse {
            val eventName = this["@event_name"].stringValue()
            return when (eventName) {
                "behandling_opprettet" -> {
                    BehandlingOpprettet(this)
                }

                "behandling_avbrutt" -> {
                    BehandlingAvbrutt(this)
                }

                "behandlingsresultat" -> {
                    BehandlingFerdig(this)
                }
                else -> throw IllegalArgumentException("Kunne ikke opprette melding for $eventName")
            }
        }
    }

    class BehandlingOpprettet(
        packet: JsonMessage,
    ) : BehandlingHendelse(packet)

    class BehandlingFerdig(
        packet: JsonMessage,
    ) : BehandlingHendelse(packet)

    class BehandlingAvbrutt(
        packet: JsonMessage,
    ) : BehandlingHendelse(packet)
}

interface Service {
    fun håndter(
        toBehandlingHendelse: BehandlingHendelse,
        context: MessageContext,
    )
}
