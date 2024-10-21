FROM golang:1.19

WORKDIR /app

COPY . .

RUN go mod tidy

RUN go build -o product-api

EXPOSE 5060

CMD ["./product-api"]
