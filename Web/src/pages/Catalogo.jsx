import React, { useEffect, useMemo, useState } from 'react';
import { databaseService } from '../services/databaseService';
import { useCart } from '../context/CartContext';
import {
  ShoppingCart,
  Check,
  RefreshCw,
  Layers,
  Search,
  ArrowDownWideNarrow,
  LayoutGrid,
  List
} from 'lucide-react';

const imagensDisponiveis = Object.values(
  import.meta.glob('../../img/*.{png,jpg,jpeg,webp,avif}', {
    eager: true,
    import: 'default',
    query: '?url',
  })
);

export default function Catalogo() {
  const [produtos, setProdutos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [erro, setErro] = useState('');
  const [adicionadoId, setAdicionadoId] = useState(null);
  const [erroEstoque, setErroEstoque] = useState('');
  const [busca, setBusca] = useState('');
  const [ordenacao, setOrdenacao] = useState('nome-asc');
  const [visualizacao, setVisualizacao] = useState('grade');
  const { adicionarItem } = useCart();

  const carregarProdutos = async (termo = busca) => {
    setLoading(true);
    setErro('');
    try {
      const lista = await databaseService.getProdutos(termo);
      setProdutos(lista);
    } catch (error) {
      setProdutos([]);
      setErro(`Não foi possível carregar os produtos da API: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  // Recarrega a lista quando o texto da busca muda (com pequena espera ao digitar)
  useEffect(() => {
    const espera = setTimeout(() => carregarProdutos(busca), 300);
    return () => clearTimeout(espera);
  }, [busca]);

  const produtosOrdenados = useMemo(() => {
    const nomeEmOrdemCrescente = (a, b) =>
      String(a.nome || '').localeCompare(String(b.nome || ''), 'pt-BR', { sensitivity: 'base' });

    return [...produtos].sort((a, b) => {
      switch (ordenacao) {
        case 'preco-asc':
          return Number(a.preco || 0) - Number(b.preco || 0) || nomeEmOrdemCrescente(a, b);
        case 'preco-desc':
          return Number(b.preco || 0) - Number(a.preco || 0) || nomeEmOrdemCrescente(a, b);
        case 'estoque-desc':
          return Number(b.estoque || 0) - Number(a.estoque || 0) || nomeEmOrdemCrescente(a, b);
        default:
          return nomeEmOrdemCrescente(a, b);
      }
    });
  }, [produtos, ordenacao]);

  const imagensPorProduto = useMemo(() => {
    if (imagensDisponiveis.length === 0) return {};

    const imagensEmbaralhadas = [...imagensDisponiveis];
    for (let i = imagensEmbaralhadas.length - 1; i > 0; i -= 1) {
      const indiceAleatorio = Math.floor(Math.random() * (i + 1));
      [imagensEmbaralhadas[i], imagensEmbaralhadas[indiceAleatorio]] =
        [imagensEmbaralhadas[indiceAleatorio], imagensEmbaralhadas[i]];
    }

    return Object.fromEntries(
      produtos.map((produto, indice) => [
        produto.id,
        imagensEmbaralhadas[indice % imagensEmbaralhadas.length],
      ])
    );
  }, [produtos]);

  const handleComprar = (produto) => {
    const resultado = adicionarItem(produto, 1);
    if (!resultado.ok) {
      setErroEstoque(resultado.mensagem);
      return;
    }
    setErroEstoque('');
    setAdicionadoId(produto.id);
    setTimeout(() => setAdicionadoId(null), 1500);
  };

  return (
    <div className="page-container">
      <div className="catalogo-header">
        <div>
          <h2>Catálogo de Produtos</h2>
          <p>Explore os itens disponíveis para compra imediata</p>
        </div>
        <button onClick={() => carregarProdutos(busca)} className="btn btn-secondary btn-icon" title="Recarregar do Banco">
          <RefreshCw size={16} /> Atualizar
        </button>
      </div>

      <div className="catalogo-toolbar">
        <div className="catalogo-busca-wrapper">
          <Search size={18} aria-hidden="true" />
          <input
            type="search"
            className="catalogo-busca"
            placeholder="Buscar produto por nome..."
            value={busca}
            onChange={(e) => setBusca(e.target.value)}
            aria-label="Buscar produto por nome"
            data-cy="busca-produto"
          />
        </div>

        <div className="catalogo-controles">
          <label className="catalogo-ordenacao">
            <ArrowDownWideNarrow size={18} aria-hidden="true" />
            <span className="visually-hidden">Ordenar produtos</span>
            <select
              value={ordenacao}
              onChange={(e) => setOrdenacao(e.target.value)}
              aria-label="Ordenar produtos"
            >
              <option value="nome-asc">Nome (A-Z)</option>
              <option value="preco-asc">Menor preço</option>
              <option value="preco-desc">Maior preço</option>
              <option value="estoque-desc">Maior estoque</option>
            </select>
          </label>
          <div className="catalogo-visualizacao" role="group" aria-label="Visualização dos produtos">
            <button
              type="button"
              className={`catalogo-visualizacao-btn ${visualizacao === 'grade' ? 'active' : ''}`}
              onClick={() => setVisualizacao('grade')}
              aria-label="Visualizar produtos em grade"
              aria-pressed={visualizacao === 'grade'}
              title="Visualizar em grade"
            >
              <LayoutGrid size={18} />
            </button>
            <button
              type="button"
              className={`catalogo-visualizacao-btn ${visualizacao === 'lista' ? 'active' : ''}`}
              onClick={() => setVisualizacao('lista')}
              aria-label="Visualizar produtos em lista"
              aria-pressed={visualizacao === 'lista'}
              title="Visualizar em lista"
            >
              <List size={18} />
            </button>
          </div>
        </div>
      </div>

      {erro && <div className="alert alert-error" role="alert">{erro}</div>}
      {erroEstoque && <div className="alert alert-warning" role="alert">{erroEstoque}</div>}

      {loading ? (
        <div className="loading-state">Carregando catálogo do banco de dados...</div>
      ) : erro ? null : produtos.length === 0 ? (
        <div className="empty-state">Nenhum produto cadastrado no momento.</div>
      ) : (
        visualizacao === 'lista' ? (
          <div className="catalogo-produtos-lista">
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
                {produtosOrdenados.map((p) => (
                  <tr key={p.id}>
                    <td className="catalogo-produto-lista">
                      {imagensPorProduto[p.id] && (
                        <img
                          className="produto-imagem-lista"
                          src={imagensPorProduto[p.id]}
                          alt={p.nome}
                          loading="lazy"
                        />
                      )}
                      <strong>{p.nome}</strong>
                    </td>
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
          <div className="catalogo-produtos-grid">
            {produtosOrdenados.map((p) => (
              <div key={p.id} className="produto-card">
                {imagensPorProduto[p.id] && (
                  <img
                    className="produto-imagem"
                    src={imagensPorProduto[p.id]}
                    alt={p.nome}
                    loading="lazy"
                  />
                )}
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
