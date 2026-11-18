package no.nav.dagpenger.permittering.statistikk

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import io.micrometer.core.instrument.MeterRegistry

internal class SøknadBehandlingMottak(
    rapidsConnection: RapidsConnection,
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
        val eventName = packet["@event_name"].stringValue()
        val behandlingId = packet["behandlingId"].stringValue()
        val søknadId = packet["behandletHendelse"]["id"].stringValue()
        withLoggingContext("behandlingId" to behandlingId, "søknadId" to søknadId) {
            logger.info { "Mottok $eventName for søknadId: $søknadId, behandlingId: $behandlingId" }
            val ident = packet["ident"].stringValue()
            if (eventName == "behandling_opprettet") {
                val behov =
                    JsonMessage.newNeed(
                        behov = setOf("PermittertFiskeforedling", "Permittert"),
                        map =
                            mapOf(
                                "ident" to ident,
                                "søknadId" to søknadId,
                                "behandlingId" to behandlingId,
                            ),
                    )
                context.publish(key = ident, message = behov.toJson())
            }
        }
    }
}
