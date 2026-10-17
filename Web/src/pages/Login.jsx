import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { databaseService } from '../services/databaseService';
import { Lock, Mail, ShieldCheck } from 'lucide-react';

export default function Login() {
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [perfil, setPerfil] = useState('CLIENTE');
  const [erro, setErro] = useState('');
  const [sucesso, setSucesso] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErro('');
    setSucesso('');

    // Validação básica de campos
    if (!email || !senha) {
      setErro('Preencha todos os campos obrigatórios.');
      return;
    }

    if (senha.length < 4) {
      setErro('A senha deve conter no mínimo 4 caracteres (Requisito Dev 1).');
      return;
    }

    try {
      await databaseService.autenticar(email, senha, perfil);
      login(email, perfil);
      setSucesso(`Login realizado com sucesso como ${perfil}!`);
      setTimeout(() => {
        if (perfil === 'VENDEDOR') {
          navigate('/cadastro-produto');
        } else {
          navigate('/');
        }
      }, 1000);
    } catch (err) {
      setErro(err.message || 'Falha ao autenticar.');
    }
  };

  return (
    <div className="page-container auth-page">
      <div className="card auth-card">
        <div className="auth-header">
          <ShieldCheck size={36} className="auth-icon" />
          <h2>Acesso ao Sistema</h2>
          <p>Módulo de Autenticação (Dev 1)</p>
        </div>

        {erro && <div className="alert alert-error">{erro}</div>}
        {sucesso && <div className="alert alert-success">{sucesso}</div>}

        <form onSubmit={handleSubmit} className="form">
          <div className="form-group">
            <label htmlFor="email">E-mail</label>
            <div className="input-icon-wrapper">
              <Mail size={18} />
              <input
                id="email"
                type="email"
                placeholder="seu.email@exemplo.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="senha">Senha</label>
            <div className="input-icon-wrapper">
              <Lock size={18} />
              <input
                id="senha"
                type="password"
                placeholder="Mínimo 4 caracteres"
                value={senha}
                onChange={(e) => setSenha(e.target.value)}
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="perfil">Tipo de Perfil</label>
            <select
              id="perfil"
              value={perfil}
              onChange={(e) => setPerfil(e.target.value)}
              className="select-input"
            >
              <option value="CLIENTE">Cliente (Visualizar e Comprar)</option>
              <option value="VENDEDOR">Vendedor (Cadastrar Produtos)</option>
            </select>
          </div>

          <button type="submit" className="btn btn-primary btn-block">
            Entrar no E-Commerce
          </button>
        </form>
      </div>
    </div>
  );
}
