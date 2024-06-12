package es.batbatcar.v2p4.controllers;

import es.batbatcar.v2p4.modelo.dto.viaje.*;
import es.batbatcar.v2p4.modelo.dto.Reserva;
import es.batbatcar.v2p4.modelo.repositories.SQLViajeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

@Controller
public class SQLViajesController {

    private final SQLViajeRepository viajeRepository;

    @Autowired
    public SQLViajesController(SQLViajeRepository viajeRepository) {
        this.viajeRepository = viajeRepository;
    }

    @GetMapping("/viajes")
    public String listado(@RequestParam(required = false) String ciudad, Model model) {
        Set<Viaje> viajes;
        if (ciudad != null) {
            viajes = viajeRepository.findByCiudad(ciudad);
        } else {
            viajes = viajeRepository.findAll();
        }
        model.addAttribute("viajes", viajes);
        return "viaje/listado";
    }

    @GetMapping("/viaje")
    public String getViaje(@RequestParam("codViaje") int id, Model model) {
        Viaje viaje = viajeRepository.findById(id);
        List<Reserva> reservas = viajeRepository.findReservasByViaje(viaje);
        boolean esViajeAbierto = viajeRepository.verificarEstado(viaje);
        boolean sePuedeReservar = viajeRepository.sePuedeReservar(viaje);
        model.addAttribute("viaje", viaje);
        model.addAttribute("reservas", reservas);
        model.addAttribute("esViajeAbierto", esViajeAbierto);
        model.addAttribute("sePuedeReservar", sePuedeReservar);
        return "viaje/viaje_detalle";
    }

    @GetMapping("/viaje/cancelar")
    public String cancelarViaje(@RequestParam("codViaje") int codViaje, RedirectAttributes redirectAttributes) {
        viajeRepository.cancelarViaje(codViaje);
        redirectAttributes.addFlashAttribute("mensaje", "El viaje ha sido cancelado");
        return "redirect:/viajes";
    }

    @GetMapping("viajes/add")
    public String addViajeForm() {
        return "viaje/viaje_form";
    }

    @PostMapping("viajes/addAction")
    public String addViajeAction(@RequestParam("ruta") String ruta,
                                 @RequestParam("plazas") int plazas,
                                 @RequestParam("propietario") String propietario,
                                 @RequestParam("precio") double precio,
                                 @RequestParam("duracion") int duracion,
                                 @RequestParam("diaSalida") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate diaSalida,
                                 @RequestParam("horaSalida") @DateTimeFormat(pattern = "HH:mm") LocalTime horaSalida,
                                 RedirectAttributes redirectAttributes) {
        try {
            if (!ruta.matches("[A-Za-z]+(-[A-Za-z]+)+") ||
                    plazas < 1 || plazas > 6 ||
                    !propietario.matches("[A-Z][a-z]* [A-Z][a-z]*") ||
                    precio <= 0 ||
                    duracion <= 0) {
                throw new Exception("Datos del formulario no válidos");
            }


            int codViaje = viajeRepository.getNextCodViaje();
            Viaje viaje = new Viaje(codViaje, propietario, ruta, diaSalida.atTime(horaSalida), duracion, (float) precio, plazas, EstadoViaje.ABIERTO);

            viajeRepository.save(viaje);

            redirectAttributes.addFlashAttribute("message", "Viaje añadido con éxito");

            return "redirect:/viajes";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Error al añadir el viaje: " + ex.getMessage());

            return "redirect:/viajes/add";
        }
    }


}