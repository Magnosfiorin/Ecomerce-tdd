describe('Teste E2E - Fluxo Completo de Compra (Dev 5)', () => {
  it('Deve autenticar, adicionar um produto e concluir checkout com persistência no PostgreSQL', () => {
    const apiUrl = Cypress.env('apiUrl') || 'http://localhost:8080/api';
    const email = `checkout-${Date.now()}@teste.com`;
    const nomeProduto = `Produto E2E ${Date.now()}`;
    const senha = 'senhaE2E123';

    cy.request('POST', `${apiUrl}/auth/cadastro/cliente`, {
      nome: 'Cliente',
      sobrenome: 'E2E',
      email,
      senha,
      cpf: '123.456.789-01',
      endereco: 'Rua E2E',
      cidade: 'São Paulo',
      estado: 'SP',
    }).then((response) => {
      expect(response.status).to.equal(201);
    });

    cy.request('POST', `${apiUrl}/produtos`, {
      nome: nomeProduto,
      preco: 19.9,
      estoque: 3,
    }).then((response) => {
      expect(response.status).to.equal(201);
    });

    cy.clearLocalStorage();
    cy.visit('/login');
    cy.get('#loginEmail').type(email);
    cy.get('#loginSenha').type(senha);
    cy.contains('button', 'Entrar').click();
    cy.contains(`Bem-vindo de volta, Cliente!`).should('be.visible');

    // Acessa a aplicação já autenticado e adiciona o produto criado para este teste.
    cy.visit('/');
    cy.contains('Catálogo de Produtos').should('be.visible');
    cy.contains('.produto-card', nomeProduto)
      .contains('button', 'Adicionar ao Carrinho')
      .click();
    cy.contains('Adicionado ao Carrinho').should('be.visible');

    // Acessa a tela de Carrinho
    cy.contains('Carrinho').click();
    cy.url().should('include', '/carrinho');
    cy.contains('Resumo do Pedido').should('be.visible');

    // 4. Prossegue para o Checkout
    cy.contains('Prosseguir para o Checkout').click();
    cy.url().should('include', '/checkout');

    // Preenche os dados de entrega
    cy.get('#nome').clear().type('Cliente Teste E2E');
    cy.get('#endereco').type('Av. Brasil, 1500 - Centro');

    // Confirma o pedido
    cy.get('[data-cy="btn-confirmar-compra"]').click();

    // Valida a confirmação do pedido persistido
    cy.get('[data-cy="sucesso-titulo"]')
      .should('be.visible')
      .and('contain.text', 'Compra Finalizada com Sucesso!');
    cy.contains('CONCLUÍDO').should('be.visible');
  });
});
