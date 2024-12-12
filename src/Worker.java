import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class Worker extends Thread{
    private final String zipCopyName, dumpName;
    private final Cracker parentCracker;
    private final int ID;

    public Worker(int ID, Cracker cracker){
        this.ID = ID;
        this.parentCracker = cracker;
        zipCopyName = "zipCopy_" + parentCracker.getZip() + ID + ".zip";
        dumpName = "contents_" + parentCracker.getZip();
        try{
            Files.copy(Path.of(parentCracker.getZip() + ".zip"), Path.of(zipCopyName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        ZipFile zipFile;
        while (!parentCracker.isPassFound()){
            long dirtyPass = parentCracker.getWork();
            if (dirtyPass == -1){
                break;
            }
            String cleanPass = Cracker.decodePass(dirtyPass, parentCracker.getPassLength());
            try {
                zipFile = new ZipFile(zipCopyName);
                zipFile.setPassword(cleanPass);
                zipFile.extractAll(dumpName+"_tmp"+ID);
                System.out.printf("%s.zip cracked! Password: %s%n", parentCracker.getZip(), cleanPass);
                ZipFile zipOG = new ZipFile(parentCracker.getZip() + ".zip");
                zipOG.setPassword(cleanPass);
                zipOG.extractAll(dumpName);
                parentCracker.safeTerminate();
            } catch (ZipException e) {
                continue;
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        deleteCopy();
    }

    private void deleteCopy(){
        try {
            Files.delete(Path.of(zipCopyName));
            deleteTMP(dumpName+"_tmp"+ID);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteTMP(String path){
        File file = new File(path);
        if (file.isDirectory()){
            File[] childlist = file.listFiles();
            if(childlist != null){
                for(File childfile: childlist){
                    deleteTMP(childfile.getPath());
                }
            }
            file.delete();
        } else if (file.isFile()){
            file.delete();
        }
    }
}
