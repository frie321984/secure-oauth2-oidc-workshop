@echo off

setlocal

rem replace this with your own installation directory of keycloak
set KEYCLOAK_HOME=C:\keycloak-24.0.4

%KEYCLOAK_HOME%\bin\kc.bat start-dev -Dkeycloak.migration.action=import -Dkeycloak.migration.realmName=workshop -Dkeycloak.migration.provider=singleFile -Dkeycloak.migration.file=keycloak_realm_workshop.json -Dkeycloak.migration.strategy=OVERWRITE_EXISTING -Dkeycloak.profile=preview
