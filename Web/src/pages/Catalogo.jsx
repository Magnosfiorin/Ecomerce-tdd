import React, { useEffect, useState } from 'react';
import { databaseService } from '../services/databaseService';
import { useCart } from '../context/CartContext';
import { ShoppingCart, Check, RefreshCw, Star, Layers, Sparkles } from 'lucide-react';

export default function Catalogo() {
  const [produtos, setProdutos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [versaoAB, setVersaoAB] = useState('B'); // 'A' ou 'B' para o teste de usabilidade (Dev 3)
  const [adicionadoId, setAdicionadoId] = useState(null);
  const [busca, setBusca] = useState('');
  const { adicionarItem } = useCart();

   const carregarProdutos = async (termo = busca) => {
    setLoading(true);
    const lista = await databaseService.getProdutos(termo);
    setProdutos(lista);
    setLoading(false);
  };

  // Recarrega a lista quando o texto da busca muda (com pequena espera ao digitar)
  useEffect(() => {
    const espera = setTimeout(() => carregarProdutos(busca), 300);
    return () => clearTimeout(espera);
  }, [busca]);

  const handleComprar = (produto) => {
    adicionarItem(produto, 1);
    setAdicionadoId(produto.id);
    setTimeout(() => setAdicionadoId(null), 1500);
  };

  return (
    <div className="page-container">
      {/* Banner de Teste de Usabilidade A/B do Dev 3 */}
      <div className="ab-testing-banner">
        <div className="ab-info">
          <Sparkles size={20} className="ab-icon" />
          <div>
            <strong>Teste de Usabilidade Cruzado (Dev 3 - A/B Testing)</strong>
            <p>Alterne entre os layouts para validação com o grupo parceiro (Métrica SEQ / SUS).</p>
          </div>
        </div>
        <div className="ab-buttons">
          <button
            onClick={() => setVersaoAB('A')}
            className={`btn-ab ${versaoAB === 'A' ? 'active' : ''}`}
          >
            Versão A (Lista Clássica)
          </button>
          <button
            onClick={() => setVersaoAB('B')}
            className={`btn-ab ${versaoAB === 'B' ? 'active' : ''}`}
          >
            Versão B (Cards Modernos)
          </button>
        </div>
      </div>

      <div className="catalogo-header">
        <div>
          <h2>Catálogo de Produtos</h2>
          <p>Explore os itens disponíveis para compra imediata</p>
        </div>
        <button onClick={() => carregarProdutos(busca)} className="btn btn-secondary btn-icon" title="Recarregar do Banco">
          <RefreshCw size={16} /> Atualizar
        </button>
      </div>

    <input
        type="search"
        className="catalogo-busca"
        placeholder="Buscar produto por nome..."
        value={busca}
        onChange={(e) => setBusca(e.target.value)}
        aria-label="Buscar produto por nome"
        data-cy="busca-produto"
      />

      {loading ? (
        <div className="loading-state">Carregando catálogo do banco de dados...</div>
      ) : produtos.length === 0 ? (
        <div className="empty-state">Nenhum produto cadastrado no momento.</div>
      ) : (
        /* Renderização Condicional: Versão A (Lista) vs Versão B (Cards) */
        versaoAB === 'A' ? (
          /* ============================================================ */
          /* VERSÃO A: Lista Clássica e Minimalista                      */
          /* ============================================================ */
          <div className="catalogo-lista-versao-a">
            <table className="tabela-produtos">
              <thead>
                <tr>
                  <th>Produto</th>
                  <th>Estoque</th>
                  <th>Preço</th>
                  <th>Ação</th>
                </tr>
              </thead>
              <tbody>
                {produtos.map((p) => (
                  <tr key={p.id}>
                    <td><strong>{p.nome}</strong></td>
                    <td>{p.estoque} unidades</td>
                    <td>R$ {Number(p.preco).toFixed(2)}</td>
                    <td>
                      <button
                        onClick={() => handleComprar(p)}
                        className="btn btn-primary btn-sm"
                        disabled={p.estoque <= 0}
                      >
                        {adicionadoId === p.id ? 'Adicionado!' : 'Comprar'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          /* ============================================================ */
          /* VERSÃO B: Cards com Foto/Ícone, Preço Destacado e Badges     */
          /* ============================================================ */
          <div className="catalogo-grid-versao-b">
            {produtos.map((p) => (
              <div key={p.id} className="produto-card">
                <div className="produto-card-badge">
                  <Layers size={14} /> {p.estoque > 0 ? `${p.estoque} em estoque` : 'Esgotado'}
                </div>
                <div className="produto-card-body">
                  <h3 className="produto-nome">{p.nome}</h3>
                  <div className="produto-preco-wrapper">
                    <span className="preco-cifrao">R$</span>
                    <span className="preco-valor">{Number(p.preco).toFixed(2)}</span>
                  </div>
                </div>
                <div className="produto-card-footer">
                  <button
                    onClick={() => handleComprar(p)}
                    className={`btn btn-block ${adicionadoId === p.id ? 'btn-success' : 'btn-primary'}`}
                    disabled={p.estoque <= 0}
                  >
                    {adicionadoId === p.id ? (
                      <>
                        <Check size={18} /> Adicionado ao Carrinho
                      </>
                    ) : (
                      <>
                        <ShoppingCart size={18} /> Adicionar ao Carrinho
                      </>
                    )}
                  </button>
                </div>
              </div>
            ))}
          </div>
        )
      )}
    </div>
  );
}
