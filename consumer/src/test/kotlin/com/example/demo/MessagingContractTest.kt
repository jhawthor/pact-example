package com.example.demo

import au.com.dius.pact.consumer.MessagePactBuilder
import au.com.dius.pact.consumer.MessagePactProviderRule
import au.com.dius.pact.consumer.Pact
import au.com.dius.pact.consumer.PactVerification
import au.com.dius.pact.model.v3.messaging.MessagePact
import com.example.demo.messaging.UserCreateEvent
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody
import org.assertj.core.api.KotlinAssertions.assertThat
import org.junit.Rule
import org.junit.Test
import java.text.SimpleDateFormat

class MessagingContractTest {

    @get:Rule
    val pactRule = MessagePactProviderRule("messaging-provider", this)

    private val datePattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX"
    private val sdf = SimpleDateFormat(datePattern)
    private val expiryDate = sdf.parse("2017-09-16T05:25:25.000+02:00")

    @Pact(provider = "messaging-provider", consumer = "messaging-consumer")
    fun createEvent(builder: MessagePactBuilder): MessagePact {

        val body = newJsonBody { body ->
            body.stringType("firstName", "Christian")
            body.stringValue("lastName", "Dräger")
            body.numberType("age", 30)
            body.booleanType("active")
            //body.timestamp()
            body.time("expiryDate", datePattern, expiryDate)

            body.`object`("ids") {
                it.numberType("id", 1)
                it.uuid("uuid")
            }
        }.build()

        return builder.given("a user was created")
                .expectsToReceive("a create event")
                .withContent(body)
                .toPact()
    }

    @Test
    @PactVerification("messaging-provider", fragment = "createEvent")
    fun canParseCreateEvent() {
        val result = jacksonObjectMapper().readValue(pactRule.message, UserCreateEvent::class.java)
        assertThat(result.firstName).isEqualTo("Christian")
        assertThat(result.lastName).isEqualTo("Dräger")
        assertThat(result.age).isEqualTo(30)
        assertThat(result.active).isTrue()
        assertThat(result.expiryDate).isEqualTo(expiryDate)
        assertThat(result.ids.id).isEqualTo(1)
        //assertThat(result.ids.uuid).isEqualTo(1)
    }
}