package com.services

import com.sendgrid.Method
import com.sendgrid.Request
import com.sendgrid.SendGrid
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.Email
import com.sendgrid.helpers.mail.objects.Personalization
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory


object EmailService {

    private val logger = LoggerFactory.getLogger(EmailService::class.java)

    private val SendGridApiKey = System.getenv("SENDGRID_API_KEY")
    private val SendGridSenderEmail = System.getenv("SENDGRID_SENDER_EMAIL")
    private const val SENDGRID_PASSWORD_RESET_TEMPLATE_ID = "d-5cc203f8107b4b90a16f7c4f58b277fc"

    suspend fun sendPasswordResetEmail(toEmail: String, otpCode: String): Boolean {
        if (SendGridApiKey.isNullOrBlank() || SendGridSenderEmail.isNullOrBlank()) {
            logger.error("SendGrid API Key or Sender Email is not configured. Email will not be sent.")
            return false
        }
        if (SENDGRID_PASSWORD_RESET_TEMPLATE_ID.isBlank()) {
            logger.error("SendGrid Password Reset Template ID is not configured. Email will not be sent.")
            return false
        }

        val from = Email(SendGridSenderEmail)
        val to = Email(toEmail)

        val mail = Mail()
        mail.setFrom(from)
        mail.setSubject("RooMatch - Password Reset Code")
        mail.setTemplateId(SENDGRID_PASSWORD_RESET_TEMPLATE_ID)

        val personalization = Personalization()
        personalization.addTo(to)
        personalization.addDynamicTemplateData("otp_code", otpCode)
        mail.addPersonalization(personalization)

        val sg = SendGrid(SendGridApiKey)
        return try {
            val sendGridRequest = Request()
            sendGridRequest.setMethod(Method.POST)
            sendGridRequest.setEndpoint("mail/send")
            sendGridRequest.setBody(mail.build())

            val response = withContext(Dispatchers.IO) {
                sg.api(sendGridRequest)
            }
            logger.info("Email sent via template to $toEmail, status: ${response.statusCode}, body: ${response.body}")

            response.statusCode in 200..299
        } catch (ex: Exception) {
            logger.error("Error sending email to $toEmail using template: ${ex.message}", ex)
            false
        }
    }


}