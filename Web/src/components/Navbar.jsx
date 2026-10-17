import React, { useEffect, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { databaseService } from '../services/databaseService';
import { ShoppingCart, User, Package, Store, CheckCircle2, AlertCircle } from 'lucide-react';

export default function Navbar() {
  const { usuario, logout, isVendedor } = useAuth();
  const { totalItens } = useCart();
  const location = useLocation();
  const [backendStatus, setBackendStatus] = useState({ online: false, database: 'Verificando...' });

  useEffect(() => {
    databaseService.checkConnection().then(setBackendStatus);
    const interval = setInterval(() => {
      databaseService.checkConnection().then(setBackendStatus);
    }, 15000);
    return () => clearInterval(interval);
  }, []);

  const isActive = (path) => location.pathname === path;

  return (
    <header className="navbar">
      <div className="navbar-container">
        <div className="navbar-brand">
          <Link to="/" className="brand-logo">
            <Store className="brand-icon" />
            <span>E-Commerce <small>TDD</small></span>
          </Link>
          <div className={`status-pill ${backendStatus.online ? 'online' : 'offline'}`} title="Status da Conexão com Backend e Banco">
            {backendStatus.online ? <CheckCircle2 size={12} /> : <AlertCircle size={12} />}
            <span>Banco: {backendStatus.database}</span>
          </div>
        </div>

        <nav className="navbar-links">
          <Link to="/" className={`nav-link ${isActive('/') ? 'active' : ''}`}>
            Catálogo
          </Link>

          {isVendedor && (
            <Link to="/cadastro-produto" className={`nav-link ${isActive('/cadastro-produto') ? 'active' : ''}`}>
              <Package size={16} />
              <span>Cadastrar Produto</span>
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
              <span className="user-email">
                <User size={14} /> {usuario.email} ({usuario.perfil})
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
