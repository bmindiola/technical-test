package main

import (
	"context"
	"log"
	"net/http"
	"os"

	"github.com/gorilla/mux"
	"github.com/joho/godotenv"
	"product-api/handlers"
	"product-api/server"
)

func main() {
	// Load the environment variables from the .env file
	err := godotenv.Load()
	if err != nil {
		log.Fatalf("Error loading .env file: %v\n", err)
	}

	// Retrieve the necessary environment variables
	serverPort := os.Getenv("PORT")
	mongoDatabaseUrl := os.Getenv("MONGO_URI")
	mongoDatabaseName := os.Getenv("MONGO_DB")
	mongoCollectionName := os.Getenv("MONGO_COLLECTION")

	// Create the server configuration with the environment variables
	productServerConfig := &server.ServerConfig{
		Port:        serverPort,
		DatabaseUrl: mongoDatabaseUrl,
		Database:    mongoDatabaseName,
		Collection:  mongoCollectionName,
	}

	// Initialize the server
	productHTTPServer, serverCreationError := server.NewServer(context.Background(), productServerConfig)
	if serverCreationError != nil {
		log.Fatalf("Error creating the server: %v\n", serverCreationError)
	}

	// Start the server and define the routes
	productHTTPServer.Start(SetupRoutes)
}

// Function to define the server routes
func SetupRoutes(productServer server.Server, router *mux.Router) {
	router.HandleFunc("/product", handlers.GetProductHandler(productServer)).Methods(http.MethodGet)
}
