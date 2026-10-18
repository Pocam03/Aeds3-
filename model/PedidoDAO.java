package model;

import java.io.File;
import java.util.ArrayList;

public class PedidoDAO {
    private Arquivo<Pedido> arqPedidos;
    private JogoDAO jogoDAO;
    private CupomDAO cupomDAO;
    private PedidoJogoDAO pedidoJogoDAO; // entidade associativa Pedido_Jogo (N:N com Jogo)
    private ArvoreBMais indiceCliente; // índice secundário sobre a FK Pedido.idCliente
    private HashExtensivel indiceId;   // índice de hash extensível para busca direta por PK (Pedido.id)

    public PedidoDAO() throws Exception {
        arqPedidos = new Arquivo<>("pedidos", Pedido.class.getConstructor());
        jogoDAO = new JogoDAO();
        cupomDAO = new CupomDAO();
        pedidoJogoDAO = new PedidoJogoDAO();
        indiceCliente = new ArvoreBMais("./dados/pedidos/idx_cliente.db");

        String arqDiretorio = "./dados/pedidos/idx_id_dir.db";
        String arqBaldes = "./dados/pedidos/idx_id_baldes.db";
        boolean indiceNovo = !new File(arqDiretorio).exists();

        indiceId = new HashExtensivel(arqDiretorio, arqBaldes);
        if (indiceNovo) arqPedidos.reconstruirIndice(indiceId); // popula o índice sobre dados pré-existentes
    }

    public Pedido buscarPedido(int id) throws Exception {
        long endereco = indiceId.buscar(id);
        if (endereco == -1) return null;
        return carregarItens(arqPedidos.lerNoEndereco(endereco));
    }

    /** Preenche os jogos do pedido a partir de Pedido_Jogo (não depende de jogos.db). */
    private Pedido carregarItens(Pedido pedido) throws Exception {
        pedido.setItens(pedidoJogoDAO.listarPorPedido(pedido.getId()));
        return pedido;
    }

    public ArrayList<Pedido> listarPedidos() throws Exception {
        ArrayList<Pedido> pedidos = arqPedidos.readAll();
        for (Pedido p : pedidos) carregarItens(p);
        return pedidos;
    }

    /**
     * Consulta típica do relacionamento 1:N via FK: em vez de percorrer
     * todo o arquivo de pedidos, busca no índice em Árvore B+ (chave =
     * idCliente) os endereços dos pedidos daquele cliente e lê cada um
     * diretamente pelo endereço.
     */
    public ArrayList<Pedido> listarPedidosPorCliente(int idCliente) throws Exception {
        ArrayList<Pedido> pedidosCliente = new ArrayList<>();
        for (long endereco : indiceCliente.buscar(idCliente)) {
            pedidosCliente.add(carregarItens(arqPedidos.lerNoEndereco(endereco)));
        }
        return pedidosCliente;
    }

    /*
     Cria um novo pedido a partir do cliente, da lista de IDs de jogos
     e do ID de um cupom. Busca os jogos e o cupom, calcula o valor final e grava o pedido.
     */
    public Pedido incluirPedido(int idCliente, ArrayList<Integer> idJogos, int idCupom) throws Exception {
        // Jogos existentes agora; título e preço são copiados para Pedido_Jogo
        ArrayList<Jogo> jogosComprados = new ArrayList<>();
        for (int idJogo : idJogos) {
            Jogo jogo = jogoDAO.buscarJogo(idJogo);
            if (jogo != null) {
                jogosComprados.add(jogo);
            }
        }

        Cupom cupom = null;
        if (idCupom != -1) {
            cupom = cupomDAO.buscarCupom(idCupom);
        }

        Pedido pedido = new Pedido(idCliente, new ArrayList<>(), idCupom);
        // valor calculado a partir dos mesmos preços que serão gravados nos itens
        ArrayList<PedidoJogo> previa = new ArrayList<>();
        for (Jogo jogo : jogosComprados) previa.add(new PedidoJogo(-1, jogo));
        pedido.calcularValorFinal(previa, cupom);

        if (arqPedidos.create(pedido) <= 0) return null;

        long endereco = arqPedidos.getUltimoEndereco();
        indiceId.inserir(pedido.getId(), endereco);
        indiceCliente.inserir(idCliente, endereco);

        ArrayList<PedidoJogo> itens = new ArrayList<>();
        for (Jogo jogo : jogosComprados) {
            itens.add(pedidoJogoDAO.incluir(pedido.getId(), jogo));
        }
        pedido.setItens(itens);

        return pedido;
    }

    /**
     * Atualiza um pedido já existente. Os jogos são sincronizados com
     * Pedido_Jogo: itens que continuam no pedido são mantidos como estão
     * (inclusive os de jogos já excluídos do catálogo, com o preço original),
     * itens removidos são apagados e jogos novos entram com título e preço
     * atuais. O valor final é recalculado a partir dos preços gravados.
     */
    public boolean alterarPedido(Pedido pedido) throws Exception {
        long enderecoAntigo = indiceId.buscar(pedido.getId());
        if (enderecoAntigo == -1) return false;

        ArrayList<PedidoJogo> existentes = pedidoJogoDAO.listarPorPedido(pedido.getId());
        ArrayList<PedidoJogo> mantidos = new ArrayList<>();
        ArrayList<Jogo> novos = new ArrayList<>();

        for (int idJogo : pedido.getIdJogos()) {
            PedidoJogo igual = null;
            for (PedidoJogo item : existentes) {
                if (item.getIdJogo() == idJogo) { igual = item; break; }
            }
            if (igual != null) {
                existentes.remove(igual);
                mantidos.add(igual);
            } else {
                Jogo jogo = jogoDAO.buscarJogo(idJogo);
                if (jogo != null) novos.add(jogo);
            }
        }
        // o que sobrou em "existentes" foi removido do pedido

        Cupom cupom = null;
        if (pedido.getIdCupom() != -1) {
            cupom = cupomDAO.buscarCupom(pedido.getIdCupom());
        }

        ArrayList<PedidoJogo> itensFinais = new ArrayList<>(mantidos);
        for (Jogo jogo : novos) itensFinais.add(new PedidoJogo(-1, jogo));
        pedido.calcularValorFinal(itensFinais, cupom);

        long enderecoNovo = arqPedidos.updateNoEndereco(enderecoAntigo, pedido);
        if (enderecoNovo == -1) return false;

        if (enderecoNovo != enderecoAntigo) {
            // registro foi realocado (não coube no espaço antigo): reindexa
            indiceId.inserir(pedido.getId(), enderecoNovo);
            indiceCliente.remover(pedido.getIdCliente(), enderecoAntigo);
            indiceCliente.inserir(pedido.getIdCliente(), enderecoNovo);
        }

        for (PedidoJogo removido : existentes) pedidoJogoDAO.excluir(removido);
        ArrayList<PedidoJogo> gravados = new ArrayList<>(mantidos);
        for (Jogo jogo : novos) gravados.add(pedidoJogoDAO.incluir(pedido.getId(), jogo));
        pedido.setItens(gravados);

        return true;
    }

    public boolean excluirPedido(int id) throws Exception {
        long endereco = indiceId.buscar(id);
        if (endereco == -1) return false;

        Pedido pedido = arqPedidos.lerNoEndereco(endereco);
        boolean sucesso = arqPedidos.deleteNoEndereco(endereco);

        if (sucesso) {
            indiceId.remover(id);
            indiceCliente.remover(pedido.getIdCliente(), endereco);
            pedidoJogoDAO.excluirPorPedido(id); // itens pertencem ao pedido: saem junto com ele
        }

        return sucesso;
    }
}
