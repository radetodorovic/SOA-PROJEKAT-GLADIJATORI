# Repository Guidelines

## Project Structure & Module Organization
This repository contains an Angular frontend in `frontend7/` and ASP.NET Core services in `bekend6/`. Frontend source lives under `frontend7/src/app/`, organized by `pages/`, `services/`, and `models/`; global assets and styles are in `frontend7/src/`. Backend code is split into `bekend6/StakeholdersService/` and `bekend6/BlogService/`, each following the same layout: `Controllers/`, `Services/`, `Repositories/`, `DTOs/`, `Models/`, `Data/`, and `Properties/`. Keep new features inside the matching service instead of creating shared cross-service folders prematurely.

## Build, Test, and Development Commands
Frontend:
- `cd frontend7 && npm install` installs Angular dependencies.
- `cd frontend7 && npm start` runs the UI at `http://localhost:4200`.
- `cd frontend7 && npm run build` creates a production build in `frontend7/dist/`.
- `cd frontend7 && npm test` runs Jasmine/Karma unit tests.

Backend:
- `dotnet build bekend6/bekend6.sln` builds both services.
- `dotnet run --project bekend6/StakeholdersService` starts the user/auth API.
- `dotnet run --project bekend6/BlogService` starts the blog API.

## Coding Style & Naming Conventions
Use 2-space indentation in Angular `.ts/.html/.css` files and 4-space indentation in C# files. Follow existing naming: Angular files use kebab-case (`my-profile.component.ts`), Angular classes use PascalCase, and services/controllers/interfaces in C# use PascalCase with `I` prefixes for interfaces (`IUserService`). Keep controllers thin and move business logic into `Services/`. No dedicated lint configuration is committed, so match surrounding style closely before introducing new patterns.

## Testing Guidelines
Frontend tests live next to components as `*.spec.ts`; keep that convention for new unit tests. Run `npm test` before submitting frontend changes. There are currently no backend test projects in `bekend6/`, so at minimum verify changes with `dotnet build` and targeted manual API checks using the `.http` files in each service folder.

## Commit & Pull Request Guidelines
Recent history uses short, task-focused commits and feature branches such as `feat/Docker`; keep commit subjects concise and imperative. In pull requests, include: what changed, which service or page is affected, how you verified it, and screenshots for UI work. Link the related issue or task when available, and avoid mixing frontend and backend refactors in one PR unless the feature requires both.

## Configuration & Security Tips
Do not commit `frontend7/src/environments/environment.ts`, database secrets, or local `.user` files. Keep connection strings in local `appsettings.Development.json` overrides, and confirm CORS changes in backend `Program.cs` still allow only intended frontend origins.
