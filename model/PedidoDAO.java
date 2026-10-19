package model;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PedidoDAO {
    private Arquivo<Pedido> arqPedidos;
    private ClienteDAO clienteDAO;
    private JogoDAO jogoDAO;
    private CupomDAO cupomDAO;
    private PedidoJogoDAO pedidoJogoDAO; // entidade associativa Pedido_Jogo (N:N com Jogo)
    private ArvoreBMais indiceCliente; // índice secundário sobre a FK Pedido.idCliente
    private HashExtensivel indiceId;   // índice de hash extensível para busca direta por PK (Pedido.id)

    public PedidoDAO() throws Exception {
        arqPedidos = new Arquivo<>("pedidos", Pedido.class.getConstructor());
        clienteDAO = new ClienteDAO();
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

    /**
     * Agrupa a lista de IDs de jogos em (idJogo -> quantidade): cada ID
     * repetido na lista é uma unidade a mais do mesmo jogo. Como
     * (idPedido, idJogo) é chave composta, cada jogo vira um único item.
     */
    private static LinkedHashMap<Integer, Integer> contarQuantidades(List<Integer> idJogos) {
        LinkedHashMap<Integer, Integer> quantidades = new LinkedHashMap<>();
        for (int idJogo : idJogos) quantidades.merge(idJogo, 1, Integer::sum);
        return quantidades;
    }

    /*
     Cria um novo pedido a partir do cliente, da lista de IDs de jogos
     e do ID de um cupom. Busca os jogos e o cupom, calcula o valor final e grava o pedido.
     */
    public Pedido incluirPedido(int idCliente, ArrayList<Integer> idJogos, int idCupom) throws Exception {
        // integridade referencial: o pedido precisa de um cliente existente
        if (clienteDAO.buscarCliente(idCliente) == null) return null;

        // Jogos existentes agora (com a quantidade de cada um); título e preço
        // são copiados para Pedido_Jogo.
        LinkedHashMap<Jogo, Integer> jogosComprados = new LinkedHashMap<>();
        for (Map.Entry<Integer, Integer> e : contarQuantidades(idJogos).entrySet()) {
            Jogo jogo = jogoDAO.buscarJogo(e.getKey());
            if (jogo != null) {
                jogosComprados.put(jogo, e.getValue());
            }
        }
        if (jogosComprados.isEmpty()) return null; // pedido sem nenhum jogo válido

        Cupom cupom = null;
        if (idCupom != -1) {
            cupom = cupomDAO.buscarCupom(idCupom);
        }

        Pedido pedido = new Pedido(idCliente, new ArrayList<>(), idCupom);
        // valor calculado a partir dos mesmos preços que serão gravados nos itens
        ArrayList<PedidoJogo> previa = new ArrayList<>();
        for (Map.Entry<Jogo, Integer> e : jogosComprados.entrySet()) previa.add(new PedidoJogo(-1, e.getKey(), e.getValue()));
        pedido.calcularValorFinal(previa, cupom);

        if (arqPedidos.create(pedido) <= 0) return null;

        long endereco = arqPedidos.getUltimoEndereco();
        indiceId.inserir(pedido.getId(), endereco);
        indiceCliente.inserir(idCliente, endereco);

        ArrayList<PedidoJogo> itens = new ArrayList<>();
        for (Map.Entry<Jogo, Integer> e : jogosComprados.entrySet()) {
            itens.add(pedidoJogoDAO.incluir(pedido.getId(), e.getKey(), e.getValue()));
        }
        pedido.setItens(itens);

        return pedido;
    }

    /**
     * Atualiza um pedido já existente. Os jogos são sincronizados com
     * Pedido_Jogo: itens que continuam no pedido são mantidos (inclusive os
     * de jogos já excluídos do catálogo, com o preço original), só com a
     * quantidade atualizada; itens removidos são apagados e jogos novos
     * entram com título e preço atuais. O valor final é recalculado a partir
     * dos preços gravados.
     */
    public boolean alterarPedido(Pedido pedido) throws Exception {
        long enderecoAntigo = indiceId.buscar(pedido.getId());
        if (enderecoAntigo == -1) return false;

        ArrayList<PedidoJogo> existentes = pedidoJogoDAO.listarPorPedido(pedido.getId());
        ArrayList<PedidoJogo> mantidos = new ArrayList<>();
        ArrayList<PedidoJogo> alterados = new ArrayList<>(); // mantidos com quantidade diferente
        LinkedHashMap<Jogo, Integer> novos = new LinkedHashMap<>();

        for (Map.Entry<Integer, Integer> e : contarQuantidades(pedido.getIdJogos()).entrySet()) {
            int idJogo = e.getKey();
            int quantidade = e.getValue();
            PedidoJogo igual = null;
            for (PedidoJogo item : existentes) {
                if (item.getIdJogo() == idJogo) { igual = item; break; }
            }
            if (igual != null) {
                existentes.remove(igual);
                if (igual.getQuantidade() != quantidade) {
                    igual.setQuantidade(quantidade);
                    alterados.add(igual);
                }
                mantidos.add(igual);
            } else {
                Jogo jogo = jogoDAO.buscarJogo(idJogo);
                if (jogo != null) novos.put(jogo, quantidade);
            }
        }
        // o que sobrou em "existentes" foi removido do pedido
        if (mantidos.isEmpty() && novos.isEmpty()) return false; // pedido não pode ficar sem jogos

        Cupom cupom = null;
        if (pedido.getIdCupom() != -1) {
            cupom = cupomDAO.buscarCupom(pedido.getIdCupom());
        }

        ArrayList<PedidoJogo> itensFinais = new ArrayList<>(mantidos);
        for (Map.Entry<Jogo, Integer> e : novos.entrySet()) itensFinais.add(new PedidoJogo(-1, e.getKey(), e.getValue()));
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
        for (PedidoJogo alterado : alterados) pedidoJogoDAO.alterar(alterado);
        ArrayList<PedidoJogo> gravados = new ArrayList<>(mantidos);
        for (Map.Entry<Jogo, Integer> e : novos.entrySet()) gravados.add(pedidoJogoDAO.incluir(pedido.getId(), e.getKey(), e.getValue()));
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
