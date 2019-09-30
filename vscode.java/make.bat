@echo off

set curdir=%cd%
cd "C:\Users\Michael\source\repos\saros-vs-code\vscode.java\bin"

rem Create jar...
"%JAVA_HOME%"\bin\jar cvfe saros.jar app.App app\*.class

rem Move jar...
move /Y saros.jar ..\..\vscode\out\saros.jar  
 
cd %curdir%