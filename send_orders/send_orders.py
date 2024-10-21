from kafka import KafkaProducer
import json

# Kafka producer configuration
producer = KafkaProducer(
    bootstrap_servers='kafka:9092',
    value_serializer=lambda v: json.dumps(v).encode('utf-8')
)

# Function to inject orders into the Kafka topic
def send_order(order_id, customer_id, products):
    order = {
        "orderId": order_id,
        "customerId": customer_id,
        "products": products
    }
    producer.send('orders', value=order)
    print(f"Order sent: {order}")

# Function to get products input from the user
def get_products():
    products = []
    while True:
        product_id = input("Enter productId (or leave blank to finish): ")
        if not product_id:  # If the field is blank, exit the loop
            break
        
        # Add the productId to the list
        products.append({
            "productId": product_id,
        })
    
    return products

# Main execution
if __name__ == "__main__":
    # Get user input for orderId, customerId, and products
    order_id = input("Enter orderId: ")
    customer_id = input("Enter customerId: ")
    products = get_products()

    if products:
        send_order(order_id, customer_id, products)
    else:
        print("No products were added. No order will be sent.")

    producer.flush()  # Ensure all messages are sent
    producer.close()
