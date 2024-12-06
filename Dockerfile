FROM eclipse-temurin:17-jre-alpine
ARG JAR_FILE
ADD ${JAR_FILE} app.jar
ENV LD_PRELOAD=/lib/libgcompat.so.0
RUN java -Djarmode=layertools -jar app.jar extract
RUN ls -Rlah dependencies
# Set the locale
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
VOLUME ["/tmp", "/opt/config"]
#RUN cp -r snapshot-dependencies/* ./
RUN cp -r dependencies/* ./
RUN cp -r spring-boot-loader/* ./
RUN cp -r application/* ./
EXPOSE 8080
ENTRYPOINT ["java","org.springframework.boot.loader.JarLauncher","-noverify","-XshowSettings:all","-XX:MaxRAMPercentage=85",\
      "-XX:+UnlockExperimentalVMOptions", \
      "-XX:+UseShenandoahGC",\
      "-Dcom.sun.management.jmxremote",  \
      "-Dcom.sun.management.jmxremote.port=9010",  \
      "-Dcom.sun.management.jmxremote.rmi.port=9010",  \
      "-Djava.rmi.server.hostname=127.0.0.1",  \
      "-Dcom.sun.management.jmxremote.local.only=false",  \
      "-Dcom.sun.management.jmxremote.authenticate=false",  \
      "-Dcom.sun.management.jmxremote.ssl=false",  \
      "-Djava.security.egd=file:/dev/./urandom", \
      "--logging.config=/opt/config/logback.xml"]
