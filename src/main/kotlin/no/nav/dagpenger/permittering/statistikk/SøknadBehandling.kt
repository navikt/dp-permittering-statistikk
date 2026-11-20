package no.nav.dagpenger.permittering.statistikk

import java.time.LocalDate
import java.util.UUID

data class SøknadBehandling(
    val søknadId: UUID,
    val mottattDato: LocalDate,
    private var behandlingId: UUID,
    private var avsluttetDato: LocalDate? = null,
    private var avsluttetType: BehandlingHendelse.HendelseType? = null,
) {
    fun avsluttSøknadBehandling(
        avsluttetDato: LocalDate,
        avsluttetType: BehandlingHendelse.HendelseType,
        behandlingId: UUID,
    ) {
        this.avsluttetDato = avsluttetDato
        this.avsluttetType = avsluttetType
        this.behandlingId = behandlingId
    }

    fun behandlingId() = this.behandlingId

    fun avsluttetDato() = this.avsluttetDato

    fun avsluttetType() = this.avsluttetType
}
