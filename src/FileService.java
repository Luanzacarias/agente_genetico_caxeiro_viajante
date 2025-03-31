import java.io.*;

public class FileService {
    public static int[][] readInputFile(String fileName) {
        int[][] cities = null;

        File file = new File(fileName);
        if (!file.exists()) return null;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int row = 0;
            while ((line = reader.readLine()) != null) {
                String[] pesos = line.split("      ");
                if (cities == null) {
                    cities = new int[pesos.length][pesos.length];
                }
                for (int i = 1; i < pesos.length; i++) {
                    cities[row][i-1] = Integer.parseInt(pesos[i].trim());
                }
                row++;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return cities;
    }
}
