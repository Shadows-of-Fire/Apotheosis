call gradlew eclipse --no-daemon
call gradlew eclipse genEclipseRuns --no-daemon
for %%I in (.) do set PROJNAME=%%~nxI
del %PROJNAME%-Client.launch
del %PROJNAME%-Server.launch
ren runClient.launch %PROJNAME%-Client.launch
ren runServer.launch %PROJNAME%-Server.launch