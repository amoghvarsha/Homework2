package edu.vt.ece;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import edu.vt.ece.bench.Counter;
import edu.vt.ece.bench.SharedCounter;
import edu.vt.ece.bench.TestThread;
import edu.vt.ece.locks.*;

import static edu.vt.ece.util.DebugConfig.DEBUG;

public class Test {

    private static int L            = 4;
    private static int THREAD_COUNT = 2;

    private static final String LOCK_ONE      = "LockOne";
    private static final String LOCK_TWO      = "LockTwo";
    private static final String PETERSON      = "Peterson";
    private static final String FILTER        = "Filter";
    private static final String BAKERY        = "Bakery";
    private static final String L_BAKERY      = "LBakery";
    private static final String TREE_PETERSON = "TreePeterson";
    
    public static void printArgs(String[] args) {

        System.out.println("\n************ Command-Line Arguments ************");
        if (args.length == 0) {
            System.out.println("No arguments provided.");
        } else {
            for (int i = 0; i < args.length; i++) {
                System.out.println((i + 1) + ". " + args[i]);
            }
        }
        System.out.println("************************************************\n");
    }

    private static Lock createLock(String lockClass, int threadCount, int l)
    throws ClassNotFoundException, NoSuchMethodException, 
           IllegalAccessException, InvocationTargetException, 
           InstantiationException {

        Class<?> lockClassObj = Class.forName("edu.vt.ece.locks." + lockClass);
        Constructor<?> lockConstructor;

        switch (lockClass) {
            case L_BAKERY:
                lockConstructor = lockClassObj.getDeclaredConstructor(int.class, int.class);
                return (Lock) lockConstructor.newInstance(l, threadCount);

            case FILTER:
            case BAKERY:
            case TREE_PETERSON:
                lockConstructor = lockClassObj.getDeclaredConstructor(int.class);
                return (Lock) lockConstructor.newInstance(threadCount);

            case LOCK_ONE:
            case LOCK_TWO:
            case PETERSON:
            default:
                lockConstructor = lockClassObj.getDeclaredConstructor();
                return (Lock) lockConstructor.newInstance();
        }
    }

    private static void run(String lockClass, int threadCount, int l)
        throws InterruptedException, ClassNotFoundException, 
                IllegalAccessException, InstantiationException, 
                NoSuchMethodException, InvocationTargetException {

        Lock lock = createLock(lockClass, threadCount, l);

        final Counter counter = new SharedCounter(0, lock);
        final TestThread[] threads = new TestThread[threadCount];
        TestThread.reset();

        for (int t = 0; t < threadCount; t++) {
            threads[t] = new TestThread(counter);
        }

        for (int t = 0; t < threadCount; t++) {
            threads[t].start();
        }

        for (int t = 0; t < threadCount; t++) {
           threads[t].join();
        }
    
    }
            
    public static void main(String[] args) {

        try {
            if (DEBUG)
                printArgs(args);
            
            String lockClass = (args.length == 0 ? PETERSON     : args[0]);
            int threadCount  = (args.length <= 1 ? THREAD_COUNT : Integer.parseInt(args[1]));
            int l            = (args.length <= 2 ? L            : Integer.parseInt(args[2]));

            if (threadCount <= 0 ) {
                throw new IllegalArgumentException("Thread count must be greater than zero.");
            }

            run(lockClass, threadCount, l);

        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | InterruptedException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
