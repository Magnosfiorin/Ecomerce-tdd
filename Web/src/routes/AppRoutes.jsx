import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import Catalogo from '../pages/Catalogo';
import Login from '../pages/Login';
import RecuperarSenha from '../pages/RecuperarSenha';
import RedefinirSenha from '../pages/RedefinirSenha';
import CadastroProduto from '../pages/CadastroProduto';
import Dashboard from '../pages/Dashboard';
import Carrinho from '../pages/Carrinho';
import Checkout from '../pages/Checkout';
import CatalogoB from '../pages/CatalogoB';

/**
 * Centralizador de Rotas do Frontend.
 * Todas as páginas dos 5 módulos estão centralizadas aqui.
 */
export default function AppRoutes() {
  const catalogoInicial = import.meta.env.VITE_CATALOGO_VARIANT === 'catalogo-b'
    ? <CatalogoB />
    : <Catalogo />;

  return (
    <Routes>
      {/* Cada build pode escolher seu catálogo inicial. */}
      <Route path="/" element={catalogoInicial} />
      <Route path="/catalogo-b" element={<CatalogoB />} />

      {/* Dev 1: Módulo de Login e Identificação de Perfil */}
      <Route path="/login" element={<Login />} />
      <Route path="/recuperar-senha" element={<RecuperarSenha />} />
      <Route path="/redefinir-senha" element={<RedefinirSenha />} />

      {/* Painel do Vendedor: Listagem de Produtos da Loja */}
      <Route path="/dashboard" element={<Dashboard />} />

      {/* Dev 2: Módulo de Cadastro de Produtos (Vendedor) */}
      <Route path="/cadastro-produto" element={<CadastroProduto />} />

      {/* Dev 4: Módulo de Carrinho de Compras */}
      <Route path="/carrinho" element={<Carrinho />} />

      {/* Dev 5: Módulo de Checkout e Fechamento de Pedido */}
      <Route path="/checkout" element={<Checkout />} />

      {/* Rota Fallback */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
