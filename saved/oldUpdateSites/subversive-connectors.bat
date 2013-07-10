set ECLIPSEDIR=C:\eclipse\eclipse-installations\eclipse-jee-helios-SR2-win32-x86_64
set ECLIPSE=%ECLIPSEDIR%/eclipsec.exe

set BUILD_DIR=%~dp0

%ECLIPSE% -nosplash ^
-application org.eclipse.ant.core.antRunner ^
-Declipse.p2.mirrors=false ^
-Dbuild.dir=%BUILD_DIR% ^
-buildfile subversive-connectors-update-site.xml