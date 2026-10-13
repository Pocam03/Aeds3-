package model;

import java.io.File;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Comparator;

public class Arquivo<T extends Registro> {
    private static final int TAM_CABECALHO = 12;
    private RandomAccessFile arquivo;
    private String nomeArquivo;
    private Constructor<T> construtor;
    private long ultimoEndereco = -1; // endereço do último registro gravado por create() ou updateNoEndereco()

    public Arquivo(String nomeArquivo, Constructor<T> construtor) throws Exception {
        File diretorio = new File("./dados");
        if (!diretorio.exists()) diretorio.mkdir();

        diretorio = new File("./dados/" + nomeArquivo);
        if (!diretorio.exists()) diretorio.mkdir();

        this.nomeArquivo = "./dados/" + nomeArquivo + "/" + nomeArquivo + ".db";
        this.construtor = construtor;
        this.arquivo = new RandomAccessFile(this.nomeArquivo, "rw");

        if (arquivo.length() < TAM_CABECALHO) {
            arquivo.writeInt(0);    // Último ID usado
            arquivo.writeLong(-1);  // Lista de registros excluídos
        }
    }

    public int create(T obj) throws Exception {
        arquivo.seek(0);
        int novoID = arquivo.readInt() + 1;
        arquivo.seek(0);
        arquivo.writeInt(novoID);
        obj.setId(novoID);
        byte[] dados = obj.toByteArray();

        long endereco = getDeleted(dados.length);
        if (endereco == -1) {
            arquivo.seek(arquivo.length());
            endereco = arquivo.getFilePointer();
            arquivo.writeByte(' ');  // Lápide
            arquivo.writeShort(dados.length);
            arquivo.write(dados);
        } else {
            arquivo.seek(endereco);
            arquivo.writeByte(' ');  // Remove a lápide
            arquivo.skipBytes(2);
            arquivo.write(dados);
        }
        ultimoEndereco = endereco;
        return obj.getId();
    }

    /**
     * Endereço em que o registro mais recente foi gravado por create() ou
     * updateNoEndereco(). Permite indexar (PK -> endereço) num índice de
     * hash extensível sem precisar de uma varredura extra do arquivo.
     */
    public long getUltimoEndereco() {
        return ultimoEndereco;
    }

    public T read(int id) throws Exception {
        arquivo.seek(TAM_CABECALHO);
        while (arquivo.getFilePointer() < arquivo.length()) {
            long posicao = arquivo.getFilePointer();
            byte lapide = arquivo.readByte();
            short tamanho = arquivo.readShort();
            byte[] dados = new byte[tamanho];
            arquivo.read(dados);

            if (lapide == ' ') {
                T obj = construtor.newInstance();
                obj.fromByteArray(dados);
                if (obj.getId() == id) {
                    return obj;
                }
            }
        }
        return null;
    }

    public boolean delete(int id) throws Exception {
        arquivo.seek(TAM_CABECALHO);
        while (arquivo.getFilePointer() < arquivo.length()) {
            long posicao = arquivo.getFilePointer();
            byte lapide = arquivo.readByte();
            short tamanho = arquivo.readShort();
            byte[] dados = new byte[tamanho];
            arquivo.read(dados);

            if (lapide == ' ') {
                T obj = construtor.newInstance();
                obj.fromByteArray(dados);
                if (obj.getId() == id) {
                    arquivo.seek(posicao);
                    arquivo.writeByte('*');
                    addDeleted(tamanho, posicao);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean update(T novoObj) throws Exception {
        arquivo.seek(TAM_CABECALHO);
        while (arquivo.getFilePointer() < arquivo.length()) {
            long posicao = arquivo.getFilePointer();
            byte lapide = arquivo.readByte();
            short tamanho = arquivo.readShort();
            byte[] dados = new byte[tamanho];
            arquivo.read(dados);

            if (lapide == ' ') {
                T obj = construtor.newInstance();
                obj.fromByteArray(dados);
                if (obj.getId() == novoObj.getId()) {
                    byte[] novosDados = novoObj.toByteArray();
                    short novoTam = (short) novosDados.length;

                    if (novoTam <= tamanho) {
                        arquivo.seek(posicao + 3);
                        arquivo.write(novosDados);
                    } else {
                        arquivo.seek(posicao);
                        arquivo.writeByte('*');
                        addDeleted(tamanho, posicao);

                        long novoEndereco = getDeleted(novosDados.length);
                        if (novoEndereco == -1) {
                            arquivo.seek(arquivo.length());
                            novoEndereco = arquivo.getFilePointer();
                            arquivo.writeByte(' ');
                            arquivo.writeShort(novoTam);
                            arquivo.write(novosDados);
                        } else {
                            arquivo.seek(novoEndereco);
                            arquivo.writeByte(' ');
                            arquivo.skipBytes(2);
                            arquivo.write(novosDados);
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private void addDeleted(int tamanhoEspaco, long enderecoEspaco) throws Exception {
        long posicao = 4;
        arquivo.seek(posicao);
        long endereco = arquivo.readLong();
        long proximo;

        if (endereco == -1) {
            arquivo.seek(4);
            arquivo.writeLong(enderecoEspaco);
            arquivo.seek(enderecoEspaco + 3);
            arquivo.writeLong(-1);
        } else {
            do {
                arquivo.seek(endereco + 1);
                int tamanho = arquivo.readShort();
                proximo = arquivo.readLong();

                if (tamanho > tamanhoEspaco) {
                    if (posicao == 4)
                        arquivo.seek(posicao);
                    else
                        arquivo.seek(posicao + 3);
                    arquivo.writeLong(enderecoEspaco);
                    arquivo.seek(enderecoEspaco + 3);
                    arquivo.writeLong(endereco);
                    break;
                }

                if (proximo == -1) {
                    arquivo.seek(endereco + 3);
                    arquivo.writeLong(enderecoEspaco);
                    arquivo.seek(enderecoEspaco + 3);
                    arquivo.writeLong(-1);
                    break;
                }

                posicao = endereco;
                endereco = proximo;
            } while (endereco != -1);
        }
    }

    private long getDeleted(int tamanhoNecessario) throws Exception {
        long posicao = 4;
        arquivo.seek(posicao);
        long endereco = arquivo.readLong();
        long proximo;
        int tamanho;

        while (endereco != -1) {
            arquivo.seek(endereco + 1);
            tamanho = arquivo.readShort();
            proximo = arquivo.readLong();

            if (tamanho > tamanhoNecessario) {
                if (posicao == 4)
                    arquivo.seek(posicao);
                else
                    arquivo.seek(posicao + 3);
                arquivo.writeLong(proximo);
                return endereco;
            }
            posicao = endereco;
            endereco = proximo;
        }
        return -1;
    }

    public long getEndereco(int id) throws Exception {
        arquivo.seek(TAM_CABECALHO);
        while (arquivo.getFilePointer() < arquivo.length()) {
            long posicao = arquivo.getFilePointer();
            byte lapide = arquivo.readByte();
            short tamanho = arquivo.readShort();
            byte[] dados = new byte[tamanho];
            arquivo.read(dados);

            if (lapide == ' ') {
                T obj = construtor.newInstance();
                obj.fromByteArray(dados);
                if (obj.getId() == id) {
                    return posicao;
                }
            }
        }
        return -1;
    }

    public T lerNoEndereco(long endereco) throws Exception {
        arquivo.seek(endereco);
        arquivo.readByte(); // lápide (assume-se registro ativo)
        short tamanho = arquivo.readShort();
        byte[] dados = new byte[tamanho];
        arquivo.read(dados);
        T obj = construtor.newInstance();
        obj.fromByteArray(dados);
        return obj;
    }

    /**
     * Versão de update() que recebe diretamente o endereço do registro (obtido
     * por um índice, ex.: HashExtensivel), evitando a varredura sequencial que
     * update(T) precisa fazer para localizar o registro pelo ID.
     *
     * Retorna o endereço final do registro: igual a `endereco` quando o novo
     * conteúdo coube no espaço original, ou o endereço realocado quando não
     * coube (mesma lógica de reaproveitamento/realocação de update()). Quem
     * chama este método é responsável por reindexar a chave caso o endereço
     * retornado seja diferente do original. Retorna -1 se não houver registro
     * ativo no endereço informado.
     */
    public long updateNoEndereco(long endereco, T novoObj) throws Exception {
        arquivo.seek(endereco);
        byte lapide = arquivo.readByte();
        if (lapide != ' ') return -1;
        short tamanho = arquivo.readShort();

        byte[] novosDados = novoObj.toByteArray();
        short novoTam = (short) novosDados.length;

        if (novoTam <= tamanho) {
            arquivo.seek(endereco + 3);
            arquivo.write(novosDados);
            ultimoEndereco = endereco;
            return endereco;
        }

        arquivo.seek(endereco);
        arquivo.writeByte('*');
        addDeleted(tamanho, endereco);

        long novoEndereco = getDeleted(novosDados.length);
        if (novoEndereco == -1) {
            arquivo.seek(arquivo.length());
            novoEndereco = arquivo.getFilePointer();
            arquivo.writeByte(' ');
            arquivo.writeShort(novoTam);
            arquivo.write(novosDados);
        } else {
            arquivo.seek(novoEndereco);
            arquivo.writeByte(' ');
            arquivo.skipBytes(2);
            arquivo.write(novosDados);
        }
        ultimoEndereco = novoEndereco;
        return novoEndereco;
    }

    /**
     * Versão de delete() que recebe diretamente o endereço do registro
     * (obtido por um índice), evitando a varredura sequencial de delete(int).
     */
    public boolean deleteNoEndereco(long endereco) throws Exception {
        arquivo.seek(endereco);
        byte lapide = arquivo.readByte();
        if (lapide != ' ') return false;
        short tamanho = arquivo.readShort();

        arquivo.seek(endereco);
        arquivo.writeByte('*');
        addDeleted(tamanho, endereco);
        return true;
    }

    /**
     * Popula um índice de hash extensível com (PK, endereço) de todos os
     * registros ativos do arquivo. Usado para construir o índice na primeira
     * vez em que ele passa a existir sobre um arquivo de dados já povoado, e
     * para reconstruí-lo depois de operações que reescrevem o arquivo de
     * dados por inteiro (ex.: ordenarPorIntercalacaoBalanceada), quando todos
     * os endereços antigos ficam inválidos.
     */
    public void reconstruirIndice(HashExtensivel indice) throws Exception {
        arquivo.seek(TAM_CABECALHO);
        while (arquivo.getFilePointer() < arquivo.length()) {
            long posicao = arquivo.getFilePointer();
            byte lapide = arquivo.readByte();
            short tamanho = arquivo.readShort();
            byte[] dados = new byte[tamanho];
            arquivo.read(dados);

            if (lapide == ' ') {
                T obj = construtor.newInstance();
                obj.fromByteArray(dados);
                indice.inserir(obj.getId(), posicao);
            }
        }
    }

    public ArrayList<T> readAll() throws Exception {
        ArrayList<T> lista = new ArrayList<>();

        arquivo.seek(TAM_CABECALHO);

        while (arquivo.getFilePointer() < arquivo.length()) {
            byte lapide = arquivo.readByte();
            int tamanho = arquivo.readUnsignedShort();

            byte[] dados = new byte[tamanho];
            arquivo.readFully(dados);

            // Só adiciona registros que não foram excluídos
            if (lapide == ' ') {
                T obj = construtor.newInstance();
                obj.fromByteArray(dados);
                lista.add(obj);
            }
        }

        return lista;
    }

    /**
     * Ordenação externa por intercalação balanceada (2 vias).
     *
     * Fase de distribuição: os registros ativos são lidos em blocos de
     * tamanhoBloco (o suficiente para caber em memória e ser ordenado ali),
     * cada bloco é ordenado e gravado como uma "corrida" alternando entre
     * dois arquivos temporários (A0/A1).
     *
     * Fase de intercalação: a cada passada, pares de corridas de A0/A1 são
     * intercalados em corridas do dobro do tamanho, gravadas alternando
     * entre outros dois arquivos temporários (B0/B1); os papéis de origem e
     * destino se invertem a cada passada, até restar uma única corrida com
     * todos os registros — que então substitui o conteúdo do arquivo.
     */
    public void ordenarPorIntercalacaoBalanceada(Comparator<T> comparador, int tamanhoBloco) throws Exception {
        ArrayList<T> registros = readAll();
        if (registros.size() <= 1) return;

        String pasta = new File(nomeArquivo).getParent();
        String fA0 = pasta + "/tmp_ord_a0.db";
        String fA1 = pasta + "/tmp_ord_a1.db";
        String fB0 = pasta + "/tmp_ord_b0.db";
        String fB1 = pasta + "/tmp_ord_b1.db";

        int tamanhoCorrida = distribuirEmCorridas(registros, comparador, tamanhoBloco, fA0, fA1);

        String origemA = fA0, origemB = fA1, destinoA = fB0, destinoB = fB1;
        while (tamanhoCorrida < registros.size()) {
            intercalarPasso(origemA, origemB, destinoA, destinoB, comparador, tamanhoCorrida);
            tamanhoCorrida *= 2;

            String tmp;
            tmp = origemA; origemA = destinoA; destinoA = tmp;
            tmp = origemB; origemB = destinoB; destinoB = tmp;
        }

        regravarOrdenado(origemA);

        new File(fA0).delete();
        new File(fA1).delete();
        new File(fB0).delete();
        new File(fB1).delete();
    }

    private void escreverRegistroTemp(RandomAccessFile raf, T obj) throws Exception {
        byte[] dados = obj.toByteArray();
        raf.writeShort(dados.length);
        raf.write(dados);
    }

    private T lerRegistroTemp(RandomAccessFile raf) throws Exception {
        if (raf.getFilePointer() >= raf.length()) return null;
        short tamanho = raf.readShort();
        byte[] dados = new byte[tamanho];
        raf.readFully(dados);
        T obj = construtor.newInstance();
        obj.fromByteArray(dados);
        return obj;
    }

    private int distribuirEmCorridas(ArrayList<T> registros, Comparator<T> comparador, int tamanhoBloco,
                                      String arqA, String arqB) throws Exception {
        new File(arqA).delete();
        new File(arqB).delete();
        RandomAccessFile rafA = new RandomAccessFile(arqA, "rw");
        RandomAccessFile rafB = new RandomAccessFile(arqB, "rw");

        boolean paraA = true;
        for (int inicio = 0; inicio < registros.size(); inicio += tamanhoBloco) {
            int fim = Math.min(inicio + tamanhoBloco, registros.size());
            ArrayList<T> bloco = new ArrayList<>(registros.subList(inicio, fim));
            bloco.sort(comparador);

            RandomAccessFile destino = paraA ? rafA : rafB;
            for (T obj : bloco) {
                escreverRegistroTemp(destino, obj);
            }
            paraA = !paraA;
        }

        rafA.close();
        rafB.close();
        return tamanhoBloco;
    }

    private void intercalarPasso(String origemA, String origemB, String destinoA, String destinoB,
                                  Comparator<T> comparador, int tamanhoCorrida) throws Exception {
        new File(destinoA).delete();
        new File(destinoB).delete();
        RandomAccessFile rafOrigemA = new RandomAccessFile(origemA, "r");
        RandomAccessFile rafOrigemB = new RandomAccessFile(origemB, "r");
        RandomAccessFile rafDestinoA = new RandomAccessFile(destinoA, "rw");
        RandomAccessFile rafDestinoB = new RandomAccessFile(destinoB, "rw");

        boolean paraA = true;
        while (rafOrigemA.getFilePointer() < rafOrigemA.length() || rafOrigemB.getFilePointer() < rafOrigemB.length()) {
            RandomAccessFile destino = paraA ? rafDestinoA : rafDestinoB;
            intercalarUmaCorrida(rafOrigemA, rafOrigemB, destino, comparador, tamanhoCorrida);
            paraA = !paraA;
        }

        rafOrigemA.close();
        rafOrigemB.close();
        rafDestinoA.close();
        rafDestinoB.close();
    }

    private void intercalarUmaCorrida(RandomAccessFile a, RandomAccessFile b, RandomAccessFile destino,
                                       Comparator<T> comparador, int tamanhoCorrida) throws Exception {
        int lidosA = 0, lidosB = 0;
        T atualA = lerRegistroTemp(a);
        lidosA = atualA != null ? 1 : lidosA;
        T atualB = lerRegistroTemp(b);
        lidosB = atualB != null ? 1 : lidosB;

        while (atualA != null || atualB != null) {
            if (atualA != null && (atualB == null || comparador.compare(atualA, atualB) <= 0)) {
                escreverRegistroTemp(destino, atualA);
                atualA = (lidosA < tamanhoCorrida) ? lerRegistroTemp(a) : null;
                lidosA++;
            } else {
                escreverRegistroTemp(destino, atualB);
                atualB = (lidosB < tamanhoCorrida) ? lerRegistroTemp(b) : null;
                lidosB++;
            }
        }
    }

    private void regravarOrdenado(String arquivoOrdenado) throws Exception {
        RandomAccessFile raf = new RandomAccessFile(arquivoOrdenado, "r");

        arquivo.seek(0);
        int ultimoId = arquivo.readInt();

        arquivo.setLength(0);
        arquivo.seek(0);
        arquivo.writeInt(ultimoId);
        arquivo.writeLong(-1); // lista de excluídos fica vazia: arquivo foi recompactado

        T obj;
        while ((obj = lerRegistroTemp(raf)) != null) {
            byte[] dados = obj.toByteArray();
            arquivo.writeByte(' ');
            arquivo.writeShort(dados.length);
            arquivo.write(dados);
        }

        raf.close();
    }

    public void close() throws Exception {
        arquivo.close();
    }
}
