package br.com.ecommerce.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilitário para carregar variáveis de ambiente a partir de um arquivo .env localizado
 * na raiz do projeto ou diretórios pai/atual.
 */
public class DotenvLoader {

    private static final Map<String, String> envValues = new HashMap<>();
    private static boolean loaded = false;
    private static String loadedFilePath = null;

    /**
     * Carrega as variáveis do arquivo .env caso ainda não tenham sido carregadas.
     */
    public static synchronized void load() {
        if (loaded) return;

        File envFile = findDotenvFile();
        if (envFile != null && envFile.exists() && envFile.isFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(envFile, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }

                    int equalsIndex = line.indexOf('=');
                    if (equalsIndex > 0) {
                        String key = line.substring(0, equalsIndex).trim();
                        String value = line.substring(equalsIndex + 1).trim();

                        // Remove aspas simples ou duplas ao redor do valor
                        if ((value.startsWith("\"") && value.endsWith("\"")) ||
                            (value.startsWith("'") && value.endsWith("'"))) {
                            value = value.substring(1, value.length() - 1);
                        }

                        envValues.put(key, value);

                        // Opcionalmente define como System Property se ainda não existir
                        if (System.getProperty(key) == null) {
                            System.setProperty(key, value);
                        }
                    }
                }
                loadedFilePath = envFile.getAbsolutePath();
                System.out.println("[Dotenv] Configurações carregadas do arquivo: " + loadedFilePath);
            } catch (Exception e) {
                System.err.println("[Dotenv] Erro ao ler arquivo .env: " + e.getMessage());
            }
        } else {
            System.out.println("[Dotenv] Nenhum arquivo .env encontrado. Usando variáveis do sistema ou application.properties.");
        }

        loaded = true;
    }

    /**
     * Busca o arquivo .env procurando no diretório atual e subindo nos diretórios pais.
     */
    private static File findDotenvFile() {
        // 1. Caminhos diretos comuns
        String[] candidates = {
            ".env",
            "../.env",
            "../../.env"
        };

        for (String candidate : candidates) {
            File f = new File(candidate);
            if (f.exists() && f.isFile()) {
                return f;
            }
        }

        // 2. Busca a partir de user.dir subindo até 4 níveis
        Path current = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        for (int i = 0; i < 4 && current != null; i++) {
            File testFile = current.resolve(".env").toFile();
            if (testFile.exists() && testFile.isFile()) {
                return testFile;
            }
            current = current.getParent();
        }

        return null;
    }

    /**
     * Retorna o valor de uma variável do .env, ou System.getenv() caso não esteja no .env.
     */
    public static String get(String key) {
        if (!loaded) {
            load();
        }

        // 1. Variável real do sistema operacional tem prioridade
        String systemEnv = System.getenv(key);
        if (systemEnv != null && !systemEnv.isBlank()) {
            return systemEnv;
        }

        // 2. Variável lida do arquivo .env
        return envValues.get(key);
    }

    public static String get(String key, String defaultValue) {
        String val = get(key);
        return (val != null && !val.isBlank()) ? val : defaultValue;
    }

    public static Map<String, String> getAll() {
        if (!loaded) load();
        return new HashMap<>(envValues);
    }

    public static String getLoadedFilePath() {
        return loadedFilePath;
    }
}
