package main

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"log"
	"net/http"
	"os"
	"strconv"
	"strings"
	"time"

	"github.com/neo4j/neo4j-go-driver/v5/neo4j"
)

type app struct {
	driver neo4j.DriverWithContext
}

type followRequest struct {
	FollowerUsername string `json:"followerUsername"`
	TargetUsername   string `json:"targetUsername"`
}

type userResponse struct {
	ID       int64  `json:"id"`
	Username string `json:"username"`
}

type checkResponse struct {
	FollowerID  int64 `json:"followerId"`
	TargetID    int64 `json:"targetId"`
	IsFollowing bool  `json:"isFollowing"`
}

func main() {
	cfg := loadConfig()

	driver, err := neo4j.NewDriverWithContext(
		cfg.neo4jURI,
		neo4j.BasicAuth(cfg.neo4jUser, cfg.neo4jPassword, ""),
	)
	if err != nil {
		log.Fatalf("failed to create Neo4j driver: %v", err)
	}
	defer driver.Close(context.Background())

	ctx, cancel := context.WithTimeout(context.Background(), 15*time.Second)
	defer cancel()

	if err := driver.VerifyConnectivity(ctx); err != nil {
		log.Fatalf("failed to connect to Neo4j: %v", err)
	}

	application := &app{driver: driver}
	if err := application.ensureConstraints(ctx); err != nil {
		log.Fatalf("failed to create Neo4j constraints: %v", err)
	}

	mux := http.NewServeMux()
	mux.HandleFunc("/health", application.handleHealth)
	mux.HandleFunc("/api/followers/check", application.handleCheck)
	mux.HandleFunc("/api/followers/following/", application.handleFollowing)
	mux.HandleFunc("/api/followers/recommendations/", application.handleRecommendations)
	mux.HandleFunc("/api/followers/", application.handleFollow)

	handler := withCORS(mux)

	log.Printf("FollowerService listening on port %s", cfg.port)
	if err := http.ListenAndServe(":"+cfg.port, handler); err != nil {
		log.Fatalf("server stopped: %v", err)
	}
}

type config struct {
	port          string
	neo4jURI      string
	neo4jUser     string
	neo4jPassword string
}

func loadConfig() config {
	return config{
		port:          getEnv("PORT", "8080"),
		neo4jURI:      getEnv("NEO4J_URI", "neo4j://localhost:7687"),
		neo4jUser:     getEnv("NEO4J_USER", "neo4j"),
		neo4jPassword: getEnv("NEO4J_PASSWORD", "password"),
	}
}

func getEnv(key, fallback string) string {
	value := strings.TrimSpace(os.Getenv(key))
	if value == "" {
		return fallback
	}
	return value
}

func withCORS(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Access-Control-Allow-Origin", "*")
		w.Header().Set("Access-Control-Allow-Headers", "Content-Type, X-User-Id, X-Username, X-Target-Username")
		w.Header().Set("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS")

		if r.Method == http.MethodOptions {
			w.WriteHeader(http.StatusNoContent)
			return
		}

		next.ServeHTTP(w, r)
	})
}

func (a *app) ensureConstraints(ctx context.Context) error {
	_, err := neo4j.ExecuteQuery(
		ctx,
		a.driver,
		"CREATE CONSTRAINT user_id_unique IF NOT EXISTS FOR (u:User) REQUIRE u.id IS UNIQUE",
		nil,
		neo4j.EagerResultTransformer,
	)
	return err
}

func (a *app) handleHealth(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		writeError(w, http.StatusMethodNotAllowed, "Method not allowed.")
		return
	}

	writeJSON(w, http.StatusOK, map[string]string{"status": "ok"})
}

func (a *app) handleFollow(w http.ResponseWriter, r *http.Request) {
	targetID, err := parseIDFromPath(r.URL.Path, "/api/followers/")
	if err != nil {
		writeError(w, http.StatusBadRequest, "Target user id must be a positive number.")
		return
	}

	followerID, err := readHeaderID(r, "X-User-Id")
	if err != nil {
		writeError(w, http.StatusUnauthorized, "X-User-Id header is required.")
		return
	}

	if followerID == targetID {
		writeError(w, http.StatusBadRequest, "Users cannot follow themselves.")
		return
	}

	switch r.Method {
	case http.MethodPost:
		a.followUser(w, r, followerID, targetID)
	case http.MethodDelete:
		a.unfollowUser(w, r, followerID, targetID)
	default:
		writeError(w, http.StatusMethodNotAllowed, "Method not allowed.")
	}
}

func (a *app) followUser(w http.ResponseWriter, r *http.Request, followerID, targetID int64) {
	var request followRequest
	_ = json.NewDecoder(r.Body).Decode(&request)

	followerUsername := firstNonEmpty(request.FollowerUsername, r.Header.Get("X-Username"), defaultUsername(followerID))
	targetUsername := firstNonEmpty(request.TargetUsername, r.Header.Get("X-Target-Username"), defaultUsername(targetID))

	ctx, cancel := context.WithTimeout(r.Context(), 5*time.Second)
	defer cancel()

	_, err := neo4j.ExecuteQuery(
		ctx,
		a.driver,
		`
MERGE (follower:User {id: $followerId})
ON CREATE SET follower.username = $followerUsername
SET follower.username = CASE
  WHEN follower.username STARTS WITH 'user-' THEN $followerUsername
  ELSE follower.username
END
MERGE (target:User {id: $targetId})
ON CREATE SET target.username = $targetUsername
SET target.username = CASE
  WHEN target.username STARTS WITH 'user-' THEN $targetUsername
  ELSE target.username
END
MERGE (follower)-[:FOLLOWS]->(target)
RETURN target
`,
		map[string]any{
			"followerId":       followerID,
			"followerUsername": followerUsername,
			"targetId":         targetID,
			"targetUsername":   targetUsername,
		},
		neo4j.EagerResultTransformer,
	)
	if err != nil {
		writeError(w, http.StatusInternalServerError, "Could not create follow relation.")
		return
	}

	writeJSON(w, http.StatusCreated, map[string]any{
		"followerId": followerID,
		"targetId":   targetID,
		"message":    "User followed successfully.",
	})
}

func (a *app) unfollowUser(w http.ResponseWriter, r *http.Request, followerID, targetID int64) {
	ctx, cancel := context.WithTimeout(r.Context(), 5*time.Second)
	defer cancel()

	_, err := neo4j.ExecuteQuery(
		ctx,
		a.driver,
		`
MATCH (:User {id: $followerId})-[relation:FOLLOWS]->(:User {id: $targetId})
DELETE relation
`,
		map[string]any{
			"followerId": followerID,
			"targetId":   targetID,
		},
		neo4j.EagerResultTransformer,
	)
	if err != nil {
		writeError(w, http.StatusInternalServerError, "Could not delete follow relation.")
		return
	}

	writeJSON(w, http.StatusOK, map[string]any{
		"followerId": followerID,
		"targetId":   targetID,
		"message":    "User unfollowed successfully.",
	})
}

func (a *app) handleFollowing(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		writeError(w, http.StatusMethodNotAllowed, "Method not allowed.")
		return
	}

	userID, err := parseIDFromPath(r.URL.Path, "/api/followers/following/")
	if err != nil {
		writeError(w, http.StatusBadRequest, "User id must be a positive number.")
		return
	}

	ctx, cancel := context.WithTimeout(r.Context(), 5*time.Second)
	defer cancel()

	result, err := neo4j.ExecuteQuery(
		ctx,
		a.driver,
		`
MATCH (:User {id: $userId})-[:FOLLOWS]->(followed:User)
RETURN followed.id AS id, followed.username AS username
ORDER BY followed.username
`,
		map[string]any{"userId": userID},
		neo4j.EagerResultTransformer,
	)
	if err != nil {
		writeError(w, http.StatusInternalServerError, "Could not read followed users.")
		return
	}

	writeJSON(w, http.StatusOK, map[string]any{"users": mapUsers(result.Records)})
}

func (a *app) handleRecommendations(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		writeError(w, http.StatusMethodNotAllowed, "Method not allowed.")
		return
	}

	userID, err := parseIDFromPath(r.URL.Path, "/api/followers/recommendations/")
	if err != nil {
		writeError(w, http.StatusBadRequest, "User id must be a positive number.")
		return
	}

	ctx, cancel := context.WithTimeout(r.Context(), 5*time.Second)
	defer cancel()

	result, err := neo4j.ExecuteQuery(
		ctx,
		a.driver,
		`
MATCH (user:User {id: $userId})-[:FOLLOWS]->(:User)-[:FOLLOWS]->(recommended:User)
WHERE recommended.id <> user.id
  AND NOT (user)-[:FOLLOWS]->(recommended)
RETURN recommended.id AS id, recommended.username AS username, count(*) AS score
ORDER BY score DESC, recommended.username
`,
		map[string]any{"userId": userID},
		neo4j.EagerResultTransformer,
	)
	if err != nil {
		writeError(w, http.StatusInternalServerError, "Could not read recommendations.")
		return
	}

	writeJSON(w, http.StatusOK, map[string]any{"users": mapUsers(result.Records)})
}

func (a *app) handleCheck(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		writeError(w, http.StatusMethodNotAllowed, "Method not allowed.")
		return
	}

	followerID, err := readQueryID(r, "followerId")
	if err != nil {
		writeError(w, http.StatusBadRequest, "followerId query parameter is required.")
		return
	}

	targetID, err := readQueryID(r, "targetId")
	if err != nil {
		writeError(w, http.StatusBadRequest, "targetId query parameter is required.")
		return
	}

	ctx, cancel := context.WithTimeout(r.Context(), 5*time.Second)
	defer cancel()

	result, err := neo4j.ExecuteQuery(
		ctx,
		a.driver,
		`
RETURN EXISTS {
  MATCH (:User {id: $followerId})-[:FOLLOWS]->(:User {id: $targetId})
} AS isFollowing
`,
		map[string]any{
			"followerId": followerID,
			"targetId":   targetID,
		},
		neo4j.EagerResultTransformer,
	)
	if err != nil || len(result.Records) == 0 {
		writeError(w, http.StatusInternalServerError, "Could not check follow relation.")
		return
	}

	isFollowing, _ := result.Records[0].Get("isFollowing")
	writeJSON(w, http.StatusOK, checkResponse{
		FollowerID:  followerID,
		TargetID:    targetID,
		IsFollowing: isFollowing.(bool),
	})
}

func mapUsers(records []*neo4j.Record) []userResponse {
	users := make([]userResponse, 0, len(records))
	for _, record := range records {
		idValue, _ := record.Get("id")
		usernameValue, _ := record.Get("username")

		id, _ := idValue.(int64)
		username, _ := usernameValue.(string)
		users = append(users, userResponse{ID: id, Username: username})
	}
	return users
}

func parseIDFromPath(path, prefix string) (int64, error) {
	value := strings.TrimPrefix(path, prefix)
	if value == path || strings.Contains(value, "/") {
		return 0, errors.New("invalid path")
	}
	return parsePositiveID(value)
}

func readHeaderID(r *http.Request, header string) (int64, error) {
	return parsePositiveID(r.Header.Get(header))
}

func readQueryID(r *http.Request, key string) (int64, error) {
	return parsePositiveID(r.URL.Query().Get(key))
}

func parsePositiveID(raw string) (int64, error) {
	id, err := strconv.ParseInt(strings.TrimSpace(raw), 10, 64)
	if err != nil || id <= 0 {
		return 0, fmt.Errorf("invalid id")
	}
	return id, nil
}

func defaultUsername(id int64) string {
	return fmt.Sprintf("user-%d", id)
}

func firstNonEmpty(values ...string) string {
	for _, value := range values {
		trimmed := strings.TrimSpace(value)
		if trimmed != "" {
			return trimmed
		}
	}
	return ""
}

func writeJSON(w http.ResponseWriter, status int, value any) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	_ = json.NewEncoder(w).Encode(value)
}

func writeError(w http.ResponseWriter, status int, message string) {
	writeJSON(w, status, map[string]string{"message": message})
}
