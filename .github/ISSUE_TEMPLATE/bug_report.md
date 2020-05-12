---
name: Bug report
about: Create a report to help us improve
title: ''
labels: 'State: Unconfirmed, Type: Bug'
assignees: m273d15 

---

**Describe the bug**
A clear and concise description of what the bug is.

**To Reproduce**
Steps to reproduce the behavior:
1. Go to '...'
2. Click on '....'
3. Scroll down to '....'
4. See error

**Expected behavior**
A clear and concise description of what you expected to happen.

**Log files**

Please provide the corresponding log files, but be sure to **remove or obfuscate sensitive data** you don't want to publish. Please also make sure only to include the log files concerning the session you encountered the bug in.

You can find the log files in the following locations:
* IntelliJ IDEA:
  The log files for JetBrains IDEs are located in the IDE system directory (here called `IDE_SYSTEM_DIR`).
  An overview over all configurations is given  on https://intellij-support.jetbrains.com/hc/en-us/articles/206544519-Directories-used-by-the-IDE-to-store-settings-caches-plugins-and-logs

  For releases 2019.3 and earlier, see https://www.jetbrains.com/help/idea/2019.3/tuning-the-ide.html#system-directory
  For release 2020.1 and later, see https://www.jetbrains.com/help/idea/2020.1/tuning-the-ide.html#system-directory

  * IDE logs - `[IDE_SYSTEM_DIR]/log/idea.log*`
  * Saros logs - `[IDE_SYSTEM_DIR]/log/SarosLogs/*.log`

* Eclipse:
  * IDE logs - `<workspace>/.metadata/.log`
  * Saros logs - `<workspace>/.metadata/.plugins/saros.eclipse/log/<date>/*.log`

**Screenshots**
If applicable, add screenshots to help explain your problem.

**Environment (please complete the following information):**
 - OS: [e.g. Windows 10, Linux Debian Jessie]
 - IDE [e.g. Eclipse 2018-09, IntelliJ IDEA 2019.1.3]
 - Saros Version [e.g. 15.0.0]

**Additional context**
Add any other context about the problem here.
