#!/usr/bin/env bash

# DSNA Core Service API examples.
# Requires curl and jq. Run the login command before protected requests.

BASE_URL="${BASE_URL:-http://localhost:8080}"
USERNAME="${USERNAME:-alice}"
PASSWORD="${PASSWORD:-ChangeMe123!}"
EMAIL="${EMAIL:-alice@example.com}"

# Register a user.
curl --request POST "${BASE_URL}/api/auth/register" \
  --header "Content-Type: application/json" \
  --data "{
    \"username\": \"${USERNAME}\",
    \"password\": \"${PASSWORD}\",
    \"email\": \"${EMAIL}\"
  }"

# Log in and capture the JWT.
LOGIN_RESPONSE="$(
  curl --silent --show-error --request POST "${BASE_URL}/api/auth/login" \
    --header "Content-Type: application/json" \
    --data "{
      \"username\": \"${USERNAME}\",
      \"password\": \"${PASSWORD}\"
    }"
)"
TOKEN="$(printf '%s' "${LOGIN_RESPONSE}" | jq --raw-output '.token')"

# Read the authenticated user's profile.
curl --request GET "${BASE_URL}/api/profile" \
  --header "Authorization: Bearer ${TOKEN}"

# Create a profile if the authenticated user does not already have one.
curl --request POST "${BASE_URL}/api/profile" \
  --header "Authorization: Bearer ${TOKEN}" \
  --header "Content-Type: application/json" \
  --data '{
    "firstName": "Alice",
    "lastName": "Example",
    "dateOfBirth": "2000-01-01",
    "bio": "Learning data structures visually."
  }'

# Update the authenticated user's profile.
curl --request PUT "${BASE_URL}/api/profile" \
  --header "Authorization: Bearer ${TOKEN}" \
  --header "Content-Type: application/json" \
  --data '{
    "firstName": "Alice",
    "lastName": "Example",
    "dateOfBirth": "2000-01-01",
    "bio": "Updated profile."
  }'

# Create a saved workspace.
WORKSPACE_RESPONSE="$(
  curl --silent --show-error --request POST "${BASE_URL}/api/v1/workspaces" \
    --header "Authorization: Bearer ${TOKEN}" \
    --header "Content-Type: application/json" \
    --data '{
      "workspaceName": "Merge sort example",
      "dataStructureType": "ARRAY",
      "structureState": {
        "values": [5, 2, 8, 1],
        "currentStep": 0
      }
    }'
)"
WORKSPACE_ID="$(printf '%s' "${WORKSPACE_RESPONSE}" | jq --raw-output '.id')"

# Update an existing workspace by including its ID.
curl --request POST "${BASE_URL}/api/v1/workspaces" \
  --header "Authorization: Bearer ${TOKEN}" \
  --header "Content-Type: application/json" \
  --data "{
    \"id\": ${WORKSPACE_ID},
    \"workspaceName\": \"Merge sort example\",
    \"dataStructureType\": \"ARRAY\",
    \"structureState\": {
      \"values\": [2, 5, 8, 1],
      \"currentStep\": 1
    }
  }"

# List all workspaces owned by the authenticated user.
curl --request GET "${BASE_URL}/api/v1/workspaces" \
  --header "Authorization: Bearer ${TOKEN}"
