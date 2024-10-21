FROM golang:1.19

WORKDIR /app

COPY . .

RUN go mod tidy

RUN go build -o customer-api

EXPOSE 5050

CMD ["./customer-api"]
