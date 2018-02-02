public class TypeNotRegisteredException extends RuntimeException {
    private final Class type;

    public TypeNotRegisteredException(Class type) {
        super(String.format("Type %s is not registered within the container.", type.getName()));
        this.type = type;
    }

    public Class getType() {
        return type;
    }
}
