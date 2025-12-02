import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import javax.imageio.ImageIO;
import java.util.*;
import java.util.List;

public class GeradorGraficos {

    private static final String BASE = "Resultados";
    private static final String OUTDIR = "imagens";

    private static final Map<String, Color> GLOBAL_COLOR_MAP = new HashMap<>();

    public static void gerarGraficos() {
        try {
            Files.createDirectories(Paths.get(OUTDIR));
        } catch (IOException ignored) {}

        GLOBAL_COLOR_MAP.clear();


        Map<String, Map<Integer, Double>> serialData = lerSerial();
        Map<String, Map<Integer, Map<Integer, Double>>> cpuData = lerCPU();
        Map<String, Map<Integer, Double>> gpuData = lerGPU();

        Set<String> todasChaves = new TreeSet<>();
        todasChaves.addAll(serialData.keySet());
        todasChaves.addAll(cpuData.keySet());
        todasChaves.addAll(gpuData.keySet());

        gerarCoresDinamicas(todasChaves);

        System.out.println("Gerando gráfico Serial...");
        gerarPNGSerial(serialData, OUTDIR + "/serial.png");

        System.out.println("Gerando gráfico CPU...");
        gerarPNGCpu(cpuData, OUTDIR + "/cpu.png");

        System.out.println("Gerando gráfico GPU...");
        gerarPNGGPU(gpuData, OUTDIR + "/gpu.png");

        System.out.println("✅ Gráficos gerados com sucesso na pasta: " + OUTDIR);
    }

    // -------------------------------------------------------------------------
    //                        SISTEMA DE CORES
    // -------------------------------------------------------------------------
    private static void gerarCoresDinamicas(Set<String> keys) {
        int total = keys.size();
        int i = 0;
        for (String key : keys) {
            float hue = (float) i / (float) total;
            Color c = Color.getHSBColor(hue, 0.75f, 0.85f);
            GLOBAL_COLOR_MAP.put(key, c);
            i++;
        }
    }

    private static Color getColor(String key) {
        return GLOBAL_COLOR_MAP.getOrDefault(key, Color.BLACK);
    }

    // -------------------------------------------------------------------------
    //                        LEITURA DE ARQUIVOS (COM SPLIT ";")
    // -------------------------------------------------------------------------

    private static Map<String, Map<Integer, Double>> lerSerial() {
        Map<String, Map<Integer, Double>> resultado = new TreeMap<>();
        Path dir = Paths.get(BASE, "serial");
        if (!Files.exists(dir)) return resultado;

        try {
            Files.list(dir).filter(p -> p.toString().endsWith(".csv")).forEach(p -> {
                String nomeArquivo = p.getFileName().toString();
                String book = extrairNomeLivro(nomeArquivo);
                try (BufferedReader br = Files.newBufferedReader(p)) {
                    br.readLine();
                    String linha;
                    while ((linha = br.readLine()) != null) {
                        // MUDANÇA 1: Split com ponto e vírgula
                        String[] cols = linha.split(";");

                        // Header: Livro;Palavra;Execucao;Threads;Count;TempoMs
                        if (cols.length >= 6) {
                            String word = cols[1].trim();
                            int exec = Integer.parseInt(cols[2].trim());

                            // MUDANÇA 2: Parse Double e troca vírgula por ponto para o Java entender
                            String valStr = cols[5].trim().replace(',', '.');
                            double tempo = Double.parseDouble(valStr);

                            String key = book + " ('" + word + "')";
                            resultado.putIfAbsent(key, new TreeMap<>());
                            resultado.get(key).put(exec, tempo);
                        }
                    }
                } catch (Exception ignored) {}
            });
        } catch (IOException ignored) {}
        return resultado;
    }

    private static Map<String, Map<Integer, Map<Integer, Double>>> lerCPU() {
        Map<String, Map<Integer, Map<Integer, Double>>> out = new TreeMap<>();
        Path dir = Paths.get(BASE, "cpu");
        if (!Files.exists(dir)) return out;

        try {
            Files.list(dir).filter(p -> p.toString().endsWith(".csv")).forEach(p -> {
                String nomeArquivo = p.getFileName().toString();
                String book = extrairNomeLivro(nomeArquivo);
                Integer threadNum = extrairThreads(nomeArquivo);
                if (threadNum == null) return;

                try (BufferedReader br = Files.newBufferedReader(p)) {
                    br.readLine();
                    String linha;
                    while ((linha = br.readLine()) != null) {
                        // MUDANÇA: Split ;
                        String[] cols = linha.split(";");

                        if (cols.length >= 6) {
                            String word = cols[1].trim();
                            int exec = Integer.parseInt(cols[2].trim());

                            // MUDANÇA: Double
                            String valStr = cols[5].trim().replace(',', '.');
                            double tempo = Double.parseDouble(valStr);

                            String key = book + " ('" + word + "')";
                            out.putIfAbsent(key, new TreeMap<>());
                            out.get(key).putIfAbsent(threadNum, new TreeMap<>());
                            out.get(key).get(threadNum).put(exec, tempo);
                        }
                    }
                } catch (Exception ignored) {}
            });
        } catch (IOException ignored) {}
        return out;
    }

    private static Map<String, Map<Integer, Double>> lerGPU() {
        Map<String, Map<Integer, Double>> resultado = new TreeMap<>();
        Path dir = Paths.get(BASE, "gpu");
        if (!Files.exists(dir)) return resultado;

        try {
            Files.list(dir).filter(p -> p.toString().endsWith(".csv")).forEach(p -> {
                String nomeArquivo = p.getFileName().toString();
                String book = extrairNomeLivro(nomeArquivo);
                try (BufferedReader br = Files.newBufferedReader(p)) {
                    br.readLine();
                    String linha;
                    while ((linha = br.readLine()) != null) {
                        // MUDANÇA: Split ;
                        String[] cols = linha.split(";");

                        if (cols.length >= 6) {
                            String word = cols[1].trim();
                            int exec = Integer.parseInt(cols[2].trim());

                            // MUDANÇA: Double e coluna correta (5)
                            String valStr = cols[5].trim().replace(',', '.');
                            double tempo = Double.parseDouble(valStr);

                            String key = book + " ('" + word + "')";
                            resultado.putIfAbsent(key, new TreeMap<>());
                            resultado.get(key).put(exec, tempo);
                        }
                    }
                } catch (Exception ignored) {}
            });
        } catch (IOException ignored) {}
        return resultado;
    }

    private static String extrairNomeLivro(String filename) {
        String base = filename;
        if (base.contains("_")) base = base.substring(0, base.indexOf("_"));
        if (base.endsWith(".csv")) base = base.substring(0, base.length() - 4);
        return base.trim();
    }

    private static Integer extrairThreads(String filename) {
        int tIdx = filename.indexOf("_threads");
        if (tIdx >= 0) {
            String tail = filename.substring(tIdx + "_threads".length());
            if (tail.endsWith(".csv")) tail = tail.substring(0, tail.length() - 4);
            try { return Integer.parseInt(tail); } catch (Exception e) { return null; }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    //                        DESENHO DOS GRÁFICOS
    // -------------------------------------------------------------------------

    private static void aplicarQualidade(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    // SERIE TEMPORAL (SERIAL E GPU)
    private static void gerarPNGSerial(Map<String, Map<Integer, Double>> dados, String outPath) {
        if (dados.isEmpty()) return;

        int W = 1200, H = 1200;
        int MARGIN = 60;
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        aplicarQualidade(g);

        g.setColor(Color.WHITE); g.fillRect(0,0,W,H);
        drawAxes(g, W, H, "Execução", "Tempo (ms)", MARGIN);

        double maxTempo = getMaxTempoSimples(dados)*1.05;
        double minTempo = getMinTempoSimples(dados)*0.9;
        if (maxTempo - minTempo < 0.1) maxTempo += 0.5;
        drawGrid(g, W, H, MARGIN, minTempo,maxTempo);

        int[] execs = {3,4,5}; // Assumindo que 0 a 2 foi warmup
        int startX = 150;
        int gapExec = 300;

        List<String> keys = new ArrayList<>(dados.keySet());

        for (int kIdx = 0; kIdx < keys.size(); kIdx++) {
            String key = keys.get(kIdx);
            Color c = getColor(key);
            g.setColor(c);
            Map<Integer, Double> mapExec = dados.get(key);

            // Shift visual para não sobrepor linhas
            int serieOffsetX = (kIdx - keys.size()/2) * 20;

            for (int e = 0; e < execs.length; e++) {
                int exec = execs[e];
                if (!mapExec.containsKey(exec)) continue;
                double tempo = mapExec.get(exec);

                int x = startX + e * gapExec + serieOffsetX;
                int y = valueToY(tempo,minTempo, maxTempo, H, MARGIN);

                // Linha conectando pontos
                if (e > 0 && mapExec.containsKey(execs[e-1])) {
                    double prev = mapExec.get(execs[e-1]);
                    int prevX = startX + (e-1)*gapExec + serieOffsetX;
                    int prevY = valueToY(prev, minTempo, maxTempo, H, MARGIN);
                    g.setStroke(new BasicStroke(2f));
                    g.drawLine(prevX, prevY, x, y);
                }

                g.fillOval(x-6, y-6, 12, 12);

                // MUDANÇA: Formatação com 3 casas decimais
                drawLabel(g, String.format("%.3f", tempo), x, y, c);
            }
            drawLegend(g, W, kIdx, key, c);
        }
        g.dispose();
        salvarImagem(img, outPath);
    }

    private static void gerarPNGGPU(Map<String, Map<Integer, Double>> dados, String outPath) {
        gerarPNGSerial(dados, outPath);
    }


    private static void gerarPNGCpu(Map<String, Map<Integer, Map<Integer, Double>>> dados, String outPath) {
        if (dados.isEmpty()) return;

        System.out.println("Gerando gráficos CPU separados por Threads...");

        // 1. Descobre quais quantidades de threads existem (ex: 4, 6, 12)
        Set<Integer> threadCounts = new TreeSet<>();
        for (var map : dados.values()) {
            threadCounts.addAll(map.keySet());
        }

        // 2. Para cada quantidade de thread, filtra os dados e gera um gráfico
        for (int t : threadCounts) {

            // Vamos criar um Map igual ao que o Serial espera: Map<Livro, Map<Execucao, Tempo>>
            Map<String, Map<Integer, Double>> dadosFiltrados = new TreeMap<>();

            for (var entry : dados.entrySet()) {
                String livro = entry.getKey();
                var threadsMap = entry.getValue();

                // Se esse livro rodou com essa quantidade de threads, pega os dados
                if (threadsMap.containsKey(t)) {
                    Map<Integer, Double> execs = threadsMap.get(t);
                    // Adiciona no map filtrado.
                    // Obs: A chave será apenas o nome do livro, pois o gráfico já é específico da thread
                    dadosFiltrados.put(livro, new TreeMap<>(execs));
                }
            }

            // 3. Define o nome do arquivo (ex: cpu_4_threads.png)
            String pathBase = outPath.endsWith(".png") ? outPath.substring(0, outPath.length() - 4) : outPath;
            String filename = String.format("%s_%d_threads.png", pathBase, t);

            System.out.println("   -> Criando: " + filename);

            // 4. MÁGICA: Reutiliza o método Serial que você já tem!
            // Ele funciona perfeitamente porque a estrutura de dados agora é compatível.
            gerarPNGSerial(dadosFiltrados, filename);
        }
    }

    // -------------------------------------------------------------------------
    //                        UTILITÁRIOS (DOUBLE)
    // -------------------------------------------------------------------------

    private static void drawAxes(Graphics2D g, int W, int H, String xLab, String yLab, int margin) {
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2f));
        g.drawLine(margin, margin, margin, H - margin);
        g.drawLine(margin, H - margin, W - margin, H - margin);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString(yLab, margin - 40, margin - 15);
        g.drawString(xLab, W/2, H - 10);
    }

    private static void drawGrid(Graphics2D g, int W, int H, int margin, double min, double max) {
        g.setColor(new Color(230,230,230));
        g.setStroke(new BasicStroke(1f));
        int steps = 5;
        double range = max - min;

        for (int i=1; i<=steps; i++) {
            double val = min +(range * i) / (double)steps;
            int y = valueToY(val, min, max, H, margin);
            g.drawLine(margin, y, W-margin, y);

            g.setColor(Color.GRAY);
            g.setFont(new Font("SansSerif", Font.PLAIN, 10));

            g.drawString(String.format("%.2f", val), margin - 45, y + 5);
            g.setColor(new Color(230,230,230));
        }
    }

    private static void drawLabel(Graphics2D g, String txt, int x, int y, Color c) {
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(txt);
        int h = fm.getHeight();

        g.setColor(new Color(255,255,255, 200));
        g.fillRect(x - w/2 - 2, y - h - 5, w+4, h);

        g.setColor(c.darker());
        g.drawString(txt, x - w/2, y - 10);
        g.setColor(c);
    }

    private static void drawLegend(Graphics2D g, int W, int idx, String txt, Color c) {
        int x = W - 220;
        int y = 80 + idx * 25;
        g.setColor(c);
        g.fillRect(x, y, 15, 15);
        g.setColor(Color.DARK_GRAY);
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.drawString(txt, x + 20, y + 12);
    }

    // Unificado: ValueToY usando double
    private static int valueToY(double val, double min, double max, int H, int margin) {

        double range = max - min;
        if (range <= 0) return H - margin;

        double usefulH = H - 2.0 * margin;

        double normalizedPosition = (val - min) / range;

        return (int) (H - margin - (normalizedPosition * usefulH));


    }

    private static double getMaxTempoSimples(Map<String, Map<Integer, Double>> dados) {
        double m = 0.1;
        for (var map : dados.values()) {
            for (double v : map.values()) if (v > m) m = v;
        }
        return m;
    }

    private static double getMinTempoSimples(Map<String, Map<Integer, Double>> dados) {
        double min = Double.MAX_VALUE;
        for (var map : dados.values()) {
            for (double v : map.values()) if (v < min) min = v;
        }
        return (min == Double.MAX_VALUE) ? 0 : min;
    }

    private static double getMaxTempoCPU(Map<String, Map<Integer, Map<Integer, Double>>> dados) {
        double m = 0.1;
        for (var mapTh : dados.values()) {
            for (var mapEx : mapTh.values()) {
                for (double v : mapEx.values()) if (v > m) m = v;
            }
        }
        return m;
    }

    private static double getMinTempoCPU(Map<String, Map<Integer, Map<Integer, Double>>> dados) {
        double min = Double.MAX_VALUE;
        for (var mapTh : dados.values()) {
            for (var mapEx : mapTh.values()) {
                for (double v : mapEx.values()) if (v < min) min = v;
            }
        }
        return (min == Double.MAX_VALUE) ? 0 : min;
    }

    private static Map<Integer, Double> calcularMedias(Map<Integer, Map<Integer, Double>> dadosThread) {
        Map<Integer, Double> medias = new TreeMap<>();
        for (var entry : dadosThread.entrySet()) {
            double soma = 0;
            int count = 0;
            for (double v : entry.getValue().values()) {
                soma += v; count++;
            }
            if (count > 0) medias.put(entry.getKey(), soma/count);
        }
        return medias;
    }
    private static Map<String, Map<Integer, Double>> flattenCpuData(Map<String, Map<Integer, Map<Integer, Double>>> cpuData) {
        Map<String, Map<Integer, Double>> flattened = new TreeMap<>();

        for (var entryBook : cpuData.entrySet()) {
            String bookKey = entryBook.getKey();
            Map<Integer, Map<Integer, Double>> threadsMap = entryBook.getValue();

            for (var entryThread : threadsMap.entrySet()) {
                int threadCount = entryThread.getKey();
                Map<Integer, Double> execsMap = entryThread.getValue();

                // AQUI ESTÁ O TRUQUE: Cria uma chave única combinando Livro + Threads
                // Exemplo: "Dracula ('the') - 4 Threads"
                String newKey = String.format("%s - %d Threads", bookKey, threadCount);

                flattened.put(newKey, new TreeMap<>(execsMap));
            }
        }
        return flattened;
    }

    private static void salvarImagem(BufferedImage img, String path) {
        try { ImageIO.write(img, "png", new File(path)); } catch (IOException e) { e.printStackTrace(); }
    }
}