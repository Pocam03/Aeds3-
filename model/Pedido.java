package model;

import java.time.LocalDate;
import java.io.*;
import java.util.ArrayList;

public class Pedido implements Registro {
    private int id;
    private int idCliente;
    // Os jogos do pedido NÃO ficam neste registro: vivem na entidade associativa
    // Pedido_Jogo (PedidoJogoDAO), com cópia de título/preço da data da compra.
    // Os campos abaixo são preenchidos pelo PedidoDAO ao carregar o pedido.
    private ArrayList<Integer> idJogos;
    private ArrayList<PedidoJogo> itens = new ArrayList<>();
    private int idCupom;       // -1 se não houver cupom
    private float valorFinal;
    private LocalDate data;

    public Pedido() {
        this(-1, -1, new ArrayList<>(), -1, 0F, LocalDate.now());
    }

    public Pedido(int idCliente, ArrayList<Integer> idJogos, int idCupom) {
        this(-1, idCliente, idJogos, idCupom, 0F, LocalDate.now());
    }

    public Pedido(int id, int idCliente, ArrayList<Integer> idJogos, int idCupom, float valorFinal, LocalDate data) {
        this.id = id;
        this.idCliente = idCliente;
        this.idJogos = idJogos;
        this.idCupom = idCupom;
        this.valorFinal = valorFinal;
        this.data = data;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public ArrayList<Integer> getIdJogos() { return idJogos; }
    public void setIdJogos(ArrayList<Integer> idJogos) { this.idJogos = idJogos; }
    public void adicionarJogo(int idJogo) { this.idJogos.add(idJogo); }
    public void removerJogo(int idJogo) { this.idJogos.remove(Integer.valueOf(idJogo)); }

    /** Itens gravados do pedido (com título e preço da época da compra). */
    public ArrayList<PedidoJogo> getItens() { return itens; }
    public void setItens(ArrayList<PedidoJogo> itens) {
        this.itens = itens;
        this.idJogos = new ArrayList<>();
        for (PedidoJogo item : itens) this.idJogos.add(item.getIdJogo());
    }

    public int getIdCupom() { return idCupom; }
    public void setIdCupom(int idCupom) { this.idCupom = idCupom; }

    public float getValorFinal() { return valorFinal; }
    public void setValorFinal(float valorFinal) { this.valorFinal = valorFinal; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    /**
     * Calcula o valor final do pedido somando o preço (da época da compra)
     * dos itens e aplicando o desconto do cupom (se houver).
     * Atualiza o campo valorFinal e retorna o valor calculado.
     */
    public float calcularValorFinal(ArrayList<PedidoJogo> itensComprados, Cupom cupom) {
        float total = 0F;
        for (PedidoJogo item : itensComprados) {
            total += item.getPreco();
        }

        if (cupom != null) {
            if (cupom.getPorcentagem() > 0) {
                total -= total * (cupom.getPorcentagem() / 100F);
            }
            if (cupom.getValor() > 0) {
                total -= cupom.getValor();
            }
            if (total < 0) {
                total = 0F;
            }
        }

        this.valorFinal = total;
        return total;
    }

    // Implementação do método toByteArray()
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(this.id);
        dos.writeInt(this.idCliente);

        dos.writeInt(this.idCupom);
        dos.writeFloat(this.valorFinal);
        dos.writeLong(this.data.toEpochDay());
        return baos.toByteArray();
    }

    // Implementação do método fromByteArray()
    public void fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        this.id = dis.readInt();
        this.idCliente = dis.readInt();

        this.idJogos = new ArrayList<>();
        this.itens = new ArrayList<>();
        this.idCupom = dis.readInt();
        this.valorFinal = dis.readFloat();
        this.data = LocalDate.ofEpochDay(dis.readLong());
    }

    @Override
    public String toString() {
        return "\nID............: " + this.id +
               "\nID Cliente....: " + this.idCliente +
               "\nJogos.........: " + this.itens +
               "\nID Cupom......: " + (this.idCupom == -1 ? "Nenhum" : this.idCupom) +
               "\nValor Final...: " + this.valorFinal +
               "\nData..........: " + this.data;
    }
}
