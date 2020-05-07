---
title: Follow Mode
---

You can use this feature to follow a single participant's navigation
activities and changes. 
* Whenever the followed participant opens an editor for a shared file on
  their computer, a matching editor is opened in your IDE.
* Any time the followed participant switches to view a different shared editor, a matching editor is switched
  to in your IDE.
* As the followed participant scrolls in a shared editor, the local viewpoint is
  moved along accordingly in your IDE to ensure that you see what they see.
  You still have the ability to scroll the local viewpoint on your own,
  but it will still be reset every time the followed user scrolls their editor.

**Prerequisite:** You have to be in a [session with another user](../getting-started.md).

![saros follow-mode gif](/assets/images/animation/saros-fm.gif){:class="img-fluid"}

## Following Another Participant

- Click the "Follow" button (![follow icon](images/icons-i/follow.png)).
- Select the user to follow from the list and click the entry.

*Alternatively:*

- Select another participant in the list of session participants in the session view (on the left) and right-click their name.
- Click the option "Follow participant".

## Leaving the Follow Mode

{% capture eclipse %}
- Click the "Follow" button (![follow icon](images/icons-e/follow.png))

*Alternatively:*

- Open the drop-down list beside the "Follow" button and click `Leave follow mode`.

**OR**

- Select the followee in the list of session participants in the session view (on the left) and right-click their name.
- Click the option "Stop Following".

{% endcapture %}
{% capture intellij %}

- Click the "Follow" button (![follow icon](images/icons-i/follow.png)).
- Click the option "Leave follow mode".
{% endcapture %}
{% include ide-tabs.html eclipse=eclipse intellij=intellij %}

## Staying Aware of Your Fellow Participants

There are multiple ways of staying aware of what a driver is currently
doing. See the [awareness information section](awareness-information.md) for more details.
