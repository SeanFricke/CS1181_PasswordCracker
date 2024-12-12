/* From a sample size of 10 runs between 3 and 4 threads for the 3 letter password:
 * 4 threads was on average, about 115.7ms shorter.
 * Which is equivalent to 22.5% faster.
 */

import java.io.FileNotFoundException;

/**
 * Project 4: Password Cracker
 * @author Sean Fricke
 * @since 24/12/11
 */
public class Main {
    public static final String ZIP_PATH = "protected3";// Zip file name without extension
    public static final int PASSWORD_LENGTH = 3; // Length of password that will be cracked
    public static final int NUM_THREADS = 4; // Number of threads to crack with

    public static void main(String[] args) throws InterruptedException, FileNotFoundException, ArithmeticException {
        Cracker zipCrack = new Cracker(ZIP_PATH, PASSWORD_LENGTH); // Configure cracker with the needed target info
        long crackTime = zipCrack.crack(NUM_THREADS); // Start crack and save time delta as crackTime
        // Report logging
        System.out.printf(
                "%n---Report---%n" +
                "Target zip: %s.zip%n" +
                "Password length: %d%n" +
                "Threads: %d%n" +
                "Crack Length: %d minutes, %d seconds, %d ms",
                ZIP_PATH, PASSWORD_LENGTH, NUM_THREADS, crackTime/1000/60, (crackTime/1000)%60, crackTime%(1000));
    }
}
