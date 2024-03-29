package br.net.jassa.cursomc.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import br.net.jassa.cursomc.domain.Cidade;
import br.net.jassa.cursomc.domain.Cliente;
import br.net.jassa.cursomc.domain.Endereco;
import br.net.jassa.cursomc.domain.enums.TipoCliente;
import br.net.jassa.cursomc.dto.ClienteDTO;
import br.net.jassa.cursomc.dto.ClienteNewDTO;
import br.net.jassa.cursomc.repositories.CidadeRepository;
import br.net.jassa.cursomc.repositories.ClienteRepository;
import br.net.jassa.cursomc.repositories.EnderecoRepository;
import br.net.jassa.cursomc.services.exceptions.DataIntegrityException;
import br.net.jassa.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class ClienteService {
	
	@Autowired
	private ClienteRepository repo;
	@Autowired
	private CidadeRepository cidadeRepository;
	@Autowired
	private EnderecoRepository enderecoRepository;
	@Autowired
	private BCryptPasswordEncoder pe;

	public Cliente find(Integer id) {
		Optional<Cliente> obj = repo.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException(
				"Objeto não encontrado! Id: " + id + ", Tipo: " + Cliente.class.getName()));
	}	
	
	public Cliente insert(Cliente obj) {
		obj.setId(null);
		obj = repo.save(obj);
		enderecoRepository.saveAll(obj.getEnderecos());
		return obj;
	}	
	
	public Cliente update (Cliente obj) {
		Cliente newObj = find(obj.getId());
		// necessário rotina abaixo pois estava persistindo o novo objeto substituindo todo o conteúdo no BD
		updateData(newObj, obj);
		return repo.save(newObj);
	}
	
	public void delete (Integer id) {
		find(id);
		try {
			repo.deleteById(id);
		} catch (DataIntegrityViolationException e) {
		    throw new DataIntegrityException("Não é possível excluir proque há pedidos relacionadas");	
		}
	}
	
	public List<Cliente> findAll() {
		return repo.findAll();
	}		
	
	public Page<Cliente> findPage(Integer page, Integer linesPerPage, String orderBy, String direction) {
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		return repo.findAll(pageRequest);
	}
	
	public Cliente fromDTO(ClienteDTO objDto) {
		return new Cliente(objDto.getId(), objDto.getNome(), objDto.getEmail(), null, null, null);
	}
	
	public Cliente fromDTO(ClienteNewDTO objDto) {
		Cliente cli = new Cliente(null, objDto.getNome(), objDto.getEmail(), objDto.getCpfOuCnpj(), TipoCliente.toEnum(objDto.getTipo()), pe.encode(objDto.getSenha()));
		Optional<Cidade> aux = cidadeRepository.findById(objDto.getCidadeId());
		Cidade cid = null;
		if (aux.isPresent()) {
			cid = aux.get();
		}
		Endereco end = new Endereco(null, objDto.getLogradouro(), objDto.getNumero(), objDto.getComplemento(), objDto.getBairro(), objDto.getCep(), cli, cid);
		cli.getEnderecos().add(end);
		cli.getTelefones().add(objDto.getTelefone1());
		if (objDto.getTelefone2()!=null) cli.getTelefones().add(objDto.getTelefone2());
		if (objDto.getTelefone3()!=null) cli.getTelefones().add(objDto.getTelefone3());
		return cli;
	}	
	
	/* rotina que atualiza o dto para o objeto gerenciado do BD */
	private void updateData(Cliente newObj, Cliente obj) {
		newObj.setNome(obj.getNome());
		newObj.setEmail(obj.getEmail());
	}

}
