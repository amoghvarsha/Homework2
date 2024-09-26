package edu.vt.ece.locks;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import edu.vt.ece.bench.ThreadId;

import static edu.vt.ece.util.DebugConfig.DEBUG;

public class Bakery implements Lock {

    private final int numOfThreads;
    private final AtomicInteger[] ticket;   
    private final AtomicBoolean[] choosing;
    
    public Bakery() {
        this(2);
    }

    public Bakery(int numOfThreads) {
        this.numOfThreads = numOfThreads;
        this.ticket = new AtomicInteger[numOfThreads];
        this.choosing = new AtomicBoolean[numOfThreads];
        for (int i = 0; i < numOfThreads; i++) {
            this.ticket[i] = new AtomicInteger(0);       // Initialize each ticket with 0
            this.choosing[i] = new AtomicBoolean(false); // Initialize each choosing flag with false
        }
        
        if (DEBUG)
            System.out.println("\nBakery Lock with " + numOfThreads + " threads initialized.");
    }
    
    private void chooseTicket(int id) {
        choosing[id].set(true); // Indicate that this thread is choosing a ticket
        ticket[id].set(findMax() + 1); // Assign the ticket
        choosing[id].set(false); // Indicate that this thread is done choosing
    }

    private void waitForTurn(int id) {
        for (int j = 0; j < numOfThreads; j++) {
            if (j == id) continue;

            // Wait until thread j is done choosing
            while (choosing[j].get()) {}

            // Wait until thread id's turn comes, with lexicographical ordering
            while (ticket[j].get() != 0 && 
                   (ticket[j].get() < ticket[id].get() || 
                   (ticket[j].get() == ticket[id].get() && j < id))) {}
        }
    }
    
    private int findMax() {
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < numOfThreads; i++) {
            max = Math.max(max, ticket[i].get());
        }
        return max;
    }

    @Override
    public void lock() {
        int threadID = ((ThreadId)Thread.currentThread()).getThreadId();
        chooseTicket(threadID);
        waitForTurn(threadID);
    }

    @Override
    public void unlock() {
        int threadID = ((ThreadId)Thread.currentThread()).getThreadId();
        ticket[threadID].set(0); // Reset the ticket
    }
}
