    import java.io.IOException;
    import java.nio.file.Files;
    import java.nio.file.Path;

    public class LeitorArquivo {

        public static String[] loadWords(Path txtFile) throws IOException {
            String textoFormatado = Files.readString(txtFile);

            //tudo minúsculo e remove pontuação
            textoFormatado = textoFormatado.toLowerCase().replaceAll("[^\\p{L}\\p{Nd}\"]+", " ");

            if (textoFormatado.trim().isEmpty()) {
                return new String[0];
            }

            return textoFormatado.trim().split("\\s+");
        }
    }
