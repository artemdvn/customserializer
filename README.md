**Custom serializer**
``````
Benchmark test results:

# Run complete. Total time: 00:06:45

Benchmark                                 Mode  Cnt       Score       Error  Units
BenchmarkTest.benchmarkCustomSerializer  thrpt    5   46046,351 ±   411,976  ops/s
BenchmarkTest.benchmarkJackson           thrpt    5  327806,149 ± 20618,420  ops/s
BenchmarkTest.benchmarkKryo              thrpt    5  368611,008 ±  2110,924  ops/s
BenchmarkTest.benchmarkProtobuf          thrpt    5  978931,268 ± 61372,079  ops/s