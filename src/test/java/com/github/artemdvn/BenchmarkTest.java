package com.github.artemdvn;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.artemdvn.domain.Car;
import com.github.artemdvn.domain.CarOption;
import com.github.artemdvn.domain.CarOuterClass;
import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BenchmarkTest {

    private static CustomSerializer customSerializer = new CustomSerializer();
    private static Kryo kryo = new Kryo();
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        Main.main(args);
    }

    @State(Scope.Thread)
    public static class TestState {
        public Car volvo = setupCar();
        public CarOuterClass.Car protoCar = setupProtoCar();
    }

    @Benchmark
    @Fork(value = 1)
    @BenchmarkMode(Mode.Throughput)
    public void benchmarkCustomSerializer(TestState state, Blackhole blackhole) throws Exception {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            customSerializer.serialize(byteArrayOutputStream, state.volvo);

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            Car car = (Car) customSerializer.deserialize(byteArrayInputStream, Car.class);
            byteArrayInputStream.close();

            blackhole.consume(car);
        }
    }

    @Benchmark
    @Fork(value = 1)
    @BenchmarkMode(Mode.Throughput)
    public void benchmarkKryo(TestState state, Blackhole blackhole) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            Output output = new Output(byteArrayOutputStream);
            kryo.writeClassAndObject(output, state.volvo);
            Input input = new Input(output.getBuffer());
            Car car = (Car) kryo.readClassAndObject(input);

            blackhole.consume(car);
        }
    }

    @Benchmark
    @Fork(value = 1)
    @BenchmarkMode(Mode.Throughput)
    public void benchmarkJackson(TestState state, Blackhole blackhole) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            objectMapper.writeValue(outputStream, state.volvo);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            Car car = objectMapper.readValue(inputStream, Car.class);
            inputStream.close();

            blackhole.consume(car);
        }
    }

    @Benchmark
    @Fork(value = 1)
    @BenchmarkMode(Mode.Throughput)
    public void benchmarkProtobuf(TestState state, Blackhole blackhole) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            state.protoCar.writeTo(outputStream);
            CarOuterClass.Car protoCarDeserialized = CarOuterClass.Car.newBuilder()
                    .mergeFrom(outputStream.toByteArray()).build();

            blackhole.consume(protoCarDeserialized);
        }
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

    private static CarOuterClass.Car setupProtoCar() {
        CarOuterClass.CarOption naviPack = CarOuterClass.CarOption.newBuilder()
                .setOption("Navi pack")
                .setPrice(1200.50)
                .build();
        CarOuterClass.CarOption safetyPack = CarOuterClass.CarOption.newBuilder()
                .setOption("Safety pack")
                .setPrice(755.25)
                .build();

        return CarOuterClass.Car.newBuilder()
                .setModel("Volvo XC60")
                .setPower(190)
                .setEngineType(CarOuterClass.Car.EngineType.DIESEL)
                .setUsed(true)
                .addOptions(naviPack)
                .addOptions(safetyPack)
                .putMileage("2017", 133.5)
                .putMileage("2017", 133.5)
                .putMileage("2017", 133.5)
                .build();
    }
}
