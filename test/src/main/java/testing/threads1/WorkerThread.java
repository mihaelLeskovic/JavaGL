package testing.threads1;

public class WorkerThread implements Runnable{
    private String name;
    private final SharedGameState sharedGameState;

    public WorkerThread(SharedGameState sharedGameState, String name) {
        this.sharedGameState = sharedGameState;
        this.name = name;
    }

    @Override
    public void run() {
        for(int i=0; i<1000; i++) {
            System.out.println("Currently working thread " + this.name + " fetching value: " + sharedGameState.getX());
            sharedGameState.incrementX();
        }
    }
}
