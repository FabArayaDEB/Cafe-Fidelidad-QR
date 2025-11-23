package com.example.cafefidelidaqrdemo.repository.base;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Clase base para todos los repositorios en la arquitectura MVVM
 * Proporciona funcionalidades comunes como manejo de estados y threading
 */
public abstract class BaseRepository {

    //Executor para operaciones asíncronas
    protected final ExecutorService executor;

    // Estados comunes para todos los repositorios
    protected final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    protected final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    protected final MutableLiveData<String> _successMessage = new MutableLiveData<>();
    protected final MutableLiveData<Boolean> _isOffline = new MutableLiveData<>(false);
    
    // Getters públicos para LiveData (inmutables)
    public LiveData<Boolean> getIsLoading() { return _isLoading; }
    public LiveData<String> getErrorMessage() { return _errorMessage; }
    public LiveData<String> getSuccessMessage() { return _successMessage; }
    public LiveData<Boolean> getIsOffline() { return _isOffline; }
    
    protected BaseRepository() {
        this.executor = Executors.newFixedThreadPool(4);
    }
    
    protected BaseRepository(int threadPoolSize) {
        this.executor = Executors.newFixedThreadPool(threadPoolSize);
    }
    
    /**
     * Establece el estado de carga
     */
    protected void setLoading(boolean isLoading) {
        _isLoading.postValue(isLoading);
    }
    
    /**
     * Establece un mensaje de error
     */
    protected void setError(String error) {
        _errorMessage.postValue(error);
        setLoading(false);
    }
    
    /**
     * Establece un mensaje de éxito
     */
    protected void setSuccess(String message) {
        _successMessage.postValue(message);
        setLoading(false);
    }
    
    /**
     * Limpia el mensaje de error
     */
    public void clearError() {
        _errorMessage.postValue(null);
    }
    
    /**
     * Limpia el mensaje de éxito
     */
    public void clearSuccess() {
        _successMessage.postValue(null);
    }
    
    /**
     * Establece el estado offline
     */
    protected void setOffline(boolean isOffline) {
        _isOffline.postValue(isOffline);
    }
    
    /**
     * Ejecuta una operación en background
     */
    protected void executeInBackground(Runnable operation) {
        executor.execute(operation);
    }
    
    /**
     * Limpia recursos del repositorio
     */
    public void cleanup() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
    
    /**
     * Interfaz para callbacks de operaciones asíncronas
     */
    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
    
    /**
     * Interfaz para operaciones simples sin resultado
     */
    public interface SimpleCallback {
        void onSuccess();
        void onError(String error);
    }
}