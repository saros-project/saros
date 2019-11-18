---
title: Documentation Writing
---

The whole documentation is hosted by [GitHub Pages](https://pages.github.com/) and the corresponding markdown sources are
located in the `docs` directory of the [Saros main repository](https://github.com/saros-project/saros). Therefore the
process of the documentation modification is the same as the common [development process](processes/development.md).

## Guidelines
We prefer a small documentation that contains only important information which are related to Saros. In order to achieve this
please follow these guidelines:
* **Remove obsoleted documentation**
* Rather **reference information** than copy them
* Keep the documentation **short and clear** (e.g. by using lists)
* Avoid new nested link structures and link lists

## Write IDE Specific Documentation
Use the following tags if you want to embed an IDE specific part into a documentation or want to provide IDE specific versions of a page.

**Markdown:**
```markdown
{{ "{% capture eclipse " }}%}
## Eclipse content here
Eclipse content here
{{ "{% endcapture " }}%}
{{ "{% capture intellij " }}%}
## Intellij content here
Intellij content here
{{ "{% endcapture " }}%}
{{ "{% include ide-tabs.html eclipse=eclipse intellij=intellij " }}%}
```
**Rendered Markdown:**
{% capture eclipse %}
## Eclipse Content Here
Eclipse content here
{% endcapture %}
{% capture intellij %}
## IntelliJ Content Here
Intellij content here
{% endcapture %}
{% include ide-tabs.html eclipse=eclipse intellij=intellij %}


**Attention:** The major disadvantage of this solution is that we use [jekyll liquid tags](https://jekyllrb.com/docs/liquid/).
Therefore the content of the documentation could break the documentation build! This happens if you have typo in your tags or forgot something etc.
If you want to make sure that the documentation works, compile the documentation (see below for more information).

## Reference IDE Specific Documentation

You can reference the IDE specific version of a page via a URL parameter `tab=<ide>` (e.g. `<url>?tab=intellij` or `<url>?tab=eclipse`).
Example:
* [IntelliJ content of this page](?tab=intellij)
* [Eclipse content of this page](?tab=eclipse)

## Compile GitHub Pages
### Compile and Show via Docker
The whole environment described below is already available in the docker image `starefossen/github-pages`. Use the following
steps in order to compile and show the GitHub pages:
* Open a bash terminal
* Change the working directory to the `docs` directory in your local Saros git repository
* execute `docker run -t --name jekyll -v "$PWD":/usr/src/app -p "4000:4000" starefossen/github-pages`

In order to remove the corresponding container simply execute `docker stop jekyll`

### Compile Locally
#### Install Required Tools and Dependencies

* [Install ruby 2.x or higher](https://www.ruby-lang.org/en/documentation/installation/)
* Install bundler:
  * Execute:`gem install bundler`
* Install jekyll and its dependencies
  * Change dir to this directory (`<repository_dir>/docs`)
  * Execute `bundle install`

See [here](https://help.github.com/articles/setting-up-your-github-pages-site-locally-with-jekyll) for more information.

#### Compile and Show Documentation

* Move into this directory (`<repository_dir>/docs`)
* Execute `bundle exec jekyll serve`
  * This command spawns a webserver on port 4000
* Open you browser and open `localhost:4000`
* Use `bundle exec jekyll serve -i` for the interactive mode that reloads the page after content changes.

##### Troubleshooting

* **If** `bundle exec jekyll serve`
  **fails with** `bundler: command not found: jekyll`
  **try** `jekyll serve`
