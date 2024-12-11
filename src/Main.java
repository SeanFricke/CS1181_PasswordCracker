import java.util.ArrayList;

public class Main {
    public static final String zipPath = "protected3"; // Zip file name without extension
    public static final int numThreads = 30;

    public static void main(String[] args) throws InterruptedException {
//        Cracker protect3 = new Cracker(zipPath, 3);
//        protect3.crack(numThreads);
        Cracker protect5 = new Cracker("protected5", 5);
        protect5.crack(numThreads);


    }
}
