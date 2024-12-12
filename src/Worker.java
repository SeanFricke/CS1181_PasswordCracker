import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Worker thread class that will attempt to bruteforce the zip file
 */
public class Worker extends Thread{
    private final String zipCopyName, dumpName; // Generated asset name references for file IO
    private final Cracker parentCracker; // Name of Cracker object it works for
    private final int ID; // ID of thread

    public Worker(int ID, Cracker cracker){
        this.ID = ID; // Set ID
        this.parentCracker = cracker; // Set parentCracker
        zipCopyName = "zipCopy_" + parentCracker.getZip() + ID + ".zip"; // Generate copy name from target zip name and self ID
        dumpName = "contents_" + parentCracker.getZip(); // Generate output folder from target zip name

        // Make a personal copy of the zip file to work with
        try{
            Files.copy(Path.of(parentCracker.getZip() + ".zip"), Path.of(zipCopyName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Thread task function to pull and attempt using possible passwords as needed, until there are no more left
     */
    @Override
    public void run() {
        ZipFile zipFile; // Zip file object to use to manipulate

        // Until the termination signal has been given
        while (!parentCracker.isPassFound()){
            long dirtyPass = parentCracker.getWork(); // Get work from manager

            // If possible passwords have run out, stop as well
            if (dirtyPass == -1){
                break;
            }

            // Decode the password long from base10 to Base26 to String
            String cleanPass = Cracker.decodePass(dirtyPass, parentCracker.getPassLength());

            // Attempt the extraction with given password
            try {
                zipFile = new ZipFile(zipCopyName); // Set target file as personal zip copy
                zipFile.setPassword(cleanPass); // Set password to current attempt
                zipFile.extractAll(dumpName+"_tmp"+ID); // Attempt to unlock and extract all data from target zip to tmp directory
                System.out.printf("Cracked! Password: '%s'%n", cleanPass); // If attempt is successful (as it has not caught an exception), log it
                ZipFile zipOG = new ZipFile(parentCracker.getZip() + ".zip"); // Set target file as the main zip target
                zipOG.setPassword(cleanPass); // Set the found correct password for extraction
                zipOG.extractAll(dumpName); // Extract it successfully to a similar directory name to the zip name.
                parentCracker.safeTerminate(); // Shut down all threads (including self)
            } catch (ZipException e) {
                // Unknown if needed or not, but placeholder continue statement to fill the catch block.
                // Put any code for incorrect attempt logging here.
                continue;
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        deleteCopy(); // Cleans up all temporary assets during cracking. May not clean up properly if terminated forcefully beforehand.
    }

    /**
     * Cleans up all files used during cracking
     */
    private void deleteCopy(){
        try {
            Files.delete(Path.of(zipCopyName)); // Delete personal zip copies
            deleteTMP(dumpName+"_tmp"+ID); // Delete all temporary output directories during bruteforce
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Recursively deletes all files in a directory tree, from a given path downwards, then deletes the directory itself
     * @param path Path to a directory to recursively delete
     */
    private void deleteTMP(String path){
        File file = new File(path); // Directory/file path

        // If file is a directory
        if (file.isDirectory()){
            File[] childlist = file.listFiles(); // Get all the child files in directory

            // If there is children in the directory
            if(childlist != null){
                // Recursively delete each of the children too
                for(File childfile: childlist){
                    deleteTMP(childfile.getPath());
                }
            }
            file.delete(); // Delete directory once there is no children left inside it

        } else if (file.isFile()){
            file.delete(); // Delete file right away as there is no need for further recursive deletion
        }
    }
}
