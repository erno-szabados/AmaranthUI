package com.esgdev.amaranthui.db.h2;

import com.esgdev.amaranthui.engine.embedding.ChatChunkEmbedding;
import com.esgdev.amaranthui.db.EmbeddingDao;
import com.esgdev.amaranthui.engine.embedding.EmbeddingConfiguration;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * H2 implementation of the EmbeddingDao interface for ChatChunkEmbedding.
 * This class handles the database operations for storing and retrieving chat chunk embeddings.
 */
public class ChatChunkEmbeddingDaoH2 implements EmbeddingDao<ChatChunkEmbedding> {
    private final EmbeddingConfiguration config;
    Logger logger = Logger.getLogger(ChatChunkEmbeddingDaoH2.class.getName());

    public ChatChunkEmbeddingDaoH2(EmbeddingConfiguration config) {
        this.config = config;
        initializeDatabase();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(config.getJdbcUrl(), config.getJdbcUser(), config.getJdbcPassword());
    }

    private void initializeDatabase() {
        String createTableSQL = """
                CREATE TABLE IF NOT EXISTS chat_chunk_embeddings (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    chunk VARCHAR NOT NULL,
                    embedding DOUBLE ARRAY NOT NULL,
                    creation_date TIMESTAMP NOT NULL,
                    last_accessed TIMESTAMP NOT NULL,
                    embedding_model VARCHAR NOT NULL,
                    conversation_id BIGINT,
                    user_id BIGINT,
                    role VARCHAR,
                    reply_to_chunk_id BIGINT,
                    topic VARCHAR -- New column for topic
                );
                """;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database schema", e);
        }
    }

    @Override
    public void addEmbedding(ChatChunkEmbedding embedding) {
        String sql = """
                INSERT INTO chat_chunk_embeddings 
                (chunk, embedding, creation_date, last_accessed, embedding_model, conversation_id, user_id, role, reply_to_chunk_id, topic) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
                """;
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, embedding.getChunk());
            stmt.setArray(2, conn.createArrayOf("DOUBLE", embedding.getEmbedding().toArray()));
            stmt.setTimestamp(3, new Timestamp(embedding.getCreationDate().getTime()));
            stmt.setTimestamp(4, new Timestamp(embedding.getLastAccessed().getTime()));
            stmt.setString(5, embedding.getEmbeddingModel());
            stmt.setObject(6, embedding.getConversationId());
            stmt.setObject(7, embedding.getUserId());
            stmt.setString(8, embedding.getRole());
            stmt.setObject(9, embedding.getReplyToChunkId());
            stmt.setString(10, embedding.getTopic());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(java.util.logging.Level.SEVERE, "Failed to add embedding", e);
        }
    }

    @Override
    public void addEmbedding(List<ChatChunkEmbedding> embeddings) {
        String sql = """
                INSERT INTO chat_chunk_embeddings 
                (chunk, embedding, creation_date, last_accessed, embedding_model, conversation_id, user_id, role, reply_to_chunk_id, topic) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
                """;
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (ChatChunkEmbedding embedding : embeddings) {
                stmt.setString(1, embedding.getChunk());
                stmt.setArray(2, conn.createArrayOf("DOUBLE", embedding.getEmbedding().toArray()));
                stmt.setTimestamp(3, new Timestamp(embedding.getCreationDate().getTime()));
                stmt.setTimestamp(4, new Timestamp(embedding.getLastAccessed().getTime()));
                stmt.setString(5, embedding.getEmbeddingModel());
                stmt.setObject(6, embedding.getConversationId());
                stmt.setObject(7, embedding.getUserId());
                stmt.setString(8, embedding.getRole());
                stmt.setObject(9, embedding.getReplyToChunkId());
                stmt.setString(10, embedding.getTopic());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            logger.log(java.util.logging.Level.SEVERE, "Failed to add embeddings", e);
        }
    }

    @Override
    public void updateEmbedding(ChatChunkEmbedding embedding) {
        String sql = """
                UPDATE chat_chunk_embeddings
                SET chunk = ?, embedding = ?, last_accessed = ?, embedding_model = ?,
                    conversation_id = ?, user_id = ?, role = ?, reply_to_chunk_id = ?, topic = ?
                WHERE id = ?;
                """;
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, embedding.getChunk());
            stmt.setArray(2, conn.createArrayOf("DOUBLE", embedding.getEmbedding().toArray()));
            stmt.setTimestamp(3, new Timestamp(embedding.getLastAccessed().getTime()));
            stmt.setString(4, embedding.getEmbeddingModel());
            stmt.setObject(5, embedding.getConversationId());
            stmt.setObject(6, embedding.getUserId());
            stmt.setString(7, embedding.getRole());
            stmt.setObject(8, embedding.getReplyToChunkId());
            stmt.setLong(9, embedding.getId());
            stmt.setString(10, embedding.getTopic()); // Set topic
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(java.util.logging.Level.SEVERE, "Failed to update embeddings", e);
        }
    }

    @Override
    public void deleteEmbedding(Long id) {
        String sql = "DELETE FROM chat_chunk_embeddings WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(java.util.logging.Level.SEVERE, "Failed to delete embeddings", e);
        }
    }

    @Override
    public List<ChatChunkEmbedding> findEmbeddingsNear(ChatChunkEmbedding sourceEmbedding, int limit) {
        String sql = """
                WITH Similarities AS (
                    SELECT id, chunk, embedding, creation_date, last_accessed, conversation_id, user_id, role, reply_to_chunk_id, embedding_model, topic,
                           COSINE_SIMILARITY(embedding, ?) AS similarity
                    FROM chat_chunk_embeddings
                    WHERE embedding_model = ? -- Filter by the current embedding model
                )
                SELECT id, chunk, embedding, creation_date, last_accessed, conversation_id, user_id, role, reply_to_chunk_id, embedding_model, topic, similarity
                FROM Similarities
                WHERE similarity IS NOT NULL
                ORDER BY similarity DESC
                LIMIT ?;
                """;

        List<ChatChunkEmbedding> similarEmbeddings = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Convert sourceEmbedding's embedding to SQL array
            Array embeddingArray = conn.createArrayOf("DOUBLE", sourceEmbedding.getEmbedding().toArray());
            stmt.setObject(1, embeddingArray);
            stmt.setString(2, sourceEmbedding.getEmbeddingModel()); // Set the embedding model filter
            stmt.setInt(3, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    similarEmbeddings.add(mapResultSetToChatChunkEmbedding(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(java.util.logging.Level.SEVERE, "Failed to find similar embeddings", e);
        }
        return similarEmbeddings;
    }

    @Override
    public ChatChunkEmbedding getEmbeddingById(Long id) {
        String sql = "SELECT * FROM chat_chunk_embeddings WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToChatChunkEmbedding(rs);
                }
            }
        } catch (SQLException e) {
            logger.log(java.util.logging.Level.SEVERE, "Failed to find embeddings", e); // Handle exceptions properly
        }
        return null; // Return null if no embedding is found
    }

    @Override
    public List<ChatChunkEmbedding> getAllEmbeddings() {
        String sql = "SELECT * FROM chat_chunk_embeddings";
        List<ChatChunkEmbedding> embeddings = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                embeddings.add(mapResultSetToChatChunkEmbedding(rs));
            }
        } catch (SQLException e) {
            logger.log(java.util.logging.Level.SEVERE, "Failed to get all embeddings", e); // Handle exceptions properly
        }
        return embeddings;
    }

    private ChatChunkEmbedding mapResultSetToChatChunkEmbedding(ResultSet rs) throws SQLException {
        ChatChunkEmbedding embedding = new ChatChunkEmbedding();
        embedding.setId(rs.getLong("id"));
        embedding.setChunk(rs.getString("chunk"));
        Array embeddingArray = rs.getArray("embedding");
        if (embeddingArray != null) {
            Object[] objectArray = (Object[]) embeddingArray.getArray();
            List<Double> embeddingValues = new ArrayList<>();
            for (Object obj : objectArray) {
                embeddingValues.add(((Number) obj).doubleValue());
            }
            embedding.setEmbedding(embeddingValues);
        }
        embedding.setCreationDate(rs.getTimestamp("creation_date"));
        embedding.setLastAccessed(rs.getTimestamp("last_accessed"));
        embedding.setEmbeddingModel(rs.getString("embedding_model"));
        embedding.setConversationId(rs.getLong("conversation_id"));
        embedding.setUserId(rs.getLong("user_id"));
        embedding.setRole(rs.getString("role"));
        embedding.setTopic(rs.getString("topic"));
        embedding.setSimilarity(rs.getDouble("similarity"));
        embedding.setReplyToChunkId(rs.getLong("reply_to_chunk_id"));
        return embedding;
    }
}