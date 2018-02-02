public class InvalidImplementationTypeException extends Exception {
    private final Class type;

    public InvalidImplementationTypeException(Class type) {
        super(String.format("Type %s is an interface and cannot be registered as an implementation type.", type.getName()));
        this.type = type;
    }

    public Class getType() {
        return type;
    }
}
