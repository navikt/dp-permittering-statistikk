package no.nav.dagpenger.permittering.statistikk

import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.helse.rapids_rivers.RapidApplication

internal class ApplicationBuilder(
    configuration: Map<String, String>,
) : RapidsConnection.StatusListener {
    private val rapidsConnection: RapidsConnection =
        RapidApplication
            .create(configuration)
            .also { rapid ->
//                SøknadBehandlingMottak(rapid)
                rapid.register(this)
            }

    fun start() = rapidsConnection.start()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        logger.info { "Starter ${Configuration.APP_NAME}" }
    }

    private companion object {
        private val logger = KotlinLogging.logger {}
    }
}
