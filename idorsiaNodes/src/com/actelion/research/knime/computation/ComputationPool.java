package com.actelion.research.knime.computation;

import java.util.concurrent.ForkJoinPool;

//Author: tl

public class ComputationPool {
    private static final ComputationPool INSTANCE = new ComputationPool();
    private ForkJoinPool pool;


    private ComputationPool() {
        this.pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors() - 1);
    }

    public static ComputationPool getInstance() {
        return INSTANCE;
    }

    public void execute(StoppableRecursiveAction task) {
        pool.execute(task);
    }
}
