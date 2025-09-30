# Documento Técnico: Flujo de Funciones del Proyecto Café Fidelidad QR

## Índice
1. [Arquitectura General](#arquitectura-general)
2. [Flujo de Autenticación y Navegación](#flujo-de-autenticación-y-navegación)
3. [Patrón MVVM y ViewModels](#patrón-mvvm-y-viewmodels)
4. [Capa de Datos y Repositorios](#capa-de-datos-y-repositorios)
5. [Adaptadores y Fragmentos](#adaptadores-y-fragmentos)
6. [Diagramas de Flujo](#diagramas-de-flujo)

---

## Arquitectura General

El proyecto **Café Fidelidad QR** implementa una arquitectura **Clean Architecture** combinada con el patrón **MVVM (Model-View-ViewModel)** para Android. Esta arquitectura garantiza la separación de responsabilidades, facilita el mantenimiento y permite la escalabilidad del código.

### Estructura de Capas

```
Proyecto Café Fidelidad QR
├── UI Layer (Activities, Fragments, Adapters)
├── ViewModel Layer (ViewModels)
├── Domain Layer (Use Cases)
├── Data Layer (Repositories)
└── Database Layer (SQLite + Room)
```

### Componentes Principales

1. **Activities**: Puntos de entrada de la aplicación
   - `MainActivity`: Actividad principal que maneja la navegación inicial
   - `AdminMainActivity`: Interfaz principal para administradores
   - `ClienteMainActivity`: Interfaz principal para clientes
   - `LoginActivity`: Manejo de autenticación
   - `CatalogoActivity`: Visualización del catálogo de productos

2. **ViewModels**: Gestión del estado de la UI
   - `MainViewModel`: Estado global de la aplicación
   - `LoginViewModel`: Lógica de autenticación
   - `AdminDashboardViewModel`: Dashboard administrativo
   - `ProductosViewModel`: Gestión de productos
   - `BeneficiosAdminViewModel`: Administración de beneficios

3. **Repositories**: Acceso a datos
   - `AuthRepository`: Autenticación y sesiones
   - `AdminRepository`: Operaciones administrativas
   - `ProductoRepository`: Gestión de productos
   - `BeneficioRepository`: Gestión de beneficios
   - `SucursalRepository`: Gestión de sucursales

---

## Flujo de Autenticación y Navegación

### 1. Inicio de la Aplicación

```mermaid
graph TD
    A [MainActivity.onCreate] --> B[AuthRepository.setContext]
    B --> C[MainViewModel.checkAuthenticationStatus]
    C --> D{¿Usuario autenticado?}
    D -->|Sí| E[Verificar tipo de usuario]
    D -->|No| F[Redirigir a OpcionesLoginActivity]
    E --> G{¿Es Admin?}
    G -->|Sí| H[AdminMainActivity]
    G -->|No| I[ClienteMainActivity]
```

### 2. Proceso de Autenticación

**Archivo**: `LoginActivity.java`
```java
// Configuración inicial
binding = DataBindingUtil.setContentView(this, R.layout.activity_login_email);
viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
AuthRepository.getInstance().setContext(this);

// Manejo del login
binding.btnLogin.setOnClickListener(v -> {
    String email = binding.etEmail.getText().toString().trim();
    String password = binding.etPassword.getText().toString().trim();
    viewModel.login(email, password);
});
```

**Flujo en AuthRepository**:
```java
public void login(String email, String password, AuthCallback<String> callback) {
    // Validación de credenciales locales
    LocalUser user = USERS.get(email.trim());
    if (user != null && user.password.equals(password.trim())) {
        currentUser = user;
        currentUserLiveData.postValue(user);
        callback.onSuccess("Login exitoso");
    } else {
        callback.onError("Credenciales incorrectas");
    }
}
```

### 3. Navegación Post-Autenticación

**MainActivity.java** - Método `initializeMainApp()`:
```java
private void initializeMainApp() {
    AuthRepository authRepository = AuthRepository.getInstance();
    
    if (authRepository.isCurrentUserAdmin()) {
        Intent intent = new Intent(this, AdminMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    } else if (authRepository.isCurrentUserCliente()) {
        Intent intent = new Intent(this, ClienteMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    } else {
        setLogin();
    }
}
```

---

## Patrón MVVM y ViewModels

### Implementación del Patrón MVVM

El proyecto utiliza **Data Binding** y **LiveData/StateFlow** para implementar el patrón MVVM de manera reactiva.

### Ejemplo: MainViewModel

```java
public class MainViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;
    private final ClienteRepository clienteRepository;
    
    // StateFlow para el estado de autenticación
    private final MutableStateFlow<Boolean> _isAuthenticated = 
        StateFlowKt.MutableStateFlow(false);
    public StateFlow<Boolean> isAuthenticated = _isAuthenticated;
    
    // StateFlow para el cliente actual
    private final MutableStateFlow<Cliente> _currentCliente = 
        StateFlowKt.MutableStateFlow(null);
    public StateFlow<Cliente> currentCliente = _currentCliente;
    
    public void checkAuthenticationStatus() {
        _isLoading.setValue(true);
        
        if (authRepository.isUserLoggedIn()) {
            _isAuthenticated.setValue(true);
            loadCurrentCliente();
        } else {
            _isAuthenticated.setValue(false);
            _isLoading.setValue(false);
        }
    }
}
```

### Data Binding en Activities

```java
// MainActivity.java
binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
viewModel = new ViewModelProvider(this).get(MainViewModel.class);
binding.setViewModel(viewModel);
binding.setLifecycleOwner(this);
```

### Observadores Reactivos

```java
private void setupObservers() {
    // Observar estado de autenticación
    viewModel.getIsAuthenticated().observe(this, isAuthenticated -> {
        if (isAuthenticated != null) {
            if (isAuthenticated) {
                initializeMainApp();
            } else {
                setLogin();
            }
        }
    });
    
    // Observar errores
    viewModel.getError().observe(this, error -> {
        if (error != null && !error.isEmpty()) {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
    });
}
```

---

## Capa de Datos y Repositorios

### Arquitectura de Repositorios

Los repositorios actúan como una capa de abstracción entre los ViewModels y las fuentes de datos (SQLite, API, etc.).

### BaseRepository

```java
public abstract class BaseRepository {
    protected final ExecutorService executor;
    
    // Estados comunes para todos los repositorios
    protected final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    protected final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    protected final MutableLiveData<String> _successMessage = new MutableLiveData<>();
    protected final MutableLiveData<Boolean> _isOffline = new MutableLiveData<>(false);
    
    public BaseRepository() {
        this.executor = Executors.newFixedThreadPool(4);
    }
}
```

### AuthRepository - Gestión de Autenticación

```java
public class AuthRepository {
    private static AuthRepository instance;
    private SessionManager sessionManager;
    private LocalUser currentUser;
    
    // Credenciales locales para demo
    private static final Map<String, LocalUser> USERS = new HashMap<>();
    static {
        USERS.put("cliente@test.com", new LocalUser("cliente123", "Cliente Demo", "cliente", "user_001"));
        USERS.put("admin@test.com", new LocalUser("admin123", "Administrador", "admin", "admin_001"));
    }
    
    public void login(String email, String password, AuthCallback<String> callback) {
        String cleanEmail = email.trim();
        String cleanPassword = password.trim();
        
        LocalUser user = USERS.get(cleanEmail);
        if (user != null && user.password.equals(cleanPassword)) {
            currentUser = user;
            currentUserLiveData.postValue(user);
            callback.onSuccess("Login exitoso");
        } else {
            callback.onError("Credenciales incorrectas");
        }
    }
}
```

### AdminRepository - Operaciones CRUD

```java
public class AdminRepository {
    private final CafeFidelidadDB database;
    private final ApiService apiService;
    private final ExecutorService executor;
    
    // LiveData para observar cambios
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    
    public AdminRepository(Context context) {
        this.database = CafeFidelidadDB.getInstance(context);
        this.apiService = RetrofitClient.getInstance(context).getApiService();
        this.executor = Executors.newFixedThreadPool(4);
    }
    
    // Métodos CRUD para productos, beneficios, sucursales
}
```

### ProductoRepository - Gestión de Productos

```java
public class ProductoRepository implements IProductoRepository {
    private final CafeFidelidadDB database;
    private final ApiService apiService;
    private final ExecutorService executor;
    
    // LiveData para observar cambios
    private final MutableLiveData<List<Producto>> productosLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Producto>> searchResultsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    
    public void searchProductos(String query, BaseRepository.RepositoryCallback<List<Producto>> callback) {
        executor.execute(() -> {
            try {
                // Búsqueda en base de datos local
                List<Producto> productos = database.productoDao().searchProductos("%" + query + "%");
                callback.onSuccess(productos);
            } catch (Exception e) {
                callback.onError("Error en búsqueda: " + e.getMessage());
            }
        });
    }
}
```

---

## Adaptadores y Fragmentos

### Adaptadores RecyclerView

Los adaptadores manejan la presentación de listas de datos en la interfaz de usuario.

### ProductosAdapter - Adaptador Unificado

```java
public class ProductosAdapter extends ListAdapter<Producto, RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_CLIENT = 0;
    private static final int VIEW_TYPE_ADMIN = 1;
    
    private boolean isAdminMode;
    private OnProductoClickListener onProductoClickListener;
    private OnProductoAdminActionListener onProductoAdminActionListener;
    
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        
        if (viewType == VIEW_TYPE_ADMIN) {
            View view = inflater.inflate(R.layout.item_producto_admin, parent, false);
            return new AdminViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_producto, parent, false);
            return new ClientViewHolder(view);
        }
    }
    
    // ViewHolder para vista de cliente
    class ClientViewHolder extends RecyclerView.ViewHolder {
        private CardView cardProducto;
        private ImageView ivProducto;
        private TextView tvNombre, tvDescripcion, tvPrecio;
        
        public void bind(Producto producto) {
            tvNombre.setText(producto.getNombre());
            tvDescripcion.setText(producto.getDescripcion());
            tvPrecio.setText(String.format("$%.2f", producto.getPrecio()));
        }
    }
    
    // ViewHolder para vista de administrador
    class AdminViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNombre, tvDescripcion, tvPrecio;
        private ImageButton btnEditar, btnEliminar;
        
        public void bind(Producto producto) {
            // Configuración específica para admin
        }
    }
}
```

### BeneficiosAdminAdapter

```java
public class BeneficiosAdminAdapter extends RecyclerView.Adapter<BeneficiosAdminAdapter.BeneficioViewHolder> {
    private List<Beneficio> beneficios;
    private final OnBeneficioActionListener listener;
    
    public interface OnBeneficioActionListener {
        void onEditarBeneficio(Beneficio beneficio);
        void onEliminarBeneficio(Beneficio beneficio);
        void onToggleActivoBeneficio(Beneficio beneficio);
    }
    
    class BeneficioViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final TextView textNombre, textDescripcion;
        private final MaterialSwitch switchActivo;
        private final MaterialButton btnEditar, btnEliminar;
        
        public void bind(Beneficio beneficio) {
            textNombre.setText(beneficio.getNombre());
            textDescripcion.setText(beneficio.getDescripcion());
            switchActivo.setChecked(beneficio.isActivo());
            
            // Configurar listeners
            btnEditar.setOnClickListener(v -> listener.onEditarBeneficio(beneficio));
            btnEliminar.setOnClickListener(v -> listener.onEliminarBeneficio(beneficio));
            switchActivo.setOnCheckedChangeListener((v, isChecked) -> 
                listener.onToggleActivoBeneficio(beneficio));
        }
    }
}
```

### Fragmentos Principales

### FragmentAdminDashboard

```java
public class FragmentAdminDashboard extends Fragment {
    private FragmentAdminDashboardBinding binding;
    private AdminDashboardViewModel viewModel;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(this).get(AdminDashboardViewModel.class);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        
        setupObservers();
        setupClickListeners();
    }
    
    private void setupObservers() {
        viewModel.getEstadisticas().observe(getViewLifecycleOwner(), estadisticas -> {
            if (estadisticas != null) {
                updateDashboard(estadisticas);
            }
        });
    }
}
```

### FragmentBeneficiosAdmin

```java
public class FragmentBeneficiosAdmin extends Fragment implements BeneficiosAdminAdapter.OnBeneficioActionListener {
    private BeneficiosAdminAdapter adapter;
    private BeneficiosAdminViewModel viewModel;
    
    private void setupRecyclerView() {
        adapter = new BeneficiosAdminAdapter(new ArrayList<>(), this);
        recyclerViewBeneficios.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewBeneficios.setAdapter(adapter);
    }
    
    private void setupObservers() {
        viewModel.getBeneficios().observe(getViewLifecycleOwner(), beneficios -> {
            if (beneficios != null) {
                adapter.updateBeneficios(beneficios);
                updateEmptyState(beneficios.isEmpty());
            }
        });
    }
    
    @Override
    public void onEditarBeneficio(Beneficio beneficio) {
        // Lógica para editar beneficio
    }
    
    @Override
    public void onEliminarBeneficio(Beneficio beneficio) {
        // Lógica para eliminar beneficio
    }
}
```

---

## Diagramas de Flujo

### Flujo Principal de la Aplicación

```
[Inicio App] → [MainActivity]
     ↓
[Verificar Autenticación]
     ↓
[¿Autenticado?] → No → [OpcionesLoginActivity] → [LoginActivity]
     ↓ Sí                                              ↓
[Verificar Tipo Usuario]                        [AuthRepository.login()]
     ↓                                                 ↓
[Admin] → [AdminMainActivity]                   [¿Credenciales válidas?]
     ↓                                                 ↓ Sí
[Cliente] → [ClienteMainActivity]               [Establecer sesión]
                                                      ↓
                                               [Redirigir según tipo]
```

### Flujo de Datos en MVVM

```
[View/Fragment] ←→ [ViewModel] ←→ [Repository] ←→ [Database/API]
       ↑                ↑              ↑              ↑
   Data Binding    LiveData/      Callbacks      SQLite/
   Click Events    StateFlow                     Retrofit
```

### Flujo de Operaciones CRUD

```
[Admin UI] → [ViewModel] → [AdminRepository] → [Database]
     ↑            ↓              ↓                ↓
[Actualizar UI] ← [LiveData] ← [Callback] ← [Operación SQL]
```

---

## Conclusión

El proyecto **Café Fidelidad QR** implementa una arquitectura robusta y escalable que separa las responsabilidades:

1. **UI Layer**: Maneja la presentación y interacción del usuario
2. **ViewModel Layer**: Gestiona el estado y la lógica de presentación
3. **Repository Layer**: Abstrae el acceso a datos
4. **Database Layer**: Persistencia local con SQLite

Esta arquitectura facilita:
- **Mantenimiento**: Código organizado y modular
- **Testing**: Cada capa puede probarse independientemente
- **Escalabilidad**: Fácil agregar nuevas funcionalidades
- **Reutilización**: Componentes reutilizables entre diferentes partes de la app

El flujo de datos es unidireccional y reactivo, garantizando una experiencia de usuario consistente y un código mantenible.