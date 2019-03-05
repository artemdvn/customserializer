package com.github.artemdvn;

import com.github.artemdvn.domain.Car;
import com.github.artemdvn.domain.CarOption;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CustomSerializerUnitTests {

    @Test
    public void testCustomSerializer() throws Exception {
        Car initialCar = setupCar();

        CustomSerializer customSerializer = new CustomSerializer();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        customSerializer.serialize(outputStream, initialCar);
        outputStream.close();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        Car actualCar = customSerializer.deserialize(inputStream, Car.class);
        inputStream.close();

        Assert.assertEquals(initialCar.getModel(), actualCar.getModel());
        Assert.assertEquals(initialCar.getPower(), actualCar.getPower());
        Assert.assertTrue(actualCar.isUsed());
    }

    @Test
    public void testCustomSerializerNullField() throws Exception {
        Car initialCar = new Car();
        initialCar.setModel(null);

        CustomSerializer customSerializer = new CustomSerializer();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        customSerializer.serialize(outputStream, initialCar);
        outputStream.close();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        Car actualCar = customSerializer.deserialize(inputStream, Car.class);
        inputStream.close();

        Assert.assertNull(actualCar.getModel());
    }

    @Test
    public void testCustomSerializerStringField() throws Exception {
        Car initialCar = new Car();
        initialCar.setModel("Some model");

        CustomSerializer customSerializer = new CustomSerializer();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        customSerializer.serialize(outputStream, initialCar);
        outputStream.close();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        Car actualCar = customSerializer.deserialize(inputStream, Car.class);
        inputStream.close();

        Assert.assertEquals(initialCar.getModel(), actualCar.getModel());
    }

    @Test
    public void testCustomSerializerPrimitiveField() throws Exception {
        Car initialCar = new Car();
        initialCar.setUsed(true);

        CustomSerializer customSerializer = new CustomSerializer();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        customSerializer.serialize(outputStream, initialCar);
        outputStream.close();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        Car actualCar = customSerializer.deserialize(inputStream, Car.class);
        inputStream.close();

        Assert.assertTrue(actualCar.isUsed());
    }

    @Test
    public void testCustomSerializerWrapperField() throws Exception {
        Car initialCar = new Car();
        initialCar.setPower(50);

        CustomSerializer customSerializer = new CustomSerializer();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        customSerializer.serialize(outputStream, initialCar);
        outputStream.close();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        Car actualCar = customSerializer.deserialize(inputStream, Car.class);
        inputStream.close();

        Assert.assertEquals(initialCar.getPower(), actualCar.getPower());
    }

    @Test
    public void testCustomSerializerEnumField() throws Exception {
        Car initialCar = new Car();
        initialCar.setEngineType(Car.EngineType.HYBRID);

        CustomSerializer customSerializer = new CustomSerializer();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        customSerializer.serialize(outputStream, initialCar);
        outputStream.close();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        Car actualCar = customSerializer.deserialize(inputStream, Car.class);
        inputStream.close();

        Assert.assertEquals(Car.EngineType.HYBRID, actualCar.getEngineType());
    }

    @Test
    public void testCustomSerializerSetOfNonPrimitives() throws Exception {
        CarOption naviPack = new CarOption("Navi pack", 1200.50);
        CarOption safetyPack = new CarOption("Safety pack", 755.25);
        CarOption optionalPack = new CarOption("Optional pack", 566.45);
        Set<CarOption> options = new HashSet<>();
        options.add(naviPack);
        options.add(safetyPack);

        Car initialCar = new Car();
        initialCar.setOptions(options);

        CustomSerializer customSerializer = new CustomSerializer();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        customSerializer.serialize(outputStream, initialCar);
        outputStream.close();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        Car actualCar = customSerializer.deserialize(inputStream, Car.class);
        inputStream.close();

        Assert.assertEquals(2, actualCar.getOptions().size());
        Assert.assertTrue(actualCar.getOptions().contains(naviPack));
        Assert.assertTrue(actualCar.getOptions().contains(safetyPack));
        Assert.assertFalse(actualCar.getOptions().contains(optionalPack));
    }

    @Test
    public void testCustomSerializerMapOfWrappers() throws Exception {
        Map<String, Double> mileage = new HashMap<>();
        mileage.put("2017", 133.5);
        mileage.put("2018", null);
        mileage.put(null, 22.2);

        Car initialCar = new Car();
        initialCar.setMileage(mileage);

        CustomSerializer customSerializer = new CustomSerializer();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        customSerializer.serialize(outputStream, initialCar);
        outputStream.close();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        Car actualCar = customSerializer.deserialize(inputStream, Car.class);
        inputStream.close();

        Assert.assertEquals(3, actualCar.getMileage().size());
        Assert.assertTrue(actualCar.getMileage().containsKey("2017"));
        Assert.assertTrue(actualCar.getMileage().containsValue(133.5));
        Assert.assertTrue(actualCar.getMileage().containsKey("2018"));
        Assert.assertTrue(actualCar.getMileage().containsValue(null));
        Assert.assertTrue(actualCar.getMileage().containsKey(null));
        Assert.assertTrue(actualCar.getMileage().containsValue(22.2));
    }

    private static Car setupCar() {
        CarOption naviPack = new CarOption("Navi pack", 1200.50);
        CarOption safetyPack = new CarOption("Safety pack", 755.25);
        Set<CarOption> options = new HashSet<>();
        options.add(naviPack);
        options.add(safetyPack);

        Map<String, Double> mileage = new HashMap<>();
        mileage.put("2017", 133.5);
        mileage.put("2018", 4113.5);
        mileage.put("2019", 727.8);

        Car volvo = new Car();
        volvo.setModel("Volvo XC60");
        volvo.setPower(190);
        volvo.setEngineType(Car.EngineType.DIESEL);
        volvo.setUsed(true);
        volvo.setOptions(options);
        volvo.setMileage(mileage);

        return volvo;
    }

}
