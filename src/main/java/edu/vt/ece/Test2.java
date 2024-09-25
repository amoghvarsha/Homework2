package edu.vt.ece;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import edu.vt.ece.bench.Counter;
import edu.vt.ece.bench.SharedCounter;
import edu.vt.ece.bench.TestThread2;
import edu.vt.ece.locks.*;

public class Test2 {

    private static final String LOCK_ONE = "LockOne";
    private static final String LOCK_TWO = "LockTwo";
    private static final String PETERSON = "Peterson";
    private static final String FILTER = "Filter";
    private static final String BAKERY = "Bakery";

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


    public static void main(String[] args) {

        try {
            printArgs(args);
            String lockClass = (args.length == 0 ? PETERSON : args[0]);
            int threadCount = (args.length <= 1 ? 16 : Integer.parseInt(args[1]));
            int totalIters = (args.length <= 2 ? 64000 : Integer.parseInt(args[2]));

            // Prevent division by zero
            if (threadCount <= 0) {
                throw new IllegalArgumentException("Thread count must be greater than zero.");
            }

            int iters = totalIters / threadCount;

            for (int i = 0; i < 2; i++) {
                run(lockClass, threadCount, iters);
            }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | InterruptedException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private static void run(String lockClass, int threadCount, int iters)
            throws InterruptedException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        
        Lock lockInstance;

        if (lockClass.equals(FILTER) || lockClass.equals(BAKERY)) {
            Class<?> lockClassObj = Class.forName("edu.vt.ece.locks." + lockClass);
            Constructor<?> lockConstructor = lockClassObj.getDeclaredConstructor(int.class);
            lockInstance = (Lock) lockConstructor.newInstance(threadCount);
        } else {
            lockInstance = (Lock) Class.forName("edu.vt.ece.locks." + lockClass).getDeclaredConstructor().newInstance();
        }

        final Counter counter = new SharedCounter(0, lockInstance);
        final TestThread2[] threads = new TestThread2[threadCount];
        TestThread2.reset();

        for (int t = 0; t < threadCount; t++) {
            threads[t] = new TestThread2(counter, iters);
        }

        for (int t = 0; t < threadCount; t++) {
            threads[t].start();
        }

        long totalTime = 0;
        for (int t = 0; t < threadCount; t++) {
            threads[t].join();
            totalTime += threads[t].getElapsedTime();
        }

        System.out.println("Average time per thread is " + totalTime / threadCount + "ms");
    }
}
