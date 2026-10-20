import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { databaseService } from '../services/databaseService';
import { PackagePlus, DollarSign, Layers, Tag } from 'lucide-react';

export default function CadastroProduto() {
  const { token } = useAuth();
  const [nome, setNome] = useState('');
  const [preco, setPreco] = useState('');
  const [estoque, setEstoque] = useState('');
  const [mensagem, setMensagem] = useState({ tipo: '', texto: '' });
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMensagem({ tipo: '', texto: '' });

    const valorPreco = parseFloat(preco);
    const qtdEstoque = Number(estoque);

    // Requisitos de validação do Dev 2
    if (!nome.trim()) {
      setMensagem({ tipo: 'error', texto: 'O nome do produto é obrigatório.' });
      return;
    }

    if (isNaN(valorPreco) || valorPreco <= 0) {
      setMensagem({ tipo: 'error', texto: 'O preço deve ser um valor positivo maior que zero.' });
      return;
    }

    if (estoque.trim() === '' || !Number.isInteger(qtdEstoque) || qtdEstoque < 0) {
      setMensagem({ tipo: 'error', texto: 'O estoque deve ser um número inteiro maior ou igual a zero.' });
      return;
    }

    try {
      setLoading(true);
      await databaseService.criarProduto({
        nome: nome.trim(),
        preco: valorPreco,
        estoque: qtdEstoque,
      }, token);

      setMensagem({ tipo: 'success', texto: `Produto "${nome}" cadastrado com sucesso no banco de dados!` });
      setNome('');
      setPreco('');
      setEstoque('');
    } catch (err) {
      setMensagem({ tipo: 'error', texto: err.message || 'Erro ao cadastrar produto.' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-container">
      <div className="card form-card">
        <div className="card-header">
          <PackagePlus size={32} className="header-icon" />
          <div>
            <h2>Cadastro de Produto</h2>
            <p>Módulo exclusivo para Vendedor (Dev 2)</p>
          </div>
        </div>

        {mensagem.texto && (
          <div className={`alert alert-${mensagem.tipo}`}>{mensagem.texto}</div>
        )}

        <form onSubmit={handleSubmit} className="form">
          <div className="form-group">
            <label htmlFor="nome">Nome do Produto</label>
            <div className="input-icon-wrapper">
              <Tag size={18} />
              <input
                id="nome"
                type="text"
                placeholder="Ex: Teclado Mecânico RGB"
                value={nome}
                onChange={(e) => setNome(e.target.value)}
                required
              />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="preco">Preço Unitário (R$)</label>
              <div className="input-icon-wrapper">
                <DollarSign size={18} />
                <input
                  id="preco"
                  type="number"
                  step="0.01"
                  min="0.01"
                  placeholder="0.00"
                  value={preco}
                  onChange={(e) => setPreco(e.target.value)}
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="estoque">Quantidade em Estoque</label>
              <div className="input-icon-wrapper">
                <Layers size={18} />
                <input
                  id="estoque"
                  type="number"
                  min="0"
                  placeholder="Ex: 10"
                  value={estoque}
                  onChange={(e) => setEstoque(e.target.value)}
                  required
                />
              </div>
            </div>
          </div>

          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Salvando no Banco...' : 'Cadastrar Produto'}
          </button>
        </form>
      </div>
    </div>
  );
}
