package br.com.ecommerce.model;

import java.util.regex.Pattern;

/**
 * Entidade de Loja do sistema com endereço detalhado.
 */
public class Loja {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private Integer id;
    private Integer usuarioId;
    private String nomeLoja;
    private String emailLoja;
    private String cnpj; // Opcional
    private String cepLoja;
    private String enderecoLoja; // Logradouro / Rua
    private String numeroLoja;
    private String bairroLoja;
    private String cidadeLoja;
    private String estadoLoja;
    private String paisLoja;

    public Loja() {
        this.paisLoja = "Brasil";
    }

    public Loja(Integer usuarioId, String nomeLoja, String emailLoja, String cnpj,
                String cepLoja, String enderecoLoja, String numeroLoja, String bairroLoja,
                String cidadeLoja, String estadoLoja, String paisLoja) {
        this.usuarioId = usuarioId;
        this.nomeLoja = nomeLoja;
        this.emailLoja = emailLoja;
        this.cnpj = cnpj;
        this.cepLoja = cepLoja;
        this.enderecoLoja = enderecoLoja;
        this.numeroLoja = numeroLoja;
        this.bairroLoja = bairroLoja;
        this.cidadeLoja = cidadeLoja;
        this.estadoLoja = estadoLoja;
        this.paisLoja = (paisLoja != null && !paisLoja.isBlank()) ? paisLoja : "Brasil";
    }

    public Loja(Integer id, Integer usuarioId, String nomeLoja, String emailLoja, String cnpj,
                String cepLoja, String enderecoLoja, String numeroLoja, String bairroLoja,
                String cidadeLoja, String estadoLoja, String paisLoja) {
        this(usuarioId, nomeLoja, emailLoja, cnpj, cepLoja, enderecoLoja, numeroLoja, bairroLoja, cidadeLoja, estadoLoja, paisLoja);
        this.id = id;
    }

    /**
     * Construtor simplificado para compatibilidade
     */
    public Loja(Integer usuarioId, String nomeLoja, String emailLoja, String enderecoLoja, String cnpj) {
        this(usuarioId, nomeLoja, emailLoja, cnpj, "", enderecoLoja, "", "", "", "", "Brasil");
    }

    public static boolean isEmailValido(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getNomeLoja() {
        return nomeLoja;
    }

    public void setNomeLoja(String nomeLoja) {
        this.nomeLoja = nomeLoja;
    }

    public String getEmailLoja() {
        return emailLoja;
    }

    public void setEmailLoja(String emailLoja) {
        this.emailLoja = emailLoja;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getCepLoja() {
        return cepLoja;
    }

    public void setCepLoja(String cepLoja) {
        this.cepLoja = cepLoja;
    }

    public String getEnderecoLoja() {
        return enderecoLoja;
    }

    public void setEnderecoLoja(String enderecoLoja) {
        this.enderecoLoja = enderecoLoja;
    }

    public String getNumeroLoja() {
        return numeroLoja;
    }

    public void setNumeroLoja(String numeroLoja) {
        this.numeroLoja = numeroLoja;
    }

    public String getBairroLoja() {
        return bairroLoja;
    }

    public void setBairroLoja(String bairroLoja) {
        this.bairroLoja = bairroLoja;
    }

    public String getCidadeLoja() {
        return cidadeLoja;
    }

    public void setCidadeLoja(String cidadeLoja) {
        this.cidadeLoja = cidadeLoja;
    }

    public String getEstadoLoja() {
        return estadoLoja;
    }

    public void setEstadoLoja(String estadoLoja) {
        this.estadoLoja = estadoLoja;
    }

    public String getPaisLoja() {
        return paisLoja;
    }

    public void setPaisLoja(String paisLoja) {
        this.paisLoja = paisLoja;
    }
}
