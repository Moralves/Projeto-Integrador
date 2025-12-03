# Backend - Spring Boot

Backend do sistema LifeTrack desenvolvido com Spring Boot 3 + PostgreSQL.

## Executar

### Forma mais simples (JAR já compilado):

```powershell
cd LifeTrack\backend
java -jar target\sos-rota-0.0.1-SNAPSHOT.jar
```

✅ Backend rodando em: **http://localhost:8080**

### Se precisar recompilar:

```powershell
# Com Maven instalado:
mvn clean install
java -jar target\sos-rota-0.0.1-SNAPSHOT.jar

# Ou diretamente:
mvn spring-boot:run
```

## Configuração

Edite `src/main/resources/application.properties` com suas credenciais do PostgreSQL.

---

**Para mais detalhes, consulte o README.md na raiz do projeto.**
