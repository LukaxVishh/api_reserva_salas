package com.salalivre.api.repository;

import com.salalivre.api.model.Sala;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
}