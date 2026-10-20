import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { ArrowLeft, CheckCircle2, Loader2, Mail, ShieldCheck } from 'lucide-react';
import { api } from '../services/api';

export default function RecuperarSenha() {
  const [email, setEmail] = useState('');
  const [enviando, setEnviando] = useState(false);
  const [enviado, setEnviado] = useState(false);
  const [erro, setErro] = useState('');

  const handleSubmit = async (event) => {
    event.preventDefault();
    setErro('');
    setEnviando(true);

    try {
      await api.solicitarRecuperacaoSenha(email.trim());
      setEnviado(true);
    } catch (error) {
      setErro(error.message || 'Não foi possível enviar o link. Tente novamente.');
    } finally {
      setEnviando(false);
    }
  };

  return (
    <div className="page-container auth-page">
      <section className="card auth-card auth-recovery-card">
        <div className="auth-header">
          {enviado ? (
            <CheckCircle2 size={38} className="auth-icon recovery-success-icon" />
          ) : (
            <ShieldCheck size={38} className="auth-icon" />
          )}
          <h2>{enviado ? 'Confira seu e-mail' : 'Recuperar senha'}</h2>
          <p className="auth-subtitle">
            {enviado
              ? 'Se o endereço informado estiver cadastrado, você receberá um link para redefinir sua senha.'
              : 'Informe o e-mail da sua conta e enviaremos um link seguro para criar uma nova senha.'}
          </p>
        </div>

        {erro && <div className="alert alert-error" role="alert">{erro}</div>}

        {!enviado && (
          <form onSubmit={handleSubmit} className="form">
            <div className="form-group">
              <label htmlFor="recoveryEmail">E-mail</label>
              <div className="input-icon-wrapper">
                <Mail size={18} aria-hidden="true" />
                <input
                  id="recoveryEmail"
                  type="email"
                  autoComplete="email"
                  placeholder="seu.email@exemplo.com"
                  value={email}
                  onChange={(event) => setEmail(event.target.value)}
                  required
                />
              </div>
            </div>

            <button type="submit" className="btn btn-primary btn-block" disabled={enviando}>
              {enviando ? <><Loader2 size={18} className="spin" /> Enviando...</> : 'Enviar link de recuperação'}
            </button>
          </form>
        )}

        <div className="auth-footer-links">
          <Link to="/login" className="btn-link-inline">
            <ArrowLeft size={16} aria-hidden="true" /> Voltar para o login
          </Link>
        </div>
      </section>
    </div>
  );
}
