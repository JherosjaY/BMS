@echo off
echo ========================================
echo   GETTING SHA-1 CERTIFICATE
echo ========================================
echo.
echo Running Gradle signingReport...
echo.

cd /d "%~dp0"
call gradlew.bat signingReport

echo.
echo ========================================
echo   DONE! Look for SHA1 in the output above
echo ========================================
echo.
pause
