package legends.entity;

public final class StatusEffect {
    private final String key, kind;
    private double value;
    private int remaining;

    public StatusEffect(String key, String kind, double value, int remaining) {
        this.key = key;
        this.kind = kind;
        this.value = value;
        this.remaining = remaining;
    }

    public String getKey() {
        return key;
    }

    public String getKind() {
        return kind;
    }

    public double getValue() {
        return value;
    }

    public int getRemaining() {
        return remaining;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setRemaining(int remaining) {
        this.remaining = remaining;
    }
}
