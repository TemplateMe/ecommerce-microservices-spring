package com.azsumtoshko.notification_server.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import java.util.Properties

@Configuration
class EmailConfiguration {
    @Value("\${spring.mail.username}")
    private val emailUsername: String? = null

    @Value("\${spring.mail.password}")
    private val emailPassword: String? = null

    @Bean
    fun javaMailSender(): JavaMailSender {
        val mailSender = JavaMailSenderImpl()
        mailSender.setHost("smtp.gmail.com")
        mailSender.setPort(587)
        mailSender.setUsername(emailUsername)
        mailSender.setPassword(emailPassword)

        val props: Properties = mailSender.javaMailProperties
        props.put("mail.transport.protocol", "smtp")
        props.put("mail.smtp.auth", "true")
        props.put("mail.smtp.starttls.enable", "true")
        props.put("mail.debug", "true")

        return mailSender
    }
}