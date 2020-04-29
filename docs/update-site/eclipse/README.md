# Update Site

In order to avoid to maintain the update-site data in our main repository,
we use a [composite repository](https://wiki.eclipse.org/Equinox/p2/Composite_Repositories_(new)) that
allows to divide a p2 repository/update-site into sub-repositories. Our composite
repository only contains one sub-repository, the repository/update-site [saros-project/update-site-artifacts](https://github.com/saros-project/update-site-artifacts)
which is hosted via GitHub Pages. We use the composite repository to avoid the update-site files in our main
repository.

This leads to a behaviour where <https://www.saros-project.org/update-site/eclipse/> is our update-site url,
which provides the content of the update-site <https://saros-project.github.io/update-site-artifacts/> (and works
in this case similar as a redirect).

We only have to change content of the composite repository if the url (<https://saros-project.github.io/update-site-artifacts/>)
of the sub-repository has changed.

New versions of the update-site have to be pushed to the master branch of [saros-project/update-site-artifacts](https://github.com/saros-project/update-site-artifacts).

## Files 

### composite(Content|Artifacts).xml

These files define the composite repository with just one child (our update-site).
See [here](https://wiki.eclipse.org/Equinox/p2/Composite_Repositories_(new)) for more information.

### p2.index

The file `p2.index` is optional and just defines where to find the composite meta data. without the file an eclipse instance
tries to GET `compositeContent.jar`/`compositeArtifacts.jar` and subsequently tries to GET `compositeContent.xml`/`compositeArtifacts.xml`.
With this file the eclipse instance only tries access the XML files.
