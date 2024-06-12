package es.batbatcar.v2p4.modelo.dao.sqldao;


import es.batbatcar.v2p4.exceptions.ViajeNotFoundException;
import es.batbatcar.v2p4.modelo.services.MySQLConnection;
import es.batbatcar.v2p4.modelo.dao.interfaces.ViajeDAO;
import es.batbatcar.v2p4.modelo.dto.viaje.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

@Repository
public class SQLViajeDAO implements ViajeDAO {

    @Autowired
    private MySQLConnection mySQLConnection;

    @Override
    public Set<Viaje> findAll() {
        Set<Viaje> viajes = new HashSet<>();
        String sql = "SELECT * FROM viajes";
        Connection connection = mySQLConnection.getConnection();
        try (

                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)
        ) {
            while (resultSet.next()) {
                Viaje viaje = new Viaje();
                viaje.setCodViaje(resultSet.getInt("codViaje"));
                viaje.setPropietario(resultSet.getString("propietario"));
                viaje.setRuta(resultSet.getString("ruta"));
                viaje.setFechaSalida(resultSet.getTimestamp("fechaSalida").toLocalDateTime());
                viaje.setDuracion(resultSet.getLong("duracion"));
                viaje.setPrecio(resultSet.getFloat("precio"));
                viaje.setPlazas(resultSet.getInt("plazasOfertadas"));
                viaje.setEstadoViaje(EstadoViaje.valueOf(resultSet.getString("estadoViaje")));

                viajes.add(viaje);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener todos los viajes", e);
        }

        return viajes;
    }

    @Override
    public Set<Viaje> findAll(String city) {
        Set<Viaje> viajes = new HashSet<>();
        String sql = "SELECT * FROM viajes WHERE ruta LIKE ?";
        Connection connection = mySQLConnection.getConnection();
        try (
                PreparedStatement statement = connection.prepareStatement(sql);
        ) {
            statement.setString(1, "%" + city + "%");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Viaje viaje = new Viaje();
                viaje.setCodViaje(resultSet.getInt("codViaje"));
                viaje.setPropietario(resultSet.getString("propietario"));
                viaje.setRuta(resultSet.getString("ruta"));
                viaje.setFechaSalida(resultSet.getTimestamp("fechaSalida").toLocalDateTime());
                viaje.setDuracion(resultSet.getLong("duracion"));
                viaje.setPrecio(resultSet.getFloat("precio"));
                viaje.setPlazas(resultSet.getInt("plazasOfertadas"));
                viaje.setEstadoViaje(EstadoViaje.valueOf(resultSet.getString("estadoViaje")));

                viajes.add(viaje);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener los viajes por ciudad", e);
        }

        return viajes;
    }

    @Override
    public Set<Viaje> findAll(EstadoViaje estadoViaje) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public Set<Viaje> findAll(Class<? extends Viaje> viajeClass) {
        throw new RuntimeException("Not yet implemented");
    }

    // En SQLViajeDAO.java
    @Override
    public Viaje findById(int codViaje) {
        Viaje viaje = null;
        String sql = "SELECT * FROM viajes WHERE codViaje = ?";
        Connection connection = mySQLConnection.getConnection();
        try (

                PreparedStatement statement = connection.prepareStatement(sql);
        ) {
            statement.setInt(1, codViaje);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                viaje = new Viaje();
                viaje.setCodViaje(resultSet.getInt("codViaje"));
                viaje.setPropietario(resultSet.getString("propietario"));
                viaje.setRuta(resultSet.getString("ruta"));
                viaje.setFechaSalida(resultSet.getTimestamp("fechaSalida").toLocalDateTime());
                viaje.setDuracion(resultSet.getLong("duracion"));
                viaje.setPrecio(resultSet.getFloat("precio"));
                viaje.setPlazas(resultSet.getInt("plazasOfertadas"));
                viaje.setEstadoViaje(EstadoViaje.valueOf(resultSet.getString("estadoViaje")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener el viaje", e);
        }

        return viaje;
    }

    @Override
    public Viaje getById(int codViaje) throws ViajeNotFoundException {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void add(Viaje viaje) {
        String sql = "INSERT INTO viajes (codViaje, propietario, ruta, fechaSalida, duracion, precio, plazasOfertadas, estadoViaje) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection connection = mySQLConnection.getConnection();
        try (
                PreparedStatement statement = connection.prepareStatement(sql);
        ) {
            statement.setInt(1, viaje.getCodViaje());
            statement.setString(2, viaje.getPropietario());
            statement.setString(3, viaje.getRuta());
            statement.setTimestamp(4, Timestamp.valueOf(viaje.getFechaSalida()));
            statement.setLong(5, viaje.getDuracion());
            statement.setFloat(6, viaje.getPrecio());
            statement.setInt(7, viaje.getPlazas());
            statement.setString(8, viaje.getEstadoViaje().name());

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Error al agregar el viaje");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al agregar el viaje", e);
        }
    }

    @Override
    public void update(Viaje viaje) throws ViajeNotFoundException {
        String sql = "UPDATE viajes SET propietario = ?, ruta = ?, fechaSalida = ?, duracion = ?, precio = ?, plazasOfertadas = ?, estadoViaje = ? WHERE codViaje = ?";
        Connection connection = mySQLConnection.getConnection();
        try (

                PreparedStatement statement = connection.prepareStatement(sql);
        ) {
            statement.setString(1, viaje.getPropietario());
            statement.setString(2, viaje.getRuta());
            statement.setTimestamp(3, Timestamp.valueOf(viaje.getFechaSalida()));
            statement.setLong(4, viaje.getDuracion());
            statement.setFloat(5, viaje.getPrecio());
            statement.setInt(6, viaje.getPlazas());
            statement.setString(7, viaje.getEstadoViaje().name());
            statement.setInt(8, viaje.getCodViaje());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new ViajeNotFoundException("No se pudo actualizar el viaje, no se encontró el viaje con el ID: " + viaje.getCodViaje());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el viaje", e);
        }
    }

    @Override
    public void remove(Viaje viaje) throws ViajeNotFoundException {
        throw new RuntimeException("Not yet implemented");
    }
}
