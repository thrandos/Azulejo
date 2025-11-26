/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.concurrency;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Utility class for working with Deferred.
 */
public final class Deferreds {

    private Deferreds() {
    }

    /**
     * Make a new Deferred from the given future, using the same thread
     * executor as the default executor.
     *
     * @param future The future
     * @param <V> The type returned by the future
     * @return A new Deferred
     */
    public static <V> Deferred<V> makeDeferred(ListenableFuture<V> future) {
        return makeDeferred(future, MoreExecutors.newDirectExecutorService());
    }

    /**
     * Make a new Deferred from the given future.
     *
     * @param future The future
     * @param executor The default executor
     * @param <V> The type returned by the future
     * @return A new Deferred
     */
    public static <V> Deferred<V> makeDeferred(ListenableFuture<V> future, ListeningExecutorService executor) {
        return new DeferredImpl<V>(future, executor);
    }

}
