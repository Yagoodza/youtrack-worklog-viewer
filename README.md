YouTrack Worklog Viewer
=======================

YouTrack is a commercial issue tracker created by [Jetbrains](https://www.jetbrains.com/youtrack/) where you also have the possibility to track the time you spent on each individual issue. Unfortunately, if you are like me and don't book your time right away, there is limited options to get an overview of how much time you have already booked on every single day of the week (addressed in [this issue](https://youtrack.jetbrains.com/issue/JT-29224)).

With this tool you can see the time you spent on each project and individual task as depicted in the following screenshot.

![Screenshot of the report](https://raw.githubusercontent.com/pbauerochse/youtrack-worklog-viewer/master/screenshot.png) (*Issues column has been obfuscated for the screenshot*)

## Latest Version

[Download v3.0.0](https://github.com/pbauerochse/youtrack-worklog-viewer/releases/tag/3.0.0)

If you find this tool useful and would like to show me your appreciation [buy me a beer](https://www.paypal.me/patrickbrandes)

## Important changes

* **v3.0.0**
  * Added a plugin mechanism, that allows you to extend the Worklog Viewer with own functionality
* **v2.5.0**
  * Switching to Java 11 as minimum required Java version. If you need to stick to Java < 11 please use the [v2.4.3 release](https://github.com/pbauerochse/youtrack-worklog-viewer/releases/tag/2.4.3) 
* **v2.4.0**
  * Dropping support for YouTrack versions < 2018.1
  * Dropping OAuth2 and password authentication in favor of new token based authentication

## How does it work?
You simply enter the URL to your YouTrack installation, and your own login data at the settings screen, select the reporting range in the main window and click on the "Download worklogs" button. That's it!

## FAQ

**What are the requirements?**

* *At least a Java 11 Runtime for the most recent version*
* *Of course you need a valid YouTrack account for the instance you want to fetch the worklogs from*

**How do I start the application?**

*Simply open up your terminal/console and start the application with `java -jar youtrack-worklog-viewer-[version].jar` or right click the file and select `Open with...` and then select the path to your java executable*

** How do I setup the authentication in YouTrack?

* There is a tutorial on how to configure YouTrack authentication on the [Wiki page](https://github.com/pbauerochse/youtrack-worklog-viewer/wiki/Authentication-with-YouTrack).

**Which YouTrack versions are supported?**

*Support for YouTrack versions older than 2017.4 has been dropped in the Worklog Viewer version 2.4.0. If you have a YouTrack version between 6 and 2017.4, please use any YouTrack WorklogViewer release before 2.4.0*

**How does the tool get the data from YouTrack?**

*Depending on your YouTrack version, the WorklogViewer creates and downloads a TimeReport on your behalf, or executes a query and downloads the results.*

**What reporting options are available?**

*You can create a report for the current week, previous week, current month and previous month or a free time range. You can also enter your daily work hours to adjust the time format (e.g. 9 booked hours will be presented in an 8 hour workday with `1d 1h` and in an 9 hour workday with `1d`)*  

**I just get a blank report. What's wrong?**

There are several reasons, why this might be the case:

* **You entered a wrong username in the settings** - *This tool checks your personal worklog by comparing your username from the settings screen, with the worklog author name from the report. YouTrack seems to allow you to have a different username than the login name. Please make sure, that in your YouTrack profile, the login name is the same as your actual username*
* **You selected the wrong connector version** - *Jetbrains is constantly working on their product. To adapt to the changes, you need to specify the matching connector version in the settings dialog. Please check, if you have selected an outdated version in the settings*
* **You entered a wrong work date field in the settings** - *There is a bug in YouTrack version 2018.2 which requires you the specify the field name for the work date query. Please check out [the Wiki](https://github.com/pbauerochse/youtrack-worklog-viewer/wiki/Work-Date-Field-Help) for help.*
* **There simply are no tracked work items in the specified time range**

**I found a bug / have a question / have a feature request**

Please feel free to file an issue here at the Github project and I'll see what I can do.