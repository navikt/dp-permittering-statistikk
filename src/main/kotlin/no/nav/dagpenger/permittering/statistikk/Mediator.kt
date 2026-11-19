package no.nav.dagpenger.permittering.statistikk

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Mediator(
    private val søknadOppslag: SøknadOppslag,
    private val repository: Repository,
) {
    suspend fun håndter(
        behandlingHendelse: BehandlingHendelse,
        context: MessageContext,
    ) {
        when (behandlingHendelse) {
            is BehandlingHendelse.BehandlingOpprettet -> {
                val erPermittert =
                    withContext(Dispatchers.IO) {
                        søknadOppslag.erPermittertUtenforFiskeindustri(
                            ident = behandlingHendelse.ident,
                            søknadId = behandlingHendelse.søknadId,
                        ) ||
                            søknadOppslag.erPermittertFraFiskeindustri(
                                ident = behandlingHendelse.ident,
                                søknadId = behandlingHendelse.søknadId,
                            )
                    }

                if (erPermittert) {
                    repository.lagre(
                        SøknadBehandling(
                            behandlingId = behandlingHendelse.behandlingId,
                            søknadId = behandlingHendelse.søknadId,
                        ),
                    )
                    JsonMessage
                        .newMessage(
                            map = behandlingHendelse.toMap(),
                        ).let {
                            context.publish(behandlingHendelse.søknadId.toString(), it.toJson())
                        }
                }
            }

            is BehandlingHendelse.BehandlingAvbrutt -> {
                if (repository.finnes(behandlingHendelse.behandlingId)) {
                    JsonMessage
                        .newMessage(
                            map = behandlingHendelse.toMap(),
                        ).let {
                            context.publish(behandlingHendelse.søknadId.toString(), it.toJson())
                        }
                }
            }

            is BehandlingHendelse.BehandlingFerdig -> TODO()
        }
    }
}
