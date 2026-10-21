import React, { useEffect, useMemo, useState } from 'react';
import { Search, RefreshCw, Minus, Plus, ShoppingCart } from 'lucide-react';
import { databaseService } from '../services/databaseService';
import { useCart } from '../context/CartContext';
import './catalogo-b.css';

const imagensDisponiveis = Object.values(
  import.meta.glob('../../img/*.{png,jpg,jpeg,webp,avif}', {
    eager: true,
    import: 'default',
    query: '?url',
  })
);

const ORDENACOES = [
  { valor: 'nome-asc', rotulo: 'Nome (A-Z)' },
  { valor: 'preco-asc', rotulo: 'Menor preço' },
  { valor: 'preco-desc', rotulo: 'Maior preço' },
  { valor: 'estoque-desc', rotulo: 'Maior estoque' },
];

const LIMITE_ULTIMAS_UNIDADES = 5;

const formatarPreco = (valor) =>
  Number(valor || 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

/**
 * Catálogo, variante B (teste A/B).
 * Mesma fonte de dados e mesma regra de estoque da variante A; muda só o layout:
 * filtros em coluna lateral, produtos em linhas e seletor de quantidade que
 * respeita o que ainda cabe no carrinho.
 */
export default function CatalogoB() {
  const [produtos, setProdutos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [erro, setErro] = useState('');
  const [busca, setBusca] = useState('');
  const [ordenacao, setOrdenacao] = useState('nome-asc');
  const [somenteEstoque, setSomenteEstoque] = useState(false);
  const [quantidades, setQuantidades] = useState({});
  const [avisos, setAvisos] = useState({});
  const { itens, adicionarItem } = useCart();

  const carregarProdutos = async (termo = busca) => {
    setLoading(true);
    setErro('');
    try {
      setProdutos(await databaseService.getProdutos(termo));
    } catch (error) {
      setProdutos([]);
      setErro(`Não foi possível carregar os produtos: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const espera = setTimeout(() => carregarProdutos(busca), 300);
    return () => clearTimeout(espera);
  }, [busca]);

  const noCarrinho = (produtoId) =>
    itens.find((item) => item.produto.id === produtoId)?.quantidade ?? 0;

  const visiveis = useMemo(() => {
    const porNome = (a, b) =>
      String(a.nome || '').localeCompare(String(b.nome || ''), 'pt-BR', { sensitivity: 'base' });
    const lista = produtos.filter((p) => !somenteEstoque || p.estoque > 0);
    return lista.sort((a, b) => {
      switch (ordenacao) {
        case 'preco-asc':
          return Number(a.preco) - Number(b.preco) || porNome(a, b);
        case 'preco-desc':
          return Number(b.preco) - Number(a.preco) || porNome(a, b);
        case 'estoque-desc':
          return Number(b.estoque) - Number(a.estoque) || porNome(a, b);
        default:
          return porNome(a, b);
      }
    });
  }, [produtos, ordenacao, somenteEstoque]);

  const maiorEstoque = Math.max(1, ...produtos.map((p) => Number(p.estoque) || 0));

  const imagemDe = (produto) =>
    imagensDisponiveis.length ? imagensDisponiveis[produto.id % imagensDisponiveis.length] : null;

  const disponivelPara = (produto) => Math.max(0, produto.estoque - noCarrinho(produto.id));

  const quantidadeEscolhida = (produto) =>
    Math.min(quantidades[produto.id] ?? 1, Math.max(1, disponivelPara(produto)));

  const alterarQuantidade = (produto, delta) => {
    const nova = Math.min(Math.max(1, quantidadeEscolhida(produto) + delta), disponivelPara(produto));
    setQuantidades((q) => ({ ...q, [produto.id]: nova }));
  };

  const avisar = (produtoId, aviso) => {
    setAvisos((a) => ({ ...a, [produtoId]: aviso }));
    if (aviso.tipo === 'ok') {
      setTimeout(() => setAvisos((a) => ({ ...a, [produtoId]: null })), 2500);
    }
  };

  const handleAdicionar = (produto) => {
    const quantidade = quantidadeEscolhida(produto);
    // A validação de estoque fica no CartContext (mesma regra da variante A).
    const resultado = adicionarItem(produto, quantidade) ?? { ok: true };
    if (resultado.ok) {
      avisar(produto.id, {
        tipo: 'ok',
        texto: quantidade === 1 ? '1 unidade no carrinho' : `${quantidade} unidades no carrinho`,
      });
      setQuantidades((q) => ({ ...q, [produto.id]: 1 }));
    } else {
      avisar(produto.id, { tipo: 'erro', texto: resultado.mensagem });
    }
  };

  const rotuloEstoque = (estoque) => {
    if (estoque <= 0) return 'Esgotado';
    if (estoque <= LIMITE_ULTIMAS_UNIDADES) return `Restam ${estoque}`;
    return `${estoque} em estoque`;
  };

  return (
    <div className="page-container">
      <div className="cb-layout">
        <aside className="cb-filtros" aria-label="Filtros do catálogo">
          <div className="cb-busca">
            <Search size={18} aria-hidden="true" />
            <input
              type="search"
              value={busca}
              onChange={(e) => setBusca(e.target.value)}
              placeholder="Buscar produto"
              aria-label="Buscar produto por nome"
              data-cy="busca-produto"
            />
          </div>

          <fieldset className="cb-grupo">
            <legend>Ordenar por</legend>
            {ORDENACOES.map((o) => (
              <label key={o.valor} className="cb-opcao">
                <input
                  type="radio"
                  name="ordenacao-b"
                  value={o.valor}
                  checked={ordenacao === o.valor}
                  onChange={() => setOrdenacao(o.valor)}
                />
                {o.rotulo}
              </label>
            ))}
          </fieldset>

          <label className="cb-opcao">
            <input
              type="checkbox"
              checked={somenteEstoque}
              onChange={(e) => setSomenteEstoque(e.target.checked)}
            />
            Mostrar só o que tem em estoque
          </label>

          <button type="button" className="btn btn-secondary btn-icon" onClick={() => carregarProdutos(busca)}>
            <RefreshCw size={16} /> Atualizar lista
          </button>
        </aside>

        <section aria-labelledby="cb-titulo">
          <div className="cb-cabecalho">
            <h2 id="cb-titulo">Produtos</h2>
            {!loading && !erro && (
              <span className="cb-contagem">
                {visiveis.length === 1 ? '1 produto' : `${visiveis.length} produtos`}
              </span>
            )}
          </div>

          {erro && <div className="alert alert-error" role="alert">{erro}</div>}

          {loading ? (
            <div className="loading-state">Carregando produtos...</div>
          ) : !erro && visiveis.length === 0 ? (
            <div className="empty-state">
              Nenhum produto encontrado. Limpe a busca ou desmarque o filtro de estoque.
            </div>
          ) : (
            <ul className="cb-lista">
              {visiveis.map((p) => {
                const disponivel = disponivelPara(p);
                const jaNoCarrinho = noCarrinho(p.id);
                const aviso = avisos[p.id];
                const nivel = p.estoque <= 0 ? 'zerado' : p.estoque <= LIMITE_ULTIMAS_UNIDADES ? 'baixo' : 'ok';
                const imagem = imagemDe(p);

                return (
                  <li key={p.id} className="cb-item">
                    {imagem ? (
                      <img className="cb-imagem" src={imagem} alt="" loading="lazy" />
                    ) : (
                      <div className="cb-imagem" aria-hidden="true" />
                    )}

                    <div className="cb-info">
                      <h3>{p.nome}</h3>
                      <div className={`cb-estoque cb-estoque-${nivel}`}>
                        <span className="cb-medidor" aria-hidden="true">
                          <span style={{ width: `${Math.max(4, (p.estoque / maiorEstoque) * 100)}%` }} />
                        </span>
                        <span>{rotuloEstoque(p.estoque)}</span>
                      </div>
                      {jaNoCarrinho > 0 && (
                        <p className="cb-no-carrinho">Você já tem {jaNoCarrinho} no carrinho</p>
                      )}
                    </div>

                    <div className="cb-compra">
                      <p className="cb-preco">{formatarPreco(p.preco)}</p>

                      {disponivel > 0 && (
                        <div className="cb-stepper" role="group" aria-label={`Quantidade de ${p.nome}`}>
                          <button
                            type="button"
                            onClick={() => alterarQuantidade(p, -1)}
                            disabled={quantidadeEscolhida(p) <= 1}
                            aria-label="Diminuir quantidade"
                          >
                            <Minus size={14} />
                          </button>
                          <span aria-live="polite">{quantidadeEscolhida(p)}</span>
                          <button
                            type="button"
                            onClick={() => alterarQuantidade(p, 1)}
                            disabled={quantidadeEscolhida(p) >= disponivel}
                            aria-label="Aumentar quantidade"
                          >
                            <Plus size={14} />
                          </button>
                        </div>
                      )}

                      <button
                        type="button"
                        className="btn btn-primary cb-botao"
                        onClick={() => handleAdicionar(p)}
                        disabled={disponivel <= 0}
                      >
                        <ShoppingCart size={16} />
                        {p.estoque <= 0
                          ? 'Esgotado'
                          : disponivel <= 0
                            ? 'Todo o estoque está no carrinho'
                            : 'Adicionar ao carrinho'}
                      </button>

                      <p
                        className={`cb-aviso ${aviso ? `cb-aviso-${aviso.tipo}` : ''}`}
                        role="status"
                      >
                        {aviso?.texto}
                      </p>
                    </div>
                  </li>
                );
              })}
            </ul>
          )}
        </section>
      </div>
    </div>
  );
}
