package es.batbatcar.v2p4.exceptions;

public class InvalidNumberOfPlacesException extends RuntimeException {

    public InvalidNumberOfPlacesException() {
        super("El número de plazas sobrepasa el número de plazas ofrecidas en el viaje.");
    }
}
