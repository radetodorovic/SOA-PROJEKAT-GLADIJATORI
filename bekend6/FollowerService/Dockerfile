FROM golang:1.22-alpine AS build

WORKDIR /src

COPY go.mod ./
COPY . .
RUN go mod tidy
RUN CGO_ENABLED=0 GOOS=linux go build -o /app/follower-service .

FROM alpine:3.20

WORKDIR /app
COPY --from=build /app/follower-service .

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["./follower-service"]
