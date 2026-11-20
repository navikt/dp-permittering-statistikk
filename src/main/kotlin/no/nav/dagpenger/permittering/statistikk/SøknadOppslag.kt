package no.nav.dagpenger.permittering.statistikk

import com.fasterxml.jackson.annotation.JsonInclude
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.jackson3.jackson
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.introspect.DefaultAccessorNamingStrategy
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

class SøknadOppslagHttpKlient(
    private val httpClient: HttpClient = httpKlient(),
    private val tokenSupplier: () -> String,
    private val baseUrl: String,
) : SøknadOppslag {
    companion object {
        fun httpKlient(engine: HttpClientEngine = CIO.create {}): HttpClient =
            HttpClient(engine).config {
                expectSuccess = true
                install(ContentNegotiation) {
                    jackson {
                        accessorNaming(
                            DefaultAccessorNamingStrategy
                                .Provider()
                                .withFirstCharAcceptance(true, true),
                        ).disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                        changeDefaultPropertyInclusion {
                            JsonInclude.Value.construct(
                                JsonInclude.Include.NON_NULL,
                                JsonInclude.Include.USE_DEFAULTS,
                            )
                        }
                    }
                }
            }
    }

    override suspend fun erPermittertFraFiskeindustri(
        ident: String,
        søknadId: UUID,
    ): Boolean = postBehov("PermittertFiskeforedling", ident, søknadId)

    override suspend fun erPermittertUtenforFiskeindustri(
        ident: String,
        søknadId: UUID,
    ): Boolean = postBehov("Permittert", ident, søknadId)

    private suspend fun postBehov(
        behovNavn: String,
        ident: String,
        søknadId: UUID,
    ): Boolean =
        httpClient
            .post("$baseUrl/behov/$behovNavn") {
                header("Authorization", "Bearer ${tokenSupplier()}")
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                setBody(
                    mapOf(
                        "ident" to ident,
                        "søknadId" to søknadId,
                    ),
                )
            }.body<BehovResponse>()
            .verdi
}

private class BehovResponse(
    val verdi: Boolean,
)
