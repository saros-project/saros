---
title: Change User Colors
---

Currently, Saros internally has the concept of 5 user colors that will be used locally.
These will be negotiated and assigned to the session participants when a session is started.
As a consequence, the other user in a two-user-session will not necessarily have the user color 1.

## Steps to change the color
{% capture eclipse %}

Saros uses the Eclipse "Annotations" for highlighting your partners' activities and
contributions. Therefore, the preferences are not located at the Saros preferences page.

* Open `"Window" > "Preferences"`.
* Select `"General" > "Editors" > "Text Editors" > "Annotoations"`
(or enter "Annotations" in the search field in the upper left corner).
* Scroll down in the list of "Annotation types" until you find the entries starting with `Saros User`.
* Select the entry you would like to change.
* Make sure to change the color of all entries of one user:
  * `Saros User 1 Contribution` - Defines how the changes of user 1 are highlighted.
  * `Saros User 1 Selection` - Defines how the code selected by user 1 is highlighted.
  * `Saros User 1 Viewport` - Defines how the viewport of user 1 is highlighted.

{% endcapture %}
{% capture intellij %}

* Open the [settings/preferences menu](https://www.jetbrains.com/help/idea/settings-preferences-dialog.html).
* Navigate to `"Editor" > "Color Scheme" > "Saros"`.
* Select the color scheme to change the colors for.
* Expand the user whose colors to change.
* Choose the annotation type to change the color for.
* Adjust the way the annotation is displayed using the options on the right.
  * These changes will be previewed in the frame on the bottom.

{% endcapture %}
{% include ide-tabs.html eclipse=eclipse intellij=intellij %}