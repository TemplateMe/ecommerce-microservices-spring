package com.azsumtoshko.notification_server.service

import com.azsumtoshko.common.domain.dto.request.AccountActivationEmailRequest
import com.azsumtoshko.common.domain.enums.EmailTemplateName
import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.mail.javamail.MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED
import org.springframework.stereotype.Service
import org.thymeleaf.context.Context
import org.thymeleaf.spring6.SpringTemplateEngine
import kotlin.text.Charsets.UTF_8

@Service
class SmtpService(
    private final val mailSender: JavaMailSender,
    private final val templateEngine: SpringTemplateEngine
) {
    private fun getTemplate(emailTemplate: EmailTemplateName): String {
        return if(emailTemplate == EmailTemplateName.ACTIVATE_ACCOUNT) "activate_account"

        else "verify_email"
    }

    fun sendAccountVerificationEmail(
        request: AccountActivationEmailRequest
    ){
        val templateName: String = getTemplate(EmailTemplateName.ACTIVATE_ACCOUNT)

        val mimeMessage: MimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(
            mimeMessage,
            MULTIPART_MODE_MIXED_RELATED,
            UTF_8.name()
        )

        val properties: Map<String, Any> = mapOf(
            "username" to request.username,
            "confirmationUrl" to request.confirmationUrl,
            "activation_code" to request.activationCode
        )

        val context = Context()
        context.setVariables(properties)

        helper.setFrom("samuildobrinski@gmail.com")
        helper.setTo(request.to)
        helper.setSubject(request.subject)

        val template: String = templateEngine.process(templateName, context)
        helper.setText(template, true)

        mailSender.send(mimeMessage)
    }
}