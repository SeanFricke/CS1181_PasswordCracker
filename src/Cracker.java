import java.util.ArrayList;
import java.util.SortedMap;

public class Cracker {
    volatile private long password = 0;
    private boolean passFound;
    private final int passLength;
    private final String zipPath;
    private ArrayList<Worker> threadList = new ArrayList<>();

    public Cracker(String zipPath, int passLength){
        this.zipPath = zipPath;
        this.passFound = false;
        this.passLength = passLength;
    }
    synchronized public long getWork(){
        long job = password;
        if (job > Math.pow(26,this.passLength)){
//            System.out.println("Test");
            return -1;
        } else {
            password++;
            return job;
        }
    }

    public void safeTerminate(){
        passFound = true;
    }

    public static String decodePass(long pass26, int passLength) {
        String result = "";
        long calc = pass26;
        for (int i = 0; i < passLength; i++) {
            byte r = (byte) (calc % 26);
            result = (char) (r + 97) + result;
            calc = (calc - r) / 26;
        }
        return result;
    }

    public long crack(int threads) throws InterruptedException {
        if (threads >= 4){
            System.out.println("Cannot use more than 4 threads, please choose thread count between 1-4");
        } else if (threads <= 0){
            System.out.println("Threads cannot be less than 1, please choose thread count between 1-4");
        } else {
            long startTime, endTime;
            startTime = System.currentTimeMillis();
            for (int i = 0; i < threads; i++) {
                Worker tempWork = new Worker(i, this);
                threadList.add(tempWork);
            }

            for (Worker thread : threadList){
                thread.start();
            }

            for (Worker thread: threadList){
                thread.join();
            }
            endTime = System.currentTimeMillis();
            return endTime - startTime;
        }
        return -1;
    }

    public String getZip(){
        return zipPath;
    }

    public int getPassLength() {
        return passLength;
    }

    public boolean isPassFound() {
        return passFound;
    }
}
