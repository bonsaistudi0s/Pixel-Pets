package com.bonsai.pixelpets.util;

import java.util.Set;

public interface IDependencyData<T> {
    Set<T> prerequisites();
    boolean requireAll();
}
