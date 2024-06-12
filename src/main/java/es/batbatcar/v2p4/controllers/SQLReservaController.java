package es.batbatcar.v2p4.controllers;

import es.batbatcar.v2p4.modelo.dto.Reserva;
import es.batbatcar.v2p4.modelo.dto.viaje.Viaje;
import es.batbatcar.v2p4.modelo.repositories.SQLViajeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;


@Controller
public class SQLReservaController {
    private final SQLViajeRepository viajeRepository;

    @Autowired
    public SQLReservaController(SQLViajeRepository viajeRepository) {
        this.viajeRepository = viajeRepository;
    }


    @GetMapping("/reserva/detalles")
    public String detallesReserva(@RequestParam("codReserva") String codigoReserva, @RequestParam("origen") String origen, Model model) {
        Reserva reserva = viajeRepository.findByCodigoReserva(codigoReserva);
        Viaje viaje = viajeRepository.findById(Integer.parseInt(codigoReserva.substring(0, 1)));
        model.addAttribute("reserva", reserva);
        model.addAttribute("viaje", viaje);
        model.addAttribute("origen", origen);
        return "reserva/reserva_detalles_form";
    }


    @GetMapping("/reserva/cancelar")
    public String cancelarReserva(@RequestParam("codReserva") String codigoReserva, RedirectAttributes redirectAttributes) {
        int codViaje = Integer.parseInt(codigoReserva.substring(0, 1));
        viajeRepository.cancelarReserva(codigoReserva);

        redirectAttributes.addFlashAttribute("message", "Reserva cancelada!");


        return "redirect:/viaje?codViaje=" + codViaje;
    }

    @GetMapping("/viaje/reservas")
    public String verReservas(@RequestParam int codViaje, Model model) {
        List<Reserva> reservas = viajeRepository.findReservasByViajeID(codViaje);
        model.addAttribute("reservas", reservas);
        model.addAttribute("codViaje", codViaje);
        return "reserva/listado";
    }

    @GetMapping("viaje/reserva/add")
    public String addReservaForm(@RequestParam("codViaje") int codViaje, Model model) {
        try {
            Viaje viaje = viajeRepository.findViajeSiPermiteReserva(codViaje, "", 0);
            model.addAttribute("codViaje", codViaje);
            return "reserva/reserva_form";
        } catch (Exception ex) {
            return "redirect:/viaje/listado";
        }
    }

    @PostMapping("viaje/reserva/addAction")
    public String addReservaAction(@RequestParam("codViaje") int codViaje, @RequestParam("usuario") String usuario, @RequestParam("plazasSolicitadas") int plazasSolicitadas, RedirectAttributes redirectAttributes) {
        try {
            Viaje viaje = viajeRepository.findViajeSiPermiteReserva(codViaje, usuario, plazasSolicitadas);
            List<Reserva> reservas = viajeRepository.findReservasByViaje(viaje);
            int nextReservaCode = reservas.isEmpty() ? 1 : Integer.parseInt(reservas.get(reservas.size() - 1).getCodigoReserva().split("-")[1]) + 1;
            String codigoReserva = codViaje + "-" + nextReservaCode;
            Reserva reserva = new Reserva(codigoReserva, usuario, plazasSolicitadas, viaje);
            viajeRepository.add(reserva);
            redirectAttributes.addFlashAttribute("mensaje", "Reserva añadida con éxito");
            return "redirect:/viajes";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Error al añadir la reserva: " + ex.getMessage());
            return "redirect:/viaje/reserva/add?codViaje=" + codViaje;
        }
    }


}