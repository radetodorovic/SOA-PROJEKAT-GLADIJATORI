# Profile Migration Plan

Ako je baza vec kreirana bez novih profile kolona, primeni EF Core migraciju:

1. `cd bekend6/StakeholdersService`
2. `dotnet tool restore` (ako koristis lokalni `dotnet-ef` alat)
3. `dotnet ef migrations add AddUserProfileFields`
4. `dotnet ef database update`

Ako migracije nisu moguce u okruzenju:

- obrisati development bazu i ponovo pokrenuti API (`EnsureCreated` ce napraviti tabelu sa novim kolonama), ili
- manualno dodati kolone u `Users` tabelu:
  - `FirstName` (varchar(100), nullable)
  - `LastName` (varchar(100), nullable)
  - `ProfileImage` (text, nullable)
  - `Biography` (text, nullable)
  - `Motto` (varchar(500), nullable)
  - `IsProfileInitialized` (boolean, not null, default false)
