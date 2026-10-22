/**
 * Teste baseado em cenário 1 - Adicionar produto ao carrinho (fluxo de sucesso).
 * Documentação: docs/TESTES-DE-CENARIO.md
 *
 * Pré-condições: sistema funcionando e ao menos um produto disponível.
 * O produto é criado pela API apenas para montar a pré-condição;
 * o cenário em si é executado inteiramente pela interface.
 */
describe('Cenário 1 - Adicionar produto ao carrinho', () => {
  const apiUrl = Cypress.expose('apiUrl') || 'http://localhost:8080/api';
  const produto = { nome: `Cenario1 Mouse ${Date.now()}`, preco: 79.9, estoque: 5 };

  before(() => {
    cy.request(`${apiUrl}/health`).its('body.database').should('eq', 'CONECTADO');
    cy.request('POST', `${apiUrl}/produtos`, produto).its('status').should('eq', 201);
  });

  it('mostra no carrinho o produto escolhido, com nome, preço, quantidade 1 e total corretos', () => {
    // Passo 1: acessar a página de produtos
    cy.clearLocalStorage();
    cy.visit('/');
    cy.contains('Catálogo de Produtos').should('be.visible');
    cy.get('[data-cy="busca-produto"]').type(produto.nome);

    // Passos 2 e 3: selecionar o produto e adicionar ao carrinho
    cy.contains('.produto-card', produto.nome).within(() => {
      cy.get('.produto-nome').should('have.text', produto.nome);
      cy.get('.preco-valor').should('have.text', '79.90');
      cy.contains('button', 'Adicionar ao Carrinho').click();
    });

    // Passo 4: abrir o carrinho
    cy.get('a[href="/carrinho"]').click();
    cy.location('pathname').should('eq', '/carrinho');

    // Resultados esperados
    cy.contains('Carrinho de Compras (1 itens)').should('be.visible');
    cy.contains('.item-linha', produto.nome).within(() => {
      cy.get('h4').should('have.text', produto.nome); // produto aparece com o nome correto
      cy.contains('R$ 79.90 cada').should('be.visible'); // preço corresponde
      cy.get('.qtd-display').should('have.text', '1'); // quantidade inicial
      cy.get('.item-subtotal').should('have.text', 'R$ 79.90');
    });
    cy.get('.total-linha').should('contain.text', 'R$ 79.90'); // total = preço do produto
  });
});