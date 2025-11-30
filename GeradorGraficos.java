import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import javax.imageio.ImageIO;
import java.util.*;
import java.util.List;

/**
 * GeradorGraficos
 * - Lê CSVs em: Resultados/serial, Resultados/cpu, Resultados/gpu
 * - Gera imagens (PNG) em: imagens/serial.png, imagens/cpu.png, imagens/gpu.png
 * - Usa apenas Swing para desenhar (renderiza JPanel em BufferedImage)
 * - Cores atualizadas para melhor contraste e harmonia.
 */
public class GeradorGraficos {

    // Cores por livro
    private static final Map<String, Color> BOOK_COLORS = new HashMap<>();
    static {
        BOOK_COLORS.put("DonQuixote", Color.decode("#6E44FF")); // Roxo
        BOOK_COLORS.put("Dracula", Color.decode("#FF90B3"));    // Rosa
        // MobyDick: Ciano escuro/harmônico (DarkCyan), visível e com bom contraste
        BOOK_COLORS.put("MobyDick", Color.decode("#008B8B"));
    }

    private static final String BASE = "Resultados";
    private static final String OUTDIR = "imagens";

    public static void gerarGraficos() {
        try {
            Files.createDirectories(Paths.get(OUTDIR));
        } catch (IOException ignored) {}

        // Coleta dados
        Map<String, Map<Integer, Long>> serial = lerSerial();
        Map<String, Map<Integer, Map<Integer, Long>>> cpu = lerCPU();
        Map<String, Map<Integer, Long>> gpu = lerGPU();

        // Gera PNGs
        gerarPNGSerial(serial, OUTDIR + "/serial.png");
        gerarPNGCpu(cpu, OUTDIR + "/cpu.png");
        gerarPNGGPU(gpu, OUTDIR + "/gpu.png");

        System.out.println("✅ Gráficos gerados em: " + OUTDIR + "/ (serial.png, cpu.png, gpu.png)");
    }

    // ----------------- Leitura dos CSVs -----------------

    private static Map<String, Map<Integer, Long>> lerSerial() {
        Map<String, Map<Integer, Long>> resultado = new TreeMap<>();
        Path dir = Paths.get(BASE, "serial");
        if (!Files.exists(dir)) return resultado;

        try {
            Files.list(dir).filter(p -> p.toString().endsWith(".csv")).forEach(p -> {
                String nomeArquivo = p.getFileName().toString();
                String book = extrairNomeLivro(nomeArquivo);
                try (BufferedReader br = Files.newBufferedReader(p)) {
                    String header = br.readLine();
                    String linha;
                    while ((linha = br.readLine()) != null) {
                        String[] cols = linha.split(",");
                        if (cols.length >= 5) {
                            int exec = Integer.parseInt(cols[1].trim());
                            long tempo = Long.parseLong(cols[4].trim());
                            resultado.putIfAbsent(book, new TreeMap<>());
                            resultado.get(book).put(exec, tempo);
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
                    String header = br.readLine();
                    String linha;
                    while ((linha = br.readLine()) != null) {
                        String[] cols = linha.split(",");
                        if (cols.length >= 5) {
                            int exec = Integer.parseInt(cols[1].trim());
                            long tempo = Long.parseLong(cols[4].trim());
                            out.putIfAbsent(book, new TreeMap<>());
                            out.get(book).putIfAbsent(threadNum, new TreeMap<>());
                            out.get(book).get(threadNum).put(exec, tempo);
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
                    String header = br.readLine();
                    String linha;
                    while ((linha = br.readLine()) != null) {
                        String[] cols = linha.split(",");
                        if (cols.length >= 4) {
                            int exec = Integer.parseInt(cols[1].trim());
                            long tempo = Long.parseLong(cols[cols.length - 1].trim());
                            resultado.putIfAbsent(book, new TreeMap<>());
                            resultado.get(book).put(exec, tempo);
                        }
                    }
                } catch (Exception ignored) {}
            });
        } catch (IOException ignored) {}
        return resultado;
    }

    // ----------------- Helpers de parsing -----------------

    private static String extrairNomeLivro(String filename) {
        String base = filename;
        int idx = base.indexOf("_exec");
        if (idx > 0) base = base.substring(0, idx);
        if (base.endsWith(".csv")) base = base.substring(0, base.length() - 4);
        return base;
    }

    private static Integer extrairThreads(String filename) {
        int tIdx = filename.indexOf("_threads");
        if (tIdx >= 0) {
            String tail = filename.substring(tIdx + "_threads".length());
            if (tail.endsWith(".csv")) tail = tail.substring(0, tail.length() - 4);
            try {
                return Integer.parseInt(tail);
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    // ----------------- Desenho e exportação -----------------

    private static void gerarPNGSerial(Map<String, Map<Integer, Long>> dados, String outPath) {
        if (dados.isEmpty()) {
            System.out.println("⚠ serial: sem dados para gerar gráfico");
            return;
        }

        int W = 1000, H = 420;
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        aplicarQualidade(g);

        g.setColor(Color.WHITE);
        g.fillRect(0,0,W,H);
        drawAxes(g, W, H, "Execução", "Tempo (ms)", 40);

        int[] execs = {1,2,3};
        int startX = 120;
        int gapExec = 220;

        List<String> livros = new ArrayList<>(dados.keySet());
        int booksCount = livros.size();
        int bookOffsetStep = 30;
        int maxOffsetTotal = (booksCount-1)*bookOffsetStep;

        long maxTempo = maxTempoSerialOrGPU(dados);

        for (int bIdx = 0; bIdx < livros.size(); bIdx++) {
            String book = livros.get(bIdx);
            Color c = colorForBook(book);
            g.setColor(c);
            Map<Integer, Long> mapExec = dados.get(book);
            for (int e = 0; e < execs.length; e++) {
                int exec = execs[e];
                if (!mapExec.containsKey(exec)) continue;
                long tempo = mapExec.get(exec);
                int baseX = startX + e * gapExec;
                int x = baseX - maxOffsetTotal/2 + bIdx * bookOffsetStep;
                int y = valueToY(tempo, maxTempo, H, 40);

                g.fillOval(x-6, y-6, 12, 12);
                g.drawString(tempo + " ms", x-20, y-12);

                if (e > 0 && mapExec.containsKey(execs[e-1])) {
                    long prev = mapExec.get(execs[e-1]);
                    int prevX = startX + (e-1)*gapExec - maxOffsetTotal/2 + bIdx * bookOffsetStep;
                    int prevY = valueToY(prev, maxTempo, H, 40);
                    g.setStroke(new BasicStroke(2f));
                    g.drawLine(prevX, prevY, x, y);
                }
            }
            // Legenda
            g.setStroke(new BasicStroke(1f));
            g.setColor(Color.BLACK);
            g.drawString(book, W - 180, 60 + bIdx * 18);
            g.setColor(colorForBook(book));
            g.fillRect(W - 210, 52 + bIdx * 18, 12, 12);
        }

        g.dispose();
        try { ImageIO.write(img, "png", new File(outPath)); } catch (IOException e) { System.out.println("Erro ao salvar serial.png: " + e.getMessage()); }
    }

    private static void gerarPNGCpu(Map<String, Map<Integer, Map<Integer, Long>>> dados, String outPath) {
        if (dados.isEmpty()) {
            System.out.println("⚠ cpu: sem dados para gerar gráfico");
            return;
        }

        Set<Integer> threadsSet = new TreeSet<>();
        for (Map<Integer, Map<Integer, Long>> m : dados.values()) threadsSet.addAll(m.keySet());
        List<Integer> threadsList = new ArrayList<>(threadsSet);
        if (threadsList.isEmpty()) {
            System.out.println("⚠ cpu: sem threads encontradas");
            return;
        }

        int W = 1200, H = 480;
        int MARGIN = 70; // Margem unificada para alinhar eixo e dados

        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        aplicarQualidade(g);

        // Fundo Branco
        g.setColor(Color.WHITE);
        g.fillRect(0,0,W,H);

        // Desenha eixos base
        drawAxes(g, W, H, "Threads", "Tempo (ms)", MARGIN);

        // Calcula Máximo
        long maxTempo = 1;
        Map<String, Map<Integer, Double>> medias = new LinkedHashMap<>();
        for (String book : dados.keySet()) {
            Map<Integer, Map<Integer, Long>> byThread = dados.get(book);
            Map<Integer, Double> avgMap = new TreeMap<>();
            for (Integer th : byThread.keySet()) {
                Map<Integer, Long> execMap = byThread.get(th);
                double sum = 0; int cnt=0;
                for (Long t : execMap.values()) { sum += t; cnt++; }
                if (cnt>0) {
                    double avg = sum/cnt;
                    avgMap.put(th, avg);
                    if ((long) Math.ceil(avg) > maxTempo) maxTempo = (long) Math.ceil(avg);
                }
            }
            medias.put(book, avgMap);
        }

        // --- NOVO: Desenha Escala do Eixo Y (Tempos) ---
        g.setColor(Color.GRAY);
        g.setFont(new Font("Arial", Font.PLAIN, 10));
        int numDivisions = 5; // Quantas divisões no eixo Y
        for (int i = 0; i <= numDivisions; i++) {
            long val = (maxTempo * i) / numDivisions;
            int yPos = valueToYDouble((double)val, maxTempo, H, MARGIN);

            // Desenha linha de grade leve (opcional, ajuda na leitura)
            g.setColor(new Color(240, 240, 240));
            g.drawLine(MARGIN, yPos, W - MARGIN, yPos);

            // Desenha o valor na esquerda (atrás do eixo Y)
            g.setColor(Color.DARK_GRAY);
            String label = String.valueOf(val);
            // Ajusta posição X baseada no tamanho do texto para alinhar à direita
            int strW = g.getFontMetrics().stringWidth(label);
            g.drawString(label, MARGIN - strW - 8, yPos + 4);
        }
        // ------------------------------------------------

        // Define posições X para threads
        int left = MARGIN + 30;
        int right = W - 120;
        int plotW = right - left;
        int gap = plotW / Math.max(1, threadsList.size()-1);

        // Desenha labels do Eixo X (Threads)
        g.setColor(Color.LIGHT_GRAY);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        for (int i=0;i<threadsList.size();i++) {
            int x = left + i * gap;
            // Linha vertical leve
            g.setColor(new Color(245,245,245));
            g.drawLine(x, MARGIN, x, H - MARGIN);

            // Número da thread
            g.setColor(Color.BLACK);
            g.drawString(String.valueOf(threadsList.get(i)), x-5, H - (MARGIN - 20));
        }

        // Desenha as linhas dos livros
        int bIdx = 0;
        for (String book : medias.keySet()) {
            Color c = colorForBook(book);
            g.setColor(c);
            g.setStroke(new BasicStroke(3f));

            Map<Integer, Double> avgMap = medias.get(book);
            Integer prevX=null; Integer prevY=null;

            for (int i=0;i<threadsList.size();i++) {
                int th = threadsList.get(i);
                if (!avgMap.containsKey(th)) continue;

                double avg = avgMap.get(th);
                int x = left + i*gap;
                int y = valueToYDouble(avg, maxTempo, H, MARGIN);

                // Ponto da média
                g.fillOval(x-6,y-6,12,12);

                // Linhas finas para execuções individuais (dispersão)
                Map<Integer, Long> execs = dados.get(book).get(th);
                if (execs != null) {
                    int smallX = x - 20;
                    int step = 18;
                    g.setStroke(new BasicStroke(1.2f));
                    g.setColor(c.darker());
                    for (int execId : execs.keySet()) {
                        long val = execs.get(execId);
                        int sy = valueToY((int)val, maxTempo, H, MARGIN);
                        g.drawLine(smallX, H - MARGIN, smallX, sy); // Linha do chão até o ponto
                        g.fillOval(smallX-3, sy-3, 6,6);
                        smallX += step;
                    }
                    g.setStroke(new BasicStroke(3f)); // Restaura linha grossa
                    g.setColor(c);
                }

                // Conecta a linha média
                if (prevX != null) {
                    g.drawLine(prevX, prevY, x, y);
                }
                prevX = x; prevY = y;
            }

            // Legenda
            g.setStroke(new BasicStroke(1f)); // Reset stroke para texto/box
            g.setColor(Color.BLACK);
            g.drawString(book, W - 180, 60 + bIdx * 18);
            g.setColor(c);
            g.fillRect(W - 200, 52 + bIdx * 18, 12, 12);
            bIdx++;
        }

        g.dispose();
        try { ImageIO.write(img, "png", new File(outPath)); } catch (IOException e) { System.out.println("Erro ao salvar cpu.png: " + e.getMessage()); }
    }

    private static void gerarPNGGPU(Map<String, Map<Integer, Long>> dados, String outPath) {
        if (dados.isEmpty()) {
            System.out.println("⚠ gpu: sem dados para gerar gráfico");
            return;
        }

        int W = 1000, H = 420;
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        aplicarQualidade(g);

        g.setColor(Color.WHITE); g.fillRect(0,0,W,H);
        drawAxes(g, W, H, "Execução", "Tempo (ms)", 40);

        int[] execs = {1,2,3};
        int startX = 120; int gapExec = 220;
        List<String> livros = new ArrayList<>(dados.keySet());
        int booksCount = livros.size();
        int bookOffsetStep = 30;
        int maxOffsetTotal = (booksCount-1)*bookOffsetStep;

        long maxTempo = maxTempoSerialOrGPU(dados);

        for (int bIdx = 0; bIdx < livros.size(); bIdx++) {
            String book = livros.get(bIdx);
            Color c = colorForBook(book);
            g.setColor(c);
            Map<Integer, Long> mapExec = dados.get(book);
            for (int e = 0; e < execs.length; e++) {
                int exec = execs[e];
                if (!mapExec.containsKey(exec)) continue;
                long tempo = mapExec.get(exec);
                int baseX = startX + e * gapExec;
                int x = baseX - maxOffsetTotal/2 + bIdx * bookOffsetStep;
                int y = valueToY(tempo, maxTempo, H, 40);
                g.fillOval(x-6, y-6, 12, 12);
                g.drawString(tempo + " ms", x-20, y-12);
                if (e > 0 && mapExec.containsKey(execs[e-1])) {
                    long prev = mapExec.get(execs[e-1]);
                    int prevX = startX + (e-1)*gapExec - maxOffsetTotal/2 + bIdx * bookOffsetStep;
                    int prevY = valueToY(prev, maxTempo, H, 40);
                    g.setStroke(new BasicStroke(2f));
                    g.drawLine(prevX, prevY, x, y);
                }
            }
            g.setStroke(new BasicStroke(1f));
            g.setColor(Color.BLACK);
            g.drawString(book, W - 180, 60 + bIdx * 18);
            g.setColor(colorForBook(book));
            g.fillRect(W - 210, 52 + bIdx * 18, 12, 12);
        }

        g.dispose();
        try { ImageIO.write(img, "png", new File(outPath)); } catch (IOException e) { System.out.println("Erro ao salvar gpu.png: " + e.getMessage()); }
    }

    // ----------------- Utilitários de desenho -----------------

    private static void aplicarQualidade(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    private static void drawAxes(Graphics2D g, int W, int H, String xlabel, String ylabel, int margin) {
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1.5f));
        // y-axis
        g.drawLine(margin, margin, margin, H - margin);
        // x-axis
        g.drawLine(margin, H - margin, W - margin, H - margin);
        // labels
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString(ylabel, margin - 20, margin - 10);
        g.drawString(xlabel, W/2 - 20, H - 10);
    }

    private static int valueToY(long value, long maxValue, int H, int margin) {
        if (maxValue <= 0) return H - margin;
        double usable = H - 2.0 * margin;
        double ratio = (double) value / (double) maxValue;
        int y = (int) Math.round(H - margin - ratio * usable);
        return Math.max(margin, Math.min(H - margin, y));
    }

    private static int valueToYDouble(double value, long maxValue, int H, int margin) {
        if (maxValue <= 0) return H - margin;
        double usable = H - 2.0 * margin;
        double ratio = value / (double) maxValue;
        int y = (int) Math.round(H - margin - ratio * usable);
        return Math.max(margin, Math.min(H - margin, y));
    }

    private static long maxTempoSerialOrGPU(Map<String, Map<Integer, Long>> dados) {
        long max = 1;
        for (Map<Integer, Long> m : dados.values())
            for (Long v : m.values()) if (v != null && v > max) max = v;
        return max;
    }

    private static Color colorForBook(String book) {
        String b = book.replace(".csv","").replace(".txt","").trim();
        for (String key : BOOK_COLORS.keySet()) {
            if (b.toLowerCase().contains(key.toLowerCase())) return BOOK_COLORS.get(key);
        }
        if (!BOOK_COLORS.containsKey(b)) {
            Random r = new Random(b.hashCode());
            int R = 120 + r.nextInt(100);
            int G = 120 + r.nextInt(100);
            int B = 120 + r.nextInt(100);
            Color c = new Color(R, G, B);
            BOOK_COLORS.put(b, c);
            return c;
        } else {
            return BOOK_COLORS.get(b);
        }
    }
}