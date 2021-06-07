@ECHO OFF
REM CHANGE THESE
SET app_package=com.example.fadi.networkinfo
SET dir_app_name=NetworkInfo
SET MAIN_ACTIVITY=MainActivity

SET ADB_SH=adb shell su -c


SET path_sysapp=/system/priv-app
SET apk_host=/home/tom/git/NetworkInfoAPI24V2/app/build/outputs/apk/app-debug.apk
SET apk_name=%dir_app_name%.apk
SET apk_target_dir=%path_sysapp%/%dir_app_name%
SET apk_target_sys=%apk_target_dir%/%apk_name% 

adb rm -f %apk_host%



%ADB_SH% mount -o rw,remount /system
%ADB_SH% chmod 777 /system/lib/
%ADB_SH% mkdir -p /sdcard/tmp
%ADB_SH% mkdir -p %apk_target_dir%
adb push %apk_host% /sdcard/tmp/%apk_name%
%ADB_SH% mv /sdcard/tmp/%apk_name% %apk_target_sys%
%ADB_SH% rmdir /sdcard/tmp

%ADB_SH% chmod 755 %apk_target_dir%
%ADB_SH% chmod 644 %apk_target_sys%

%ADB_SH% mount -o remount,ro /

%ADB_SH% am force-stop %app_package%

::%ADB_SH% am start -n \%app_package%/%app_package%.%MAIN_ACTIVITY%\ -a android.intent.action.MAIN -c android.intent.category.LAUNCHER
         


