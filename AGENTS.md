# Repository Guidelines

## Project Structure & Module Organization

This repository contains a small service-oriented application. Backend services live in `bekend6/`: `StakeholdersService` and `BlogService` are ASP.NET Core .NET 8 APIs, while `FollowerService` is a Go 1.22 service backed by Neo4j. The Angular 16 frontend lives in `frontend7/`, with code under `frontend7/src/app`, models in `models`, API wrappers in `services`, and pages in `pages`. Local infrastructure is defined in `docker-compose.yml`.

## Build, Test, and Development Commands

- `docker compose up --build`: builds and runs backend services plus PostgreSQL and Neo4j.
- `dotnet build bekend6/bekend6.sln`: builds the .NET backend solution.
- `dotnet run --project bekend6/StakeholdersService/StakeholdersService.csproj`: runs the stakeholders API locally.
- `dotnet run --project bekend6/BlogService/BlogService.csproj`: runs the blog API locally.
- `go run ./bekend6/FollowerService`: runs the Go follower service.
- `cd frontend7 && npm install`: installs Angular dependencies.
- `cd frontend7 && npm start`: starts the Angular development server.
- `cd frontend7 && npm run build`: creates a production frontend build.
- `cd frontend7 && npm test`: runs Angular Karma/Jasmine tests.

## Coding Style & Naming Conventions

Use the existing directory conventions: `Controllers`, `Services`, `Repositories`, `DTOs`, `Models`, and `Data` for .NET services. C# uses nullable reference types and implicit usings; keep public types in PascalCase and private/local variables in camelCase. Angular files follow CLI naming such as `blogs.component.ts`, `auth.service.ts`, and `blog-post.ts`; keep components, services, and models separated. Format Go code with `gofmt` before committing.

## Testing Guidelines

The frontend currently has Jasmine/Karma specs such as `*.component.spec.ts`; add Angular tests beside the component or service being tested. No dedicated .NET or Go test projects are present yet; when adding backend behavior, prefer focused unit or integration tests instead of relying only on `.http` files. Run `npm test` for frontend changes and `dotnet build bekend6/bekend6.sln` for backend compile validation.

## Commit & Pull Request Guidelines

Recent history uses short, descriptive messages such as `docker for blogs` and feature branches like `feat/FollowerService`. Keep commits focused on one change and use imperative or concise descriptive wording. Pull requests should include a short summary, affected services, commands run, linked issue or task reference when available, and screenshots for UI changes.

## Security & Configuration Tips

Do not commit real secrets. Development credentials in `docker-compose.yml` are for local containers only. Keep service URLs, database connection strings, and Neo4j credentials configurable through environment variables or `appsettings.Development.json`.
