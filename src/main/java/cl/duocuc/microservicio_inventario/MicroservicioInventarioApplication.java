package cl.duocuc.microservicio_inventario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class MicroservicioInventarioApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicroservicioInventarioApplication.class, args);
        System.out.println("================================================");
        System.out.println("  MICROSERVICIO DE INVENTARIO - VERSION 1.0    ");
        System.out.println("================================================");
        System.out.println("Estado: ACTIVO");
        System.out.println("Puerto: 8081");
        System.out.println("Base de Datos: Oracle 21c");
        System.out.println("Esquema: INV_USER");
        System.out.println("");
        System.out.println("ENDPOINTS DISPONIBLES:");
        System.out.println("GET    /api/productos                    - Listar todos los productos");
        System.out.println("GET    /api/productos/{id}               - Obtener producto por ID");
        System.out.println("POST   /api/productos                    - Crear nuevo producto");
        System.out.println("PUT    /api/productos/{id}               - Actualizar producto existente");
        System.out.println("DELETE /api/productos/{id}               - Eliminar producto");
        System.out.println("GET    /api/productos/buscar?nombre=xxx  - Buscar productos por nombre");
        System.out.println("GET    /api/productos/stats              - Estadísticas del inventario");
        System.out.println("GET    /api/productos/health             - Estado del servicio");
        System.out.println("GET    /api/productos/low-stock          - Productos con bajo stock");
        System.out.println("");
        System.out.println("URL Base: http://localhost:8081");
        System.out.println("Documentación: http://localhost:8081/api/productos/docs");
        System.out.println("================================================");
    }
}

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*", maxAge = 3600)
class ProductoController {

    @Autowired
    private DataSource dataSource;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ENDPOINT: GET /api/productos - LISTAR TODOS LOS PRODUCTOS
    @GetMapping
    public ResponseEntity<Map<String, Object>> listarTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        logRequest("GET", "/api/productos", "Listando productos - Página: " + page + ", Tamaño: " + size);
        Map<String, Object> response = createResponse();
        
        try (Connection conn = dataSource.getConnection()) {
            // Consulta con paginación y ordenamiento
            String orderDirection = sortDir.equalsIgnoreCase("desc") ? "DESC" : "ASC";
            String sql = String.format(
                "SELECT * FROM (SELECT ID, NOMBRE, DESCRIPCION, CANTIDAD, PRECIO, ROW_NUMBER() OVER (ORDER BY %s %s) as rn FROM PRODUCTOS) WHERE rn BETWEEN ? AND ?",
                sortBy.toUpperCase(), orderDirection
            );
            
            int offset = page * size + 1;
            int limit = (page + 1) * size;
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, offset);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();
            
            List<Map<String, Object>> productos = new ArrayList<>();
            while (rs.next()) {
                productos.add(buildProductoMap(rs));
            }
            
            // Contar total de registros
            PreparedStatement countStmt = conn.prepareStatement("SELECT COUNT(*) as total FROM PRODUCTOS");
            ResultSet countRs = countStmt.executeQuery();
            int total = 0;
            if (countRs.next()) {
                total = countRs.getInt("total");
            }
            
            response.put("success", true);
            response.put("message", "Productos obtenidos exitosamente");
            response.put("data", productos);
            response.put("pagination", Map.of(
                "page", page,
                "size", size,
                "total", total,
                "totalPages", (int) Math.ceil((double) total / size)
            ));
            
            logSuccess("Productos encontrados: " + productos.size() + "/" + total);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return handleError(response, "Error al listar productos", e);
        }
    }

    // ENDPOINT: GET /api/productos/{id} - OBTENER PRODUCTO POR ID
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerPorId(@PathVariable Long id) {
        logRequest("GET", "/api/productos/" + id, "Buscando producto por ID");
        Map<String, Object> response = createResponse();
        
        if (id == null || id <= 0) {
            response.put("success", false);
            response.put("message", "ID inválido. Debe ser un número positivo");
            return ResponseEntity.badRequest().body(response);
        }
        
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT ID, NOMBRE, DESCRIPCION, CANTIDAD, PRECIO FROM PRODUCTOS WHERE ID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Map<String, Object> producto = buildProductoMap(rs);
                response.put("success", true);
                response.put("message", "Producto encontrado");
                response.put("data", producto);
                
                logSuccess("Producto encontrado: " + producto.get("nombre"));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Producto no encontrado con ID: " + id);
                logWarning("Producto no encontrado con ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
        } catch (Exception e) {
            return handleError(response, "Error al buscar producto", e);
        }
    }

    // ENDPOINT: POST /api/productos - CREAR NUEVO PRODUCTO
    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(@RequestBody Map<String, Object> datos) {
        logRequest("POST", "/api/productos", "Creando nuevo producto: " + datos.get("nombre"));
        Map<String, Object> response = createResponse();
        
        // Validaciones exhaustivas
        String validationError = validateProductoData(datos, false);
        if (validationError != null) {
            response.put("success", false);
            response.put("message", validationError);
            return ResponseEntity.badRequest().body(response);
        }
        
        try (Connection conn = dataSource.getConnection()) {
            // Verificar si ya existe un producto con el mismo nombre
            String checkSql = "SELECT COUNT(*) FROM PRODUCTOS WHERE UPPER(NOMBRE) = UPPER(?)";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, datos.get("nombre").toString());
            ResultSet checkRs = checkStmt.executeQuery();
            
            if (checkRs.next() && checkRs.getInt(1) > 0) {
                response.put("success", false);
                response.put("message", "Ya existe un producto con el nombre: " + datos.get("nombre"));
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            
            String sql = "INSERT INTO PRODUCTOS (NOMBRE, DESCRIPCION, CANTIDAD, PRECIO) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            stmt.setString(1, datos.get("nombre").toString().trim());
            stmt.setString(2, datos.get("descripcion") != null ? datos.get("descripcion").toString().trim() : "");
            stmt.setInt(3, Integer.parseInt(datos.get("cantidad").toString()));
            stmt.setDouble(4, Double.parseDouble(datos.get("precio").toString()));
            
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                Long nuevoId = null;
                if (generatedKeys.next()) {
                    nuevoId = generatedKeys.getLong(1);
                }
                
                Map<String, Object> productoCreado = Map.of(
                    "id", nuevoId,
                    "nombre", datos.get("nombre").toString().trim(),
                    "descripcion", datos.get("descripcion") != null ? datos.get("descripcion").toString().trim() : "",
                    "cantidad", Integer.parseInt(datos.get("cantidad").toString()),
                    "precio", Double.parseDouble(datos.get("precio").toString())
                );
                
                response.put("success", true);
                response.put("message", "Producto creado exitosamente");
                response.put("data", productoCreado);
                
                logSuccess("Producto creado con ID: " + nuevoId);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                throw new RuntimeException("No se pudo insertar el producto en la base de datos");
            }
            
        } catch (Exception e) {
            return handleError(response, "Error al crear producto", e);
        }
    }

    // ENDPOINT: PUT /api/productos/{id} - ACTUALIZAR PRODUCTO
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> actualizar(@PathVariable Long id, @RequestBody Map<String, Object> datos) {
        logRequest("PUT", "/api/productos/" + id, "Actualizando producto");
        Map<String, Object> response = createResponse();
        
        if (id == null || id <= 0) {
            response.put("success", false);
            response.put("message", "ID inválido. Debe ser un número positivo");
            return ResponseEntity.badRequest().body(response);
        }
        
        String validationError = validateProductoData(datos, true);
        if (validationError != null) {
            response.put("success", false);
            response.put("message", validationError);
            return ResponseEntity.badRequest().body(response);
        }
        
        try (Connection conn = dataSource.getConnection()) {
            // Verificar que el producto existe
            String checkSql = "SELECT COUNT(*) FROM PRODUCTOS WHERE ID = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setLong(1, id);
            ResultSet checkRs = checkStmt.executeQuery();
            
            if (!checkRs.next() || checkRs.getInt(1) == 0) {
                response.put("success", false);
                response.put("message", "Producto no encontrado con ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Verificar si otro producto tiene el mismo nombre
            String duplicateSql = "SELECT COUNT(*) FROM PRODUCTOS WHERE UPPER(NOMBRE) = UPPER(?) AND ID != ?";
            PreparedStatement duplicateStmt = conn.prepareStatement(duplicateSql);
            duplicateStmt.setString(1, datos.get("nombre").toString());
            duplicateStmt.setLong(2, id);
            ResultSet duplicateRs = duplicateStmt.executeQuery();
            
            if (duplicateRs.next() && duplicateRs.getInt(1) > 0) {
                response.put("success", false);
                response.put("message", "Ya existe otro producto con el nombre: " + datos.get("nombre"));
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            
            String sql = "UPDATE PRODUCTOS SET NOMBRE = ?, DESCRIPCION = ?, CANTIDAD = ?, PRECIO = ? WHERE ID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setString(1, datos.get("nombre").toString().trim());
            stmt.setString(2, datos.get("descripcion") != null ? datos.get("descripcion").toString().trim() : "");
            stmt.setInt(3, Integer.parseInt(datos.get("cantidad").toString()));
            stmt.setDouble(4, Double.parseDouble(datos.get("precio").toString()));
            stmt.setLong(5, id);
            
            int filasActualizadas = stmt.executeUpdate();
            
            if (filasActualizadas > 0) {
                Map<String, Object> productoActualizado = Map.of(
                    "id", id,
                    "nombre", datos.get("nombre").toString().trim(),
                    "descripcion", datos.get("descripcion") != null ? datos.get("descripcion").toString().trim() : "",
                    "cantidad", Integer.parseInt(datos.get("cantidad").toString()),
                    "precio", Double.parseDouble(datos.get("precio").toString())
                );
                
                response.put("success", true);
                response.put("message", "Producto actualizado exitosamente");
                response.put("data", productoActualizado);
                
                logSuccess("Producto actualizado exitosamente");
                return ResponseEntity.ok(response);
            } else {
                throw new RuntimeException("No se pudo actualizar el producto");
            }
            
        } catch (Exception e) {
            return handleError(response, "Error al actualizar producto", e);
        }
    }

    // ENDPOINT: DELETE /api/productos/{id} - ELIMINAR PRODUCTO
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminar(@PathVariable Long id) {
        logRequest("DELETE", "/api/productos/" + id, "Eliminando producto");
        Map<String, Object> response = createResponse();
        
        if (id == null || id <= 0) {
            response.put("success", false);
            response.put("message", "ID inválido. Debe ser un número positivo");
            return ResponseEntity.badRequest().body(response);
        }
        
        try (Connection conn = dataSource.getConnection()) {
            // Primero obtener información del producto antes de eliminarlo
            String selectSql = "SELECT NOMBRE FROM PRODUCTOS WHERE ID = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectSql);
            selectStmt.setLong(1, id);
            ResultSet selectRs = selectStmt.executeQuery();
            
            String nombreProducto = null;
            if (selectRs.next()) {
                nombreProducto = selectRs.getString("NOMBRE");
            } else {
                response.put("success", false);
                response.put("message", "Producto no encontrado con ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            String sql = "DELETE FROM PRODUCTOS WHERE ID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);
            
            int filasEliminadas = stmt.executeUpdate();
            
            if (filasEliminadas > 0) {
                response.put("success", true);
                response.put("message", "Producto eliminado exitosamente");
                response.put("data", Map.of(
                    "id_eliminado", id,
                    "nombre_eliminado", nombreProducto
                ));
                
                logSuccess("Producto eliminado: " + nombreProducto);
                return ResponseEntity.ok(response);
            } else {
                throw new RuntimeException("No se pudo eliminar el producto");
            }
            
        } catch (Exception e) {
            return handleError(response, "Error al eliminar producto", e);
        }
    }

    // ENDPOINT: GET /api/productos/buscar - BUSCAR PRODUCTOS POR NOMBRE
    @GetMapping("/buscar")
    public ResponseEntity<Map<String, Object>> buscar(@RequestParam String nombre) {
        logRequest("GET", "/api/productos/buscar", "Buscando productos con nombre: " + nombre);
        Map<String, Object> response = createResponse();
        
        if (nombre == null || nombre.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "El parámetro 'nombre' es requerido y no puede estar vacío");
            return ResponseEntity.badRequest().body(response);
        }
        
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT ID, NOMBRE, DESCRIPCION, CANTIDAD, PRECIO FROM PRODUCTOS WHERE UPPER(NOMBRE) LIKE UPPER(?) ORDER BY NOMBRE";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + nombre.trim() + "%");
            ResultSet rs = stmt.executeQuery();
            
            List<Map<String, Object>> productos = new ArrayList<>();
            while (rs.next()) {
                productos.add(buildProductoMap(rs));
            }
            
            response.put("success", true);
            response.put("message", "Búsqueda completada exitosamente");
            response.put("data", productos);
            response.put("meta", Map.of(
                "termino_busqueda", nombre.trim(),
                "total_encontrados", productos.size()
            ));
            
            logSuccess("Productos encontrados con '" + nombre + "': " + productos.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return handleError(response, "Error en la búsqueda", e);
        }
    }

    // ENDPOINT: GET /api/productos/stats - ESTADÍSTICAS DEL INVENTARIO
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> estadisticas() {
        logRequest("GET", "/api/productos/stats", "Obteniendo estadísticas del inventario");
        Map<String, Object> response = createResponse();
        
        try (Connection conn = dataSource.getConnection()) {
            Map<String, Object> stats = new HashMap<>();
            
            // Total de productos
            PreparedStatement totalStmt = conn.prepareStatement("SELECT COUNT(*) as total FROM PRODUCTOS");
            ResultSet totalRs = totalStmt.executeQuery();
            if (totalRs.next()) {
                stats.put("total_productos", totalRs.getInt("total"));
            }
            
            // Total de stock
            PreparedStatement stockStmt = conn.prepareStatement("SELECT SUM(CANTIDAD) as total_stock FROM PRODUCTOS");
            ResultSet stockRs = stockStmt.executeQuery();
            if (stockRs.next()) {
                stats.put("total_stock", stockRs.getInt("total_stock"));
            }
            
            // Valor total del inventario
            PreparedStatement valorStmt = conn.prepareStatement("SELECT SUM(CANTIDAD * PRECIO) as valor_total FROM PRODUCTOS");
            ResultSet valorRs = valorStmt.executeQuery();
            if (valorRs.next()) {
                stats.put("valor_total_inventario", Math.round(valorRs.getDouble("valor_total") * 100.0) / 100.0);
            }
            
            // Productos con bajo stock (cantidad < 5)
            PreparedStatement bajoStockStmt = conn.prepareStatement("SELECT COUNT(*) as bajo_stock FROM PRODUCTOS WHERE CANTIDAD < 5");
            ResultSet bajoStockRs = bajoStockStmt.executeQuery();
            if (bajoStockRs.next()) {
                stats.put("productos_bajo_stock", bajoStockRs.getInt("bajo_stock"));
            }
            
            // Producto más caro
            PreparedStatement caroStmt = conn.prepareStatement("SELECT NOMBRE, PRECIO FROM PRODUCTOS WHERE PRECIO = (SELECT MAX(PRECIO) FROM PRODUCTOS)");
            ResultSet caroRs = caroStmt.executeQuery();
            if (caroRs.next()) {
                stats.put("producto_mas_caro", Map.of(
                    "nombre", caroRs.getString("NOMBRE"),
                    "precio", caroRs.getDouble("PRECIO")
                ));
            }
            
            response.put("success", true);
            response.put("message", "Estadísticas obtenidas exitosamente");
            response.put("data", stats);
            
            logSuccess("Estadísticas generadas exitosamente");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return handleError(response, "Error al obtener estadísticas", e);
        }
    }

    // ENDPOINT: GET /api/productos/low-stock - PRODUCTOS CON BAJO STOCK
    @GetMapping("/low-stock")
    public ResponseEntity<Map<String, Object>> bajoStock(@RequestParam(defaultValue = "5") int limite) {
        logRequest("GET", "/api/productos/low-stock", "Obteniendo productos con stock menor a: " + limite);
        Map<String, Object> response = createResponse();
        
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT ID, NOMBRE, DESCRIPCION, CANTIDAD, PRECIO FROM PRODUCTOS WHERE CANTIDAD < ? ORDER BY CANTIDAD ASC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, limite);
            ResultSet rs = stmt.executeQuery();
            
            List<Map<String, Object>> productos = new ArrayList<>();
            while (rs.next()) {
                productos.add(buildProductoMap(rs));
            }
            
            response.put("success", true);
            response.put("message", "Productos con bajo stock obtenidos exitosamente");
            response.put("data", productos);
            response.put("meta", Map.of(
                "limite_stock", limite,
                "total_productos_bajo_stock", productos.size()
            ));
            
            logSuccess("Productos con bajo stock encontrados: " + productos.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return handleError(response, "Error al obtener productos con bajo stock", e);
        }
    }

    // ENDPOINT: GET /api/productos/health - ESTADO DEL SERVICIO
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = createResponse();
        
        try (Connection conn = dataSource.getConnection()) {
            response.put("success", true);
            response.put("message", "Servicio funcionando correctamente");
            response.put("data", Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now().format(formatter),
                "database", Map.of(
                    "status", "CONNECTED",
                    "product_name", conn.getMetaData().getDatabaseProductName(),
                    "version", conn.getMetaData().getDatabaseProductVersion()
                ),
                "version", "1.0.0",
                "uptime", "Running"
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Servicio con problemas");
            response.put("data", Map.of(
                "status", "DOWN",
                "timestamp", LocalDateTime.now().format(formatter),
                "error", e.getMessage()
            ));
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }

    // ENDPOINT: GET /api/productos/docs - DOCUMENTACIÓN DE LA API
    @GetMapping("/docs")
    public ResponseEntity<Map<String, Object>> documentacion() {
        Map<String, Object> docs = Map.of(
            "api_name", "Microservicio de Inventario",
            "version", "1.0.0",
            "description", "API REST para gestión de productos e inventario",
            "base_url", "http://localhost:8081/api/productos",
            "endpoints", List.of(
                Map.of("method", "GET", "path", "/", "description", "Listar todos los productos con paginación"),
                Map.of("method", "GET", "path", "/{id}", "description", "Obtener producto por ID"),
                Map.of("method", "POST", "path", "/", "description", "Crear nuevo producto"),
                Map.of("method", "PUT", "path", "/{id}", "description", "Actualizar producto existente"),
                Map.of("method", "DELETE", "path", "/{id}", "description", "Eliminar producto"),
                Map.of("method", "GET", "path", "/buscar?nombre=xxx", "description", "Buscar productos por nombre"),
                Map.of("method", "GET", "path", "/stats", "description", "Obtener estadísticas del inventario"),
                Map.of("method", "GET", "path", "/low-stock", "description", "Productos con bajo stock"),
                Map.of("method", "GET", "path", "/health", "description", "Estado del servicio")
            ),
            "example_product", Map.of(
                "nombre", "Laptop HP",
                "descripcion", "Laptop para oficina",
                "cantidad", 10,
                "precio", 850000.50
            )
        );
        
        return ResponseEntity.ok(docs);
    }

    // MÉTODOS AUXILIARES
    private Map<String, Object> createResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().format(formatter));
        response.put("service", "microservicio-inventario");
        return response;
    }

    private Map<String, Object> buildProductoMap(ResultSet rs) throws Exception {
        Map<String, Object> producto = new HashMap<>();
        producto.put("id", rs.getLong("ID"));
        producto.put("nombre", rs.getString("NOMBRE"));
        producto.put("descripcion", rs.getString("DESCRIPCION"));
        producto.put("cantidad", rs.getInt("CANTIDAD"));
        producto.put("precio", rs.getDouble("PRECIO"));
        return producto;
    }

    private String validateProductoData(Map<String, Object> datos, boolean isUpdate) {
        if (datos.get("nombre") == null || datos.get("nombre").toString().trim().isEmpty()) {
            return "El campo 'nombre' es obligatorio y no puede estar vacío";
        }
        
        if (datos.get("nombre").toString().length() > 100) {
            return "El nombre no puede exceder 100 caracteres";
        }
        
        if (datos.get("descripcion") != null && datos.get("descripcion").toString().length() > 4000) {
            return "La descripción no puede exceder 4000 caracteres";
        }
        
        try {
            int cantidad = Integer.parseInt(datos.get("cantidad").toString());
            if (cantidad < 0) {
                return "La cantidad no puede ser negativa";
            }
        } catch (Exception e) {
            return "La cantidad debe ser un número entero válido";
        }
        
        try {
            double precio = Double.parseDouble(datos.get("precio").toString());
            if (precio < 0) {
                return "El precio no puede ser negativo";
            }
        } catch (Exception e) {
            return "El precio debe ser un número válido";
        }
        
        return null;
    }

    private ResponseEntity<Map<String, Object>> handleError(Map<String, Object> response, String message, Exception e) {
        logError(message + ": " + e.getMessage());
        response.put("success", false);
        response.put("message", message);
        response.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private void logRequest(String method, String endpoint, String message) {
        System.out.println(String.format("[%s] %s %s - %s", 
            LocalDateTime.now().format(formatter), method, endpoint, message));
    }

    private void logSuccess(String message) {
        System.out.println(String.format("[%s] SUCCESS - %s", 
            LocalDateTime.now().format(formatter), message));
    }

    private void logWarning(String message) {
        System.out.println(String.format("[%s] WARNING - %s", 
            LocalDateTime.now().format(formatter), message));
    }

    private void logError(String message) {
        System.err.println(String.format("[%s] ERROR - %s", 
            LocalDateTime.now().format(formatter), message));
    }
}