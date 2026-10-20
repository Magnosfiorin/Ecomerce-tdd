import React, { useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { ArrowLeft, CheckCircle2, LockKeyhole, Loader2, ShieldCheck } from 'lucide-react';
import { api } from '../services/api';

export default function RedefinirSenha() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token') || '';
  const [senha, setSenha] = useState('');
  const [confirmacao, setConfirmacao] = useState('');
  const [enviando, setEnviando] = useState(false);
  const [concluido, setConcluido] = useState(false);
  const [erro, setErro] = useState('');

  const handleSubmit = async (event) => {
    event.preventDefault();
    setErro('');

    if (senha !== confirmacao) {
      setErro('As senhas digitadas não coincidem.');
      return;
    }

    setEnviando(true);
    try {
      await api.redefinirSenha(token, senha);
      setConcluido(true);
    } catch (error) {
      setErro(error.message || 'Não foi possível redefinir sua senha. Solicite um novo link.');
    } finally {
      setEnviando(false);
    }
  };

  const tokenAusente = !token;

  return (
    <div className="page-container auth-page">
      <section className="card auth-card auth-recovery-card">
        <div className="auth-header">
          {concluido ? (
            <CheckCircle2 size={38} className="auth-icon recovery-success-icon" />
          ) : (
            <ShieldCheck size={38} className="auth-icon" />
          )}
          <h2>{concluido ? 'Senha redefinida' : 'Crie uma nova senha'}</h2>
          <p className="auth-subtitle">
            {concluido
              ? 'Sua senha foi atualizada. Agora você já pode entrar na sua conta.'
              : 'Escolha uma senha segura com pelo menos 8 caracteres.'}
          </p>
        </div>

        {erro && <div className="alert alert-error" role="alert">{erro}</div>}

        {!concluido && (tokenAusente ? (
          <div className="alert alert-warning" role="alert">
            Este link está incompleto. Solicite uma nova recuperação de senha.
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="form">
            <div className="form-group">
              <label htmlFor="newPassword">Nova senha</label>
              <div className="input-icon-wrapper">
                <LockKeyhole size={18} aria-hidden="true" />
                <input
                  id="newPassword"
                  type="password"
                  autoComplete="new-password"
                  minLength={8}
                  placeholder="Mínimo de 8 caracteres"
                  value={senha}
                  onChange={(event) => setSenha(event.target.value)}
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="confirmPassword">Confirme a nova senha</label>
              <div className="input-icon-wrapper">
                <LockKeyhole size={18} aria-hidden="true" />
                <input
                  id="confirmPassword"
                  type="password"
                  autoComplete="new-password"
                  minLength={8}
                  placeholder="Repita a nova senha"
                  value={confirmacao}
                  onChange={(event) => setConfirmacao(event.target.value)}
                  required
                />
              </div>
            </div>

            <button type="submit" className="btn btn-primary btn-block" disabled={enviando}>
              {enviando ? <><Loader2 size={18} className="spin" /> Salvando...</> : 'Redefinir senha'}
            </button>
          </form>
        ))}

        <div className="auth-footer-links">
          <Link to="/login" className="btn-link-inline">
            <ArrowLeft size={16} aria-hidden="true" /> Voltar para o login
          </Link>
        </div>
      </section>
    </div>
  );
}
