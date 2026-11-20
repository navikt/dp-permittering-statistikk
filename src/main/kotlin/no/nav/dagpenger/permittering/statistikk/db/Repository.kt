package no.nav.dagpenger.permittering.statistikk.db

import no.nav.dagpenger.permittering.statistikk.SøknadBehandling
import java.util.UUID

interface Repository {
    fun lagre(søknadBehandling: SøknadBehandling)

    fun finnSøknadBehandling(søknadId: UUID): SøknadBehandling?
}
