package es.batbatcar.v2p4.modelo.repositories;

import es.batbatcar.v2p4.exceptions.*;
import es.batbatcar.v2p4.modelo.dao.inmemorydao.InMemoryReservaDAO;
import es.batbatcar.v2p4.modelo.dao.inmemorydao.InMemoryViajeDAO;
import es.batbatcar.v2p4.modelo.dto.Reserva;
import es.batbatcar.v2p4.modelo.dto.viaje.EstadoViaje;
import es.batbatcar.v2p4.modelo.dto.viaje.Viaje;
import es.batbatcar.v2p4.modelo.dao.interfaces.ReservaDAO;
import es.batbatcar.v2p4.modelo.dao.interfaces.ViajeDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public class ViajesRepository {

    private final ViajeDAO viajeDAO;
    private final ReservaDAO reservaDAO;

    public ViajesRepository(@Autowired InMemoryViajeDAO viajeDAO, @Autowired InMemoryReservaDAO reservaDAO) {
        this.viajeDAO = viajeDAO;
        this.reservaDAO = reservaDAO;
    }

    /**
     * Obtiene un conjunto de todos los viajes
     *
     * @return
     */
    public Set<Viaje> findAll() {

        // Se recuperan todos los viajes del DAO de viajes
        Set<Viaje> viajes = viajeDAO.findAll();

        // Se completa la información acerca de las reservas de cada viaje a través del DAO de reservas
        for (Viaje viaje : viajes) {
            if (this.reservaDAO.findAllByTravel(viaje).size() > 0) {
                viaje.setSeHanRealizadoReservas(true);
            }
        }
        return viajes;
    }

    /**
     * Obtiene el código del siguiente viaje
     *
     * @return
     */
    public int getNextCodViaje() {
        return this.viajeDAO.findAll().size() + 1;
    }

    /**
     * Guarda el viaje (actualiza si ya existe o añade si no existe)
     *
     * @param viaje
     * @throws ViajeAlreadyExistsException
     * @throws ViajeNotFoundException
     */
    public void save(Viaje viaje) throws ViajeAlreadyExistsException, ViajeNotFoundException {

        if (viajeDAO.findById(viaje.getCodViaje()) == null) {
            viajeDAO.add(viaje);
        } else {
            viajeDAO.update(viaje);
        }
    }

    /**
     * Encuentra todas las reservas de @viaje
     *
     * @param viaje
     * @return
     */
    public List<Reserva> findReservasByViaje(Viaje viaje) {
        return reservaDAO.findAllByTravel(viaje);
    }

    /**
     * Guarda la reserva
     *
     * @param reserva
     * @throws ReservaAlreadyExistsException
     * @throws ReservaNotFoundException
     */
    public void save(Reserva reserva) throws ReservaAlreadyExistsException, ReservaNotFoundException {

        if (reservaDAO.findById(reserva.getCodigoReserva()) == null) {
            reservaDAO.add(reserva);
        } else {
            reservaDAO.update(reserva);
        }
    }

    /**
     * Elimina la reserva
     *
     * @param reserva
     * @throws ReservaNotFoundException
     */
    public void remove(Reserva reserva) throws ReservaNotFoundException {
        reservaDAO.remove(reserva);
    }

    public Viaje getViajeById(int codViaje) throws ViajeNotFoundException {
        Viaje viaje = viajeDAO.getById(codViaje);
        if (viaje == null) {
            throw new ViajeNotFoundException("El viaje no existe.");
        }
        return viaje;
    }

    public List<Reserva> getReservasById(Viaje viaje) throws ViajeNotFoundException {
        return reservaDAO.findAllByTravel(viaje);
    }

   public Viaje findViajeSiPermiteReserva(int codViaje, String usuario, int plazasSolicitadas) throws Exception {
    Viaje viaje = viajeDAO.getById(codViaje);
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



}
