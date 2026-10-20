import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { databaseService } from '../services/databaseService';
import {
  Store,
  Package,
  PlusCircle,
  DollarSign,
  Layers,
  ShoppingBag,
  MapPin,
  Mail,
  Building2,
  CheckCircle2,
  ArrowRight,
  Trash2
} from 'lucide-react';

export default function Dashboard() {
  const { usuario, loja, token } = useAuth();
  const [produtos, setProdutos] = useState([]);
  const [quantidadePedidos, setQuantidadePedidos] = useState(0);
  const [valorVendas, setValorVendas] = useState(0);
  const [loading, setLoading] = useState(true);
  const [produtoExcluindo, setProdutoExcluindo] = useState(null);
  const [erro, setErro] = useState('');

  useEffect(() => {
    async function carregarDashboard() {
      try {
        setLoading(true);
        setErro('');
        const data = await databaseService.carregarDashboardLoja(token);
        setProdutos(data.produtos || []);
        setQuantidadePedidos(data.quantidadePedidos || 0);
        setValorVendas(Number(data.valorVendas) || 0);
      } catch (err) {
        setErro(`Não foi possível carregar os dados da loja: ${err.message}`);
      } finally {
        setLoading(false);
      }
    }
    carregarDashboard();
  }, [token]);

  const excluirProduto = async (produto) => {
    if (!window.confirm(`Deseja excluir "${produto.nome}" do catálogo?`)) return;

    try {
      setProdutoExcluindo(produto.id);
      setErro('');
      await databaseService.excluirProduto(produto.id, token);
      setProdutos((atuais) => atuais.filter((item) => item.id !== produto.id));
    } catch (err) {
      setErro(`Não foi possível excluir o produto: ${err.message}`);
    } finally {
      setProdutoExcluindo(null);
    }
  };

  const totalEstoque = produtos.reduce((acc, p) => acc + (Number(p.estoque) || 0), 0);
  const formatarMoeda = (valor) =>
    new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(valor);

  return (
    <div className="page-container dashboard-container">
      {/* CABEÇALHO DA LOJA */}
      <div className="dashboard-hero">
        <div className="dashboard-hero-content">
          <div className="dashboard-hero-info">
            <div className="loja-avatar">
              <Store size={36} />
            </div>
            <div>
              <div className="loja-titulo-row">
                <h2>{loja?.nomeLoja || 'Minha Loja'}</h2>
                <span className="badge-loja-status">
                  <CheckCircle2 size={13} /> Loja Ativa
                </span>
              </div>
              <p className="loja-responsavel">
                Responsável: <strong>{usuario?.nome || 'Vendedor'} {usuario?.sobrenome || ''}</strong>
              </p>
              <div className="loja-detalhes-list">
                {loja?.emailLoja && (
                  <span>
                    <Mail size={14} /> {loja.emailLoja}
                  </span>
                )}
                {loja?.cidadeLoja && loja?.estadoLoja && (
                  <span>
                    <MapPin size={14} /> {loja.cidadeLoja} - {loja.estadoLoja}
                  </span>
                )}
                {loja?.cnpj && (
                  <span>
                    <Building2 size={14} /> CNPJ: {loja.cnpj}
                  </span>
                )}
              </div>
            </div>
          </div>

          <div className="dashboard-hero-acoes">
            <Link to="/cadastro-produto" className="btn btn-primary btn-novo-produto">
              <PlusCircle size={18} /> Cadastrar Novo Produto
            </Link>
          </div>
        </div>
      </div>

      {/* CARDS DE MÉTRICAS */}
      <div className="dashboard-metricas-grid">
        <div className="metrica-card">
          <div className="metrica-icon bg-blue">
            <Package size={22} />
          </div>
          <div className="metrica-dados">
            <span className="metrica-label">Total de Produtos</span>
            <span className="metrica-valor">{produtos.length}</span>
          </div>
        </div>

        <div className="metrica-card">
          <div className="metrica-icon bg-emerald">
            <Layers size={22} />
          </div>
          <div className="metrica-dados">
            <span className="metrica-label">Itens em Estoque</span>
            <span className="metrica-valor">{totalEstoque}</span>
          </div>
        </div>

        <div className="metrica-card">
          <div className="metrica-icon bg-purple">
            <DollarSign size={22} />
          </div>
          <div className="metrica-dados">
            <span className="metrica-label">Valor Total das Vendas</span>
            <span className="metrica-valor">{formatarMoeda(valorVendas)}</span>
          </div>
        </div>

        <div className="metrica-card">
          <div className="metrica-icon bg-blue">
            <ShoppingBag size={22} />
          </div>
          <div className="metrica-dados">
            <span className="metrica-label">Pedidos Vendidos</span>
            <span className="metrica-valor">{quantidadePedidos}</span>
          </div>
        </div>
      </div>

      {/* SEÇÃO DA TABELA DE PRODUTOS */}
      <div className="card dashboard-tabela-card">
        <div className="dashboard-tabela-header">
          <div>
            <h3>Produtos da Loja</h3>
            <p className="text-muted">Listagem dos produtos cadastrados para esta loja</p>
          </div>
          <Link to="/cadastro-produto" className="btn-link-action">
            Adicionar Produto <ArrowRight size={14} />
          </Link>
        </div>

        {loading ? (
          <div className="dashboard-loading">
            <p>Carregando produtos da loja...</p>
          </div>
        ) : erro ? (
          <div className="alert alert-error" role="alert">{erro}</div>
        ) : produtos.length === 0 ? (
          <div className="empty-state">
            <Package size={48} className="empty-icon" />
            <h3>Nenhum produto cadastrado ainda</h3>
            <p>Sua loja está pronta para vender! Adicione o seu primeiro item ao catálogo.</p>
            <Link to="/cadastro-produto" className="btn btn-primary" style={{ marginTop: '1rem' }}>
              <PlusCircle size={18} /> Cadastrar Primeiro Produto
            </Link>
          </div>
        ) : (
          <div className="tabela-wrapper">
            <table className="tabela-produtos">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Produto</th>
                  <th>Preço Unitário</th>
                  <th>Estoque</th>
                  <th>Unidades vendidas</th>
                  <th>Status</th>
                  <th>Ações</th>
                </tr>
              </thead>
              <tbody>
                {produtos.map((p) => (
                  <tr key={p.id}>
                    <td>#{p.id}</td>
                    <td>
                      <strong>{p.nome}</strong>
                    </td>
                    <td>
                      {formatarMoeda(Number(p.preco) || 0)}
                    </td>
                    <td>
                      <span className={`estoque-tag ${p.estoque > 0 ? 'em-estoque' : 'esgotado'}`}>
                        {p.estoque} un.
                      </span>
                    </td>
                    <td>{Number(p.quantidadeVendida) || 0}</td>
                    <td>
                      {p.estoque > 0 ? (
                        <span className="badge-status-ativo">Ativo</span>
                      ) : (
                        <span className="badge-status-esgotado">Esgotado</span>
                      )}
                    </td>
                    <td>
                      <button
                        type="button"
                        className="btn btn-danger btn-sm"
                        onClick={() => excluirProduto(p)}
                        disabled={produtoExcluindo === p.id}
                        aria-label={`Excluir ${p.nome}`}
                      >
                        <Trash2 size={15} />
                        {produtoExcluindo === p.id ? 'Excluindo...' : 'Excluir'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
