package com.azsumtoshko.notification_server.configuration

import com.azsumtoshko.common.constant.NOTIFICATION_GROUP_ID
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer

@Configuration
class KafkaConfiguration {
    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    private fun baseConsumerProps(): Map<String, Any> {
        return mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            ConsumerConfig.GROUP_ID_CONFIG to NOTIFICATION_GROUP_ID,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest"
        )
    }

    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, Any> {
        val deserializer = JsonDeserializer<Any>()
        deserializer.setRemoveTypeHeaders(false)
        deserializer.addTrustedPackages("com.azsumtoshko.common.domain.dto.request.email.*")
        deserializer.setUseTypeMapperForKey(true)

        val consumerFactory: ConsumerFactory<String, Any> = DefaultKafkaConsumerFactory(
            baseConsumerProps(),
            StringDeserializer(),
            deserializer
        )

        val factory = ConcurrentKafkaListenerContainerFactory<String, Any>()
        factory.consumerFactory = consumerFactory
        return factory
    }
}