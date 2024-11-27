FROM flink:1.16-scala_2.12-java8

RUN chmod -R 777 /opt/flink/conf

RUN wget --no-check-certificate -P ./plugins/s3-fs-presto/flink-s3-fs-presto-1.16.0.jar "https://repo1.maven.org/maven2/org/apache/flink/flink-s3-fs-presto/1.16.0/flink-s3-fs-presto-1.16.0.jar" \
    && wget --no-check-certificate -P ./plugins/s3-fs-hadoop/flink-s3-fs-hadoop-1.16.0.jar "https://repo1.maven.org/maven2/org/apache/flink/flink-s3-fs-hadoop/1.16.0/flink-s3-fs-hadoop-1.16.0.jar" 


RUN chmod 777 ./plugins/s3-fs-presto/flink-s3-fs-presto-1.16.0.jar \
    && chmod 777 ./plugins/s3-fs-hadoop/flink-s3-fs-hadoop-1.16.0.jar

RUN mkdir /app

COPY ./target/scala-2.12/*.jar ./app/flink-enrichment.jar

RUN chmod 777 -R /opt/flink/log

RUN chmod 777 /opt/flink/conf/flink-conf.yaml


