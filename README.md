## Ejecución del microservicio

### Requisitos previos
- Java 21
- Maven
- MySQL/Oracle (según tu configuración)

### Pasos de ejecución
1. Clonar el repositorio.
2. Ejecutar `mvn spring-boot:run` en la raíz del proyecto.
3. Acceder a `http://localhost:8080`.

### Estrategia de ramas
- **main** → producción
- **develop** → integración
- **feature/** → nuevas funcionalidades
- **hotfix/** → correcciones rápidas

### Integración continua
- Configuración inicial con **GitHub Actions** para pruebas automáticas.

### Uso de IA
Se declaró el apoyo de herramientas de IA para documentación y validaciones.

