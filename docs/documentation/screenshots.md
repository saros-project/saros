---
title: Screenshots
---

{% capture eclipse %}

Let's gather some impressions of Saros/E.

![Saros
Annotations](images/screenshot_annotated.png){:class="img-fluid"}

**There are several features that enable you to remain aware of what
your collaborators are doing.**

1.  Marked files, which users with write-access have open (yellow) or
    visible (green).
2.  Current text selected by another user.
3.  Text changed by another user (by default, the last twenty edits
    are marked).
4.  Current view scope of other users (i.e. what portion of a
    file can they see).

**The main components of the Saros interface.**

5.  The users in the current session. Notice how their color here
    matches their annotations in the editor.
6.  The contact list, where all the people known to you are kept.
7.  The chat window. Here you can send instant messages to everyone in
    the current session.

------------------------------------------------------------------------

![Saros
Whiteboard](images/screenshot_whiteboard.png){:class="img-fluid"}

**There is the whiteboard feature that allows you to sketch and share
your ideas with other users.**

8.  The list of tools to sketch the ideas of an user.
    -   If you want to select some drawn ideas, you need to use the
        tools Select and Marquee.
    -   To Draw your ideas, you have the choice to take the prescribed
        objects, like ellipse or rectangle, or you can choose the
        freehand pencil.

9.  The surface for the sketches, which are created by the users.
10. The menu bar of the whiteboard with the functions of undo,
    selecting, copy, pasting, deleting and resizing of
    the sketch-surface.

{% endcapture %}

{% capture intellij %}

As Saros/I is still in its early alpha stages and the UI is still actively being changed, we haven't gotten around to create this page yet.

{% endcapture %}

{% include ide-tabs.html eclipse=eclipse intellij=intellij %}
