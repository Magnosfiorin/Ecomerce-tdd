/**
 * Teste baseado em cenário 2 - Tentativa de comprar acima do estoque.
 * Documentação: docs/TESTES-DE-CENARIO.md
 *
 * Pré-condições: sistema funcionando, produto com 2 unidades em estoque,
 * cliente autenticado e carrinho vazio. Produto e cliente são criados pela API
 * apenas para montar a pré-condição; o login e o cenário rodam pela interface.
 */
describe('Cenário 2 - Tentativa de comprar acima do estoque', () => {
  const apiUrl = Cypress.expose('apiUrl') || 'http://localhost:8080/api';
  const produto = { nome: `Cenario2 Teclado ${Date.now()}`, preco: 120, estoque: 2 };
  const cliente = {
    email: `cenario2.${Date.now()}@teste.com`,
    senha: 'senhaE2E123',
  };

  before(() => {
    cy.request(`${apiUrl}/health`).its('body.database').should('eq', 'CONECTADO');
    cy.request('POST', `${apiUrl}/produtos`, produto).its('status').should('eq', 201);
    cy.request('POST', `${apiUrl}/auth/cadastro/cliente`, {
      nome: 'Cliente',
      sobrenome: 'Cenario',
      email: cliente.email,
      senha: cliente.senha,
      cpf: '123.456.789-01',
      endereco: 'Rua E2E',
      cidade: 'São Paulo',
      estado: 'SP',
    }).its('status').should('eq', 201);
  });

  beforeEach(() => {
    cy.intercept('POST', '**/api/checkout').as('checkout');

    cy.visit('/login');
    cy.get('#loginEmail').type(cliente.email);
    cy.get('#loginSenha').type(cliente.senha);
    cy.contains('button', 'Entrar').click();
    cy.contains('Bem-vindo de volta, Cliente!').should('be.visible');
  });

  // Abre o catálogo, filtra o produto do teste e clica em "Adicionar" N vezes.
  const adicionarPeloCatalogo = (vezes) => {
    cy.visit('/');
    cy.get('[data-cy="busca-produto"]').type(produto.nome);
    cy.contains('.produto-card', produto.nome).within(() => {
      for (let i = 0; i < vezes; i += 1) {
        cy.get('button').click();
      }
    });
    cy.get('a[href="/carrinho"]').click();
    cy.location('pathname').should('eq', '/carrinho');
  };

  it('parte A - no carrinho, o botão "+" trava quando a quantidade atinge o estoque', () => {
    adicionarPeloCatalogo(1);
    cy.contains('Carrinho de Compras (1 itens)').should('be.visible');
    cy.contains('Estoque: 2 un.').should('be.visible');

    // Aumenta até o limite do estoque (2 unidades)
    cy.contains('.btn-qtd', '+').click();
    cy.get('.qtd-display').should('have.text', '2');
    cy.contains('R$ 240.00').should('be.visible');

    // Tenta aumentar de novo: o botão está desabilitado
    cy.contains('.btn-qtd', '+').should('be.disabled');
    cy.get('.qtd-display').should('have.text', '2');
  });
});