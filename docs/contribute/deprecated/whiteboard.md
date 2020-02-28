---
title: Saros Whiteboard (deprecated)
---

The Saros Whiteboard was designed to enable Saros users to communicate ideas graphically
via a synchronized sketching tool. The first implementation was based on the Saros/E plugin.
In order to provide an approach that can be re-used in Saros/I we developed an HTML-GUI version
of the whiteboard.
However, the HTML Whiteboard was not deliverable, because of the reasons described in [HTML-GUI](html-gui.md).

The last commit that contains both whiteboard approaches is [`e257a06164dce14041d3cf3536329090e2faa2c1`](https://github.com/saros-project/saros/commit/e257a06164dce14041d3cf3536329090e2faa2c1).

## Developing the HTML Whiteboard

### Setup
First of all, the view has to be un-commented from `saros.whiteboard/plugin.xml`, the view has the following id: `saros.whiteboard.ui.HTMLWhiteboardView`.

Then install the dependencies using the command `npm run setup`, note that `npm install` does not suffice.

### Build for Development
Use the command `npm run dev` to run webpack in watch mode, any changes to the code will automatically trigger the bundling. To test the whiteboard, simply open the `index.html`. You can adjust the build configurations in the `webpack.config.js` file.

### Build for Production
Use the command `npm run build`. When building for production, all of the resources of the whiteboard will be bundled into one file `dist/index.html`.

## Technology Stack
The Saros HTML Whiteboard uses a graphics library called [Fabric.js](http://fabricjs.com/) to display the shapes in the page, it also uses [React](https://reactjs.org/) to render the menu.

As for the bundling, it uses [Webpack](https://webpack.js.org/) to bundle the resources using [Babel](https://babeljs.io/) loader to transpile the code to a more browser-friendly version.

## How it Works Internally

The Saros HTML Whiteboard builds upon the internal infrastructure of the [GEF](https://www.eclipse.org/gef/) based Whiteboard and uses most of its components such as [SXE](https://xmpp.org/extensions/xep-0284.html).

SXE is a synchronization protocol for syncing XML files, which fits GEF since it renders XML-based [SVG](https://www.w3.org/TR/SVG2/) shapes.

The Java part of the whiteboard contains an implementation of SXE and is in turn responsible for the synchronization of the whiteboard's state and data.
The HTML part of the whiteboard acts as a view and enables the rendering and the manipulation of the aforementioned data.

For more detailed information on how the GEF based Whiteboard works and the available implementation of the SXE, you can check the [Master thesis](https://www.inf.fu-berlin.de/inst/ag-se/theses/Jurke10-saros-whiteboard.pdf) behind it (in German).
