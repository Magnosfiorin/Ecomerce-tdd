package br.com.ecommerce.service;

import br.com.ecommerce.config.DatabaseConfig;
import br.com.ecommerce.config.DotenvLoader;
import br.com.ecommerce.model.Usuario;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HexFormat;
import java.util.OptionalInt;

public class PasswordResetService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final long TOKEN_EXPIRATION_MINUTES = 30;

    private final DatabaseConfig databaseConfig;
    private final SmtpEmailSender emailSender;

    public PasswordResetService(DatabaseConfig databaseConfig) {
        this(databaseConfig, new SmtpEmailSender());
    }

    PasswordResetService(DatabaseConfig databaseConfig, SmtpEmailSender emailSender) {
        this.databaseConfig = databaseConfig;
        this.emailSender = emailSender;
    }

    public void solicitarRecuperacao(String email) {
        if (!Usuario.isEmailValido(email)) {
            throw new IllegalArgumentException("Informe um e-mail válido.");
        }
        if (databaseConfig == null) {
            throw new IllegalStateException("O serviço de recuperação está indisponível.");
        }
        emailSender.validarConfiguracao();
        String resetUrl = obterUrlBase();

        String nome;
        String emailCadastrado;
        int usuarioId;
        String token = gerarToken();
        String tokenHash = hashToken(token);

        try (Connection connection = databaseConfig.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT id, nome, email FROM usuarios WHERE LOWER(email) = LOWER(?)")) {
                    statement.setString(1, email.trim());
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            connection.commit();
                            return;
                        }
                        usuarioId = result.getInt("id");
                        nome = result.getString("nome");
                        emailCadastrado = result.getString("email");
                    }
                }

                try (PreparedStatement delete = connection.prepareStatement(
                        "DELETE FROM password_reset_tokens WHERE usuario_id = ?")) {
                    delete.setInt(1, usuarioId);
                    delete.executeUpdate();
                }
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO password_reset_tokens (token_hash, usuario_id, expires_at) "
                                + "VALUES (?, ?, CURRENT_TIMESTAMP + (? * INTERVAL '1 minute'))")) {
                    insert.setString(1, tokenHash);
                    insert.setInt(2, usuarioId);
                    insert.setLong(3, TOKEN_EXPIRATION_MINUTES);
                    insert.executeUpdate();
                }
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("O serviço de recuperação está indisponível.", e);
        }

        String link = resetUrl + "/redefinir-senha?token=" + token;
        try {
            emailSender.enviarLinkRecuperacao(emailCadastrado, nome, link);
        } catch (RuntimeException e) {
            invalidarToken(tokenHash);
            throw e;
        }
    }

    public OptionalInt redefinirSenha(String token, String novaSenha) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("O link de recuperação é inválido ou expirou.");
        }
        if (novaSenha == null || novaSenha.length() < 8) {
            throw new IllegalArgumentException("A senha deve conter no mínimo 8 caracteres.");
        }
        if (databaseConfig == null) {
            throw new IllegalStateException("O serviço de recuperação está indisponível.");
        }

        String tokenHash = hashToken(token);
        try (Connection connection = databaseConfig.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int usuarioId;
                try (PreparedStatement find = connection.prepareStatement(
                        "SELECT usuario_id FROM password_reset_tokens "
                                + "WHERE token_hash = ? AND expires_at > CURRENT_TIMESTAMP "
                                + "AND used_at IS NULL FOR UPDATE")) {
                    find.setString(1, tokenHash);
                    try (ResultSet result = find.executeQuery()) {
                        if (!result.next()) {
                            connection.rollback();
                            return OptionalInt.empty();
                        }
                        usuarioId = result.getInt("usuario_id");
                    }
                }

                try (PreparedStatement updatePassword = connection.prepareStatement(
                        "UPDATE usuarios SET senha = ? WHERE id = ?")) {
                    updatePassword.setString(1, SenhaUtils.hashSenha(novaSenha));
                    updatePassword.setInt(2, usuarioId);
                    if (updatePassword.executeUpdate() != 1) {
                        connection.rollback();
                        return OptionalInt.empty();
                    }
                }
                try (PreparedStatement markUsed = connection.prepareStatement(
                        "UPDATE password_reset_tokens SET used_at = CURRENT_TIMESTAMP "
                                + "WHERE token_hash = ? AND used_at IS NULL")) {
                    markUsed.setString(1, tokenHash);
                    if (markUsed.executeUpdate() != 1) {
                        connection.rollback();
                        return OptionalInt.empty();
                    }
                }
                connection.commit();
                return OptionalInt.of(usuarioId);
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Não foi possível redefinir a senha.", e);
        }
    }

    private String obterUrlBase() {
        String value = DotenvLoader.get("APP_BASE_URL");
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("A URL pública do frontend não está configurada.");
        }
        try {
            URI uri = URI.create(value);
            if (!("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                    || uri.getHost() == null) {
                throw new IllegalArgumentException();
            }
            return value.replaceAll("/+$", "");
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("A URL pública do frontend está inválida.", e);
        }
    }

    private String gerarToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Não foi possível proteger o token de recuperação.", e);
        }
    }

    private void invalidarToken(String tokenHash) {
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM password_reset_tokens WHERE token_hash = ?")) {
            statement.setString(1, tokenHash);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Não foi possível invalidar o link de recuperação.", e);
        }
    }
}
