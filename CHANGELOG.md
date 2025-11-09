# Changelog

All notable changes to the Shrtkt API will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Table of Contents

### Versions
- [2.0.0](#200---2025-10-22) - Authentication & Security
- [1.4.0](#140---2025-09-30) - Monitoring Improvements
- [1.3.0](#130---2025-09-09) - User Management & Advanced Features
- [1.2.0](#120---2025-08-24) - Analytics & Persistence
- [1.1.0](#110---2025-08-12) - Database Integration
- [1.0.0](#100---2025-07-26) - Production Ready
- [0.1.0](#010---2025-01-12) - Initial Release

### Reference Sections
- [API Endpoints Reference](#api-endpoints-reference)
- [Breaking Changes Timeline](#breaking-changes-timeline)

---

## [2.0.0] - 2025-10-22

### Added
- **Authentication Middleware**: All API endpoints now require proper authentication
- **API Key Blacklist**: Ability to blacklist compromised API keys
- **Enhanced Monitoring**: Server-Timing headers and detailed request logging
- **Security Improvements**: Additional security layers for API protection

### Breaking Changes
- **Authentication Required**: All endpoints now require valid API keys (except `/status` and `/`)
- **Blacklisted Keys**: Previously valid API keys may now be blacklisted and rejected

### Migration Guide
**[See detailed migration guide](MIGRATION.md#migrating-to-v200---authentication-using-api-key)**

Quick steps:
- **Get API Key**: Contact avdhutt2@gmail.com or use `POST /user`
- **Add Authentication**: Add `x-api-key` header to all requests
- **Handle Errors**: Update error handling for 401/403 responses
- **Test**: Verify all API calls work with authentication

## [1.4.0] - 2025-09-30

### Added
- **Call Logging**: Enhanced request tracking and logging capabilities
- **Performance Monitoring**: Improved monitoring of API performance

### Changed
- **Infrastructure**: Updated deployment configuration

## [1.3.0] - 2025-09-09

### Added
- **Health Check Endpoint**: `GET /status` for service health monitoring
- **User Management**: Complete user account management system
  - `POST /user` - Create user accounts
  - `GET /urls` - Retrieve user's shortened URLs
- **Advanced URL Features**:
  - Password-protected short URLs
  - URL expiration dates
  - Custom short codes
  - Bulk URL creation (`POST /bulk/shorten`) for enterprise users
- **Analytics**: Top URLs statistics (`GET /stats/top-urls`)
- **User Tiers**: Different access levels (hobby vs enterprise)

### Fixed
- **Enterprise Access**: Fixed tier validation for enterprise users

### Changed
- **Request Format**: Updated request body format for `/shorten` endpoint to support new features

### Migration Guide
**[See detailed migration guide](MIGRATION.md#migrating-to-v130)**

Quick steps:
- **Get API Key**: Contact avdhutt2@gmail.com
- **Update Requests**: Add optional fields to `/shorten` endpoint
- **Add Headers**: Include `x-api-key` header for protected endpoints

## [1.2.0] - 2025-08-24

### Added
- **URL Analytics**: Visit count tracking for redirects
- **URL Management**: 
  - `PUT /shorten/:shortCode` - Update existing short URLs
  - `DELETE /shorten/:code` - Delete short URLs
- **Smart URL Handling**: Duplicate URLs now return the same short code
- **Database Persistence**: URLs are now permanently stored

### Changed
- **Storage**: Moved from in-memory to persistent database storage

### Migration Guide
**[See detailed migration guide](MIGRATION.md#migrating-to-v120)**

Quick steps:
- **Data Migration**: URLs created before this version may need to be recreated
- **Persistence**: URLs are now permanent until explicitly deleted
- **New Features**: Use new update and delete endpoints

## [1.1.0] - 2025-08-12

### Added
- **Complete CRUD Operations**: Full create, read, update, delete functionality
- **Database Integration**: PostgreSQL database for reliable data storage
- **Comprehensive Testing**: Full test coverage for all endpoints
- **Docker Support**: Easy deployment with Docker Compose

### Changed
- **Reliability**: Improved service reliability with database persistence

## [1.0.0] - 2025-07-26

### Added
- **Docker Deployment**: Easy containerized deployment
- **Performance Testing**: Comprehensive performance benchmarks
- **Documentation**: Complete API documentation and setup guides

## [0.1.0] - 2025-01-12

### Added
- **Basic URL Shortening**: Core functionality for creating and accessing short URLs
  - `POST /shorten` - Create short URLs
  - `GET /redirect` - Redirect to original URLs
  - `GET /` - Service root endpoint

---

## API Endpoints Reference

### Core Endpoints
- `GET /` - Service information
- `GET /status` - Health check
- `POST /shorten` - Create short URL
- `GET /redirect` - Redirect to original URL
- `PUT /shorten/:shortCode` - Update short URL
- `DELETE /shorten/:code` - Delete short URL

### User Management
- `POST /user` - Create user account
- `GET /urls` - Get user's URLs

### Advanced Features
- `POST /bulk/shorten` - Bulk URL creation (Enterprise only)
- `GET /stats/top-urls` - Top URLs statistics

### Authentication
All endpoints (except `/`, `/status`, and `/redirect`) require:
- `x-api-key` header with valid API key
- Valid user account for most operations

---

## Breaking Changes Timeline

### v2.0.0 (2025-10-22)
- **Authentication Required**: All endpoints now require API keys
- **Blacklist Support**: API keys can be blacklisted

### v1.3.0 (2025-09-09)
- **Request Format**: Changed `/shorten` request body format
- **API Key Requirement**: Most endpoints now require API keys

### v1.2.0 (2025-08-24)
- **Storage Change**: Moved from in-memory to database storage
- **URL Persistence**: URLs are now permanently stored

