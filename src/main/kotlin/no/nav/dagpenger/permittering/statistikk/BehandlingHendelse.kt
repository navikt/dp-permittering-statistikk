package no.nav.dagpenger.permittering.statistikk

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import no.nav.dagpenger.permittering.statistikk.BehandlingHendelse.HendelseType.FERDIG_BEHANDLET
import no.nav.dagpenger.permittering.statistikk.BehandlingHendelse.HendelseType.SØKT
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

sealed class BehandlingHendelse(
    val søknadId: UUID,
    val behandlingId: UUID,
    val dato: LocalDate,
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
            val behandlingId: UUID = UUID.fromString(this["behandlingId"].stringValue())
            val ident: String = this["ident"].stringValue()
            val årsak: String = this["årsak"].stringValue("")
            return when (eventName) {
                "behandling_opprettet" -> {
                    val dato: LocalDate = LocalDate.parse(this["behandletHendelse"]["skjedde"].stringValue())
                    BehandlingOpprettet(søknadId, behandlingId, dato, ident)
                }

                "behandling_avbrutt" -> {
                    val dato: LocalDate = LocalDateTime.parse(this["@opprettet"].stringValue()).toLocalDate()
                    BehandlingAvbrutt(søknadId, behandlingId, dato, ident, årsak)
                }

                "behandlingsresultat" -> {
                    val dato: LocalDate = LocalDateTime.parse(this["sistEndret"].stringValue()).toLocalDate()
                    BehandlingFerdig(søknadId, behandlingId, dato, ident)
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
            "dato" to dato,
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BehandlingHendelse) return false

        if (søknadId != other.søknadId) return false
        if (behandlingId != other.behandlingId) return false
        if (dato != other.dato) return false
        if (ident != other.ident) return false
        if (hendelseType != other.hendelseType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = søknadId.hashCode()
        result = 31 * result + behandlingId.hashCode()
        result = 31 * result + dato.hashCode()
        result = 31 * result + ident.hashCode()
        result = 31 * result + hendelseType.hashCode()
        return result
    }

    override fun toString(): String =
        "BehandlingHendelse(søknadId=$søknadId, behandlingId=$behandlingId, skjedde=$dato, hendelseType=$hendelseType)"

    class BehandlingOpprettet(
        søknadId: UUID,
        behandlingId: UUID,
        dato: LocalDate,
        ident: String,
    ) : BehandlingHendelse(søknadId, behandlingId, dato, ident) {
        override val hendelseType = SØKT
    }

    class BehandlingFerdig(
        søknadId: UUID,
        behandlingId: UUID,
        dato: LocalDate,
        ident: String,
    ) : BehandlingHendelse(søknadId, behandlingId, dato, ident) {
        override val hendelseType: HendelseType = FERDIG_BEHANDLET
    }

    class BehandlingAvbrutt(
        søknadId: UUID,
        behandlingId: UUID,
        dato: LocalDate,
        ident: String,
        årsak: String,
    ) : BehandlingHendelse(søknadId, behandlingId, dato, ident) {
        override val hendelseType: HendelseType =
            when (årsak) {
                "Oppgaven er endret i Arena" -> {
                    HendelseType.AVBRUTT_BEHANDLES_ARENA
                }
                else -> HendelseType.AVBRUTT
            }
    }
}
