package demo;

import module java.base;

record Product(String slug,
        String name,
        int packageQuantity,
        String packageUnit,
        BigDecimal price,
        Currency currency) { }
