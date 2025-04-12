package com.esgdev.amaranthui.db;

import com.esgdev.amaranthui.engine.EmbeddingConfiguration;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatChunkEmbeddingDaoH2 implements ChatChunkEmbeddingDao {
    private final EmbeddingConfiguration config;

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
                    conversation_id BIGINT,
                    user_id BIGINT,
                    role VARCHAR,
                    reply_to_chunk_id BIGINT
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
    public Long insertChatChunkEmbedding(ChatChunkEmbedding embedding) {
        String sql = """
                INSERT INTO chat_chunk_embeddings 
                (chunk, embedding, creation_date, last_accessed, conversation_id, user_id, role, reply_to_chunk_id) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?);
                """;
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, embedding.getChunk());
            stmt.setArray(2, conn.createArrayOf("DOUBLE", embedding.getEmbedding().toArray()));
            stmt.setTimestamp(3, new Timestamp(embedding.getCreationDate().getTime()));
            stmt.setTimestamp(4, new Timestamp(embedding.getLastAccessed().getTime()));
            stmt.setObject(5, embedding.getConversationId());
            stmt.setObject(6, embedding.getUserId());
            stmt.setString(7, embedding.getRole());
            stmt.setObject(8, embedding.getReplyToChunkId());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ChatChunkEmbedding getChatChunkEmbeddingById(Long id) {
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
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<ChatChunkEmbedding> getChatChunkEmbeddingsByConversationId(Long conversationId) {
        String sql = "SELECT * FROM chat_chunk_embeddings WHERE conversation_id = ?";
        List<ChatChunkEmbedding> embeddings = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, conversationId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    embeddings.add(mapResultSetToChatChunkEmbedding(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return embeddings;
    }

    @Override
    public List<ChatChunkEmbedding> getChatChunkEmbeddingsByUserId(Long userId) {
        String sql = "SELECT * FROM chat_chunk_embeddings WHERE user_id = ?";
        List<ChatChunkEmbedding> embeddings = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    embeddings.add(mapResultSetToChatChunkEmbedding(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return embeddings;
    }

    @Override
    public void updateChatChunkEmbedding(ChatChunkEmbedding embedding) {
        String sql = """
                UPDATE chat_chunk_embeddings 
                SET chunk = ?, embedding = ?, last_accessed = ?, conversation_id = ?, user_id = ?, role = ?, reply_to_chunk_id = ? 
                WHERE id = ?;
                """;
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, embedding.getChunk());
            stmt.setArray(2, conn.createArrayOf("DOUBLE", embedding.getEmbedding().toArray()));
            stmt.setTimestamp(3, new Timestamp(embedding.getLastAccessed().getTime()));
            stmt.setObject(4, embedding.getConversationId());
            stmt.setObject(5, embedding.getUserId());
            stmt.setString(6, embedding.getRole());
            stmt.setObject(7, embedding.getReplyToChunkId());
            stmt.setLong(8, embedding.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteChatChunkEmbedding(Long id) {
        String sql = "DELETE FROM chat_chunk_embeddings WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<ChatChunkEmbedding> findEmbeddingsNear(ChatChunkEmbedding sourceEmbedding, int limit) {
        String sql = """
                WITH Similarities AS (
                    SELECT id, chunk, embedding, creation_date, last_accessed, conversation_id, user_id, role, reply_to_chunk_id,
                           COSINE_SIMILARITY(embedding, ?) AS similarity
                    FROM chat_chunk_embeddings
                )
                SELECT id, chunk, embedding, creation_date, last_accessed, conversation_id, user_id, role, reply_to_chunk_id
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
            stmt.setInt(2, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    similarEmbeddings.add(mapResultSetToChatChunkEmbedding(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle exceptions properly
        }
        return similarEmbeddings;
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
            e.printStackTrace(); // Handle exceptions properly
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
        embedding.setConversationId(rs.getLong("conversation_id"));
        embedding.setUserId(rs.getLong("user_id"));
        embedding.setRole(rs.getString("role"));
        embedding.setReplyToChunkId(rs.getLong("reply_to_chunk_id"));
        return embedding;
    }
}