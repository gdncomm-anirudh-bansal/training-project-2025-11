# API Gateway - Spring Cloud Gateway with JWT Authentication

A Spring Cloud Gateway application that provides:
1. **Service Routing**: Routes requests to different microservices
2. **JWT Authentication**: Generates and validates JWT tokens containing userId

## Features

- ✅ JWT token generation and validation
- ✅ Dual authentication support (Cookie and Authorization Header)
- ✅ Service routing with dynamic path configuration
- ✅ UserId extraction and forwarding to downstream services
- ✅ Secure cookie handling (HttpOnly, Secure, SameSite)
- ✅ Stateless authentication

## Prerequisites

- Java 21
- Maven 3.6+
- Member Service running on port 8081
- Cart Service running on port 8082 (optional)

## Configuration

### application.properties

Update the following properties:

```properties
# JWT Secret (CHANGE IN PRODUCTION!)
jwt.secret=your-secret-key-should-be-at-least-256-bits-long-for-hmac-sha-256-algorithm

# Token expiration in milliseconds (default: 1 hour)
jwt.expiration=3600000

# Service URLs
member.service.url=http://localhost:8081
cart.service.url=http://localhost:8082
```

## Running the Application

```bash
mvn clean install
mvn spring-boot:run
```

The gateway will start on port 8080.

## API Endpoints

### Authentication Endpoints

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "user",
  "password": "password",
  "email": "user@example.com"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "user",
  "password": "password"
}
```

**Response:**
- Body: `{"token": "<JWT>", "user_id": "123", "message": "Login successful"}`
- Cookie: `token=<JWT>` (HttpOnly, Secure, SameSite=Strict)

#### Logout
```http
POST /api/auth/logout
```

**Response:**
- Cookie cleared (Max-Age=0) for cookie-based auth
- Client should discard token for header-based auth

#### Validate Token
```http
GET /api/auth/validate
Authorization: Bearer <JWT>
# OR
Cookie: token=<JWT>
```

### Service Routes

#### Member Service (Protected)
```http
GET /api/member/**
POST /api/member/**
# Requires JWT authentication
```

#### Cart Service (Protected)
```http
GET /api/cart/**
POST /api/cart/**
# Requires JWT authentication
```

#### Public Member Service Routes
```http
GET /api/public/member/**
# No authentication required
```

## Authentication Methods

### Method 1: Cookie-Based (Recommended for Web)

1. Login to get JWT token
2. Browser automatically sends cookie with subsequent requests
3. Gateway validates cookie and extracts userId
4. Logout clears the cookie

### Method 2: Header-Based (Recommended for Mobile/API)

1. Login to get JWT token from response body
2. Include token in Authorization header: `Authorization: Bearer <JWT>`
3. Gateway validates header and extracts userId
4. Logout: Client discards token

## Request Flow

1. **Client** → **API Gateway** (with JWT)
2. **API Gateway** validates JWT and extracts userId
3. **API Gateway** → **Service** (with X-User-Id header)
4. **Service** processes request and returns response
5. **API Gateway** → **Client** (with response)

## Downstream Service Integration

Downstream services receive the validated `userId` in the `X-User-Id` header:

```java
@GetMapping("/cart")
public ResponseEntity<?> getCart(@RequestHeader("X-User-Id") String userId) {
    // Use userId to fetch user-specific data
    return ResponseEntity.ok(cartService.getCart(userId));
}
```

## Security Notes

⚠️ **IMPORTANT**: 
- Change the JWT secret in production
- Use HTTPS in production (Secure cookie flag requires HTTPS)
- Configure CORS origins for production
- Consider implementing refresh tokens for long-lived sessions
- Add rate limiting to prevent abuse

## Project Structure

```
src/main/java/com/Project/Api_Gateway/
├── ApiGatewayApplication.java    # Main application class
├── Config/
│   ├── GatewayConfig.java        # Route configuration
│   └── CorsConfig.java           # CORS configuration
├── Controller/
│   └── AuthController.java       # Authentication endpoints
├── Filter/
│   └── JwtAuthenticationFilter.java  # JWT validation filter
└── Service/
    └── JwtService.java           # JWT generation and validation
```

## Troubleshooting

### Token Validation Fails
- Check JWT secret matches between generation and validation
- Verify token hasn't expired
- Ensure token is properly formatted

### Service Not Found
- Verify service URLs in GatewayConfig.java
- Check if services are running on configured ports
- Review route path patterns

### CORS Issues
- Update CorsConfig.java with specific origins
- Check preflight OPTIONS requests are handled

## License

This project is part of a microservices architecture demonstration.

