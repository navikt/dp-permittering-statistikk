package no.nav.dagpenger.permittering.statistikk

import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import no.nav.dagpenger.oauth2.CachedOauth2Client
import no.nav.dagpenger.oauth2.OAuth2Config

object Configuration {
    const val APP_NAME = "dp-permittering-statistikk"

    private val defaultProperties =
        ConfigurationMap(
            mapOf(
                "RAPID_APP_NAME" to APP_NAME,
                "KAFKA_CONSUMER_GROUP_ID" to "dp-permittering-statistikk-v1",
                "KAFKA_RAPID_TOPIC" to "teamdagpenger.permittering-statistikk.v1",
                "KAFKA_EXTRA_TOPIC" to "teamdagpenger.rapid.v1",
                "KAFKA_RESET_POLICY" to "LATEST",
                "SOKNAD_ORKESTRATOR_BASE_URL" to "http://dp-soknad-orkestrator",
                "SOKNAD_ORKESTRATOR_API_SCOPE" to "api://dev-gcp.teamdagpenger.dp-soknad-orkestrator/.default"
            ),
        )

    private val properties =
        ConfigurationProperties.systemProperties() overriding EnvironmentVariables() overriding defaultProperties

    val config: Map<String, String> =
        properties.list().reversed().fold(emptyMap()) { map, pair ->
            map + pair.second
        }

    val azureAdClient: CachedOauth2Client by lazy {
        val azureAdConfig = OAuth2Config.AzureAd(config = properties)
        CachedOauth2Client(
            tokenEndpointUrl = azureAdConfig.tokenEndpointUrl,
            authType = azureAdConfig.clientSecret(),
        )
    }


    val søknadOrkestratorTokenProvider = { clientCredentialsTokenProvider(søknadOrkestratorApiScope) }
    val søknadOrkestratorBaseUrl: String = properties[Key("SOKNAD_ORKESTRATOR_BASE_URL", stringType)]
    val søknadOrkestratorApiScope: String = properties[Key("SOKNAD_ORKESTRATOR_API_SCOPE", stringType)]
    private val clientCredentialsTokenProvider = { scope: String ->
        azureAdClient.clientCredentials(scope).access_token
            ?: throw RuntimeException("Failed to get access token")
    }
}
