# Sistema de Control de Parqueo

Control de entrada/salida de vehículos con cobro por hora (Java + MySQL), Proyecto Integrador.

## Funcionalidades

- Listar espacios de parqueo (libre/ocupado).
- Registrar entrada de un vehículo (ocupa el espacio y crea el registro en una transacción).
- Registrar salida: calcula las horas transcurridas (`java.time.Duration`, redondeado hacia arriba, mínimo 1 hora) y el monto a pagar (₡800/hora), libera el espacio.

## Estructura

```
src/
├── dao/ConexionBD.java, ParqueoDAO.java
└── app/Main.java
```

## Base de datos

[`database/parqueo.sql`](database/parqueo.sql): `ESPACIOS`, `REGISTROS_PARQUEO`.

## Cómo ejecutarlo

```bash
mysql -u root -p < database/parqueo.sql
javac -d bin -cp "lib/mysql-connector-j-9.5.0.jar" src/dao/*.java src/app/*.java
java -cp "bin;lib/mysql-connector-j-9.5.0.jar" app.Main
```

> Compilado y verificado con `javac` sin errores; conexión real a MySQL no probada en este entorno (sin servidor corriendo, como acordamos).

## Capturas

_Pendiente: agregar capturas en `capturas/`._

## Licencia

MIT — ver [LICENSE](LICENSE).
