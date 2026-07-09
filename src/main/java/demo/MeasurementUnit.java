package demo;

import module java.base;

enum MeasurementUnit {

    GRAM("g", Dimension.MASS, BigDecimal.ONE),
    KILOGRAM("kg", Dimension.MASS, BigDecimal.valueOf(1_000)),
    MILLILITRE("ml", Dimension.VOLUME, BigDecimal.ONE);

    private final String symbol;
    private final Dimension dimension;
    private final BigDecimal baseUnitMultiplier;

    MeasurementUnit(final String symbol, final Dimension dimension, final BigDecimal baseUnitMultiplier) {
        this.symbol = symbol;
        this.dimension = dimension;
        this.baseUnitMultiplier = baseUnitMultiplier;
    }

    static MeasurementUnit from(final String symbol) {
        return Arrays.stream(values())
                .filter(unit -> unit.symbol.equals(symbol))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Ingredients and packages must use supported units"));
    }

    String symbol() {
        return symbol;
    }

    BigDecimal toBaseUnits(final BigDecimal quantity) {
        return quantity.multiply(baseUnitMultiplier);
    }

    boolean hasSameDimensionAs(final MeasurementUnit other) {
        return dimension == other.dimension;
    }

    private enum Dimension {
        MASS,
        VOLUME
    }
}
