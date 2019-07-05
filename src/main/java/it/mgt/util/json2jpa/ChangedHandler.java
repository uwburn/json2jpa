package it.mgt.util.json2jpa;

public abstract class ChangedHandler<T> {

    @SuppressWarnings("unchecked")
    void invoke(Object oldValue, Object newValue) {
        this.handle((T) oldValue, (T) newValue);
    }

    public abstract void handle(T oldValue, T newValue);

}
