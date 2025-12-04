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
# Usando Maven Wrapper (recomendado - não precisa ter Maven instalado):
.\mvnw.cmd clean install -DskipTests
java -jar target\sos-rota-0.0.1-SNAPSHOT.jar

# Ou executar diretamente sem gerar JAR:
.\mvnw.cmd spring-boot:run
```

> **Nota:** O projeto usa Maven Wrapper (`mvnw.cmd`), então você **não precisa ter Maven instalado**. Use sempre `.\mvnw.cmd` ao invés de `mvn`.

## Configuração

Edite `src/main/resources/application.properties` com suas credenciais do PostgreSQL.

---

**Para mais detalhes, consulte o README.md na raiz do projeto.**
