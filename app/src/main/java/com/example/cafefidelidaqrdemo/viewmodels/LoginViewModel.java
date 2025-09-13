package com.example.cafefidelidaqrdemo.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


import kotlinx.coroutines.flow.MutableStateFlow;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.coroutines.flow.StateFlowKt;
// Removiendo importación incorrecta de asLiveData

import com.example.cafefidelidaqrdemo.data.repositories.AuthRepository;

/**
 * ViewModel para LoginActivity
 * Maneja la lógica de autenticación de usuarios
 */
public class LoginViewModel extends ViewModel {
    
    private final AuthRepository authRepository;
    
    // StateFlow para el email
    private final MutableStateFlow<String> _email = StateFlowKt.MutableStateFlow("");
    public StateFlow<String> email = _email;
    
    // StateFlow para la contraseña
    private final MutableStateFlow<String> _password = StateFlowKt.MutableStateFlow("");
    public StateFlow<String> password = _password;
    
    // StateFlow para errores
    private final MutableStateFlow<String> _error = StateFlowKt.MutableStateFlow(null);
    public StateFlow<String> error = _error;
    
    // StateFlow para estado de carga
    private final MutableStateFlow<Boolean> _isLoading = StateFlowKt.MutableStateFlow(false);
    public StateFlow<Boolean> isLoading = _isLoading;
    
    // StateFlow para éxito en login
    private final MutableStateFlow<Boolean> _loginSuccess = StateFlowKt.MutableStateFlow(false);
    public StateFlow<Boolean> loginSuccess = _loginSuccess;
    
    // StateFlow para validación de email
    private final MutableStateFlow<String> _emailError = StateFlowKt.MutableStateFlow(null);
    public StateFlow<String> emailError = _emailError;
    
    // StateFlow para validación de contraseña
    private final MutableStateFlow<String> _passwordError = StateFlowKt.MutableStateFlow(null);
    public StateFlow<String> passwordError = _passwordError;
    
    // MutableLiveData para compatibilidad con Data Binding
    private final MutableLiveData<String> emailLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> passwordLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loginSuccessLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> emailErrorLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> passwordErrorLiveData = new MutableLiveData<>();
    
    // Los StateFlow se pueden usar directamente en Data Binding
    
    public LoginViewModel() {
        this.authRepository = AuthRepository.getInstance();
        // Los valores por defecto ya están inicializados en los StateFlow
    }
    
    /**
     * Actualiza el email
     */
    public void setEmail(String email) {
        _email.setValue(email);
        validateEmail(email);
    }
    
    /**
     * Actualiza la contraseña
     */
    public void setPassword(String password) {
        _password.setValue(password);
        validatePassword(password);
    }
    
    /**
     * Valida el formato del email
     */
    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            _emailError.setValue("El email es requerido");
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailError.setValue("Formato de email inválido");
        } else {
            _emailError.setValue(null);
        }
    }
    
    /**
     * Valida la contraseña
     */
    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            _passwordError.setValue("La contraseña es requerida");
        } else if (password.length() < 6) {
            _passwordError.setValue("La contraseña debe tener al menos 6 caracteres");
        } else {
            _passwordError.setValue(null);
        }
    }
    
    /**
     * Realiza el login del usuario
     */
    public void login() {
        String emailValue = _email.getValue();
        String passwordValue = _password.getValue();
        
        // Validar campos
        if (emailValue == null || emailValue.trim().isEmpty()) {
            _emailError.setValue("El email es requerido");
            return;
        }
        
        if (passwordValue == null || passwordValue.trim().isEmpty()) {
            _passwordError.setValue("La contraseña es requerida");
            return;
        }
        
        // Validar formato de email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
            _emailError.setValue("Formato de email inválido");
            return;
        }
        
        // Iniciar proceso de login
        _isLoading.setValue(true);
        _error.setValue(null);
        
        authRepository.login(emailValue, passwordValue, new AuthRepository.AuthCallback<String>() {
            @Override
            public void onSuccess(String userId) {
                _isLoading.setValue(false);
                _loginSuccess.setValue(true);
            }
            
            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _error.setValue(error);
            }
        });
    }
    
    /**
     * Limpia el mensaje de error
     */
    public void clearError() {
        _error.setValue(null);
    }
    
    /**
     * Limpia el estado de éxito de login
     */
    public void clearLoginSuccess() {
        _loginSuccess.setValue(false);
    }
    
    /**
     * Verifica si los datos son válidos para login
     */
    public boolean isValidForLogin() {
        String emailValue = _email.getValue();
        String passwordValue = _password.getValue();
        
        return emailValue != null && !emailValue.trim().isEmpty() &&
               android.util.Patterns.EMAIL_ADDRESS.matcher(emailValue).matches() &&
               passwordValue != null && !passwordValue.trim().isEmpty() &&
               passwordValue.length() >= 6;
    }
    
    // Getters para StateFlow
    public StateFlow<String> getEmail() {
        return email;
    }
    
    public StateFlow<String> getPassword() {
        return password;
    }
    
    public StateFlow<String> getError() {
        return error;
    }
    
    public StateFlow<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public StateFlow<Boolean> getLoginSuccess() {
        return loginSuccess;
    }
    
    public StateFlow<String> getEmailError() {
        return emailError;
    }
    
    public StateFlow<String> getPasswordError() {
        return passwordError;
    }
    
    // Getters para Data Binding (LiveData)
    public LiveData<String> getEmailLiveData() {
        return emailLiveData;
    }
    
    public LiveData<String> getPasswordLiveData() {
        return passwordLiveData;
    }
    
    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }
    
    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }
    
    public LiveData<Boolean> getLoginSuccessLiveData() {
        return loginSuccessLiveData;
    }
    
    public LiveData<String> getEmailErrorLiveData() {
        return emailErrorLiveData;
    }
    
    public LiveData<String> getPasswordErrorLiveData() {
        return passwordErrorLiveData;
    }
}