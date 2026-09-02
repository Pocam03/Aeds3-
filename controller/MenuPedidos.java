package controller;

import java.util.ArrayList;

import model.Cliente;
import model.ClienteDAO;
import model.Cupom;
import model.CupomDAO;
import model.Jogo;
import model.JogoDAO;
import model.Pedido;
import model.PedidoDAO;
import view.PedidoView;

public class MenuPedidos {
    private PedidoDAO pedidoDAO;
    private ClienteDAO clienteDAO;
    private JogoDAO jogoDAO;
    private CupomDAO cupomDAO;
    private PedidoView view = new PedidoView();

    public MenuPedidos() throws Exception {
        pedidoDAO = new PedidoDAO();
        clienteDAO = new ClienteDAO();
        jogoDAO = new JogoDAO();
        cupomDAO = new CupomDAO();
    }

    public void menu(boolean admin) {
        if (admin) {
            menuAdmin();
        } else {
            menuCliente();
        }
    }

    private void menuAdmin() {
        int opcao;
        do {
            opcao = view.exibirMenuAdmin();

            switch (opcao) {
                case 1:
                    buscarPedido();
                    break;
                case 2:
                    listarPedidos();
                    break;
                case 3:
                    listarPedidosCliente();
                    break;
                case 4:
                    excluirPedido();
                    break;
                case 0:
                    break;
                default:
                    view.exibirOpcaoInvalidaMenu();
                    break;
            }
        } while (opcao != 0);
    }

    private void menuCliente() {
        int opcao;
        do {
            opcao = view.exibirMenuCliente();

            switch (opcao) {
                case 1:
                    buscarPedido();
                    break;
                case 2:
                    listarPedidosCliente();
                    break;
                case 3:
                    incluirPedido();
                    break;
                case 4:
                    alterarPedido();
                    break;
                case 5:
                    excluirPedido();
                    break;
                case 0:
                    break;
                default:
                    view.exibirOpcaoInvalidaMenu();
                    break;
            }
        } while (opcao != 0);
    }

    private void listarPedidosCliente() {
        String cpf = view.lerCpfCliente();
        try {
            Cliente cliente = clienteDAO.buscarClientePorCpf(cpf);
            if (cliente == null) {
                view.exibirClienteNaoEncontrado();
                return;
            }

            ArrayList<Pedido> pedidos = pedidoDAO.listarPedidosPorCliente(cliente.getId());
            if (pedidos.isEmpty()) {
                view.exibirNenhumPedidoDoCliente();
                return;
            }

            view.exibirPedidosDoCliente(pedidos);
        } catch (Exception e) {
            view.exibirMensagem("Erro ao buscar pedidos.");
        }
    }

    private void buscarPedido() {
        int id = view.lerIdPedido("\nID do pedido: ");
        try {
            Pedido pedido = pedidoDAO.buscarPedido(id);
            if (pedido != null) {
                view.exibirPedido(pedido);
            } else {
                view.exibirPedidoNaoEncontrado();
            }
        } catch (Exception e) {
            view.exibirMensagem("Erro ao buscar pedido.");
        }
    }

    private void incluirPedido() {
        view.exibirMensagem("\nInclusão de pedido");

        try {
            String cpf = view.lerCpfCliente();

            Cliente cliente = clienteDAO.buscarClientePorCpf(cpf);
            if (cliente == null) {
                view.exibirClienteNaoEncontradoPedidoCancelado();
                return;
            }
            int idCliente = cliente.getId();

            ArrayList<Integer> idJogos = new ArrayList<>();
            boolean adicionando = true;
            while (adicionando) {
                int idJogo = view.lerIdJogoAdicionar();

                Jogo jogo = jogoDAO.buscarJogo(idJogo);
                if (jogo == null) {
                    view.exibirJogoNaoEncontrado();
                } else {
                    idJogos.add(idJogo);
                    view.exibirJogoAdicionado(jogo.getTitulo());
                }

                char resp = view.lerAdicionarOutroJogo();
                adicionando = (resp == 'S' || resp == 's');
            }

            if (idJogos.isEmpty()) {
                view.exibirNenhumJogoAdicionado();
                return;
            }

            int idCupom = -1;
            char respCupom = view.lerAplicarCupom();
            if (respCupom == 'S' || respCupom == 's') {
                String codigoCupom = view.lerCodigoCupom();

                Cupom cupom = cupomDAO.buscarCupomPorCodigo(codigoCupom);
                if (cupom != null) {
                    idCupom = cupom.getId();
                } else {
                    view.exibirCupomNaoEncontradoSemDesconto();
                    idCupom = -1;
                }
            }

            Pedido pedido = pedidoDAO.incluirPedido(idCliente, idJogos, idCupom);
            if (pedido != null) {
                view.exibirPedidoIncluidoComSucesso(pedido.getValorFinal());
            } else {
                view.exibirMensagem("Erro ao incluir pedido.");
            }
        } catch (Exception e) {
            view.exibirMensagem("Erro ao incluir pedido.");
        }
    }

    private void alterarPedido() {
        int id = view.lerIdPedido("\nID do pedido a ser alterado: ");

        try {
            Pedido pedido = pedidoDAO.buscarPedido(id);
            if (pedido == null) {
                view.exibirPedidoNaoEncontrado();
                return;
            }

            view.exibirJogosAtuais(pedido.getIdJogos());
            char respAdd = view.lerAdicionarNovoJogo();
            if (respAdd == 'S' || respAdd == 's') {
                int idJogo = view.lerIdJogoAdicionar();

                Jogo jogo = jogoDAO.buscarJogo(idJogo);
                if (jogo != null) {
                    pedido.adicionarJogo(idJogo);
                    view.exibirJogoAdicionado(jogo.getTitulo());
                } else {
                    view.exibirJogoNaoEncontrado();
                }
            }

            char respRem = view.lerRemoverJogo();
            if (respRem == 'S' || respRem == 's') {
                int idJogo = view.lerIdJogoRemover();
                pedido.removerJogo(idJogo);
                view.exibirJogoRemovido();
            }

            char respCupom = view.lerAlterarCupom();
            if (respCupom == 'S' || respCupom == 's') {
                String codigoCupom = view.lerNovoCodigoCupom();

                if (codigoCupom.isEmpty()) {
                    pedido.setIdCupom(-1);
                } else {
                    Cupom cupom = cupomDAO.buscarCupomPorCodigo(codigoCupom);
                    if (cupom != null) {
                        pedido.setIdCupom(cupom.getId());
                    } else {
                        view.exibirCupomNaoEncontradoNaoAlterado();
                    }
                }
            }

            if (pedidoDAO.alterarPedido(pedido)) {
                view.exibirPedidoAlteradoComSucesso(pedido.getValorFinal());
            } else {
                view.exibirMensagem("Erro ao alterar pedido.");
            }
        } catch (Exception e) {
            view.exibirMensagem("Erro ao alterar pedido.");
        }
    }

    private void excluirPedido() {
        int id = view.lerIdPedido("\nID do pedido a ser excluído: ");

        try {
            Pedido pedido = pedidoDAO.buscarPedido(id);
            if (pedido == null) {
                view.exibirPedidoNaoEncontrado();
                return;
            }

            char resp = view.lerConfirmacaoExclusao();
            if (resp == 'S' || resp == 's') {
                if (pedidoDAO.excluirPedido(id)) {
                    view.exibirMensagem("Pedido excluído com sucesso.");
                } else {
                    view.exibirMensagem("Erro ao excluir pedido.");
                }
            }
        } catch (Exception e) {
            view.exibirMensagem("Erro ao excluir pedido.");
        }
    }

    private void listarPedidos() {
        try {
            ArrayList<Pedido> pedidos = pedidoDAO.listarPedidos();

            if (pedidos.isEmpty()) {
                view.exibirMensagem("\nNenhum pedido cadastrado.");
                return;
            }

            view.exibirListaPedidos(pedidos);
        } catch (Exception e) {
            view.exibirMensagem("Erro ao listar pedidos.");
        }
    }
}
