package no.nav.dagpenger.permittering.statistikk

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import no.nav.dagpenger.permittering.statistikk.BehandlingHendelse.HendelseType.FERDIG_BEHANDLET
import no.nav.dagpenger.permittering.statistikk.BehandlingHendelse.HendelseType.SØKT
import java.time.LocalDate
import java.util.UUID

sealed class BehandlingHendelse(
    val søknadId: UUID,
    val behandlingId: UUID,
    val skjedde: LocalDate,
    val ident: String,
) {
    enum class HendelseType {
        SØKT,
        AVBRUTT,
        AVBRUTT_BEHANDLES_ARENA,
        FERDIG_BEHANDLET,
    }

    companion object {
        fun JsonMessage.toBehandlingHendelse(): BehandlingHendelse {
            val eventName = this["@event_name"].stringValue()
            val søknadId: UUID = UUID.fromString(this["behandletHendelse"]["id"].stringValue())
            val skjedde: LocalDate = LocalDate.parse(this["behandletHendelse"]["skjedde"].stringValue())
            val behandlingId: UUID = UUID.fromString(this["behandlingId"].stringValue())
            val ident: String = this["ident"].stringValue()
            val årsak: String = this["årsak"].stringValue("")
            return when (eventName) {
                "behandling_opprettet" -> {
                    BehandlingOpprettet(søknadId, behandlingId, skjedde, ident)
                }

                "behandling_avbrutt" -> {
                    BehandlingAvbrutt(søknadId, behandlingId, skjedde, ident, årsak)
                }

                "behandlingsresultat" -> {
                    BehandlingFerdig(søknadId, behandlingId, skjedde, ident)
                }

                else -> throw IllegalArgumentException("Kunne ikke opprette melding for $eventName")
            }
        }
    }

    abstract val hendelseType: HendelseType

    fun toMap(): Map<String, Any> =
        mapOf(
            "hendelse" to hendelseType,
            "ident" to ident,
            "behandlingId" to behandlingId,
            "søknadId" to søknadId,
            "dato" to skjedde,
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BehandlingHendelse) return false

        if (søknadId != other.søknadId) return false
        if (behandlingId != other.behandlingId) return false
        if (skjedde != other.skjedde) return false
        if (ident != other.ident) return false
        if (hendelseType != other.hendelseType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = søknadId.hashCode()
        result = 31 * result + behandlingId.hashCode()
        result = 31 * result + skjedde.hashCode()
        result = 31 * result + ident.hashCode()
        result = 31 * result + hendelseType.hashCode()
        return result
    }

    class BehandlingOpprettet(
        søknadId: UUID,
        behandlingId: UUID,
        skjedde: LocalDate,
        ident: String,
    ) : BehandlingHendelse(søknadId, behandlingId, skjedde, ident) {
        override val hendelseType = SØKT
    }

    class BehandlingFerdig(
        søknadId: UUID,
        behandlingId: UUID,
        skjedde: LocalDate,
        ident: String,
    ) : BehandlingHendelse(søknadId, behandlingId, skjedde, ident) {
        override val hendelseType: HendelseType = FERDIG_BEHANDLET
    }

    class BehandlingAvbrutt(
        søknadId: UUID,
        behandlingId: UUID,
        skjedde: LocalDate,
        ident: String,
        årsak: String,
    ) : BehandlingHendelse(søknadId, behandlingId, skjedde, ident) {
        override val hendelseType: HendelseType =
            when (årsak) {
                "Oppgaven er endret i Arena" -> {
                    HendelseType.AVBRUTT_BEHANDLES_ARENA
                }
                else -> HendelseType.AVBRUTT
            }
    }
}
