package controller;

import java.util.ArrayList;

import model.Jogo;
import model.JogoDAO;
import view.JogoView;

public class MenuJogos {
    private JogoDAO jogoDAO;
    private JogoView view = new JogoView();

    public MenuJogos() throws Exception {
        jogoDAO = new JogoDAO();
    }

    public void menu() {
        int opcao;
        do {
            opcao = view.exibirMenuPrincipal();

            switch (opcao) {
                case 1:
                    buscarJogo();
                    break;
                case 2:
                    incluirJogo();
                    break;
                case 3:
                    alterarJogo();
                    break;
                case 4:
                    excluirJogo();
                    break;
                case 5:
                    listarJogos();
                    break;
                case 6:
                    ordenarJogosPorPreco();
                    break;
                case 0:
                    break;
                default:
                    view.exibirOpcaoInvalidaMenu();
                    break;
            }
        } while (opcao != 0);
    }

    private void buscarJogo() {
        int id = view.lerIdJogo("\nID do jogo: ");
        try {
            Jogo jogo = jogoDAO.buscarJogo(id);
            if (jogo != null) {
                view.exibirJogo(jogo);
            } else {
                view.exibirJogoNaoEncontrado();
            }
        } catch (Exception e) {
            view.exibirMensagem("Erro ao buscar jogo.");
        }
    }

    private void incluirJogo() {
        view.exibirMensagem("\nInclusão de jogo");

        String titulo = view.lerTitulo();
        String desenvolvedora = view.lerDesenvolvedora();
        int classificacao = view.lerClassificacao();
        float preco = view.lerPreco();
        int ano = view.lerAnoLancamento();

        try {
            Jogo jogo = new Jogo(titulo, desenvolvedora, classificacao, preco, ano);
            if (jogoDAO.incluirJogo(jogo)) {
                view.exibirJogo(jogo);
                view.exibirMensagem("Jogo incluído com sucesso.");
            } else {
                view.exibirMensagem("Erro ao incluir jogo.");
            }
        } catch (Exception e) {
            view.exibirMensagem("Erro ao incluir jogo.");
        }
    }

    private void alterarJogo() {
        int id = view.lerIdJogo("\nID do jogo a ser alterado: ");

        try {
            Jogo jogo = jogoDAO.buscarJogo(id);
            if (jogo == null) {
                view.exibirJogoNaoEncontrado();
                return;
            }

            String titulo = view.lerNovoTitulo();
            if (!titulo.isEmpty()) jogo.setTitulo(titulo);

            String desenvolvedora = view.lerNovaDesenvolvedora();
            if (!desenvolvedora.isEmpty()) jogo.setDesenvolvedora(desenvolvedora);

            String classificacaoStr = view.lerNovaClassificacao();
            if (!classificacaoStr.isEmpty()) jogo.setClassificacaoIndicativa(Integer.parseInt(classificacaoStr));

            String precoStr = view.lerNovoPreco();
            if (!precoStr.isEmpty()) jogo.setPreco(Float.parseFloat(precoStr));

            String anoStr = view.lerNovoAno();
            if (!anoStr.isEmpty()) jogo.setAnoLancamento(Integer.parseInt(anoStr));

            if (jogoDAO.alterarJogo(jogo)) {
                view.exibirMensagem("Jogo alterado com sucesso.");
            } else {
                view.exibirMensagem("Erro ao alterar jogo.");
            }
        } catch (Exception e) {
            view.exibirMensagem("Erro ao alterar jogo.");
        }
    }

    private void excluirJogo() {
        int id = view.lerIdJogo("\nID do jogo a ser excluído: ");

        try {
            Jogo jogo = jogoDAO.buscarJogo(id);
            if (jogo == null) {
                view.exibirJogoNaoEncontrado();
                return;
            }

            char resp = view.lerConfirmacaoExclusao();
            if (resp == 'S' || resp == 's') {
                if (jogoDAO.excluirJogo(id)) {
                    view.exibirMensagem("Jogo excluído com sucesso.");
                } else {
                    view.exibirMensagem("Erro ao excluir jogo.");
                }
            }
        } catch (Exception e) {
            view.exibirMensagem("Erro ao excluir jogo.");
        }
    }

    private void listarJogos() {
        try {
            ArrayList<Jogo> jogos = jogoDAO.listarJogos();

            if (jogos.isEmpty()) {
                view.exibirMensagem("\nNenhum jogo cadastrado.");
                return;
            }

            view.exibirListaJogos(jogos);
        } catch (Exception e) {
            view.exibirMensagem("Erro ao listar jogos.");
        }
    }

    private void ordenarJogosPorPreco() {
        try {
            jogoDAO.ordenarPorPreco();
            view.exibirMensagem("\nJogos ordenados por preço (ordenação externa por intercalação balanceada).");
            listarJogos();
        } catch (Exception e) {
            view.exibirMensagem("Erro ao ordenar jogos.");
        }
    }
}
