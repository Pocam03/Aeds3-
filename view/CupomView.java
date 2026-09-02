package view;

import java.util.ArrayList;

import model.Cupom;

public class CupomView extends ConsoleView {

    public int exibirMenuPrincipal() {
        exibirCabecalho("Início > Cupons");
        exibirLinha("\n1 - Buscar");
        exibirLinha("2 - Incluir");
        exibirLinha("3 - Alterar");
        exibirLinha("4 - Excluir");
        exibirLinha("5 - Listar todos os cupons");
        exibirLinha("0 - Voltar");
        return lerOpcao();
    }

    public void exibirOpcaoInvalidaMenu() {
        exibirOpcaoInvalida();
    }

    public int lerIdCupom(String rotulo) {
        return lerInteiro(rotulo);
    }

    public void exibirCupom(Cupom cupom) {
        System.out.println(cupom);
    }

    public void exibirCupomNaoEncontrado() {
        exibirLinha("Cupom não encontrado.");
    }

    public void exibirListaCupons(ArrayList<Cupom> cupons) {
        exibirLinha("\nLista de cupons");
        for (Cupom cupom : cupons) {
            System.out.println(cupom);
            exibirLinha("-------------------------");
        }
    }

    public String lerCodigo() {
        return lerTexto("\nCódigo: ");
    }

    public int lerValor() {
        return lerInteiro("Valor: ");
    }

    public int lerPorcentagem() {
        return lerInteiro("Porcentagem: ");
    }

    public String lerNovoCodigo() {
        return lerTexto("\nNovo código (vazio para manter): ");
    }

    public String lerNovoValor() {
        return lerTexto("Novo valor (vazio para manter): ");
    }

    public String lerNovaPorcentagem() {
        return lerTexto("Nova porcentagem (vazio para manter): ");
    }

    public char lerConfirmacaoExclusao() {
        return lerConfirmacao("Confirma exclusão? (S/N): ");
    }

    public void exibirMensagem(String mensagem) {
        exibirLinha(mensagem);
    }
}
