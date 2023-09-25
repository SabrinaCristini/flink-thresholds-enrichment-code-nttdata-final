FROM flink:1.16-scala_2.12-java8

RUN mkdir ./plugins/s3-fs-presto
RUN cp ./opt/flink-s3-fs-presto-1.16.0.jar ./plugins/s3-fs-presto/

RUN mkdir ./plugins/s3-fs-hadoop
RUN cp ./opt/flink-s3-fs-hadoop-1.16.0.jar ./plugins/s3-fs-hadoop/

RUN mkdir ./app

#COPY src/main/scala/Config/config.json ./app

#RUN chmod 775 ./app/config.json

ADD ./target/scala-2.12/*.jar ./app/flink-enrichment.jar

RUN chmod 777 -R /opt/flink/log


