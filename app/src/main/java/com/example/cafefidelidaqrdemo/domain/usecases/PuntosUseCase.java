package com.example.cafefidelidaqrdemo.domain.usecases;

import com.example.cafefidelidaqrdemo.repository.ClienteRepository;
import com.example.cafefidelidaqrdemo.repository.TransaccionRepository;
import com.example.cafefidelidaqrdemo.repository.AuthRepository;
import com.example.cafefidelidaqrdemo.database.entities.ClienteEntity;
import com.example.cafefidelidaqrdemo.database.entities.TransaccionEntity;

import java.util.List;
import java.util.ArrayList;

/**
 * Use Case para operaciones de puntos y beneficios
 * Encapsula la lógica de negocio para manejo de puntos, canjes y recompensas
 */
public class PuntosUseCase {
    
    private final ClienteRepository clienteRepository;
    private final TransaccionRepository transaccionRepository;
    private final AuthRepository authRepository;
    
    // Constantes de beneficios
    private static final int PUNTOS_CAFE_GRATIS = 100;
    private static final int PUNTOS_DESCUENTO_10 = 50;
    private static final int PUNTOS_DESCUENTO_20 = 150;
    private static final int PUNTOS_POSTRE_GRATIS = 80;
    private static final int PUNTOS_BEBIDA_PREMIUM = 120;
    
    public PuntosUseCase() {
        this.clienteRepository = ClienteRepository.getInstance();
        this.transaccionRepository = TransaccionRepository.getInstance();
        this.authRepository = AuthRepository.getInstance();
    }
    
    /**
     * Modelo para representar un beneficio disponible
     */
    public static class Beneficio {
        private String id;
        private String nombre;
        private String descripcion;
        private int puntosRequeridos;
        private String icono;
        private boolean disponible;
        
        public Beneficio(String id, String nombre, String descripcion, int puntosRequeridos, String icono) {
            this.id = id;
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.puntosRequeridos = puntosRequeridos;
            this.icono = icono;
            this.disponible = true;
        }
        
        // Getters y setters
        public String getId() { return id; }
        public String getNombre() { return nombre; }
        public String getDescripcion() { return descripcion; }
        public int getPuntosRequeridos() { return puntosRequeridos; }
        public String getIcono() { return icono; }
        public boolean isDisponible() { return disponible; }
        public void setDisponible(boolean disponible) { this.disponible = disponible; }
    }
    
    /**
     * Callback para operaciones de puntos
     */
    public interface PuntosCallback {
        void onSuccess(ClienteEntity cliente);
        void onError(String error);
    }
    
    /**
     * Callback para lista de beneficios
     */
    public interface BeneficiosCallback {
        void onSuccess(List<Beneficio> beneficios);
        void onError(String error);
    }
    
    /**
     * Callback para canje de beneficios
     */
    public interface CanjeCallback {
        void onSuccess(ClienteEntity clienteActualizado, String codigoCanje);
        void onError(String error);
    }
    
    /**
     * Obtiene los puntos actuales del cliente
     */
    public void getPuntosActuales(PuntosCallback callback) {
        authRepository.getCurrentUser(new AuthRepository.AuthCallback<String>() {
            @Override
            public void onSuccess(String userId) {
                if (userId != null) {
                    clienteRepository.getClienteById(userId, new ClienteRepository.ClienteCallback() {
                        @Override
                        public void onSuccess(ClienteEntity cliente) {
                            callback.onSuccess(cliente);
                        }
                        
                        @Override
                        public void onError(String error) {
                            callback.onError("Error al obtener puntos: " + error);
                        }
                    });
                } else {
                    callback.onError("Usuario no autenticado");
                }
            }
            
            @Override
            public void onError(String error) {
                callback.onError("Error de autenticación: " + error);
            }
        });
    }
    
    /**
     * Obtiene la lista de beneficios disponibles
     */
    public void getBeneficiosDisponibles(BeneficiosCallback callback) {
        authRepository.getCurrentUser(new AuthRepository.AuthCallback<String>() {
            @Override
            public void onSuccess(String userId) {
                if (userId != null) {
                    clienteRepository.getClienteById(userId, new ClienteRepository.ClienteCallback() {
                        @Override
                        public void onSuccess(ClienteEntity cliente) {
                            List<Beneficio> beneficios = crearListaBeneficios(cliente.getPuntos());
                            callback.onSuccess(beneficios);
                        }
                        
                        @Override
                        public void onError(String error) {
                            callback.onError("Error al obtener datos del cliente: " + error);
                        }
                    });
                } else {
                    callback.onError("Usuario no autenticado");
                }
            }
            
            @Override
            public void onError(String error) {
                callback.onError("Error de autenticación: " + error);
            }
        });
    }
    
    /**
     * Crea la lista de beneficios con disponibilidad basada en puntos
     */
    private List<Beneficio> crearListaBeneficios(int puntosActuales) {
        List<Beneficio> beneficios = new ArrayList<>();
        
        Beneficio cafeGratis = new Beneficio(
            "cafe_gratis",
            "Café Gratis",
            "Disfruta de un café americano o espresso gratis",
            PUNTOS_CAFE_GRATIS,
            "☕"
        );
        cafeGratis.setDisponible(puntosActuales >= PUNTOS_CAFE_GRATIS);
        beneficios.add(cafeGratis);
        
        Beneficio descuento10 = new Beneficio(
            "descuento_10",
            "10% de Descuento",
            "Obtén 10% de descuento en tu próxima compra",
            PUNTOS_DESCUENTO_10,
            "🎫"
        );
        descuento10.setDisponible(puntosActuales >= PUNTOS_DESCUENTO_10);
        beneficios.add(descuento10);
        
        Beneficio postreGratis = new Beneficio(
            "postre_gratis",
            "Postre Gratis",
            "Elige cualquier postre de nuestra vitrina sin costo",
            PUNTOS_POSTRE_GRATIS,
            "🧁"
        );
        postreGratis.setDisponible(puntosActuales >= PUNTOS_POSTRE_GRATIS);
        beneficios.add(postreGratis);
        
        Beneficio bebidaPremium = new Beneficio(
            "bebida_premium",
            "Bebida Premium",
            "Cappuccino, Latte o Frappé de tu elección gratis",
            PUNTOS_BEBIDA_PREMIUM,
            "🥤"
        );
        bebidaPremium.setDisponible(puntosActuales >= PUNTOS_BEBIDA_PREMIUM);
        beneficios.add(bebidaPremium);
        
        Beneficio descuento20 = new Beneficio(
            "descuento_20",
            "20% de Descuento",
            "Obtén 20% de descuento en tu próxima compra",
            PUNTOS_DESCUENTO_20,
            "🎟️"
        );
        descuento20.setDisponible(puntosActuales >= PUNTOS_DESCUENTO_20);
        beneficios.add(descuento20);
        
        return beneficios;
    }
    
    /**
     * Canjea un beneficio por puntos
     */
    public void canjearBeneficio(String beneficioId, CanjeCallback callback) {
        authRepository.getCurrentUser(new AuthRepository.AuthCallback<String>() {
            @Override
            public void onSuccess(String userId) {
                if (userId != null) {
                    clienteRepository.getClienteById(userId, new ClienteRepository.ClienteCallback() {
                        @Override
                        public void onSuccess(ClienteEntity cliente) {
                            procesarCanje(cliente, beneficioId, callback);
                        }
                        
                        @Override
                        public void onError(String error) {
                            callback.onError("Error al obtener datos del cliente: " + error);
                        }
                    });
                } else {
                    callback.onError("Usuario no autenticado");
                }
            }
            
            @Override
            public void onError(String error) {
                callback.onError("Error de autenticación: " + error);
            }
        });
    }
    
    /**
     * Procesa el canje de un beneficio específico
     */
    private void procesarCanje(ClienteEntity cliente, String beneficioId, CanjeCallback callback) {
        int puntosRequeridos = getPuntosRequeridosPorBeneficio(beneficioId);
        
        if (puntosRequeridos == -1) {
            callback.onError("Beneficio no válido");
            return;
        }
        
        if (cliente.getPuntos() < puntosRequeridos) {
            callback.onError("Puntos insuficientes. Necesitas " + puntosRequeridos + " puntos");
            return;
        }
        
        // Descontar puntos
        int nuevoPuntaje = cliente.getPuntos() - puntosRequeridos;
        // ClienteEntity no tiene setPuntos, se maneja a través de transacciones
        
        // Actualizar cliente
        clienteRepository.updateCliente(cliente, new ClienteRepository.ClienteCallback() {
            @Override
            public void onSuccess(ClienteEntity clienteActualizado) {
                // Registrar transacción de canje
                registrarTransaccionCanje(cliente.getId_cliente(), beneficioId, puntosRequeridos, new TransaccionRepository.TransaccionCallback() {
                    @Override
                    public void onSuccess(TransaccionEntity transaccion) {
                        String codigoCanje = generarCodigoCanje(beneficioId);
                        callback.onSuccess(clienteActualizado, codigoCanje);
                    }
                    
                    @Override
                    public void onError(String error) {
                        // El canje se procesó pero falló el registro
                        String codigoCanje = generarCodigoCanje(beneficioId);
                        callback.onSuccess(clienteActualizado, codigoCanje);
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                callback.onError("Error al procesar canje: " + error);
            }
        });
    }
    
    /**
     * Registra una transacción de canje
     */
    private void registrarTransaccionCanje(String clienteId, String beneficioId, int puntosUsados, TransaccionRepository.TransaccionCallback callback) {
        TransaccionEntity transaccion = new TransaccionEntity();
        transaccion.setUserId(clienteId);
        transaccion.setMonto(0); // Los canjes no tienen monto monetario
        transaccion.setDescripcion("Canje: " + getNombreBeneficio(beneficioId));
        transaccion.setFecha(System.currentTimeMillis());
        transaccion.setTipo("CANJE");
        transaccion.setPuntos(-puntosUsados); // Puntos negativos para indicar uso
        
        transaccionRepository.createTransaccion(transaccion, callback);
    }
    
    /**
     * Obtiene los puntos requeridos para un beneficio específico
     */
    private int getPuntosRequeridosPorBeneficio(String beneficioId) {
        switch (beneficioId) {
            case "cafe_gratis": return PUNTOS_CAFE_GRATIS;
            case "descuento_10": return PUNTOS_DESCUENTO_10;
            case "descuento_20": return PUNTOS_DESCUENTO_20;
            case "postre_gratis": return PUNTOS_POSTRE_GRATIS;
            case "bebida_premium": return PUNTOS_BEBIDA_PREMIUM;
            default: return -1;
        }
    }
    
    /**
     * Obtiene el nombre de un beneficio por su ID
     */
    private String getNombreBeneficio(String beneficioId) {
        switch (beneficioId) {
            case "cafe_gratis": return "Café Gratis";
            case "descuento_10": return "10% de Descuento";
            case "descuento_20": return "20% de Descuento";
            case "postre_gratis": return "Postre Gratis";
            case "bebida_premium": return "Bebida Premium";
            default: return "Beneficio Desconocido";
        }
    }
    
    /**
     * Genera un código único para el canje
     */
    private String generarCodigoCanje(String beneficioId) {
        long timestamp = System.currentTimeMillis();
        String codigo = beneficioId.toUpperCase().substring(0, Math.min(4, beneficioId.length())) + 
                       String.valueOf(timestamp).substring(8); // Últimos 5 dígitos del timestamp
        return codigo;
    }
    
    /**
     * Calcula el progreso hacia el próximo beneficio
     */
    public void getProgresoProximoBeneficio(ProgresoCallback callback) {
        getPuntosActuales(new PuntosCallback() {
            @Override
            public void onSuccess(ClienteEntity cliente) {
                int puntosActuales = cliente.getPuntos();
                
                // Encontrar el próximo beneficio alcanzable
                int[] nivelesRecompensa = {PUNTOS_DESCUENTO_10, PUNTOS_POSTRE_GRATIS, PUNTOS_CAFE_GRATIS, PUNTOS_BEBIDA_PREMIUM, PUNTOS_DESCUENTO_20};
                
                for (int nivel : nivelesRecompensa) {
                    if (puntosActuales < nivel) {
                        int puntosRestantes = nivel - puntosActuales;
                        double progreso = (double) puntosActuales / nivel * 100;
                        callback.onSuccess(puntosActuales, nivel, puntosRestantes, progreso);
                        return;
                    }
                }
                
                // Si ya alcanzó todos los niveles
                callback.onSuccess(puntosActuales, PUNTOS_DESCUENTO_20, 0, 100.0);
            }
            
            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
    
    /**
     * Callback para progreso de beneficios
     */
    public interface ProgresoCallback {
        void onSuccess(int puntosActuales, int puntosObjetivo, int puntosRestantes, double porcentajeProgreso);
        void onError(String error);
    }
}