package it.mgt.util.json2jpa;

class DetectedChanges {

    private Object oldValue;
    private Object newValue;
    private ChangedHandler<?> handler;

    DetectedChanges(Object oldValue, Object newValue, ChangedHandler<?> handler) {
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.handler = handler;
    }

    void invoke() {
        handler.invoke(oldValue, newValue);
    }

}
