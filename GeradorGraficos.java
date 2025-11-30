import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import javax.imageio.ImageIO;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class GeradorGraficos {

    private static final String BASE = "Resultados";
    private static final String OUTDIR = "imagens";

    // Mapa global de cores para garantir consistência entre os 3 gráficos
    private static final Map<String, Color> GLOBAL_COLOR_MAP = new HashMap<>();

    public static void gerarGraficos() {
        try {
            Files.createDirectories(Paths.get(OUTDIR));
        } catch (IOException ignored) {}

        GLOBAL_COLOR_MAP.clear();

        // 1. Ler todos os dados
        Map<String, Map<Integer, Long>> serialData = lerSerial();
        Map<String, Map<Integer, Map<Integer, Long>>> cpuData = lerCPU();
        Map<String, Map<Integer, Long>> gpuData = lerGPU();

        // 2. Coletar todas as chaves únicas (Livro + Palavra) de todos os datasets
        // para gerar uma paleta de cores única e consistente.
        Set<String> todasChaves = new TreeSet<>(); // TreeSet ordena alfabeticamente
        todasChaves.addAll(serialData.keySet());
        todasChaves.addAll(cpuData.keySet());
        todasChaves.addAll(gpuData.keySet());

        gerarCoresDinamicas(todasChaves);

        // 3. Gerar Gráficos
        System.out.println("Gerando gráfico Serial...");
        gerarPNGSerial(serialData, OUTDIR + "/serial.png");

        System.out.println("Gerando gráfico CPU...");
        gerarPNGCpu(cpuData, OUTDIR + "/cpu.png");

        System.out.println("Gerando gráfico GPU...");
        gerarPNGGPU(gpuData, OUTDIR + "/gpu.png"); // Reutiliza lógica similar ao Serial

        System.out.println("✅ Gráficos gerados com sucesso na pasta: " + OUTDIR);
    }

    // -------------------------------------------------------------------------
    //                        SISTEMA DE CORES
    // -------------------------------------------------------------------------
    private static void gerarCoresDinamicas(Set<String> keys) {
        int total = keys.size();
        int i = 0;
        for (String key : keys) {
            // Gera uma cor baseada no Hue (Matiz) dividindo o espectro igualmente
            float hue = (float) i / (float) total;
            // Saturação 0.7 e Brilho 0.85 garantem cores vivas e legíveis
            Color c = Color.getHSBColor(hue, 0.75f, 0.85f);
            GLOBAL_COLOR_MAP.put(key, c);
            i++;
        }
    }

    private static Color getColor(String key) {
        return GLOBAL_COLOR_MAP.getOrDefault(key, Color.BLACK);
    }

    // -------------------------------------------------------------------------
    //                        LEITURA DE ARQUIVOS
    // -------------------------------------------------------------------------

    private static Map<String, Map<Integer, Long>> lerSerial() {
        Map<String, Map<Integer, Long>> resultado = new TreeMap<>();
        Path dir = Paths.get(BASE, "serial");
        if (!Files.exists(dir)) return resultado;

        try {
            Files.list(dir).filter(p -> p.toString().endsWith(".csv")).forEach(p -> {
                String nomeArquivo = p.getFileName().toString();
                String book = extrairNomeLivro(nomeArquivo);
                try (BufferedReader br = Files.newBufferedReader(p)) {
                    br.readLine(); // Header
                    String linha;
                    while ((linha = br.readLine()) != null) {
                        String[] cols = linha.split(",");
                        // Serial Header: Livro,Palavra,Execucao,Threads,Count,TempoMs
                        if (cols.length >= 6) {
                            String word = cols[1].trim();
                            int exec = Integer.parseInt(cols[2].trim());
                            long tempo = Long.parseLong(cols[5].trim()); // Coluna 5 é Tempo
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

    private static Map<String, Map<Integer, Map<Integer, Long>>> lerCPU() {
        Map<String, Map<Integer, Map<Integer, Long>>> out = new TreeMap<>();
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
                        String[] cols = linha.split(",");
                        // CPU Header: Livro,Palavra,Execucao,Threads,Count,TempoMs
                        if (cols.length >= 6) {
                            String word = cols[1].trim();
                            int exec = Integer.parseInt(cols[2].trim());
                            long tempo = Long.parseLong(cols[5].trim()); // Coluna 5 é Tempo
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

    private static Map<String, Map<Integer, Long>> lerGPU() {
        Map<String, Map<Integer, Long>> resultado = new TreeMap<>();
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
                        String[] cols = linha.split(",");
                        // GPU Header na Main: Livro,Palavra,Execucao,Count,TempoMs
                        // Índices:            0     1       2        3     4
                        if (cols.length >= 5) {
                            String word = cols[1].trim();
                            int exec = Integer.parseInt(cols[2].trim());
                            long tempo = Long.parseLong(cols[4].trim()); // Coluna 4 é Tempo na GPU
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
        // Tenta limpar sufixos conhecidos para pegar o nome puro
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

    // Serve para Serial e GPU
    private static void gerarPNGSerial(Map<String, Map<Integer, Long>> dados, String outPath) {
        if (dados.isEmpty()) return;

        int W = 1200, H = 600;
        int MARGIN = 60;
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        aplicarQualidade(g);

        g.setColor(Color.WHITE); g.fillRect(0,0,W,H);
        drawAxes(g, W, H, "Execução", "Tempo (ms)", MARGIN);

        long maxTempo = getMaxTempoSimples(dados);
        drawGrid(g, W, H, MARGIN, maxTempo);

        int[] execs = {1,2,3};
        int startX = 150;
        int gapExec = 300;

        // Offset para separar linhas sobrepostas
        List<String> keys = new ArrayList<>(dados.keySet());
        int offsetStep = 45; // Distância vertical forçada entre chaves
        int totalOffset = (keys.size() - 1) * offsetStep;

        for (int kIdx = 0; kIdx < keys.size(); kIdx++) {
            String key = keys.get(kIdx);
            Color c = getColor(key);
            g.setColor(c);
            Map<Integer, Long> mapExec = dados.get(key);

            // Calcula o deslocamento vertical "falso" para evitar sobreposição visual exata
            // O gráfico mostra o valor real no eixo Y, mas deslocamos visualmente o conjunto inteiro um pouco
            // Ou melhor: mantemos Y real, mas deslocamos X ou rótulo.
            // Para "aumentar a distância", vamos adicionar um offset fixo ao X para cada série
            int serieOffsetX = (kIdx - keys.size()/2) * 20;

            for (int e = 0; e < execs.length; e++) {
                int exec = execs[e];
                if (!mapExec.containsKey(exec)) continue;
                long tempo = mapExec.get(exec);

                int x = startX + e * gapExec + serieOffsetX;
                int y = valueToY(tempo, maxTempo, H, MARGIN);

                // Linha
                if (e > 0 && mapExec.containsKey(execs[e-1])) {
                    long prev = mapExec.get(execs[e-1]);
                    int prevX = startX + (e-1)*gapExec + serieOffsetX;
                    int prevY = valueToY(prev, maxTempo, H, MARGIN);
                    g.setStroke(new BasicStroke(2f));
                    g.drawLine(prevX, prevY, x, y);
                }

                // Ponto
                g.fillOval(x-6, y-6, 12, 12);

                // Rótulo com fundo para não misturar
                drawLabel(g, tempo + "ms", x, y, c);
            }

            drawLegend(g, W, kIdx, key, c);
        }
        g.dispose();
        salvarImagem(img, outPath);
    }

    // Serve para GPU (mesma estrutura de dados Map<Integer, Long>)
    private static void gerarPNGGPU(Map<String, Map<Integer, Long>> dados, String outPath) {
        gerarPNGSerial(dados, outPath); // Reutiliza o método pois a estrutura é idêntica
    }

    private static void gerarPNGCpu(Map<String, Map<Integer, Map<Integer, Long>>> dados, String outPath) {
        if (dados.isEmpty()) return;

        Set<Integer> threadsSet = new TreeSet<>();
        for (var m : dados.values()) threadsSet.addAll(m.keySet());
        List<Integer> threadsList = new ArrayList<>(threadsSet);

        int W = 1400, H = 600;
        int MARGIN = 80;

        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        aplicarQualidade(g);

        g.setColor(Color.WHITE); g.fillRect(0,0,W,H);
        drawAxes(g, W, H, "Threads", "Tempo Médio (ms)", MARGIN);

        long maxTempo = getMaxTempoCPU(dados);
        drawGrid(g, W, H, MARGIN, maxTempo);

        int left = MARGIN + 50;
        int right = W - 250;
        int plotW = right - left;
        int gap = plotW / Math.max(1, threadsList.size()-1);

        // Labels eixo X
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        for (int i=0; i<threadsList.size(); i++) {
            int x = left + i * gap;
            g.drawString(threadsList.get(i) + " Threads", x-20, H - MARGIN + 25);
        }

        List<String> keys = new ArrayList<>(dados.keySet());

        for (int kIdx = 0; kIdx < keys.size(); kIdx++) {
            String key = keys.get(kIdx);
            Color c = getColor(key);
            g.setColor(c);
            g.setStroke(new BasicStroke(3f));

            Map<Integer, Double> medias = calcularMedias(dados.get(key));

            // Offset X pequeno para cada linha não ficar uma em cima da outra
            int serieShiftX = (kIdx - keys.size()/2) * 12;

            Integer prevX=null, prevY=null;

            for (int i=0; i<threadsList.size(); i++) {
                int th = threadsList.get(i);
                if (!medias.containsKey(th)) continue;

                double val = medias.get(th);
                int x = left + i*gap + serieShiftX;
                int y = valueToYDouble(val, maxTempo, H, MARGIN);

                // Pontos de dispersão (bolinhas pequenas transparentes)
                Map<Integer, Long> execs = dados.get(key).get(th);
                if (execs != null) {
                    g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 100));
                    int dispersaoW = 20;
                    int dx = x - dispersaoW;
                    for (long raw : execs.values()) {
                        int dy = valueToY(raw, maxTempo, H, MARGIN);
                        g.fillOval(dx, dy-3, 6, 6);
                        dx += 10;
                    }
                    g.setColor(c); // Volta cor sólida
                }

                if (prevX != null) g.drawLine(prevX, prevY, x, y);
                g.fillOval(x-5, y-5, 10, 10);

                // Exibe valor médio se houver espaço
                if (i % 2 == 0 || kIdx % 2 == 0) { // Alterna labels para poluir menos
                    drawLabel(g, String.format("%.0f", val), x, y, c);
                }

                prevX = x; prevY = y;
            }
            drawLegend(g, W, kIdx, key, c);
        }
        g.dispose();
        salvarImagem(img, outPath);
    }

    // -------------------------------------------------------------------------
    //                        UTILITÁRIOS
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

    private static void drawGrid(Graphics2D g, int W, int H, int margin, long max) {
        g.setColor(new Color(230,230,230));
        g.setStroke(new BasicStroke(1f));
        int steps = 10;
        for (int i=1; i<=steps; i++) {
            long val = (max * i) / steps;
            int y = valueToY(val, max, H, margin);
            g.drawLine(margin, y, W-margin, y);

            g.setColor(Color.GRAY);
            g.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g.drawString(String.valueOf(val), margin - 35, y + 5);
            g.setColor(new Color(230,230,230));
        }
    }

    private static void drawLabel(Graphics2D g, String txt, int x, int y, Color c) {
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(txt);
        int h = fm.getHeight();

        // Fundo branco semitransparente
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

    private static int valueToY(long val, long max, int H, int margin) {
        if (max == 0) return H - margin;
        double usefulH = H - 2.0 * margin;
        double ratio = (double) val / max;
        return (int) (H - margin - (ratio * usefulH * 0.9)); // 0.9 deixa um teto livre
    }

    private static int valueToYDouble(double val, long max, int H, int margin) {
        if (max == 0) return H - margin;
        double usefulH = H - 2.0 * margin;
        double ratio = val / max;
        return (int) (H - margin - (ratio * usefulH * 0.9));
    }

    private static long getMaxTempoSimples(Map<String, Map<Integer, Long>> dados) {
        long m = 1;
        for (var map : dados.values()) {
            for (long v : map.values()) if (v > m) m = v;
        }
        return m;
    }

    private static long getMaxTempoCPU(Map<String, Map<Integer, Map<Integer, Long>>> dados) {
        long m = 1;
        for (var mapTh : dados.values()) {
            for (var mapEx : mapTh.values()) {
                for (long v : mapEx.values()) if (v > m) m = v;
            }
        }
        return m;
    }

    private static Map<Integer, Double> calcularMedias(Map<Integer, Map<Integer, Long>> dadosThread) {
        Map<Integer, Double> medias = new TreeMap<>();
        for (var entry : dadosThread.entrySet()) {
            double soma = 0;
            int count = 0;
            for (long v : entry.getValue().values()) {
                soma += v; count++;
            }
            if (count > 0) medias.put(entry.getKey(), soma/count);
        }
        return medias;
    }

    private static void salvarImagem(BufferedImage img, String path) {
        try { ImageIO.write(img, "png", new File(path)); } catch (IOException e) { e.printStackTrace(); }
    }
}