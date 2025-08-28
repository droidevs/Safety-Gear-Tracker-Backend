# Safety Gear Tracker Backend

## Project Overview

The **Safety Gear Tracker Backend** is a robust and scalable Java Spring Boot application designed to enhance workplace safety by automatically detecting the presence or absence of required safety gear in designated zones. Leveraging advanced video processing, object detection, and Google's Generative AI, this system provides real-time monitoring, generates alerts for violations, and offers comprehensive management of cameras, safety zones, users, and recordings.

This backend serves as the core intelligence for a proactive safety monitoring solution, ensuring compliance with safety regulations and fostering a safer work environment.

## Features

*   **Camera Management:**
    *   Add, update, view, and delete camera configurations.
    *   Integrate with Hikvision cameras via RTSP.
    *   Manage camera activation status.
*   **Zone Management:**
    *   Define and manage safety zones within monitored areas.
    *   Associate specific safety gear requirements with each zone.
*   **Safety Gear Detection (AI-Powered):**
    *   Utilizes advanced object detection to identify `HARD_HAT`, `SAFETY_VEST`, `GLOVES`, and `SAFETY_GLASSES`.
    *   Real-time analysis of video streams to detect safety violations.
*   **Alerting System:**
    *   Generate alerts upon detection of safety gear violations.
    *   Store and manage alerts, including details of the violation and associated recordings.
    *   Configurable notification mechanisms.
*   **Video Recording & Streaming:**
    *   Record video footage from monitored cameras.
    *   Securely store recordings in AWS S3.
    *   Stream recorded footage and live camera feeds.
*   **User Management & Authentication:**
    *   Secure user registration, login, and profile management.
    *   Role-based access control (RBAC).
    *   JWT-based authentication with OTP and email verification.
*   **Google Generative AI Integration:**
    *   Future-proofed architecture with integration points for Google's Generative AI models.
    *   Potential applications include:
        *   Contextual alert generation (e.g., explaining why an alert was triggered).
        *   Summarizing incident reports based on video analysis.
        *   Intelligent query processing for safety data.
*   **Scalability & Robustness:**
    *   Built with Spring Boot for enterprise-grade performance.
    *   Asynchronous processing for non-blocking operations.
    *   Comprehensive error handling.
*   **Cloud Integration:**
    *   Seamless integration with AWS S3 for media storage.

## Technologies Used

*   **Backend:** Java 17+, Spring Boot
*   **Database:** PostgreSQL (Common choice for Spring Boot, assumed for typical enterprise apps)
*   **Generative AI:** Google Generative AI SDK for Java (Gemini API)
*   **Cloud Storage:** AWS S3
*   **Video Processing:** FFmpeg (implied by video processing services)
*   **Security:** Spring Security, JWT
*   **Build Tool:** Maven

## Architecture

The Safety Gear Tracker Backend follows a modular, layered architecture:

*   **Controllers:** Handle incoming API requests and orchestrate responses.
*   **Services:** Encapsulate business logic, interacting with repositories and external services.
*   **Repositories:** Manage data persistence (e.g., Spring Data JPA).
*   **Processors:** Dedicated modules for video processing and AI inference.
*   **Configuration:** Manages application settings, security, and external service integrations.

### [Conceptual Diagram: System Architecture]
*   **Description:** A high-level block diagram illustrating the flow of data and interaction between key components.
*   **Elements to show:**
    *   **Cameras:** Input source.
    *   **Safety Gear Tracker Backend:** Central hub.
    *   **Database:** For persistent data (cameras, zones, users, alerts).
    *   **AWS S3:** For storing recordings and images.
    *   **Google Generative AI:** For advanced AI capabilities.
    *   **Notifications/Alerts:** Output mechanism.
*   **Flow:** Cameras -> Backend (Video Processing, Safety Gear Detection, Alerting) -> Database / S3 / Generative AI. Backend also communicates with a (hypothetical) Frontend/Dashboard.

## Generative AI Integration

This project is built with the `com.google.genai:google-genai` SDK, enabling seamless interaction with Google's Gemini models. The `GenerativeAiConfig.java` class sets up the necessary `GenerativeModel` instance. While current implementations focus on core safety detection, the integrated Generative AI offers vast potential for future enhancements, such as:

*   **Smart Alert Context:** Providing natural language explanations for detected violations.
*   **Automated Reporting:** Generating detailed incident reports based on processed video data and detected events.
*   **Proactive Safety Recommendations:** Analyzing historical data to suggest preventative measures.
*   **Multimodal Analysis:** Combining video frames with textual descriptions for richer insights.

## Getting Started

### Prerequisites

*   Java Development Kit (JDK) 17 or higher
*   Maven 3.6+
*   Docker and Docker Compose (for local development with services like database)
*   Google Cloud Project with Generative AI API enabled and a service account or API key configured.
*   AWS Account with S3 bucket configured and credentials.
*   Environment variables set for API keys and cloud credentials (e.g., `GEMINI_API_KEY`, `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_REGION`, `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, etc.).

### Installation

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/your-org/safety-gear-tracker-backend.git
    cd safety-gear-tracker-backend
    ```

2.  **Configure Environment Variables:**
    Create a `.env` file or set system-wide environment variables for your Google Cloud Project ID, location, Gemini API key, AWS credentials, database connection, and other sensitive information. Refer to `application.properties` for expected variables.

3.  **Build the Project:**
    ```bash
    mvn clean install
    ```

4.  **Run with Docker Compose (Recommended for local development):**
    This will typically start a database and any other required services.
    ```bash
    docker-compose up --build
    ```

5.  **Run Directly (without Docker Compose for the app itself):**
    ```bash
    mvn spring-boot:run
    ```

### API Endpoints

The backend exposes a RESTful API for managing safety operations. For detailed API documentation, refer to the Swagger UI, which will be available at `/swagger-ui.html` when the application is running.

**Example Endpoints:**

*   `POST /api/v1/auth/register`: Register a new user.
*   `POST /api/v1/auth/authenticate`: Authenticate and get a JWT token.
*   `GET /api/v1/cameras`: Retrieve a list of registered cameras.
*   `GET /api/v1/zones`: Retrieve a list of configured safety zones.
*   `GET /api/v1/alerts`: Retrieve a list of safety alerts.
*   `GET /api/v1/recordings/{recordingId}/stream`: Stream a specific recording.

### [Interactive Demo: Safety Violation Workflow Animation]
*   **Description:** An animated GIF or embedded video showcasing the core functionality of the system.
*   **Scenario:**
    1.  **Camera Feed:** Display a live-like video feed of a person entering a designated safety zone.
    2.  **Detection:** Highlight or visually indicate the system detecting the person and their safety gear (or lack thereof). For instance, bounding boxes around the person and their gear, with labels like "NO HARD_HAT".
    3.  **Alert Trigger:** Show an alert being generated in response to a detected violation (e.g., a notification pop-up or an alert dashboard snippet).
    4.  **Resolution (Optional):** Show the person complying with safety regulations, and the alert being cleared or updated.

## Contribution

We welcome contributions to the Safety Gear Tracker Backend! Please fork the repository and submit pull requests. For major changes, please open an issue first to discuss what you would like to change.

## License

This project is licensed under the MIT License. See the `LICENSE` file for details.