import java.nio.file.Path;
import java.io.*;

public class Main {

    public static void main(String[] args) {

        int[] NUM_THREADS = {4, 6, 12};
        String[] livros = {"DonQuixote.txt", "Dracula.txt", "MobyDick.txt"};
        String palavra = "the";
        String[] palavras_drac = {"", "the", ""};
        String[] palavras_don = {"", "the", ""};
        String[] palavras_moby = {"", "the", ""};

        new File("Resultados/serial").mkdirs();
        new File("Resultados/cpu").mkdirs();
        new File("Resultados/gpu").mkdirs();

        try {
            for (String livro : livros) {
                Path caminhoArquivo = Path.of("C:\\Users\\loren\\Downloads\\Compt Paralela\\Trabalho do Inferno\\BuscaLivros\\Amostras\\" + livro);

                int numDisponiveis = Runtime.getRuntime().availableProcessors();
                System.out.println("Usando " + numDisponiveis + " threads no paralelo");
                System.out.println("----------------------------------------");

                String[] texto = LeitorArquivo.loadWords(caminhoArquivo);

                for (int exec = 1; exec <= 3; exec++) {

                    // ======== SERIAL ========
                    long inicioSerial = System.currentTimeMillis();
                    long countSerial = SerialCPU.count(texto, palavra);
                    long fimSerial = System.currentTimeMillis();
                    long tempoSerial = fimSerial - inicioSerial;

                    System.out.println("SerialCPU: " + countSerial + " ocorrências em " + tempoSerial + " ms");

                    try (FileWriter fwSerial = new FileWriter("Resultados/serial/" + livro.replace(".txt", "") + "_exec" + exec + ".csv");
                         PrintWriter serialW = new PrintWriter(fwSerial)) {
                        serialW.println("Livro,Execucao,Threads,Count,TempoMs");
                        serialW.printf("%s,%d,%d,%d,%d%n", livro, exec, 1, countSerial, tempoSerial);
                    }

                    // ======== PARALELO CPU (variando threads) ========
                    for (int t : NUM_THREADS) {

                        long inicioCPU = System.currentTimeMillis();
                        long countCPU = ParallelCPU.countWords(texto, t, palavra);
                        long fimCPU = System.currentTimeMillis();
                        long tempoCPU = fimCPU - inicioCPU;

                        System.out.println("ParallelCPU (" + t + " threads): " + countCPU + " ocorrências em " + tempoCPU + " ms");

                        try (FileWriter fwCPU = new FileWriter("Resultados/cpu/" + livro.replace(".txt", "") + "_exec" + exec + "_threads" + t + ".csv");
                             PrintWriter cpuW = new PrintWriter(fwCPU)) {
                            cpuW.println("Livro,Execucao,Threads,Count,TempoMs");
                            cpuW.printf("%s,%d,%d,%d,%d%n", livro, exec, t, countCPU, tempoCPU);
                        }

                        // SEU PRINT ORIGINAL MANTIDO ✅
                        System.out.println(livro.replace(".txt","") + " | exec " + exec + " | " + t + " threads -> ✅ CSV salvo");
                    }

                    // ======== PARALELO GPU ========
                    long inicioGPU = System.currentTimeMillis();
                    int countGPU = ParallelGPU.countWordsGPU(texto, palavra);
                    long fimGPU = System.currentTimeMillis();
                    long tempoGPU = fimGPU - inicioGPU;

                    System.out.println("ParallelGPU (OpenCL): " + countGPU + " ocorrências em " + tempoGPU + " ms");

                    try (FileWriter fwGPU = new FileWriter("Resultados/gpu/" + livro.replace(".txt", "") + "_exec" + exec + ".csv");
                         PrintWriter gpuW = new PrintWriter(fwGPU)) {
                        gpuW.println("Livro,Execucao,Count,TempoMs");
                        gpuW.printf("%s,%d,%d,%d%n", livro, exec, countGPU, tempoGPU);
                    }

                    System.out.println();
                }

                System.out.println();
            }

            // SEU PRINT ORIGINAL FINAL ✅
            System.out.println("Todos CSVs foram gerados dentro da pasta Resultados!");


            GeradorGraficos.gerarGraficos();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
