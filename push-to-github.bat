@echo off
REM 🚀 BMS WebSocket Phase 1 - Push to GitHub
REM Just double-click this file and it will push everything!

color 0B
cls

echo.
echo ========================================
echo   🚀 BMS WebSocket Phase 1
echo   Pushing to GitHub...
echo ========================================
echo.

REM Step 1: Add all changes
echo 📦 Step 1: Adding all files...
git add .
echo ✅ Files added!
echo.

REM Step 2: Commit with message
echo 💾 Step 2: Creating commit...
git commit -m "Feat: Add WebSocket real-time synchronization - Phase 1 Complete"
echo ✅ Commit created!
echo.

REM Step 3: Push to GitHub
echo 🌐 Step 3: Pushing to GitHub...
git push origin main

echo.
echo ========================================
echo   ✅ PUSH COMPLETE!
echo ========================================
echo.
echo 📊 What happens next:
echo    1. Render detects changes (2-3 min)
echo    2. Render rebuilds backend
echo    3. WebSocket goes LIVE
echo    4. Check: https://bms-1op6.onrender.com/ws/status
echo.
echo 🎯 Next Phase: Android WebSocket Client
echo.
echo ✨ All done! Check your GitHub repo and Render dashboard!
echo.
pause
