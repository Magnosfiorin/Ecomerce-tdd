package br.com.ecommerce.service;

import br.com.ecommerce.config.DatabaseConfig;
import br.com.ecommerce.model.Loja;
import br.com.ecommerce.model.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço responsável pelas regras de negócio de Usuários, Autenticação e Lojas,
 * persistindo e manipulando endereços detalhados (CEP, logradouro, número, bairro, cidade, estado, país).
 */
public class UsuarioService {

    private final DatabaseConfig databaseConfig;

    // Repositório em memória para fallback/testes
    private final List<Usuario> mockUsuarios = new ArrayList<>();
    private final List<Loja> mockLojas = new ArrayList<>();
    private int nextUsuarioId = 1;
    private int nextLojaId = 1;

    public UsuarioService(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    /**
     * Autentica um usuário por e-mail e senha.
     * Requisitos de validação:
     * - E-mail e senha são obrigatórios.
     * - O e-mail deve possuir formato válido (RFC / Regex).
     * - A senha deve possuir no mínimo 4 caracteres.
     */
    public Usuario autenticar(String email, String senha) {
        if (email == null || email.isBlank() || senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("E-mail e senha são obrigatórios.");
        }

        if (!Usuario.isEmailValido(email)) {
            throw new IllegalArgumentException("E-mail em formato inválido. Informe um formato válido (exemplo@dominio.com).");
        }

        if (senha.length() < 4) {
            throw new IllegalArgumentException("A senha deve conter no mínimo 4 caracteres.");
        }

        if (databaseConfig != null && databaseConfig.testConnection()) {
            String sql = "SELECT id, nome, sobrenome, email, senha, cpf, cep, endereco, numero, bairro, cidade, estado, pais, tipo_usuario " +
                         "FROM usuarios WHERE LOWER(email) = LOWER(?) AND senha = ?";
            try (Connection conn = databaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email.trim());
                stmt.setString(2, senha.trim());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapearUsuario(rs);
                    }
                }
            } catch (SQLException e) {
                System.err.println("[UsuarioService] Erro ao consultar banco: " + e.getMessage());
            }
        }

        // Fallback em memória
        return mockUsuarios.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email.trim()) && u.getSenha().equals(senha.trim()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Cadastra um novo cliente (tipo 1).
     */
    public Usuario cadastrarCliente(Usuario usuario) {
        validarDadosUsuario(usuario);
        usuario.setTipoUsuario(Usuario.TIPO_CLIENTE);

        if (databaseConfig != null && databaseConfig.testConnection()) {
            String sql = "INSERT INTO usuarios (nome, sobrenome, email, senha, cpf, cep, endereco, numero, bairro, cidade, estado, pais, tipo_usuario) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
            try (Connection conn = databaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                preencherParametrosUsuario(stmt, usuario);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        usuario.setId(rs.getInt("id"));
                        return usuario;
                    }
                }
            } catch (SQLException e) {
                if (e.getMessage().contains("duplicate key") || e.getMessage().contains("unique")) {
                    throw new IllegalArgumentException("Já existe um usuário cadastrado com este e-mail.");
                }
                throw new RuntimeException("Erro ao cadastrar cliente no banco: " + e.getMessage(), e);
            }
        }

        // Fallback em memória
        verificarEmailDuplicadoMock(usuario.getEmail());
        usuario.setId(nextUsuarioId++);
        mockUsuarios.add(usuario);
        return usuario;
    }

    /**
     * Cadastra um vendedor (tipo 0) e sua respectiva loja vinculada.
     */
    public Usuario cadastrarVendedorComLoja(Usuario usuario, Loja loja) {
        validarDadosUsuario(usuario);
        validarDadosLoja(loja);
        usuario.setTipoUsuario(Usuario.TIPO_VENDEDOR);

        if (databaseConfig != null && databaseConfig.testConnection()) {
            String sqlUsuario = "INSERT INTO usuarios (nome, sobrenome, email, senha, cpf, cep, endereco, numero, bairro, cidade, estado, pais, tipo_usuario) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
            String sqlLoja = "INSERT INTO lojas (usuario_id, nome_loja, email_loja, cnpj, cep_loja, endereco_loja, numero_loja, bairro_loja, cidade_loja, estado_loja, pais_loja) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

            try (Connection conn = databaseConfig.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    // 1. Insere o Usuário
                    int usuarioId;
                    try (PreparedStatement stmtU = conn.prepareStatement(sqlUsuario)) {
                        preencherParametrosUsuario(stmtU, usuario);
                        try (ResultSet rs = stmtU.executeQuery()) {
                            if (rs.next()) {
                                usuarioId = rs.getInt("id");
                                usuario.setId(usuarioId);
                            } else {
                                throw new SQLException("Falha ao obter ID do usuário gerado.");
                            }
                        }
                    }

                    // 2. Insere a Loja vinculada
                    try (PreparedStatement stmtL = conn.prepareStatement(sqlLoja)) {
                        preencherParametrosLoja(stmtL, usuarioId, loja);
                        try (ResultSet rsL = stmtL.executeQuery()) {
                            if (rsL.next()) {
                                loja.setId(rsL.getInt("id"));
                                loja.setUsuarioId(usuarioId);
                            }
                        }
                    }

                    conn.commit();
                    return usuario;
                } catch (SQLException ex) {
                    conn.rollback();
                    if (ex.getMessage().contains("duplicate key") || ex.getMessage().contains("unique")) {
                        throw new IllegalArgumentException("Já existe um usuário cadastrado com este e-mail.");
                    }
                    throw ex;
                }
            } catch (SQLException e) {
                throw new RuntimeException("Erro ao cadastrar vendedor e loja no banco: " + e.getMessage(), e);
            }
        }

        // Fallback em memória
        verificarEmailDuplicadoMock(usuario.getEmail());
        usuario.setId(nextUsuarioId++);
        mockUsuarios.add(usuario);

        loja.setId(nextLojaId++);
        loja.setUsuarioId(usuario.getId());
        mockLojas.add(loja);

        return usuario;
    }

    /**
     * Permite a um cliente logado criar sua loja, sendo promovido a Vendedor/Cliente (tipo 2).
     */
    public Loja criarLojaParaCliente(int usuarioId, Loja loja) {
        validarDadosLoja(loja);

        if (databaseConfig != null && databaseConfig.testConnection()) {
            String sqlLoja = "INSERT INTO lojas (usuario_id, nome_loja, email_loja, cnpj, cep_loja, endereco_loja, numero_loja, bairro_loja, cidade_loja, estado_loja, pais_loja) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
            String sqlUpdateTipo = "UPDATE usuarios SET tipo_usuario = ? WHERE id = ?";

            try (Connection conn = databaseConfig.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    // 1. Cria a loja
                    try (PreparedStatement stmtL = conn.prepareStatement(sqlLoja)) {
                        preencherParametrosLoja(stmtL, usuarioId, loja);
                        try (ResultSet rs = stmtL.executeQuery()) {
                            if (rs.next()) {
                                loja.setId(rs.getInt("id"));
                                loja.setUsuarioId(usuarioId);
                            }
                        }
                    }

                    // 2. Promove o usuário para tipo 2 (Vendedor/Cliente)
                    try (PreparedStatement stmtU = conn.prepareStatement(sqlUpdateTipo)) {
                        stmtU.setInt(1, Usuario.TIPO_VENDEDOR_CLIENTE);
                        stmtU.setInt(2, usuarioId);
                        stmtU.executeUpdate();
                    }

                    conn.commit();
                    return loja;
                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                }
            } catch (SQLException e) {
                throw new RuntimeException("Erro ao criar loja no banco: " + e.getMessage(), e);
            }
        }

        // Fallback em memória
        Usuario u = mockUsuarios.stream().filter(usr -> usr.getId() != null && usr.getId() == usuarioId).findFirst().orElse(null);
        if (u != null) {
            u.setTipoUsuario(Usuario.TIPO_VENDEDOR_CLIENTE);
        }
        loja.setId(nextLojaId++);
        loja.setUsuarioId(usuarioId);
        mockLojas.add(loja);
        return loja;
    }

    /**
     * Busca a loja associada a um usuário.
     */
    public Loja buscarLojaPorUsuario(int usuarioId) {
        if (databaseConfig != null && databaseConfig.testConnection()) {
            String sql = "SELECT id, usuario_id, nome_loja, email_loja, cnpj, cep_loja, endereco_loja, numero_loja, bairro_loja, cidade_loja, estado_loja, pais_loja " +
                         "FROM lojas WHERE usuario_id = ?";
            try (Connection conn = databaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, usuarioId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapearLoja(rs);
                    }
                }
            } catch (SQLException e) {
                System.err.println("[UsuarioService] Erro ao buscar loja: " + e.getMessage());
            }
        }

        return mockLojas.stream().filter(l -> l.getUsuarioId() != null && l.getUsuarioId() == usuarioId).findFirst().orElse(null);
    }

    private void preencherParametrosUsuario(PreparedStatement stmt, Usuario usuario) throws SQLException {
        stmt.setString(1, usuario.getNome());
        stmt.setString(2, usuario.getSobrenome() != null ? usuario.getSobrenome() : "");
        stmt.setString(3, usuario.getEmail().trim().toLowerCase());
        stmt.setString(4, usuario.getSenha());
        stmt.setString(5, usuario.getCpf() != null ? usuario.getCpf() : "");
        stmt.setString(6, usuario.getCep() != null ? usuario.getCep() : "");
        stmt.setString(7, usuario.getEndereco() != null ? usuario.getEndereco() : "");
        stmt.setString(8, usuario.getNumero() != null ? usuario.getNumero() : "");
        stmt.setString(9, usuario.getBairro() != null ? usuario.getBairro() : "");
        stmt.setString(10, usuario.getCidade() != null ? usuario.getCidade() : "");
        stmt.setString(11, usuario.getEstado() != null ? usuario.getEstado() : "");
        stmt.setString(12, usuario.getPais() != null ? usuario.getPais() : "Brasil");
        stmt.setInt(13, usuario.getTipoUsuario());
    }

    private void preencherParametrosLoja(PreparedStatement stmt, int usuarioId, Loja loja) throws SQLException {
        stmt.setInt(1, usuarioId);
        stmt.setString(2, loja.getNomeLoja());
        stmt.setString(3, loja.getEmailLoja());
        stmt.setString(4, loja.getCnpj());
        stmt.setString(5, loja.getCepLoja() != null ? loja.getCepLoja() : "");
        stmt.setString(6, loja.getEnderecoLoja() != null ? loja.getEnderecoLoja() : "");
        stmt.setString(7, loja.getNumeroLoja() != null ? loja.getNumeroLoja() : "");
        stmt.setString(8, loja.getBairroLoja() != null ? loja.getBairroLoja() : "");
        stmt.setString(9, loja.getCidadeLoja() != null ? loja.getCidadeLoja() : "");
        stmt.setString(10, loja.getEstadoLoja() != null ? loja.getEstadoLoja() : "");
        stmt.setString(11, loja.getPaisLoja() != null ? loja.getPaisLoja() : "Brasil");
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("sobrenome"),
                rs.getString("email"),
                rs.getString("senha"),
                rs.getString("cpf"),
                rs.getString("cep"),
                rs.getString("endereco"),
                rs.getString("numero"),
                rs.getString("bairro"),
                rs.getString("cidade"),
                rs.getString("estado"),
                rs.getString("pais"),
                rs.getInt("tipo_usuario")
        );
    }

    private Loja mapearLoja(ResultSet rs) throws SQLException {
        return new Loja(
                rs.getInt("id"),
                rs.getInt("usuario_id"),
                rs.getString("nome_loja"),
                rs.getString("email_loja"),
                rs.getString("cnpj"),
                rs.getString("cep_loja"),
                rs.getString("endereco_loja"),
                rs.getString("numero_loja"),
                rs.getString("bairro_loja"),
                rs.getString("cidade_loja"),
                rs.getString("estado_loja"),
                rs.getString("pais_loja")
        );
    }

    private void validarDadosUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Dados de usuário não informados.");
        }
        if (usuario.getNome() == null || usuario.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome é obrigatório.");
        }
        if (!Usuario.isEmailValido(usuario.getEmail())) {
            throw new IllegalArgumentException("E-mail inválido. Informe um formato válido (exemplo@dominio.com).");
        }
        if (usuario.getSenha() == null || usuario.getSenha().length() < 4) {
            throw new IllegalArgumentException("A senha deve conter no mínimo 4 caracteres.");
        }
    }

    private void validarDadosLoja(Loja loja) {
        if (loja == null) {
            throw new IllegalArgumentException("Dados da loja não informados.");
        }
        if (loja.getNomeLoja() == null || loja.getNomeLoja().isBlank()) {
            throw new IllegalArgumentException("O nome da loja é obrigatório.");
        }
        if (!Loja.isEmailValido(loja.getEmailLoja())) {
            throw new IllegalArgumentException("E-mail da loja inválido. Informe um formato válido.");
        }
    }

    private void verificarEmailDuplicadoMock(String email) {
        boolean duplicado = mockUsuarios.stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(email.trim()));
        if (duplicado) {
            throw new IllegalArgumentException("Já existe um usuário cadastrado com este e-mail.");
        }
    }
}
