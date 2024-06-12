package es.batbatcar.v2p4.controllers;

import es.batbatcar.v2p4.modelo.dto.viaje.EstadoViaje;
import es.batbatcar.v2p4.modelo.repositories.ViajesRepository;
import es.batbatcar.v2p4.modelo.dto.viaje.Viaje;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Controller
public class ViajesController {

    @Autowired
    private ViajesRepository viajesRepository;

    /**
     * Endpoint que muestra el listado de todos los viajes disponibles
     */
    @GetMapping("viajes")
    public String getViajesAction(@RequestParam(value = "destino", required = false) String destino, Model model) {
        Set<Viaje> allViajes = viajesRepository.findAll();
        Set<Viaje> viajesFiltrados = new HashSet<>();

        if (destino != null && !destino.isEmpty()) {
            for (Viaje viaje : allViajes) {
                if (viaje.getRuta().contains(destino)) {
                    viajesFiltrados.add(viaje);
                }
            }
        } else {
            viajesFiltrados = allViajes;
        }

        model.addAttribute("viajes", viajesFiltrados);
        model.addAttribute("titulo", "Listado de viajes");
        return "viaje/listado";
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


            int codViaje = viajesRepository.getNextCodViaje();
            Viaje viaje = new Viaje(codViaje, propietario, ruta, diaSalida.atTime(horaSalida), duracion, (float) precio, plazas, EstadoViaje.ABIERTO);

            viajesRepository.save(viaje);

            redirectAttributes.addFlashAttribute("message", "Viaje añadido con éxito");

            return "redirect:/viajes";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Error al añadir el viaje: " + ex.getMessage());

            return "redirect:/viajes/add";
        }
    }

    @GetMapping("viaje")
    public String getViajeDetalles(@RequestParam("codViaje") int codViaje, Model model) {
        try {
            Viaje viaje = viajesRepository.getViajeById(codViaje);
            model.addAttribute("viaje", viaje);
            return "viaje/viaje_detalle";
        } catch (Exception ex) {
            return "redirect:/viajes";
        }
    }
}
