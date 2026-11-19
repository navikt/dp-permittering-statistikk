package no.nav.dagpenger.permittering.statistikk

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import java.time.LocalDate
import java.util.UUID

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

    val søknadId: UUID = UUID.fromString(packet["behandletHendelse"]["id"].stringValue())
    val behandlingId: UUID = UUID.fromString(packet["behandlingId"].stringValue())
    val skjedde: LocalDate = LocalDate.parse(packet["behandletHendelse"]["skjedde"].stringValue())
    val ident: String = packet["ident"].stringValue()

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
