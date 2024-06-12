package es.batbatcar.v2p4.controllers;

import es.batbatcar.v2p4.exceptions.InvalidNumberOfPlacesException;
import es.batbatcar.v2p4.exceptions.ReservaAlreadyExistsException;
import es.batbatcar.v2p4.exceptions.ReservaNotFoundException;
import es.batbatcar.v2p4.exceptions.ViajeNotFoundException;
import es.batbatcar.v2p4.modelo.dto.Reserva;
import es.batbatcar.v2p4.modelo.dto.viaje.Viaje;
import es.batbatcar.v2p4.modelo.repositories.ViajesRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
public class ReservaController {

    @Autowired
    private ViajesRepository viajesRepository;


    @GetMapping("viaje/reserva/add")
    public String addReservaForm(@RequestParam("codViaje") int codViaje, Model model) {
        try {
            Viaje viaje = viajesRepository.findViajeSiPermiteReserva(codViaje, "", 0);
            model.addAttribute("codViaje", codViaje);
            return "reserva/reserva_form";
        } catch (Exception ex) {
            return "redirect:/viaje/listado";
        }
    }

    @PostMapping("viaje/reserva/addAction")
    public String addReservaAction(@RequestParam("codViaje") int codViaje, @RequestParam("usuario") String usuario, @RequestParam("plazasSolicitadas") int plazasSolicitadas, RedirectAttributes redirectAttributes) {
        try {
            Viaje viaje = viajesRepository.findViajeSiPermiteReserva(codViaje, usuario, plazasSolicitadas);
            List<Reserva> reservas = viajesRepository.findReservasByViaje(viaje);
            int nextReservaCode = reservas.isEmpty() ? 1 : Integer.parseInt(reservas.get(reservas.size() - 1).getCodigoReserva().split("-")[1]) + 1;
            String codigoReserva = codViaje + "-" + nextReservaCode;
            Reserva reserva = new Reserva(codigoReserva, usuario, plazasSolicitadas, viaje);
            viajesRepository.save(reserva);
            redirectAttributes.addFlashAttribute("mensaje", "Reserva añadida con éxito");
            return "redirect:/viajes";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Error al añadir la reserva: " + ex.getMessage());
            return "redirect:/viaje/reserva/add?codViaje=" + codViaje;
        }
    }

    @GetMapping("viaje/reservas")
    public String listadoReservas(@RequestParam("codViaje") int codViaje, Model model) {
        try {
            Viaje viaje = viajesRepository.getViajeById(codViaje);
            model.addAttribute("codViaje", codViaje);
            model.addAttribute("reservas", viajesRepository.getReservasById(viaje));
            return "reserva/listado";
        } catch (ViajeNotFoundException ex) {
            return "redirect:/viaje/listado";
        }
    }


}
