package no.nav.dagpenger.permittering.statistikk

import io.kotest.assertions.json.shouldEqualSpecifiedJsonIgnoringOrder
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.util.UUID

class SøknadOppslagHttpKlientTest {
    @Test
    fun `sender behov til riktig endepunkt med token og søknadsdata`() =
        runBlocking {
            lateinit var capturedRequest: CapturedRequest
            val engine =
                MockEngine { request ->
                    capturedRequest =
                        CapturedRequest(
                            method = request.method.value,
                            path = request.url.encodedPath,
                            authorization = request.headers[HttpHeaders.Authorization],
                            body = request.body.toByteArray().toString(Charsets.UTF_8),
                        )

                    val verdi = request.url.encodedPath.endsWith("PermittertFiskeforedling")
                    respond(
                        content = """{"verdi":$verdi}""",
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }
            val httpClient = SøknadOppslagHttpKlient.httpKlient(engine)
            val klient =
                SøknadOppslagHttpKlient(
                    httpClient = httpClient,
                    tokenSupplier = { "test-token" },
                    baseUrl = "http://localhost",
                )
            val ident = "test-ident"
            val søknadId = UUID.randomUUID()

            klient.erPermittertFraFiskeindustri(ident, søknadId) shouldBe true
            capturedRequest.method shouldBe "POST"
            capturedRequest.path shouldBe "/behov/PermittertFiskeforedling"
            capturedRequest.authorization shouldBe "Bearer test-token"
            capturedRequest.body shouldEqualSpecifiedJsonIgnoringOrder
                """
                {
                  "ident": "$ident",
                  "søknadId": "$søknadId"
                }
                
                """.trimIndent()

            klient.erPermittertUtenforFiskeindustri(ident, søknadId) shouldBe false
            capturedRequest.method shouldBe "POST"
            capturedRequest.path shouldBe "/behov/PermittertUtenforFiskeforedling"
            capturedRequest.authorization shouldBe "Bearer test-token"
            capturedRequest.body shouldEqualSpecifiedJsonIgnoringOrder
                """
                {
                  "ident": "$ident",
                  "søknadId": "$søknadId"
                }
                
                """.trimIndent()
        }

    private data class CapturedRequest(
        val method: String,
        val path: String,
        val authorization: String?,
        val body: String,
    )
}
