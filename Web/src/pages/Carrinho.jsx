import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { Trash2, ShoppingBag, ArrowRight, ArrowLeft, AlertCircle } from 'lucide-react';

export default function Carrinho() {
  const { itens, removerItem, adicionarItem, total, totalItens } = useCart();
  const [alertaEstoque, setAlertaEstoque] = useState('');
  const navigate = useNavigate();

  const handleAlterarQuantidade = (item, novaQtd) => {
    setAlertaEstoque('');
    if (novaQtd <= 0) {
      removerItem(item.produto.id);
      return;
    }

    // Requisito do Dev 4: Impedir compra acima do estoque disponível
    if (novaQtd > item.produto.estoque) {
      setAlertaEstoque(`Limite atingido: O produto "${item.produto.nome}" possui apenas ${item.produto.estoque} unidades em estoque.`);
      return;
    }

    const diferenca = novaQtd - item.quantidade;
    adicionarItem(item.produto, diferenca);
  };

  if (itens.length === 0) {
    return (
      <div className="page-container">
        <div className="card empty-cart-card">
          <ShoppingBag size={48} className="empty-icon" />
          <h2>Seu carrinho está vazio</h2>
          <p>Explore nosso catálogo e adicione itens para começar suas compras.</p>
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
            <h2>Carrinho de Compras ({totalItens} itens)</h2>

            {alertaEstoque && (
              <div className="alert alert-warning">
                <AlertCircle size={18} />
                <span>{alertaEstoque}</span>
              </div>
            )}

            <div className="itens-lista">
              {itens.map((item) => (
                <div key={item.produto.id} className="item-linha">
                  <div className="item-info">
                    <h4>{item.produto.nome}</h4>
                    <span className="item-preco-unitario">
                      R$ {Number(item.produto.preco).toFixed(2)} cada
                    </span>
                    <span className="item-estoque-info">
                      Estoque: {item.produto.estoque} un.
                    </span>
                  </div>

                  <div className="item-controles">
                    <div className="quantidade-seletor">
                      <button
                        onClick={() => handleAlterarQuantidade(item, item.quantidade - 1)}
                        className="btn-qtd"
                      >
                        -
                      </button>
                      <span className="qtd-display">{item.quantidade}</span>
                      <button
                        onClick={() => handleAlterarQuantidade(item, item.quantidade + 1)}
                        className="btn-qtd"
                        disabled={item.quantidade >= item.produto.estoque}
                      >
                        +
                      </button>
                    </div>

                    <span className="item-subtotal">
                      R$ {(item.produto.preco * item.quantidade).toFixed(2)}
                    </span>

                    <button
                      onClick={() => removerItem(item.produto.id)}
                      className="btn-remover"
                      title="Remover item"
                    >
                      <Trash2 size={18} />
                    </button>
                  </div>
                </div>
              ))}
            </div>

            <div className="carrinho-acoes">
              <Link to="/" className="btn btn-link">
                <ArrowLeft size={16} /> Continuar Comprando
              </Link>
            </div>
          </div>
        </div>

        <div className="carrinho-resumo-col">
          <div className="card resumo-card">
            <h3>Resumo do Pedido</h3>
            <div className="resumo-linha">
              <span>Subtotal</span>
              <span>R$ {total.toFixed(2)}</span>
            </div>
            <div className="resumo-linha">
              <span>Frete</span>
              <span className="frete-gratis">GRÁTIS</span>
            </div>
            <hr />
            <div className="resumo-linha total-linha">
              <strong>Total</strong>
              <strong>R$ {total.toFixed(2)}</strong>
            </div>

            <button
              onClick={() => navigate('/checkout')}
              className="btn btn-primary btn-block btn-lg"
            >
              Prosseguir para o Checkout <ArrowRight size={18} />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
