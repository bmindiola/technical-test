package handlers

import (
	"encoding/json"
	"net/http"
	"customer-api/server"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
	"context"
	"log"
	"go.mongodb.org/mongo-driver/bson"
)

// Structure that represents a customer
type Customer struct {
	CustomerID string `json:"customerId"`
	Name       string `json:"name"`
	Email      string `json:"email"`
	Phone      string `json:"phone"`
	Status     string `json:"status"`
}

// Connection to MongoDB using the server configuration
func connectToMongoDB(customerServer server.Server) (*mongo.Client, error) {
	mongoClientOptions := options.Client().ApplyURI(customerServer.Config().DatabaseUrl)
	mongoClient, connectionError := mongo.Connect(context.TODO(), mongoClientOptions)
	if connectionError != nil {
		return nil, connectionError
	}

	// Verify that the connection is active
	pingError := mongoClient.Ping(context.TODO(), nil)
	if pingError != nil {
		return nil, pingError
	}

	log.Println("Successful connection to MongoDB")
	return mongoClient, nil
}

// Handler to get customer details using the server configuration
func GetCustomerHandler(customerServer server.Server) http.HandlerFunc {
	return func(responseWriter http.ResponseWriter, request *http.Request) {
		responseWriter.Header().Set("Content-Type", "application/json")
		
		// Connect to MongoDB using the server configuration
		mongoClient, dbConnectionError := connectToMongoDB(customerServer)
		if dbConnectionError != nil {
			http.Error(responseWriter, "Error connecting to the database", http.StatusInternalServerError)
			return
		}

		// Get the customer ID from the URL parameters
		customerID := request.URL.Query().Get("id")
		if customerID == "" {
			http.Error(responseWriter, "Customer ID not provided", http.StatusBadRequest)
			return
		}

		// Access the database and collection using the server configuration
		customerCollection := mongoClient.Database(customerServer.Config().Database).Collection(customerServer.Config().Collection)

		var customerDetails Customer
		// Find the customer in the customer collection with the provided ID
		findError := customerCollection.FindOne(context.TODO(), bson.M{"customerId": customerID}).Decode(&customerDetails)
		if findError != nil {
			http.Error(responseWriter, "Customer not found", http.StatusNotFound)
			return
		}

		// Respond with the customer details in JSON format
		json.NewEncoder(responseWriter).Encode(customerDetails)
	}
}
