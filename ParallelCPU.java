public class ParallelCPU {

    public static int countWords(String[] text, int numThreads, String palavraAlvo) throws InterruptedException {

        String alvo = palavraAlvo.toLowerCase();
        int total = text.length;


        int block = total / numThreads;
        int[] partial = new int[numThreads];

        Thread[] threads = new Thread[numThreads];

        for (int t = 0; t < numThreads; t++) {

            final int threadIndex = t;
            final int start = t * block;
            final int end = (t == numThreads - 1) ? total : start + block;

            threads[t] = new Thread(() -> {

                int local = 0;

                for (int i = start; i < end; i++) {
                    if (text[i].equals(alvo)) {
                        local++;
                    }
                }

                partial[threadIndex] = local;
            });

            threads[t].start();
        }

        // espera todas concluírem
        for (Thread th : threads) {
            th.join();
        }

        int finalCount = 0;
        for (int c : partial) finalCount += c;

        return finalCount;
    }
}
