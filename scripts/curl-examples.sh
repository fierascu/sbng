#!/usr/bin/env bash
# Example curl calls against the sbng API.
#
# /api2/services/** is exempt from both auth and CSRF (see SecurityConfig), so its
# POST needs neither credentials nor a token. /api/tasks requires both: Basic auth
# (user "q", password "q"), and a CSRF token obtained from a prior GET. /api/todos
# requires OAuth2 login via Keycloak instead, so it isn't something curl can drive.

# basic usage:
# curl http://localhost:8080/api2/services/
# curl -X POST -d '' http://localhost:8080/api2/services/service1


set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
COOKIE_JAR="$(mktemp)"
trap 'rm -f "$COOKIE_JAR"' EXIT

fetch_xsrf_token() {
  # $1: extra curl args (e.g. -u q:q) for the GET used to seed the cookie
  curl -s -c "$COOKIE_JAR" "$@" -o /dev/null
  grep XSRF-TOKEN "$COOKIE_JAR" | awk '{print $7}'
}

echo "== Without credentials: GET /api2/services =="
curl -i "$BASE_URL/api2/services"
echo

echo "== Without credentials: POST /api2/services/service1 =="
curl -i -X POST -d '' "$BASE_URL/api2/services/service1"
echo

echo "== With credentials: GET /api/tasks =="
curl -i -u q:q "$BASE_URL/api/tasks"
echo

echo "== With credentials: POST /api/tasks/task1 =="
XSRF=$(fetch_xsrf_token -u q:q "$BASE_URL/api/tasks")
curl -i -X POST -u q:q -b "$COOKIE_JAR" -H "X-XSRF-TOKEN: $XSRF" "$BASE_URL/api/tasks/task1"
echo
