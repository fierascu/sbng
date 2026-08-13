#!/usr/bin/env bash
# Example curl calls against the sbng API.
#
# /services/** is exempt from both auth and CSRF (see SecurityConfig), so its
# POST needs neither credentials nor a token. /api/todos still requires both:
# Basic auth, and a CSRF token obtained from a prior GET.

# basic usage:
# curl http://localhost:8080/services/
# curl -X POST -d '' http://localhost:8080/services/service1


set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
COOKIE_JAR="$(mktemp)"
trap 'rm -f "$COOKIE_JAR"' EXIT

fetch_xsrf_token() {
  # $1: extra curl args (e.g. -u q:q) for the GET used to seed the cookie
  curl -s -c "$COOKIE_JAR" "$@" -o /dev/null
  grep XSRF-TOKEN "$COOKIE_JAR" | awk '{print $7}'
}

echo "== Without credentials: GET /services =="
curl -i "$BASE_URL/services"
echo

echo "== Without credentials: POST /services/service1 =="
curl -i -X POST -d '' "$BASE_URL/services/service1"
echo

echo "== With credentials: GET /api/todos =="
curl -i -u q:q "$BASE_URL/api/todos"
echo

echo "== With credentials: POST /api/todos/todo1 =="
XSRF=$(fetch_xsrf_token -u q:q "$BASE_URL/api/todos")
curl -i -X POST -u q:q -b "$COOKIE_JAR" -H "X-XSRF-TOKEN: $XSRF" "$BASE_URL/api/todos/todo1"
echo
