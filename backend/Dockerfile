# Backend (Java 17 + SQLite). O contexto do build e a RAIZ do repositorio (precisa do pom pai).
#   docker build -f backend/Dockerfile -t granja-backend .
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml ./
COPY backend/pom.xml backend/
COPY backend/src backend/src
# gera o jar do backend e junta as bibliotecas na mesma pasta
RUN mvn -q -pl backend -DskipTests package dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=/app \
 && cp backend/target/granja-backend-*.jar /app/

FROM eclipse-temurin:17-jre
RUN useradd --system --create-home --home-dir /home/granja granja \
 && mkdir /dados && chown granja /dados
WORKDIR /app
COPY --from=build /app /app
USER granja
# o banco e os backups ficam em /dados (volume), assim sobrevivem a recriacao do contêiner
ENV HOST=0.0.0.0 PORTA=8080 GRANJA_DB=/dados/granja.db BACKUPS=/dados/backups
VOLUME /dados
EXPOSE 8080
CMD ["java", "-cp", "/app/*", "br.ufpb.aps.granja.Main"]
