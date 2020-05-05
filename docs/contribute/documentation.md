---
title: Documentation Writing
---

In order to change the documentation, you just simply follow the common [development process](processes/development.md).
The changes are deployed as soon as they are merged into the master branch.

## Guidelines
We prefer a small documentation that contains only important information which are related to Saros. In order to achieve this
please follow these guidelines:
* **Remove obsoleted documentation**
* Rather **reference information** than copy them
* Keep the documentation **short and clear** (e.g. by using lists)
* Avoid new nested link structures and link lists

### Hosting and CI

We use [GitHub Pages](https://pages.github.com/) for hosting our documentation.
The static content is automatically published to the `gh-pages` branch. It is not
intended to change the content manually (you have to change the content in the `docs`
directory on the master branch).

## Create and Change Diagrams
In order to create diagrams, we use [mermaid](https://mermaid-js.github.io/mermaid/#/).
There currently is no jekyll integration for this, meaning you have to add html tags into your
markdown file:
```markdown
<div class="mermaid" markdown="0">
mermaid content...
</div>
```

{% alert warning %}
Don't forget the **`markdown="0"`** attribute in order to avoid Jekyll converting the mermaid code to html.
{% endalert %}

## Write Alerts

In order to write an (Bootstrap) [alert box](https://getbootstrap.com/docs/4.0/components/alerts/)  like:
{% alert danger %}
### Danger warning
{% endalert %}
you have to use our custom `alert` [Jekyll Tag Block](https://jekyllrb.com/docs/plugins/tags/#tag-blocks).
```markdown
{{ "{% alert danger " }}%}
### Danger warning
{{ "{% endalert " }}%}
```
Possible alerts are: `primary`, `secondary`, `success`, `danger`, `warning`, `info`, `light`, `dark`

If you want to inspect or change the corresponding code see `docs/_plugins/alert.rb`.

## Write Accordions

In order to write an (Bootstrap) [accordion](https://getbootstrap.com/docs/4.0/components/collapse/#accordion-example) like:

{% accordion example-accordion-id %}
{% collapsible Entry h5 %}
Content
{% endcollapsible %}
{% collapsible ### Entry h3 %}
Content
{% endcollapsible %}
{% endaccordion %}

you have to use our custom `accordion` and `collapsible` [Jekyll Tag Block](https://jekyllrb.com/docs/plugins/tags/#tag-blocks).
```markdown
{{ "{% accordion example-accordion-id " }}%}
{{ "{% collapsible Entry h5" }}%}
Content
{{ "{% endcollapsible " }}%}
{{ "{% collapsible ### Entry h3" }}%}
Content
{{ "{% endcollapsible " }}%}
{{ "{% endaccordion " }}%}
```

The `collapsible` entry uses a default heading of h5. If you need another heading, define it in markdown (as in `### Entry h3`).
If you want to inspect or change the corresponding code see `docs/_plugins/accordion.rb` and `docs/_plugins/collapsible.rb`.

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

## Reference IDE Specific Documentation

You can reference the IDE specific version of a page via a URL parameter `tab=<ide>` (e.g. `<url>?tab=intellij` or `<url>?tab=eclipse`).
Example:
* [IntelliJ content of this page](?tab=intellij)
* [Eclipse content of this page](?tab=eclipse)

## Compile GitHub Pages

### Compile Locally
#### Install Required Tools and Dependencies

* [Install ruby 2.x or higher](https://www.ruby-lang.org/en/documentation/installation/)
* Install bundler:
  * Execute:`gem install bundler`
* Install [npm](https://www.npmjs.com/get-npm).
* Change dir to the documentation directory (`<repository_dir>/docs`)
* Install jekyll and its dependencies
  * Execute `npm install`
  * Execute `bundle install`

#### Compile and Show Documentation

* Execute `bundle exec jekyll serve`
  * This command spawns a webserver on port 4000
* Open you browser and open `localhost:4000`
* Use `bundle exec jekyll serve -i` for the interactive mode that reloads the page after content changes.

##### Troubleshooting

* **If** `bundle exec jekyll serve`
  **fails with** `bundler: command not found: jekyll`
  **try** `jekyll serve`

## Find broken links automatically

You can [`html-proofer`](https://github.com/gjtorikian/html-proofer) to
verify your build results (produced by `bundle exec jekyll serve` or `bundle exec jekyll build`)
in the directory `docs/_site`.

```bash
htmlproofer \
  ./_site \
  --assume-extension \ # Allows urls without a file extension
  --allow-hash-href \ # Allow hash refs (e.g. '#test')
  --alt-ignore '/.*/'\ # Ignore missing "alt" warning
  --file-ignore "/node_modules\/.*/" # Ignore html files in the 'node_modules' dir
```

If you want to exclude external links add the option `--disable-external`.