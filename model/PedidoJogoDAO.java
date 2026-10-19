package model;

import java.util.ArrayList;

/**
 * Acesso à entidade Pedido_Jogo (relacionamento N:N).
 *
 * Os itens ficam em dados/pedido_jogo/pedido_jogo.db e são consultados por
 * dois índices secundários em Árvore B+ (chave duplicada -> endereço):
 *  - idx_pedido.db: idPedido -> itens do pedido (jogos de um pedido);
 *  - idx_jogo.db:   idJogo   -> itens do jogo   (pedidos de um jogo).
 *
 * A chave do relacionamento é composta: (idPedido, idJogo). O campo id do
 * registro existe só porque Arquivo<T> gera um ID sequencial para todo
 * registro; a unicidade da associação é garantida por incluir(), que soma a
 * quantidade quando o mesmo jogo é comprado de novo no mesmo pedido.
 *
 * Nenhuma operação daqui depende de o jogo ainda existir em jogos.db.
 */
public class PedidoJogoDAO {
    private Arquivo<PedidoJogo> arqItens;
    private ArvoreBMais indicePedido;
    private ArvoreBMais indiceJogo;

    public PedidoJogoDAO() throws Exception {
        arqItens = new Arquivo<>("pedido_jogo", PedidoJogo.class.getConstructor());
        indicePedido = new ArvoreBMais("./dados/pedido_jogo/idx_pedido.db");
        indiceJogo = new ArvoreBMais("./dados/pedido_jogo/idx_jogo.db");
    }

    /**
     * Busca pela chave composta (idPedido, idJogo). Usa o índice de idPedido
     * para chegar só aos itens daquele pedido e filtra pelo jogo.
     */
    public PedidoJogo buscar(int idPedido, int idJogo) throws Exception {
        for (long endereco : indicePedido.buscar(idPedido)) {
            PedidoJogo item = arqItens.lerNoEndereco(endereco);
            if (item.getIdJogo() == idJogo) return item;
        }
        return null;
    }

    /**
     * Grava um item copiando título e preço atuais do jogo. A chave composta
     * (idPedido, idJogo) é única: se a associação já existir, nenhum registro
     * novo é criado — a quantidade do item existente é somada.
     */
    public PedidoJogo incluir(int idPedido, Jogo jogo, int quantidade) throws Exception {
        PedidoJogo existente = buscar(idPedido, jogo.getId());
        if (existente != null) {
            existente.setQuantidade(existente.getQuantidade() + quantidade);
            return alterar(existente) ? existente : null;
        }

        PedidoJogo item = new PedidoJogo(idPedido, jogo, quantidade);
        if (arqItens.create(item) <= 0) return null;

        long endereco = arqItens.getUltimoEndereco();
        indicePedido.inserir(idPedido, endereco);
        indiceJogo.inserir(item.getIdJogo(), endereco);
        return item;
    }

    /** Regrava um item já existente (ex.: nova quantidade), localizado pela chave composta. */
    public boolean alterar(PedidoJogo item) throws Exception {
        for (long endereco : indicePedido.buscar(item.getIdPedido())) {
            PedidoJogo atual = arqItens.lerNoEndereco(endereco);
            if (atual.getIdJogo() != item.getIdJogo()) continue;

            long novoEndereco = arqItens.updateNoEndereco(endereco, item);
            if (novoEndereco == -1) return false;
            if (novoEndereco != endereco) { // registro realocado: reindexa nos dois índices
                indicePedido.remover(item.getIdPedido(), endereco);
                indiceJogo.remover(item.getIdJogo(), endereco);
                indicePedido.inserir(item.getIdPedido(), novoEndereco);
                indiceJogo.inserir(item.getIdJogo(), novoEndereco);
            }
            return true;
        }
        return false;
    }

    // Jogos (itens) de um pedido
    public ArrayList<PedidoJogo> listarPorPedido(int idPedido) throws Exception {
        ArrayList<PedidoJogo> itens = new ArrayList<>();
        for (long endereco : indicePedido.buscar(idPedido)) {
            itens.add(arqItens.lerNoEndereco(endereco));
        }
        return itens;
    }

    // Itens de pedidos que contêm um determinado jogo
    public ArrayList<PedidoJogo> listarPorJogo(int idJogo) throws Exception {
        ArrayList<PedidoJogo> itens = new ArrayList<>();
        for (long endereco : indiceJogo.buscar(idJogo)) {
            itens.add(arqItens.lerNoEndereco(endereco));
        }
        return itens;
    }

    public ArrayList<PedidoJogo> listarTodos() throws Exception {
        return arqItens.readAll();
    }

    /** Remove um único item (usado ao tirar um jogo de um pedido editado). */
    public boolean excluir(PedidoJogo item) throws Exception {
        for (long endereco : indicePedido.buscar(item.getIdPedido())) {
            PedidoJogo atual = arqItens.lerNoEndereco(endereco);
            if (atual.getId() == item.getId()) {
                removerNoEndereco(endereco, atual);
                return true;
            }
        }
        return false;
    }

    /** Remove todos os itens de um pedido (usado ao excluir o pedido). */
    public void excluirPorPedido(int idPedido) throws Exception {
        for (long endereco : indicePedido.buscar(idPedido)) {
            removerNoEndereco(endereco, arqItens.lerNoEndereco(endereco));
        }
    }

    private void removerNoEndereco(long endereco, PedidoJogo item) throws Exception {
        if (arqItens.deleteNoEndereco(endereco)) {
            indicePedido.remover(item.getIdPedido(), endereco);
            indiceJogo.remover(item.getIdJogo(), endereco);
        }
    }
}
