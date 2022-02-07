if exist build rd /q /s build
call gradlew eclipse --no-daemon
call gradlew genEclipseRuns --no-daemon
for %%I in (.) do set PROJNAME=%%~nxI
del %PROJNAME%-Client.launch
del %PROJNAME%-Server.launch
del %PROJNAME%-Data.launch
ren runClient.launch %PROJNAME%-Client.launch
ren runServer.launch %PROJNAME%-Server.launch
ren runData.launch %PROJNAME%-Data.launch

For /F "tokens=1* delims==" %%A IN (gradle.properties) DO (
    IF "%%A"=="modid" set modid=%%B
)

cd src\main\resources
ren modid.mixins.json %modid%.mixins.json
cd data
ren modid %modid%
cd ../assets
ren modid %modid%
cd ../../../..
call gradlew processResources --no-daemon