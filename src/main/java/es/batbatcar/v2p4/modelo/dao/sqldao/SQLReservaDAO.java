package es.batbatcar.v2p4.modelo.dao.sqldao;

import es.batbatcar.v2p4.exceptions.ReservaAlreadyExistsException;
import es.batbatcar.v2p4.exceptions.ReservaNotFoundException;
import es.batbatcar.v2p4.modelo.dto.Reserva;
import es.batbatcar.v2p4.modelo.dto.viaje.Viaje;
import es.batbatcar.v2p4.modelo.services.MySQLConnection;
import es.batbatcar.v2p4.modelo.dao.interfaces.ReservaDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Repository
public class SQLReservaDAO implements ReservaDAO {

    @Autowired
    private MySQLConnection mySQLConnection;

    @Override
    public Set<Reserva> findAll() {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public Reserva findById(String id) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public ArrayList<Reserva> findAllByUser(String user) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public ArrayList<Reserva> findAllByTravel(Viaje viaje) {
        ArrayList<Reserva> reservas = new ArrayList<>();
        String sql = "SELECT * FROM reservas WHERE viaje = ?";
        Connection connection = mySQLConnection.getConnection();
        try (

                PreparedStatement statement = connection.prepareStatement(sql);
        ) {
            statement.setInt(1, viaje.getCodViaje());
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Reserva reserva = new Reserva();
                reserva.setCodigoReserva(resultSet.getString("codigoReserva"));
                reserva.setUsuario(resultSet.getString("usuario"));
                reserva.setPlazasSolicitadas(resultSet.getInt("plazasSolicitadas"));
                reserva.setFechaRealizacion(resultSet.getTimestamp("fechaRealizacion").toLocalDateTime());
                reserva.setViaje(viaje);

                reservas.add(reserva);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener todas las reservas para el viaje", e);
        }

        return reservas;
    }

    @Override
    public Reserva getById(String id) throws ReservaNotFoundException {
        String sql = "SELECT * FROM reservas WHERE codigoReserva = ?";
        Connection connection = mySQLConnection.getConnection();
        try (
                PreparedStatement statement = connection.prepareStatement(sql);
        ) {
            statement.setString(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Reserva reserva = new Reserva();
                reserva.setCodigoReserva(resultSet.getString("codigoReserva"));
                reserva.setUsuario(resultSet.getString("usuario"));
                reserva.setPlazasSolicitadas(resultSet.getInt("plazasSolicitadas"));
                reserva.setFechaRealizacion(resultSet.getTimestamp("fechaRealizacion").toLocalDateTime());
                return reserva;
            } else {
                throw new ReservaNotFoundException("No se encontró ninguna reserva con el ID: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener la reserva", e);
        }
    }

    @Override
    public List<Reserva> findAllBySearchParams(Viaje viaje, String searchParams) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void add(Reserva reserva) throws ReservaAlreadyExistsException {
        String sql = "INSERT INTO reservas (codigoReserva, usuario, plazasSolicitadas, fechaRealizacion, viaje) VALUES (?, ?, ?, ?, ?)";
        Connection connection = mySQLConnection.getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, reserva.getCodigoReserva());
            statement.setString(2, reserva.getUsuario());
            statement.setInt(3, reserva.getPlazasSolicitadas());
            statement.setTimestamp(4, Timestamp.valueOf(reserva.getFechaRealizacion()));
            statement.setInt(5, reserva.getCodigoViaje());
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new ReservaAlreadyExistsException(reserva);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al agregar la reserva", e);
        }
    }

    @Override
    public void update(Reserva reserva) throws ReservaNotFoundException {
        throw new RuntimeException("Not yet implemented");

    }

    @Override
    public void remove(Reserva reserva) throws ReservaNotFoundException {
        String sql = "DELETE FROM reservas WHERE codigoReserva = ?";
        Connection connection = mySQLConnection.getConnection();
        try (
                PreparedStatement statement = connection.prepareStatement(sql);
        ) {
            statement.setString(1, reserva.getCodigoReserva());
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 0) {
                throw new ReservaNotFoundException("No se encontró ninguna reserva con el ID: " + reserva.getCodigoReserva());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar la reserva", e);
        }
    }

    @Override
    public int getNumPlazasReservadasEnViaje(Viaje viaje) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public Reserva findByUserInTravel(String usuario, Viaje viaje) {
        throw new RuntimeException("Not yet implemented");
    }
}
