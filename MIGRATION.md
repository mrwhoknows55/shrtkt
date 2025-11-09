# Migration Guide

This guide helps you migrate between major versions of the Shrtkt API.

## Table of Contents

- [Migrating to v2.0.0 - Authentication Using API-Key](#migrating-to-v200---authentication-using-api-key)
- [Migrating to v1.3.0](#migrating-to-v130)
- [Migrating to v1.2.0](#migrating-to-v120)

---

## Migrating to v2.0.0 - Authentication Using API-Key

### Overview
Version 2.0.0 introduces mandatory authentication for all API endpoints (except `/status` and `/`). This is a **breaking change** that requires updates to your existing integrations.

### Step-by-Step Migration

#### 1. Get Your API Key

To get your API key, write to: **avdhutt2@gmail.com**

Alternatively, create a user account programmatically:

```bash
POST /user
Content-Type: application/json

{
  "name": "Your Name",
  "email": "your@email.com",
  "tier": "hobby"
}
```

Response includes your `apiKey`:
```json
{
  "apiKey": "your-api-key-here",
  "name": "Your Name",
  "email": "your@email.com"
}
```

**Important**: Save your API key securely. You'll need it for all API requests.

#### 2. Update Your API Calls
Add the `x-api-key` header to all requests:

**Before (v1.x):**
```bash
POST /shorten
Content-Type: application/json

{
  "url": "https://example.com"
}
```

**After (v2.0.0):**
```bash
POST /shorten
Content-Type: application/json
x-api-key: your-api-key-here

{
  "url": "https://example.com"
}
```

#### 3. Handle Authentication Errors
Update your error handling to catch authentication failures:

- **401 Unauthorized**: Invalid or missing API key
- **403 Forbidden**: API key is blacklisted

Example error response:
```json
{
  "error": "Invalid API Key"
}
```

#### 4. Update All Endpoints
The following endpoints now require authentication:
- `POST /shorten`
- `PUT /shorten/:shortCode`
- `DELETE /shorten/:code`
- `POST /bulk/shorten`
- `GET /urls`
- `GET /stats/top-urls`

#### 5. Test Your Integration
1. Verify your API key works with a simple request
2. Test error handling with an invalid API key
3. Confirm all your existing workflows function correctly

### Quick Checklist
- [ ] Create user account and save API key
- [ ] Add `x-api-key` header to all API calls
- [ ] Update error handling for 401/403 responses
- [ ] Test all endpoints with authentication
- [ ] Update documentation for your team

### Need Help?
- **Get an API Key**: Contact avdhutt2@gmail.com
- Check if your API key is blacklisted by testing with `POST /shorten`
- Verify your headers are correctly formatted
- Ensure the API key is not expired or revoked

---

## Migrating to v1.3.0

### API Key Setup
To get your API key, contact: **avdhutt2@gmail.com**

Or create user accounts programmatically using `POST /user` and save your API key for future requests.

### Request Format Changes
Update `/shorten` requests to include new optional fields:

```json
{
  "url": "https://example.com",
  "shortCode": "optional-custom-code",
  "expiredAt": "2025-12-31T23:59:59",
  "password": "optional-password"
}
```

### Headers
Add `x-api-key` header to protected endpoints

---

## Migrating to v1.2.0

### Data Migration
URLs created before this version may need to be recreated

### Persistence
URLs are now permanent until explicitly deleted

### New Endpoints
Take advantage of new update and delete functionality:
- `PUT /shorten/:shortCode` - Update existing URLs
- `DELETE /shorten/:code` - Delete URLs

