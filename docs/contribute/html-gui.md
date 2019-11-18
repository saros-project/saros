---
title: HTML-GUI (Deprecated)
---


After starting the development of the Saros for IntelliJ version, the idea came up to develop an agnostic GUI that allows to maintain just one GUI independent of the IDE.
The selected solution was to embed a browser that renders a JavaScript/HTML-GUI and communicates all information to the Java backend.
The main issue with this approach is the integration of a browser that is easy to release without creating a big jar file that contains a whole browser.
The first implementation used the SWT-Browser. The second approach used the JavaFX WebView that is based on WebKit.
In the following you find a list of pro and cons for each approach and why we were not able to use the approach. If you want to go even more back in time see this [thesis](https://www.inf.fu-berlin.de/inst/ag-se/theses/Cikryt15-saros-browser-as-ide-gui.pdf).

## SWT-Browser
The solution based on the SWT browser used the SWTBrowser which was delivered with the swt toolkit that is released with eclipse.

### Eclipse Integration
In order to build and test Saros it was necessary to download an OS specific SWT version (this was handeled by the build tool). It was easy to
release the solution for eclipse, because eclipse already provides a swt version, because the whole IDE is written in swt.

### IntelliJ Integration
The IntelliJ version of the integration required to load a swt version manually which was loaded during runtime of Saros with a self-written
SWT loader. This is not a solution that is safe to be released. The whole IntelliJ specific code was located in the package `saros.intellij.ui.swt_browser`.

### Issues With the Integration
Even if the SWT library was available and loaded, the browser integration was brittle.
This was rooted in the way the SWT browser was implemented. Instead of releasing an browser with swt, the OS default browser is used.
This integration of the default browser was brittle on all platforms, but the most unstable was Linux. The newer versions of swt (e.g. swt released with eclipse 4.8) are more stable, but the `BrowserFunction` (allows to call java from javascript) was still not stable in all versions (unavailable in 4.8-4.12 due to [#538335](https://bugs.eclipse.org/bugs/show_bug.cgi?id=538335)).

## JavaFX
The second approach was to embed a view of the GUI toolkit JavaFX scene into the view of the Saros plugin. JavaFX provides a WebView with integrated WebKit version.
Furthermore, JavaFX provides a bridge to swt as well as to swing. Therefore it is possible to embed a JavaFX component into an eclipse plugin (that uses swt) as well
as into an IntelliJ plugin (that uses swing). 

### Eclipse Integration
We integrated JavaFX in eclipse with the help of the efxclipse tooling and integrated the feature `org.eclipse.fx.target.rcp.feature`.
This feature provides the osgi bundle `org.eclipse.fx.ui.workbench3`. This bundle provides the class `org.eclipse.fx.ui.workbench3.FXPartView` that allows to write an e3 (old eclipse ui model) view with JavaFX components.
This class simply wraps the `javafx.embed.swt.FXCanvas`. In order to load the JavaFX library during runtime the osgi bundle `org.eclipse.fx.osgi` is also provided. This bundle is a fragment of the bundle `org.eclipse.osgi` and adds a hook that enhances the classpath of eclipse in order to load the additional javafx/swt jar that allows to integrate javafx components into swt. If this bundle is missing, the class `javafx.embed.swt.FXCanvas` is missing.

### IntelliJ Integration
The integration into IntelliJ was straightforward: create and use a `javafx.embed.swing.JFXPanel`. The following [post](https://stackoverflow.com/a/35611230/6948317) gives an implementation example.

### Eclipse Tests
The first tests on Windows and Linux were successful (tested with Eclipse Neon, Oxygen, 2019-06 and JDK8-9). The only issue
was that it was necessary with JDK8 to force eclipse to use GTK 2, because the JavaFX library (shipped with JDK8) was linked to GTK 2.

For using this solution with JDK8-10 it would be necessary to install JavaFX or download a JDK with JavaFX.
Unfortunately, it was decided that the JavaFX library is not released with the JDK anymore.
Therefore, the way changed how JavaFX is used. The [official documentation](https://openjfx.io/openjfx-docs) proposes the following solutions:
* Add the JavaFX modules into the module path of your `java`/`javac` call (would require that the user changes the IDE start script).
* Create a platform-specific runtime image/JRE containing the JavaFX version. (would require to ship a Saros JDK or the user would have to create an own JRE)

Both variants are not applicable, because we are a plugin that has to use what is provided by the IDE.

One approach that might work for eclipse, but not Intellij (therefore we never tried the approach) could be:
* Create osgi bundles for each JavaFX module for each platform
* Add a corresponding `Eclipse-PlatformFilter:` to the manifest file.
* Add the line `Java-Module: <JavaFX module name>` to the manifest file.
* Provide all bundles via an update-site an hope that the eclipse plugin installer decides which platform is required and installes only the required version.
* The `org.eclipse.fx.osgi` bundle should detect the JavaFX bundle and add it to the class or module path during runtime.

### IntelliJ Tests
We tested the IntelliJ version only with 2019-01 and the corresponding JDKs after we noticed that the approach does not work in all cases.


## Usage of the Removed HTML-GUI
**This documentation is only relevant if you want to try the old HTML-GUI approach**<br/>
Below you can find how you can build and test the HTML-GUI. One prerequisite is to checkout the [last commit](https://github.com/saros-project/saros/commit/18d77e9f18d50accd1267f4d801c8f74ef301715) including the HTML-GUI.

The usage of the HTML version of Saros is guarded by a feature toggle.

### Eclipse Setup

In Eclipse you have to uncomment the `SarosViewBrowserVersion` view in `eclipse/plugin.xml`.
and provide the Java property
```properties
saros.swtbrowser=true
```
by changing the corresponding line in `eclipse/saros.properties`.

To be able to see the HTML GUI in eclipse when running Saros, you have
to open the Saros view in eclipse via Window > Show View > Other > Saros > Saros View.

### IntelliJ Setup

In IntelliJ you just have to provide
```properties
-Dsaros.swtbrowser=true
```
as VM Option (in the Run Configurations dialog). 


### Installing Dependencies

To develop the Saros GUI JavaScript application, the
[NodeJS](http://nodejs.org) package manager,
[NPM](https://www.npmjs.com/) is required. NPM is responsible for
providing external JavaScript resources and tools for tasks like running
unit tests and building the application. For more information about
installing NodeJS and NPM see [here](http://nodejs.org).

Before you are able to run the build tasks you have to navigate to
`ui.frontend/html` and run `npm install` to
download and install external dependencies.

Required dependencies and tools as well as convenient script definitions
are defined in `ui.frontend/html/package.json`
see [here](https://docs.npmjs.com/files/package.json) for a detailed
documentation.

### Building the JavaScript Application

Currently, the building of the JavaScript application is NOT integrated
in the general Saros build process and must therefore be executed
separately. To build the JavaScript application, navigate to
`ui.frontend/html` and run `npm run build`.
Alternatively it is also possible to run `npm run watch`, which builds
the application once and then listens to any future changes and
automatically rebuilds everytime.

### Run the HTML-GUI

* Perform the IDE specific setup steps as descibed above.
* Install javascript dependencies and build the pages as described above.
* Start an intellij/eclipse test instance and search for the saros html view.

If the view is empty, well you might have issues with the browser integration (which is the reason we stopped working on the approach).
