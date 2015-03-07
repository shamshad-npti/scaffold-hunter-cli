Installation
========================
Scaffold Hunter is written completely in Java, therefore no installation
is required to run the application. Simply download the Scaffold Hunter 
ZIP archive from the website and extract it somewhere on your computer 
(e.g., in case you have admin rights, C:\Program Files\ScaffoldHunter on 
Windows or /opt/scaffoldhunter on Unix/Linux/Mac, or alternatively in any
local directory).

CLI Installation from Source
===========================
  * Prerequisite: java and ant
  * Run compile.bat to compile the source
  * Once source has been compiled successfully, open command prompt 
  	change directory to path/to/dist and run 'sh help' which will list
  	all available command

Starting Scaffold Hunter
========================
To launch Scaffold Hunter, simply run the supplied start-script (run.cmd 
on Windows or run.sh on Unix/Linux/Mac). If you want to work with large 
data sets, you can increase the maximum memory available for Scaffold 
Hunter by editing the start-script. Please refer to the manual contained 
in the ZIP archive for further information.


Requirements
========================

Hardware:
  * CPU: A CPU with at least 2 GHz is recommended to run the program.
  * RAM: At least 1 GB must be available to the Java Virtual Machine 
    running the program. 4 GB are recommended for full functionality.
  * Hard Disk: 40 MB of free space are sufficient to store the program
    and the database connections.
  * Display: A display with a resolution of at least 1024x768 is 
    recommended.

Software:
  * JRE: A Java VM supporting Java SE 6 code is required to run the 
    program. Please refer to http://www.java.com for installation 
    instructions concerning Java.
  * Database: Scaffold Hunter comes with HSQLDB which is sufficient 
    to store small personal data sets. For productive use a MySQL 
    server is recommended. Please refer to http://www.mysql.com for 
    installation instructions concerning MySQL.