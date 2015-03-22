package jk_5.nailed.server.scheduler;

import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultPromise;
import jk_5.eventbus.EventHandler;
import jk_5.nailed.api.event.server.ServerPostTickEvent;
import jk_5.nailed.api.scheduler.Scheduler;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.*;

public class NailedScheduler implements Scheduler {

    private static final NailedScheduler INSTANCE = new NailedScheduler();

    private final DefaultEventExecutorGroup executor = new DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors() * 2);
    private final Queue<Runnable> executionQueue = new LinkedList<Runnable>();

    @Nonnull
    @Override
    public Future<?> submit(@Nonnull Runnable task) {
        return executor.submit(task);
    }

    @Nonnull
    @Override
    public <T> Future<T> submit(@Nonnull Runnable task, T result) {
        return executor.submit(task, result);
    }

    @Nonnull
    @Override
    public <T> Future<T> submit(@Nonnull Callable<T> task) {
        return executor.submit(task);
    }

    @Nonnull
    @Override
    public ScheduledFuture<?> schedule(@Nonnull Runnable command, long delay, @Nonnull TimeUnit unit) {
        return executor.schedule(command, delay, unit);
    }

    @Nonnull
    @Override
    public <V> ScheduledFuture<V> schedule(@Nonnull Callable<V> callable, long delay, @Nonnull TimeUnit unit) {
        return executor.schedule(callable, delay, unit);
    }

    @Nonnull
    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(@Nonnull Runnable command, long initialDelay, long period, @Nonnull TimeUnit unit) {
        return executor.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    @Nonnull
    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(@Nonnull Runnable command, long initialDelay, long delay, @Nonnull TimeUnit unit) {
        return executor.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    @Nonnull
    @Override
    public <T> List<Future<T>> invokeAll(@Nonnull Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return executor.invokeAll(tasks);
    }

    @Nonnull
    @Override
    public <T> List<Future<T>> invokeAll(@Nonnull Collection<? extends Callable<T>> tasks, long timeout, @Nonnull TimeUnit unit) throws InterruptedException {
        return executor.invokeAll(tasks, timeout, unit);
    }

    @Nonnull
    @Override
    public <T> T invokeAny(@Nonnull Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return executor.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(@Nonnull Collection<? extends Callable<T>> tasks, long timeout, @Nonnull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return executor.invokeAny(tasks, timeout, unit);
    }

    @Override
    public void execute(@Nonnull Runnable command) {
        executor.execute(command);
    }

    @Nonnull
    @Override
    public Future<?> submitSync(@Nonnull Runnable task) {
        return this.submitSync(task, null);
    }

    @Nonnull
    @Override
    public <T> Future<T> submitSync(@Nonnull final Runnable task, final T result) {
        final DefaultPromise<T> future = new DefaultPromise<T>(this.executor.next());
        this.executionQueue.add(new Runnable() {
            @Override
            public void run() {
                try{
                    task.run();
                    future.setSuccess(result);
                }catch(Exception e){
                    future.setFailure(e);
                }
            }
        });
        return future;
    }

    @Nonnull
    @Override
    public <T> Future<T> submitSync(@Nonnull final Callable<T> task) {
        final DefaultPromise<T> future = new DefaultPromise<T>(this.executor.next());
        this.executionQueue.add(new Runnable() {
            @Override
            public void run() {
                try{
                    future.setSuccess(task.call());
                }catch(Exception e){
                    future.setFailure(e);
                }
            }
        });
        return future;
    }

    @Override
    public <T> T invokeAnySync(@Nonnull Collection<? extends Callable<T>> tasks, long timeout, @Nonnull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public ScheduledFuture<?> scheduleSync(@Nonnull Runnable command, long delay, @Nonnull TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public <V> ScheduledFuture<V> scheduleSync(@Nonnull Callable<V> callable, long delay, @Nonnull TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public ScheduledFuture<?> scheduleAtFixedRateSync(@Nonnull Runnable command, long initialDelay, long period, @Nonnull TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public ScheduledFuture<?> scheduleWithFixedDelaySync(@Nonnull Runnable command, long initialDelay, long delay, @Nonnull TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public <T> List<Future<T>> invokeAllSync(@Nonnull Collection<? extends Callable<T>> tasks) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public <T> List<Future<T>> invokeAllSync(@Nonnull Collection<? extends Callable<T>> tasks, long timeout, @Nonnull TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public <T> T invokeAnySync(@Nonnull Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void executeSync(@Nonnull Runnable command) {
        this.executionQueue.add(command);
    }

    @Override
    public void shutdown() {

    }

    @Override
    public List<Runnable> shutdownNow() {
        return Collections.emptyList();
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @EventHandler
    public void onTick(ServerPostTickEvent event){
        if(executionQueue.size() > 0){
            for (Runnable e : executionQueue) {
                e.run();
            }
            executionQueue.clear();
        }
    }

    public DefaultEventExecutorGroup getExecutor() {
        return executor;
    }

    public static NailedScheduler instance(){
        return INSTANCE;
    }
}
