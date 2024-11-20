FROM openjdk:17
WORKDIR /
ADD target/huatuo-tts-0.0.1-SNAPSHOT.jar temp.jar
EXPOSE 8655
CMD ["java", "-jar", "temp.jar"]
