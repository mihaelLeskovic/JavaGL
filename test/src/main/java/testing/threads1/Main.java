package testing.threads1;

public class Main {
    static private Thread thread1;
    static private Thread thread2;
    static private SharedGameState sharedGameState;

    public static void main(String[] args) throws InterruptedException {
        sharedGameState = new SharedGameState();

        thread1 = new Thread(new WorkerThread(sharedGameState, "thread1"));
        thread2 = new Thread(new WorkerThread(sharedGameState, "thread2"));

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        System.out.println("Rezultat: " + sharedGameState.getX());
    }
}
