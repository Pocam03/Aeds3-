package model;

import java.util.ArrayList;

public class PedidoDAO {
    private Arquivo<Pedido> arqPedidos;
    private JogoDAO jogoDAO;
    private CupomDAO cupomDAO;

    public PedidoDAO() throws Exception {
        arqPedidos = new Arquivo<>("pedidos", Pedido.class.getConstructor());
        jogoDAO = new JogoDAO();
        cupomDAO = new CupomDAO();
    }

    public Pedido buscarPedido(int id) throws Exception {
        return arqPedidos.read(id);
    }

    public ArrayList<Pedido> listarPedidos() throws Exception {
        return arqPedidos.readAll();
    }

    public ArrayList<Pedido> listarPedidosPorCliente(int idCliente) throws Exception {
        ArrayList<Pedido> pedidosCliente = new ArrayList<>();
        for (Pedido pedido : listarPedidos()) {
            if (pedido.getIdCliente() == idCliente) {
                pedidosCliente.add(pedido);
            }
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

        return arqPedidos.create(pedido) > 0 ? pedido : null;
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

        pedido.calcularValorFinal(jogosComprados, cupom);
        return arqPedidos.update(pedido);
    }

    public boolean excluirPedido(int id) throws Exception {
        return arqPedidos.delete(id);
    }
}