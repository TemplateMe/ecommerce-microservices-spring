package com.azsumtoshko.notification_server.service

import com.azsumtoshko.common.domain.dto.request.email.*
import com.azsumtoshko.common.domain.dto.response.base.ApiResponse
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
    private val mailSender: JavaMailSender,
    private val templateEngine: SpringTemplateEngine
) {

    private fun getTemplate(emailTemplate: EmailTemplateName): String {
        return when (emailTemplate) {
            EmailTemplateName.ACTIVATE_ACCOUNT -> "activate_account"
            EmailTemplateName.FEEDBACK -> "feedback"
            EmailTemplateName.FORGOT_PASSWORD -> "forgot_password"
            EmailTemplateName.MESSAGE_RECEIVED -> "message_received"
            EmailTemplateName.ORDER_RECEIVED -> "order_received"
            EmailTemplateName.PAYMENT_RECEIVED -> "payment_received"
            EmailTemplateName.VERIFY_EMAIL -> "verify_email"
        }
    }

    private fun sendEmail(
        to: String,
        subject: String,
        templateName: EmailTemplateName,
        properties: Map<String, Any>
    ): ApiResponse {
        return try {
            val mimeMessage: MimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(mimeMessage, MULTIPART_MODE_MIXED_RELATED, UTF_8.name())

            val context = Context()
            context.setVariables(properties)

            helper.setFrom("samuildobrinski@gmail.com")
            helper.setTo(to)
            helper.setSubject(subject)
            val content = templateEngine.process(getTemplate(templateName), context)
            helper.setText(content, true)

            mailSender.send(mimeMessage)

            ApiResponse(
                success = true,
                statusCode = 200,
                message = "Email sent successfully",
                data = mapOf("to" to to, "template" to templateName.name)
            )
        } catch (ex: Exception) {
            ApiResponse(
                success = false,
                statusCode = 500,
                message = "Failed to send email",
                errors = listOf(ex.message ?: "Unknown error")
            )
        }
    }

    fun sendAccountActivationEmail(request: AccountActivationEmailRequest): ApiResponse {
        val properties = mapOf(
            "username" to request.username,
            "confirmationUrl" to request.confirmationUrl,
            "activation_code" to request.activationCode
        )
        return sendEmail(request.to, request.subject, EmailTemplateName.ACTIVATE_ACCOUNT, properties)
    }

    fun sendForgotPasswordEmail(request: ForgotPasswordEmailRequest): ApiResponse {
        val properties = mapOf(
            "username" to request.username,
            "resetUrl" to request.resetUrl
        )
        return sendEmail(request.to, request.subject, EmailTemplateName.FORGOT_PASSWORD, properties)
    }

    fun sendFeedbackEmail(request: FeedbackEmailRequest): ApiResponse {
        val properties = mapOf(
            "username" to request.username,
            "message" to request.message
        )
        return sendEmail(request.to, request.subject, EmailTemplateName.FEEDBACK, properties)
    }

    fun sendMessageReceivedEmail(request: MessageReceivedEmailRequest): ApiResponse {
        val properties = mapOf(
            "senderName" to request.senderName,
            "message" to request.message
        )
        return sendEmail(request.to, request.subject, EmailTemplateName.MESSAGE_RECEIVED, properties)
    }

    fun sendOrderReceivedEmail(request: OrderReceivedEmailRequest): ApiResponse {
        val properties = mapOf(
            "orderId" to request.orderId,
            "orderDate" to request.orderDate,
            "username" to request.username
        )
        return sendEmail(request.to, request.subject, EmailTemplateName.ORDER_RECEIVED, properties)
    }

    fun sendPaymentReceivedEmail(request: PaymentReceivedEmailRequest): ApiResponse {
        val properties = mapOf(
            "paymentAmount" to request.paymentAmount,
            "paymentDate" to request.paymentDate,
            "username" to request.username
        )
        return sendEmail(request.to, request.subject, EmailTemplateName.PAYMENT_RECEIVED, properties)
    }

    fun sendVerifyEmail(request: VerifyEmailRequest): ApiResponse {
        val properties = mapOf(
            "verificationUrl" to request.verificationUrl,
            "username" to request.username
        )
        return sendEmail(request.to, request.subject, EmailTemplateName.VERIFY_EMAIL, properties)
    }
}