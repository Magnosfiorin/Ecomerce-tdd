/**
 * Centralizador de Acesso à API Backend e Banco de Dados PostgreSQL.
 * 
 * Gerencia as chamadas HTTP para o backend Java (porta 8080).
 */

const DEFAULT_API_BASE_URL = import.meta.env.PROD
  ? 'https://ecomerce-tdd-back-steel.vercel.app/api'
  : 'http://localhost:8080/api';
const CONFIGURED_API_BASE_URL = import.meta.env.VITE_API_BASE_URL;
const API_BASE_URL = import.meta.env.PROD && CONFIGURED_API_BASE_URL?.startsWith('http://localhost:')
  ? DEFAULT_API_BASE_URL
  : CONFIGURED_API_BASE_URL || DEFAULT_API_BASE_URL;

async function request(endpoint, options = {}) {
  const url = `${API_BASE_URL}${endpoint}`;
  const defaultHeaders = {
    'Content-Type': 'application/json',
  };

  try {
    const response = await fetch(url, {
      ...options,
      headers: {
        ...defaultHeaders,
        ...options.headers,
      },
    });

    const isJson = response.headers.get('content-type')?.includes('application/json');
    const data = isJson ? await response.json() : await response.text();

    if (!response.ok) {
      const mensagemErro = (typeof data === 'object' && data?.erro) ? data.erro : `Erro HTTP: ${response.status}`;
      throw new Error(mensagemErro);
    }

    return data;
  } catch (error) {
    console.warn(`[API] Falha no endpoint ${endpoint}:`, error.message);
    throw error;
  }
}

export const api = {
  async getHealth() {
    return request('/health');
  },

  // =========================================================================
  // Módulo de Autenticação e Usuários (Dev 1)
  // =========================================================================

  async login(email, senha) {
    return request('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, senha }),
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

  // =========================================================================
  // Módulo de Produtos e Catálogo (Dev 2 e Dev 3)
  // =========================================================================

  async cadastrarProduto(produto) {
    return request('/produtos', {
      method: 'POST',
      body: JSON.stringify(produto),
    });
  },

   async listarProdutos(query = '') {
    return request(`/produtos${query}`, {
      method: 'GET',
    });
  },

  // =========================================================================
  // Módulo Carrinho e Checkout (Dev 4 e Dev 5)
  // =========================================================================

  async atualizarCarrinho(itens) {
    return request('/carrinho', {
      method: 'POST',
      body: JSON.stringify({ itens }),
    });
  },

  async finalizarCheckout(dadosPedido) {
    return request('/checkout', {
      method: 'POST',
      body: JSON.stringify(dadosPedido),
    });
  },
};
