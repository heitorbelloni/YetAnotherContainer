import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Container implements IServiceProvider, Serializable {
    private final ConcurrentMap<Class<?>, SupplierWithExceptions<?>> registeredTypes;

    public Container() {
        registeredTypes = new ConcurrentHashMap<>();
    }

    public <T> void register(Class<T> implementationType) throws InvalidImplementationTypeException {
        register(implementationType, implementationType);
    }

    public <T> void register(Class<T> serviceType, Class<? extends T> implementationType) throws InvalidImplementationTypeException {
        internalRegister(serviceType, implementationType, () -> instantiate(implementationType));
    }

    public <T> void registerSingleton(Class<T> serviceType, T instance) {
        registeredTypes.put(serviceType, () -> instance);
    }

    public <T> void registerSingleton(Class<T> implementationType) throws InvalidImplementationTypeException {
        registerSingleton(implementationType, implementationType);
    }

    public <T> void registerSingleton(Class<T> serviceType, Class<? extends T> implementationType) throws InvalidImplementationTypeException {
        internalRegister(serviceType, implementationType, () -> instantiateSingleton(serviceType, implementationType));
    }

    private <T> void internalRegister(Class<T> serviceType, Class<? extends T> implementationType, SupplierWithExceptions supplier) throws InvalidImplementationTypeException {
        if(implementationType.isInterface()) {
            throw new InvalidImplementationTypeException(implementationType);
        }
        registeredTypes.put(serviceType, supplier);
    }

    private <T> T instantiateSingleton(Class<T> serviceType, Class<? extends T> implementationType) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        T instance = instantiate(implementationType);
        registeredTypes.put(serviceType, () -> instance);
        return instance;
    }

    private <T> T instantiate(Class<T> implementationType) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        Constructor constructor = implementationType.getConstructors()[0];
        Parameter[] parameters = constructor.getParameters();
        if(parameters.length == 0) {
            return implementationType.newInstance();
        }

        List<Object> instantiatedParameters = new ArrayList<>(parameters.length);
        for(Parameter parameter : parameters) {
            SupplierWithExceptions supplier = registeredTypes.get(parameter.getType());
            instantiatedParameters.add(supplier.invoke());
        }
        return (T) constructor.newInstance(instantiatedParameters.toArray());
    }

    @Override
    public <T> T getService(Class<T> serviceType) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        SupplierWithExceptions supplier = registeredTypes.get(serviceType);
        if(supplier == null) {
            throw new TypeNotRegisteredException(serviceType);
        }
        return (T) supplier.invoke();
    }

    @FunctionalInterface
    private interface SupplierWithExceptions<T> extends Serializable {
        T invoke() throws IllegalAccessException, InstantiationException, InvocationTargetException;
    }
}
