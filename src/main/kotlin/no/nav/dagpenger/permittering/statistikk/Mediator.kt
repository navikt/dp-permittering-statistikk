package no.nav.dagpenger.permittering.statistikk

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext

class Mediator {
    fun håndter(
        behandlingHendelse: BehandlingHendelse,
        context: MessageContext,
    ) {
        when (behandlingHendelse){
            //spørre dp-soknad-orkestrator behov permittering/permitterings fisk
            // dersom er permittering så må vi lagre noe et eller annet sted
            // og sende ut en hendelse til A-team

            is BehandlingHendelse.BehandlingOpprettet -> {
                JsonMessage.newNeed(
                    behov = setOf("PermittertFiskeforedling", "Permittert"),
                    map =
                        mapOf(
                            "ident" to behandlingHendelse.ident,
                            "søknadId" to behandlingHendelse.søknadId,
                            "behandlingId" to behandlingHendelse.behandlingId,
                            "skjedde" to behandlingHendelse.skjedde,
                        ),
                ).let {
                    context.publish(behandlingHendelse.søknadId.toString(), it.toJson())
                }
            }

            is BehandlingHendelse.BehandlingAvbrutt -> {
            }

            is BehandlingHendelse.BehandlingFerdig -> TODO()
        }
    }
}
