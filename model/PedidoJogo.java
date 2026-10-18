package model;

import java.io.*;

/**
 * Entidade associativa do relacionamento N:N entre Pedido e Jogo.
 *
 * Cada registro representa um item de um pedido. Além das chaves
 * estrangeiras (idPedido e idJogo), guarda uma cópia do título e do preço do
 * jogo no momento da compra. Por isso, se o jogo for excluído (ou tiver o
 * preço alterado) depois, o pedido já realizado continua íntegro: o item
 * segue existindo com os dados originais e o valor do pedido não muda.
 */
public class PedidoJogo implements Registro {
    private int id;
    private int idPedido;
    private int idJogo;
    private String titulo; // cópia do Jogo.titulo na data da compra
    private float preco;   // cópia do Jogo.preco na data da compra

    public PedidoJogo() {
        this(-1, -1, -1, "", 0F);
    }

    public PedidoJogo(int idPedido, Jogo jogo) {
        this(-1, idPedido, jogo.getId(), jogo.getTitulo(), jogo.getPreco());
    }

    public PedidoJogo(int id, int idPedido, int idJogo, String titulo, float preco) {
        this.id = id;
        this.idPedido = idPedido;
        this.idJogo = idJogo;
        this.titulo = titulo;
        this.preco = preco;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdPedido() { return idPedido; }
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }

    public int getIdJogo() { return idJogo; }
    public void setIdJogo(int idJogo) { this.idJogo = idJogo; }

    public String getTitulo() { return titulo; }
    public float getPreco() { return preco; }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(this.id);
        dos.writeInt(this.idPedido);
        dos.writeInt(this.idJogo);
        dos.writeUTF(this.titulo);
        dos.writeFloat(this.preco);
        return baos.toByteArray();
    }

    public void fromByteArray(byte[] b) throws IOException {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(b));
        this.id = dis.readInt();
        this.idPedido = dis.readInt();
        this.idJogo = dis.readInt();
        this.titulo = dis.readUTF();
        this.preco = dis.readFloat();
    }

    @Override
    public String toString() {
        return idJogo + " - " + titulo + " (R$ " + String.format("%.2f", preco) + ")";
    }
}
