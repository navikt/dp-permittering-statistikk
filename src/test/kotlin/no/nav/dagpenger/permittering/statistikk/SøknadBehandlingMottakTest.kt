package no.nav.dagpenger.permittering.statistikk

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.UUID
import tools.jackson.databind.JsonNode

class SøknadBehandlingMottakTest {
    private val testRapid = TestRapid()
    private val behandlingId = UUID.randomUUID()
    private val søknadId = UUID.randomUUID()
    private val ident = "12345612345"

    @Test
    fun `Skal publisere behov når en søknadsbehandling er opprettet`() {
        SøknadBehandlingMottak(testRapid)

        testRapid.sendTestMessage(
            søknadsbehandlingHendelse(hendelseNavn = "behandling_opprettet"),
        )
        testRapid.inspektør.size shouldBe 1
        testRapid.inspektør.message(0).let {
            it["@event_name"].stringValue() shouldBe "behov"
            it["@behov"].map { behov -> behov.stringValue() } shouldBe listOf("PermittertFiskeforedling", "Permittert")
            it["ident"].stringValue() shouldBe ident
            it["søknadId"].stringValue() shouldBe søknadId.toString()
            it["behandlingId"].stringValue() shouldBe behandlingId.toString()
        }
    }

    private fun søknadsbehandlingHendelse(
        hendelseNavn: String,
        søknadId: UUID = this.søknadId,
        behandlingId: UUID = this.behandlingId,
        ident: String = this.ident,
    ): String {
        //language=JSON
        return """
            {
              "@event_name": "$hendelseNavn",
              "behandletHendelse": {
                "type": "Søknad",
                "id": "$søknadId"
              },
              "behandlingId": "$behandlingId",
              "ident": "$ident"
            }
            """.trimIndent()
    }

    private fun behov(
        søknadId: UUID = this.søknadId,
        behandlingId: UUID = this.behandlingId,
        ident: String = this.ident,
    ): String {
        //language=JSON
        return """
            {
              "@event_name": "behov",
              "@behov": ["PermittertFiskeforedling", "Permittert"],,
              "ident": "$ident"
              "søknadId": "$søknadId",
              "behandlingId": "$behandlingId"
            }
            """.trimIndent()
    }
}
fun JsonNode.asUUID(): UUID = this.asString().let { UUID.fromString(it) }