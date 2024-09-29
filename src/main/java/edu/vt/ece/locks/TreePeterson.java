package edu.vt.ece.locks;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import edu.vt.ece.bench.ThreadId;

import static edu.vt.ece.util.DebugConfig.DEBUG;

public class TreePeterson implements Lock {

    private class Peterson {

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

        public void lock(int id)
        {
            int me = id;
            int other = 1 - me;

            flag[me].set(true);
            victim.set(me);

            while (flag[other].get() && victim.get() == me) {}; // spin
        }

        public void unlock(int id)
        {
            int me = id;
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
        this.locks = new Peterson[threads-1]; 
    
        for (int i = 0; i < locks.length; i++) {
            locks[i] = new Peterson();
        }
    }
    
    private int getThreadLeafIndex()
    {
        int threadId = ((ThreadId)Thread.currentThread()).getThreadId();
        return (threads - 1) + threadId;
    }

    @Override
    public void lock()
    {
        // Acquire all the locks by
        // traversing from the leaf lock to the root lock
        // by picking left parent lock
        int index = getThreadLeafIndex();
        while (index != 0)
        {
            int parentIndex = (index - 1) / 2;
            int id          = index % 2;

            if (DEBUG)
                System.out.printf("Thread %d waiting for lock %d at node: %d\n", ((ThreadId)Thread.currentThread()).getThreadId(), id, parentIndex);
            locks[parentIndex].lock(id); 
            if (DEBUG)
                System.out.printf("Thread %d acquired for lock %d at node: %d\n", ((ThreadId)Thread.currentThread()).getThreadId(), id, parentIndex);
            index = parentIndex;
        }
    }

    private void unlock(int index)
    {
        // Release all the locks by
        // traversing from the root lock to the leaf lock
        // recursively
        if (index != 0)
        {
            int parentIndex = (index - 1) / 2;
            int id          = index % 2;

            unlock(parentIndex);
            if (DEBUG)
                System.out.printf("Thread: %d released lock %d at node: %d\n", ((ThreadId)Thread.currentThread()).getThreadId(), id, parentIndex);
            locks[parentIndex].unlock(id);
            
        }
    }

    @Override
    public void unlock()
    {
        int index = getThreadLeafIndex();
        unlock(index); 
    }
}
