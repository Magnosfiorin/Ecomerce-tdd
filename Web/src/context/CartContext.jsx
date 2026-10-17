import React, { createContext, useContext, useState, useEffect } from 'react';

const CartContext = createContext();

export function CartProvider({ children }) {
  const [itens, setItens] = useState(() => {
    const salvo = localStorage.getItem('ecommerce_carrinho');
    return salvo ? JSON.parse(salvo) : [];
  });

  useEffect(() => {
    localStorage.setItem('ecommerce_carrinho', JSON.stringify(itens));
  }, [itens]);

  const adicionarItem = (produto, quantidade = 1) => {
    setItens((prev) => {
      const existente = prev.find((item) => item.produto.id === produto.id);
      if (existente) {
        return prev.map((item) =>
          item.produto.id === produto.id
            ? { ...item, quantidade: item.quantidade + quantidade }
            : item
        );
      }
      return [...prev, { produto, quantidade }];
    });
  };

  const removerItem = (produtoId) => {
    setItens((prev) => prev.filter((item) => item.produto.id !== produtoId));
  };

  const limparCarrinho = () => {
    setItens([]);
  };

  const total = itens.reduce((acc, item) => acc + item.produto.preco * item.quantidade, 0);
  const totalItens = itens.reduce((acc, item) => acc + item.quantidade, 0);

  return (
    <CartContext.Provider
      value={{
        itens,
        adicionarItem,
        removerItem,
        limparCarrinho,
        total,
        totalItens,
      }}
    >
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  return useContext(CartContext);
}
