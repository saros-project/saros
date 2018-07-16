---
title: History of Saros
---

# {{ page.title }}
{:.no_toc}


This is very roughly how Saros happened:

*   Saros started as an idea of **Stephan Salinger** (research associate
    in the [group of Prof. Lutz Prechelt at Freie Universität
    Berlin](http://www.inf.fu-berlin.de/w/SE)) in early 2006: Wouldn't
    it be great if both members of a pair programming team could each
    use their own machine?\
    They would not even strictly need to be in the same room then
    (assuming some voice connection is present)!
*   He convinced his colleague **Christopher Özbek** and they decided to
    issue a topic for a diploma thesis that called for building a
    prototype of such a tool.
*   **Riad Djemili** took up the topic and developed the first version
    of Saros as an Eclipse plugin.\
    It had a strict driver/observer model (with token handover) but
    worked reasonably well and had a strong-enough architecture to build
    upon it.
*   Christopher and Stephan decided Saros was worth pursuing and started
    working with a long string of students to extend and improve it:\
    During 2008 to 2010, we added many things to Saros: concurrent
    editing, VoIP, screen sharing, a whiteboard for sketching,
    multi-project support, dynamic adding of files, text chat, NAT
    traversal, and various other functionality.\
    Most of it initially did not work or was otherwise deficient (e.g
    too cryptic to find, too minimal, or plain crappy).
*   At the end of that extension period, Saros was fairly large, but
    totally unstable. Also, it was difficult to understand, both for
    Saros programmers and users.
*   So we entered a long period of consolidation (roughly 2011 to 2013):
    Fixing the nasty bugs, re-simplifying the now-contrived architecture
    that had provoked many of those defects, kicking out much of the
    non-core functionality the usefulness of which did not pay for its
    maintenance burden (in particular VoIP and screensharing), etc.\
    Meanwhile, the senior people also changed: Christopher left and
    others took over the chief architect role from him, in particular
    **Franz Zieris**.
*   Another aspect of consolidation was a lot of investment in usability
    work to make Saros easy to install, understand, and use. **Julia
    Schenk** led many rounds of improvement in this regard.
*   One of our most capable students stuck after the end of his thesis
    work and is now a long-time volunteer open source contributor to
    Saros: **Stefan Rossbach**. Without him, Saros would not be what it
    is today.
*   At the end of this consolidation work, we are proud to say Saros is
    now an industrial-strength tool for synchronous distributed
    collaborative work.\
    And we built this interactive, framework-heavy, real-time,
    distributed/concurrent (summing up: difficult) software with mostly
    inexperienced developers and a staff turnover of around 150% a year.
    Astonishing! (Refactoring works -- if you do it seriously.)
*   In 2014, we started to clean up the architecture even further by
    splitting Saros formally into two parts: An Eclipse-specific part
    (for the GUI and wiring) and the Eclipse-independend Saros Core
    (containing the data model, the networking stuff, and the
    concurrency logic).
*   An independent core also allows building session-compatible variants
    of Saros for other IDEs and we are now beginning to do this for
    IntelliJ IDEA.\
    Lithuanian outsourcing provider [NFQ](http://nfq.com) is sponsoring
    this development by paying for a full-time developer, because they
    want to use Saros to introduce an [Agile
    Offsharing](http://www.inf.fu-berlin.de/inst/ag-se/pubs/agogidea-chase2013.pdf)
    work style between them and their customers.
*   ...to be continued

The following is a somewhat more complete list of contributors, small
and large, to Saros:

*   Stephan Salinger
*   Christopher Özbek
*   Riad Djemili
*   Lutz Prechelt
*   Christoph Jacob
*   Björn Gustavs
*   Ulrich Stärk
*   Oliver Rieger
*   Marc Rintsch
*   Lisa Dohrmann
*   Sebastian Ziller
*   Tas Soti
*   Edna Rosen
*   Alena Kiwitt
*   Sandor Szücs
*   Olaf Loga
*   Stefan Lau
*   Henning Staib
*   Eike Starkmann
*   Karl Beecher
*   Florian Pütz
*   Benjamin Aschenbrenner
*   Tobias Albig
*   Robert Ende
*   Robert Fehrmann
*   Franz Gatzke
*   Dennis Gölde
*   Georg Graf
*   Cenk Gündogan
*   Muhammet Karakütük
*   Maximilian Lengsfeld
*   Ines Moosdorf
*   Miriam Ney
*   Jakob Pfender
*   Maximilien Riehl
*   Sebastian Schlaak
*   Simon Schmitt
*   Ramdane Sennoun
*   Jan Wötzel
*   Andreas Haferburg
*   Umut Erdogan
*   Michael Jurke
*   Sebastian Bauch
*   Patrick Bitterling
*   Lin Chen
*   Danou Nauck
*   Klaus Zöller
*   Björn Kahlert
*   Philipp Cordes
*   Christian Dohnert
*   Wjatscheslaw Belousow
*   Antonia Kresse
*   Benjamin Eckstein
*   Christian Kühl
*   Hartono Sugih
*   Hernando Saenz Sanchez
*   Maria Formisano
*   Markus Bischoff
*   Max Losch
*   Michael Prüm
*   Roman Stolzenburg
*   Simon Tippenhauer
*   Thea Schröter
*   Tu Tran
*   Karl Held
*   Arsenij Solovjev
*   Meike Johannsen
*   Alexander Narweleit
*   Franz Zieris
*   Alexander Waldmann
*   Norman Warnatsch
*   Hendrik Degener
*   Jennifer Möwert
*   Holger Freyther
*   Sascha Kretzschmann
*   Michael Scheppat
*   Vera Schlemm
*   Patrick Steinhardt
*   Maria Spiering
*   Sebastian Starroske
*   Lev Stejngardt
*   Richhard Möhn
*   Conrad Läßig
*   Christoph Krüger
*   Patrick Schlott
*   Nils Bussas
*   Andrej Szaffranietz
*   Raimondas Kvietkaukas
*   Holger Schmeisky