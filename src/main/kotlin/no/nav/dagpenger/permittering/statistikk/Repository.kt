package no.nav.dagpenger.permittering.statistikk

import java.util.UUID

interface Repository {
    fun lagre(søknadBehandling: SøknadBehandling)
}

data class SøknadBehandling(
    val søknadId: UUID,
    val behandlingId: UUID,
)
