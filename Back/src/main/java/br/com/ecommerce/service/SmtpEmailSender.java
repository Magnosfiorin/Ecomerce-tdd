package br.com.ecommerce.service;

import br.com.ecommerce.config.DotenvLoader;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class SmtpEmailSender {

    public void validarConfiguracao() {
        configuracao();
    }

    public void enviarLinkRecuperacao(String destinatario, String nome, String link) {
        SmtpConfig config = configuracao();
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.host", config.host());
        properties.put("mail.smtp.port", config.port());
        properties.put("mail.smtp.ssl.enable", Boolean.toString(config.ssl()));
        properties.put("mail.smtp.starttls.enable", Boolean.toString(!config.ssl()));
        properties.put("mail.smtp.starttls.required", Boolean.toString(!config.ssl()));
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "10000");
        properties.put("mail.smtp.writetimeout", "10000");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(config.username(), config.password());
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(config.from()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject("Recuperação de senha", StandardCharsets.UTF_8.name());
            message.setText(
                    "Olá, " + nome + "!\n\n"
                            + "Recebemos uma solicitação para redefinir a senha da sua conta.\n"
                            + "Use o link abaixo em até 30 minutos. Ele poderá ser usado uma única vez:\n\n"
                            + link + "\n\n"
                            + "Se você não solicitou esta alteração, ignore este e-mail.",
                    StandardCharsets.UTF_8.name());
            Transport.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Não foi possível enviar o e-mail de recuperação.", e);
        }
    }

    private SmtpConfig configuracao() {
        String host = DotenvLoader.get("SMTP_HOST");
        String port = DotenvLoader.get("SMTP_PORT", "587");
        String username = DotenvLoader.get("SMTP_USERNAME");
        String password = DotenvLoader.get("SMTP_PASSWORD");
        String from = DotenvLoader.get("SMTP_FROM");
        String sslValue = DotenvLoader.get("SMTP_USE_SSL", "false");

        if (isBlank(host) || isBlank(username) || isBlank(password) || isBlank(from)) {
            throw new IllegalStateException("O envio de e-mail não está configurado.");
        }

        try {
            int portNumber = Integer.parseInt(port);
            if (portNumber < 1 || portNumber > 65535) {
                throw new NumberFormatException();
            }
            InternetAddress fromAddress = new InternetAddress(from, true);
            fromAddress.validate();
            return new SmtpConfig(host, Integer.toString(portNumber), username, password, from,
                    Boolean.parseBoolean(sslValue));
        } catch (NumberFormatException | MessagingException e) {
            throw new IllegalStateException("A configuração SMTP está inválida.", e);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record SmtpConfig(String host, String port, String username, String password, String from, boolean ssl) {}
}
