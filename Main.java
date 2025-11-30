import java.nio.file.Path;
import java.io.*;

public class Main {

    public static void main(String[] args) {

        int[] NUM_THREADS = {4, 6, 12};
        String[] livros = {"DonQuixote.txt", "Dracula.txt", "MobyDick.txt"};

        // Arrays de palavras especificas
        String[] palavras_drac = {"and", "the", "to"};
        String[] palavras_don = {"que", "de", "y"};
        String[] palavras_moby = {"and", "the", "of"};

        new File("Resultados/serial").mkdirs();
        new File("Resultados/cpu").mkdirs();
        new File("Resultados/gpu").mkdirs();

        try {
            for (String livro : livros) {
                Path caminhoArquivo = Path.of("C:\\Users\\loren\\Downloads\\Compt Paralela\\Trabalho do Inferno\\BuscaLivros\\Amostras\\" + livro);

                int numDisponiveis = Runtime.getRuntime().availableProcessors();
                System.out.println("========================================");
                System.out.println("LIVRO: " + livro);
                System.out.println("Usando " + numDisponiveis + " threads no paralelo");
                System.out.println("========================================");

                // Carrega o texto apenas uma vez por livro para economizar I/O
                String[] texto = LeitorArquivo.loadWords(caminhoArquivo);

                // 1. Determina quais palavras usar baseada no livro atual
                String[] palavrasAtuais;
                if (livro.equals("Dracula.txt")) {
                    palavrasAtuais = palavras_drac;
                } else if (livro.equals("DonQuixote.txt")) {
                    palavrasAtuais = palavras_don;
                } else {
                    palavrasAtuais = palavras_moby; // MobyDick
                }

                // 2. Itera sobre cada palavra do array selecionado
                for (String palavra : palavrasAtuais) {

                    System.out.println("\n>>> Buscando palavra: '" + palavra + "' no livro " + livro);

                    for (int exec = 1; exec <= 3; exec++) {

                        // Define um sufixo para o nome do arquivo incluir a palavra (ex: Dracula_the_exec1)
                        String fileSuffix = "_" + palavra + "_exec" + exec;
                        String livroNome = livro.replace(".txt", "");

                        // ======== SERIAL ========
                        long inicioSerial = System.currentTimeMillis();
                        long countSerial = SerialCPU.count(texto, palavra);
                        long fimSerial = System.currentTimeMillis();
                        //TODO MELHORAR
                        long tempoSerial = (fimSerial - inicioSerial);

                        System.out.println("SerialCPU: " + countSerial + " ocorrências em " + tempoSerial + " ms");

                        try (FileWriter fwSerial = new FileWriter("Resultados/serial/" + livroNome + fileSuffix + ".csv");
                             PrintWriter serialW = new PrintWriter(fwSerial)) {
                            serialW.println("Livro,Palavra,Execucao,Threads,Count,TempoMs");
                            serialW.printf("%s,%s,%d,%d,%d,%d%n", livro, palavra, exec, 1, countSerial, tempoSerial);
                        }

                        // ======== PARALELO CPU (variando threads) ========
                        for (int t : NUM_THREADS) {

                            long inicioCPU = System.currentTimeMillis();
                            long countCPU = ParallelCPU.countWords(texto, t, palavra);
                            long fimCPU = System.currentTimeMillis();
                            long tempoCPU = fimCPU - inicioCPU;

                            System.out.println("ParallelCPU (" + t + " threads): " + countCPU + " ocorrências em " + tempoCPU + " ms");

                            try (FileWriter fwCPU = new FileWriter("Resultados/cpu/" + livroNome + fileSuffix + "_threads" + t + ".csv");
                                 PrintWriter cpuW = new PrintWriter(fwCPU)) {
                                cpuW.println("Livro,Palavra,Execucao,Threads,Count,TempoMs");
                                cpuW.printf("%s,%s,%d,%d,%d,%d%n", livro, palavra, exec, t, countCPU, tempoCPU);
                            }

                            System.out.println(livroNome + " | palavra: " + palavra + " | exec " + exec + " | " + t + " threads -> ✅ CSV salvo");
                        }

                        // ======== PARALELO GPU ========
                        long inicioGPU = System.currentTimeMillis();
                        int countGPU = ParallelGPU.countWordsGPU(texto, palavra);
                        long fimGPU = System.currentTimeMillis();
                        long tempoGPU = fimGPU - inicioGPU;

                        System.out.println("ParallelGPU (OpenCL): " + countGPU + " ocorrências em " + tempoGPU + " ms");

                        try (FileWriter fwGPU = new FileWriter("Resultados/gpu/" + livroNome + fileSuffix + ".csv");
                             PrintWriter gpuW = new PrintWriter(fwGPU)) {
                            gpuW.println("Livro,Palavra,Execucao,Count,TempoMs");
                            gpuW.printf("%s,%s,%d,%d,%d%n", livro, palavra, exec, countGPU, tempoGPU);
                        }

                        System.out.println("--- Fim exec " + exec + " para palavra '" + palavra + "' ---");
                    }
                }
                System.out.println();
            }

            System.out.println("Todos CSVs foram gerados dentro da pasta Resultados!");

            GeradorGraficos.gerarGraficos();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}