package com.kamteezey;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Incremental<T> {

    private final AtomicInteger stabilizationNumber = new AtomicInteger(0);
    private final PriorityQueue<Cell<T>> recomputeHeap = new PriorityQueue<>(Comparator.comparingInt(Cell::getHeight));
    private final Set<Cell<T>> recomputeSet = new HashSet<>();
    private final Set<Cell<T>> observedCells = new HashSet<>();

    public void setValue(Cell<T> cell, T value) {
        T oldVal = cell.getValue();
        if (oldVal.equals(value)) return;
        int n = stabilizationNumber.incrementAndGet();
        cell.setChangeId(n);
        cell.setValue(value);

        for (Cell<T> e: cell.getDependants()) {
            if (e.getNecessary() && recomputeSet.add(e)) {
                recomputeHeap.add(e);
            }
        }
    }

    public T getValue(Cell<T> cell) {
        if (observedCells.contains(cell)) {
            stabilize();
        }

        return cell.getValue();
    }

    public void observe(Cell<T> cell) {
        if (observedCells.contains(cell)) return;
        observedCells.add(cell);
        cell.setNecessary(true);

        if (cell.isStale() && recomputeSet.add(cell)) {
            recomputeHeap.add(cell);
        }

        for (Cell<T> c: cell.getInputs()) {
            c.addDependants(cell);
            observe(c);
        }
    }

    private void stabilize() {
        if (recomputeHeap.isEmpty()) {
            return;
        }

        int n = stabilizationNumber.incrementAndGet();

        while (!recomputeHeap.isEmpty()) {
            Cell<T> top = recomputeHeap.poll();
            if (!top.isStale()) return;

            top.compute(n);

            if (top.getChangeId() == n) {
                for(Cell<T> d: top.getDependants()) {
                    if (d.getNecessary() && recomputeSet.add(d)) {
                        recomputeHeap.add(d);
                    }
                }
            }
        }
        recomputeSet.clear();
    }
}
