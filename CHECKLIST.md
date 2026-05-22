# Checklist

## Commands

1. Start PostgreSQL through Docker if Docker is installed:
```powershell
docker compose up -d postgres pgadmin
docker compose config
```
2. If Docker is not available, prepare a local PostgreSQL instance with:
```text
host=localhost
port=5432
database=autoservice_db
username=autoservice
password=autoservice123
```
3. Set EDS settings for local startup:
```powershell
$env:APP_EDS_KEY_STORE_PATH="file:./src/test/resources/eds/test-keystore.p12"
$env:APP_EDS_KEY_STORE_TYPE="PKCS12"
$env:APP_EDS_KEY_STORE_PASSWORD="changeit"
$env:APP_EDS_KEY_ALIAS="antivirus-test"
$env:APP_EDS_KEY_PASSWORD="changeit"
```
4. Run the application:
```powershell
.\mvnw.cmd spring-boot:run
```
5. Run tests:
```powershell
.\mvnw.cmd test
```

## Role Setup

1. Run `Register Admin Candidate` from `postman_collection.json`.
2. Grant the `ADMIN` role in PostgreSQL:
```sql
insert into auth_user_roles(user_id, role_id)
select u.id, r.id
from auth_users u
join auth_roles r on r.name = 'ADMIN'
where u.username = 'antivirus-admin'
on conflict do nothing;
```
3. Run `Login Admin` again to get an ADMIN JWT.

## Check Order

1. `Register User`
2. `Register Admin Candidate`
3. Grant `ADMIN` role in DB
4. `Login User`
5. `Refresh User Token`
6. `Login Admin`
7. `Get Certificate`
8. `Sign Test`
9. `Verify Test`
10. `Create Signature`
11. `User Full Signatures`
12. `User Increment Signatures`
13. `User By IDs Signatures`
14. `User Create Forbidden`
15. `User Update Forbidden`
16. `User Delete Forbidden`
17. `User History Forbidden`
18. `User Audit Forbidden`
19. `Update Signature`
20. `Get History`
21. `Get Audit`
22. `Delete Signature`
23. `Full After Delete`
24. `Increment After Delete`
25. `Binary Full`
26. `Binary Increment`
27. `Binary By IDs`
28. `Logout User`

## Expected Results

- Flyway runs on app startup and creates antivirus auth/signature tables if they do not exist yet.
- `USER` gets `200` for `full`, `increment`, `by-ids`, and binary export endpoints.
- `USER` gets `403` for create, update, delete, history, and audit endpoints.
- `ADMIN` gets `200` for EDS endpoints and signature write/history/audit endpoints.
- `Sign Test` returns a Base64 signature.
- `Verify Test` returns `valid=true`.
- `Create Signature` returns `id`, `updatedAt`, and `digitalSignatureBase64`.
- `Get History` is non-empty after update.
- `Get Audit` is non-empty after create/update/delete.
- `Delete Signature` returns the same signature with `status=DELETED`.
- `Full After Delete` must not contain `{{signatureId}}`.
- `Increment After Delete` must contain `{{signatureId}}` with `status=DELETED`.
- Binary endpoints return `Content-Type: multipart/mixed` and contain `manifest.bin` and `data.bin`.

## Notes

- On this machine, `docker` was not available in `PATH` on May 22, 2026, so `docker compose config` could not be executed here.
- A local PostgreSQL listener was present on `127.0.0.1:5432`, but it rejected the project default credentials `autoservice/autoservice123`, so `.\mvnw.cmd spring-boot:run` could not be completed against that existing instance without reconfiguring `SPRING_DATASOURCE_*` or starting the project PostgreSQL container.
