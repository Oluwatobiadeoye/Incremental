package com.kamteezey;

import java.util.List;

@FunctionalInterface
public interface Updater<T> {

    T apply(List<T> inputs);

    
}
