package com.actelion.research.knime.computation;

import java.util.concurrent.RecursiveAction;

public abstract class StoppableRecursiveAction extends RecursiveAction {
    private final StoppableRecursiveAction parent;
    private boolean stopRequested = false;

    StoppableRecursiveAction(StoppableRecursiveAction parent) {
        this.parent = parent;
    }

    public void requestStop() {
        this.stopRequested = true;
    }

    boolean isStopRequested() {
        if (parent != null) {
            return parent.isStopRequested();
        }
        return stopRequested;
    }
}
