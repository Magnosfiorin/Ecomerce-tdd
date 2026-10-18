import React, { useEffect, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { databaseService } from '../services/databaseService';
import { ShoppingCart, User, Package, Store, CheckCircle2, AlertCircle, Sparkles } from 'lucide-react';

export default function Navbar() {
  const { usuario, loja, logout, isVendedor, tipoDescricao } = useAuth();
  const { totalItens } = useCart();
  const location = useLocation();
  const [backendStatus, setBackendStatus] = useState({
    online: false,
    databaseConnected: false,
    database: 'Verificando...',
  });

  useEffect(() => {
    databaseService.checkConnection().then(setBackendStatus);
    const interval = setInterval(() => {
      databaseService.checkConnection().then(setBackendStatus);
    }, 15000);
    return () => clearInterval(interval);
  }, []);

  const isActive = (path) => location.pathname === path;
  const bancoConectado = backendStatus.online && backendStatus.databaseConnected;
  const statusClass = bancoConectado ? 'online' : backendStatus.online ? 'degraded' : 'offline';

  return (
    <header className="navbar">
      <div className="navbar-container">
        <div className="navbar-brand">
          <Link to="/" className="brand-logo">
            <Store className="brand-icon" />
            <span>E-Commerce <small>TDD</small></span>
          </Link>
          <div className={`status-pill ${statusClass}`} title="Status da Conexão com Backend e Banco">
            {bancoConectado ? <CheckCircle2 size={12} /> : <AlertCircle size={12} />}
            <span>Banco: {backendStatus.database}</span>
          </div>
        </div>

        <nav className="navbar-links">
          <Link to="/" className={`nav-link ${isActive('/') ? 'active' : ''}`}>
            Catálogo
          </Link>

          {isVendedor && (
            <>
              <Link to="/dashboard" className={`nav-link ${isActive('/dashboard') ? 'active' : ''}`}>
                <Store size={16} />
                <span>Minha Loja {loja?.nomeLoja ? `(${loja.nomeLoja})` : ''}</span>
              </Link>
              <Link to="/cadastro-produto" className={`nav-link ${isActive('/cadastro-produto') ? 'active' : ''}`}>
                <Package size={16} />
                <span>Cadastrar Produto</span>
              </Link>
            </>
          )}

          {/* Atalho para cliente que deseja virar vendedor */}
          {usuario && usuario.tipoUsuario === 1 && (
            <Link to="/login" className="btn-nav-vender" title="Crie sua loja no e-commerce">
              <Sparkles size={14} /> Venha vender com a gente
            </Link>
          )}

          <Link to="/carrinho" className={`nav-link cart-link ${isActive('/carrinho') ? 'active' : ''}`}>
            <ShoppingCart size={18} />
            <span>Carrinho</span>
            {totalItens > 0 && <span className="cart-badge">{totalItens}</span>}
          </Link>

          <Link to="/checkout" className={`nav-link ${isActive('/checkout') ? 'active' : ''}`}>
            Checkout
          </Link>

          {usuario ? (
            <div className="user-profile">
              <span className="user-email" title={`ID: ${usuario.id} | ${usuario.email}`}>
                <User size={14} /> {usuario.nome} ({tipoDescricao})
              </span>
              <button onClick={logout} className="btn-logout">
                Sair
              </button>
            </div>
          ) : (
            <Link to="/login" className={`nav-link btn-login ${isActive('/login') ? 'active' : ''}`}>
              <User size={16} /> Entrar
            </Link>
          )}
        </nav>
      </div>
    </header>
  );
}
