package com.salalivre.api.repository;

import com.salalivre.api.model.EventoSistema;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Repository
public class EventoSistemaRepository {

    private final Connection connection;

    public EventoSistemaRepository(Connection connection) {
        this.connection = connection;
    }

    public EventoSistema salvar(EventoSistema evento) {
        String sql = "INSERT INTO eventos_sistema (tipo, payload) VALUES (?, ?)";

        // A Connection injetada e compartilhada entre threads HTTP e o consumer do RabbitMQ.
        // Sincronizamos para evitar uso concorrente do mesmo java.sql.Connection.
        synchronized (connection) {
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, evento.getTipo());
                stmt.setString(2, evento.getPayload());
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        evento.setId(rs.getInt(1));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Erro ao salvar evento do sistema.", e);
            }
        }

        return evento;
    }
}
