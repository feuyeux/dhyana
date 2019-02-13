```
mvn clean package -DskipTests
```

```
java -Ddhyana.port=12300 -Ddhyana.nodeName=N0 -Dserver.port=12200 -Ddhyana.clean=T -jar target/dhyana-example.jar
```

```
java -Ddhyana.port=12301 -Ddhyana.nodeName=N1 -Dserver.port=12201 -jar target/dhyana-example.jar
```

```
java -Ddhyana.port=12302 -Ddhyana.nodeName=N2 -Dserver.port=12202 -jar target/dhyana-example.jar
```

```
java -Ddhyana.port=12310 -Dserver.port=12210 -jar target/dhyana-example.jar
java -Ddhyana.port=12311 -Dserver.port=12211 -jar target/dhyana-example.jar
java -Ddhyana.port=12312 -Dserver.port=12212 -jar target/dhyana-example.jar
```

```
curl localhost:12200/cluster
```