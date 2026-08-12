package com.rusobr.common.util;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public final class ExceptionUtils {

    private ExceptionUtils() {
    }

    public static Throwable unwrapRootCause(Throwable throwable) {
        Throwable current = throwable;
        while (isAsyncWrapper(current) && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    private static boolean isAsyncWrapper(Throwable throwable) {
        return throwable instanceof ExecutionException
                || throwable instanceof CompletionException
                || throwable instanceof InvocationTargetException;
    }
}
