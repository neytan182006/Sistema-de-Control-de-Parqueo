package app;

import dao.ParqueoDAO;

import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    private static final Scanner TECLADO = new Scanner(System.in);
    private static final ParqueoDAO parqueoDAO = new ParqueoDAO();

    public static void main(String[] args) {
        int opcion;
        do {
            mostrarMenu();
            opcion = leerEntero("Seleccione una opcion: ");

            try {
                switch (opcion) {
                    case 1 -> parqueoDAO.listarEspacios();
                    case 2 -> registrarEntrada();
                    case 3 -> registrarSalida();
                    case 0 -> System.out.println("Hasta luego.");
                    default -> System.out.println("Opcion invalida.");
                }
            } catch (SQLException e) {
                System.out.println("Error de base de datos: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    private static void mostrarMenu() {
        System.out.println("\n=== SISTEMA DE CONTROL DE PARQUEO ===");
        System.out.println("1. Listar espacios");
        System.out.println("2. Registrar entrada de vehiculo");
        System.out.println("3. Registrar salida (calcula el monto a pagar)");
        System.out.println("0. Salir");
    }

    private static void registrarEntrada() throws SQLException {
        int idEspacio = leerEntero("Id del espacio (ver opcion 1): ");
        System.out.print("Placa del vehiculo: ");
        String placa = TECLADO.nextLine();
        System.out.println(parqueoDAO.registrarEntrada(idEspacio, placa));
    }

    private static void registrarSalida() throws SQLException {
        int idRegistro = leerEntero("Id del registro de entrada: ");
        System.out.println(parqueoDAO.registrarSalida(idRegistro));
    }

    private static int leerEntero(String mensaje) {
        System.out.print(mensaje);
        while (!TECLADO.hasNextInt()) {
            System.out.print("Ingrese un numero valido: ");
            TECLADO.next();
        }
        int valor = TECLADO.nextInt();
        TECLADO.nextLine();
        return valor;
    }
}
