#!/bin/bash

# Read the Kotlin code from challenge.kt and properly escape it for JSON
KOTLIN_CODE=$(cat challenge.kt | jq -Rs .)

# Create the JSON payload using jq to ensure proper escaping
JSON_PAYLOAD=$(jq -n \
  --arg team_id "urban-pancake" \
  --arg teamName "urban-pancake" \
  --argjson interpreterCode "$KOTLIN_CODE" \
  '{team_id: $team_id, teamName: $teamName, interpreterCode: $interpreterCode}')

# Submit to local Next.js API proxy (which forwards to the real server)
curl -X POST http://localhost:3000/api/submit \
  -H "Content-Type: application/json" \
  -d "$JSON_PAYLOAD" \
  -w "\n\nHTTP Status: %{http_code}\n" \
  -v

echo "Submission complete!"