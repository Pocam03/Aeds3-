package model;

import java.io.File;
import java.util.ArrayList;

public class ClienteDAO {
    private Arquivo<Cliente> arqClientes;
    private HashExtensivel indiceId; // índice de hash extensível para busca direta por PK (Cliente.id)

    public ClienteDAO() throws Exception {
        arqClientes = new Arquivo<>("clientes", Cliente.class.getConstructor());

        String arqDiretorio = "./dados/clientes/idx_id_dir.db";
        String arqBaldes = "./dados/clientes/idx_id_baldes.db";
        boolean indiceNovo = !new File(arqDiretorio).exists();

        indiceId = new HashExtensivel(arqDiretorio, arqBaldes);
        if (indiceNovo) arqClientes.reconstruirIndice(indiceId); // popula o índice sobre dados pré-existentes
    }

    public Cliente buscarCliente(int id) throws Exception {
        long endereco = indiceId.buscar(id);
        if (endereco == -1) return null;
        return arqClientes.lerNoEndereco(endereco);
    }

    public Cliente buscarClientePorCpf(String cpf) throws Exception {
        ArrayList<Cliente> clientes = arqClientes.readAll();
        for (Cliente cliente : clientes) {
            if (cliente.getCpf().equals(cpf)) {
                return cliente;
            }
        }
        return null;
    }

    public boolean incluirCliente(Cliente cliente) throws Exception {
        int id = arqClientes.create(cliente);
        if (id <= 0) return false;
        indiceId.inserir(id, arqClientes.getUltimoEndereco());
        return true;
    }

    public boolean alterarCliente(Cliente cliente) throws Exception {
        long enderecoAntigo = indiceId.buscar(cliente.getId());
        if (enderecoAntigo == -1) return false;

        long enderecoNovo = arqClientes.updateNoEndereco(enderecoAntigo, cliente);
        if (enderecoNovo == -1) return false;

        if (enderecoNovo != enderecoAntigo) {
            indiceId.inserir(cliente.getId(), enderecoNovo); // registro foi realocado: reindexa
        }
        return true;
    }

    public boolean excluirCliente(int id) throws Exception {
        long endereco = indiceId.buscar(id);
        if (endereco == -1) return false;

        boolean sucesso = arqClientes.deleteNoEndereco(endereco);
        if (sucesso) indiceId.remover(id);
        return sucesso;
    }

    public ArrayList<Cliente> listarClientes() throws Exception {
        return arqClientes.readAll();
    }
}
