package no.nav.dagpenger.permittering.statistikk

import io.kotest.matchers.maps.shouldContain
import kotlin.test.Test

class ConfigurationTest {
    @Test
    fun `har rapid-konfigurasjon som default`() {
        Configuration.config shouldContain ("KAFKA_RAPID_TOPIC" to "teamdagpenger.rapid.v1")
        Configuration.config shouldContain ("KAFKA_CONSUMER_GROUP_ID" to "dp-permittering-statistikk-v1")
    }
}
