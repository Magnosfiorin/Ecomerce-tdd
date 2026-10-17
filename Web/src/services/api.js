/**
 * Centralizador de Acesso à API Backend e Banco de Dados Externo.
 * 
 * Este módulo gerencia as chamadas HTTP para o backend Java (que acessa o PostgreSQL),
 * configurado através da variável de ambiente VITE_API_BASE_URL.
 */

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

/**
 * Utilitário genérico para requisições HTTP com tratamento de erro e JSON.
 */
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
      throw new Error(data?.erro || `Erro HTTP: ${response.status}`);
    }

    return data;
  } catch (error) {
    console.warn(`[API] Falha ao comunicar com endpoint ${endpoint}:`, error.message);
    throw error;
  }
}

export const api = {
  /**
   * Verifica a conectividade do Backend e do Banco PostgreSQL Externo.
   */
  async getHealth() {
    return request('/health');
  },

  // =========================================================================
  // Dev 1: Módulo Login & Autenticação
  // =========================================================================
  async login(email, senha, perfil) {
    return request('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, senha, perfil }),
    });
  },

  // =========================================================================
  // Dev 2: Módulo Cadastro de Produtos (Vendedor)
  // =========================================================================
  async cadastrarProduto(produto) {
    return request('/produtos', {
      method: 'POST',
      body: JSON.stringify(produto),
    });
  },

  // =========================================================================
  // Dev 3: Módulo Catálogo (Cliente)
  // =========================================================================
  async listarProdutos() {
    return request('/produtos', {
      method: 'GET',
    });
  },

  // =========================================================================
  // Dev 4: Módulo Carrinho de Compras
  // =========================================================================
  async atualizarCarrinho(itens) {
    return request('/carrinho', {
      method: 'POST',
      body: JSON.stringify({ itens }),
    });
  },

  // =========================================================================
  // Dev 5: Módulo Checkout
  // =========================================================================
  async finalizarCheckout(dadosPedido) {
    return request('/checkout', {
      method: 'POST',
      body: JSON.stringify(dadosPedido),
    });
  },
};
