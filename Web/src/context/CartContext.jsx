import React, { createContext, useContext, useState, useEffect } from 'react';
import { useAuth } from './AuthContext';

const CartContext = createContext();

function storageKey(usuarioId) {
  return usuarioId ? `ecommerce_carrinho_${usuarioId}` : 'ecommerce_carrinho_guest';
}

function carregarItens(key) {
  const salvo = localStorage.getItem(key);
  return salvo ? JSON.parse(salvo) : [];
}

export function CartProvider({ children }) {
  const { usuario } = useAuth();
  const key = storageKey(usuario?.id);
  const [carrinho, setCarrinho] = useState(() => ({
    key,
    itens: carregarItens(key),
  }));
  const itens = carrinho.key === key ? carrinho.itens : carregarItens(key);

  useEffect(() => {
    if (carrinho.key === key) {
      localStorage.setItem(key, JSON.stringify(carrinho.itens));
    } else {
      setCarrinho({ key, itens });
    }
  }, [carrinho, itens, key]);

  // Retorna { ok, mensagem }. Recusa a adição quando passaria do estoque.
  const adicionarItem = (produto, quantidade = 1) => {
    if (quantidade > 0) {
      const estoque = Number(produto.estoque ?? 0);
      const noCarrinho = itens.find((item) => item.produto.id === produto.id)?.quantidade ?? 0;

      if (estoque <= 0) {
        return { ok: false, mensagem: `O produto "${produto.nome}" está esgotado.` };
      }
      if (noCarrinho + quantidade > estoque) {
        return {
          ok: false,
          mensagem: `Limite atingido: "${produto.nome}" tem apenas ${estoque} unidade(s) em estoque (você já tem ${noCarrinho} no carrinho).`,
        };
      }
    }

    setCarrinho((atual) => {
      const prev = atual.key === key ? atual.itens : carregarItens(key);
      const existente = prev.find((item) => item.produto.id === produto.id);
      if (existente) {
        return {
          key,
          itens: prev.map((item) => item.produto.id === produto.id
            ? { ...item, quantidade: item.quantidade + quantidade }
            : item),
        };
      }
      return { key, itens: [...prev, { produto, quantidade }] };
    });
    return { ok: true };
  };

  const removerItem = (produtoId) => {
    setCarrinho((atual) => ({
      key,
      itens: (atual.key === key ? atual.itens : carregarItens(key))
        .filter((item) => item.produto.id !== produtoId),
    }));
  };

  const limparCarrinho = () => {
    setCarrinho({ key, itens: [] });
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
