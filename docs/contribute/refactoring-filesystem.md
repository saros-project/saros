
# Refactoring of Saros Filesystem

During the beginning of the development of Saros for IntelliJ (Saros/I), some parts of Saros/E were extracted to an independent component (Saros Core). One extracted part is the resource implementation that represents Saros' filesystem.
The design of the Saros filesystem is very similar to the filesystem of Eclipse IDE.
The Eclipse filesystem can be read [here](https://help.eclipse.org/2019-09/topic/org.eclipse.platform.doc.isv/guide/resInt.htm).

Because of the similarity, the implementation of the filesystem for Saros/E was easy.
With the implementation of Saros/I it turned out, that the design of the Saros filesystem is not suitable for Saros/I.
This has several reasons: e.g. An IntelliJ module can be encapsulated in other IntelliJ modules. Furthermore, a module can have several content roots.
The structure of the IntelliJ filesystem can be read [here](https://www.jetbrains.com/help/idea/creating-and-managing-modules.html) and the comparison to the Eclipse filesystem can be read [here](https://www.jetbrains.com/help/idea/migrating-from-eclipse-to-intellij-idea.html)


## The new approach

The new approach is to leave the project-based architecture of Saros:

A Saros user should be able to share an any subtree of his resources with Saros. The root of the subtree must have an absolute path on each Saros user's enviroment. It doesn't matter, where the the subtree is located (ex. On *C*: oder *D*: drive), but what the absolute paths unites is, that they mean the same reference (IReferencePoint). That means that every Saros user can choose for themselves where the resources are located. If a user uses Windows, the resources can be located in `C:` or `D:` drive. If a user uses Linux, the resources can be located in `/home/path/to/bar`. So the absolute paths of the resources are different, but if they start a session and want sharing their resources, the resources have the same reference point even though they have different absolute paths. Every Saros instance should know, where an IReferencePoint object is located in the local file system, so that the resources can be determined in combination of an IReferencePoint and a relative path to the resource.

The IReferencePoint can be look like this:

> public interface IReferencePoint {
>          /* Nothing here */
>          }

Every resource knows their reference point, so that a reference point is easy accessible, but not the other way around.

### Current Situation
The current situation is, that all resources belongs to their projects.
For example, all actions of a Saros user (like editing a file, creating a new folder, move files) are called activities.
The activities contain a global project id (each shared project gets a unique id for all Saros instances in a session) and a relative path to the affected resource outgoing from the project. Every Saros instance knows which id belongs to which project, so it can process the activities and determine the resources easily.

### Design of new filesystem
Because every Saros instance knows, where the reference point is located in the local filesystem, the Saros' filesystem can be simplified. Saros should not know anymore if a resource is `IWorkspace` or `IProject`. Saros must distinguish resources between files and folders so that the design of the filesystem can look like this:
A Saros filesystem consists of resources, called `IResource`. It contains its `IReferencePoint` and can either be an `IFolder` or an `IFile`.

## Concepts how to reach the final state

The refactoring of the project-based architecture to a reference point based one is very complicated. Especially the relationship between the components needs a long analysis to find an effective strategy to refactor Saros.
An important strategy for the refactoring is to build the reference point based architecture, but also keeping the
project-based architecture simultaneously until the refactoring is completed.
For providing the dual based architecture a special mapper is needed, which maps the reference point to the IDE specific file system resources. Because every component has different resources, a special mapper (ReferencePointManager) is required for each of them.

### Saros Core
Saros Core needs a mapper, which is called `CoreReferencePointManager` , with following features:

1. It maps a reference points object to a Saros project object internally
2. It provides the functionalities of the Saros project

Because a reference point itself has no methods, it is important that the CoreReferencePointManager provides functionalities of a Saros project, so that the co-existence of both architectures is possible.


### Saros/E

In situations, like processing activities, Saros/E converts Saros resources backs to Eclipse resource. So it is very useful, that the mapper in Saros/E gets the features to determine Eclipse resources given by reference points and relative paths to the resources. This mapper is called `EclipseReferencePointManager` and has the following features:

1.  It maps a reference point object to an Eclipse project internally
2.  It determines Eclipse resources based on a combination of reference points and relative paths to the resources

### Saros/I

The `IntelliJReferencePointManager` is needed for Saros/I and has two responsibilities:

1.  It maps a reference point object to an IntelliJ module internally
2.  It determines java resources based on a combination of reference points and relative paths to the resources

### Saros Server

The `ServerReferencePointManager` is needed for Saros Server and has two responsibilities:

1.  It maps a reference point object to a Java IO file resource internally
2.  It determines java resources based on a combination of reference points and relative paths to the resources

All `ReferencePointManager`'s are also needed after the refactoring. They are useful especially for handling Saros activities (e.g. A saros resource was created).
The Saros path (`SPath`) is a use case, why the `ReferencePointManager` is needed:
 The current situation is, that the `SPath` contains a Saros project (`IProject`) instance and an `IPath` instance as relative path. A `SPath` instance can return the Saros project  and `IPath` instance, but it can also determine and return resources in the combination Saros project and `IPath` as relative path. After the refactoring, the SPath will contain a reference point (`IReferencePoint`) and an `IPath`. Determining resources without a `ReferencePointManager` is not possible, so the functions like `getFolder()` or `getFile()` in `SPath` won't work anymore and will be removed.
But for determining the resource from the `SPath`, the `ReferencePointManager` can be used for this.


### Imported Refactoring Steps

For introducing the reference point based interfaces, but also keeping project-based interfaces during the refactoring, following steps are useful:

1.  Mark project-based interface as deprecated
2.  Insert the reference point based interface
3.  Implement the reference point based interface
4.  Remove the project-based interface's implementation and reference this to the reference point based interface
5.  Find alle usages of project-based interfaces and reference them to reference point based interface
6.  Delete project based interface

This procedure has the adventages, that the parallel development of Saros is provided, so that developer can use the reference point based interfaces and the (still) untouched components are not affected directly. After all usages references the reference point based interface, the project based one can be deleted.

There are other refactoring situations to handle for reaching to final state:

#### Rename Class/Methods:

Because of the project-based architecture many classes and methods contain the keyword "project" (like `SharedProjectMapper`). After adjusting to reference points, the naming of them could lead to confusion, so the classes and methods must be renamed.

#### Insert Parameter:

There are some situations during the refactoring, that a Saros project for example determated resources, which the reference point can not do. So the affected methods should be extended by a ReferencePointManager, so that previous functionalities still remains.

#### Remove Parameter:

There are some classes, that a ReferencePointManager not only needed as a parameter extension for the methods, but as a class variable. Because the ReferencePointManager exists as a class variable, the method extensions can be reverted.

#### Pull up field:
If two  sub classes have the same super class and field (ex: the same ReferencePointManager), the field can be pull up to the super class and removed in the sub classes.