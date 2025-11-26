package com.example.cafefidelidaqrdemo.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.cafefidelidaqrdemo.repository.AuthRepository;

/**
 * ViewModel SIMPLIFICADO para LoginActivity
 */
public class LoginViewModel extends ViewModel {
    
    private final AuthRepository authRepository;
    
    // Solo LiveData - más simple
    private final MutableLiveData<String> email = new MutableLiveData<>("");
    private final MutableLiveData<String> password = new MutableLiveData<>("");
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<String> emailError = new MutableLiveData<>();
    private final MutableLiveData<String> passwordError = new MutableLiveData<>();
    
    public LoginViewModel() {
        this.authRepository = AuthRepository.getInstance();
        android.util.Log.d("LoginViewModel", "LoginViewModel inicializado");
    }
    
    public void setEmail(String email) {
        this.email.setValue(email);
        validateEmail(email);
    }
    
    public void setPassword(String password) {
        this.password.setValue(password);
        validatePassword(password);
    }
    
    /**
     * Valida el formato del email
     */
    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            emailError.setValue("El email es requerido");
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError.setValue("Formato de email inválido");
        } else {
            emailError.setValue(null);
        }
    }
    
    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            passwordError.setValue("La contraseña es requerida");
        } else {
            passwordError.setValue(null);
        }
    }
    
    /**
     * Realiza el login del usuario
     */
    public void login() {
        String emailValue = email.getValue();
        String passwordValue = password.getValue();
        
        android.util.Log.d("LoginViewModel", "Login simple - Email: " + emailValue);
        
        // Validación básica
        if (emailValue == null || emailValue.trim().isEmpty()) {
            emailError.setValue("El email es requerido");
            return;
        }
        
        if (passwordValue == null || passwordValue.trim().isEmpty()) {
            passwordError.setValue("La contraseña es requerida");
            return;
        }
        
        // Limpiar errores
        error.setValue(null);
        emailError.setValue(null);
        passwordError.setValue(null);
        
        // Iniciar login
        isLoading.setValue(true);
        
        authRepository.login(emailValue, passwordValue, new AuthRepository.AuthCallback<String>() {
            @Override
            public void onSuccess(String userId) {
                android.util.Log.d("LoginViewModel", "Login exitoso - userId: " + userId);
                // El callback puede ejecutarse en background; usar postValue
                isLoading.postValue(false);
                loginSuccess.postValue(true);
            }
            
            @Override
            public void onError(String errorMsg) {
                android.util.Log.d("LoginViewModel", "Login error: " + errorMsg);
                // El callback puede ejecutarse en background; usar postValue
                isLoading.postValue(false);
                error.postValue(errorMsg);
            }
        });
    }
    
    public void clearError() {
        error.setValue(null);
    }
    
    public void clearLoginSuccess() {
        loginSuccess.setValue(false);
    }
    
    public boolean isValidForLogin() {
        String emailValue = email.getValue();
        String passwordValue = password.getValue();
        
        return emailValue != null && !emailValue.trim().isEmpty() &&
               passwordValue != null && !passwordValue.trim().isEmpty();
    }
    // Getters para LiveData
    public LiveData<String> getEmail() {
        return email;
    }
    
    public LiveData<String> getPassword() {
        return password;
    }
    
    public LiveData<String> getError() {
        return error;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<Boolean> getLoginSuccess() {
        return loginSuccess;
    }
    
    public LiveData<String> getEmailError() {
        return emailError;
    }
    
    public LiveData<String> getPasswordError() {
        return passwordError;
    }
}
