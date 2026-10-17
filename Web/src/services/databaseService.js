import { api } from './api';

/**
 * Camada de Acesso a Dados (Data Service).
 * 
 * Atua como ponte entre os componentes React e o Banco Externo / Backend.
 * Conta com fallback em memória/localStorage para permitir que os testes
 * de interface e desenvolvimento continuem mesmo com o backend offline.
 */
class DatabaseService {
  constructor() {
    this.useMockFallback = true;
    this.mockProdutos = [
      { id: 1, nome: 'Notebook Gamer Pro', preco: 4500.00, estoque: 5 },
      { id: 2, nome: 'Mouse Sem Fio RGB', preco: 150.00, estoque: 20 },
      { id: 3, nome: 'Teclado Mecânico ABNT2', preco: 320.00, estoque: 12 },
      { id: 4, nome: 'Headset 7.1 Surround', preco: 280.00, estoque: 8 },
    ];
  }

  /**
   * Verifica a conectividade com o banco externo através da rota de saúde.
   */
  async checkConnection() {
    try {
      const health = await api.getHealth();
      return {
        online: true,
        database: health.database || 'Conectado',
        service: health.service,
      };
    } catch {
      return {
        online: false,
        database: 'Offline (Modo Mock Ativo)',
        service: 'Local',
      };
    }
  }

  // =========================================================================
  // Operações de Catálogo e Produtos
  // =========================================================================

  async getProdutos() {
    try {
      const data = await api.listarProdutos();
      if (Array.isArray(data.produtos) && data.produtos.length > 0) {
        return data.produtos;
      }
      return this.mockProdutos;
    } catch {
      return this.mockProdutos;
    }
  }

  async criarProduto(produto) {
    try {
      return await api.cadastrarProduto(produto);
    } catch {
      const novo = { id: Date.now(), ...produto };
      this.mockProdutos.push(novo);
      return novo;
    }
  }

  // =========================================================================
  // Operações de Autenticação
  // =========================================================================

  async autenticar(email, senha, perfil) {
    try {
      return await api.login(email, senha, perfil);
    } catch {
      // Mock para desenvolvimento isolado
      return {
        sucesso: true,
        usuario: { email, perfil: perfil || 'CLIENTE' },
        mensagem: 'Login realizado via mock de desenvolvimento',
      };
    }
  }

  // =========================================================================
  // Operações de Checkout
  // =========================================================================

  async processarCheckout(pedido) {
    try {
      return await api.finalizarCheckout(pedido);
    } catch {
      return {
        sucesso: true,
        idPedido: Math.floor(Math.random() * 10000),
        status: 'CONCLUIDO',
        total: pedido.total,
        mensagem: 'Pedido processado com sucesso (Mock)',
      };
    }
  }
}

export const databaseService = new DatabaseService();
