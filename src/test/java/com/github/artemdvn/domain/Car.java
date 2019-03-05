package com.github.artemdvn.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Car {
    private String model;
    private Integer power;
    private EngineType engineType;
    private boolean used;

    private Set<CarOption> options;
    private Map<String, Double> mileage;

    public enum EngineType {
        GASOLINE,
        DIESEL,
        HYBRID,
        ELECTRIC
    }
}
