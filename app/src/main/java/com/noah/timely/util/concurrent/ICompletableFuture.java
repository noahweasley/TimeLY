package com.noah.timely.util.concurrent;

import com.noah.timely.util.collections.IFunction;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ICompletableFuture <V> implements Future<V> {
   private final ExecutorService executorService = Executors.newSingleThreadExecutor();
   private volatile boolean isCancelled;
   private volatile boolean isDone;
   private V value;
   private IFunction<V, Object> function;

   public ICompletableFuture<V> supplyAsync(Callable<V> callable) {
      executorService.execute(() -> {
         try {
            if (function != null) {
               function.apply(value = callable.call());
            }
            isDone = true;
         } catch (Exception exception) {
            isCancelled = true;
            if (!executorService.isShutdown())
               executorService.shutdown();
         }
      });
      return this;
   }

   public void thenApply(IFunction<V, Object> function) {
      this.function = function;
   }

   @Override
   public boolean cancel(boolean b) {
      executorService.shutdown();
      return isCancelled = executorService.isShutdown();
   }

   @Override
   public boolean isCancelled() {
      return isCancelled;
   }

   @Override
   public boolean isDone() {
      return isDone;
   }

   @Override
   public V get() throws ExecutionException, InterruptedException {
      //noinspection StatementWithEmptyBody
      while (!isDone) {
         if (isCancelled) throw new InterruptedException();
      }
      return value;
   }

   @Override
   public V get(long l, TimeUnit timeUnit) throws ExecutionException, InterruptedException, TimeoutException {
      //noinspection StatementWithEmptyBody
      while (!isDone) {
         if (isCancelled) throw new InterruptedException();
      }
      return value;
   }

}
