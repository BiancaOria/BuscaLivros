import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {

        try {
            // Caminho do arquivo
            //Path caminhoArquivo = Path.of("C:\\Users\\55859\\Downloads\\Buscador\\Amostras\\Dracula.txt");
            Path caminhoArquivo = Path.of("C:\\Users\\loren\\Downloads\\Compt Paralela\\Trabalho do Inferno\\BuscaLivros\\Amostras\\Dracula.txt");

            // Palavra alvo
            String palavra = "the";

            // Número de threads paralelas (ajuste automático é recomendado)
            //int numThreads = 10;
            int numThreads = Runtime.getRuntime().availableProcessors();


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

            tempoInicio = System.currentTimeMillis();
            int countGPU = ParallelGPU.countWordsGPU(texto, palavra);
            tempoFim = System.currentTimeMillis();

            System.out.println("ParallelGPU (OpenCL): "
                    + countGPU + " ocorrências em "
                    + (tempoFim - tempoInicio) + " ms");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
