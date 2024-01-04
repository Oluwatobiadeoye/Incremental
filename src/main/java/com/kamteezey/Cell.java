package com.kamteezey;

import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@EqualsAndHashCode(of = {"id"})
public class Cell<T> {

    private T value;
    private final UUID id;
    private int height;
    private int recomputeId;
    private int changeId;
    private boolean necessary;
    private final Updater<T> updater;
    private final List<Cell<T>> inputs;
    private boolean neverComputed = true;
    private final List<Cell<T>> dependants = new ArrayList<>();

    public Cell(T value) {
        this.value = value;
        this.updater = null;
        this.inputs = new ArrayList<>();
        this.id = UUID.randomUUID();
    }

    @SafeVarargs
    public Cell(Updater<T> updater, Cell<T>... inputs) {
        if (inputs.length == 0) {
            throw new IllegalArgumentException("Inputs size cannot be less than 1");
        }
        this.updater = updater;
        this.height = Arrays.stream(inputs).map(Cell::getHeight).mapToInt(v -> v).max().getAsInt() + 1;
        this.inputs = Arrays.asList(inputs);
        this.id = UUID.randomUUID();
    }


    protected int getHeight() {
        return height;
    }

    protected T getValue() {
        return value;
    }

    protected void setValue(T value) {
        this.value = value;
    }

    protected List<Cell<T>> getDependants() {
        return dependants;
    }

    protected List<Cell<T>> getInputs() {
        return inputs;
    }

    protected void addDependants(Cell<T> e) {
        this.dependants.add(e);
    }

    protected void addInput(Cell e) {
        this.inputs.add(e);
    }

    protected Updater<T> getUpdater() {
        return updater;
    }

    protected void setNecessary(boolean necessary) {
        this.necessary = necessary;
    }

    protected boolean getNecessary() {
      return necessary;
    }

    protected int getRecomputeId() {
        return recomputeId;
    }

    protected int getChangeId() {
        return changeId;
    }

    protected void setRecomputeId(int recomputeId) {
        this.recomputeId = recomputeId;
    }

    protected void setChangeId(int changeId) {
        this.changeId = changeId;
    }

    protected void compute(int stabilizationNumber) {
        if (recomputeId == stabilizationNumber) {
            return;
        }
        neverComputed = false;
        this.recomputeId = stabilizationNumber;
        if (!hasDependency())  {
            return;
        }

        List<T> dependencies = inputs.stream().map(Cell::getValue).collect(Collectors.toList());
        T newValue = updater.apply(dependencies);

        if (newValue != value) {
            this.value = newValue;
            this.changeId = stabilizationNumber;
        }
    }

    protected boolean isStale() {
        boolean recomputeIdLessThanChangeId = false;

        for (Cell<T> input: inputs) {
            if (this.recomputeId < input.getChangeId()) {
                recomputeIdLessThanChangeId = true;
                break;
            }
        }

        return necessary && (recomputeIdLessThanChangeId || neverComputed);
    }

    private boolean hasDependency() {
        return !inputs.isEmpty();
    }
}
