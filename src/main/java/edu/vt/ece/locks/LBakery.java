package edu.vt.ece.locks;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import edu.vt.ece.bench.ThreadId;

import static edu.vt.ece.util.DebugConfig.DEBUG;

public class LBakery implements Lock {

    private final int numOfThreads;
    private final int L;
    private final AtomicInteger[] ticket;    
    private final AtomicBoolean[] choosing; 

    public LBakery() {
        this(3,8);
    }


    public LBakery(int L, int numOfThreads) {
        this.numOfThreads = numOfThreads;
        this.L = L;
        this.ticket = new AtomicInteger[numOfThreads];
        this.choosing = new AtomicBoolean[numOfThreads];
        for (int i = 0; i < numOfThreads; i++) {
            this.ticket[i] = new AtomicInteger(0);       
            this.choosing[i] = new AtomicBoolean(false); 
        }
        
        if (DEBUG)
            System.out.println("LBakery Lock with " + numOfThreads + " threads and L = " + L + " initialized.");
    }

    private void chooseTicket(int id) {
        choosing[id].set(true); 
        ticket[id].set(findMax() + 1); 
        choosing[id].set(false); 
    }

    private void waitForTurn(int id) {
        while (true) {
            int count = 0;
            for (int j = 0; j < numOfThreads; j++) {
                if (j == id) continue;

                while (choosing[j].get()) {}

                int jTicket = ticket[j].get();
                int idTicket = ticket[id].get();

                if (jTicket != 0 && 
                   (jTicket < idTicket || 
                   (jTicket == idTicket && j < id))) {
                    count++;
                }
            }
            if (count < L) {
                break;
            }
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
        ticket[threadID].set(0); 
    }

}
