# FollowerService

Go microservice for KT2 follow relationships and profile recommendations. It stores users and `FOLLOWS` relationships in Neo4j.

## Configuration

Environment variables:

- `PORT` - HTTP port inside the container, default `8080`.
- `NEO4J_URI` - Neo4j Bolt URI, for Docker Compose use `neo4j://neo4j:7687`.
- `NEO4J_USER` - Neo4j username.
- `NEO4J_PASSWORD` - Neo4j password.

## API

Current user identity is read from the `X-User-Id` header for follow/unfollow operations.

- `POST /api/followers/{targetUserId}` follows a user.
- `DELETE /api/followers/{targetUserId}` unfollows a user.
- `GET /api/followers/following/{userId}` returns users followed by `userId`.
- `GET /api/followers/recommendations/{userId}` returns profiles followed by people that `userId` already follows.
- `GET /api/followers/check?followerId=&targetId=` returns whether `followerId` follows `targetId`.

Optional usernames can be sent through JSON body fields `followerUsername` and `targetUsername`, or headers `X-Username` and `X-Target-Username`. If omitted, the service stores placeholder values such as `user-2`.

## Local Docker Usage

From the repository root:

```powershell
docker compose up --build neo4j follower-service
```

FollowerService is exposed at `http://localhost:5010`. Neo4j Browser is exposed at `http://localhost:7474` with username `neo4j` and password `password`.
