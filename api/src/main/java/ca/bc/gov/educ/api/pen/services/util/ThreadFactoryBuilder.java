package ca.bc.gov.educ.api.pen.services.util;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The type Thread factory builder.
 */
public class ThreadFactoryBuilder {

  /**
   * The Name format.
   */
  private String nameFormat;
  /**
   * The Daemon thread.
   */
  private Boolean daemonThread;
  /**
   * The Priority.
   */
  private Integer priority;
  /**
   * The Uncaught exception handler.
   */
  private Thread.UncaughtExceptionHandler uncaughtExceptionHandler = null;
  /**
   * The Backing thread factory.
   */
  private ThreadFactory backingThreadFactory = null;

  /**
   * Returns new {@code ThreadFactory} builder.
   *
   * @return the thread factory builder
   */
  public static ThreadFactoryBuilder create() {
    return new ThreadFactoryBuilder();
  }

  /**
   * Get thread factory.
   *
   * @param builder the builder
   * @return the thread factory
   */
  private static ThreadFactory get(final ThreadFactoryBuilder builder) {
    final String nameFormat = builder.nameFormat;
    final Boolean daemon = builder.daemonThread;
    final Integer priority = builder.priority;
    final Thread.UncaughtExceptionHandler uncaughtExceptionHandler = builder.uncaughtExceptionHandler;

    final ThreadFactory backingThreadFactory =
        (builder.backingThreadFactory != null)
            ? builder.backingThreadFactory
            : Executors.defaultThreadFactory();

    final AtomicLong count = (nameFormat != null) ? new AtomicLong(0) : null;

    return runnable -> {
      final Thread thread = backingThreadFactory.newThread(runnable);
      if (nameFormat != null) {
        final String name = String.format(nameFormat, count.getAndIncrement());

        thread.setName(name);
      }
      if (daemon != null) {
        thread.setDaemon(daemon);
      }
      if (priority != null) {
        thread.setPriority(priority);
      }
      if (uncaughtExceptionHandler != null) {
        thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
      }
      return thread;
    };
  }

  /**
   * Sets the printf-compatible naming format for threads.
   * Use {@code %d} to replace it with the thread number.
   *
   * @param nameFormat the name format
   * @return the thread factory builder
   */
  public ThreadFactoryBuilder withNameFormat(final String nameFormat) {
    this.nameFormat = nameFormat;
    return this;
  }

  /**
   * Sets if new threads will be daemon.
   *
   * @param daemon the daemon
   * @return the thread factory builder
   */
  public ThreadFactoryBuilder withDaemon(final boolean daemon) {
    this.daemonThread = daemon;
    return this;
  }

  /**
   * Sets the threads priority.
   *
   * @param priority the priority
   * @return the thread factory builder
   */
  public ThreadFactoryBuilder withPriority(final int priority) {
    this.priority = priority;
    return this;
  }

  /**
   * Sets the {@code UncaughtExceptionHandler} for new threads created.
   *
   * @param uncaughtExceptionHandler the uncaught exception handler
   * @return the thread factory builder
   */
  public ThreadFactoryBuilder withUncaughtExceptionHandler(
      final Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {

    this.uncaughtExceptionHandler = Objects.requireNonNull(uncaughtExceptionHandler);
    return this;
  }

  /**
   * Sets the backing {@code ThreadFactory} for new threads. Threads
   * will be created by invoking {@code newThread(Runnable} on this backing factory.
   *
   * @param backingThreadFactory the backing thread factory
   * @return the thread factory builder
   */
  public ThreadFactoryBuilder withBackingThreadFactory(final ThreadFactory backingThreadFactory) {
    this.backingThreadFactory = Objects.requireNonNull(backingThreadFactory);
    return this;
  }

  /**
   * Returns a new thread factory using the options supplied during the building process. After
   * building, it is still possible to change the options used to build the ThreadFactory and/or
   * build again.
   *
   * @return the thread factory
   */
  public ThreadFactory get() {
    return get(this);
  }

}
