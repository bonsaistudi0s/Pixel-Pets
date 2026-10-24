package com.bonsai.pixelpets.util;

import java.util.Set;

public record DependencyData<T>(Set<T> prerequisites, boolean requireAll) implements IDependencyData<T> {
}
