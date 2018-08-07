---
title: Commit and Coding Guidelines
---

# {{ page.title }}
{:.no_toc}

{% include toc.html %}

## Review rules at a glance
All pull requests in our project have to be reviewed. See the [review process documentation](processes/review.md) for more information.

Make sure that your pull request:

* has a commit message as defined below
* is small
* is formatted as defined below

In order to merge a pull request you need:

* all checks (Travis CI, Sonar) passed
* two reviewers approved **OR**
* one reviewer approved two days ago and no further modification requests are posted

**For each review you received you have to perform two reviews.**

## Commit message

1.  Follow the rule of [how Git commit messages are formatted](https://chris.beams.io/posts/git-commit/).
    In particular, every patch must have an
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


## Naming

This section is mainly based on the [Code Conventions for the Java TM Programming Language, Chapter 9](http://www.oracle.com/technetwork/java/codeconventions-135099.md#367)
* Differences and additional conventions
  * Non-Listener interfaces should be preceded by an `I`
    e.g: `IProject`, `IPreferenceStore`
  * Listener interfaces should use the name of their corresponding class and add `Listener` to it
    e.g. `MouseListener`
* All test case classes **must** end with **Test**.
  e.g  `HelloWorldTest`
*   A test suite classes must contain the character sequence
    **TestSuite**
* Every test class that is used for White-Box testing must be declared
  in the same package.
  e.g `foo.bar.HelloWorld` -> `foo.bar.HelloWorldTest`
* STF test cases **must** be put in any subpackage of
  `de.fu_berlin.inf.dpp.stf.test`, e.g
  `de.fu_berlin.inf.dpp.stf.test.account.AccountPreferencePageTest`

## Documentation

### Keep this size up-to-date
Update this website (located in the directory `docs`) if a pull request:

* changes something that is documented on this website
* introduces a new feature 

If a pull request changes a GUI, a logic or something else which is documented
in this sites then the pull request also has to contain corresponding changes of this site.

#### Rules

* Create new pages
  * Check whether your content fits into another page before you create a new page.
  * Add the page into the corresponding sidebar (change file `../_data/*/sidebar.yml`).
* Avoid to link sections. Otherwise the link will be broken if the section name is changed.
* **Never** copy content of another page (e.g. a tutorial), rather write a short sentence which links to the corresponding page
* **Always** separate user documentation (located in the directory `docs/documentation`) and developer documentation (located in the directory `docs/contribute`)
* Dont be afraid to remove superfluous documentation

### Commenting in Code

#### No @author tags

We don't use @author tags in new code because of the following two
reasons:

* Such tags do not reliably point to a person that can be asked
  questions concerning the tagged file.
* Such tags do not accurately represent the actual involvement of/work
  done by a certain developer. More often than not, developers in our
  project work in files they did not create. If anyone really wants to
  determine the authorship of certain files, our version control does
  a much better job by accurately crediting both the developers and
  reviewers of each commit.

#### JavaDoc

* JavaDoc documentation and names should be meaningful and make the
  programming element understandable with minimum insight into
  the code. If your comments make the code more difficult to
  understand, remove them.
* Don't use single line comments (starting with //) for multi line
  text.
* Comments should describe complex code in **shorter** text. Comments
  like "Create HashMap", "Set Value", "Loop here", or "else" should
  be removed.

* The following JavaDoc tags should be used for highlighting important
  aspects of a method or a class:
  * `@ui` or `@swing` or `@swt` - This method needs to be called
    from the UI thread, otherwise an exception might occur.
  * `@nonBlocking` - This method does return before finishing
    its activities. If there is at least one method is a class which
    is non-blocking it is highly recommended to put `@blocking` to
    all other methods to help users recognize that blocking behavior
    is important for this class
  * `@blocking` - This method is potentially long-running and blocks
    until it is done. This is the default for all method, which
    are unmarked.
  * `@valueObject` - The objects of this class are immutable. All
    methods are side effect free.
  * `@nonReentrant` - This method cannot be called twice at the
    same time.
  * `@threadsafe` - This method can safely be called twice at the
    same time.
  * `@caching` - If possible, this method uses a cache to calculate
    the return value.

#### What to comment

Generally you should document the code starting from the highest level
of code hierarchy. This means that all packages need a documentation
followed by interfaces and classes. All documentation should be in
JavaDoc comments in order to automatically generate HTML source code
documentation.

-   Each interface should be documented.
    -   The comments in the interface should provide a short description
        of what you can use it for. For all exposed routines you should
        at a minimum document each parameter and each return value but
        hide implementation details.

-   Each class should be documented.
    -   The description of the class should provide a short overview of
        what this class is about. Design decisions and limitations
        should be mentioned as well.

-   Methods should be documented, if they are complex enough and it will
    be helpful for other readers to summarize or explain the purpose of
    these methods.
-   Important objects and variables should also be briefly documented.

## Logging with Log4J [NO]

-   We use one private static final logger per class. An editor template
    for Eclipse:\
    `private static final Logger LOG = LOgger.getLogger(${enclosing_type}.class);`
-   We use the following Log-Levels:
    -   `ERROR` An error should be printed if something occurred which
        is the fault of the developer and which will cause unexpected
        behavior for the user.
    -   `WARN` A warning should be printed if something occurred which
        is the fault of the developer but which while not expected to
        occur should be handled gracefully by the application.
    -   `INFO` Any information that is of interest to the user may be
        printed using INFO.
    -   `DEBUG` Any information which might be useful for the developer
        when figuring out what the application did may be printed
        as DEBUG. The amount of information printed should not cause the
        performance of the application to suffer (use `TRACE` for this).
    -   `TRACE` Detailed information about the program execution should
        use the TRACE level. This information usually is so detailed
        that the application runs slower and thus should never be
        enabled in a production version.

## Error handling
We expect that all source code used in thesis to deal gracefully with
errors. The goals to aim for should be:

-   Provide information to the user that something went wrong and how
    s/he can fix the problem (if). A typical example for this is a
    failed to log-in to a server, because the password is wrong which
    should lead to a error-dialog box. In the best of all cases this
    error message will offer the user to correct the problem or lead him
    to the place to correct it (for instance a preference dialog).
-   Provide information to the developer when some operation failed that
    should not have (unexpected) and where the problem
    occurred (stack-trace).
-   Provide information about the kind of events that happened
    internally (tracing/logging). It should be possible to
    disable these.

## Refactor --> Rename

This section contains a couple of guidelines for renaming classes or
methods. They are worth sticking to because of our review process,
which requires you to create follow-up commits, which can be problematic
in combination with renaming.

**Beware of renaming classes.** If you want to rename a
class, put the Refactor --> Rename in a separate patch, and post/commit
it as soon as possible.

If you rename a class and make additional changes to that class, your
pull request becomes hard to review, because the additional changes are now in
a different file. The Compare editor in Eclipse and the Diff view in the
Reviewboard won't be able to display what the additional change was.

Even worse, what if someone else makes changes to that class? You
renamed it locally, and someone else commited a change to the "old"
class, how are you going to merge that? You'll be in integration hell
faster than you can say *"Refactoring is easy in Java"*.

## Formatting

We expect source code to be formatted and cleaned up using an automated
tool prior to submission.

*   For projects that are managed by Eclipse (dpp, dpp.core, dpp.ui,
    dpp.server, dpp.whiteboard), the code formatter should be called
    automatically on every save. So there is no need to take
    any actions.
*   For the projects that are managed by Intellij (currently only
    dpp.intellij), code formatting styles are configured, but need to be
    invoked manually through CTRL+ALT+L while the src (or test) folder
    is selected. Make sure to check "Organize Imports" in the
    configuration dialog.

The following steps are necessary to ensure that your Eclipse recognizes
our code style presets. You only need to do this once for any given
Eclipse installation:

1. Install the **clean up profile**
   1. Navigate to *Window > Preferences > Java > Code Style > Clean Up*
   2. Click on the *Import...* Button and select [`de.fu_berlin.inf.dpp/clean-up-profile.xml`](https://github.com/saros-project/saros/blob/master/de.fu_berlin.inf.dpp/clean-up-profile.xml)
   3. *Apply* these changes
2. Install the **formatter profile**
   1. Navigate to *Window > Preferences > Java > Code Style > Formatter*
   2. Click on the *Import...* Button and select [`de.fu_berlin.inf.dpp/clean-up-profile.xml`](https://github.com/saros-project/saros/blob/master/de.fu_berlin.inf.dpp/formatter-profile.xml)
   3. *Apply* these changes


## License

Saros is licensed under GPLv2.
All 3rd party code that has not been written by a Saros team member
is kept in a separate source folder named `ext-src`.

## Language

All code (i.e. identifiers) and comments are written in American
English.
