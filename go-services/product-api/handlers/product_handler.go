package handlers

import (
	"encoding/json"
	"net/http"
	"product-api/server"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
	"context"
	"log"
	"go.mongodb.org/mongo-driver/bson"
)

// Structure that represents a product
type Product struct {
	ProductID   string  `json:"productId"`
	Name        string  `json:"name"`
	Description string  `json:"description"`
	Price       float64 `json:"price"`
	Stock       int     `json:"stock"`
}

// Connection to MongoDB using the server configuration
func connectToMongoDB(productServer server.Server) (*mongo.Client, error) {
	mongoClientOptions := options.Client().ApplyURI(productServer.Config().DatabaseUrl)
	mongoClient, connectionError := mongo.Connect(context.TODO(), mongoClientOptions)
	if connectionError != nil {
		return nil, connectionError
	}

	// Verify that the connection is active
	pingError := mongoClient.Ping(context.TODO(), nil)
	if pingError != nil {
		return nil, pingError
	}

	log.Println("Successfully connected to MongoDB")
	return mongoClient, nil
}

// Handler to get product details using the server configuration
func GetProductHandler(productServer server.Server) http.HandlerFunc {
	return func(responseWriter http.ResponseWriter, request *http.Request) {
		responseWriter.Header().Set("Content-Type", "application/json")
		
		// Connect to MongoDB using the server configuration
		mongoClient, dbConnectionError := connectToMongoDB(productServer)
		if dbConnectionError != nil {
			http.Error(responseWriter, "Error connecting to the database", http.StatusInternalServerError)
			return
		}

		// Get the product ID from the URL parameters
		productID := request.URL.Query().Get("id")
		if productID == "" {
			http.Error(responseWriter, "Product ID not provided", http.StatusBadRequest)
			return
		}

		// Access the database and collection using the server configuration
		productCollection := mongoClient.Database(productServer.Config().Database).Collection(productServer.Config().Collection)

		var productDetails Product
		// Find the product in the product collection with the provided ID
		findError := productCollection.FindOne(context.TODO(), bson.M{"productId": productID}).Decode(&productDetails)
		if findError != nil {
			http.Error(responseWriter, "Product not found", http.StatusNotFound)
			return
		}

		// Respond with the product details in JSON format
		json.NewEncoder(responseWriter).Encode(productDetails)
	}
}
