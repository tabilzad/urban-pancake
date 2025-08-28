#!/bin/bash

# Read the test JSON receipt
JSON_RECEIPT=$(cat test-receipt.json | jq -c .)

# Create the print payload using jq for proper JSON handling
JSON_PAYLOAD=$(jq -n \
  --arg team_id "urban-pancake" \
  --argjson jsonData "$JSON_RECEIPT" \
  --arg round "0" \
  '{team_id: $team_id, jsonData: $jsonData, round: $round}')

echo "Printing receipt with team: urban-pancake"
echo "JSON payload size: $(echo "$JSON_PAYLOAD" | wc -c) bytes"

# Submit print request to local Next.js API proxy
curl -X POST http://localhost:3000/api/print \
  -H "Content-Type: application/json" \
  -d "$JSON_PAYLOAD" \
  -w "\n\nHTTP Status: %{http_code}\n" \
  -v

echo "Print request complete!"