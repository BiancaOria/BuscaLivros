
public class SerialCPU {

    public static long count(String[] text, String palavraAlvo) {
        long count = 0;
        String alvo = palavraAlvo.toLowerCase();

        for (String word : text) {
            if (word.equals(alvo)) {
                count++;
            }
        }
        return count;
    }
}
