@REM Maven Wrapper startup script for Windows
@REM Downloads Maven if not present and runs it.

@echo off
setlocal

set "MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.6"
set "MAVEN_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip"

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    echo Downloading Maven 3.9.6...
    mkdir "%MAVEN_HOME%" 2>nul
    set "TMPZIP=%TEMP%\maven-%RANDOM%.zip"
    curl -sL -o "%TMPZIP%" "%MAVEN_URL%"
    powershell -Command "Expand-Archive -Path '%TMPZIP%' -DestinationPath '%MAVEN_HOME%' -Force"
    REM Move contents from subfolder
    for /d %%D in ("%MAVEN_HOME%\apache-maven-*") do (
        xcopy /s /e /q /y "%%D\*" "%MAVEN_HOME%\" >nul
        rmdir /s /q "%%D"
    )
    del "%TMPZIP%"
    echo Maven installed to %MAVEN_HOME%
)

"%MAVEN_HOME%\bin\mvn.cmd" %*
