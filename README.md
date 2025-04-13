# Amaranth - Model Client

Amaranth is a Java-based application that provides a chat interface for interacting with AI models. 
It integrates with the Ollama API to send chat requests, generate embeddings, and manage chat history. 
The project is built using **Java** and **Maven** and includes a Swing-based user interface.

## Features

- **Chat Interface**: A user-friendly chat panel for sending messages and receiving responses.
- **Chat History Management**: Maintains a history of chat entries with a configurable size.
- **Embedding Generation**: Generates and stores text and chat embeddings using the Ollama API.
- **Database Integration**: Stores embeddings in an H2 database with support for similarity search.
- **Customizable Configuration**: Easily configurable settings for chat and embedding models.

## Scope

This is a hobby research project aimed at exploring the capabilities of AI models in a chat interface.


## Technologies Used

I preferred lightweight and simple solutions over complex frameworks.

- **Java**: Core programming language.
- **Maven**: Build and dependency management.
- **Swing**: For the graphical user interface.
- **H2 Database**: For storing embeddings. H2 is not a vector database, so won't scape but it is lightweight and easy to use.
- **Ollama API**: For chat and embedding generation. For embeddings, nomic-embed-text is used, for chat, the model is gemma3:1b.
This configuration can be run locally, and is reasonably fast even with CPU inference.

## System Requirements (for running locally)

- Java 11 or higher
- Ollama instance running locally
- 16 GB of RAM (recommended for better performance)
- 100MB of disk space (for the H2 database and application files)

## How to Run

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd amaranth

2. Build the project using Maven:
    ```bash
    mvn clean install
    ```

3. Run the application:
    ```bash 
    java -jar target/amaranth-<version>.jar
    ```

The application will open a chat interface where you can interact with the AI model.



## Configuration
The application uses a config.properties file for configuration. Key settings include:

Chat History Size: Maximum number of chat entries to store.
Chat Model: AI model to use for chat interactions.
Embedding Model: Model used for generating embeddings.
Database Connection: JDBC URL, username, and password for the H2 database.

## License
This project is licensed under the MIT License. See the LICENSE file for details.