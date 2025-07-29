package IntegracionBackFront.backfront.Controller.Categories;

import IntegracionBackFront.backfront.Exceptions.Category.ExceptionCategoryNotFound;
import IntegracionBackFront.backfront.Exceptions.Category.ExceptionColumnDuplicate;
import IntegracionBackFront.backfront.Models.DTO.Categories.CategoryDTO;
import IntegracionBackFront.backfront.Services.Categories.CategoryService;
import IntegracionBackFront.backfront.Services.Products.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/category")
@CrossOrigin
public class CategoryController {

    @Autowired
    private CategoryService service;

    @GetMapping("/getDataCategory")
    private ResponseEntity<Page<CategoryDTO>> getData(
            //Parametros en donde se manda la cantidad de paginas inicial y la cantidad de datos que se quieren ver en cada pagina
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size){
        //El tamaño de registro minimo tiene que ser 1 y el maximo dependera de nosoytros en este caso sera de 50 registros por pagina
        if (size <= 0 || size > 50){
            ResponseEntity.badRequest().body(Map.of(
                    //Si esto no se cumple se manda un badrequest de que ha sobrepasado el tamaño
               "Status", "El tamaño de la pagina debe de estar entre 1 y 50"
            ));
            //Y se retorna null es decir no se muestra ningun dato
            return ResponseEntity.ok(null);
        }
        //Si los parametros de size esta entre nuestras medidas entonces se mandan la cantidad de paginas incial y la cantidad de paginas por dato al metodo getAllCategories de nuestro service
        Page<CategoryDTO> category = service.getAllCategories(page, size);
        //Si category es null significa que ha ocurrido un error al obtener los datos, por ende se manda un badrequest
        if (category == null){
            ResponseEntity.badRequest().body(Map.of(
               "Status", "Error al obtener los datos"
            ));
        }
        //Si logramos obtener los datos con exito mandamos los datos para que se muestren y una respuiesta ok es decir un respuesta 200
        return ResponseEntity.ok(category);


    }

    @PostMapping("/newCategory")
    private ResponseEntity<Map<String, Object>> inserCategory(@Valid @RequestBody CategoryDTO json, HttpServletRequest request){
        try{
            CategoryDTO response =service.insert(json);
            if (response == null){
                return ResponseEntity.badRequest().body(Map.of(
                        "Error", "Inserción incorrecta",
                        "Estatus", "Inserción incorrecta",
                        "Descripción", "Verifique los valores"
                ));
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "Estado", "Completado",
                "data", response
            ));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "error",
                            "message", "Error al registrar Categoria",
                            "detail", e.getMessage()
                    ));
        }
    }

    @PutMapping("/updateCategory/{id}")
    public ResponseEntity<?> modificarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDTO usuario,
            BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            Map<String, String> errores = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errores.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errores);
        }

        try{
            CategoryDTO usuarioActualizado = service.update(id, usuario);
            return ResponseEntity.ok(usuarioActualizado);
        }
        catch (ExceptionCategoryNotFound e){
            return ResponseEntity.notFound().build();
        }
        catch (ExceptionColumnDuplicate e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("error", "Datos duplicados","campo", e.getColumnDuplicate())
            );
        }
    }

    // Mapea este metodo a una petición DELETE con un parámetro de ruta {id}
    @DeleteMapping("/deleteCategory/{id}")
    public ResponseEntity<Map<String, Object>> eliminarUsuario(@PathVariable Long id) {
        try {
            // Intenta eliminar la categoria usando objeto 'service'
            // Si el metodo delete retorna false (no encontró la categoria)
            if (!service.delete(id)) {
                // Retorna una respuesta 404 (Not Found) con información detallada
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        // Agrega un header personalizado
                        .header("X-Mensaje-Error", "Categoría no encontrada")
                        // Cuerpo de la respuesta con detalles del error
                        .body(Map.of(
                                "error", "Not found",  // Tipo de error
                                "mensaje", "La categoria no ha sido encontrada",  // Mensaje descriptivo
                                "timestamp", Instant.now().toString()  // Marca de tiempo del error
                        ));
            }

            // Si la eliminación fue exitosa, retorna 200 (OK) con mensaje de confirmación
            return ResponseEntity.ok().body(Map.of(
                    "status", "Proceso completado",  // Estado de la operación
                    "message", "Categoría eliminada exitosamente"  // Mensaje de éxito
            ));

        } catch (Exception e) {
            // Si ocurre cualquier error inesperado, retorna 500 (Internal Server Error)
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "Error",  // Indicador de error
                    "message", "Error al eliminar la categoría",  // Mensaje general
                    "detail", e.getMessage()  // Detalles técnicos del error (para debugging)
            ));
        }
    }

}
