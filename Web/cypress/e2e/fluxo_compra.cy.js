describe('Teste E2E - Fluxo Completo de Compra (Dev 5)', () => {
  it('Deve navegar pelo catálogo, adicionar item ao carrinho e concluir checkout', () => {
    // 1. Acessa a aplicação na tela de Catálogo
    cy.visit('/');
    cy.contains('Catálogo de Produtos').should('be.visible');

    // 2. Adiciona o primeiro produto ao carrinho
    cy.contains('Adicionar ao Carrinho').first().click();
    cy.contains('Adicionado ao Carrinho').should('be.visible');

    // 3. Acessa a tela de Carrinho
    cy.contains('Carrinho').click();
    cy.url().should('include', '/carrinho');
    cy.contains('Resumo do Pedido').should('be.visible');

    // 4. Prossegue para o Checkout
    cy.contains('Prosseguir para o Checkout').click();
    cy.url().should('include', '/checkout');

    // 5. Preenche os dados de entrega
    cy.get('#nome').clear().type('Cliente Teste E2E');
    cy.get('#endereco').type('Av. Brasil, 1500 - Centro');

    // 6. Confirma o pedido
    cy.get('[data-cy="btn-confirmar-compra"]').click();

    // 7. Validação do resultado final (Confirmação de Sucesso)
    cy.get('[data-cy="sucesso-titulo"]')
      .should('be.visible')
      .and('contain.text', 'Compra Finalizada com Sucesso!');
    cy.contains('CONCLUÍDO').should('be.visible');
  });
});
