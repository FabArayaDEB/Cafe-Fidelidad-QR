package com.example.cafefidelidaqrdemo.managers;

import android.content.Context;
import com.example.cafefidelidaqrdemo.models.Beneficio;
import com.example.cafefidelidaqrdemo.models.Visita;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Calendar;
import java.util.Date;

/**
 * Gestor de beneficios que maneja la lógica de activación automática
 * y aplicación de beneficios basados en reglas de negocio.
 *
 * 🔹 Ahora incluye compatibilidad con el nuevo sistema de SELLOS DIGITALES:
 * cada compra = 1 sello. Al completar cierta cantidad, se otorga un beneficio.
 */
public class BeneficioManager {

    private Context context;

    public BeneficioManager(Context context) {
        this.context = context;
    }

    /**
     * Evalúa y activa beneficios automáticamente basado en las visitas del cliente.
     */
    public List<Beneficio> evaluarBeneficiosAutomaticos(String clienteId, List<Visita> visitasRecientes) {
        List<Beneficio> nuevosBeneficios = new ArrayList<>();

        try {
            int totalVisitas = visitasRecientes.size();

            // Evaluar reglas de beneficios predefinidas
            nuevosBeneficios.addAll(evaluarReglasBasicas(clienteId, totalVisitas));
            nuevosBeneficios.addAll(evaluarReglasTemporales(clienteId, visitasRecientes));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return nuevosBeneficios;
    }

    /**
     * Reglas básicas de beneficios por cantidad de visitas (sellos).
     */
    private List<Beneficio> evaluarReglasBasicas(String clienteId, int totalVisitas) {
        List<Beneficio> beneficios = new ArrayList<>();

        // Cada 5 visitas = 10% de descuento
        if (totalVisitas > 0 && totalVisitas % 5 == 0) {
            beneficios.add(crearBeneficioDescuentoPorcentaje(
                    "¡5 Visitas Completadas!",
                    "10% de descuento en tu próxima compra",
                    10.0,
                    clienteId,
                    7 // Válido por 7 días
            ));
        }

        // Cada 10 visitas = Café gratis ☕
        if (totalVisitas > 0 && totalVisitas % 10 == 0) {
            beneficios.add(crearBeneficioProductoGratis(
                    "¡10 Visitas Completadas!",
                    "Café americano gratis",
                    "cafe_americano",
                    clienteId,
                    14
            ));
        }

        // Cada 20 visitas = 2x1
        if (totalVisitas > 0 && totalVisitas % 20 == 0) {
            beneficios.add(crearBeneficioDosxUno(
                    "¡20 Visitas Completadas!",
                    "2x1 en cualquier bebida",
                    clienteId,
                    30
            ));
        }

        // Cada 50 visitas = Cliente VIP
        if (totalVisitas > 0 && totalVisitas % 50 == 0) {
            beneficios.add(crearBeneficioDescuentoPorcentaje(
                    "¡Cliente VIP - 50 Visitas!",
                    "25% de descuento en toda tu compra",
                    25.0,
                    clienteId,
                    60
            ));
        }

        return beneficios;
    }

    /**
     * Reglas temporales basadas en frecuencia de visitas.
     */
    private List<Beneficio> evaluarReglasTemporales(String clienteId, List<Visita> visitasRecientes) {
        List<Beneficio> beneficios = new ArrayList<>();

        // Cliente frecuente: 3 visitas en una semana
        if (visitasRecientes.size() >= 3) {
            long visitasUltimaSemana = visitasRecientes.stream()
                    .filter(v -> esDentroDeUltimaSemana(new Date(v.getFechaVisita())))
                    .count();

            if (visitasUltimaSemana >= 3) {
                beneficios.add(crearBeneficioDescuentoPorcentaje(
                        "¡Cliente Frecuente!",
                        "15% de descuento por tu fidelidad semanal",
                        15.0,
                        clienteId,
                        3
                ));
            }
        }

        // Racha diaria: 5 días consecutivos
        if (tieneVisitasDiariasConsecutivas(visitasRecientes, 5)) {
            beneficios.add(crearBeneficioProductoGratis(
                    "¡Racha de 5 Días!",
                    "Postre gratis por tu constancia",
                    "postre_del_dia",
                    clienteId,
                    7
            ));
        }

        return beneficios;
    }

    /**
     * Aplica beneficios disponibles a una compra.
     */
    public double aplicarBeneficios(String clienteId, double montoCompra, List<Beneficio> beneficiosDisponibles, boolean acumularBeneficios) {
        double descuentoTotal = 0.0;

        try {
            for (Beneficio beneficio : beneficiosDisponibles) {
                if (!beneficio.isActivo() || "expirado".equals(beneficio.getEstado())) continue;

                double descuento = 0.0;

                switch (beneficio.getTipo()) {
                    case "descuento_porcentaje":
                        descuento = montoCompra * (beneficio.getValorDescuento() / 100.0);
                        break;
                    case "descuento_fijo":
                        descuento = Math.min(beneficio.getValorDescuento(), montoCompra);
                        break;
                }

                if (descuento > 0) {
                    descuentoTotal += descuento;
                    beneficio.marcarComoUsado();
                    beneficio.setActivo(false);

                    if (!acumularBeneficios) break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return descuentoTotal;
    }

    /**
     * Obtiene beneficios disponibles para un cliente.
     */
    public List<Beneficio> obtenerBeneficiosDisponibles(String clienteId, List<Beneficio> todosBeneficios) {
        try {
            List<Beneficio> beneficiosCliente = todosBeneficios.stream()
                    .filter(b -> clienteId.equals(b.getClienteId()))
                    .collect(Collectors.toList());

            long ahora = System.currentTimeMillis();
            beneficiosCliente.removeIf(b -> b.getFechaVencimiento() > 0 && b.getFechaVencimiento() < ahora);

            for (Beneficio beneficio : beneficiosCliente) {
                if (beneficio.estaVencido()) beneficio.setEstado("expirado");
            }

            return beneficiosCliente;

        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // -------------------------------
    // 🔹 NUEVOS MÉTODOS DE SELLOS 🔹
    // -------------------------------

    /**
     * Nuevo sistema de sellos digitales (tarjeta virtual).
     * Cada compra equivale a 1 sello. Al completar cierta cantidad se obtiene un beneficio.
     */
    public boolean verificarBeneficioPorSellos(List<Visita> visitas, int sellosRequeridos) {
        if (visitas == null) return false;
        int sellosActuales = visitas.size(); // cada compra = 1 sello
        return sellosActuales >= sellosRequeridos;
    }

    /**
     * Calcula cuántos sellos le faltan al cliente para su siguiente beneficio.
     */
    public int obtenerSellosRestantes(List<Visita> visitas, int sellosRequeridos) {
        int actuales = (visitas == null) ? 0 : visitas.size();
        return Math.max(0, sellosRequeridos - actuales);
    }

    // -------------------------------
    // 🔹 MÉTODOS AUXILIARES EXISTENTES 🔹
    // -------------------------------

    private Beneficio crearBeneficioDescuentoPorcentaje(String nombre, String descripcion, double porcentaje, String clienteId, int diasValidez) {
        Beneficio beneficio = new Beneficio(UUID.randomUUID().toString(), nombre, descripcion, "descuento_porcentaje", 0, porcentaje);
        beneficio.setClienteId(clienteId);
        beneficio.setFechaVencimiento(System.currentTimeMillis() + (diasValidez * 24L * 60L * 60L * 1000L));
        return beneficio;
    }

    private Beneficio crearBeneficioDescuentoFijo(String nombre, String descripcion, double monto, String clienteId, int diasValidez) {
        Beneficio beneficio = new Beneficio(UUID.randomUUID().toString(), nombre, descripcion, "descuento_fijo", 0, monto);
        beneficio.setClienteId(clienteId);
        beneficio.setFechaVencimiento(System.currentTimeMillis() + (diasValidez * 24L * 60L * 60L * 1000L));
        return beneficio;
    }

    private Beneficio crearBeneficioProductoGratis(String nombre, String descripcion, String productoId, String clienteId, int diasValidez) {
        Beneficio beneficio = new Beneficio(UUID.randomUUID().toString(), nombre, descripcion, "producto_gratis", 0, 0.0);
        beneficio.setClienteId(clienteId);
        beneficio.setProductoId(productoId);
        beneficio.setFechaVencimiento(System.currentTimeMillis() + (diasValidez * 24L * 60L * 60L * 1000L));
        return beneficio;
    }

    private Beneficio crearBeneficioDosxUno(String nombre, String descripcion, String clienteId, int diasValidez) {
        Beneficio beneficio = new Beneficio(UUID.randomUUID().toString(), nombre, descripcion, "dos_por_uno", 0, 0.0);
        beneficio.setClienteId(clienteId);
        beneficio.setFechaVencimiento(System.currentTimeMillis() + (diasValidez * 24L * 60L * 60L * 1000L));
        return beneficio;
    }

    private boolean esDentroDeUltimaSemana(Date fecha) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -7);
        return fecha.after(cal.getTime());
    }

    private boolean tieneVisitasDiariasConsecutivas(List<Visita> visitas, int diasRequeridos) {
        if (visitas.size() < diasRequeridos) return false;
        visitas.sort((v1, v2) -> Long.compare(v2.getFechaVisita(), v1.getFechaVisita()));
        Calendar cal = Calendar.getInstance();
        int diasConsecutivos = 0;
        Date fechaAnterior = null;

        for (Visita visita : visitas) {
            Date fechaVisita = new Date(visita.getFechaVisita());
            if (fechaAnterior == null) {
                diasConsecutivos = 1;
                fechaAnterior = fechaVisita;
            } else {
                cal.setTime(fechaAnterior);
                cal.add(Calendar.DAY_OF_YEAR, -1);
                if (esMismoDia(fechaVisita, cal.getTime())) {
                    diasConsecutivos++;
                    fechaAnterior = fechaVisita;
                } else {
                    break;
                }
            }
        }
        return diasConsecutivos >= diasRequeridos;
    }

    private boolean esMismoDia(Date fecha1, Date fecha2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(fecha1);
        cal2.setTime(fecha2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
    // ---------------------------------------------------------------------
// 🔹 Registra un canje basado en el sistema de sellos digitales
// ---------------------------------------------------------------------
    public void registrarCanjePorSellos(int clienteId, int totalSellos) {
        try {
            // Simulación de beneficio por sellos
            String beneficioId = "1"; // ID fijo o lógico según tu sistema
            com.example.cafefidelidaqrdemo.repository.CanjeRepository canjeRepository =
                    new com.example.cafefidelidaqrdemo.repository.CanjeRepository(context);

            // Registrar el canje usando la lógica del repositorio
            canjeRepository.registrarCanjePorSellos(
                    String.valueOf(clienteId), // ID cliente como String
                    beneficioId,               // ID del beneficio
                    totalSellos,               // sellos requeridos
                    result -> {
                        if (result) {
                            android.util.Log.d("BeneficioManager", "✅ Canje por sellos registrado correctamente");
                        } else {
                            android.util.Log.w("BeneficioManager", "⚠️ No se pudo registrar el canje por sellos");
                        }
                    }
            );

        } catch (Exception e) {
            android.util.Log.e("BeneficioManager", "Error al registrar canje por sellos", e);
        }
    }

}
