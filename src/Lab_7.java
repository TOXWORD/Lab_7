import java.io.File;
import java.util.Scanner;

public class Lab_7 {
    public static void main(String[] args) {
        try {
            DataBaseFinder dbf = new DataBaseFinder();
            dbf.createDataBase();
            dbf.loadToDataBase();

            try (Scanner sc = new Scanner(new File("requests.txt"))) {
                int pos = 0;
                while (sc.hasNextLine()) {
                    dbf.find(sc.nextLine(), pos);
                    pos++;
                }
            }

            dbf.dropDataBase();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
