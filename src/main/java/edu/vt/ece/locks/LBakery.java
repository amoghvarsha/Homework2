package edu.vt.ece.locks;

import edu.vt.ece.bench.ThreadId;

import static edu.vt.ece.util.DebugConfig.DEBUG;

import java.util.concurrent.atomic.AtomicInteger;

public class LBakery implements Lock {

    private final int numOfThreads;
    private volatile int[] ticket;
    private volatile boolean[] choosing;
    private AtomicInteger threadsinCriticalSection; // Use AtomicInteger for thread-safe operations
    private final int L;

    public LBakery() {
        this(3, 8);
    }

    public LBakery(int L, int numOfThreads) {
        this.L = L;
        this.numOfThreads = numOfThreads;
        this.ticket = new int[numOfThreads];
        this.choosing = new boolean[numOfThreads];
        for (int i = 0; i < numOfThreads; i++) {
            this.ticket[i] = 0;       // Initialize each ticket with 0
            this.choosing[i] = false; // Initialize each choosing flag with false
        }

        this.threadsinCriticalSection = new AtomicInteger(0); // Initialize to 0 (no threads in the critical section)

        if (DEBUG)
            System.out.println("\nLBakery Lock with " + numOfThreads + " threads and L " + L + " initialized.");
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
    
            // Wait until thread id's turn comes, with lexicographical ordering and L-exclusion check
            while (ticket[j] != 0 &&
                   (ticket[j] < ticket[id] ||
                   (ticket[j] == ticket[id] && j < id)) &&
                   threadsinCriticalSection.get() >= L) {
            }
        }
    
        // Now wait until we can safely enter the critical section (up to L threads allowed)
        while (true) {
            int current = threadsinCriticalSection.get();
            if (current < L) {
                // If fewer than L threads are in the critical section, attempt to increment atomically
                if (threadsinCriticalSection.compareAndSet(current, current + 1)) {
                    break; // Successfully entered the critical section
                }
            } 
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
        int threadID = ((ThreadId) Thread.currentThread()).getThreadId();
        chooseTicket(threadID);
        waitForTurn(threadID);

        if (DEBUG)
            System.out.println("Thread " + threadID + " AL, L is " + threadsinCriticalSection.get());
    }

    @Override
    public void unlock() {
        int threadID = ((ThreadId) Thread.currentThread()).getThreadId();
        ticket[threadID] = 0; // Reset the ticket
        threadsinCriticalSection.decrementAndGet(); // Decrement after leaving the critical section
        
        if (DEBUG)
            System.out.println("Thread " + threadID + " RL, L is " + threadsinCriticalSection.get());
    }
    
}

