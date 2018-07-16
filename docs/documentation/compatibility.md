---
title: Compatibility of the Eclipse Saros Plugin
---

# {{ page.title }}
{:.no_toc}

## Eclipse plugins tested with Saros Eclipse plugins tested with Saros  

Saros is compatible with any well-behaved language-plugin whose editor
is based on Eclipse's standard text editor class.

The following table lists language plugins for which we have explicitly
verified that they work with Saros.\
The version numbers indicate the latest version tested.

 

  Plugin/Product                                                                Language                                 Version   Eclipse version        Saros version tested   Supported   Open Bugs
  ----------------------------------------------------------------------------- ---------------------------------------- --------- ---------------------- ---------------------- ----------- -------------------------------------------------------------------------------------------------
  [Aptana Studio](http://www.aptana.com/)                                       PHP, Ruby, Python, Javascript            2.0       Eclipse 3.5 Galileo    9.12.4                 Yes          
  [Aptana Studio](http://www.aptana.com/)                                       HTML, CSS, ERB                           2.0       Eclipse 3.5 Galileo    9.12.4                 No          [2939379](https://sourceforge.net/tracker/?func=detail&aid=2939379&group_id=167540&atid=843359)
  [CDT](http://www.eclipse.org/cdt/)                                            C/C++                                    6.0.1     Eclipse 3.5 Galileo    9.12.4                 Yes          
  [EasyEclipse](http://www.easyeclipse.org/site/home/)                          C/C++, PHP, Ruby & Rails, Python, Perl   1.2.2.2   Eclipse 3.5 Galileo    9.12.4                 No           
  [EPIC](http://www.epic-ide.org/)                                              Perl                                     0.6.35    Eclipse 3.5 Galileo    9.12.4                 Yes          
  [JDT](http://www.eclipse.org/jdt/)                                            Java                                     3.5       Eclipse 3.5 Galileo    9.12.4                 Yes          
  [PHPeclipse](http://www.phpeclipse.com/)                                      PHP                                      1.2.1     Eclipse 3.4 Ganymede   9.6.23                 Yes          
  [PDT](http://www.eclipse.org/pdt/)                                            PHP                                      2.1.0     Eclipse 3.5 Galileo    9.12.4                 Yes          
  [PyDev](http://pydev.sourceforge.net/)                                        Python                                   1.5.4     Eclipse 3.5 Galileo    9.12.4                 Yes          
  [Dev3](http://www.dev3.org/)                                                  PHP, TypoScript                          1.0.3     Eclipse 3.5 Galileo    9.12.4                 No          [2836302](http://sourceforge.net/tracker/?func=detail&aid=2836302&group_id=167540&atid=843359)
  [Flex Builder 3](http://www.adobe.com/products/flex/features/flex_builder/)   MXML, ActionScript™, and CSS             3         Eclipse 3.5 Galileo    9.12.4                 No           

If your favorite plug-in is not supported feel free to [report a
bug](https://github.com/saros-project/saros/issues) or tell us
at <saros-devel@googlegroups.com>.
 
  

## Types of incompatibility

Generally speaking, the following types of problems can potentially
occur when other plugins are combined with Saros:

*   The other plugin requires versions of Eclipse with which Saros is
    not compatible
*   Minor problems from conflicting markup/highlighting behavior, e.g.
    heavy use of the annotation bar.
*   Various kinds of problems may occur if other plug-ins (or any sort
    of firewall software outside of Eclipse) intercept network traffic.
*   Integrity problems may occur if other software modifies
    files without notifying Eclipse. Well-behaved plugins will not
    do this. (However, Saros includes a consistency watchdog that will
    often recognize and repair such situations quickly.)