package br.com.ecommerce.unit.fake;

import br.com.ecommerce.model.Produto;
import br.com.ecommerce.repository.ProdutoRepository;
import java.util.ArrayList;
import java.util.List;

public class ProdutoRepositoryEmMemoria implements ProdutoRepository {
    private final List<Produto> produtos = new ArrayList<>();

    @Override
    public Produto salvar(Produto produto) {
        produto.setId(produtos.size() + 1);
        produtos.add(produto);
        return produto;
    }

    public List<Produto> listar() { return List.copyOf(produtos); }
}