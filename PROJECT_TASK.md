# PROJECT_TASK.md

# Antivirus Signature Server — Project Requirements

## Project Goal

Implement a secure Spring Boot backend for antivirus licensing and malware signature management.

The project must support:

* JWT authentication and authorization
* HTTPS encryption
* PostgreSQL database
* Electronic Digital Signature (EDS)
* Malware signatures CRUD subsystem
* Binary API for signature synchronization
* Audit and history logging
* Optional MinIO integration

---

# Tech Stack

Required stack:

* Java 17+
* Spring Boot
* Spring Security
* JWT access/refresh tokens
* PostgreSQL
* JPA/Hibernate
* Maven or Gradle
* HTTPS
* Docker (optional)
* MinIO (optional)

---

# Existing Requirements

The project already contains:

* authentication
* authorization
* PostgreSQL connection
* HTTPS support
* CI/CD pipeline

These modules may be migrated from previous RBPO project or from example repository.

---

# Security Requirements

## Authentication

Use JWT authentication:

* access token
* refresh token

Access token:

* short lifetime

Refresh token:

* long lifetime

---

## Authorization

Roles:

### ADMIN

Can:

* create signatures
* update signatures
* delete signatures
* get history
* get audit
* full access

### USER

Can:

* get full database
* get increment
* get signatures by ids

---

## HTTPS

Application MUST work over HTTPS.

Keystore configuration must be externalized.

---

# Database

Use PostgreSQL.

Use migrations if possible:

* Flyway
* Liquibase

---

# Digital Signature Module (EDS)

## Goal

Implement digital signing subsystem.

The module must:

* load private key from keystore
* expose public certificate
* canonicalize JSON
* sign canonical UTF-8 bytes
* return Base64 signature

---

## Required Algorithm

Use:

SHA256withRSA

---

## Required Features

### KeyStore

Support:

* JKS
* configurable path
* configurable passwords
* configurable alias

---

## JSON Canonicalization

Canonicalization must be deterministic.

Preferred:

* RFC 8785 (JCS)

Rules:

* stable field ordering
* UTF-8 encoding
* no formatting spaces
* deterministic serialization

---

## Required Services

### KeyProvider

Responsibilities:

* load private key
* load public key
* cache keys
* thread safe

### CanonicalizationService

Responsibilities:

* canonical JSON generation
* deterministic UTF-8 bytes

### SigningService

Responsibilities:

* sign payload objects
* sign raw byte arrays
* return Base64 signature

---

## Additional Requirement

SigningService must support:

```java
byte[] sign(byte[] payload)
```

This method will be used for manifest signing in binary API.

---

# License Subsystem

Implement license management subsystem.

Main entities:

* users
* product
* license_type
* license
* device
* device_license
* license_history

---

## Required Operations

### Create License

ADMIN only.

Must:

* validate product
* validate license type
* validate owner
* generate activation code
* create history record

---

### Activate License

Must:

* bind license to user
* bind license to device
* check device limit
* create history record
* generate ticket

---

### Renew License

Must:

* validate renew conditions
* extend ending_date
* create history record

---

### Check License

Must:

* validate device
* validate active license
* validate expiration
* validate blocked state

---

# Malware Signature Subsystem

Implement malware signatures storage and synchronization subsystem.

---

# Main Entity

## MalwareSignature

Fields:

* id
* threatName
* firstBytesHex
* remainderHashHex
* remainderLength
* fileType
* offsetStart
* offsetEnd
* updatedAt
* status
* digitalSignatureBase64

---

# Signature Statuses

## ACTUAL

Active signature.

## DELETED

Logical deletion.

Deleted records:

* MUST remain in database
* MUST appear in increment responses
* MUST NOT appear in full database responses

---

# History Table

## signatures_history

Must contain:

* previous versions before update/delete
* complete previous state

History MUST be written:

* on update
* on delete

---

# Audit Table

## signatures_audit

Must contain:

* who changed
* when changed
* fields changed
* description

Audit MUST be written:

* on create
* on update
* on delete

---

# Validation Rules

Required validations:

* threatName not blank
* firstBytesHex not blank
* remainderHashHex not blank
* fileType not blank
* remainderLength >= 0
* offsetStart >= 0
* offsetEnd >= offsetStart
* hex fields must contain valid hex characters

Invalid input:

* return 400 Bad Request

---

# Signature Signing Rules

The following fields MUST participate in EDS signing:

* threatName
* firstBytesHex
* remainderHashHex
* remainderLength
* fileType
* offsetStart
* offsetEnd
* status

The following fields MUST NOT participate:

* updatedAt
* digitalSignatureBase64

---

# Required REST Operations

## 1. Full Database

GET /api/signatures/full

Requirements:

* return only ACTUAL
* exclude DELETED

---

## 2. Increment

GET /api/signatures/increment?since=...

Requirements:

* return updatedAt > since
* include ACTUAL and DELETED

---

## 3. Get By IDs

POST /api/signatures/by-ids

Body:

* list of UUIDs

---

## 4. Create Signature

ADMIN only.

Must:

* validate input
* generate signature
* write audit
* set status ACTUAL
* set updatedAt

---

## 5. Update Signature

ADMIN only.

Must:

* save previous state to history
* update fields
* regenerate digital signature
* update updatedAt
* write audit
* store changed fields

---

## 6. Delete Signature

ADMIN only.

Must:

* logical delete only
* save previous state to history
* set status DELETED
* update updatedAt
* write audit

---

## 7. Get History

ADMIN only.

Return:

* history records by signatureId

---

## 8. Get Audit

ADMIN only.

Return:

* audit records by signatureId

---

# Binary API

Implement separate binary synchronization API.

Base path:

/api/binary/signatures

---

# Required Endpoints

## Full Export

GET /api/binary/signatures/full

Return:

* multipart/mixed
* manifest.bin
* data.bin

Only ACTUAL records.

---

## Increment Export

GET /api/binary/signatures/increment?since=...

Return:

* multipart/mixed
* manifest.bin
* data.bin

Include:

* ACTUAL
* DELETED

---

## Export By IDs

POST /api/binary/signatures/by-ids

Return:

* multipart/mixed
* manifest.bin
* data.bin

---

# Multipart Requirements

Each response must contain:

1. manifest.bin
2. data.bin

Content-Type:

* application/octet-stream

Main response:

* multipart/mixed

---

# Manifest Structure

Manifest contains:

## Header

* magic
* version
* exportType
* generatedAtEpochMillis
* sinceEpochMillis
* recordCount
* dataSha256

## Entries

Each entry contains:

* id
* statusCode
* updatedAtEpochMillis
* dataOffset
* dataLength
* recordSignatureLength
* recordSignatureBytes

## Manifest Signature

Manifest must be signed.

Must contain:

* manifestSignatureLength
* manifestSignatureBytes

---

# Data.bin Structure

## Header

* magic
* version
* recordCount

## Records

Each record contains:

* threatName
* firstBytes
* remainderHash
* remainderLength
* fileType
* offsetStart
* offsetEnd

---

# Binary Format Rules

Choose one byte order:

* BigEndian preferred

Use same byte order everywhere.

---

# Multipart Rules

Stable order:

1. manifest.bin
2. data.bin

Use:

* LinkedMultiValueMap

---

# CI/CD

Pipeline must contain:

* test
* build

Optional:

* docker build

---

# Optional MinIO Module

## Features

* upload signature files
* generate pre-signed URLs
* private buckets
* separate access keys

---

# Suggested Architecture

Recommended packages:

```text
config/
security/
auth/
license/
signature/
binary/
audit/
history/
repository/
service/
controller/
dto/
entity/
mapper/
```

---

# Recommended Development Order

1. Security + JWT
2. PostgreSQL
3. EDS module
4. Malware signatures CRUD
5. History + Audit
6. Binary API
7. MinIO

---

# Important Business Rules

* delete is logical
* update/delete save history
* create/update/delete save audit
* create/update recalculate EDS
* full export excludes DELETED
* increment includes DELETED
* manifest must be signed
* record signatures must already exist before export
