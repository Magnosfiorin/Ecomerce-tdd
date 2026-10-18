import React, { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(() => {
    const salvo = localStorage.getItem('ecommerce_usuario');
    return salvo ? JSON.parse(salvo) : null;
  });

  const [loja, setLoja] = useState(() => {
    const salvo = localStorage.getItem('ecommerce_loja');
    return salvo ? JSON.parse(salvo) : null;
  });

  useEffect(() => {
    if (usuario) {
      localStorage.setItem('ecommerce_usuario', JSON.stringify(usuario));
    } else {
      localStorage.removeItem('ecommerce_usuario');
    }
  }, [usuario]);

  useEffect(() => {
    if (loja) {
      localStorage.setItem('ecommerce_loja', JSON.stringify(loja));
    } else {
      localStorage.removeItem('ecommerce_loja');
    }
  }, [loja]);

  const login = (dadosUsuario, dadosLoja = null) => {
    setUsuario(dadosUsuario);
    if (dadosLoja) {
      setLoja(dadosLoja);
    }
    return dadosUsuario;
  };

  const logout = () => {
    setUsuario(null);
    setLoja(null);
  };

  const promoverParaVendedor = (novaLoja) => {
    if (usuario) {
      const atualizado = { ...usuario, tipoUsuario: 2 };
      setUsuario(atualizado);
      setLoja(novaLoja);
    }
  };

  const isVendedor = usuario?.tipoUsuario === 0 || usuario?.tipoUsuario === 2;
  const isCliente = usuario?.tipoUsuario === 1 || usuario?.tipoUsuario === 2;

  const getTipoDescricao = () => {
    if (!usuario) return '';
    switch (usuario.tipoUsuario) {
      case 0:
        return 'Vendedor';
      case 1:
        return 'Cliente';
      case 2:
        return 'Vendedor & Cliente';
      default:
        return 'Cliente';
    }
  };

  return (
    <AuthContext.Provider
      value={{
        usuario,
        loja,
        login,
        logout,
        promoverParaVendedor,
        isVendedor,
        isCliente,
        tipoDescricao: getTipoDescricao(),
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
