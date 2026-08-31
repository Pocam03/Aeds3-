import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
public class ClienteDAO {
    private Arquivo<Cliente> arqClientes;

    public ClienteDAO() throws Exception {
        arqClientes = new Arquivo<>("clientes", Cliente.class.getConstructor());
    }

    public Cliente buscarCliente(int id) throws Exception {
        return arqClientes.read(id);
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
        return arqClientes.create(cliente) > 0;
    }

    public boolean alterarCliente(Cliente cliente) throws Exception {
        return arqClientes.update(cliente);
    }

    public boolean excluirCliente(int id) throws Exception {
        return arqClientes.delete(id);
    }

    public ArrayList<Cliente> listarClientes() throws Exception {
        return arqClientes.readAll();
    }
}
