package model;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

public class JogoDAO {
    private static final int TAMANHO_BLOCO_ORDENACAO = 3; // pequeno de propósito, pra forçar várias passadas de intercalação

    private Arquivo<Jogo> arqJogos;
    private HashExtensivel indiceId; // índice de hash extensível para busca direta por PK (Jogo.id)

    public JogoDAO() throws Exception {
        arqJogos = new Arquivo<>("jogos", Jogo.class.getConstructor());

        String arqDiretorio = "./dados/jogos/idx_id_dir.db";
        String arqBaldes = "./dados/jogos/idx_id_baldes.db";
        boolean indiceNovo = !new File(arqDiretorio).exists();

        indiceId = new HashExtensivel(arqDiretorio, arqBaldes);
        if (indiceNovo) arqJogos.reconstruirIndice(indiceId); // popula o índice sobre dados pré-existentes
    }

    public Jogo buscarJogo(int id) throws Exception {
        long endereco = indiceId.buscar(id);
        if (endereco == -1) return null;
        return arqJogos.lerNoEndereco(endereco);
    }

    public boolean incluirJogo(Jogo jogo) throws Exception {
        int id = arqJogos.create(jogo);
        if (id <= 0) return false;
        indiceId.inserir(id, arqJogos.getUltimoEndereco());
        return true;
    }

    public boolean alterarJogo(Jogo jogo) throws Exception {
        long enderecoAntigo = indiceId.buscar(jogo.getId());
        if (enderecoAntigo == -1) return false;

        long enderecoNovo = arqJogos.updateNoEndereco(enderecoAntigo, jogo);
        if (enderecoNovo == -1) return false;

        if (enderecoNovo != enderecoAntigo) {
            indiceId.inserir(jogo.getId(), enderecoNovo); // registro foi realocado: reindexa
        }
        return true;
    }

    public boolean excluirJogo(int id) throws Exception {
        long endereco = indiceId.buscar(id);
        if (endereco == -1) return false;

        boolean sucesso = arqJogos.deleteNoEndereco(endereco);
        if (sucesso) indiceId.remover(id);
        return sucesso;
    }

    public ArrayList<Jogo> listarJogos() throws Exception {
        return arqJogos.readAll();
    }

    /**
     * Ordena jogos.db pelo preço (ordenação externa por intercalação
     * balanceada). O arquivo em disco fica reescrito em ordem crescente de
     * preço, então listarJogos() passa a devolver os jogos já ordenados.
     *
     * Como a reescrita muda o endereço de todo registro, o índice de PK
     * fica obsoleto após a ordenação e precisa ser reconstruído do zero.
     */
    public void ordenarPorPreco() throws Exception {
        arqJogos.ordenarPorIntercalacaoBalanceada(Comparator.comparing(Jogo::getPreco), TAMANHO_BLOCO_ORDENACAO);
        indiceId.limpar();
        arqJogos.reconstruirIndice(indiceId);
    }
}
