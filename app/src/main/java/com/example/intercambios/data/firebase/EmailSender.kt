package com.example.intercambios.data.firebase

import android.util.Log
import javax.mail.*
import javax.mail.internet.*
import java.util.*

class EmailSender {

    fun enviarCorreoSMTP(
        recipientEmail: String,
        organizer: String,
        code: String,
        eventName: String,
        link: String
    ) {
        val email = "intercambiosjr.notify@gmail.com"
        val password = "xbgorlgkhhuvcgju"

        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
        }

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(email, password)
            }
        })

        try {
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(email))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail))
                subject = "Invitación a un intercambio"
                setText("Hola! $organizer te ha invitado al intercambio $eventName. Únete usando el código: $code o el siguiente enlace\n$link")
            }

            // Envía el correo en un hilo secundario
            Thread {
                try {
                    Transport.send(message)
                    Log.d("Email", "Correo enviado exitosamente")
                } catch (e: Exception) {
                    Log.e("Email", "Error enviando el correo", e)
                }
            }.start()
        } catch (e: Exception) {
            Log.e("Email", "Error preparando el correo", e)
        }
    }

    fun notificacionCambioTemas(
        recipientEmail: String,
        organizer: String,
        code: String,
        eventName: String,
        link: String
    ) {
        val email = "intercambiosjr.notify@gmail.com"
        val password = "xbgorlgkhhuvcgju"

        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
        }

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(email, password)
            }
        })

        try {
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(email))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail))
                subject = "Hubo una actualización en un intercambio"
                setText("Hola! $organizer ha modificado el intercambio $eventName, es necesario que entres a actualizar tu tema. Si no te has unido, únete usando el código: $code o el siguiente enlace\n$link")
            }

            // Envía el correo en un hilo secundario
            Thread {
                try {
                    Transport.send(message)
                    Log.d("Email", "Correo enviado exitosamente")
                } catch (e: Exception) {
                    Log.e("Email", "Error enviando el correo", e)
                }
            }.start()
        } catch (e: Exception) {
            Log.e("Email", "Error preparando el correo", e)
        }
    }

}