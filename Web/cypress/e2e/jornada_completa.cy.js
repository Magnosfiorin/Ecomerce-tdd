/**
 * E2E - Jornada completa do site, feita 100% pela interface (sem atalhos via API).
 *
 * Atores:
 *  - Vendedor: cadastra loja, faz login, cadastra produto e depois confere as vendas.
 *  - Visitante/Cliente: tenta comprar sem conta, cria conta, compra e vê o produto esgotado.
 *
 * Pré-requisitos: backend na porta 8080 (com PostgreSQL de TESTE) e frontend na 3000.
 * Os testes rodam em ordem e dependem do produto criado no teste 2.
 */
describe('E2E - Jornada completa do e-commerce', () => {
  const apiUrl = Cypress.expose('apiUrl') || 'http://localhost:8080/api';
  const LENTO = { timeout: 10000 }; // cadastro faz hash de senha e grava no banco

  const unico = Date.now();
  const senha = 'senhaE2E123';
  const cpf = '12345678901';
  const cepFalso = '90010000';

  const vendedor = {
    nome: 'Vendedor',
    sobrenome: 'E2E',
    email: `vendedor.${unico}@teste.com`,
    loja: `Loja E2E ${unico}`,
    emailLoja: `loja.${unico}@teste.com`,
  };
  const cliente = {
    nome: 'Cliente',
    sobrenome: 'E2E',
    email: `cliente.${unico}@teste.com`,
  };
  const produto = {
    nome: `Produto E2E ${unico}`,
    preco: '49.90',
    estoque: 2,
  };

  // Garante que o backend e o banco estão de pé (o frontend usa dados mock se a API cair).
  before(() => {
    cy.request(`${apiUrl}/health`).its('body.database').should('eq', 'CONECTADO');
  });

  // Simula o ViaCEP para o teste não depender da internet.
  beforeEach(() => {
    cy.intercept('GET', 'https://viacep.com.br/ws/*/json/', {
      statusCode: 200,
      body: {
        logradouro: 'Rua dos Testes',
        bairro: 'Centro',
        localidade: 'Porto Alegre',
        uf: 'RS',
      },
    }).as('viacep');
  });

  // ---------------------------------------------------------------- ajudantes

  const digitarCep = (seletor) => {
    cy.get(seletor).type(cepFalso);
    cy.wait('@viacep');
  };

  const entrarPelaTela = (email, senhaDigitada = senha) => {
    cy.visit('/login');
    cy.get('#loginEmail').type(email);
    cy.get('#loginSenha').type(senhaDigitada);
    cy.contains('button', 'Entrar').click();
  };

  const abrirCatalogoBuscandoOProduto = () => {
    cy.visit('/');
    cy.get('[data-cy="busca-produto"]').type(produto.nome);
    return cy.contains('.produto-card', produto.nome);
  };

  // -------------------------------------------------------------------- testes

  it('1) recusa o login de um e-mail que não existe', () => {
    entrarPelaTela(`naoexiste.${unico}@teste.com`, 'qualquerSenha1');

    cy.get('.alert-error').should('be.visible');
    cy.location('pathname').should('eq', '/login');
  });

  it('2) vendedor se cadastra com loja, faz login e cadastra um produto', () => {
    // Etapa 1: dados pessoais
    cy.visit('/login');
    cy.contains('button', 'Venha vender com a gente').click();
    cy.contains('Cadastro de Vendedor (Etapa 1/2)').should('be.visible');

    cy.get('#vendNome').type(vendedor.nome);
    cy.get('#vendSobrenome').type(vendedor.sobrenome);
    cy.get('#vendEmail').type(vendedor.email);
    cy.get('#vendCpf').type(cpf);
    cy.get('#vendSenha').type(senha);
    cy.get('#vendConfSenha').type(senha);
    digitarCep('#vendCep');
    cy.get('#vendEndereco').should('have.value', 'Rua dos Testes');
    cy.get('#vendNumero').type('100');
    cy.contains('button', 'Avançar para Etapa 2').click();

    // Etapa 2: dados da loja
    cy.contains('Cadastro da sua Loja (Etapa 2/2)').should('be.visible');
    cy.get('#lojaNome').type(vendedor.loja);
    cy.get('#lojaEmail').type(vendedor.emailLoja);
    digitarCep('#lojaCep');
    cy.get('#lojaEndereco').should('have.value', 'Rua dos Testes');
    cy.get('#lojaNumero').type('200');
    cy.contains('button', 'Concluir Cadastro de Vendedor e Loja').click();

    cy.contains('Conta de Vendedor e Loja criadas com sucesso!', LENTO).should('be.visible');
    cy.location('pathname', LENTO).should('eq', '/dashboard');

    // O cadastro de vendedor não devolve token: sai e entra para ter sessão válida.
    cy.contains('button', 'Sair').click();
    entrarPelaTela(vendedor.email);
    cy.contains(`Bem-vindo de volta, ${vendedor.nome}!`, LENTO).should('be.visible');
    cy.location('pathname', LENTO).should('eq', '/dashboard');
    cy.contains('h2', vendedor.loja).should('be.visible');

    // Cadastro do produto
    cy.contains('a', 'Cadastrar Novo Produto').click();
    cy.location('pathname').should('eq', '/cadastro-produto');
    cy.get('#nome').type(produto.nome);
    cy.get('#preco').type(produto.preco);
    cy.get('#estoque').type(String(produto.estoque));
    cy.contains('button', 'Cadastrar Produto').click();

    cy.contains('cadastrado com sucesso', LENTO).should('be.visible');
    cy.location('pathname', LENTO).should('eq', '/dashboard');
    cy.contains('tr', produto.nome)
      .should('contain', `${produto.estoque} un.`)
      .and('contain', 'Ativo');
  });

  it('3) visitante é barrado no checkout, cria conta de cliente e conclui a compra', () => {
    // --- Visitante tenta comprar sem conta
    abrirCatalogoBuscandoOProduto().within(() => {
      cy.contains('button', 'Adicionar ao Carrinho').click();
    });
    cy.get('a[href="/carrinho"]').click();
    cy.contains('Resumo do Pedido').should('be.visible');
    cy.contains('button', 'Prosseguir para o Checkout').click();
    cy.contains('Entre na sua conta para finalizar a compra').should('be.visible');

    // --- Cadastro de cliente
    cy.contains('a', 'Entrar ou criar conta').click();
    cy.location('pathname').should('eq', '/login');
    cy.contains('button', 'Cadastre-se como cliente').click();
    cy.contains('Cadastro de Cliente').should('be.visible');

    cy.get('#cliNome').type(cliente.nome);
    cy.get('#cliSobrenome').type(cliente.sobrenome);
    cy.get('#cliEmail').type(cliente.email);
    cy.get('#cliCpf').type(cpf);
    cy.get('#cliSenha').type(senha);
    cy.get('#cliConfSenha').type(senha);
    digitarCep('#cliCep');
    cy.get('#cliEndereco').should('have.value', 'Rua dos Testes');
    cy.get('#cliNumero').type('300');
    cy.contains('button', 'Finalizar Cadastro de Cliente').click();

    cy.contains('Conta de cliente criada com sucesso!', LENTO).should('be.visible');
    cy.location('pathname', LENTO).should('eq', '/');

    // --- O carrinho do visitante não migra para a conta: adiciona de novo, já logado
    abrirCatalogoBuscandoOProduto().within(() => {
      cy.contains('button', 'Adicionar ao Carrinho').click();
    });
    cy.get('a[href="/carrinho"]').click();
    cy.contains('Carrinho de Compras (1 itens)').should('be.visible');

    // Aumenta até o limite do estoque (2): o botão "+" deve travar
    cy.contains('.btn-qtd', '+').click();
    cy.contains('Carrinho de Compras (2 itens)').should('be.visible');
    cy.contains('.btn-qtd', '+').should('be.disabled');
    cy.contains('R$ 99.80').should('be.visible');

    // --- Checkout
    cy.contains('button', 'Prosseguir para o Checkout').click();
    cy.location('pathname').should('eq', '/checkout');
    cy.contains(`2x ${produto.nome}`).should('be.visible');
    cy.get('#nome').should('have.value', `${cliente.nome} ${cliente.sobrenome}`);
    cy.get('#endereco').type('Av. Brasil, 1500 - Centro');
    cy.contains('label', 'Cartão de Crédito').click();
    cy.get('[data-cy="btn-confirmar-compra"]').click();

    cy.get('[data-cy="sucesso-titulo"]', LENTO)
      .should('be.visible')
      .and('contain.text', 'Compra Finalizada com Sucesso!');
    cy.contains('CONCLUÍDO').should('be.visible');

    // --- Estoque acabou: produto aparece esgotado e o botão fica desativado
    cy.contains('a', 'Voltar ao Início').click();
    abrirCatalogoBuscandoOProduto()
      .should('have.class', 'produto-card--esgotado')
      .within(() => {
        cy.contains('button', 'Esgotado').should('be.disabled');
      });
  });

  it('4) vendedor faz login e vê no dashboard a venda e o produto esgotado', () => {
    entrarPelaTela(vendedor.email);
    cy.location('pathname', LENTO).should('eq', '/dashboard');

    cy.contains('tr', produto.nome, LENTO).within(() => {
      cy.get('td').eq(3).should('contain', '0 un.'); // estoque
      cy.get('td').eq(4).should('have.text', String(produto.estoque)); // unidades vendidas
      cy.contains('Esgotado').should('be.visible');
    });
  });
});