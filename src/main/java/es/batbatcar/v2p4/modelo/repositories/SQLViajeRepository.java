package es.batbatcar.v2p4.modelo.repositories;

import es.batbatcar.v2p4.exceptions.*;
import es.batbatcar.v2p4.modelo.dao.sqldao.SQLReservaDAO;
import es.batbatcar.v2p4.modelo.dao.sqldao.SQLViajeDAO;
import es.batbatcar.v2p4.modelo.dto.Reserva;
import es.batbatcar.v2p4.modelo.dto.viaje.EstadoViaje;
import es.batbatcar.v2p4.modelo.dto.viaje.Viaje;
import es.batbatcar.v2p4.modelo.dao.interfaces.ReservaDAO;
import es.batbatcar.v2p4.modelo.dao.interfaces.ViajeDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
public class SQLViajeRepository {

    private final ViajeDAO viajeDAO;
    private final ReservaDAO reservaDAO;

    public SQLViajeRepository(@Autowired SQLViajeDAO viajeDAO, @Autowired SQLReservaDAO reservaDAO) {
        this.viajeDAO = viajeDAO;
        this.reservaDAO = reservaDAO;
    }

    public Set<Viaje> findAll() {
        Set<Viaje> viajes = viajeDAO.findAll();

        for (Viaje viaje : viajes) {
            if (!this.reservaDAO.findAllByTravel(viaje).isEmpty()) {
                viaje.setSeHanRealizadoReservas(true);
            }
        }
        return viajes;
    }

    public Viaje findById(int codViaje) {
        return viajeDAO.findById(codViaje);
    }

    public void cancelarViaje(int codViaje) {
        Viaje viaje = viajeDAO.findById(codViaje);
        try {
            viaje.setEstadoViaje(EstadoViaje.CANCELADO);
            viajeDAO.update(viaje);
        } catch (ViajeNotFoundException e) {
            throw new RuntimeException("Error al cancelar el viaje", e);
        }
    }

    public List<Reserva> findReservasByViaje(Viaje viaje) {
        return reservaDAO.findAllByTravel(viaje);
    }

    public boolean verificarEstado(Viaje viaje) {
        if (viaje.getEstadoViaje().equals(EstadoViaje.ABIERTO)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean sePuedeReservar(Viaje viaje) {

        boolean estaAbierto = viaje.getEstadoViaje().equals(EstadoViaje.ABIERTO);

        int plazasReservadas = 0;
        List<Reserva> reservas = reservaDAO.findAllByTravel(viaje);
        for (Reserva reserva : reservas) {
            plazasReservadas += reserva.getPlazasSolicitadas();
        }

        boolean quedanPlazas = viaje.getPlazasOfertadas() > plazasReservadas;
        boolean noHaSalido = LocalDateTime.now().isBefore(viaje.getFechaSalida());

        return estaAbierto && quedanPlazas && noHaSalido;
    }


    public Reserva findByCodigoReserva(String codigoReserva) {
        try {
            return reservaDAO.getById(codigoReserva);
        } catch (Exception ex) {
            throw new RuntimeException("Error al buscar la reserva: ", ex);
        }
    }

    public void cancelarReserva(String codigoReserva) {

        try {
            Reserva reserva = reservaDAO.getById(codigoReserva);
            reservaDAO.remove(reserva);
        } catch (ReservaNotFoundException e) {
            throw new RuntimeException("Error al cancelar la reserva", e);
        }
    }

    public Set findByCiudad(String ciudad) {
        return viajeDAO.findAll(ciudad);
    }

    public List<Reserva> findReservasByViajeID(int codViaje) {
        Viaje viaje = viajeDAO.findById(codViaje);
        return reservaDAO.findAllByTravel(viaje);
    }

    public Viaje findViajeSiPermiteReserva(int codViaje, String usuario, int plazasSolicitadas) throws Exception {
        Viaje viaje = viajeDAO.findById(codViaje);
        if (viaje == null) {
            throw new ViajeNotFoundException("El viaje no existe.");
        }
        if (usuario.equals(viaje.getPropietario())) {
            throw new Exception("El usuario de la reserva no puede ser el propietario del viaje.");
        }
        if (viaje.getEstado() != EstadoViaje.ABIERTO) {
            throw new Exception("El viaje no está abierto.");
        }
        if (viaje.isCancelado()) {
            throw new Exception("El viaje está cancelado.");
        }

        List<Reserva> reservas = reservaDAO.findAllByTravel(viaje);
        int plazasReservadas = 0;
        for (Reserva reserva : reservas) {
            plazasReservadas += reserva.getPlazasSolicitadas();
        }

        if (viaje.getPlazasOfertadas() - plazasReservadas < plazasSolicitadas) {
            throw new Exception("No hay espacio en el viaje para tantas plazas o ya se han acabado las plazas. Vuelve a consultar los detalles del viaje.");
        }

        for (Reserva reserva : reservas) {
            if (reserva.getUsuario().equals(usuario)) {
                throw new Exception("El usuario ya ha realizado una reserva en este viaje.");
            }
        }
        return viaje;
    }

    public void add(Reserva reserva) throws ReservaAlreadyExistsException{
        try {
        reservaDAO.add(reserva);
        } catch (Exception e) {
            throw new ReservaAlreadyExistsException(reserva);
        }
        }

    public int getNextCodViaje() {
        return this.viajeDAO.findAll().size() + 1;
    }

    public void save(Viaje viaje) throws ViajeAlreadyExistsException, ViajeNotFoundException {

        if (viajeDAO.findById(viaje.getCodViaje()) == null) {
            viajeDAO.add(viaje);
        } else {
            viajeDAO.update(viaje);
        }
    }
}