## About guava-compatibility-agent

Guava compatibility agent which allows a subset of common APIs removed
in older versions of guava to continue working at runtime, when a
newer version of guava is available on the classpath.

### Reconstituted APIs:

* `MoreExecutors.sameThreadExecutor()`: Delegates to the modern `MoreExecutors.newDirectExecutorService()`
* `Objects.firstNonNull(first, second)`: Delegates to the modern `MoreObjects.firstNonNull(first, second)`
* `Futures.transform(future, function)` -> Delegates to the modern `Futures.transform(future, function, MoreExecutors.directExecutor())`
* `Futures.transform(future, asyncFunction, exec)` -> Delegates to the modern `Futures.transformAsync(future, asyncFunction, exec)`
* `Futures.transform(future, asyncFunction)` -> Delegates to the modern `Futures.transformAsync(future, asyncFunction, MoreExecutors.directExecutor())`
* `Futures.addCallback(future, callback)` -> `Futures.addCallback(future, callback, MoreExecutors.directExecutor())`
* `Futures.withFallback(future, fallback, executor)` -> `Futures.catchingAsync(future, Throwable.class, fallback, executor)`
* `Futures.withFallback(future, fallback)` -> `Futures.catchingAsync(future, Throwable.class, fallback, MoreExecutors.directExecutor())`

## Not handled yet

* `Objects.toStringHelper` and overloads: This requires us to define a duplicate of `MoreObjects$ToStringHelper` which is a bit more involved. Unclear precisely how this would work.
* `Iterators.emptyIterator()` signature returns the `UnmodifiableIterator` type, should be trivial to handle.
* Many, many more.
