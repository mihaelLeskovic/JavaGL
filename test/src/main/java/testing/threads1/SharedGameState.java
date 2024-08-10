package testing.threads1;

public class SharedGameState {
    private final Object lock = new Object();
    private int x;

    public SharedGameState() {
        x = 0;
    }

    public int getX() {
        synchronized (lock) {
            return x;
        }
    }

    public void setX(int x) {
        synchronized (lock) {
            this.x = x;
        }
    }

    public void incrementX() {
        synchronized (lock) {
            this.x++;
        }
    }
}
