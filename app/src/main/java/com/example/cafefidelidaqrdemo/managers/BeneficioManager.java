package com.example.cafefidelidaqrdemo.managers;

import android.content.Context;
import com.example.cafefidelidaqrdemo.models.Beneficio;
import com.example.cafefidelidaqrdemo.models.Visita;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Calendar;

/**
 * Gestor de beneficios que maneja la lógica de activación automática
 * y aplicación de beneficios basados en reglas de negocio
 */
public class BeneficioManager {
    
    private Context context;
    
    public BeneficioManager(Context context) {
        this.context = context;
    }
    
    /**
     * Evalúa y activa beneficios automáticamente basado en las visitas del cliente
     */
    public List<Beneficio> evaluarBeneficiosAutomaticos(String clienteId, List<Visita> visitasRecientes) {
        List<Beneficio> nuevosBeneficios = new ArrayList<>();
        
        try {
            int totalVisitas = visitasRecientes.size();
            
            // Evaluar reglas de beneficios predefinidas
            nuevosBeneficios.addAll(evaluarReglasBasicas(clienteId, totalVisitas));
            nuevosBeneficios.addAll(evaluarReglasTemporales(clienteId, visitasRecientes));
            
        } catch (Exception e) {
            // Log error
        }
        
        return nuevosBeneficios;
    }
    
    /**
     * Reglas básicas de beneficios por cantidad de visitas
     */
    private List<Beneficio> evaluarReglasBasicas(String clienteId, int totalVisitas) {
        List<Beneficio> beneficios = new ArrayList<>();
        
        // Cada 5 visitas: 10% de descuento
        if (totalVisitas > 0 && totalVisitas % 5 == 0) {
            Beneficio descuento5 = crearBeneficioDescuentoPorcentaje(
                "¡5 Visitas Completadas!",
                "10% de descuento en tu próxima compra",
                10.0,
                clienteId,
                7 // Válido por 7 días
            );
            beneficios.add(descuento5);
        }
        
        // Cada 10 visitas: Café gratis
        if (totalVisitas > 0 && totalVisitas % 10 == 0) {
            Beneficio cafeGratis = crearBeneficioProductoGratis(
                "¡10 Visitas Completadas!",
                "Café americano gratis",
                "cafe_americano",
                clienteId,
                14 // Válido por 14 días
            );
            beneficios.add(cafeGratis);
        }
        
        // Cada 20 visitas: 2x1 en cualquier bebida
        if (totalVisitas > 0 && totalVisitas % 20 == 0) {
            Beneficio dosxUno = crearBeneficioDosxUno(
                "¡20 Visitas Completadas!",
                "2x1 en cualquier bebida",
                clienteId,
                30 // Válido por 30 días
            );
            beneficios.add(dosxUno);
        }
        
        // Cada 50 visitas: 25% de descuento
        if (totalVisitas > 0 && totalVisitas % 50 == 0) {
            Beneficio descuentoEspecial = crearBeneficioDescuentoPorcentaje(
                "¡Cliente VIP - 50 Visitas!",
                "25% de descuento en toda tu compra",
                25.0,
                clienteId,
                60 // Válido por 60 días
            );
            beneficios.add(descuentoEspecial);
        }
        
        return beneficios;
    }
    
    /**
     * Reglas temporales basadas en frecuencia de visitas
     */
    private List<Beneficio> evaluarReglasTemporales(String clienteId, List<Visita> visitasRecientes) {
        List<Beneficio> beneficios = new ArrayList<>();
        
        // Cliente frecuente: 3 visitas en una semana
        if (visitasRecientes.size() >= 3) {
            long visitasUltimaSemana = visitasRecientes.stream()
                .filter(v -> esDentroDeUltimaSemana(new Date(v.getFechaVisita())))
                .count();
                
            if (visitasUltimaSemana >= 3) {
                Beneficio frecuente = crearBeneficioDescuentoPorcentaje(
                    "¡Cliente Frecuente!",
                    "15% de descuento por tu fidelidad semanal",
                    15.0,
                    clienteId,
                    3 // Válido por 3 días
                );
                beneficios.add(frecuente);
            }
        }
        
        // Visita diaria: 5 días consecutivos
        if (tieneVisitasDiariasConsecutivas(visitasRecientes, 5)) {
            Beneficio diario = crearBeneficioProductoGratis(
                "¡Racha de 5 Días!",
                "Postre gratis por tu constancia",
                "postre_del_dia",
                clienteId,
                7
            );
            beneficios.add(diario);
        }
        
        return beneficios;
    }
    

    
    /**
     * Aplica beneficios disponibles a una compra
     */
    public double aplicarBeneficios(String clienteId, double montoCompra, List<Beneficio> beneficiosDisponibles, boolean acumularBeneficios) {
        double descuentoTotal = 0.0;
        
        try {
            for (Beneficio beneficio : beneficiosDisponibles) {
                if (!beneficio.isActivo() || beneficio.getEstado() == Beneficio.EstadoBeneficio.EXPIRADO) {
                    continue;
                }
                
                double descuento = 0.0;
                
                // Aplicar según el tipo de beneficio
                switch (beneficio.getTipo()) {
                    case DESCUENTO_PORCENTAJE:
                        descuento = montoCompra * (beneficio.getValorDescuentoPorcentaje() / 100.0);
                        break;
                    case DESCUENTO_FIJO:
                        descuento = Math.min(beneficio.getValorDescuentoFijo(), montoCompra);
                        break;
                    // Otros tipos se manejan de forma diferente
                }
                
                if (descuento > 0) {
                    descuentoTotal += descuento;
                    
                    // Actualizar uso del beneficio
                    beneficio.setCantidadUsosActuales(beneficio.getCantidadUsosActuales() + 1);
                    beneficio.setMontoTotalAhorrado(beneficio.getMontoTotalAhorrado() + descuento);
                    
                    // Si alcanzó el máximo de usos, desactivar
                    if (beneficio.getCantidadMaximaUsos() > 0 && 
                        beneficio.getCantidadUsosActuales() >= beneficio.getCantidadMaximaUsos()) {
                        beneficio.setActivo(false);
                    }
                    
                    // Si no es acumulable, solo aplicar uno
                    if (!acumularBeneficios) {
                        break;
                    }
                }
            }
            
        } catch (Exception e) {
            // Error aplicando beneficios
        }
        
        return descuentoTotal;
    }
    
    /**
     * Obtiene beneficios disponibles para un cliente
     */
    public List<Beneficio> obtenerBeneficiosDisponibles(String clienteId, List<Beneficio> todosBeneficios) {
        try {
            // Filtrar beneficios del cliente específico
            List<Beneficio> beneficiosCliente = todosBeneficios.stream()
                .filter(b -> clienteId.equals(b.getClienteId()))
                .collect(Collectors.toList());
            
            // Filtrar beneficios expirados
            Date ahora = new Date();
            beneficiosCliente.removeIf(b -> b.getFechaFinVigencia() != null && b.getFechaFinVigencia().before(ahora));
            
            // Marcar como expirados
            for (Beneficio beneficio : beneficiosCliente) {
                if (beneficio.getFechaFinVigencia() != null && beneficio.getFechaFinVigencia().before(ahora)) {
                    beneficio.expirar();
                }
            }
            
            return beneficiosCliente;
            
        } catch (Exception e) {
            // Error obteniendo beneficios disponibles
            return new ArrayList<>();
        }
    }
    
    // Métodos auxiliares para crear beneficios
    private Beneficio crearBeneficioDescuentoPorcentaje(String nombre, String descripcion, double porcentaje, String clienteId, int diasValidez) {
        Beneficio beneficio = new Beneficio(nombre, descripcion, Beneficio.TipoBeneficio.DESCUENTO_PORCENTAJE, 0);
        beneficio.setId(UUID.randomUUID().toString());
        beneficio.setClienteId(clienteId);
        beneficio.setValorDescuentoPorcentaje(porcentaje);
        beneficio.setFechaInicioVigencia(new Date());
        beneficio.setFechaFinVigencia(agregarDias(new Date(), diasValidez));
        beneficio.setCantidadMaximaUsos(1);
        beneficio.setEsPersonalizado(true);
        beneficio.activar();
        return beneficio;
    }
    
    private Beneficio crearBeneficioDescuentoFijo(String nombre, String descripcion, double monto, String clienteId, int diasValidez) {
        Beneficio beneficio = new Beneficio(nombre, descripcion, Beneficio.TipoBeneficio.DESCUENTO_FIJO, 0);
        beneficio.setId(UUID.randomUUID().toString());
        beneficio.setClienteId(clienteId);
        beneficio.setValorDescuentoFijo(monto);
        beneficio.setFechaInicioVigencia(new Date());
        beneficio.setFechaFinVigencia(agregarDias(new Date(), diasValidez));
        beneficio.setCantidadMaximaUsos(1);
        beneficio.setEsPersonalizado(true);
        beneficio.activar();
        return beneficio;
    }
    
    private Beneficio crearBeneficioProductoGratis(String nombre, String descripcion, String productoId, String clienteId, int diasValidez) {
        Beneficio beneficio = new Beneficio(nombre, descripcion, Beneficio.TipoBeneficio.PRODUCTO_GRATIS, 0);
        beneficio.setId(UUID.randomUUID().toString());
        beneficio.setClienteId(clienteId);
        beneficio.setProductoGratisId(productoId);
        beneficio.setFechaInicioVigencia(new Date());
        beneficio.setFechaFinVigencia(agregarDias(new Date(), diasValidez));
        beneficio.setCantidadMaximaUsos(1);
        beneficio.setEsPersonalizado(true);
        beneficio.activar();
        return beneficio;
    }
    
    private Beneficio crearBeneficioDosxUno(String nombre, String descripcion, String clienteId, int diasValidez) {
        Beneficio beneficio = new Beneficio(nombre, descripcion, Beneficio.TipoBeneficio.DOS_POR_UNO, 0);
        beneficio.setId(UUID.randomUUID().toString());
        beneficio.setClienteId(clienteId);
        beneficio.setFechaInicioVigencia(new Date());
        beneficio.setFechaFinVigencia(agregarDias(new Date(), diasValidez));
        beneficio.setCantidadMaximaUsos(1);
        beneficio.setEsPersonalizado(true);
        beneficio.activar();
        return beneficio;
    }
    
    // Métodos auxiliares de validación
    private boolean esDentroDeUltimaSemana(Date fecha) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -7);
        return fecha.after(cal.getTime());
    }
    
    private boolean tieneVisitasDiariasConsecutivas(List<Visita> visitas, int diasRequeridos) {
        if (visitas.size() < diasRequeridos) return false;
        
        // Ordenar visitas por fecha
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
    
    private Date agregarDias(Date fecha, int dias) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        cal.add(Calendar.DAY_OF_YEAR, dias);
        return cal.getTime();
    }
}