package model;

import java.io.File;
import java.util.ArrayList;

public class CupomDAO {
    private Arquivo<Cupom> arqCupons;
    private HashExtensivel indiceId; // índice de hash extensível para busca direta por PK (Cupom.id)

    public CupomDAO() throws Exception {
        arqCupons = new Arquivo<>("cupons", Cupom.class.getConstructor());

        String arqDiretorio = "./dados/cupons/idx_id_dir.db";
        String arqBaldes = "./dados/cupons/idx_id_baldes.db";
        boolean indiceNovo = !new File(arqDiretorio).exists();

        indiceId = new HashExtensivel(arqDiretorio, arqBaldes);
        if (indiceNovo) arqCupons.reconstruirIndice(indiceId); // popula o índice sobre dados pré-existentes
    }

    public Cupom buscarCupom(int id) throws Exception {
        long endereco = indiceId.buscar(id);
        if (endereco == -1) return null;
        return arqCupons.lerNoEndereco(endereco);
    }

    public Cupom buscarCupomPorCodigo(String codigo) throws Exception {
        for (Cupom cupom : listarCupons()) {
            if (cupom.getCodigo().equals(codigo)) {
                return cupom;
            }
        }
        return null;
    }

    public boolean incluirCupom(Cupom cupom) throws Exception {
        int id = arqCupons.create(cupom);
        if (id <= 0) return false;
        indiceId.inserir(id, arqCupons.getUltimoEndereco());
        return true;
    }

    public boolean alterarCupom(Cupom cupom) throws Exception {
        long enderecoAntigo = indiceId.buscar(cupom.getId());
        if (enderecoAntigo == -1) return false;

        long enderecoNovo = arqCupons.updateNoEndereco(enderecoAntigo, cupom);
        if (enderecoNovo == -1) return false;

        if (enderecoNovo != enderecoAntigo) {
            indiceId.inserir(cupom.getId(), enderecoNovo); // registro foi realocado: reindexa
        }
        return true;
    }

    public boolean excluirCupom(int id) throws Exception {
        long endereco = indiceId.buscar(id);
        if (endereco == -1) return false;

        boolean sucesso = arqCupons.deleteNoEndereco(endereco);
        if (sucesso) indiceId.remover(id);
        return sucesso;
    }

    public ArrayList<Cupom> listarCupons() throws Exception {
        return arqCupons.readAll();
    }
}
