---
title: Saros Documentation
toc: false
---

## What is Saros? What is it not?

### Saros is an Open Source IDE plugin for distributed collaborative software development.

*   Currently, it works only with and within Eclipse. Saros users can
    use all Eclipse functionality as usual.
*   We are working on an IntelliJ version as well.

### Saros is a real-time collaborative editor for eclipse projects.

*   All collaborators have an **identical copy of Eclipse projects.**
*   Two or more users can **jointly edit files** in the project.
*   **Each** **user** has and **modifies** his or her own copy of the
    **file locally.**
*   Saros **keeps these copies in sync** by transmitting each change to
    all of the other collaborators.

### Saros supports up to 5 participants at once.

*   Saros is designed to at least work with two participants in a
    session - as inherent for pair programming.
*   But it supports **up to 5 distributed parties** in a session.
*   The initiator of a session, the **host**, has a **privileged role**.
    To get familiar with this concept check out our comic:

[//]: # This link does not point to a markdown file because Jekyll is unable to convert this nested link into a link with html extension
[![](images/comics/small_6-1_host-comic_frame-1.jpg)
![](images/comics/small_6-1_host-comic_frame-2.jpg)
![](images/comics/small_6-1_host-comic_frame-3.jpg)
![](images/comics/small_6-1_host-comic_frame-4.jpg)
![](images/comics/small_host-comic_frame-5.jpg)
![](images/comics/small_host-comic_frame-6.jpg)](host-comic.html)


### Saros is not screen sharing, desktop sharing, or application sharing.

*   That means for instance that it does not support joint
    interactive [testing](host-comic.md).

## Saros can be used in various scenarios.

* **Joint review**

  One participant (**"driver"**)
  reviews the contents of one or more
  files together with other
  participants
  (**"observers"**). Saros is set to
  always show the observers the same
  region of text (program code or
  whatever else) that the driver sees
  (**"follow mode"**). At any time,
  any participant (driver as well as
  observers) can highlight text with
  the mouse for all the others to
  see. Also, any participant can
  become driver at any time.


* **Introducing beginners**

  Much like before, except that
  explaining rather than reviewing is
  the goal. Each of the beginners
  (staying in the observer role) can
  individually peek at other regions
  of the file or even other files
  without influencing the flow of the
  session for the others. To do that,
  s/he will temporarily leave follow
  mode.


* **Distributed Party Programming**

  **Two or more participants work
  together in a loosely coupled
  fashion.** They **work independently
  much of the time, but can call one
  of the others to help whenever the
  need arises**, for possibly only a
  very short time or for a longer
  episode. In this mode, distributed
  work can even be more powerful than
  working in the same room, as nobody
  needs to leave a seat to help
  multiple others in the course of an
  hour. Just think how this can speed
  up the coordination work just before
  release time after a code freeze!


* **Distributed Pair Programming **
  is a particular form of Distributed
  Party Programming in which two
  people develop code or text in
  continuous close collaboration,
  discussing the approach and
  combining the best of their ideas.
