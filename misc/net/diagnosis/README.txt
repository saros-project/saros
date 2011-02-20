Saros Connectivity Diagnosis
----------------------------
This script is used to determine connectivity problems with the XMPP server 
"saros-con.imp.fu-berlin.de".

Usage
-----
1) Place the script "saros-connectivity-diagnosis_X_Y.sh" in a writeable
   directory. (X and Y correspond to the script's version number)

2) Run "sh saros-connectivity-diagnosis_X_X.sh" in the command line / 
   terminal.
   This generates a diagnosis file named
   "[DATE]T[TIME]-saros-connectivity-diagnosis.txt" in the working
   directory.

3) Send the diagnosis file to dpp-user@lists.sourceforge.net.
   We will answer you shortly with details to your problem.

Changelog
---------
0.4
Bugfix: ifconfig is not correctly redirected to diagnosis file

0.3
Improved usability

0.2
Compatibility to major UNIX/Linux releases

0.1
Initial version
Only tested on Mac OS X 10.6