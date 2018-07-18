# Documentation writing

## Compile GitHub Pages locally

### Install required tools and dependencies:

* [Install ruby 2.x or higher](https://www.ruby-lang.org/en/documentation/installation/)
* Install bundler:
  * Execute:`gem install bundler`
* Install jekyll and its dependencies
  * Change dir to this directory (`<repository_dir>/docs`) 
  * Execute `bundle install`

See [here](https://help.github.com/articles/setting-up-your-github-pages-site-locally-with-jekyll) for more information.

### Compile and show documentation

* Move into this directory (`<repository_dir>/docs`) 
* Execute `bundle exec jekyll serve`
  * This command spawns a webserver on port 4000
* Open you browser and open `localhost:4000`
* Use `bundle exec jekyll serve -i` for the interactive mode that reloads the page after content changes.

#### Troubleshooting

* **If** `bundle exec jekyll serve`
  **fails with** `bundler: command not found: jekyll`
  **try** `jekyll serve`
