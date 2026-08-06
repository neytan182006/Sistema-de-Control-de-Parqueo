package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;

public class ParqueoDAO {

    private static final double TARIFA_POR_HORA = 800; // colones

    public void listarEspacios() throws SQLException {
        String sql = "SELECT IdEspacio, Numero, Ocupado FROM ESPACIOS ORDER BY Numero";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                System.out.printf("[%d] Espacio %d - %s%n",
                        rs.getInt("IdEspacio"), rs.getInt("Numero"), rs.getBoolean("Ocupado") ? "OCUPADO" : "LIBRE");
            }
        }
    }

    public String registrarEntrada(int idEspacio, String placa) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                String sqlOcupar = "UPDATE ESPACIOS SET Ocupado = TRUE WHERE IdEspacio = ? AND Ocupado = FALSE";
                boolean ok;
                try (PreparedStatement ps = con.prepareStatement(sqlOcupar)) {
                    ps.setInt(1, idEspacio);
                    ok = ps.executeUpdate() > 0;
                }
                if (!ok) {
                    con.rollback();
                    return "Ese espacio ya esta ocupado.";
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO REGISTROS_PARQUEO (IdEspacio, Placa) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, idEspacio);
                    ps.setString(2, placa);
                    ps.executeUpdate();
                    try (ResultSet generadas = ps.getGeneratedKeys()) {
                        generadas.next();
                        con.commit();
                        return "Entrada registrada. Id de registro: " + generadas.getInt(1);
                    }
                }
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public String registrarSalida(int idRegistro) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                int idEspacio;
                LocalDateTime horaEntrada;

                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT IdEspacio, HoraEntrada FROM REGISTROS_PARQUEO WHERE IdRegistro = ? AND HoraSalida IS NULL")) {
                    ps.setInt(1, idRegistro);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            return "No se encontro un registro activo con ese id.";
                        }
                        idEspacio = rs.getInt("IdEspacio");
                        horaEntrada = rs.getTimestamp("HoraEntrada").toLocalDateTime();
                    }
                }

                LocalDateTime ahora = LocalDateTime.now();
                double horas = Math.max(1, Math.ceil(Duration.between(horaEntrada, ahora).toMinutes() / 60.0));
                double monto = horas * TARIFA_POR_HORA;

                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE REGISTROS_PARQUEO SET HoraSalida = ?, Monto = ? WHERE IdRegistro = ?")) {
                    ps.setTimestamp(1, Timestamp.valueOf(ahora));
                    ps.setDouble(2, monto);
                    ps.setInt(3, idRegistro);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement("UPDATE ESPACIOS SET Ocupado = FALSE WHERE IdEspacio = ?")) {
                    ps.setInt(1, idEspacio);
                    ps.executeUpdate();
                }

                con.commit();
                return String.format("Salida registrada. Tiempo: %.0f hora(s). Monto a pagar: $%.2f", horas, monto);
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }
}
