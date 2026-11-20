package no.nav.dagpenger.permittering.statistikk

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.dagpenger.permittering.statistikk.db.Repository

private val logger = KotlinLogging.logger {}

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
                            søknadId = behandlingHendelse.søknadId,
                            behandlingId = behandlingHendelse.behandlingId,
                            mottattDato = behandlingHendelse.dato,
                        ),
                    )
                    JsonMessage
                        .newMessage(
                            map = behandlingHendelse.toMap(),
                        ).let {
                            context.publish(behandlingHendelse.søknadId.toString(), it.toJson())
                        }
                    logger.info { "Send permittering søknad(${behandlingHendelse.søknadId}) mottatt til statistikk" }
                }
            }

            is BehandlingHendelse.BehandlingAvbrutt,
            is BehandlingHendelse.BehandlingFerdig,
            -> {
                val søknadBehandling = repository.finnSøknadBehandling(søknadId = behandlingHendelse.søknadId)
                if (søknadBehandling != null) {
                    søknadBehandling.avsluttSøknadBehandling(
                        avsluttetDato = behandlingHendelse.dato,
                        avsluttetType = behandlingHendelse.hendelseType,
                        behandlingId = behandlingHendelse.behandlingId,
                    )
                    repository.lagre(søknadBehandling)
                    JsonMessage
                        .newMessage(
                            map = behandlingHendelse.toMap(),
                        ).let {
                            context.publish(behandlingHendelse.søknadId.toString(), it.toJson())
                        }
                    logger.info { "Send permittering søknad(${behandlingHendelse.søknadId}) avsluttet til statistikk" }
                }
            }
        }
    }
}
