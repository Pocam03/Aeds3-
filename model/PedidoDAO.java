package model;

import java.io.File;
import java.util.ArrayList;

public class PedidoDAO {
    private Arquivo<Pedido> arqPedidos;
    private JogoDAO jogoDAO;
    private CupomDAO cupomDAO;
    private ArvoreBMais indiceCliente; // índice secundário sobre a FK Pedido.idCliente
    private HashExtensivel indiceId;   // índice de hash extensível para busca direta por PK (Pedido.id)

    public PedidoDAO() throws Exception {
        arqPedidos = new Arquivo<>("pedidos", Pedido.class.getConstructor());
        jogoDAO = new JogoDAO();
        cupomDAO = new CupomDAO();
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
        return arqPedidos.lerNoEndereco(endereco);
    }

    public ArrayList<Pedido> listarPedidos() throws Exception {
        return arqPedidos.readAll();
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
            pedidosCliente.add(arqPedidos.lerNoEndereco(endereco));
        }
        return pedidosCliente;
    }

    /*
     Cria um novo pedido a partir do cliente, da lista de IDs de jogos
     e do ID de um cupom. Busca os jogos e o cupom, calcula o valor final e grava o pedido.
     */
    public Pedido incluirPedido(int idCliente, ArrayList<Integer> idJogos, int idCupom) throws Exception {
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

        Pedido pedido = new Pedido(idCliente, idJogos, idCupom);
        pedido.calcularValorFinal(jogosComprados, cupom);

        if (arqPedidos.create(pedido) <= 0) return null;

        long endereco = arqPedidos.getUltimoEndereco();
        indiceId.inserir(pedido.getId(), endereco);
        indiceCliente.inserir(idCliente, endereco);

        return pedido;
    }

    /**
     * Recalcula e atualiza o valor final de um pedido já existente
     * (útil se a lista de jogos ou o cupom forem alterados).
     */
    public boolean alterarPedido(Pedido pedido) throws Exception {
        ArrayList<Jogo> jogosComprados = new ArrayList<>();
        for (int idJogo : pedido.getIdJogos()) {
            Jogo jogo = jogoDAO.buscarJogo(idJogo);
            if (jogo != null) {
                jogosComprados.add(jogo);
            }
        }

        Cupom cupom = null;
        if (pedido.getIdCupom() != -1) {
            cupom = cupomDAO.buscarCupom(pedido.getIdCupom());
        }

        long enderecoAntigo = indiceId.buscar(pedido.getId());
        if (enderecoAntigo == -1) return false;

        pedido.calcularValorFinal(jogosComprados, cupom);
        long enderecoNovo = arqPedidos.updateNoEndereco(enderecoAntigo, pedido);
        boolean sucesso = enderecoNovo != -1;

        if (sucesso && enderecoNovo != enderecoAntigo) {
            // registro foi realocado (não coube no espaço antigo): reindexa
            indiceId.inserir(pedido.getId(), enderecoNovo);
            indiceCliente.remover(pedido.getIdCliente(), enderecoAntigo);
            indiceCliente.inserir(pedido.getIdCliente(), enderecoNovo);
        }

        return sucesso;
    }

    public boolean excluirPedido(int id) throws Exception {
        long endereco = indiceId.buscar(id);
        if (endereco == -1) return false;

        Pedido pedido = arqPedidos.lerNoEndereco(endereco);
        boolean sucesso = arqPedidos.deleteNoEndereco(endereco);

        if (sucesso) {
            indiceId.remover(id);
            indiceCliente.remover(pedido.getIdCliente(), endereco);
        }

        return sucesso;
    }
}
