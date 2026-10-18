package model;

import java.util.ArrayList;

/**
 * Acesso à entidade associativa Pedido_Jogo (relacionamento N:N).
 *
 * Os itens ficam em dados/pedido_jogo/pedido_jogo.db e são consultados por
 * dois índices secundários em Árvore B+ (chave duplicada -> endereço):
 *  - idx_pedido.db: idPedido -> itens do pedido (jogos de um pedido);
 *  - idx_jogo.db:   idJogo   -> itens do jogo   (pedidos de um jogo).
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

    /** Grava um item copiando título e preço atuais do jogo. */
    public PedidoJogo incluir(int idPedido, Jogo jogo) throws Exception {
        PedidoJogo item = new PedidoJogo(idPedido, jogo);
        if (arqItens.create(item) <= 0) return null;

        long endereco = arqItens.getUltimoEndereco();
        indicePedido.inserir(idPedido, endereco);
        indiceJogo.inserir(item.getIdJogo(), endereco);
        return item;
    }

    /** Jogos (itens) de um pedido, com os dados da época da compra. */
    public ArrayList<PedidoJogo> listarPorPedido(int idPedido) throws Exception {
        ArrayList<PedidoJogo> itens = new ArrayList<>();
        for (long endereco : indicePedido.buscar(idPedido)) {
            itens.add(arqItens.lerNoEndereco(endereco));
        }
        return itens;
    }

    /** Itens de pedidos que contêm um determinado jogo. */
    public ArrayList<PedidoJogo> listarPorJogo(int idJogo) throws Exception {
        ArrayList<PedidoJogo> itens = new ArrayList<>();
        for (long endereco : indiceJogo.buscar(idJogo)) {
            itens.add(arqItens.lerNoEndereco(endereco));
        }
        return itens;
    }

    /** Todos os itens de todos os pedidos. */
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
