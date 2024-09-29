package edu.vt.ece.bench;

public class TestThread extends Thread implements ThreadId {
    
    private static int ID_GEN = 0;
    private Counter counter;
    private int id;

    private static final int MAX_COUNT = 1000;
    
    public TestThread(Counter counter) {
        id = ID_GEN++;
        this.counter = counter;
    }

    public static void reset() {
        ID_GEN = 0;
    }

    @Override
    public void run() {
        for(int i=0; i<MAX_COUNT; i++)
            counter.getAndIncrement();
        System.out.println("Thread " + id + " DONE.. <Counter:" + counter + ">");
    }

    public int getThreadId(){
        return id;
    }
}
