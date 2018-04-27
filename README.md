Open Meerkat Bot Simulation Testbed
-----------------------------------

This is a fork from https://code.google.com/p/opentestbed

I've ported it to a maven project, so it should be easier to setup the development environment (specially in an IDE other than Eclipse) and make it run. For more information on how to run it, see the [Wiki](https://github.com/corintio/opentestbed/wiki/How-To-Simulate-Cash-Games)


Dependencies
------------

This project requires the Meerkat API 2.5 available at http://www.poker-academy.com/community.php
The .jar is located in the /lib folder. Documentation can be found at the URL above.

To install the meerkat api into your maven repository:
````mvn install:install-file -Dfile=lib/meerkat-api.jar -DgroupId=com.biotools.meerkat -DartifactId=meerkat-api -Dversion=2.5 -Dpackaging=jar````