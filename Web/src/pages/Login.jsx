import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { databaseService } from '../services/databaseService';
import { buscarEnderecoPorCep } from '../services/cepService';
import {
  Lock,
  Mail,
  User,
  MapPin,
  FileText,
  Store,
  ArrowRight,
  ArrowLeft,
  ShieldCheck,
  Sparkles,
  Search,
  Loader2,
  Globe
} from 'lucide-react';

const EMAIL_REGEX = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

export default function Login() {
  const { usuario, login, promoverParaVendedor } = useAuth();
  const navigate = useNavigate();

  // Modos de exibição da tela:
  // 'LOGIN' | 'CADASTRO_CLIENTE' | 'VENDEDOR_ETAPA_1' | 'VENDEDOR_ETAPA_2' | 'CRIAR_LOJA_CLIENTE_LOGADO'
  const [modo, setModo] = useState(() =>
    usuario?.tipoUsuario === 1 ? 'CRIAR_LOJA_CLIENTE_LOGADO' : 'LOGIN'
  );

  // Estado de Formulário de Login
  const [loginEmail, setLoginEmail] = useState('');
  const [loginSenha, setLoginSenha] = useState('');

  // Estado de Dados Pessoais (Cliente ou Vendedor Etapa 1)
  const [nome, setNome] = useState('');
  const [sobrenome, setSobrenome] = useState('');
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [confirmarSenha, setConfirmarSenha] = useState('');
  const [cpf, setCpf] = useState('');

  // Endereço Detalhado Pessoal
  const [cep, setCep] = useState('');
  const [endereco, setEndereco] = useState('');
  const [numero, setNumero] = useState('');
  const [bairro, setBairro] = useState('');
  const [cidade, setCidade] = useState('');
  const [estado, setEstado] = useState('');
  const [pais, setPais] = useState('Brasil');
  const [buscandoCepPessoal, setBuscandoCepPessoal] = useState(false);
  const [camposBloqueadosPessoal, setCamposBloqueadosPessoal] = useState({
    endereco: false,
    bairro: false,
    cidade: false,
    estado: false
  });

  // Estado de Dados da Loja (Vendedor Etapa 2)
  const [nomeLoja, setNomeLoja] = useState('');
  const [emailLoja, setEmailLoja] = useState('');
  const [cnpj, setCnpj] = useState('');

  // Endereço Detalhado da Loja
  const [cepLoja, setCepLoja] = useState('');
  const [enderecoLoja, setEnderecoLoja] = useState('');
  const [numeroLoja, setNumeroLoja] = useState('');
  const [bairroLoja, setBairroLoja] = useState('');
  const [cidadeLoja, setCidadeLoja] = useState('');
  const [estadoLoja, setEstadoLoja] = useState('');
  const [paisLoja, setPaisLoja] = useState('Brasil');
  const [buscandoCepLoja, setBuscandoCepLoja] = useState(false);
  const [camposBloqueadosLoja, setCamposBloqueadosLoja] = useState({
    endereco: false,
    bairro: false,
    cidade: false,
    estado: false
  });

  // Feedbacks
  const [erro, setErro] = useState('');
  const [sucesso, setSucesso] = useState('');
  const [loading, setLoading] = useState(false);

  const isEmailValido = (valor) => EMAIL_REGEX.test(valor.trim());

  // =========================================================================
  // Busca Automática de CEP via ViaCEP (Gratuita e sem autenticação)
  // Bloqueio dinâmico: bloqueia SOMENTE os campos que vierem preenchidos pelo CEP
  // =========================================================================

  const handleConsultarCepPessoal = async (valorCep) => {
    setCep(valorCep);
    const cepNumeros = valorCep.replace(/\D/g, '');
    if (cepNumeros.length === 8) {
      setBuscandoCepPessoal(true);
      const dados = await buscarEnderecoPorCep(cepNumeros);
      setBuscandoCepPessoal(false);

      if (dados) {
        setEndereco(dados.endereco || '');
        setBairro(dados.bairro || '');
        setCidade(dados.cidade || '');
        setEstado(dados.estado || '');
        setPais(dados.pais || 'Brasil');

        setCamposBloqueadosPessoal({
          endereco: Boolean(dados.endereco && dados.endereco.trim()),
          bairro: Boolean(dados.bairro && dados.bairro.trim()),
          cidade: Boolean(dados.cidade && dados.cidade.trim()),
          estado: Boolean(dados.estado && dados.estado.trim())
        });
      } else {
        setCamposBloqueadosPessoal({
          endereco: false,
          bairro: false,
          cidade: false,
          estado: false
        });
      }
    } else {
      setCamposBloqueadosPessoal({
        endereco: false,
        bairro: false,
        cidade: false,
        estado: false
      });
    }
  };

  const handleConsultarCepLoja = async (valorCep) => {
    setCepLoja(valorCep);
    const cepNumeros = valorCep.replace(/\D/g, '');
    if (cepNumeros.length === 8) {
      setBuscandoCepLoja(true);
      const dados = await buscarEnderecoPorCep(cepNumeros);
      setBuscandoCepLoja(false);

      if (dados) {
        setEnderecoLoja(dados.endereco || '');
        setBairroLoja(dados.bairro || '');
        setCidadeLoja(dados.cidade || '');
        setEstadoLoja(dados.estado || '');
        setPaisLoja(dados.pais || 'Brasil');

        setCamposBloqueadosLoja({
          endereco: Boolean(dados.endereco && dados.endereco.trim()),
          bairro: Boolean(dados.bairro && dados.bairro.trim()),
          cidade: Boolean(dados.cidade && dados.cidade.trim()),
          estado: Boolean(dados.estado && dados.estado.trim())
        });
      } else {
        setCamposBloqueadosLoja({
          endereco: false,
          bairro: false,
          cidade: false,
          estado: false
        });
      }
    } else {
      setCamposBloqueadosLoja({
        endereco: false,
        bairro: false,
        cidade: false,
        estado: false
      });
    }
  };

  // =========================================================================
  // 1. Ação de Login
  // =========================================================================
  const handleLogin = async (e) => {
    e.preventDefault();
    setErro('');
    setSucesso('');

    if (!loginEmail || !loginSenha) {
      setErro('Informe seu e-mail e senha.');
      return;
    }

    if (!isEmailValido(loginEmail)) {
      setErro('Formato de e-mail inválido.');
      return;
    }

    try {
      setLoading(true);
      const resultado = await databaseService.login(loginEmail, loginSenha);
      login(resultado.usuario, resultado.loja, resultado.token);
      setSucesso(`Bem-vindo de volta, ${resultado.usuario.nome}!`);
      setTimeout(() => {
        if (resultado.usuario.tipoUsuario === 0 || resultado.usuario.tipoUsuario === 2) {
          navigate('/dashboard');
        } else {
          navigate('/');
        }
      }, 900);
    } catch (err) {
      setErro(err.message || 'E-mail ou senha incorretos.');
    } finally {
      setLoading(false);
    }
  };

  // =========================================================================
  // 2. Ação de Cadastro de Cliente
  // =========================================================================
  const handleCadastroCliente = async (e) => {
    e.preventDefault();
    setErro('');
    setSucesso('');

    if (!nome.trim() || !sobrenome.trim() || !cpf.trim() || !endereco.trim() || !cidade.trim() || !estado.trim()) {
      setErro('Preencha os campos obrigatórios do formulário.');
      return;
    }

    if (!isEmailValido(email)) {
      setErro('E-mail inválido. Utilize o formato nome@dominio.com');
      return;
    }

    if (senha.length < 4) {
      setErro('A senha deve ter no mínimo 4 caracteres.');
      return;
    }

    if (senha !== confirmarSenha) {
      setErro('As senhas digitadas não coincidem.');
      return;
    }

    try {
      setLoading(true);
      const resultado = await databaseService.cadastrarCliente({
        nome: nome.trim(),
        sobrenome: sobrenome.trim(),
        email: email.trim().toLowerCase(),
        senha,
        cpf: cpf.trim(),
        cep: cep.trim(),
        endereco: endereco.trim(),
        numero: numero.trim(),
        bairro: bairro.trim(),
        cidade: cidade.trim(),
        estado: estado.trim(),
        pais: pais.trim() || 'Brasil',
      });

      login(resultado.usuario, null, resultado.token);
      setSucesso('Conta de cliente criada com sucesso!');
      setTimeout(() => navigate('/'), 1000);
    } catch (err) {
      setErro(err.message || 'Erro ao criar conta de cliente.');
    } finally {
      setLoading(false);
    }
  };

  // =========================================================================
  // 3. Ação de Vendedor - Etapa 1 -> Etapa 2
  // =========================================================================
  const avancarParaEtapa2Vendedor = (e) => {
    e.preventDefault();
    setErro('');

    if (!nome.trim() || !sobrenome.trim() || !cpf.trim() || !endereco.trim() || !cidade.trim() || !estado.trim()) {
      setErro('Preencha todos os dados pessoais e endereço para continuar.');
      return;
    }

    if (!isEmailValido(email)) {
      setErro('E-mail pessoal inválido.');
      return;
    }

    if (senha.length < 4) {
      setErro('A senha deve ter no mínimo 4 caracteres.');
      return;
    }

    if (senha !== confirmarSenha) {
      setErro('As senhas não coincidem.');
      return;
    }

    setModo('VENDEDOR_ETAPA_2');
  };

  // =========================================================================
  // 4. Ação de Vendedor - Concluir Cadastro com Loja (Etapa 2)
  // =========================================================================
  const handleFinalizarCadastroVendedor = async (e) => {
    e.preventDefault();
    setErro('');
    setSucesso('');

    if (!nomeLoja.trim() || !enderecoLoja.trim() || !cidadeLoja.trim() || !estadoLoja.trim()) {
      setErro('Preencha o nome e o endereço completo da sua loja.');
      return;
    }

    if (!isEmailValido(emailLoja)) {
      setErro('E-mail comercial da loja inválido.');
      return;
    }

    try {
      setLoading(true);
      const resultado = await databaseService.cadastrarVendedor(
        {
          nome: nome.trim(),
          sobrenome: sobrenome.trim(),
          email: email.trim().toLowerCase(),
          senha,
          cpf: cpf.trim(),
          cep: cep.trim(),
          endereco: endereco.trim(),
          numero: numero.trim(),
          bairro: bairro.trim(),
          cidade: cidade.trim(),
          estado: estado.trim(),
          pais: pais.trim() || 'Brasil',
        },
        {
          nomeLoja: nomeLoja.trim(),
          emailLoja: emailLoja.trim().toLowerCase(),
          cnpj: cnpj.trim() || null,
          cepLoja: cepLoja.trim(),
          enderecoLoja: enderecoLoja.trim(),
          numeroLoja: numeroLoja.trim(),
          bairroLoja: bairroLoja.trim(),
          cidadeLoja: cidadeLoja.trim(),
          estadoLoja: estadoLoja.trim(),
          paisLoja: paisLoja.trim() || 'Brasil',
        }
      );

      login(resultado.usuario, resultado.loja);
      setSucesso('Conta de Vendedor e Loja criadas com sucesso!');
      setTimeout(() => navigate('/dashboard'), 1000);
    } catch (err) {
      setErro(err.message || 'Erro ao criar conta de vendedor e loja.');
    } finally {
      setLoading(false);
    }
  };

  // =========================================================================
  // 5. Ação de Criar Loja para Cliente Já Logado (Promove para tipo 2)
  // =========================================================================
  const handleCriarLojaClienteLogado = async (e) => {
    e.preventDefault();
    setErro('');
    setSucesso('');

    if (!nomeLoja.trim() || !enderecoLoja.trim() || !cidadeLoja.trim() || !estadoLoja.trim()) {
      setErro('Preencha o nome e o endereço completo da sua loja.');
      return;
    }

    if (!isEmailValido(emailLoja)) {
      setErro('E-mail comercial da loja inválido.');
      return;
    }

    try {
      setLoading(true);
      const resultado = await databaseService.criarLojaParaCliente(usuario.id, {
        nomeLoja: nomeLoja.trim(),
        emailLoja: emailLoja.trim().toLowerCase(),
        cnpj: cnpj.trim() || null,
        cepLoja: cepLoja.trim(),
        enderecoLoja: enderecoLoja.trim(),
        numeroLoja: numeroLoja.trim(),
        bairroLoja: bairroLoja.trim(),
        cidadeLoja: cidadeLoja.trim(),
        estadoLoja: estadoLoja.trim(),
        paisLoja: paisLoja.trim() || 'Brasil',
      });

      promoverParaVendedor(resultado.loja);
      setSucesso('Parabéns! Sua loja foi criada e seu perfil agora é Vendedor e Cliente.');
      setTimeout(() => navigate('/dashboard'), 1200);
    } catch (err) {
      setErro(err.message || 'Erro ao criar loja.');
    } finally {
      setLoading(false);
    }
  };

  const handleCliqueVenhaVender = () => {
    setErro('');
    setSucesso('');
    if (usuario) {
      if (usuario.tipoUsuario === 0 || usuario.tipoUsuario === 2) {
        navigate('/dashboard');
      } else {
        setModo('CRIAR_LOJA_CLIENTE_LOGADO');
      }
    } else {
      setModo('VENDEDOR_ETAPA_1');
    }
  };

  return (
    <div className="page-container auth-page">
      <div className="card auth-card-expandida">
        <div className="auth-header">
          <ShieldCheck size={36} className="auth-icon" />
          <h2>
            {modo === 'LOGIN' && 'Acesso ao Sistema'}
            {modo === 'CADASTRO_CLIENTE' && 'Cadastro de Cliente'}
            {modo === 'VENDEDOR_ETAPA_1' && 'Cadastro de Vendedor (Etapa 1/2)'}
            {modo === 'VENDEDOR_ETAPA_2' && 'Cadastro da sua Loja (Etapa 2/2)'}
            {modo === 'CRIAR_LOJA_CLIENTE_LOGADO' && 'Abrir sua Loja no E-Commerce'}
          </h2>
          <p className="auth-subtitle">
            {modo === 'LOGIN' && 'Entre na sua conta ou crie um novo cadastro'}
            {modo === 'CADASTRO_CLIENTE' && 'Preencha seus dados com preenchimento automático por CEP'}
            {modo === 'VENDEDOR_ETAPA_1' && 'Passo 1: Seus dados pessoais e endereço'}
            {modo === 'VENDEDOR_ETAPA_2' && 'Passo 2: Informações comerciais e endereço da loja'}
            {modo === 'CRIAR_LOJA_CLIENTE_LOGADO' && `Olá ${usuario?.nome}! Registre sua loja e torne-se Vendedor.`}
          </p>
        </div>

        {erro && <div className="alert alert-error">{erro}</div>}
        {sucesso && <div className="alert alert-success">{sucesso}</div>}

        {/* ================================================================ */}
        {/* TELA 1: LOGIN SIMPLES                                            */}
        {/* ================================================================ */}
        {modo === 'LOGIN' && (
          <form onSubmit={handleLogin} className="form">
            <div className="form-group">
              <label htmlFor="loginEmail">E-mail</label>
              <div className="input-icon-wrapper">
                <Mail size={18} />
                <input
                  id="loginEmail"
                  type="email"
                  placeholder="seu.email@exemplo.com"
                  value={loginEmail}
                  onChange={(e) => setLoginEmail(e.target.value)}
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="loginSenha">Senha</label>
              <div className="input-icon-wrapper">
                <Lock size={18} />
                <input
                  id="loginSenha"
                  type="password"
                  placeholder="Sua senha secreta"
                  value={loginSenha}
                  onChange={(e) => setLoginSenha(e.target.value)}
                  required
                />
              </div>
            </div>

            <div className="login-botoes-row">
              <button type="submit" className="btn btn-primary btn-flex-1" disabled={loading}>
                {loading ? 'Entrando...' : 'Entrar'}
              </button>

              <button
                type="button"
                onClick={handleCliqueVenhaVender}
                className="btn btn-destaque-vendedor"
                title="Cadastre-se como vendedor e monte sua loja"
              >
                <Sparkles size={16} /> Venha vender com a gente
              </button>
            </div>

            <div className="auth-footer-links">
              <span>Ainda não tem conta de cliente? </span>
              <button
                type="button"
                onClick={() => { setModo('CADASTRO_CLIENTE'); setErro(''); }}
                className="btn-link-inline"
              >
                Cadastre-se como cliente
              </button>
            </div>
          </form>
        )}

        {/* ================================================================ */}
        {/* TELA 2: CADASTRO DE CLIENTE (TIPO 1) COM ENDEREÇO DETALHADO     */}
        {/* ================================================================ */}
        {modo === 'CADASTRO_CLIENTE' && (
          <form onSubmit={handleCadastroCliente} className="form">
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="cliNome">Nome</label>
                <div className="input-icon-wrapper">
                  <User size={18} />
                  <input
                    id="cliNome"
                    type="text"
                    placeholder="Primeiro nome"
                    value={nome}
                    onChange={(e) => setNome(e.target.value)}
                    required
                  />
                </div>
              </div>
              <div className="form-group">
                <label htmlFor="cliSobrenome">Sobrenome</label>
                <input
                  id="cliSobrenome"
                  type="text"
                  placeholder="Sobrenome"
                  value={sobrenome}
                  onChange={(e) => setSobrenome(e.target.value)}
                  required
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="cliEmail">E-mail (Regex)</label>
                <div className="input-icon-wrapper">
                  <Mail size={18} />
                  <input
                    id="cliEmail"
                    type="email"
                    placeholder="nome@dominio.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                  />
                </div>
              </div>
              <div className="form-group">
                <label htmlFor="cliCpf">CPF</label>
                <div className="input-icon-wrapper">
                  <FileText size={18} />
                  <input
                    id="cliCpf"
                    type="text"
                    placeholder="000.000.000-00"
                    value={cpf}
                    onChange={(e) => setCpf(e.target.value)}
                    required
                  />
                </div>
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="cliSenha">Senha</label>
                <div className="input-icon-wrapper">
                  <Lock size={18} />
                  <input
                    id="cliSenha"
                    type="password"
                    placeholder="Mínimo 4 caracteres"
                    value={senha}
                    onChange={(e) => setSenha(e.target.value)}
                    required
                  />
                </div>
              </div>
              <div className="form-group">
                <label htmlFor="cliConfSenha">Confirmar Senha</label>
                <div className="input-icon-wrapper">
                  <Lock size={18} />
                  <input
                    id="cliConfSenha"
                    type="password"
                    placeholder="Repita a senha"
                    value={confirmarSenha}
                    onChange={(e) => setConfirmarSenha(e.target.value)}
                    required
                  />
                </div>
              </div>
            </div>

            {/* SEÇÃO DE ENDEREÇO DETALHADO COM BUSCA DE CEP */}
            <div className="endereco-section">
              <div className="endereco-section-header">
                <h4>Endereço Residencial</h4>
                <div className="endereco-header-acoes">
                  {(camposBloqueadosPessoal.cidade || camposBloqueadosPessoal.endereco) && (
                    <span className="badge-bloqueado" title="Campos preenchidos automaticamente pelo CEP">
                      <Lock size={12} /> Preenchido via CEP
                    </span>
                  )}
                  <span className="viacep-tag">Integração ViaCEP</span>
                </div>
              </div>

              <div className="form-row cep-row">
                <div className="form-group">
                  <label htmlFor="cliCep">CEP (Preenche automático)</label>
                  <div className="input-icon-wrapper">
                    {buscandoCepPessoal ? <Loader2 size={18} className="spin" /> : <Search size={18} />}
                    <input
                      id="cliCep"
                      type="text"
                      placeholder="00000-000"
                      maxLength={9}
                      value={cep}
                      onChange={(e) => handleConsultarCepPessoal(e.target.value)}
                      required
                    />
                  </div>
                </div>
                <div className="form-group">
                  <label htmlFor="cliNumero">Número (Obrigatório)</label>
                  <input
                    id="cliNumero"
                    type="text"
                    placeholder="Ex: 120"
                    value={numero}
                    onChange={(e) => setNumero(e.target.value)}
                    required
                  />
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="cliEndereco">Endereço (Rua / Logradouro)</label>
                <div className="input-icon-wrapper">
                  <MapPin size={18} />
                  <input
                    id="cliEndereco"
                    type="text"
                    placeholder="Nome da rua ou avenida"
                    value={endereco}
                    onChange={(e) => setEndereco(e.target.value)}
                    readOnly={camposBloqueadosPessoal.endereco}
                    className={camposBloqueadosPessoal.endereco ? 'input-bloqueado' : ''}
                    required
                  />
                </div>
              </div>

              <div className="form-row-3">
                <div className="form-group">
                  <label htmlFor="cliBairro">Bairro</label>
                  <input
                    id="cliBairro"
                    type="text"
                    placeholder="Bairro"
                    value={bairro}
                    onChange={(e) => setBairro(e.target.value)}
                    readOnly={camposBloqueadosPessoal.bairro}
                    className={camposBloqueadosPessoal.bairro ? 'input-bloqueado' : ''}
                    required
                  />
                </div>
                <div className="form-group">
                  <label htmlFor="cliCidade">Cidade</label>
                  <input
                    id="cliCidade"
                    type="text"
                    placeholder="Cidade"
                    value={cidade}
                    onChange={(e) => setCidade(e.target.value)}
                    readOnly={camposBloqueadosPessoal.cidade}
                    className={camposBloqueadosPessoal.cidade ? 'input-bloqueado' : ''}
                    required
                  />
                </div>
                <div className="form-group">
                  <label htmlFor="cliEstado">Estado</label>
                  <input
                    id="cliEstado"
                    type="text"
                    placeholder="UF"
                    value={estado}
                    onChange={(e) => setEstado(e.target.value)}
                    readOnly={camposBloqueadosPessoal.estado}
                    className={camposBloqueadosPessoal.estado ? 'input-bloqueado' : ''}
                    required
                  />
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="cliPais">País</label>
                <div className="input-icon-wrapper">
                  <Globe size={18} />
                  <input
                    id="cliPais"
                    type="text"
                    value={pais}
                    readOnly={true}
                    className="input-bloqueado"
                    required
                  />
                </div>
              </div>
            </div>

            <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
              {loading ? 'Salvando...' : 'Finalizar Cadastro de Cliente'}
            </button>

            <div className="auth-footer-links">
              <button
                type="button"
                onClick={() => { setModo('LOGIN'); setErro(''); }}
                className="btn-link-inline"
              >
                <ArrowLeft size={14} /> Voltar para o Login
              </button>
            </div>
          </form>
        )}

        {/* ================================================================ */}
        {/* TELA 3: CADASTRO DE VENDEDOR - ETAPA 1 (DADOS PESSOAIS)          */}
        {/* ================================================================ */}
        {modo === 'VENDEDOR_ETAPA_1' && (
          <form onSubmit={avancarParaEtapa2Vendedor} className="form">
            <div className="etapas-indicador">
              <span className="etapa ativa">1. Dados Pessoais &amp; Endereço</span>
              <span className="etapa-divisor">&rarr;</span>
              <span className="etapa inativa">2. Dados da Loja</span>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="vendNome">Nome do Responsável</label>
                <div className="input-icon-wrapper">
                  <User size={18} />
                  <input
                    id="vendNome"
                    type="text"
                    placeholder="Seu nome"
                    value={nome}
                    onChange={(e) => setNome(e.target.value)}
                    required
                  />
                </div>
              </div>
              <div className="form-group">
                <label htmlFor="vendSobrenome">Sobrenome</label>
                <input
                  id="vendSobrenome"
                  type="text"
                  placeholder="Sobrenome"
                  value={sobrenome}
                  onChange={(e) => setSobrenome(e.target.value)}
                  required
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="vendEmail">E-mail Pessoal (Regex)</label>
                <div className="input-icon-wrapper">
                  <Mail size={18} />
                  <input
                    id="vendEmail"
                    type="email"
                    placeholder="exemplo@dominio.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                  />
                </div>
              </div>
              <div className="form-group">
                <label htmlFor="vendCpf">CPF do Responsável</label>
                <div className="input-icon-wrapper">
                  <FileText size={18} />
                  <input
                    id="vendCpf"
                    type="text"
                    placeholder="000.000.000-00"
                    value={cpf}
                    onChange={(e) => setCpf(e.target.value)}
                    required
                  />
                </div>
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="vendSenha">Senha</label>
                <div className="input-icon-wrapper">
                  <Lock size={18} />
                  <input
                    id="vendSenha"
                    type="password"
                    placeholder="Mínimo 4 caracteres"
                    value={senha}
                    onChange={(e) => setSenha(e.target.value)}
                    required
                  />
                </div>
              </div>
              <div className="form-group">
                <label htmlFor="vendConfSenha">Confirmar Senha</label>
                <div className="input-icon-wrapper">
                  <Lock size={18} />
                  <input
                    id="vendConfSenha"
                    type="password"
                    placeholder="Repita a senha"
                    value={confirmarSenha}
                    onChange={(e) => setConfirmarSenha(e.target.value)}
                    required
                  />
                </div>
              </div>
            </div>

            {/* ENDEREÇO PESSOAL DO VENDEDOR */}
            <div className="endereco-section">
              <div className="endereco-section-header">
                <h4>Endereço Pessoal</h4>
                <div className="endereco-header-acoes">
                  {(camposBloqueadosPessoal.cidade || camposBloqueadosPessoal.endereco) && (
                    <span className="badge-bloqueado" title="Campos preenchidos automaticamente pelo CEP">
                      <Lock size={12} /> Preenchido via CEP
                    </span>
                  )}
                  <span className="viacep-tag">Integração ViaCEP</span>
                </div>
              </div>

              <div className="form-row cep-row">
                <div className="form-group">
                  <label htmlFor="vendCep">CEP (Busca Automática)</label>
                  <div className="input-icon-wrapper">
                    {buscandoCepPessoal ? <Loader2 size={18} className="spin" /> : <Search size={18} />}
                    <input
                      id="vendCep"
                      type="text"
                      placeholder="00000-000"
                      maxLength={9}
                      value={cep}
                      onChange={(e) => handleConsultarCepPessoal(e.target.value)}
                      required
                    />
                  </div>
                </div>
                <div className="form-group">
                  <label htmlFor="vendNumero">Número (Obrigatório)</label>
                  <input
                    id="vendNumero"
                    type="text"
                    placeholder="Ex: 50"
                    value={numero}
                    onChange={(e) => setNumero(e.target.value)}
                    required
                  />
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="vendEndereco">Endereço (Rua / Logradouro)</label>
                <div className="input-icon-wrapper">
                  <MapPin size={18} />
                  <input
                    id="vendEndereco"
                    type="text"
                    placeholder="Rua ou Avenida"
                    value={endereco}
                    onChange={(e) => setEndereco(e.target.value)}
                    readOnly={camposBloqueadosPessoal.endereco}
                    className={camposBloqueadosPessoal.endereco ? 'input-bloqueado' : ''}
                    required
                  />
                </div>
              </div>

              <div className="form-row-3">
                <div className="form-group">
                  <label htmlFor="vendBairro">Bairro</label>
                  <input
                    id="vendBairro"
                    type="text"
                    placeholder="Bairro"
                    value={bairro}
                    onChange={(e) => setBairro(e.target.value)}
                    readOnly={camposBloqueadosPessoal.bairro}
                    className={camposBloqueadosPessoal.bairro ? 'input-bloqueado' : ''}
                    required
                  />
                </div>
                <div className="form-group">
                  <label htmlFor="vendCidade">Cidade</label>
                  <input
                    id="vendCidade"
                    type="text"
                    placeholder="Cidade"
                    value={cidade}
                    onChange={(e) => setCidade(e.target.value)}
                    readOnly={camposBloqueadosPessoal.cidade}
                    className={camposBloqueadosPessoal.cidade ? 'input-bloqueado' : ''}
                    required
                  />
                </div>
                <div className="form-group">
                  <label htmlFor="vendEstado">Estado</label>
                  <input
                    id="vendEstado"
                    type="text"
                    placeholder="UF"
                    value={estado}
                    onChange={(e) => setEstado(e.target.value)}
                    readOnly={camposBloqueadosPessoal.estado}
                    className={camposBloqueadosPessoal.estado ? 'input-bloqueado' : ''}
                    required
                  />
                </div>
              </div>
            </div>

            <button type="submit" className="btn btn-primary btn-block">
              Avançar para Etapa 2 (Dados da Loja) <ArrowRight size={16} />
            </button>

            <div className="auth-footer-links">
              <button
                type="button"
                onClick={() => { setModo('LOGIN'); setErro(''); }}
                className="btn-link-inline"
              >
                <ArrowLeft size={14} /> Voltar para o Login
              </button>
            </div>
          </form>
        )}

        {/* ================================================================ */}
        {/* TELA 4: CADASTRO DE VENDEDOR - ETAPA 2 (DADOS DA LOJA)           */}
        {/* ================================================================ */}
        {(modo === 'VENDEDOR_ETAPA_2' || modo === 'CRIAR_LOJA_CLIENTE_LOGADO') && (
          <form
            onSubmit={modo === 'CRIAR_LOJA_CLIENTE_LOGADO' ? handleCriarLojaClienteLogado : handleFinalizarCadastroVendedor}
            className="form"
          >
            {modo === 'VENDEDOR_ETAPA_2' && (
              <div className="etapas-indicador">
                <span className="etapa inativa">1. Dados Pessoais</span>
                <span className="etapa-divisor">&rarr;</span>
                <span className="etapa ativa">2. Dados da Loja</span>
              </div>
            )}

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="lojaNome">Nome da Loja</label>
                <div className="input-icon-wrapper">
                  <Store size={18} />
                  <input
                    id="lojaNome"
                    type="text"
                    placeholder="Ex: TechStore Informática"
                    value={nomeLoja}
                    onChange={(e) => setNomeLoja(e.target.value)}
                    required
                  />
                </div>
              </div>
              <div className="form-group">
                <label htmlFor="lojaCnpj">CNPJ (Opcional)</label>
                <div className="input-icon-wrapper">
                  <FileText size={18} />
                  <input
                    id="lojaCnpj"
                    type="text"
                    placeholder="00.000.000/0001-00"
                    value={cnpj}
                    onChange={(e) => setCnpj(e.target.value)}
                  />
                </div>
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="lojaEmail">E-mail Comercial da Loja (Validação Regex)</label>
              <div className="input-icon-wrapper">
                <Mail size={18} />
                <input
                  id="lojaEmail"
                  type="email"
                  placeholder="contato@minhaloja.com"
                  value={emailLoja}
                  onChange={(e) => setEmailLoja(e.target.value)}
                  required
                />
              </div>
            </div>

            {/* ENDEREÇO DETALHADO DA LOJA COM BUSCA POR CEP */}
            <div className="endereco-section">
              <div className="endereco-section-header">
                <h4>Endereço Comercial da Loja</h4>
                <div className="endereco-header-acoes">
                  {(camposBloqueadosLoja.cidade || camposBloqueadosLoja.endereco) && (
                    <span className="badge-bloqueado" title="Campos preenchidos automaticamente pelo CEP">
                      <Lock size={12} /> Preenchido via CEP
                    </span>
                  )}
                  <span className="viacep-tag">Integração ViaCEP</span>
                </div>
              </div>

              <div className="form-row cep-row">
                <div className="form-group">
                  <label htmlFor="lojaCep">CEP da Loja (Preenche automático)</label>
                  <div className="input-icon-wrapper">
                    {buscandoCepLoja ? <Loader2 size={18} className="spin" /> : <Search size={18} />}
                    <input
                      id="lojaCep"
                      type="text"
                      placeholder="00000-000"
                      maxLength={9}
                      value={cepLoja}
                      onChange={(e) => handleConsultarCepLoja(e.target.value)}
                      required
                    />
                  </div>
                </div>
                <div className="form-group">
                  <label htmlFor="lojaNumero">Número / Sala (Obrigatório)</label>
                  <input
                    id="lojaNumero"
                    type="text"
                    placeholder="Ex: 500, Sala 12"
                    value={numeroLoja}
                    onChange={(e) => setNumeroLoja(e.target.value)}
                    required
                  />
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="lojaEndereco">Endereço Comercial</label>
                <div className="input-icon-wrapper">
                  <MapPin size={18} />
                  <input
                    id="lojaEndereco"
                    type="text"
                    placeholder="Av. Comercial ou Rua"
                    value={enderecoLoja}
                    onChange={(e) => setEnderecoLoja(e.target.value)}
                    readOnly={camposBloqueadosLoja.endereco}
                    className={camposBloqueadosLoja.endereco ? 'input-bloqueado' : ''}
                    required
                  />
                </div>
              </div>

              <div className="form-row-3">
                <div className="form-group">
                  <label htmlFor="lojaBairro">Bairro</label>
                  <input
                    id="lojaBairro"
                    type="text"
                    placeholder="Bairro"
                    value={bairroLoja}
                    onChange={(e) => setBairroLoja(e.target.value)}
                    readOnly={camposBloqueadosLoja.bairro}
                    className={camposBloqueadosLoja.bairro ? 'input-bloqueado' : ''}
                    required
                  />
                </div>
                <div className="form-group">
                  <label htmlFor="lojaCidade">Cidade</label>
                  <input
                    id="lojaCidade"
                    type="text"
                    placeholder="Cidade"
                    value={cidadeLoja}
                    onChange={(e) => setCidadeLoja(e.target.value)}
                    readOnly={camposBloqueadosLoja.cidade}
                    className={camposBloqueadosLoja.cidade ? 'input-bloqueado' : ''}
                    required
                  />
                </div>
                <div className="form-group">
                  <label htmlFor="lojaEstado">Estado</label>
                  <input
                    id="lojaEstado"
                    type="text"
                    placeholder="UF"
                    value={estadoLoja}
                    onChange={(e) => setEstadoLoja(e.target.value)}
                    readOnly={camposBloqueadosLoja.estado}
                    className={camposBloqueadosLoja.estado ? 'input-bloqueado' : ''}
                    required
                  />
                </div>
              </div>
            </div>

            <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
              {loading
                ? 'Criando Loja...'
                : modo === 'CRIAR_LOJA_CLIENTE_LOGADO'
                ? 'Concluir e Virar Vendedor'
                : 'Concluir Cadastro de Vendedor e Loja'}
            </button>

            <div className="auth-footer-links">
              {modo === 'VENDEDOR_ETAPA_2' ? (
                <button
                  type="button"
                  onClick={() => { setModo('VENDEDOR_ETAPA_1'); setErro(''); }}
                  className="btn-link-inline"
                >
                  <ArrowLeft size={14} /> Voltar para Etapa 1
                </button>
              ) : (
                <button
                  type="button"
                  onClick={() => navigate('/')}
                  className="btn-link-inline"
                >
                  <ArrowLeft size={14} /> Voltar ao Início
                </button>
              )}
            </div>
          </form>
        )}
      </div>
    </div>
  );
}
