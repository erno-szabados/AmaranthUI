package com.esgdev.amethystui;

import com.esgdev.amethystui.engine.EmbeddingManager;
import com.esgdev.amethystui.h2.VectorSimilarity;
import io.github.ollama4j.OllamaAPI;
import org.h2.jdbcx.JdbcDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DependencyFactory {

    private static final Connection dbConnection;
    private static final OllamaAPI ollamaAPI;
    private static final String embeddingModel;

    static {
        try {
            // Load configuration from config.properties
            Properties properties = new Properties();
            properties.load(DependencyFactory.class.getClassLoader().getResourceAsStream("config.properties"));

            // Initialize H2 in-memory database
            JdbcDataSource dataSource = new JdbcDataSource();
            dataSource.setURL("jdbc:h2:mem:vector_db");
            dbConnection = dataSource.getConnection();

            // Register vector similarity functions
            registerVectorFunctions(dbConnection);

            // Create a table for vector storage
            try (Statement statement = dbConnection.createStatement()) {
                statement.execute("CREATE TABLE vectors (id INT PRIMARY KEY, vector VARCHAR)");
            }

            // Initialize OllamaAPI
            String host = properties.getProperty("host", "http://localhost:11434/");
            boolean proxyEnabled = Boolean.parseBoolean(properties.getProperty("proxy.enabled", "false"));

            if (proxyEnabled) {
                String proxyHost = properties.getProperty("proxy.host", "");
                String proxyPort = properties.getProperty("proxy.port", "");
                if (!proxyHost.isEmpty() && !proxyPort.isEmpty()) {
                    System.setProperty("http.proxyHost", proxyHost);
                    System.setProperty("http.proxyPort", proxyPort);
                }
            }

            ollamaAPI = new OllamaAPI(host);

            // Load embedding model
            embeddingModel = properties.getProperty("embedding_model", "nomic-embed-text:latest");

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize dependencies", e);
        }
    }

   private static void registerVectorFunctions(Connection connection) throws SQLException {
       try (Statement statement = connection.createStatement()) {
           statement.execute("CREATE ALIAS IF NOT EXISTS COSINESIMILARITY FOR \"" + VectorSimilarity.class.getName() + ".cosineSimilarity\"");
           statement.execute("CREATE ALIAS IF NOT EXISTS EUCLIDEANDISTANCE FOR \"" + VectorSimilarity.class.getName() + ".euclideanDistance\"");
       } catch (SQLException e) {
           throw new SQLException("Failed to register vector functions", e);
       }
   }

    public static EmbeddingManager createEmbeddingManager() {
        return new EmbeddingManager(dbConnection, ollamaAPI, embeddingModel);
    }

    public static Connection getDbConnection() {
        return dbConnection;
    }

    public static OllamaAPI getOllamaAPI() {
        return ollamaAPI;
    }

    public static String getEmbeddingModel() {
        return embeddingModel;
    }
}