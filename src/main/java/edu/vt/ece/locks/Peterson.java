package edu.vt.ece.locks;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import edu.vt.ece.bench.ThreadId;

import static edu.vt.ece.util.DebugConfig.DEBUG;

public class Peterson implements Lock{

    private AtomicBoolean flag[] = new AtomicBoolean[2];
    private AtomicInteger victim;

    public Peterson() {
        flag[0] = new AtomicBoolean();
        flag[1] = new AtomicBoolean();
        victim = new AtomicInteger();
        
        if (DEBUG)
            System.out.println("\nPeterson Lock with " + 2 + " threads initialized.");
    }

    @Override
    public void lock() {
        int i = ((ThreadId)Thread.currentThread()).getThreadId();
        int j = 1-i;
        flag[i].set(true);
        victim.set(i);
        while(flag[j].get() && victim.get() == i);
    }

    @Override
    public void unlock() {
        int i = ((ThreadId)Thread.currentThread()).getThreadId();
        flag[i].set(false);
    }
}
