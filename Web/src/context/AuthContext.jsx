import React, { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(() => {
    const salvo = localStorage.getItem('ecommerce_usuario');
    return salvo ? JSON.parse(salvo) : null;
  });

  useEffect(() => {
    if (usuario) {
      localStorage.setItem('ecommerce_usuario', JSON.stringify(usuario));
    } else {
      localStorage.removeItem('ecommerce_usuario');
    }
  }, [usuario]);

  const login = (email, perfil) => {
    const novoUsuario = { email, perfil: perfil || 'CLIENTE' };
    setUsuario(novoUsuario);
    return novoUsuario;
  };

  const logout = () => {
    setUsuario(null);
  };

  return (
    <AuthContext.Provider value={{ usuario, login, logout, isVendedor: usuario?.perfil === 'VENDEDOR' }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
