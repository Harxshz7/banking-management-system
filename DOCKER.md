# Dockerizing BankPro API

This guide provides instructions on how to build and run the BankPro API using Docker. 
The desktop Swing application is a native desktop app and is not containerized.

## Running the API using Docker Compose

To start the REST API along with a persistent volume for the SQLite database, run the following command from the root directory:

```bash
docker compose up -d
```

This will:
- Build the API and core code into a Docker image using a multi-stage process.
- Map the internal API port (8080) to port `8080` on your host machine.
- Mount a Docker named volume (`bankpro_data`) to persist the SQLite database inside the container.

To stop the service:

```bash
docker compose down
```

## Smoke Test (cURL)

Once the container is running (`docker compose up -d`), you can test the API.

1. **Login to obtain a JWT Token:**

```bash
curl -X POST http://localhost:8080/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"your_username", "password":"your_password"}'
```
*(Replace `your_username` and `your_password` with actual credentials)*

The response will contain your `token` and `id` (userId).

2. **Access a protected endpoint (e.g., Get Accounts):**

Assuming the `id` you received above is `U-123456`, and the `token` is `eyJhb...`:

```bash
curl -X GET http://localhost:8080/accounts/U-123456 \
     -H "Authorization: Bearer <YOUR_TOKEN>"
```
*(Replace `<YOUR_TOKEN>` with the token string)*
