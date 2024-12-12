import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * Zip password bruteforce tool, with support for up to 13 character passwords a-z.<br>
 * Can only run up to and including 4 threads.
 */
public class Cracker {
    volatile private long password = 0; // Password to start at (in base 10)
    private boolean passFound; // Signal for sibling thread termination after successful attempt.
    private final int passLength; // Length of password in chars to look for
    private final String zipPath; // Path to target zip file
    private ArrayList<Worker> threadList = new ArrayList<>(); // List of threads that are attempting to bruteforce

    /**
     * Constructor method to create a new Cracker object.
     * @param zipPath Target zip file to crack WITHOUT the .zip extension
     * @param passLength Char length of the password for the target zip file
     * @throws FileNotFoundException Zip file does not exist
     * @throws ArithmeticException Password would be too extensive to bruteforce. The datatype will overflow.
     */
    public Cracker(String zipPath, int passLength) throws FileNotFoundException, ArithmeticException {
        // Check if zip exists and set the field as such if true
        if (Path.of(zipPath + ".zip").toFile().exists()){
            this.zipPath = zipPath;
        } else {
            throw new FileNotFoundException("Could not find " + zipPath + ".zip in the working directory");
        }

        // If password can fit in a long datatype as base26 with given length, set the field to its length
        if (password <= 13 && password > 0){
            this.passLength = passLength;
        } else {
            throw new ArithmeticException("Password datatype can only hold character length up to 13");
        }
    }

    /**
     * Work distribution method, which gives threads the next password to attempt.
     * @return Next possible password in base26, if no more possible passwords, return -1
     */
    synchronized public long getWork(){
        long job = password; // Set a temp variable to the current next password

        // If all passwords have been checked (26^len - 1), give quit status
        if (job > Math.pow(26,this.passLength)){
            return -1;
        } else {
            password++; // Increment password to next possible one
            return job; // Return with password from the start of call
        }
    }

    /**
     * Start terminating all threads on password found, waits until the thread has completed current task loop
     * iteration as not to interrupt it.
     */
    public void safeTerminate(){
        passFound = true;
    }

    /**
     * Decode password using base26 into "blocks" of a-z chars using ASCII code
     * @param pass26 long with base26 format in mind, aka password attempt
     * @param passLength Number of chars to pull from it and return.
     * @return String of length <i>passLength</i> that was pulled from the password long in lowercase chars
     */
    public static String decodePass(long pass26, int passLength) {
        String result = ""; // Start with empty string
        long calc = pass26; // pull copy of pass26 for reference and manipulation

        /*
            For the length of chars to pull out of the long:
             - Find the mod 26 of the stored long, which is the last "block" in the base26
             - Add the char equivalent representation (offset to be in the range of ASCII a-z)
                to the start of the string, since we are pulling it off sorta like a stack, so that the first block is
                at the end of the final string to avoid it coming out with a 'little endian'-like format.
             - Do a base26 equivalent right bitshift by subtracting the first block and then dividing it by 26
             - Repeat as now it has been shifted
            Finally, return the new password string
        */
        for (int i = 0; i < passLength; i++) {
            byte r = (byte) (calc % 26);
            result = (char) (r + 97) + result;
            calc = (calc - r) / 26;
        }
        return result;
    }

    /**
     * Start the crack with a given number of threads, bruteforcing it.
     * @param threads Number of threads to crack the password with. Cannot be more than 4.
     * @return Amount of time it took (in ms) to crack the password, or get through the attempts if unsuccessful
     * @throws InterruptedException InterruptedException for thread join.
     */
    public long crack(int threads) throws InterruptedException {
        // Check to see if 0 < threads < 5
        if (threads > 4){
            System.out.println("Cannot use more than 4 threads, please choose thread count between 1-4");
        } else if (threads <= 0){
            System.out.println("Threads cannot be less than 1, please choose thread count between 1-4");
        } else {
            this.passFound = false; // Assign password found as false before cracking
            long startTime, endTime; // Define time delta vars
            startTime = System.currentTimeMillis(); // Capture start time of crack

            // Create new threads and add each to list
            for (int i = 0; i < threads; i++) {
                Worker tempWork = new Worker(i, this);
                threadList.add(tempWork);
            }

            // Start each thread in list
            for (Worker thread : threadList){
                thread.start();
            }

            // Wait for each thread in list to finish
            for (Worker thread: threadList){
                thread.join();
            }

            endTime = System.currentTimeMillis(); // Capture end time of crack
            return endTime - startTime; // Return the time delta
        }
        return -1; // If invalid number of threads, return -1 for status code
    }

    /**
     * Get zip file name/path
     * @return Path of zip file as a string
     */
    public String getZip(){
        return zipPath;
    }

    /**
     * Get length of password for crack attempt
     * @return Num of chars of the password attempts as int
     */
    public int getPassLength() {
        return passLength;
    }

    /**
     * Get whether correct password has been found at this point.
     * @return Whether password has been found or not, as boolean
     */
    public boolean isPassFound() {
        return passFound;
    }
}
