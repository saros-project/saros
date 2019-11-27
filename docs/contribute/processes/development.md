---
title: Development Process
---

## Development Workflow

Our development workflow bases on the standard [GitHub fork workflow](https://guides.github.com/activities/forking/).

Before a pull request is merged, it will have to pass out [review process](review.md).

## Pull Request Structure

In the Saros project, we use two different strategies to merge pull requests: [squash and merge](#squash-and-merge) and [rebase and merge](#rebase-and-merge).

Even though only maintainers can merge pull requests (meaning you can't actively choose the strategy to use), these different merge strategies have different requirements and are used in different cases, so please have them in mind when creating a pull request.
Which strategy is used to merge a particular pull request depends on the size, content and structure of the commits contained in the pull request. This is explained in more detail in the corresponding subsections.

Independently of the merge strategy, please make sure that all commits have an [appropriate commit message](../guidelines.html#commit-message).

### [Squash and Merge](https://help.github.com/articles/about-pull-request-merges/#squash-and-merge-your-pull-request-commits)

*This is the preferred merging strategy for small and coherent changes.*

If your change only touches a specific file, functionality, or API and is not "to large" (this is somewhat subjective and will be up to you and the reviewers; as a general rule of thumb, it should not change more than 200 lines of code).
This allows the changes to be bundled into one concise commit.

### [Rebase and Merge](https://help.github.com/en/articles/about-pull-request-merges#rebase-and-merge-your-pull-request-commits)

*This is the preferred merging strategy for larger changes.*

If the pull request contains many changes (touches many different files/areas of the code and changes a large number of lines of code), we would prefer to split it into multiple commits make the review process easier and improve the usability of the git history.
This is especially true if your pull requests contains changes made by automated tools (i.e. refactorings) as they can be large in size but don't need to be reviewed as thoroughly.
So please make sure to structure you pull request accordingly by splitting it into sensible commits.

Furthermore, to enable us to retain this structure when merging the pull request, please make sure to keep the commit history of the pull request clean (only include commits that are supposed to end up on the main repository) and not to include any merge commits (as they unnecessarily pollute the history) in your pull request so that it can be easily rebased and merged.
This will most likely require you to rebase and amend your branch locally when updating your changes and then force push it to your fork to update the pull request.
