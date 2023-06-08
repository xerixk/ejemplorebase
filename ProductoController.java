package es.spring.trabajo.controllers;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import es.spring.trabajo.models.entity.Producto;
import es.spring.trabajo.models.services.ProductoService;
import jakarta.validation.Valid;

@CrossOrigin(origins = { "http://localhost:4200" })
@RestController
@RequestMapping
public class ProductoController {

	private final Logger log = LoggerFactory.getLogger(ProductoController.class);

	@Autowired
	private ProductoService productoService;

	@GetMapping("/productos")
	public List<Producto> index() {
		return productoService.findAll();
	}

	@GetMapping("/productos/page/{page}")
	public Page<Producto> index(@PathVariable Integer page) {
		return productoService.findAll(PageRequest.of(page, 5));
	}

	@GetMapping("/productos/{id}")
	public ResponseEntity<?> show(@PathVariable Long id) {
		Map<String, Object> response = new HashMap<>();
		Producto producto = null;
		try {
			producto = productoService.findById(id);
		} catch (DataAccessException e) {
			response.put("mensaje", "Error en la bbdd al listar");
			response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		if (producto == null) {
			response.put("mensaje", "El producto Id: ".concat(id.toString().concat(" no existe")));
			return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(producto, HttpStatus.OK);
	}

	@PostMapping("/productos")
	@ResponseStatus(code = HttpStatus.CREATED)
	public ResponseEntity<?> create(@Valid @RequestBody Producto producto, BindingResult result) {
		UserDetails user =(UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Map<String, Object> response = new HashMap<>();
		Producto producto2 = null;
		if (result.hasErrors()) {
			List<String> errors = result.getFieldErrors().stream()
					.map(err -> "El campo '" + err.getField() + "' " + err.getDefaultMessage())
					.collect(Collectors.toList());
			response.put("errors", errors);
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		
		try {
			producto2 = productoService.save(producto);
		} catch (DataAccessException e) {
			response.put("mensaje", "Error en la bbdd al crear");
			response.put("errors", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		response.put("mensaje", "El cliente ha sido creado con exito");
		response.put("producto", producto2);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@PutMapping("/productos/{id}")
	@ResponseStatus(code = HttpStatus.CREATED)
	public ResponseEntity<?> update(@Valid @RequestBody Producto producto, BindingResult result,
			@PathVariable Long id) {
		Map<String, Object> response = new HashMap<>();
		Producto producto2 = productoService.findById(id);
		Producto producto3 = null;
		if (producto2 == null) {
			response.put("mensaje",
					"Error, no se pudo editar: El producto Id: ".concat(id.toString().concat(" no existe")));
			return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
		}
		System.out.println(result.getErrorCount());
		if (result.hasErrors()) {
			List<String> errors = result.getFieldErrors().stream()
					.map(err -> "El campo '" + err.getField() + "' " + err.getDefaultMessage())
					.collect(Collectors.toList());
			response.put("errors", errors);
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		try {
			producto2.setDescripcion(producto.getDescripcion());
			producto2.setImagen(producto.getImagen());
			producto2.setNombre(producto.getNombre());
			producto2.setPrecio(producto.getPrecio());
			producto2.setCantidad(producto.getCantidad());
			producto3 = productoService.save(producto2);
		} catch (DataAccessException e) {
			response.put("mensaje", "Error en la bbdd al actualizar");
			response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		response.put("mensaje", "El producto ha sido actualizado con exito");
		response.put("producto", producto3);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@DeleteMapping("/productos/{id}")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public ResponseEntity<?> delete(@PathVariable Long id) {
		Map<String, Object> response = new HashMap<>();
		try {
			productoService.delete(id);
		} catch (DataAccessException e) {
			response.put("mensaje", "Error en la bbdd al eliminar");
			response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		response.put("mensaje", "Producto eliminado con exito");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	
	/*Producto producto = productoService.findById(id);
			String nombreFoto = producto.getFoto();

			if (nombreFoto != null && nombreFoto.length() > 0) {
				Path rutaFoto = Paths.get("uploads").resolve(nombreFoto).toAbsolutePath();
				File archivoFoto = rutaFoto.toFile();
				if (archivoFoto.exists() && archivoFoto.canRead())
					archivoFoto.delete();
			}
			
			@PostMapping("/productos/upload")
	public ResponseEntity<?> upload(@RequestParam("archivo") MultipartFile archivo, @RequestParam("id") Long id) {
		Map<String, Object> response = new HashMap<>();

		Producto producto = productoService.findById(id);

		if (!archivo.isEmpty()) {
			String nombreArchivo = UUID.randomUUID().toString() + "_" + archivo.getOriginalFilename().replace(" ", "");
			Path rutaArchivo = Paths.get("uploads").resolve(nombreArchivo).toAbsolutePath();

			try {
				Files.copy(archivo.getInputStream(), rutaArchivo);
			} catch (IOException e) {
				response.put("mensaje", "Error al subir la imagen: " + nombreArchivo);
				response.put("error", e.getMessage().concat(": ").concat(e.getCause().getMessage()));
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}

			String nombreFoto = producto.getFoto();

			if (nombreFoto != null && nombreFoto.length() > 0) {
				Path rutaFoto = Paths.get("uploads").resolve(nombreFoto).toAbsolutePath();
				File archivoFoto = rutaFoto.toFile();
				if (archivoFoto.exists() && archivoFoto.canRead())
					archivoFoto.delete();
			}

			producto.setFoto(nombreArchivo);

			productoService.save(producto);

			response.put("usuario", producto);
			response.put("mensaje", "Has subido correctamente la imagen: " + nombreArchivo);
		}

		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}
	
	@GetMapping("/uploads/img/{nombreFoto:.+}")
	public ResponseEntity<Resource> verFoto(@PathVariable String nombreFoto) {
		Path rutaArchivo = Paths.get("uploads").resolve(nombreFoto).toAbsolutePath();

		Resource recurso = null;

		try {
			recurso = new UrlResource(rutaArchivo.toUri());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		if (!recurso.exists() && !recurso.isReadable()) {
			rutaArchivo = Paths.get("src/main/resources/static/images").resolve("no-user.png").toAbsolutePath();
			try {
				recurso = new UrlResource(rutaArchivo.toUri());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			log.error("Error no se pudo cargar la imagen: " + nombreFoto);
		}

		HttpHeaders cabecera = new HttpHeaders();
		cabecera.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getFilename() + "\"");

		return new ResponseEntity<>(recurso, cabecera, HttpStatus.OK);
	}
*/
}
