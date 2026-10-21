const DEFAULT_API_BASE_URL = import.meta.env.PROD
  ? 'https://ecomerce-tdd-back-steel.vercel.app/api'
  : 'http://localhost:8080/api';
const CONFIGURED_API_BASE_URL = import.meta.env.VITE_API_BASE_URL;
const API_BASE_URL = import.meta.env.PROD && CONFIGURED_API_BASE_URL?.startsWith('http://localhost:')
  ? DEFAULT_API_BASE_URL
  : CONFIGURED_API_BASE_URL || DEFAULT_API_BASE_URL;

async function request(endpoint, options = {}) {
  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  });

  const isJson = response.headers.get('content-type')?.includes('application/json');
  const data = isJson ? await response.json() : await response.text();

  if (!response.ok) {
    const message = typeof data === 'object' && data?.erro
      ? data.erro
      : `Erro HTTP: ${response.status}`;
    throw new Error(message);
  }

  return data;
}

function authorization(token) {
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export const api = {
  async getHealth() {
    return request('/health');
  },

  async login(email, senha) {
    return request('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, senha }),
    });
  },

  async solicitarRecuperacaoSenha(email) {
    return request('/auth/recuperar-senha', {
      method: 'POST',
      body: JSON.stringify({ email }),
    });
  },

  async redefinirSenha(token, senha) {
    return request('/auth/redefinir-senha', {
      method: 'POST',
      body: JSON.stringify({ token, senha }),
    });
  },

  async cadastrarCliente(dadosCliente) {
    return request('/auth/cadastro/cliente', {
      method: 'POST',
      body: JSON.stringify(dadosCliente),
    });
  },

  async cadastrarVendedor(usuario, loja) {
    return request('/auth/cadastro/vendedor', {
      method: 'POST',
      body: JSON.stringify({ usuario, loja }),
    });
  },

  async criarLojaParaCliente(usuarioId, dadosLoja) {
    return request('/lojas', {
      method: 'POST',
      body: JSON.stringify({ usuarioId, ...dadosLoja }),
    });
  },

  async cadastrarProduto(produto, token) {
    return request('/produtos', {
      method: 'POST',
      headers: authorization(token),
      body: JSON.stringify(produto),
    });
  },

  async listarProdutos(query = '') {
    return request(`/produtos${query}`);
  },

  async carregarDashboardLoja(token) {
    return request('/loja/dashboard', {
      headers: authorization(token),
    });
  },

  async excluirProduto(produtoId, token) {
    return request(`/produtos/${encodeURIComponent(produtoId)}`, {
      method: 'DELETE',
      headers: authorization(token),
    });
  },

  async atualizarCarrinho(itens) {
    return request('/carrinho', {
      method: 'POST',
      body: JSON.stringify({ itens }),
    });
  },

  async finalizarCheckout(dadosPedido, token) {
    return request('/checkout', {
      method: 'POST',
      headers: authorization(token),
      body: JSON.stringify(dadosPedido),
    });
  },
};
