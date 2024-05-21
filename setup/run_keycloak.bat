@echo off

setlocal

rem replace this with your own installation directory of keycloak
set KEYCLOAK_HOME=C:\keycloak-24.0.4

%KEYCLOAK_HOME%\bin\kc.bat start-dev -Dkeycloak.profile=preview
