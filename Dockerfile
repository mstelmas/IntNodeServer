FROM java:8
COPY ./target/IntNodeServer-0.0.1-SNAPSHOT.jar /tmp/int.jar
CMD java -jar /tmp/int.jar
