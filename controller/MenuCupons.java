package controller;

import java.util.ArrayList;

import model.Cupom;
import model.CupomDAO;
import view.CupomView;

public class MenuCupons {
    private CupomDAO cupomDAO;
    private CupomView view = new CupomView();

    public MenuCupons() throws Exception {
        cupomDAO = new CupomDAO();
    }

    public void menu() {
        int opcao;
        do {
            opcao = view.exibirMenuPrincipal();

            switch (opcao) {
                case 1:
                    buscarCupom();
                    break;
                case 2:
                    incluirCupom();
                    break;
                case 3:
                    alterarCupom();
                    break;
                case 4:
                    excluirCupom();
                    break;
                case 5:
                    listarCupons();
                    break;
                case 0:
                    break;
                default:
                    view.exibirOpcaoInvalidaMenu();
                    break;
            }
        } while (opcao != 0);
    }

    private void buscarCupom() {
        int id = view.lerIdCupom("\nID do cupom: ");
        try {
            Cupom cupom = cupomDAO.buscarCupom(id);
            if (cupom != null) {
                view.exibirCupom(cupom);
            } else {
                view.exibirCupomNaoEncontrado();
            }
        } catch (Exception e) {
            view.exibirMensagem("Erro ao buscar cupom.");
        }
    }

    private void incluirCupom() {
        view.exibirMensagem("\nInclusão de cupom");

        String codigo = view.lerCodigo();
        int valor = view.lerValor();
        int porcentagem = view.lerPorcentagem();

        try {
            Cupom cupom = new Cupom(codigo, valor, porcentagem);
            if (cupomDAO.incluirCupom(cupom)) {
                view.exibirCupom(cupom);
                view.exibirMensagem("Cupom incluído com sucesso.");
            } else {
                view.exibirMensagem("Erro ao incluir cupom.");
            }
        } catch (Exception e) {
            view.exibirMensagem("Erro ao incluir cupom.");
        }
    }

    private void alterarCupom() {
        int id = view.lerIdCupom("\nID do cupom a ser alterado: ");

        try {
            Cupom cupom = cupomDAO.buscarCupom(id);
            if (cupom == null) {
                view.exibirCupomNaoEncontrado();
                return;
            }

            String codigo = view.lerNovoCodigo();
            if (!codigo.isEmpty()) cupom.setCodigo(codigo);

            String valorStr = view.lerNovoValor();
            if (!valorStr.isEmpty()) cupom.setValor(Integer.parseInt(valorStr));

            String porcentagemStr = view.lerNovaPorcentagem();
            if (!porcentagemStr.isEmpty()) cupom.setPorcentagem(Integer.parseInt(porcentagemStr));

            if (cupomDAO.alterarCupom(cupom)) {
                view.exibirMensagem("Cupom alterado com sucesso.");
            } else {
                view.exibirMensagem("Erro ao alterar cupom.");
            }
        } catch (Exception e) {
            view.exibirMensagem("Erro ao alterar cupom.");
        }
    }

    private void excluirCupom() {
        int id = view.lerIdCupom("\nID do cupom a ser excluído: ");

        try {
            Cupom cupom = cupomDAO.buscarCupom(id);
            if (cupom == null) {
                view.exibirCupomNaoEncontrado();
                return;
            }

            char resp = view.lerConfirmacaoExclusao();
            if (resp == 'S' || resp == 's') {
                if (cupomDAO.excluirCupom(id)) {
                    view.exibirMensagem("Cupom excluído com sucesso.");
                } else {
                    view.exibirMensagem("Erro ao excluir cupom.");
                }
            }
        } catch (Exception e) {
            view.exibirMensagem("Erro ao excluir cupom.");
        }
    }

    private void listarCupons() {
        try {
            ArrayList<Cupom> cupons = cupomDAO.listarCupons();

            if (cupons.isEmpty()) {
                view.exibirMensagem("\nNenhum cupom cadastrado.");
                return;
            }

            view.exibirListaCupons(cupons);
        } catch (Exception e) {
            view.exibirMensagem("Erro ao listar cupons.");
        }
    }
}
