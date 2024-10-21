package server

import (
	"context"
	"errors"
	"log"
	"net/http"

	"github.com/gorilla/mux"
)

// Contains the server configuration
type ServerConfig struct {
	Port        string
	DatabaseUrl string
	Database    string
	Collection  string
}

// Interface that defines the server configuration
type Server interface {
	Config() *ServerConfig
}

// Implements the Server interface and contains the configuration and router
type HTTPServer struct {
	serverConfig *ServerConfig
	router       *mux.Router
}

// Returns the server configuration
func (httpServer *HTTPServer) Config() *ServerConfig {
	return httpServer.serverConfig
}

// Initializes a new HTTP server with the provided configuration
func NewServer(ctx context.Context, serverConfig *ServerConfig) (*HTTPServer, error) {
	if serverConfig.Port == "" {
		return nil, errors.New("port is required to start the server")
	}
	if serverConfig.DatabaseUrl == "" {
		return nil, errors.New("database URL is required")
	}

	// Create a new instance of HTTPServer with the configuration
	httpServer := &HTTPServer{
		serverConfig: serverConfig,
		router:       mux.NewRouter(),
	}
	return httpServer, nil
}

// Start the HTTP server and bind the provided routes
func (httpServer *HTTPServer) Start(routeBinder func(server Server, router *mux.Router)) {
	httpServer.router = mux.NewRouter()

	// Link the routes
	routeBinder(httpServer, httpServer.router)

	// Start the server
	log.Println("Starting server on port", httpServer.serverConfig.Port)
	if err := http.ListenAndServe(":"+httpServer.serverConfig.Port, httpServer.router); err != nil {
		log.Println("Error starting the server:", err)
	} else {
		log.Fatalf("The server stopped unexpectedly")
	}
}
