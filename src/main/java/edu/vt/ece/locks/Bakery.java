package edu.vt.ece.locks;

import edu.vt.ece.bench.ThreadId;

import static edu.vt.ece.util.DebugConfig.DEBUG;

public class Bakery implements Lock {

    private final int numOfThreads;
    private volatile int[] ticket;
    private volatile boolean[] choosing;
    
    public Bakery() {
        this(2);
    }

    public Bakery(int numOfThreads) {
        this.numOfThreads = numOfThreads;
        this.ticket = new int[numOfThreads];
        this.choosing = new boolean[numOfThreads];
        for (int i = 0; i < numOfThreads; i++) {
            this.ticket[i] = 0;       // Initialize each ticket with 0
            this.choosing[i] = false; // Initialize each choosing flag with false
        }
        
        if (DEBUG)
            System.out.println("\nBakery Lock with " + numOfThreads + " threads initialized.");
    }
    
    private void chooseTicket(int id) {
        choosing[id] = true; // Indicate that this thread is choosing a ticket
        ticket[id] = findMax() + 1; // Assign the ticket
        choosing[id] = false; // Indicate that this thread is done choosing
    }

    private void waitForTurn(int id) {
        for (int j = 0; j < numOfThreads; j++) {
            if (j == id) continue;

            // Wait until thread j is done choosing
            while (choosing[j]) {}

            // Wait until thread id's turn comes, with lexicographical ordering
            while (ticket[j] != 0 && 
                   (ticket[j] < ticket[id] || 
                   (ticket[j] == ticket[id] && j < id))) {}
        }
    }
    
    private int findMax() {
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < numOfThreads; i++) {
            max = Math.max(max, ticket[i]);
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
        ticket[threadID] = 0; // Reset the ticket
    }
}
