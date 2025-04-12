package com.esgdev.amethystui.db;

import com.esgdev.amethystui.h2.VectorSimilarity;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * H2 implementation of TextEmbeddingDao for managing text embeddings in an H2 database.
 * Note: this is a compromise. H2 is not a vector database, but it can be used for
 * storing embeddings with some limitations. This is not a scalable solution, but
 * good for small projects or testing.
 * <p>
 * Hint: to browse the database, you can use the builtin tool, for example:
 * java -cp /opt/h2/bin/h2.jar org.h2.tools.Server -help
 */
public class TextEmbeddingDaoH2 implements TextEmbeddingDao {

    private final String jdbcUrl = "jdbc:h2:~/text_embeddings"; // Adjust as needed
    private final String jdbcUser = "sa";
    private final String jdbcPassword = "";

    public TextEmbeddingDaoH2() {
        initializeDatabase();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
    }

    private void initializeDatabase() {
        String createTableSQL = """
                CREATE TABLE IF NOT EXISTS embeddings (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    chunk VARCHAR NOT NULL,
                    embedding DOUBLE ARRAY NOT NULL,
                    creation_date TIMESTAMP NOT NULL,
                    last_accessed TIMESTAMP NOT NULL
                );
                """;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            registerVectorFunctions(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database schema", e);
        }
    }

    private static void registerVectorFunctions(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE ALIAS IF NOT EXISTS COSINE_SIMILARITY FOR \"" + VectorSimilarity.class.getName() + ".cosineSimilarity\"");
            statement.execute("CREATE ALIAS IF NOT EXISTS EUCLIDEAN_DISTANCE FOR \"" + VectorSimilarity.class.getName() + ".euclideanDistance\"");
        } catch (SQLException e) {
            throw new SQLException("Failed to register vector functions", e);
        }
    }

    @Override
    public TextEmbedding getEmbeddingById(long id) {
        String sql = "SELECT * FROM embeddings WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToEmbedding(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle exceptions properly
        }
        return null;
    }

    @Override
    public List<TextEmbedding> getAllEmbeddings() {
        String sql = "SELECT * FROM embeddings";
        List<TextEmbedding> embeddings = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                embeddings.add(mapResultSetToEmbedding(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle exceptions properly
        }
        return embeddings;
    }

    @Override
    public void addEmbedding(TextEmbedding embedding) {
        List<TextEmbedding> embeddings = new ArrayList<>();
        embeddings.add(embedding);
        addEmbedding(embeddings);
    }

    @Override
    public void addEmbedding(List<TextEmbedding> embeddings) {
        String sql = "INSERT INTO embeddings (chunk, embedding, creation_date, last_accessed) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (TextEmbedding embedding : embeddings) {
                stmt.setString(1, embedding.getChunk());
                stmt.setArray(2, conn.createArrayOf("DOUBLE", embedding.getEmbedding().toArray()));
                stmt.setTimestamp(3, new java.sql.Timestamp(embedding.getCreationDate().getTime()));
                stmt.setTimestamp(4, new java.sql.Timestamp(embedding.getLastAccessed().getTime()));
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace(); // Handle exceptions properly
        }
    }


    @Override
    public void updateEmbedding(TextEmbedding embedding) {
        String sql = "UPDATE embeddings SET chunk = ?, embedding = ?, last_accessed = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, embedding.getChunk());
            stmt.setArray(2, conn.createArrayOf("DOUBLE", embedding.getEmbedding().toArray()));
            stmt.setDate(3, new java.sql.Date(embedding.getLastAccessed().getTime()));
            stmt.setLong(4, embedding.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); // Handle exceptions properly
        }
    }

    @Override
    public void deleteEmbedding(long id) {
        String sql = "DELETE FROM embeddings WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); // Handle exceptions properly
        }
    }

    @Override
    public List<TextEmbedding> findEmbeddingsNear(TextEmbedding sourceEmbedding, int limit) {
        String sql = """
                WITH Similarities AS (
                    SELECT id, chunk, embedding, creation_date, last_accessed,
                           COSINE_SIMILARITY(embedding, ?) AS similarity
                    FROM embeddings
                )
                SELECT id, chunk, embedding, creation_date, last_accessed
                FROM Similarities
                WHERE similarity IS NOT NULL
                ORDER BY similarity DESC
                LIMIT ?;
                """;

        List<TextEmbedding> similarEmbeddings = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Convert targetEmbedding to SQL array
            Array embeddingArray = conn.createArrayOf("DOUBLE", sourceEmbedding.getEmbedding().toArray());
            stmt.setObject(1, embeddingArray);
            stmt.setInt(2, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    similarEmbeddings.add(mapResultSetToEmbedding(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle exceptions properly
        }
        return similarEmbeddings;
    }

    private TextEmbedding mapResultSetToEmbedding(ResultSet rs) throws SQLException {
        TextEmbedding embedding = new TextEmbedding();
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
        } else {
            embedding.setEmbedding(new ArrayList<>());
        }
      embedding.setCreationDate(rs.getTimestamp("creation_date"));
      embedding.setLastAccessed(rs.getTimestamp("last_accessed"));

      return embedding;
    }
}