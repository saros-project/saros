---
title: Best Practices
---

## Code Structure

### Getters and Setters

*   Use getters and setters by default except for very good reason
*   Internal Local Entity Classes do not need (but may) use
    getters/setters
*   Internal collections may be returned from classes, but it MUST be
    clearly indicated whether the returned value MUST not be changed
    (then an immutable view should be returned) or whether a user of a
    class MAY change the collection (in which case the class must ensure
    that the behavior is well defined, for instance using an
    observable collection)

### Class member visibility

By default all fields and methods should be **`private`**. For any field
or method with a visibility higher than **`private `**(visible from the
outside) there MUST be a detailed JavaDoc explanation. Thus, especially
making something `public `should be a deliberate and conscious act.

To facilitate testing, you may be tempted to make members more
accessible. This is fine up to **package-private** (no modifier). But it
is not acceptable to make a member part of a package's API
(`protected `or `public`) solely for testing purposes.

### Final members and variables

*   For class variables: By default, make them final, unless you find a
    good reason not to. It makes the code easier to understand when you
    know where changes are expected to happen.
*   For local variables and parameters: In principle, the same rule
    applies as for class variables. But since local variables and
    parameters have a limited scope, the additional information gained
    through the presence of a final modifer is not tremendous.
    Therefore, we tend to not use the final keyword here.
*   For methods: By default, don't make them final, unless you have good
    reason not to. After all, we want to use
    object-oriented programming.

### Classes and Interfaces

*   Take your time, look at the environment of your code and think. When
    it comes to the establishment of classes and interfaces, there is a
    lot of mistakes to make that work against the designed architecture
    and make solving problems afterwards very expensive.
*   Components must implement an interface if they access
    external resources. Implementing an interface to combine things to
    reusable units is always a good idea, but before doing so make sure
    that there is no such similar implementation in place already and
    how the newly created one would fit into the architecture.
*   If you develop a listener interface with more than one method, you
    should in most cases provide an abstract base implementation which
    provides empty implementations, because in many cases implementors
    of your interface will not want to implement all methods. Also it
    helps to improve ability to change the program, as methods can be
    added to the interface more easily.
*   Do not implement more than one listener interface per class,
    especially if using a top level class, because it makes the code
    much less readable and makes you more likely to forget unregistering
    your listener.
    *   Anonymous inner-classes assigned to fields are much better:

Instead of:

```java
public class A implements B, C, D {
  ...
}
```
you should write:
```java
public class A implements D {
  ...

  B b = new B(){
    ...
  };

  C c = new C(){
    ...
  };

  ...
```

### Control flow

Test whether you can return from a method instead of testing whether
you should execute a block of code.

Instead of:

```java
    public void foo(){
      //some code

      if (condition){
        // long block of code
      }
    }
```

you should write:

```java
    public void foo(){
      //some code

      if (!condition)
        return;

      // long block of code
    }
```

Furthermore, there is no need to put the code after the return
statement into an explicit "else"-branch. You can easily save one
level of block-nesting.

### Checking parameters

*   Methods may assume that they are called with correct non-null input
    unless the method specifies that it allows incorrect or null input.
*   If a parameter may be `null `or is checked add a `@param`-JavaDoc
    that indicates this.


```java
        /**
         * Get positions of slashes in the filename.
         * @param filename may be null
         * @return Null-based indices into the string
                   pointing to the slash positions.
         */

        public int[] findAllSlashes(String filename) {
          if (filename == null)
            return new int[];
          ...
        }
```

*   If a method checks for correct parameters it should throw an
    `IllegalArgumentException` in case of error.
    *   It is recommended to perform checking in particular at important
        component boundaries. For instance, we had a central entry point
        were events were buffered for sending over the network. Somehow
        a `null `event was inserted into the buffer queue and caused a
        `NullPointerException` later on. The programmer of the method
        which inserted the event into the buffer should have checked at
        this important boundary with many callers.
*   Use assert to check for complex preconditions, that cost a lot
    during runtime.

## Usability

Saros/E is an Eclipse IDE Plugin. Therefore it needs to meet
the [Eclipse User Interface Guidelines](http://wiki.eclipse.org/User_Interface_Guidelines#Checklist_For_Developers).

### Standard heuristics for system design

When improving Saros either technically or visually you should check
if you followed the [10 basic heuristics for good user interfaces](https://www.nngroup.com/articles/ten-usability-heuristics).

You do not need many users for this, usually 3 users are enough to quickly find out the most
important. You can kindly ask them record their test with a tool like [Screencast-O-Matic](http://www.screencast-o-matic.com).

### Progress and Cancelation 101

Whenever a method is long-running, i.e. there is a chance that it will
take longer than 100ms or involves any external factors such as the user
or input/output, the software engineer is responsible to provide a way
to track progress of the operation and provide to the caller the
possibility to cancel the operation.

If the software engineer does not provide such opportunity the user
experience might be reduced. For instance in Saros, there used to be no
possibility to cancel a file transfer from one user to the other but
just to cancel in between the files. This behavior seems okay, but once
we started to see archive files of 10 MB and more, which could only be
canceled by disconnecting from the Jabber-Server, the undesireability of
the behavior becomes pretty clear.

Fortunately enough there is a straight-forward and simple solution,
which also improves the general threading behavior of the application:
The class to use is called `SubMonitor` which implements the
`IProgressMonitor` interface.

Now all methods which are long running, need to be changed to take a
`SubMonitor` as a last parameter (this is our convention):

```java
public Result computeSomething(List input, ..., SubMonitor progress){
  //something
}
```

Inside those methods, first, we need to initialize the `ProgressMonitor`
with the name of the task and the number of steps this task will take
(if the number of steps is unknown we set it to a large integer, 10000
is our convention):

```java
progress.beginTask("Computing Something", input.size());
```

Now whenever we have made some progress towards this task, we can report
this to the monitor:

```java
for (Something some : input) {
  ... process input
  progress.worked(1);
}
```

At the end of the task, we should report, that we are done with the
task:
```java
progress.done()
```
This basic contract of `beginTask()`, `worked()`, and `done()` is
sufficient to achieve the basic uses of progress monitoring.

#### Nesting Progress

In many cases the situtation is a little bit more complicated, as the
operation that is long-running is making calls to other long-running
operations as well. To solve this problem, we need to create **child
progress monitors**, which consume a given number of work steps from
their parent:

```java
public void computeInTwoSteps(IProgressMonitor monitor){
    SubMonitor subMonitor = SubMonitor.convert(
                                     monitor,
                                     "Compute in two steps",
                                     2
                                   );
    progress.beginTask("Compute in two steps", 2);
    computeFirstStep(subMonitor.newChild(1));
    computeSecondStep(subMonitor.newChild(1));
    progress.done();
}
```

This code will pass two SubMonitors to the two methods, which then are
free to use them just as the parent method did:

```java
public void computeFirstStep(SubMonitor progress){

  progress.beginTask("Compute the first step", 140);
  ...
  progress.worked(5); // etc.
  ...
  progress.done();

}
```

#### Reporting information to the user

A progress monitor provides 3 ways to report information from a long
running operation to the user

* The amount of steps already worked as given by `worked()`
* The name of the task, as set using `beginTask(String)` and
  `setTaskName(String)`
* The name of the sub-task, as set using `subTask(String)`

This information is typically presented to the user as a Dialog with a
message being equal to the taskname of the top level progress monitor, a
progress bar showing the growing amount of work already done and a label
for the current sub-task which switches everytime the sub-task is being
set.

Since the taskName is fixed (by default), only the top level task name
is shown to the user. The task name of the nested operations are never
shown to the user. To report status messages from nested operations, the
sub-task needs to be used:

```java
public void computeInTwoSteps(SubMonitor progress){

  progress.beginTask("Compute in two steps", 2);

  progress.subTask("Two Step Computation: Step 1");
  computeFirstStep(progress.newChild(1));

  progress.subTask("Two Step Computation: Step 2");
  computeSecondStep(progress.newChild(1));

  progress.done();

}
```

#### Dealing with operation of unspecified length

To have a progress dialog on operations for which the amount of steps
are unknown, the following solution is recommended:

```java
while (!done()){
  ... do work

  progress.setWorkRemaining(1000);
  progress.worked(1);
}
```

This code will advance the progress bar 0,1% of the remaining total of
the progress monitor and thus logarithmically approach 100% worked. The
percentage 0,1% should be adjusted to achieve 50% progress on the
expected number of work steps.

#### Cancellation

To achieve **cancellation** support in an operation, we should check
frequently whether the user has requested that we stop our tasks:

```java
for (Something some : input){
  if (progress.isCanceled())
    return;
  ... process input
  progress.worked(1)
}
```

The easiest way to response to a request for cancellation is to just
return as shown above, but in most cases this is undesirable, because
the caller will not know whether the operation finished or not. Instead,
methods should rather throw a `CancellationException` so that a caller
can recognize that the operation was canceled:

```java
public BigInteger factorial(int n, SubMonitor progress){

  progress.beginTask("Computing Factorial of " + n, n);

  BigInteger result = BigInteger.ONE;

  for (int i = 1; i < n; i++) {
    if (progress.isCanceled())
      throw new CancellationException();

    result = result.multiply(BigInteger.valueOf(i));
    progress.worked(1);
  }

  progress.done();
  return result;
}
```

Btw: It is an convention that we try to avoid `InterruptedException` for
this, because it is a checked exception and thus cumbersome for the
caller. To maintain this convention, a method MUST specify whether it is
cancelable, by providing the demonstrated JavaDoc tag.

### Error Handling

The first step to achieve this, is to make sure that you notice when
things go wrong. Thus, all Runnables passed to Threads or Executors and
all methods called from 3rd party software, such as Actions called from
Eclipse, or Listeners from the network API need to be made secure as to
catch all `RuntimeException` that might have slipped up.

Use the following method for this (you might want to pass up
`RuntimeException`s up the stack as well):

```java
    /**
     * Return a new Runnable which runs the given runnable but catches all
     * RuntimeExceptions and logs them to the given logger.
     *
     * Errors are logged and rethrown.
     *
     * This method does NOT actually run the given runnable, but only wraps it.
     */
    public static Runnable wrapSafe(final Logger log, final Runnable runnable) {
      return new Runnable() {
        public void run() {
          try {
            runnable.run();
          } catch (RuntimeException e) {
            log.error("Internal Error:", e);
          } catch (Error e) {
            log.error("Internal Fatal Error:", e);

            // Rethrow errors (such as an OutOfMemoryError)
            throw e;
          }
        }
      };
    }
```

When developing in Eclipse the following code-snippets might help:

-   Error reporting to the user can be done using an ErrorDialog:
```java
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                MessageDialog.openError(
                    Display.getDefault().getActiveShell(),
                    "Dialog Title", "Error message"
                );
            }
        });
```
-   Error reporting for the developer can be done using the ErrorLog:
```java
        YourPlugin.getDefault().getLog().log(
          new Status(IStatus.ERROR, "Plug-In ID goes here", IStatus.ERROR, message, e));
```
-   Input from the user needs always to be checked and untainted
    on input.
    -   Error messages need to be human readable.
    -   Exceptions need to be handled correctly, i.e.
        -   If the program could do something about the exception it
            should try (for instance repeat an failed operation).
        -   ~~Otherwise an unchecked exception should be thrown to
            prevent =throws=-clauses littering the code.~~
        -   If you can't handle the exception then throw it back to the
            caller

Anti-example:
```java
    public Javadoc updateJavadoc(String filename, String name,
      String newJavadocText, int isType) {
      Javadoc jd = null;
      try {
        ... Try to update Javadoc ...
      } catch (Exception e) { // No, no, no!
        e.printStackTrace();
      }

      System.out.println("The new javadoc-------\n" + jd);
      return jd;
    }
```
How to do it right:
```java
    public Javadoc updateJavadoc(String filename, String name, String newJavadocText, int isType)
      throws IOException {

      Javadoc jd = null;
      try {
        ... Try to update Javadoc ...
      } catch (IOException e){ // Catch the checked exceptions that occur
        // bring the internal logic back to a stable state (if you can)
        throw e; // let the caller handle this exception
      }

      System.out.println("The new javadoc-------\n" + jd);
      return jd;
    }
```

#### Dealing with InterruptedException

-   When calling a blocking method, Java uses InterruptedException to
    signal that the waiting thread was told to stop waiting.
-   As a a caller to a blocking method it is your responsibility to deal
    with the possibility of being interrupted. This is why exception is
    checked in Java.
-   The contract of InterruptedException is the following:
    -   If interrupted a method honoring the contract MUST either throw
        the InterruptedException or set the interrupt-flag.
-   Since the InterruptException-contract is assumed to be honored by
    all methods in Java, there are three ways of dealing with the
    InterruptedException:
    1.  Rethrowing the InterruptedException to tell callers that this
        method might be interrupted: As we do not like checked exception
        this is an inferior solution to the problem.
    2.  Resetting the interrupt flag
        -   It is your responsibility to always reset the Interrupt flag
            in case you catch the Exception, because somebody who called
            you might depend on it. This will look like this and is the
            normal case for 90% of all cases:
```java
                try {
                  Thread.sleep(500);
                } catch(InterruptedException e){
                  // The line of code will continue after the catch
                  Thread.currentThread().interrupt();
                }
```
        -   This is recommended even if you are sure that you will never
            be interrupted (because the program might change in
            the future)
        -   Special case: Spinning / Busy Waiting
            -   If you use Thread.sleep() inside a while() loop, then
                you cannot use the above pattern without leaving the
                while loop, because Thread.sleep() (all methods that
                honor the contract of InterruptedException) will return
                immediately without sleeping. Thus your while loop
                becomes a busy waiting loop.
            -   If you really do not want to be interruptible, then you
                need to do the following:
```java
                    boolean interrupted = false;
                    try {
                      while (looping){
                        // do stuff
                        try {
                          Thread.sleep(500);
                        } catch(InterruptedException e){
                          interrupted = true;
                        }
                      }
                    } finally {
                      if (interrupted)
                        // The line of code will continue after the catch
                        Thread.currentThread().interrupt();
                    }
```
    3.  Tell others that you violate the contract: Add to the signature
        of your method, that you do not support the contract
        of Thread.interrupt(). Be aware that by violating the
        InterruptedException-contract you put all your callers into
        violation as well (since if handle an InterruptedException
        incorrectly they cannot honor the contract anymore). Use the
        following to tell callers that you do not honor the contract:\
```java
            /**
             * @nonInterruptable This method does not support being interrupted
             */
```
-   BTW: There is no obligation in the contract for you to exit as
    quickly as possible.
-   For more read:
    <http://www-128.ibm.com/developerworks/java/library/j-jtp05236.md>

## Broadcast Listener

To avoid repeated code blocks in an Observable like
```java
class Observable {

  List listeners = new ...
  public void addListener(Listener listener){listeners.add(listener)}
  public void removeListener(Listener listener){...}

  public void someMethod() {
    ...
    // notify all listeners
    for (Listener l : listeners) {
      l.notify();
    }
  }

  public void someMethod2() {
    ...
    // notify all listeners again
    for (Listener l : listeners) {
     l.notify();
    }
  }

}
```
It is recommended to use a helper class `BroadcastListener` that
provides a method to notify all its registered listeners. The
`BroadcastListener` should be a singleton managed by `PicoContainer`.
```java
public class BroadcastListener implements Listener {

  List listeners = new ...
  public void add(Listener listener){listeners.add(listener)}
  public void remove(Listener listener){...}

  public void notify() {
    for (Listener l : listeners) {
      l.notify();
    }
  }

}
```

The code for an Observable becomes therfore much simpler. It only needs
to know the `BroadcastListener` and can easily notify all registered
listeners at once.
```java
class Observable {

  BroadcastListener listener = new BroadcastListener();

  public void someMethod(){
    ...
    listener.notify();
  }

  public void someMethod2(){
    ...
    listener.notify();
   }
}
```


## Software Design Rules

-   Distinguish clearly between the common building blocks of
    applications, namely:
    -   Services - Providers of functionality that can be well
        encapsulated
    -   Entities - Mutable classes that represent the business world.
        Group these into aggregates.
    -   Values - Use the [Value Object
        Pattern](http://www.c2.com/cgi/wiki?ValueObject) to ensure that
        users of the class can rely on the objects to be immutable and
        all methods to be side effect free. This helps A LOT when using
        a class.
-   Avoid implementing many interfaces in the same class whereever
    possible (more than one is necessary only in rare cases). Use nested
    or anonymous classes instead. It makes it much easier to understand
    and modify your code.

-   The Singleton Pattern is a inferior solution to define the
    architecture of an application compared to using Dependency
    Injection. To achieve the lazy semantics often
    also associated with the Singleton Pattern you should inject a
    dependency to a lazy initializer of the component that you actually
    need instead.

## Threading and Concurrency

  * Try to avoid instantiating the class `Thread` directly but
    rather use a `ThreadFactory` (in particular the `NamedThreadFactory`
    so that your threads are named) or even better an `Executor`.
  * Spend some time learning about the [Java Concurrency library java.util.concurrent](http://java.sun.com/javase/6/docs/api/java/util/concurrent/package-summary.md).

## Kinds of comments

This section is based on Steve McConnell's Code Complete, Chapter 32.

There are six categories of comments:

1.  **Repeat of the Code**
    -   If something is not complex and thus documentation repeats code,
        you should not document at all. For instance, don't document
        getters and setters (an exception would be to explain what the
        variable actually contains that you are getting).
    -   A counter-example:
```java
            /**
             * return a stack of String,
             * @param text
             * @return Stack
             */

            public Stack getFormatJavadocLine(String text) {

              StringTokenizer st = new StringTokenizer(text, "\n");
              Stack stack = new Stack();

              while (st.hasMoreTokens()) {
                stack.add(st.nextToken());
              }

              return stack;
            }
```
    -   The documentation is absolutely useless as it just repeats the
        signature and even fails to explain the method name. Furthermore
        the method name is wrong for the task that is
        actually performed. The important question whether this method
        returns a stack of the individual lines in text from top to
        bottom or bottom to top remains unanswered.

2.  **Explanation of the Code**
    -   If the code needs to be explained, it is usually better to
        improve the code instead of adding comments.

3.  **Marker in the Code**
    -   Marker comments are notes for the developers that the work isn't
        done yet. They should not be left in the code. If you have to
        mark a section in the code, use „TODO“. This way all marker
        comments will be standardized and it is easier to locate them.
```java
            /*
             * TODO FIX: Our caller should be able to distinguish whether the
             * query failed or it is an IM client which sends back the message
             */
```
4.  **Summary of the Code**
    -   Comments that summarize the code in a few sentences can be
        valuable especially for readers who haven't written the code.
        They can scan these comments more quickly than the code.
```java
            /*
             * move all chess pieces to start position
             */
```
5.  **Description of the Code's Intent**
    -   Intent comments explain the purpose of a section of code. In
        contrast to the summary comments which operate at the level of
        the solution the intent comment operates at the level of
        the problem.
```java
            /*
             * initialize a new chess game
             */
```
6.  **Information that cannot possibly be expressed by the Code Itself**
    -   Some information needs to be written in comments since they
        cannot be expressed in code. This can be copyright notices,
        version numbers, notes about the code's design, optimization
        notes and so on.

Acceptable code comments are summary comments (4.), intent comments
(5.), and information that cannot be expressed in code (6.). Markers
(3.) are acceptable during development but should be removed before
release. Try to avoid comments that repeat (1.) or explain the code
(2.).
