import java.util.ArrayList;

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

    public void crack(int threads) throws InterruptedException {
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
