public class Main {
    public static final String ZIP_PATH = "protected3";// Zip file name without extension
    public static final int PASSWORD_LENGTH = 3; // Length of password that will be cracked
    public static final int NUM_THREADS = 3; // Number of threads to crack with

    public static void main(String[] args) throws InterruptedException {
        // TODO take out /tmp/ file dirs when extracting.


        Cracker zipCrack = new Cracker(ZIP_PATH, PASSWORD_LENGTH);
        long crackTime = zipCrack.crack(NUM_THREADS);
        System.out.println(crackTime);
    }
}
