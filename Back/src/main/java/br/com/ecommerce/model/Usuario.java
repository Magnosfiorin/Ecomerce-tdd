package br.com.ecommerce.model;

import java.util.regex.Pattern;

/**
 * Entidade de Usuário do sistema.
 * 
 * Tipos de usuário:
 * 0 = Vendedor
 * 1 = Cliente
 * 2 = Vendedor e Cliente (Cliente que posteriormente criou uma loja)
 */
public class Usuario {

    public static final int TIPO_VENDEDOR = 0;
    public static final int TIPO_CLIENTE = 1;
    public static final int TIPO_VENDEDOR_CLIENTE = 2;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private Integer id;
    private String nome;
    private String sobrenome;
    private String email;
    private String senha;
    private String cpf;
    private String cep;
    private String endereco; // Logradouro / Rua
    private String numero;
    private String bairro;
    private String cidade;
    private String estado;
    private String pais;
    private int tipoUsuario;

    public Usuario() {
        this.tipoUsuario = TIPO_CLIENTE;
        this.pais = "Brasil";
    }

    public Usuario(String nome, String sobrenome, String email, String senha, String cpf,
                   String cep, String endereco, String numero, String bairro, String cidade,
                   String estado, String pais, int tipoUsuario) {
        this.nome = nome;
        this.sobrenome = sobrenome;
        this.email = email;
        this.senha = senha;
        this.cpf = cpf;
        this.cep = cep;
        this.endereco = endereco;
        this.numero = numero;
        this.bairro = bairro;
        this.cidade = cidade;
        this.estado = estado;
        this.pais = (pais != null && !pais.isBlank()) ? pais : "Brasil";
        this.tipoUsuario = tipoUsuario;
    }

    public Usuario(Integer id, String nome, String sobrenome, String email, String senha, String cpf,
                   String cep, String endereco, String numero, String bairro, String cidade,
                   String estado, String pais, int tipoUsuario) {
        this(nome, sobrenome, email, senha, cpf, cep, endereco, numero, bairro, cidade, estado, pais, tipoUsuario);
        this.id = id;
    }

    /**
     * Construtor simplificado para compatibilidade
     */
    public Usuario(String nome, String sobrenome, String email, String senha, String endereco, String cpf, int tipoUsuario) {
        this(nome, sobrenome, email, senha, cpf, "", endereco, "", "", "", "", "Brasil", tipoUsuario);
    }

    /**
     * Valida se o formato do e-mail é válido utilizando expressão regular (Regex).
     */
    public static boolean isEmailValido(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public boolean isVendedor() {
        return this.tipoUsuario == TIPO_VENDEDOR || this.tipoUsuario == TIPO_VENDEDOR_CLIENTE;
    }

    public boolean isCliente() {
        return this.tipoUsuario == TIPO_CLIENTE || this.tipoUsuario == TIPO_VENDEDOR_CLIENTE;
    }

    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSobrenome() {
        return sobrenome;
    }

    public void setSobrenome(String sobrenome) {
        this.sobrenome = sobrenome;
    }

    public String getNomeCompleto() {
        return (nome != null ? nome : "") + " " + (sobrenome != null ? sobrenome : "").trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public int getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(int tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }
}
