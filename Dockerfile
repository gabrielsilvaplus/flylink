# Estágio de Build (Usando JDK completo para compilar)
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copia apenas o wrapper do Maven e os arquivos de dependência primeiro
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Dá permissão de execução ao mvnw (caso o arquivo no Windows/Linux perca permissões)
RUN chmod +x ./mvnw

# Baixa as dependências offline (isso cria uma camada de cache no Docker)
RUN ./mvnw dependency:go-offline

# Copia o código-fonte restante e compila
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Estágio de Execução (Imagem super leve com apenas a JRE)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Boas práticas de segurança: Criar usuário não-root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copia o Jar gerado do estágio anterior
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

# Ponto de entrada padrão
ENTRYPOINT ["java", "-jar", "app.jar"]
