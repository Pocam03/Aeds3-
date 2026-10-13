package model;

import java.io.File;
import java.io.RandomAccessFile;

/**
 * Índice de Hash Extensível para busca direta por chave primária (PK).
 * Chave: int (o ID sequencial gerado por Arquivo.create). Referência: long
 * (endereço do registro correspondente no arquivo de dados, para acesso
 * direto via Arquivo.lerNoEndereco). Como a PK é única, cada chave tem no
 * máximo uma referência associada.
 *
 * Persistência em dois arquivos binários:
 *  - arquivo de diretório: profundidade global seguida do vetor de
 *    endereços de baldes (2^profundidadeGlobal posições). Como o vetor
 *    cresce a cada duplicação, o arquivo inteiro é reescrito sempre que o
 *    diretório muda — ele é pequeno, então o custo é desprezível.
 *  - arquivo de baldes: baldes de tamanho fixo (profundidadeLocal, número
 *    de entradas ocupadas e um vetor de pares chave/referência).
 *
 * A função de hash usada é a identidade sobre os bits da própria chave: como
 * as PKs são inteiros sequenciais, os bits menos significativos já
 * distribuem bem as chaves entre os baldes.
 *
 * Simplificação assumida (mesma da ArvoreBMais): a remoção apaga a entrada
 * do balde mas não funde baldes subdimensionados nem reduz a profundidade
 * global. Isso não compromete a corretude da busca, só a compactação do
 * índice ao longo do tempo.
 */
public class HashExtensivel {
    private static final int FATOR_BALDE = 4; // capacidade (nº de entradas) de cada balde
    private static final int TAM_ENTRADA = 4 + 8; // chave (int) + referência (long)
    private static final int TAM_BALDE = 4 + 4 + FATOR_BALDE * TAM_ENTRADA; // profundidadeLocal + n + entradas

    private RandomAccessFile arqDiretorio;
    private RandomAccessFile arqBaldes;
    private int profundidadeGlobal;
    private long[] diretorio;

    private static class Balde {
        long endereco;
        int profundidadeLocal;
        int n;
        int[] chaves = new int[FATOR_BALDE];
        long[] referencias = new long[FATOR_BALDE];
    }

    public HashExtensivel(String nomeArquivoDiretorio, String nomeArquivoBaldes) throws Exception {
        File dirPai = new File(nomeArquivoDiretorio).getParentFile();
        if (dirPai != null && !dirPai.exists()) dirPai.mkdirs();

        this.arqDiretorio = new RandomAccessFile(nomeArquivoDiretorio, "rw");
        this.arqBaldes = new RandomAccessFile(nomeArquivoBaldes, "rw");

        if (arqDiretorio.length() == 0) {
            profundidadeGlobal = 0;
            long enderecoBalde = novoBalde(0);
            diretorio = new long[1];
            diretorio[0] = enderecoBalde;
            gravarDiretorio();
        } else {
            carregarDiretorio();
        }
    }

    private void carregarDiretorio() throws Exception {
        arqDiretorio.seek(0);
        profundidadeGlobal = arqDiretorio.readInt();
        int tamanho = 1 << profundidadeGlobal;
        diretorio = new long[tamanho];
        for (int i = 0; i < tamanho; i++) {
            diretorio[i] = arqDiretorio.readLong();
        }
    }

    private void gravarDiretorio() throws Exception {
        arqDiretorio.setLength(0);
        arqDiretorio.seek(0);
        arqDiretorio.writeInt(profundidadeGlobal);
        for (long endereco : diretorio) {
            arqDiretorio.writeLong(endereco);
        }
    }

    private int hash(int chave) {
        return chave & 0x7FFFFFFF; // garante não-negativo; bits baixos usados na indexação
    }

    private int posicaoDiretorio(int chave) {
        int mascara = (1 << profundidadeGlobal) - 1;
        return hash(chave) & mascara;
    }

    private Balde lerBalde(long endereco) throws Exception {
        arqBaldes.seek(endereco);
        Balde b = new Balde();
        b.endereco = endereco;
        b.profundidadeLocal = arqBaldes.readInt();
        b.n = arqBaldes.readInt();
        for (int i = 0; i < FATOR_BALDE; i++) {
            b.chaves[i] = arqBaldes.readInt();
            b.referencias[i] = arqBaldes.readLong();
        }
        return b;
    }

    private void escreverBalde(Balde b) throws Exception {
        arqBaldes.seek(b.endereco);
        arqBaldes.writeInt(b.profundidadeLocal);
        arqBaldes.writeInt(b.n);
        for (int i = 0; i < FATOR_BALDE; i++) {
            arqBaldes.writeInt(b.chaves[i]);
            arqBaldes.writeLong(b.referencias[i]);
        }
    }

    private long novoBalde(int profundidadeLocal) throws Exception {
        long endereco = arqBaldes.length();
        Balde b = new Balde();
        b.endereco = endereco;
        b.profundidadeLocal = profundidadeLocal;
        b.n = 0;
        for (int i = 0; i < FATOR_BALDE; i++) {
            b.chaves[i] = -1;
            b.referencias[i] = -1;
        }
        escreverBalde(b);
        return endereco;
    }

    // ---------------------------------------------------------------
    // Busca
    // ---------------------------------------------------------------

    /** Retorna o endereço associado à chave, ou -1 se ela não estiver indexada. */
    public long buscar(int chave) throws Exception {
        Balde b = lerBalde(diretorio[posicaoDiretorio(chave)]);
        for (int i = 0; i < b.n; i++) {
            if (b.chaves[i] == chave) return b.referencias[i];
        }
        return -1;
    }

    // ---------------------------------------------------------------
    // Inserção
    // ---------------------------------------------------------------

    /**
     * Insere (chave, referência) no índice. Se a chave já existir, apenas
     * atualiza a referência associada (caso de um update que realocou o
     * registro para outro endereço).
     */
    public void inserir(int chave, long referencia) throws Exception {
        int pos = posicaoDiretorio(chave);
        Balde b = lerBalde(diretorio[pos]);

        for (int i = 0; i < b.n; i++) {
            if (b.chaves[i] == chave) {
                b.referencias[i] = referencia;
                escreverBalde(b);
                return;
            }
        }

        if (b.n < FATOR_BALDE) {
            b.chaves[b.n] = chave;
            b.referencias[b.n] = referencia;
            b.n++;
            escreverBalde(b);
            return;
        }

        dividirBalde(b);
        inserir(chave, referencia); // tenta novamente após a divisão
    }

    private void dividirBalde(Balde b) throws Exception {
        if (b.profundidadeLocal == profundidadeGlobal) {
            duplicarDiretorio();
        }

        int novaProfundidade = b.profundidadeLocal + 1;
        long enderecoNovoBalde = novoBalde(novaProfundidade);

        int[] chavesAntigas = b.chaves.clone();
        long[] referenciasAntigas = b.referencias.clone();
        int nAntigo = b.n;

        b.profundidadeLocal = novaProfundidade;
        b.n = 0;
        for (int i = 0; i < FATOR_BALDE; i++) {
            b.chaves[i] = -1;
            b.referencias[i] = -1;
        }
        Balde novoBalde = lerBalde(enderecoNovoBalde);

        // o bit recém-considerado (posição novaProfundidade-1) decide se a
        // entrada fica no balde original ou vai para o novo
        int bitDiscriminante = 1 << (novaProfundidade - 1);
        for (int i = 0; i < nAntigo; i++) {
            int chave = chavesAntigas[i];
            long ref = referenciasAntigas[i];
            if ((hash(chave) & bitDiscriminante) == 0) {
                b.chaves[b.n] = chave;
                b.referencias[b.n] = ref;
                b.n++;
            } else {
                novoBalde.chaves[novoBalde.n] = chave;
                novoBalde.referencias[novoBalde.n] = ref;
                novoBalde.n++;
            }
        }

        escreverBalde(b);
        escreverBalde(novoBalde);

        // redireciona, para o novo balde, as entradas do diretório que
        // apontavam para o balde original e cujo bit discriminante é 1
        for (int i = 0; i < diretorio.length; i++) {
            if (diretorio[i] == b.endereco && (i & bitDiscriminante) != 0) {
                diretorio[i] = enderecoNovoBalde;
            }
        }
        gravarDiretorio();
    }

    private void duplicarDiretorio() throws Exception {
        long[] novoDiretorio = new long[diretorio.length * 2];
        for (int i = 0; i < diretorio.length; i++) {
            novoDiretorio[i] = diretorio[i];
            novoDiretorio[i + diretorio.length] = diretorio[i];
        }
        diretorio = novoDiretorio;
        profundidadeGlobal++;
        gravarDiretorio();
    }

    // ---------------------------------------------------------------
    // Remoção
    // ---------------------------------------------------------------

    public boolean remover(int chave) throws Exception {
        int pos = posicaoDiretorio(chave);
        Balde b = lerBalde(diretorio[pos]);

        for (int i = 0; i < b.n; i++) {
            if (b.chaves[i] == chave) {
                for (int j = i; j < b.n - 1; j++) {
                    b.chaves[j] = b.chaves[j + 1];
                    b.referencias[j] = b.referencias[j + 1];
                }
                b.n--;
                b.chaves[b.n] = -1;
                b.referencias[b.n] = -1;
                escreverBalde(b);
                return true;
            }
        }
        return false;
    }

    // ---------------------------------------------------------------
    // Reconstrução (usada quando o arquivo de dados é reescrito por
    // inteiro, ex.: depois de uma ordenação externa, os endereços antigos
    // ficam todos inválidos)
    // ---------------------------------------------------------------

    /** Descarta todas as entradas e volta o índice ao estado inicial (1 balde, profundidade 0). */
    public void limpar() throws Exception {
        arqBaldes.setLength(0);
        profundidadeGlobal = 0;
        long enderecoBalde = novoBalde(0);
        diretorio = new long[1];
        diretorio[0] = enderecoBalde;
        gravarDiretorio();
    }

    public void close() throws Exception {
        arqDiretorio.close();
        arqBaldes.close();
    }
}
