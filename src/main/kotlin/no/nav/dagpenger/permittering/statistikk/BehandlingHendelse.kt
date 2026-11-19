package no.nav.dagpenger.permittering.statistikk

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
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
                    BehandlingAvbrutt(søknadId, behandlingId, skjedde, ident)
                }

                "behandlingsresultat" -> {
                    BehandlingFerdig(søknadId, behandlingId, skjedde, ident)
                }

                else -> throw IllegalArgumentException("Kunne ikke opprette melding for $eventName")
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as BehandlingHendelse

        return this.behandlingId == other.behandlingId &&
            this.søknadId == other.søknadId &&
            this.ident == other.ident &&
            this.skjedde == other.skjedde
    }

    override fun hashCode(): Int {
        var result = søknadId.hashCode()
        result = 31 * result + behandlingId.hashCode()
        result = 31 * result + skjedde.hashCode()
        result = 31 * result + ident.hashCode()
        return result
    }

    abstract fun hendelsesType(): HendelseType

    fun toMap(): Map<String, Any> =
        mapOf(
            "hendelse" to hendelsesType(),
            "ident" to ident,
            "behandlingId" to behandlingId,
            "søknadId" to søknadId,
            "dato" to skjedde,
        )

    class BehandlingOpprettet(
        søknadId: UUID,
        behandlingId: UUID,
        skjedde: LocalDate,
        ident: String,
    ) : BehandlingHendelse(søknadId, behandlingId, skjedde, ident) {
        override fun hendelsesType() = SØKT
    }

    class BehandlingFerdig(
        søknadId: UUID,
        behandlingId: UUID,
        skjedde: LocalDate,
        ident: String,
    ) : BehandlingHendelse(søknadId, behandlingId, skjedde, ident) {
        override fun hendelsesType(): HendelseType = HendelseType.FERDIG_BEHANDLET
    }

    class BehandlingAvbrutt(
        søknadId: UUID,
        behandlingId: UUID,
        skjedde: LocalDate,
        ident: String,
    ) : BehandlingHendelse(søknadId, behandlingId, skjedde, ident) {
        override fun hendelsesType(): HendelseType {
            TODO("Not yet implemented")
        }
    }
}
