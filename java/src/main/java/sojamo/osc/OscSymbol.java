package sojamo.osc;


public class OscSymbol {

    private final String symbol;

    public OscSymbol(String theSymbol) {
        symbol = theSymbol;
    }

    public String get() {
        return symbol;
    }

    @Override
    public String toString() {
        return symbol;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!OscSymbol.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final OscSymbol other = (OscSymbol) obj;
        return (this.symbol == null) ? other.symbol == null : this.symbol.equals(other.symbol);
    }

}
