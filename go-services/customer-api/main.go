package main

import (
	"context"
	"log"
	"net/http"
	"os"

	"github.com/gorilla/mux"
	"github.com/joho/godotenv"
	"customer-api/handlers"
	"customer-api/server"
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
	customerServerConfig := &server.ServerConfig{
		Port:        serverPort,
		DatabaseUrl: mongoDatabaseUrl,
		Database:    mongoDatabaseName,
		Collection:  mongoCollectionName,
	}

	// Initialize the server
	customerServer, serverCreationError := server.NewServer(context.Background(), customerServerConfig)
	if serverCreationError != nil {
		log.Fatalf("Error creating the server: %v\n", serverCreationError)
	}

	// Start the server and define the routes
	customerServer.Start(SetupRoutes)
}

// Function to define the server routes
func SetupRoutes(customerServer server.Server, router *mux.Router) {
	router.HandleFunc("/customer", handlers.GetCustomerHandler(customerServer)).Methods(http.MethodGet)
}
