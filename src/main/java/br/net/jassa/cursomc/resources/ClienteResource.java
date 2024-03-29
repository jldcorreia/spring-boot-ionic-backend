package br.net.jassa.cursomc.resources;


import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.net.jassa.cursomc.domain.Cliente;
import br.net.jassa.cursomc.dto.ClienteDTO;
import br.net.jassa.cursomc.dto.ClienteNewDTO;
import br.net.jassa.cursomc.services.ClienteService;

@RestController
@RequestMapping(value = "/clientes")
public class ClienteResource {
	
	@Autowired
	private ClienteService service;
	
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public ResponseEntity<Cliente> find(@PathVariable Integer id) {
		Cliente obj = service.find(id);
		return ResponseEntity.ok().body(obj);
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<Void> insert(@Valid @RequestBody ClienteNewDTO objDto) {
		Cliente obj = service.fromDTO(objDto);
		obj = service.insert(obj);
		URI uri = ServletUriComponentsBuilder
				       .fromCurrentRequest()
				       .path("/{id}")
				       .buildAndExpand(obj.getId())
				       .toUri();
		return ResponseEntity.created(uri).build();
	}	
	
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	public ResponseEntity<Void> update(@Valid @RequestBody ClienteDTO objDto, @PathVariable Integer id) {
		Cliente obj = service.fromDTO(objDto);
		obj.setId(id);
		obj = service.update(obj);
		return ResponseEntity.noContent().build();
	}
	
	@PreAuthorize("hasAnyRole('ADMIN')")
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<Void> delete(@PathVariable Integer id) {
		service.delete(id);
		return ResponseEntity.noContent().build();
	}
	
	@PreAuthorize("hasAnyRole('ADMIN')")
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<List<ClienteDTO>> findAll() {
		List<Cliente> list = service.findAll();
		/* Padrão DTO -> cria uma outra classe para apenas trabalhar com os dados da classe original
		 * P. Ex. Cliente traz os produtos da categoria, mas não queremos, então criamos outra classe ClienteDTO
		 * apenas com os dados que queremos mostrar e lá na classe ClienteDTO o construtor recebe o Cliente */
		List<ClienteDTO> listDto = list.stream()
				                         .map(obj -> new ClienteDTO(obj)) 
				                         .collect(Collectors.toList());
		return ResponseEntity.ok()
				             .body(listDto);
	}		
	
	@RequestMapping(value = "/page",method = RequestMethod.GET)
	public ResponseEntity<Page<ClienteDTO>> findPage(
			                                           @RequestParam(value="page", defaultValue="0")          Integer page, 
			                                           @RequestParam(value="linesPerPage", defaultValue="24") Integer linesPerPage, 
			                                           @RequestParam(value="orderBy", defaultValue="nome")    String orderBy, 
			                                           @RequestParam(value="direction", defaultValue="ASC")   String direction) {
		Page<Cliente> list = service.findPage(page, linesPerPage, orderBy, direction);
		Page<ClienteDTO> listDto = list.map(obj -> new ClienteDTO(obj));
		return ResponseEntity.ok()
				             .body(listDto);
	}		
	
}
