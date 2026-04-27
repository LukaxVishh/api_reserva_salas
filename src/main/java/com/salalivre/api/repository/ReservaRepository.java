package com.salalivre.api.repository;

import com.salalivre.api.model.Reserva;
import com.salalivre.api.model.StatusReserva;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ReservaRepository {

    private final Connection connection;

    public ReservaRepository(Connection connection) {
        this.connection = connection;
    }

    public List<Reserva> listarTodas() {
        List<Reserva> reservas = new ArrayList<>();
        String sql = "SELECT * FROM reservas";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                reservas.add(mapear(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar reservas.", e);
        }

        return reservas;
    }

    public Reserva buscarPorId(Integer id) {
        String sql = "SELECT * FROM reservas WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar reserva por id.", e);
        }

        return null;
    }

    public List<Reserva> listarPorSala(Integer salaId) {
        List<Reserva> reservas = new ArrayList<>();
        String sql = "SELECT * FROM reservas WHERE sala_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, salaId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reservas.add(mapear(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar reservas da sala.", e);
        }

        return reservas;
    }

    public List<Reserva> buscarConflitos(Integer salaId, LocalDate data, LocalTime horaInicio,
                                         LocalTime horaFim, Integer idIgnorar) {
        List<Reserva> reservas = new ArrayList<>();
        String sql = "SELECT * FROM reservas " +
                "WHERE sala_id = ? AND data_reserva = ? " +
                "AND hora_inicio < ? AND hora_fim > ? " +
                "AND (? IS NULL OR id <> ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, salaId);
            stmt.setDate(2, Date.valueOf(data));
            stmt.setTime(3, Time.valueOf(horaFim));
            stmt.setTime(4, Time.valueOf(horaInicio));

            if (idIgnorar == null) {
                stmt.setNull(5, java.sql.Types.INTEGER);
                stmt.setNull(6, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(5, idIgnorar);
                stmt.setInt(6, idIgnorar);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reservas.add(mapear(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar conflitos de reserva.", e);
        }

        return reservas;
    }

    public Reserva salvar(Reserva reserva) {
        String sql = "INSERT INTO reservas (nome_responsavel, descricao, data_reserva, hora_inicio, hora_fim, status, sala_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, reserva.getNomeResponsavel());
            stmt.setString(2, reserva.getDescricao());
            stmt.setDate(3, Date.valueOf(reserva.getData()));
            stmt.setTime(4, Time.valueOf(reserva.getHoraInicio()));
            stmt.setTime(5, Time.valueOf(reserva.getHoraFim()));
            stmt.setString(6, reserva.getStatus().name());
            stmt.setInt(7, reserva.getSalaId());

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                reserva.setId(rs.getInt(1));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar reserva.", e);
        }

        return reserva;
    }

    public Reserva atualizar(Integer id, Reserva reserva) {
        String sql = "UPDATE reservas SET nome_responsavel = ?, descricao = ?, data_reserva = ?, " +
                "hora_inicio = ?, hora_fim = ?, status = ?, sala_id = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, reserva.getNomeResponsavel());
            stmt.setString(2, reserva.getDescricao());
            stmt.setDate(3, Date.valueOf(reserva.getData()));
            stmt.setTime(4, Time.valueOf(reserva.getHoraInicio()));
            stmt.setTime(5, Time.valueOf(reserva.getHoraFim()));
            stmt.setString(6, reserva.getStatus().name());
            stmt.setInt(7, reserva.getSalaId());
            stmt.setInt(8, id);

            stmt.executeUpdate();
            reserva.setId(id);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar reserva.", e);
        }

        return reserva;
    }

    public void deletar(Integer id) {
        String sql = "DELETE FROM reservas WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar reserva.", e);
        }
    }

    private Reserva mapear(ResultSet rs) throws SQLException {
        return new Reserva(
                rs.getInt("id"),
                rs.getString("nome_responsavel"),
                rs.getString("descricao"),
                rs.getDate("data_reserva").toLocalDate(),
                rs.getTime("hora_inicio").toLocalTime(),
                rs.getTime("hora_fim").toLocalTime(),
                StatusReserva.valueOf(rs.getString("status")),
                rs.getInt("sala_id")
        );
    }
}
