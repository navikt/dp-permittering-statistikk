package no.nav.dagpenger.permittering.statistikk

import java.util.UUID

interface SøknadOppslag {
    suspend fun erPermittertFraFiskeindustri(
        ident: String,
        søknadId: UUID,
    ): Boolean

    suspend fun erPermittertUtenforFiskeindustri(
        ident: String,
        søknadId: UUID,
    ): Boolean
}
