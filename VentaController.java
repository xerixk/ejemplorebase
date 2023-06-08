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
import es.spring.trabajo.models.entity.Venta;
import es.spring.trabajo.models.services.ProductoService;
import es.spring.trabajo.models.services.UsuarioService;
import es.spring.trabajo.models.services.VentaService;
import jakarta.validation.Valid;

@CrossOrigin(origins = { "http://localhost:4200" })
@RestController
@RequestMapping
public class VentaController {

	private final Logger log = LoggerFactory.getLogger(VentaController.class);

	@Autowired
	private VentaService ventaService;
	
	@Autowired
	private UsuarioService usuarioService;

	@GetMapping("/ventas/page/{page}")
	public Page<Venta> index(@PathVariable Integer page) {
		return ventaService.findAll(PageRequest.of(page, 5));
	}

	@GetMapping("/usuarios/{id}/ventas/page/{page}")
	public Page<Venta> listaVentasPorUsuario(@PathVariable Long id ,@PathVariable Integer page){
		return ventaService.findByUsuario(usuarioService.findById(id), PageRequest.of(page,5));
	}
	
	@PostMapping("/ventas")
	@ResponseStatus(code = HttpStatus.CREATED)
	public ResponseEntity<?> create(@Valid @RequestBody Venta venta, BindingResult result) {
		Map<String, Object> response = new HashMap<>();
		Venta venta2 = null;
		if (result.hasErrors()) {
			List<String> errors = result.getFieldErrors().stream()
					.map(err -> "El campo '" + err.getField() + "' " + err.getDefaultMessage())
					.collect(Collectors.toList());
			response.put("errors", errors);
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		
		try {
			venta2 = ventaService.save(venta);
		} catch (DataAccessException e) {
			response.put("mensaje", "Error en la bbdd al crear");
			response.put("errors", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		response.put("mensaje", "El cliente ha sido creado con exito");
		response.put("producto", venta2);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}


}
