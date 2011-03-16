::
:: Saros Connectivity Diagnosis
:: (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2010-2011
:: (c) Bjšrn Kahlert - 2010-2011
::
:: This program is free software; you can redistribute it and/or modify
:: it under the terms of the GNU General Public License as published by
:: the Free Software Foundation; either version 1, or (at your option)
:: any later version.
::
:: This program is distributed in the hope that it will be useful,
:: but WITHOUT ANY WARRANTY; without even the implied warranty of
:: MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
:: GNU General Public License for more details.
::
:: You should have received a copy of the GNU General Public License
:: along with this program; if not, write to the Free Software
:: Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
::

:: Important notes: This script uses netcat (nc.exe) in order to check Saros connectivity.
:: TODO: Determine the users public IP

@echo off

set NSLOOKUP_CMD=nslookup
set PING_CMD=ping
set TRACEROUTE_CMD=tracert
set IFCONFIG_CMD=ipconfig
set NETCAT_CMD=nc.exe
set TELNET_CMD=telnet

set XMPP_SERVER=saros-con.imp.fu-berlin.de
set WWW_SERVER=saros-project.org

set HOUR=%time:~0,2%
set HOUR=%HOUR: =0%
set LOGFILE=%date:~6,4%%date:~3,2%%date:~0,2%T%HOUR%%time:~3,2%%time:~6,2%-saros-connectivity-diagnosis.txt
set MAX_HOPS=32

set VERSION=0.4


goto main

:str_len
  set str=%~2
  set #=%str%
  set length=0
  :loop
  if defined # (
    set #=%#:~1%&set /A length += 1&goto loop
  )
  endlocal && set %~1=%length%
  exit /b 0
goto :EOF

:print_n_times
  setlocal enabledelayedexpansion

  set R=
  set N=

  set /a N=%~2 2>nul

  if not defined N goto print_n_times_break
  if %N% leq 0 goto print_n_times_break

  set R=%~3

  if not defined R goto print_n_times_break

  call :str_len K "%R%"

  set /a L=1
  set /a N=%N%-1

  :print_n_times_continue
    if %N% leq 0 goto print_n_times_break

    if %N% leq %L% (
      set /a M=%N% * %K%
      call :print_n_times_remainder
      set R=%R%!Q!
      goto print_n_times_break
    )

    set /a N=%N% - %L%
    set R=%R%%R%
    set /a L*=2
    goto print_n_times_continue

    :print_n_times_break
      endlocal && set %~1=%R%
    exit /b 0

    :print_n_times_remainder
      set Q=!R:~-%M%!
goto :eof

:log
  if "%~2" equ "\n" (
    echo.
    echo. >> %1
  ) else (
    echo %~2
    echo %~2 >> %1
  )
goto :eof

:Tee      ::Version 1.0.1, http://www.administrator.de/index.php?content=96916
::Parameters:   <LogFileName> <Command with parameters ...> 
::Example:      call :Tee .\MyLogFile.txt echo It is a test.   (print on screen and to logfile) 
  for /F "tokens=1*" %%a in ("%*") do ( ::%%b=Parameterzeile abzüglich %1 
    for /F "tokens=1* delims=:" %%x in ('^(%%b^) 2^>^&1 ^| findstr /n $') do (echo.%%y) >>%1 & echo.%%y 
  ) 
 exit /b 0

:run_cmd
  call :log %1 %2
  
  call :str_len COMMANDLENGTH %2
  call :print_n_times SEPARATOR %COMMANDLENGTH% -
  call :log %1 %SEPARATOR%

  REM call :Tee %1 %~2 ::bug, clips always the first character of each output line
  call %~2
  call %~2 >> %1 2>&1
  
  
  call :log %1 \n
  call :log %1 \n
goto :eof



:print_banner
  echo Running Saros Connectivity Diagnosis...
  echo Please be patient. The diagnosis can take up to 10 minutes.
  echo.
goto :eof

:delay_start
  echo Diagnosis begins in 5 seconds...
  echo.
  @ping -n 5 localhost> nul
goto :eof

:create_logfile
  echo Saros Connectivity Diagnosis > %1
  echo ---------------------------- >> %1
goto :eof

:logfile_banner
  call :log %1 "Version: %VERSION%"
  call :log %1 "Date: %date:~6,4%-%date:~3,2%-%date:~0,2%"
  call :log %1 "Time: %HOUR%:%time:~3,2%:%time:~6,2%"
  call :log %1 \n
goto :eof

:log_public_ip
  if [[ -x $CURL_CMD ]]; then
    log "Public IP: $($CURL_CMD -s http://whatismyip.org/)"
  elif [[ -x $WGET_CMD ]]; then
    log "Public IP: $($WGET_CMD --quiet -O - http://whatismyip.org/)"
  fi
  call :log \n
goto :eof

:log_network_setup
  call :run_cmd %1 "%IFCONFIG_CMD% /all"
goto :eof

:log_dnslookups
  call :run_cmd %1 "%NSLOOKUP_CMD% %WWW_SERVER%"
  call :run_cmd %1 "%NSLOOKUP_CMD% -type=SRV _xmpp-server._tcp.%XMPP_SERVER%"
goto :eof

:log_icmp_tests
  call :run_cmd %1 "%PING_CMD% -w 3600 -n 3 %WWW_SERVER%"
  call :run_cmd %1 "%PING_CMD% -w 3600 -n 3 %XMPP_SERVER%"
  call :run_cmd %1 "%TRACEROUTE_CMD% -w 1000 -h %MAX_HOPS% %WWW_SERVER%"
  call :run_cmd %1 "%TRACEROUTE_CMD% -w 1000 -h %MAX_HOPS% %XMPP_SERVER%"
goto :eof

:log_tcp_tests
  if exist %NETCAT_CMD% (
    call :run_cmd %1 "%NETCAT_CMD% -vz %XMPP_SERVER% 5222"
    call :run_cmd %1 "%NETCAT_CMD% -vz %XMPP_SERVER% 5269"
  ) else (
    call :log %1 "No nc.exe found. Can not check ports 5222 and 5269 on %XMPP_SERVER%."
  )
goto :eof

:main
  cls
  call :print_banner %LOGFILE%
  call :delay_start %LOGFILE%
  call :create_logfile %LOGFILE%
  call :logfile_banner %LOGFILE%
  REM call :log_public_ip %LOGFILE%
  call :log_network_setup %LOGFILE%
  call :log_dnslookups %LOGFILE%
  call :log_icmp_tests %LOGFILE%
  call :log_tcp_tests %LOGFILE%
  echo.
  echo Results saved in: %LOGFILE%
  goto :eof