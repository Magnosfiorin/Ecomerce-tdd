package br.com.ecommerce.service;

import org.mindrot.jbcrypt.BCrypt;

import java.util.regex.Pattern;

/**
 * Utilitário para hashing e verificação segura de senhas utilizando BCrypt.
 * 
 * O BCrypt é um algoritmo de hashing adaptativo que inclui um salt automático
 * e fator de custo configurável, tornando ataques de força bruta inviáveis.
 * 
 * IMPORTANTE: Senhas NUNCA devem ser armazenadas em texto puro no banco de dados.
 */
public final class SenhaUtils {
    private static final Pattern BCRYPT_HASH_PATTERN =
            Pattern.compile("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");

    /**
     * Fator de custo do BCrypt (log2 do número de rounds).
     * 12 = 2^12 = 4096 rounds — bom equilíbrio entre segurança e performance.
     */
    private static final int BCRYPT_COST = 12;

    private SenhaUtils() {
        // Classe utilitária — não instanciável
    }

    /**
     * Gera o hash BCrypt de uma senha em texto puro.
     *
     * @param senhaTexto Senha em texto puro informada pelo usuário.
     * @return Hash BCrypt da senha (60 caracteres, inclui salt embutido).
     */
    public static String hashSenha(String senhaTexto) {
        if (senhaTexto == null || senhaTexto.isBlank()) {
            throw new IllegalArgumentException("A senha não pode ser nula ou vazia para gerar hash.");
        }
        return BCrypt.hashpw(senhaTexto, BCrypt.gensalt(BCRYPT_COST));
    }

    /**
     * Verifica se uma senha em texto puro corresponde a um hash BCrypt armazenado.
     *
     * @param senhaTexto   Senha em texto puro informada no login.
     * @param hashArmazenado Hash BCrypt armazenado no banco de dados.
     * @return true se a senha confere com o hash, false caso contrário.
     */
    public static boolean verificarSenha(String senhaTexto, String hashArmazenado) {
        if (senhaTexto == null || senhaTexto.isBlank() || hashArmazenado == null || hashArmazenado.isBlank()) {
            return false;
        }
        try {
            return BCrypt.checkpw(senhaTexto, hashArmazenado);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Verifica se um valor já é um hash BCrypt válido.
     * Hashes BCrypt sempre começam com "$2a$", "$2b$" ou "$2y$" e têm 60 caracteres.
     *
     * @param valor String a ser verificada.
     * @return true se o valor parece ser um hash BCrypt.
     */
    public static boolean isHashBCrypt(String valor) {
        return valor != null && BCRYPT_HASH_PATTERN.matcher(valor).matches();
    }
}
