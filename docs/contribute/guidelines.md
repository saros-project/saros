---
title: Coding and Commit Guidelines
---

## Git

### Configuration
1.  Make sure that both "Author" and "Committer" conform to the format:

        John Doe <john.doe@anywhere.com>

    The following formats are **not** allowed: "**johnd**
    &lt;john.doe@...&gt;" or "**johnny** &lt;john.doe@...&gt;" or
    "**John** &lt;john.doe@...&gt;"


2.  Make sure you commit files with Unix line endings. Refer to the [Git
    manual](https://git-scm.com/book/tr/v2/Customizing-Git-Git-Configuration#Formatting-and-Whitespace)
    for more information.

### Commit Message

1.  Follow the general rule of how Git commit messages are formatted.
    Refer to the [Git manual](https://git-scm.com/book/en/v2/Distributed-Git-Contributing-to-a-Project#_commit_guidelines) for
    more information. In particular, every patch must have an
    informative short summary or "subject line" (also see next point in
    the list) and it should have a more detailed explanation below.
2.  In addition, use one of the following **type tags** in the subject
    line to make it easier for people to understand what your commit was
    about:

|Tag|Includes|Description
|----------|----------|-----------------------------------
|`[NOP]`|          | This commit did not have any effect and only concerns whitespace, removing unused methods, fixing documentation typos, etc.
|`[TASK]`  | `[NOP]` | Adds things that need to be done.
|`[DOC]`   | `[TASK]` | Improves JavaDocs or comments.
|`[INTERNAL]` | `[DOC]` | Only affects the details of the implementation without any effects to users of the component.
|`[API]` | `[INTERNAL]` | Affects the interface or dependencies of a component without creating any new functionality or fixing an existing bug.
|`[REFACTOR]` | |`API` or `INTERNAL` changes, which are done using automated tool support. So while `REFACTOR` changes usually result in large diffs, they are not very error prone. Caution: A refactoring should always be a separate patch, because refactorings are very hard to read.
|(`[FIX]` \| `[FIX] #bug id`) | `[INTERNAL]` | Fixes a bug. If existing, please attach the SourceForge bug tracker ID.
|(`[FEATURE]`\|`[FEATURE] #feature id`) | `[INTERNAL]` | Improves the functionality of the software. If existing, please attach the SourceForge feature tracker ID.
|`[LOG]` | `[DOC]` | Improves Logging with Log4j.
|`[UI]` | `[INTERNAL]` | Improvements to the user interface.
|(`[JUNIT]`\|`[STF]`) | `[INTERNAL]` | Improves testing -- either JUnit tests or STF tests.
|`[BUILD]` | `[TASK]` | Changes the way how the sources are compiled or distributed, e.g. changes to build scripts, MANIFEST files, or update sites.

 The following **scope tags** can (and should) be used in addition to
 make it easier to track what part of Saros your commit is changing.

|Tag|Description
|---|----------------------------
|`[E]`|    This commit ONLY affects the Eclipse version of Saros
|`[I]`|    IntelliJ version of Saros
|`[S]`|    Saros Server
|`[HTML]`| Saros HTML UI
|`[CORE]`| Saros core

Example usage: `[INTERNAL][I]` = Only affects the details of the
implementation in IntelliJ.
If you can't decide on a single type tag, you probably mixed up
different concerns and should consider splitting your patch.

## Coding Rules

### Formatting

We expect source code to be formatted and cleaned up using an automated
tool prior to submission. The formatting is also checked by the CI server
and will fail if a commit does not comply with the formatting rules.
See the [development environment documentation](development-environment.md) for more information.

### Naming Conventions
We use the conventions defined in [Java TM Programming Language, Chapter 9](http://www.oracle.com/technetwork/java/codeconventions-135099.html#367).

The only differences and additional conventions are:
* Non-Listener interfaces should be preceded by an I.<br>
  e.g. `IProject`, `IPreferenceStore`
* Listener interfaces should use the name of their corresponding class and add Listener to it.<br>
  e.g. `MouseListener`
* All test case classes must end with `Test`.<br>
  e.g `HelloWorldTest`
* A test suite classes must contain  `TestSuite`.<br>
* Every test class that is used for White-Box testing must be declared in the same package.<br>
  e.g. `foo.bar.HelloWorld` -> `foo.bar.HelloWorldTest`
* STF test cases must be put in any subpackage of `saros.stf.test`.<br>
  e.g `saros.stf.test.account.AccountPreferencePageTest`

### Structure

#### Getters and Setters

*   Use getters and setters by default except for very good reason
*   Internal Local Entity Classes do not need (but may) use
    getters/setters
*   Internal collections may be returned from classes, but it MUST be
    clearly indicated whether the returned value MUST not be changed
    (then an immutable view should be returned) or whether a user of a
    class MAY change the collection (in which case the class must ensure
    that the behavior is well defined, for instance using an
    observable collection)

#### Class Member Visibility

By default, all fields and methods should be **`private`**. For any field
or method with a visibility higher than **`private `**(visible from the
outside) there MUST be a detailed JavaDoc explanation. Thus, especially
making something `public `should be a deliberate and conscious act.

To facilitate testing, you may be tempted to make members more
accessible. This is fine up to **package-private** (no modifier). But it
is not acceptable to make a member part of a package's API
(`protected `or `public`) solely for testing purposes.

#### Final Members and Variables

*   For class variables: By default, make them final, unless you find a
    good reason not to. It makes the code easier to understand when you
    know where changes are expected to happen.
*   For local variables and parameters: In principle, the same rule
    applies as for class variables. But since local variables and
    parameters have a limited scope, the additional information gained
    through the presence of a final modifier is not tremendous.
    Therefore, we tend to not use the final keyword here.
*   For methods: By default, don't make them final, unless you have good
    reason not to. After all, we want to use
    object-oriented programming.


#### Classes and Interfaces

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
    *   see [Naming Conventions](#naming-conventions) for
        more information
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

#### Control Flow

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

#### Checking Parameters

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
    `IllegalArgumentException `in case of error.
    *   It is recommended to perform checking in particular at important
        component boundaries. For instance, we had a central entry point
        were events were buffered for sending over the network. Somehow
        a `null `event was inserted into the buffer queue and caused a
        `NullPointerException `later on. The programmer of the method
        which inserted the event into the buffer should have checked at
        this important boundary with many callers.
*   Use assert to check for complex preconditions, that cost a lot
    during runtime.
