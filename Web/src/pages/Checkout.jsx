import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import { databaseService } from '../services/databaseService';
import { CheckCircle, ShieldCheck, ShoppingBag } from 'lucide-react';

export default function Checkout() {
  const { itens, total, limparCarrinho } = useCart();
  const { usuario, token } = useAuth();
  const navigate = useNavigate();

  const [nome, setNome] = useState(
    [usuario?.nome, usuario?.sobrenome].filter(Boolean).join(' ')
  );
  const [endereco, setEndereco] = useState('');
  const [metodoPagamento, setMetodoPagamento] = useState('PIX');
  const [loading, setLoading] = useState(false);
  const [pedidoConcluido, setPedidoConcluido] = useState(null);
  const [erro, setErro] = useState('');

  const handleFinalizarCompra = async (e) => {
    e.preventDefault();
    setErro('');

    if (itens.length === 0) {
      setErro('O carrinho está vazio.');
      return;
    }

    if (!endereco.trim()) {
      setErro('Por favor, informe o endereço de entrega.');
      return;
    }

    try {
      setLoading(true);
      const resultado = await databaseService.processarCheckout({
        itens: itens.map((item) => ({
          produtoId: item.produto.id,
          quantidade: item.quantidade,
        })),
        nome: nome.trim(),
        metodoPagamento,
        endereco,
      }, token);

      if (!resultado?.sucesso || resultado.status !== 'CONCLUÍDO') {
        throw new Error('A API retornou uma confirmação de pedido inválida.');
      }
      setPedidoConcluido(resultado);
      limparCarrinho();
    } catch (err) {
      setErro(err.message || 'Erro ao processar o checkout no banco.');
    } finally {
      setLoading(false);
    }
  };

  if (pedidoConcluido) {
    return (
      <div className="page-container">
        <div className="card checkout-sucesso-card">
          <CheckCircle size={64} className="sucesso-icon" />
          <h2 data-cy="sucesso-titulo">Compra Finalizada com Sucesso!</h2>
          <p className="pedido-numero">
            Código do Pedido: <strong>#{pedidoConcluido.idPedido}</strong>
          </p>
          <p className="pedido-status">
            Status no PostgreSQL: <span className="badge-sucesso">{pedidoConcluido.status}</span>
          </p>
          <p className="pedido-info">
            O estoque dos produtos foi atualizado no banco de dados. Obrigado pela preferência!
          </p>
          <div className="acoes-sucesso">
            <Link to="/" className="btn btn-primary">
              Voltar ao Início
            </Link>
          </div>
        </div>
      </div>
    );
  }

  if (!usuario?.id || !token) {
    return (
      <div className="page-container">
        <div className="card empty-cart-card">
          <h2>Entre na sua conta para finalizar a compra</h2>
          <p>O checkout exige um cliente autenticado para registrar o pedido.</p>
          <Link to="/login" className="btn btn-primary">
            Entrar ou criar conta
          </Link>
        </div>
      </div>
    );
  }

  if (itens.length === 0) {
    return (
      <div className="page-container">
        <div className="card empty-cart-card">
          <ShoppingBag size={48} className="empty-icon" />
          <h2>Nenhum item selecionado para checkout</h2>
          <Link to="/" className="btn btn-primary">
            Ir para o Catálogo
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="page-container">
      <div className="carrinho-layout">
        <div className="carrinho-itens-col">
          <div className="card">
            <h2>Finalizar Pedido</h2>
            <p>Informe os dados de entrega e pagamento para concluir seu pedido.</p>

            {erro && <div className="alert alert-error">{erro}</div>}

            <form onSubmit={handleFinalizarCompra} className="form">
              <div className="form-group">
                <label htmlFor="nome">Nome Completo</label>
                <input
                  id="nome"
                  type="text"
                  placeholder="Seu nome"
                  value={nome}
                  onChange={(e) => setNome(e.target.value)}
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="endereco">Endereço de Entrega</label>
                <input
                  id="endereco"
                  type="text"
                  placeholder="Rua, Número, Bairro, Cidade - CEP"
                  value={endereco}
                  onChange={(e) => setEndereco(e.target.value)}
                  required
                />
              </div>

              <div className="form-group">
                <label>Forma de Pagamento</label>
                <div className="pagamento-opcoes">
                  {['PIX', 'CARTAO', 'BOLETO'].map((metodo) => (
                    <label key={metodo} className={`pagamento-opcao ${metodoPagamento === metodo ? 'selecionada' : ''}`}>
                      <input
                        type="radio"
                        name="pagamento"
                        value={metodo}
                        checked={metodoPagamento === metodo}
                        onChange={(e) => setMetodoPagamento(e.target.value)}
                      />
                      <span>{metodo === 'CARTAO' ? 'Cartão de Crédito' : metodo}</span>
                    </label>
                  ))}
                </div>
              </div>

              <button
                type="submit"
                className="btn btn-primary btn-block btn-lg"
                disabled={loading}
                data-cy="btn-confirmar-compra"
              >
                {loading ? 'Processando com Banco...' : `Confirmar e Pagar R$ ${total.toFixed(2)}`}
              </button>
            </form>
          </div>
        </div>

        <div className="carrinho-resumo-col">
          <div className="card resumo-card">
            <h3>Itens do Pedido</h3>
            <ul className="checkout-lista-itens">
              {itens.map((item) => (
                <li key={item.produto.id}>
                  <span>{item.quantidade}x {item.produto.nome}</span>
                  <span>R$ {(item.produto.preco * item.quantidade).toFixed(2)}</span>
                </li>
              ))}
            </ul>
            <hr />
            <div className="resumo-linha total-linha">
              <strong>Total</strong>
              <strong>R$ {total.toFixed(2)}</strong>
            </div>
            <div className="garantia-box">
              <ShieldCheck size={20} />
              <span>Transação protegida e integrada com backend</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
