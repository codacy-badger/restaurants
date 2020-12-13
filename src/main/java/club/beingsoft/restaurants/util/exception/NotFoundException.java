package club.beingsoft.restaurants.util.exception;

public class NotFoundException extends RuntimeException {
    public <T> NotFoundException(T clazz, Integer id) {
        super(clazz.getClass().getName() + " not found with id " + id);
    }

    public <T> NotFoundException(String message) {
        super(message);
    }
}
