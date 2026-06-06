#!/bin/bash

BASE_URL="http://localhost:8080"
echo "========================================="
echo "Instant Payment Service API Demo"
echo "========================================="

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 1. Health Check
echo -e "\n${BLUE}1. Health Check${NC}"
curl -s -X GET "${BASE_URL}/actuator/health" | jq '.'
echo ""

# 2. Register a new user
echo -e "\n${BLUE}2. Register a new user${NC}"
REGISTER_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+994501234567",
    "email": "test@example.com",
    "password": "Test@123456",
    "fullName": "Test User"
  }')
echo $REGISTER_RESPONSE | jq '.'
USER_ID=$(echo $REGISTER_RESPONSE | jq -r '.userId')
WALLET_ID=$(echo $REGISTER_RESPONSE | jq -r '.walletId')
echo -e "${GREEN}User ID: $USER_ID${NC}"
echo -e "${GREEN}Wallet ID: $WALLET_ID${NC}"

# 3. Login
echo -e "\n${BLUE}3. Login${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+994501234567",
    "password": "Test@123456"
  }')
echo $LOGIN_RESPONSE | jq '.'
ACCESS_TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.accessToken')
REFRESH_TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.refreshToken')
WALLET_ID=$(echo $LOGIN_RESPONSE | jq -r '.wallets[0].walletId')
echo -e "${GREEN}Access Token: ${ACCESS_TOKEN:0:50}...${NC}"
echo -e "${GREEN}Refresh Token: ${REFRESH_TOKEN:0:50}...${NC}"
echo -e "${GREEN}Wallet ID: $WALLET_ID${NC}"

# 4. Get Current User Profile
echo -e "\n${BLUE}4. Get Current User Profile${NC}"
curl -s -X GET "${BASE_URL}/api/v1/auth/me" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" | jq '.'

# 5. Set Wallet PIN
echo -e "\n${BLUE}5. Set Wallet PIN${NC}"
curl -s -X POST "${BASE_URL}/api/v1/wallets/pin/set" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{
    \"walletId\": \"${WALLET_ID}\",
    \"pin\": \"123456\"
  }" | jq '.'

# 6. Verify Wallet PIN
echo -e "\n${BLUE}6. Verify Wallet PIN${NC}"
curl -s -X POST "${BASE_URL}/api/v1/wallets/pin/verify" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{
    \"walletId\": \"${WALLET_ID}\",
    \"pin\": \"123456\"
  }" | jq '.'

# 7. Create a payment/transfer
echo -e "\n${BLUE}7. Send Payment${NC}"
PAYMENT_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/v1/payments/transfer" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{
    \"fromWalletId\": \"${WALLET_ID}\",
    \"toWalletId\": \"WLT_ABCD1234\",
    \"amount\": 50.00,
    \"currency\": \"AZN\",
    \"description\": \"Test payment\",
    \"idempotencyKey\": \"$(uuidgen)\"
  }")
echo $PAYMENT_RESPONSE | jq '.'
TRANSACTION_ID=$(echo $PAYMENT_RESPONSE | jq -r '.transactionId')
echo -e "${GREEN}Transaction ID: $TRANSACTION_ID${NC}"

# 8. Refresh Token
echo -e "\n${BLUE}8. Refresh Access Token${NC}"
curl -s -X POST "${BASE_URL}/api/v1/auth/refresh" \
  -H "Refresh-Token: ${REFRESH_TOKEN}" | jq '.'

# 9. Change Password
echo -e "\n${BLUE}9. Change Password${NC}"
curl -s -X POST "${BASE_URL}/api/v1/auth/change-password" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "currentPassword": "Test@123456",
    "newPassword": "Test@123456"
  }' | jq '.'

# 10. Enable 2FA
echo -e "\n${BLUE}10. Enable Two-Factor Authentication${NC}"
curl -s -X POST "${BASE_URL}/api/v1/auth/2fa/enable" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" | jq '.'

# 11. Logout
echo -e "\n${BLUE}11. Logout${NC}"
curl -s -X POST "${BASE_URL}/api/v1/auth/logout" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" | jq '.'

echo -e "\n${GREEN}=========================================${NC}"
echo -e "${GREEN}Demo completed successfully!${NC}"
echo -e "${GREEN}=========================================${NC}"