package com.salalivre.api.repository;

import com.salalivre.api.model.Sala;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Repository
public class SalaRepository {

    private final Connection connection;

    public SalaRepository(Connection connection) {
        this.connection = connection;
    }

    public List<Sala> listarTodas() {
        List<Sala> salas = new ArrayList<>();
        String sql = "SELECT * FROM salas";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Sala sala = new Sala(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("bloco"),
                        rs.getInt("capacidade"),
                        rs.getBoolean("tem_projetor"),
                        rs.getBoolean("ativa")
                );
                salas.add(sala);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar salas.", e);
        }

        return salas;
    }

    public Sala buscarPorId(Integer id) {
        String sql = "SELECT * FROM salas WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Sala(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getString("bloco"),
                            rs.getInt("capacidade"),
                            rs.getBoolean("tem_projetor"),
                            rs.getBoolean("ativa")
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar sala por id.", e);
        }

        return null;
    }

    public Sala salvar(Sala sala) {
        String sql = "INSERT INTO salas (nome, bloco, capacidade, tem_projetor, ativa) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, sala.getNome());
            stmt.setString(2, sala.getBloco());
            stmt.setInt(3, sala.getCapacidade());
            stmt.setBoolean(4, sala.getTemProjetor());
            stmt.setBoolean(5, sala.getAtiva());

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                sala.setId(rs.getInt(1));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar sala.", e);
        }

        return sala;
    }

    public Sala atualizar(Integer id, Sala sala) {
        String sql = "UPDATE salas SET nome = ?, bloco = ?, capacidade = ?, tem_projetor = ?, ativa = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, sala.getNome());
            stmt.setString(2, sala.getBloco());
            stmt.setInt(3, sala.getCapacidade());
            stmt.setBoolean(4, sala.getTemProjetor());
            stmt.setBoolean(5, sala.getAtiva());
            stmt.setInt(6, id);

            stmt.executeUpdate();
            sala.setId(id);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar sala.", e);
        }

        return sala;
    }

    public void deletar(Integer id) {
        String sql = "DELETE FROM salas WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar sala.", e);
        }
    }
}