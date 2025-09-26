package com.example.cafefidelidaqrdemo.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.location.Location;
import com.example.cafefidelidaqrdemo.models.Sucursal;
import java.util.List;
import java.util.ArrayList;

/**
 * Gestor de búsquedas simplificado
 * Maneja búsquedas básicas y cálculos de distancia
 */
public class SearchManager {
    
    /**
     * Clase para representar sucursal con distancia
     */
    public static class SucursalWithDistance {
        private Sucursal sucursal;
        private double distance;
        
        public SucursalWithDistance(Sucursal sucursal, double distance) {
            this.sucursal = sucursal;
            this.distance = distance;
        }
        
        public Sucursal getSucursal() {
            return sucursal;
        }
        
        public double getDistance() {
            return distance;
        }
        
        public void setSucursal(Sucursal sucursal) {
            this.sucursal = sucursal;
        }
        
        public void setDistance(double distance) {
            this.distance = distance;
        }
    }
    
    /**
     * Busca sucursales por nombre
     */
    public List<Sucursal> searchByName(List<Sucursal> sucursales, String query) {
        List<Sucursal> results = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return sucursales;
        }
        
        String lowerQuery = query.toLowerCase().trim();
        for (Sucursal sucursal : sucursales) {
            if (sucursal.getNombre().toLowerCase().contains(lowerQuery) ||
                sucursal.getDireccion().toLowerCase().contains(lowerQuery)) {
                results.add(sucursal);
            }
        }
        return results;
    }
    
    /**
     * Calcula distancia básica entre dos puntos
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radio de la Tierra en km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c; // Distancia en km
    }
    
    /**
     * Ordena sucursales por distancia
     */
    public List<SucursalWithDistance> sortByDistance(List<Sucursal> sucursales, 
                                                    double userLat, double userLon) {
        List<SucursalWithDistance> results = new ArrayList<>();
        
        for (Sucursal sucursal : sucursales) {
            double distance = calculateDistance(userLat, userLon, 
                                              sucursal.getLatitud(), sucursal.getLongitud());
            results.add(new SucursalWithDistance(sucursal, distance));
        }
        
        // Ordenar por distancia
        results.sort((a, b) -> Double.compare(a.getDistance(), b.getDistance()));
        
        return results;
    }
    
    /**
     * Busca sucursales localmente con filtros
     */
    public LiveData<List<SucursalWithDistance>> searchSucursalesLocal(
            List<Sucursal> sucursales, 
            String query, 
            Location userLocation, 
            Boolean sortByDistance, 
            Double maxDistance) {
        
        MutableLiveData<List<SucursalWithDistance>> result = new MutableLiveData<>();
        
        // Filtrar por query si existe
        List<Sucursal> filtered = sucursales;
        if (query != null && !query.trim().isEmpty()) {
            filtered = searchByName(sucursales, query);
        }
        
        // Convertir a SucursalWithDistance
        List<SucursalWithDistance> withDistance = new ArrayList<>();
        for (Sucursal sucursal : filtered) {
            double distance = 0;
            if (userLocation != null) {
                distance = calculateDistance(
                    userLocation.getLatitude(), userLocation.getLongitude(),
                    sucursal.getLatitud(), sucursal.getLongitud()
                );
            }
            
            // Filtrar por distancia máxima si se especifica
            if (maxDistance == null || distance <= maxDistance) {
                withDistance.add(new SucursalWithDistance(sucursal, distance));
            }
        }
        
        // Ordenar por distancia si se solicita
        if (sortByDistance != null && sortByDistance && userLocation != null) {
            withDistance.sort((a, b) -> Double.compare(a.getDistance(), b.getDistance()));
        }
        
        result.setValue(withDistance);
        return result;
    }
}