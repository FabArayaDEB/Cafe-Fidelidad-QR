package com.example.cafefidelidaqrdemo.utils;

import android.location.Location;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cafefidelidaqrdemo.database.entities.ProductoEntity;
import com.example.cafefidelidaqrdemo.database.entities.SucursalEntity;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manager para funcionalidad de búsqueda y filtrado
 * Maneja búsquedas locales inmediatas y búsquedas remotas opcionales
 */
public class SearchManager {

    private final ExecutorService searchExecutor;
    private final MutableLiveData<Boolean> isSearching;
    private final MutableLiveData<String> searchError;

    // Configuración de búsqueda
    private static final int MIN_SEARCH_LENGTH = 2;
    private static final long SEARCH_DELAY_MS = 300; // Debounce
    private static final int MAX_SEARCH_RESULTS = 50;

    public SearchManager() {
        this.searchExecutor = Executors.newSingleThreadExecutor();
        this.isSearching = new MutableLiveData<>(false);
        this.searchError = new MutableLiveData<>();
    }

    /**
     * Realiza búsqueda local de productos
     */
    public LiveData<List<ProductoEntity>> searchProductsLocal(
            List<ProductoEntity> allProducts,
            String query,
            String categoria,
            Boolean disponible) {
        
        MutableLiveData<List<ProductoEntity>> results = new MutableLiveData<>();
        
        searchExecutor.execute(() -> {
            isSearching.postValue(true);
            
            try {
                List<ProductoEntity> filteredProducts = filterProducts(
                    allProducts, query, categoria, disponible);
                
                // Ordenar por relevancia
                if (query != null && !query.trim().isEmpty()) {
                    filteredProducts = sortProductsByRelevance(filteredProducts, query);
                }
                
                results.postValue(filteredProducts);
            } catch (Exception e) {
                searchError.postValue("Error en búsqueda local: " + e.getMessage());
                results.postValue(new ArrayList<>());
            } finally {
                isSearching.postValue(false);
            }
        });
        
        return results;
    }

    /**
     * Realiza búsqueda local de sucursales
     */
    public LiveData<List<SucursalWithDistance>> searchSucursalesLocal(
            List<SucursalEntity> allSucursales,
            String query,
            Location userLocation,
            Boolean soloActivas,
            Double maxDistanceKm) {
        
        MutableLiveData<List<SucursalWithDistance>> results = new MutableLiveData<>();
        
        searchExecutor.execute(() -> {
            isSearching.postValue(true);
            
            try {
                List<SucursalWithDistance> filteredSucursales = filterSucursales(
                    allSucursales, query, userLocation, soloActivas, maxDistanceKm);
                
                // Ordenar por relevancia y distancia
                filteredSucursales = sortSucursalesByRelevanceAndDistance(
                    filteredSucursales, query, userLocation != null);
                
                results.postValue(filteredSucursales);
            } catch (Exception e) {
                searchError.postValue("Error en búsqueda local: " + e.getMessage());
                results.postValue(new ArrayList<>());
            } finally {
                isSearching.postValue(false);
            }
        });
        
        return results;
    }

    /**
     * Filtra productos por texto, categoría y disponibilidad
     */
    private List<ProductoEntity> filterProducts(
            List<ProductoEntity> products,
            String query,
            String categoria,
            Boolean disponible) {
        
        List<ProductoEntity> filtered = new ArrayList<>();
        String normalizedQuery = normalizeText(query);
        
        for (ProductoEntity product : products) {
            // Filtro por disponibilidad
            if (disponible != null && product.isDisponible() != disponible) {
                continue;
            }
            
            // Filtro por categoría
            if (categoria != null && !categoria.isEmpty() && 
                !categoria.equalsIgnoreCase("todas") &&
                !categoria.equalsIgnoreCase(product.getCategoria())) {
                continue;
            }
            
            // Filtro por texto
            if (normalizedQuery != null && !normalizedQuery.isEmpty()) {
                if (!matchesSearchQuery(product, normalizedQuery)) {
                    continue;
                }
            }
            
            filtered.add(product);
        }
        
        return filtered.size() > MAX_SEARCH_RESULTS ? 
               filtered.subList(0, MAX_SEARCH_RESULTS) : filtered;
    }

    /**
     * Filtra sucursales por texto, ubicación y estado
     */
    private List<SucursalWithDistance> filterSucursales(
            List<SucursalEntity> sucursales,
            String query,
            Location userLocation,
            Boolean soloActivas,
            Double maxDistanceKm) {
        
        List<SucursalWithDistance> filtered = new ArrayList<>();
        String normalizedQuery = normalizeText(query);
        
        for (SucursalEntity sucursal : sucursales) {
            // Filtro por estado
            if (soloActivas != null && soloActivas && !sucursal.isActiva()) {
                continue;
            }
            
            // Calcular distancia si hay ubicación del usuario
            Double distance = null;
            if (userLocation != null && 
                LocationUtils.isValidCoordinates(sucursal.getLat(), sucursal.getLon())) {
                distance = LocationUtils.calculateDistance(
                    userLocation.getLatitude(), userLocation.getLongitude(),
                    sucursal.getLat(), sucursal.getLon());
                
                // Filtro por distancia máxima
                if (maxDistanceKm != null && distance > maxDistanceKm) {
                    continue;
                }
            }
            
            // Filtro por texto
            if (normalizedQuery != null && !normalizedQuery.isEmpty()) {
                if (!matchesSearchQuery(sucursal, normalizedQuery)) {
                    continue;
                }
            }
            
            filtered.add(new SucursalWithDistance(sucursal, distance));
        }
        
        return filtered.size() > MAX_SEARCH_RESULTS ? 
               filtered.subList(0, MAX_SEARCH_RESULTS) : filtered;
    }

    /**
     * Verifica si un producto coincide con la consulta de búsqueda
     */
    private boolean matchesSearchQuery(ProductoEntity product, String normalizedQuery) {
        String productText = normalizeText(
            product.getNombre() + " " + 
            product.getDescripcion() + " " + 
            product.getCategoria());
        
        return productText.contains(normalizedQuery);
    }

    /**
     * Verifica si una sucursal coincide con la consulta de búsqueda
     */
    private boolean matchesSearchQuery(SucursalEntity sucursal, String normalizedQuery) {
        String sucursalText = normalizeText(
            sucursal.getNombre() + " " + 
            sucursal.getDireccion());
        
        return sucursalText.contains(normalizedQuery);
    }

    /**
     * Ordena productos por relevancia de búsqueda
     */
    private List<ProductoEntity> sortProductsByRelevance(
            List<ProductoEntity> products, String query) {
        
        String normalizedQuery = normalizeText(query);
        
        Collections.sort(products, (p1, p2) -> {
            int score1 = calculateProductRelevanceScore(p1, normalizedQuery);
            int score2 = calculateProductRelevanceScore(p2, normalizedQuery);
            
            // Ordenar por score descendente, luego por nombre
            if (score1 != score2) {
                return Integer.compare(score2, score1);
            }
            return p1.getNombre().compareToIgnoreCase(p2.getNombre());
        });
        
        return products;
    }

    /**
     * Ordena sucursales por relevancia y distancia
     */
    private List<SucursalWithDistance> sortSucursalesByRelevanceAndDistance(
            List<SucursalWithDistance> sucursales, String query, boolean hasLocation) {
        
        String normalizedQuery = normalizeText(query);
        
        Collections.sort(sucursales, (s1, s2) -> {
            // Si hay query, priorizar relevancia
            if (normalizedQuery != null && !normalizedQuery.isEmpty()) {
                int score1 = calculateSucursalRelevanceScore(s1.getSucursal(), normalizedQuery);
                int score2 = calculateSucursalRelevanceScore(s2.getSucursal(), normalizedQuery);
                
                if (score1 != score2) {
                    return Integer.compare(score2, score1);
                }
            }
            
            // Si hay ubicación, ordenar por distancia
            if (hasLocation && s1.getDistance() != null && s2.getDistance() != null) {
                return Double.compare(s1.getDistance(), s2.getDistance());
            }
            
            // Por defecto, ordenar por nombre
            return s1.getSucursal().getNombre().compareToIgnoreCase(
                s2.getSucursal().getNombre());
        });
        
        return sucursales;
    }

    /**
     * Calcula score de relevancia para producto
     */
    private int calculateProductRelevanceScore(ProductoEntity product, String query) {
        int score = 0;
        String normalizedName = normalizeText(product.getNombre());
        String normalizedDesc = normalizeText(product.getDescripcion());
        String normalizedCategory = normalizeText(product.getCategoria());
        
        // Coincidencia exacta en nombre (mayor score)
        if (normalizedName.equals(query)) {
            score += 100;
        } else if (normalizedName.startsWith(query)) {
            score += 50;
        } else if (normalizedName.contains(query)) {
            score += 25;
        }
        
        // Coincidencia en descripción
        if (normalizedDesc.contains(query)) {
            score += 10;
        }
        
        // Coincidencia en categoría
        if (normalizedCategory.contains(query)) {
            score += 15;
        }
        
        // Bonus por disponibilidad
        if (product.isDisponible()) {
            score += 5;
        }
        
        return score;
    }

    /**
     * Calcula score de relevancia para sucursal
     */
    private int calculateSucursalRelevanceScore(SucursalEntity sucursal, String query) {
        int score = 0;
        String normalizedName = normalizeText(sucursal.getNombre());
        String normalizedAddress = normalizeText(sucursal.getDireccion());
        
        // Coincidencia exacta en nombre (mayor score)
        if (normalizedName.equals(query)) {
            score += 100;
        } else if (normalizedName.startsWith(query)) {
            score += 50;
        } else if (normalizedName.contains(query)) {
            score += 25;
        }
        
        // Coincidencia en dirección
        if (normalizedAddress.contains(query)) {
            score += 20;
        }
        
        // Bonus por estar activa
        if (sucursal.isActiva()) {
            score += 5;
        }
        
        return score;
    }

    /**
     * Normaliza texto para búsqueda (sin acentos, minúsculas)
     */
    private String normalizeText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        
        // Convertir a minúsculas y remover acentos
        String normalized = text.toLowerCase().trim();
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        
        return normalized;
    }

    /**
     * Valida si la consulta es válida para búsqueda
     */
    public boolean isValidSearchQuery(String query) {
        return query != null && query.trim().length() >= MIN_SEARCH_LENGTH;
    }

    /**
     * Obtiene LiveData del estado de búsqueda
     */
    public LiveData<Boolean> getIsSearching() {
        return isSearching;
    }

    /**
     * Obtiene LiveData de errores de búsqueda
     */
    public LiveData<String> getSearchError() {
        return searchError;
    }

    /**
     * Limpia errores de búsqueda
     */
    public void clearSearchError() {
        searchError.setValue(null);
    }

    /**
     * Libera recursos
     */
    public void cleanup() {
        if (searchExecutor != null && !searchExecutor.isShutdown()) {
            searchExecutor.shutdown();
        }
    }

    /**
     * Clase para representar sucursal con distancia
     */
    public static class SucursalWithDistance {
        private final SucursalEntity sucursal;
        private final Double distance; // en kilómetros

        public SucursalWithDistance(SucursalEntity sucursal, Double distance) {
            this.sucursal = sucursal;
            this.distance = distance;
        }

        public SucursalEntity getSucursal() {
            return sucursal;
        }

        public Double getDistance() {
            return distance;
        }

        public String getFormattedDistance() {
            return distance != null ? LocationUtils.formatDistance(distance) : null;
        }

        public boolean hasDistance() {
            return distance != null;
        }
    }

    /**
     * Configuración de filtros de búsqueda
     */
    public static class SearchFilters {
        private String query;
        private String categoria;
        private Boolean disponible;
        private Boolean soloActivas;
        private Double maxDistanceKm;
        private Location userLocation;

        public SearchFilters() {}

        // Getters y setters
        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }

        public String getCategoria() { return categoria; }
        public void setCategoria(String categoria) { this.categoria = categoria; }

        public Boolean getDisponible() { return disponible; }
        public void setDisponible(Boolean disponible) { this.disponible = disponible; }

        public Boolean getSoloActivas() { return soloActivas; }
        public void setSoloActivas(Boolean soloActivas) { this.soloActivas = soloActivas; }

        public Double getMaxDistanceKm() { return maxDistanceKm; }
        public void setMaxDistanceKm(Double maxDistanceKm) { this.maxDistanceKm = maxDistanceKm; }

        public Location getUserLocation() { return userLocation; }
        public void setUserLocation(Location userLocation) { this.userLocation = userLocation; }
    }
}