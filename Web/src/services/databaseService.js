import { api } from './api';

class DatabaseService {
  constructor() {
    this.mockProdutos = [
      { id: 1, nome: 'Notebook Gamer Pro', preco: 4500.00, estoque: 5 },
      { id: 2, nome: 'Mouse Sem Fio RGB', preco: 150.00, estoque: 20 },
      { id: 3, nome: 'Teclado Mecânico ABNT2', preco: 320.00, estoque: 12 },
      { id: 4, nome: 'Headset 7.1 Surround', preco: 280.00, estoque: 8 },
    ];
    this.mockUsuarios = [];
  }

  async checkConnection() {
    try {
      const health = await api.getHealth();
      return {
        online: true,
        databaseConnected: health.database === 'CONECTADO',
        database: health.database || 'Status desconhecido',
        service: health.service,
      };
    } catch {
      return {
        online: false,
        databaseConnected: false,
        database: 'API indisponível',
        service: 'Local',
      };
    }
  }

  // =========================================================================
  // Autenticação & Cadastro
  // =========================================================================

  async login(email, senha) {
    try {
      const resp = await api.login(email, senha);
      return resp;
    } catch (error) {
      // Fallback em memória se backend estiver offline
      const u = this.mockUsuarios.find(
        (usr) => usr.email.toLowerCase() === email.toLowerCase() && usr.senha === senha
      );
      if (u) {
        return { sucesso: true, usuario: u };
      }
      throw error;
    }
  }

  async cadastrarCliente(dados) {
    try {
      return await api.cadastrarCliente(dados);
    } catch (error) {
      const novo = { id: Date.now(), ...dados, tipoUsuario: 1 };
      this.mockUsuarios.push(novo);
      return { sucesso: true, usuario: novo, mensagem: 'Cadastrado via mock' };
    }
  }

  async cadastrarVendedor(usuario, loja) {
    try {
      return await api.cadastrarVendedor(usuario, loja);
    } catch (error) {
      const novoUsuario = { id: Date.now(), ...usuario, tipoUsuario: 0 };
      const novaLoja = { id: Date.now() + 1, usuarioId: novoUsuario.id, ...loja };
      this.mockUsuarios.push(novoUsuario);
      return { sucesso: true, usuario: novoUsuario, loja: novaLoja, mensagem: 'Cadastrado via mock' };
    }
  }

  async criarLojaParaCliente(usuarioId, dadosLoja) {
    try {
      return await api.criarLojaParaCliente(usuarioId, dadosLoja);
    } catch (error) {
      const u = this.mockUsuarios.find((usr) => usr.id === usuarioId);
      if (u) {
        u.tipoUsuario = 2;
      }
      return {
        sucesso: true,
        loja: { id: Date.now(), usuarioId, ...dadosLoja },
        novoTipoUsuario: 2,
      };
    }
  }

  // =========================================================================
  // Produtos & Catálogo
  // =========================================================================

  async getProdutos(busca = '') {
    try {
      const query = busca.trim() ? `?busca=${encodeURIComponent(busca.trim())}` : '';
      const data = await api.listarProdutos(query);
      if (Array.isArray(data.produtos)) {
        return data.produtos;
      }
      throw new Error('A API retornou uma resposta de produtos inválida.');
    } catch (error) {
      if (import.meta.env.DEV) {
        return this.filtrarMock(busca);
      }
      throw error;
    }
  }

  filtrarMock(busca = '') {
    const termo = busca.trim().toLowerCase();
    return this.mockProdutos.filter((p) => p.nome.toLowerCase().includes(termo));
  }
  
  async criarProduto(produto) {
    try {
      return await api.cadastrarProduto(produto);
    } catch (error) {
      if (import.meta.env.DEV) {
        const novo = { id: Date.now(), ...produto };
        this.mockProdutos.push(novo);
        return novo;
      }
      throw error;
    }
  }

  // =========================================================================
  // Checkout
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
