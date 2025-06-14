# RooMatch - Server Side API

## Description

**RooMatch** is the backend for an application that helps users find roommates and apartments.  
It supports multiple user roles (roommates and property owners), real-time matching, secure user authentication, and automated email notifications.


## 🛠️ Tech Stack

- **Kotlin** + **Ktor** – Backend framework
- **MongoDB Atlas** – Cloud-based NoSQL database
- **JWT** – JSON Web Token for authentication
- **Google OAuth** – Login via Google
- **SendGrid** – Email OTP authentication
- **Gemini API** – For smart recommendations
- **Docker** – Deployment container
- **Swagger UI** – Auto-generated API documentation


## Prerequisites

Before you can run the API locally, ensure you have the following installed:

- **Java Development Kit (JDK)**: Version 11+
- **IntelliJ IDEA** or any Kotlin-compatible IDE
- **MongoDB Atlas** account and connection string
- Internet access to use Google, Gemini, and SendGrid APIs


## Environment Configuration

Create a `.env` file or configure your environment variables. Here's the format:

```env
MONGODB_DB=your_db_name
MONGO_CONNECTION_STRING=your_mongodb_atlas_connection_string

GEMINI_API_KEY=your_gemini_api_key

GOOGLE_WEB_CLIENT_ID=your_web_client_id
GOOGLE_ANDROID_CLIENT_ID=your_android_client_id

SENDGRID_API_KEY=your_sendgrid_api_key
SENDGRID_SENDER_EMAIL=your_verified_sender_email

```
Make sure to add your `.env` to `Run->Edit Configuration...`
## Running Locally

1. Clone the repository

```
git clone https://github.com/ofekamirav/Roomatch-Back
cd RooMatch-Back
```
2. Open in IntelliJ IDEA

- Open the project directory
- Locate Application.kt
- Right-click → Run to start the Ktor server

 #### Access the Swagger UI
  Visit: http://localhost:8080/openapi

 ## Deployment

 The server is deployed on a college-hosted Docker server.

 To build and deploy the Docker container:
```
docker build -t roomatch-server .
docker run -d -p 8080:8080 --env-file .env roomatch-server
```
Make sure the .env file is configured properly or pass environment variables manually via -e.
 
## Authentication
RooMatch uses:
- JWT to authorize subsequent requests after login
- Google OAuth2 for user sign-in (via Web and Android)
- SendGrid for reset password via OTP verification 

## Project Structure
```
src/
 ┣ config/            ← Env Configuration
 ┣ database/          ← MongoDB Atlas handlers
 ┣ routes/            ← Api REST
 ┣ models/            ← Data classes
 ┣ services/          ← Business logic
 ┣ utils/             ← Helpers 
 ┗ Application.kt     ← Ktor entry point
 ```

## License

This project is licensed under the MIT License.  
Feel free to use, modify, and distribute it under the terms of the license.

© 2025 RooMatch Team. All rights reserved.
