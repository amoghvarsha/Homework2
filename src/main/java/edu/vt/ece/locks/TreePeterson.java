package edu.vt.ece.locks;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import edu.vt.ece.bench.ThreadId;

import static edu.vt.ece.util.DebugConfig.DEBUG;

public class TreePeterson implements Lock {

    private class Peterson implements Lock {

        private AtomicBoolean flag[] = new AtomicBoolean[2];
        private AtomicInteger victim;
   
        public Peterson()
        {
            flag[0] = new AtomicBoolean();
            flag[1] = new AtomicBoolean();
            victim  = new AtomicInteger();
            
            if (DEBUG)
                System.out.println("\nPeterson Lock with " + 2 + " threads initialized.");

        }

        @Override
        public void lock()
        {
            int id = ((ThreadId)Thread.currentThread()).getThreadId() ;
            int me = id % 2;
            int other = 1 - me;

            flag[me].set(true);
            victim.set(me);
            while (flag[other].get() && victim.get() == me) {}; // spin
        }

        @Override
        public void unlock()
        {
            int id = ((ThreadId)Thread.currentThread()).getThreadId();
            int me = id % 2;
            flag[me].set(false);
        }
    }


    private Peterson[] locks;
    private int threads;

    public TreePeterson() {
        this(2);
    }

    public TreePeterson(int threads)
    {
        this.threads = threads;
        this.locks = new Peterson[threads]; 
    
        for (int i = 0; i < locks.length; i++) {
            locks[i] = new Peterson();
        }
    }
    
    private int getLeafLock()
    {
        int id = ((ThreadId)Thread.currentThread()).getThreadId();
        return (threads + id)/2;
    }

    @Override
    public void lock()
    {
        int index = getLeafLock();
        while (index != 0)
        {
            locks[index].lock();
            index /= 2;
        }
    }

    private void unlock(int index)
    {
        if (index != 0)
        {
            unlock(index/2);
            locks[index].unlock();
        }
    }

    @Override
    public void unlock()
    {
        int index = getLeafLock();
        unlock(index); 
    }

}
