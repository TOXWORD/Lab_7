import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Lab_7 {

    private static final Logger logger = Logger.getLogger("log");

    static {
        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream("logging.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
            logger.info(e.getMessage());
        }
    }
}
