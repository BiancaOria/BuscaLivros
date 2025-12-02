import java.nio.file.Path;
import java.io.*;

public class Main {

    public static void main(String[] args) {

        int[] NUM_THREADS = {4, 6, 12};
        String[] livros = {"DonQuixote.txt", "Dracula.txt", "MobyDick.txt"};

        String[] palavras_drac = {"and", "the", "to"};
        String[] palavras_don = {"que", "de", "y"};
        String[] palavras_moby = {"and", "the", "of"};

        new File("Resultados/serial").mkdirs();
        new File("Resultados/cpu").mkdirs();
        new File("Resultados/gpu").mkdirs();

        // INICIALIZAÇÃO DA GPU 
       // System.out.println(">>> Inicializando GPU...");
        try {
            ParallelGPU.init();
        } catch (Exception e) {
            //System.err.println("ERRO CRÍTICO: Não foi possível iniciar a GPU/OpenCL.");
            e.printStackTrace();
            return;
        }
        try {
            for (String livro : livros) {

                Path caminhoArquivo = Path.of("Amostras", livro);

                //System.out.println("========================================");
                //System.out.println("LIVRO: " + livro);
                //System.out.println("========================================");

                String[] texto = LeitorArquivo.loadWords(caminhoArquivo);

                String[] palavrasAtuais;
                if (livro.equals("Dracula.txt")) palavrasAtuais = palavras_drac;
                else if (livro.equals("DonQuixote.txt")) palavrasAtuais = palavras_don;
                else palavrasAtuais = palavras_moby;

                for (String palavra : palavrasAtuais) {
                   // System.out.println("\n>>> Palavra: '" + palavra + "'");

                    for (int exec = 0; exec < 6; exec++) {

                        boolean isWarmup = (exec <3);
                        //if (isWarmup) System.out.print("[Aquecendo JVM/Caches...] ");

                        String fileSuffix = "_" + palavra + "_exec" + exec;
                        String livroNome = livro.replace(".txt", "");

                        // ==========================================
                        // A. SERIAL
                        // ==========================================
                        long inicioSerial = System.nanoTime();
                        long countSerial = SerialCPU.count(texto, palavra);
                        long fimSerial = System.nanoTime();
                        double msSerial = (fimSerial - inicioSerial) / 1_000_000.0;

                        if (!isWarmup) {
                            //System.out.printf("Serial: %d oc. | Tempo: %.4f ms%n", countSerial, msSerial);
                            salvarCSV("Resultados/serial/" + livroNome + fileSuffix + ".csv",
                                    livro, palavra, exec, 1, countSerial, msSerial);
                        }
                        // ==========================================
                        // B. PARALELO CPU (Variando Threads)
                        // ==========================================
                        for (int t : NUM_THREADS) {
                            long inicioCPU = System.nanoTime();
                            long countCPU = ParallelCPU.countWords(texto, t, palavra);
                            long fimCPU = System.nanoTime();
                            double msCPU = (fimCPU - inicioCPU) / 1_000_000.0;

                            if (!isWarmup) {
                                //System.out.printf("CPU (%d threads): %d oc. | Tempo: %.4f ms%n", t, countCPU, msCPU);
                                salvarCSV("Resultados/cpu/" + livroNome + fileSuffix + "_threads" + t + ".csv",
                                        livro, palavra, exec, t, countCPU, msCPU);
                            }
                        }
                        // ==========================================
                        // C. PARALELO GPU (OpenCL)
                        // ==========================================
                        long inicioGPU = System.nanoTime();
                        long countGPU = ParallelGPU.countWordsGPU(texto, palavra);
                        long fimGPU = System.nanoTime();
                        double msGPU = (fimGPU - inicioGPU) / 1_000_000.0;



                        if (!isWarmup) {
                            //System.out.printf("GPU (OpenCL): %d oc. | Tempo: %.4f ms%n", countGPU, msGPU);
                            salvarCSV("Resultados/gpu/" + livroNome + fileSuffix + ".csv",
                                    livro, palavra, exec, 0, countGPU, msGPU);
                        }

                    }
                }
            }

           //System.out.println("\nProcessamento concluído. CSVs gerados.");
            GeradorGraficos.gerarGraficos();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            ParallelGPU.close();
        }
    }
        private static void salvarCSV (String path, String livro, String palavra,int exec, int threads, long count,
        double tempo){
            try (FileWriter fw = new FileWriter(path); PrintWriter pw = new PrintWriter(fw)) {

               pw.println("Livro;Palavra;Execucao;Threads;Count;TempoMs");

                String linha = String.format("%s;%s;%d;%d;%d;%.4f", livro, palavra, exec, threads, count, tempo);
                pw.println(linha);

                //System.out.println("Todos CSVs foram gerados dentro da pasta Resultados!");


            } catch (IOException e) {
               // System.err.println("Erro ao salvar CSV: " + path);
                e.printStackTrace();
            }
        }
}