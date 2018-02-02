import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

public interface IServiceProvider extends Serializable {
    <T> T getService(Class<T> type) throws IllegalAccessException, InstantiationException, InvocationTargetException;
}