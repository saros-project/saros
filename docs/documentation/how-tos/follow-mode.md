---
title: Follow Mode
---

You can use this feature to follow a single participant's navigation
activities and changes. 

-   Whenever the participant opens a file on his/her computer, it is opened on
    yours too.
-   Any time the participant switches to view a different file, it is switched
    on yours.
-   As participant scrolls through a file, the viewpoint is moved on your
    computer also, so that you see what s/he sees.

**Prerequisite:** You have to be in a [session with another user](../getting-started.md).

![saros follow-mode gif](/assets/images/animation/saros-fm.gif){:class="img-fluid"}

## Following Another Participant

{% capture eclipse %}
- Open the drop-down list beside the "Follow" button (![follow drop-down](images/icons-e/follow.png)).
- Select the user to follow from the list and click the entry.

{% endcapture %}
{% capture intellij %}
- Click the "Follow" button (![follow icon](images/icons-i/follow.png)).
- Select the user to follow from the list and click the entry.

*Alternatively:*

- Select another participant in the list of session participants in the session view (on the left) and right-click their name.
- Click the option "Follow participant".
{% endcapture %}
{% include ide-tabs.html eclipse=eclipse intellij=intellij %}

## Leaving the Follow Mode

{% capture eclipse %}
- Click the "Follow" button (![follow icon](images/icons-e/follow.png))

*Alternatively:*

- Open the drop-down list beside the "Follow" button and click `Leave follow mode`.

## Staying Aware of Your Fellow Participants

There are multiple ways of staying aware of what a driver is currently
doing:

-   In the package explorer (or resource navigator):
    - ![active file](images/icons-e/active_file.png) A colored dot decorates the file that a participant has currently
        in focus. The color resembles the color of the active user.
    - ![shared file](images/icons-e/shared_file.png) A
        blue arrow decorates a file that is shared with
        other participants.
-   Cursors:
    The position of a participant's cursor appears in the file in
    his/her color.
-   Selections:
    Any text selected by a participant also appears highlighted in
    the file.
-   Changes:
    If a participant writes something, his/her text will appear
    highlighted in his/her color.
-   Locate participants:
    On the right side of editors you can see colored bars representing
    the viewports of each user. These show which part of the file each
    user can see.
-   Follow participants:
    Follow mode allows you to follow all movements of another user as
    s/he moves within and between files. In the Session list, right
    click on the user you wish to follow and select Follow Participant.
{% endcapture %}
{% capture intellij %}

- Click the "Follow" button (![follow icon](images/icons-i/follow.png)).
- Click the option "Leave follow mode".
{% endcapture %}
{% include ide-tabs.html eclipse=eclipse intellij=intellij %}
