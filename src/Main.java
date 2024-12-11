public class Main {
    public static final String ZIP_PATH = "protected3";// Zip file name without extension
    public static final int PASSWORD_LENGTH = 3; // Length of password that will be cracked
    public static final int NUM_THREADS = 5; // Number of threads to crack with

    public static void main(String[] args) throws InterruptedException {
        // TODO take out /tmp/ file dirs when extracting.
        long startTime, endTime;
        double timeDelta;

        Cracker zipCrack = new Cracker(ZIP_PATH, PASSWORD_LENGTH);
        startTime = System.currentTimeMillis();
        zipCrack.crack(NUM_THREADS);
        endTime = System.currentTimeMillis();
        timeDelta = (endTime - startTime) / 100.0;
        System.out.println(timeDelta);
    }
}
