
# Guía de Configuración y Ejecución del Proyecto

## Requisitos Previos

Asegúrate de tener los siguientes programas instalados en tu máquina:
- Docker

## Paso 1: Clonar el repositorio

Clona el repositorio que contiene todos los archivos del proyecto, incluyendo los datos de MongoDB (clientes y productos), la configuración de Kafka, el productor de Python y el trabajador en Java.

```bash
git clone https://github.com/bmindiola/technical-test.git
o
git clone git@github.com:bmindiola/technical-test.git
cd technical-test
```

## Paso 2: Ejecutar el proyecto utilizando Docker Compose

Para iniciar todos los servicios, ejecuta el siguiente comando:

```bash
docker-compose up --build
```

Este comando hará lo siguiente:
- Inicia **Kafka** y **Zookeeper**.
- Inicia **MongoDB**.
- Inicia el **worker Java**.
- Inicia los **servicios de productos y clientes creados en Go**.

## Paso 3: Inyectar una orden en Kafka utilizando un script de Python

Una vez que todos los servicios estén en funcionamiento, puedes inyectar órdenes en el tópico `orders` de Kafka usando el script de Python proporcionado.

Ejecuta el siguiente comando para ejecutar el script en Python:

```bash
docker-compose run --rm python-producer
```

El script te pedirá los datos `orderId`, `customerId` y `productIds`. Por ejemplo:

```
Enter orderId: order-001
Enter customerId: customer-001
Enter productId (or leave blank to finish): product-001
Enter productId (or leave blank to finish): product-002
Enter productId (or leave blank to finish): 
Order sent: {'orderId': 'order-001', 'customerId': 'customer-001', 'products': [{'productId': 'product-001'}, {'productId': 'product-002'}]}
```

Este comando inyectará la orden en el tópico de Kafka.

## Paso 4: Recuperar las órdenes procesadas desde MongoDB

Después de que el trabajador procese la orden, se almacenará en MongoDB.

Puedes acceder a MongoDB y recuperar las órdenes procesadas ejecutando:

```bash
docker exec -it mongodb mongosh -u root -p passw0rd --authenticationDatabase admin
use workerdb
db.orders.find().pretty()
```

Ejemplo de salida de una orden procesada:

```json
{
  "_id": ObjectId("617a66b3b4323a1e8c78a45c"),
  "orderId": "order-001",
  "customerId": "customer-001",
  "products": [
    {
      "productId": "product-001",
      "name": "Laptop",
      "description": "High-performance laptop",
      "price": 999,
      "stock": 50
    }
  ]
}
```

## Paso 5: Detener la configuración

Cuando hayas terminado, puedes detener y limpiar los contenedores utilizando:

```bash
docker-compose down
```

Esto detendrá todos los contenedores en ejecución y eliminará las redes creadas por Docker Compose.
