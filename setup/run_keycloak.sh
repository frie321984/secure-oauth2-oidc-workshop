#!/bin/sh

# replace this with your own installation directory of keycloak
export KEYCLOAK_HOME=/home/afa/Development/keycloak-24.0.4

$KEYCLOAK_HOME/bin/kc.sh start-dev -Dkeycloak.profile=preview

