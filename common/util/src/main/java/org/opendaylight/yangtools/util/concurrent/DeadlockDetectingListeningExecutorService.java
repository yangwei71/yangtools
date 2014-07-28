/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.util.concurrent;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;
import com.google.common.util.concurrent.AbstractListeningExecutorService;
import com.google.common.util.concurrent.ForwardingListenableFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An implementation of ListeningExecutorService that attempts to detect deadlock scenarios that
 * could occur if clients invoke the returned Future's <ode>get</code> methods synchronously.
 * <p>
 * Deadlock scenarios are most apt to occur with a backing single-threaded executor where setting of
 * the Future's result is executed on the single thread. Here's a scenario:
 * <ul>
 * <li>Client code is currently executing in an executor's single thread.</li>
 * <li>The client submits another task to the same executor.</li>
 * <li>The client calls <code>get()</code> synchronously on the returned Future</li>
 * </ul>
 * The second submitted task will never execute since the single thread is currently executing
 * the client code which is blocked waiting for the submitted task to complete. Thus, deadlock has
 * occurred.
 * <p>
 * This class prevents this scenario via the use of a ThreadLocal variable. When a task is invoked,
 * the ThreadLocal is set and, when a task completes, the ThreadLocal is cleared. Futures returned
 * from this class override the <code>get</code> methods to check if the ThreadLocal is set. If it is,
 * an ExecutionException is thrown with a custom cause.
 *
 * @author Thomas Pantelis
 */
public class DeadlockDetectingListeningExecutorService extends AbstractListeningExecutorService {
    private final ThreadLocal<Boolean> deadlockDetector = new ThreadLocal<>();
    private final Function<Void, Exception> deadlockExceptionFunction;
    private final ExecutorService delegate;

    /**
     * Constructor.
     *
     * @param delegate the backing ExecutorService.
     * @param deadlockExceptionFunction Function that returns an Exception instance to set as the
     *             cause of the ExecutionException when a deadlock is detected.
     */
    public DeadlockDetectingListeningExecutorService(final ExecutorService delegate,
            final Function<Void,Exception> deadlockExceptionFunction) {
        this.delegate = checkNotNull(delegate);
        this.deadlockExceptionFunction = checkNotNull(deadlockExceptionFunction);
    }

    @Override
    public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
        return delegate.awaitTermination(timeout, unit);
    }

    @Override
    public boolean isShutdown() {
        return delegate.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return delegate.isTerminated();
    }

    @Override
    public void shutdown() {
        delegate.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return delegate.shutdownNow();
    }

    @Override
    public void execute(final Runnable command) {
        delegate.execute(wrapRunnable(command));
    }

    @Override
    public <T> ListenableFuture<T> submit(final Callable<T> task ) {
        final ListenableFutureTask<T> futureTask = ListenableFutureTask.create(wrapCallable(task));
        delegate.execute(futureTask);
        return wrapListenableFuture(futureTask);
    }

    @Override
    public ListenableFuture<?> submit( final Runnable task ) {
        ListenableFutureTask<Void> futureTask = ListenableFutureTask.create(wrapRunnable(task), null);
        delegate.execute(futureTask);
        return wrapListenableFuture(futureTask);
    }

    @Override
    public <T> ListenableFuture<T> submit(final Runnable task, final T result) {
        ListenableFutureTask<T> futureTask = ListenableFutureTask.create(wrapRunnable(task), result);
        delegate.execute(futureTask);
        return wrapListenableFuture(futureTask);
    }

    private Runnable wrapRunnable(final Runnable task) {
        return new Runnable() {
            @Override
            public void run() {
                deadlockDetector.set(Boolean.TRUE);
                try {
                    task.run();
                } finally {
                    deadlockDetector.set(null);
                }
            }
        };
    }

    private <T> Callable<T> wrapCallable(final Callable<T> delagate) {
        return new Callable<T>() {
            @Override
            public T call() throws Exception {
                deadlockDetector.set(Boolean.TRUE);
                try {
                    return delagate.call();
                } finally {
                    deadlockDetector.set(null);
                }
            }
        };
    }

    private <T> ListenableFuture<T> wrapListenableFuture(final ListenableFuture<T> delegate ) {
        /*
         *  This creates a forwarding Future that overrides calls to get(...) to check, via the ThreadLocal,
         * if the caller is doing a blocking call on a thread from this executor. If so, we detect this as
         * a deadlock and throw an ExecutionException even though it may not be a deadlock if there are
         * more than 1 thread in the pool. Either way, there's bad practice somewhere, either on the client
         * side for doing a blocking call or in the framework's threading model.
         */
        return new ForwardingListenableFuture.SimpleForwardingListenableFuture<T>(delegate) {
            @Override
            public T get() throws InterruptedException, ExecutionException {
                checkDeadLockDetectorTL();
                return super.get();
            }

            @Override
            public T get(final long timeout, final TimeUnit unit)
                    throws InterruptedException, ExecutionException, TimeoutException {
                checkDeadLockDetectorTL();
                return super.get(timeout, unit);
            }

            void checkDeadLockDetectorTL() throws ExecutionException {
                if (deadlockDetector.get() != null) {
                    throw new ExecutionException("A potential deadlock was detected.",
                            deadlockExceptionFunction.apply(null));
                }
            }
        };
    }
}
