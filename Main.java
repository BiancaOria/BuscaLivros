import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {

        try {
            // Caminho do arquivo
            Path caminhoArquivo = Path.of("C:\\Users\\55859\\Downloads\\Buscador\\Amostras\\Dracula.txt");

            // Palavra alvo
            String palavra = "Dracula";

            // Número de threads paralelas (ajuste automático é recomendado)
            int numThreads = 10;
            // Você também pode usar: int numThreads = 10;

            System.out.println("Usando " + numThreads + " threads no paralelo");
            System.out.println("----------------------------------------");

            // ======== LER ARQUIVO ========

            String[] texto = LeitorArquivo.loadWords(caminhoArquivo);

            // ======== SERIAL ========
            long tempoInicio = System.currentTimeMillis();
            long countSerial = SerialCPU.count(texto, palavra);
            long tempoFim = System.currentTimeMillis();

            System.out.println("SerialCPU: " + countSerial + " ocorrências em "
                    + (tempoFim - tempoInicio) + " ms");

            // ======== PARALELO CPU ========

            tempoInicio = System.currentTimeMillis();
            long countParalelo = ParallelCPU.countWords(texto, numThreads, palavra);
            tempoFim = System.currentTimeMillis();

            System.out.println("ParallelCPU (" + numThreads + " threads): "
                    + countParalelo + " ocorrências em "
                    + (tempoFim - tempoInicio) + " ms");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
