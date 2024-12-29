package com.example.intercambios.data.firebase

import android.content.Context
import android.util.Log
import com.example.intercambios.R
import javax.mail.*
import javax.mail.internet.*
import java.util.*

class EmailSender {

    fun enviarCorreoSMTP(
        recipientEmail: String,
        organizer: String,
        code: String,
        eventName: String,
        link: String,
        context: Context
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
                subject = context.getString(R.string.asunto_smtp)
                setText(context.getString(R.string.invitacion_intercambio_es,organizer,eventName,code,link))
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
        link: String,
        context: Context
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
                subject = context.getString(R.string.asunto_notif_temas)
                setText(context.getString(R.string.modificar_intercambio_es,organizer,eventName,code,link))
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

    fun notificarResultados(
        recipientEmail: String,
        eventName: String,
        link: String,
        context: Context
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
                subject = context.getString(R.string.resultados_listos_titulo)
                setText(context.getString(R.string.resultados_listos_es,eventName,link))
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