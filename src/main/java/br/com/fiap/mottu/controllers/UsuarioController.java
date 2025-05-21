package br.com.fiap.mottu.controllers;

import br.com.fiap.mottu.dto.IntroDTO;
import br.com.fiap.mottu.dto.UsuarioDTO;
import br.com.fiap.mottu.models.Usuario;
import br.com.fiap.mottu.repositories.UsuarioRepository;
import br.com.fiap.mottu.service.UsuarioCachingService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/usuarios")
@EnableSpringDataWebSupport
public class UsuarioController {

    @Autowired
    UsuarioRepository repository;

    @Autowired
    UsuarioCachingService cachingService;

    @GetMapping
    public ResponseEntity<EntityModel<IntroDTO>> intro() {

        IntroDTO dto = new IntroDTO("Setor de usuários da Mottu");

        EntityModel<IntroDTO> resource = EntityModel.of(dto);

        resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UsuarioController.class).pegueTodos())
                .withRel("listar-usuarios"));

        resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UsuarioController.class).pegarPeloCpf(null))
                .withRel("listar-usuarios-pelo-cpf"));

        resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UsuarioController.class).cadastro(null))
                .withRel("cadastrar-usuarios"));

        resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UsuarioController.class).atualizar(null, null))
                .withRel("atualizar-usuarios"));

        resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UsuarioController.class).deletar(null))
                .withRel("deletar-usuarios"));

        return ResponseEntity.ok(resource);
    }

    @GetMapping("/todos")
    public ResponseEntity pegueTodos(){
        List<Usuario> listaDeUsuarios = cachingService.cacheFindAll();
        return ResponseEntity.ok(listaDeUsuarios);
    }

    public ResponseEntity pegarPeloCpf(@PathVariable(value = "cpf")  String cpf) {
        Optional<Usuario> usuario = cachingService.findById(cpf);

        if(usuario.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encotrado.");
        } else {
            return ResponseEntity.status(HttpStatus.FOUND).body(usuario);
        }
    }

    @PostMapping("/cadastro")
    public ResponseEntity cadastro(@RequestBody @Valid UsuarioDTO dto) {
        var usuario = new Usuario();
        BeanUtils.copyProperties(dto, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(usuario));
    }

    @DeleteMapping("/{cpf}")
    public ResponseEntity deletar(@PathVariable(value = "cpf") String cpf) {
        Optional<Usuario> usuario = repository.findById(cpf);
        if (usuario.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado para deletar.");
        }
        repository.delete(usuario.get());
        return ResponseEntity.status(HttpStatus.OK).body("Usuário deletado com sucesso.");
    }

    @PutMapping("/{cpf}")
    public ResponseEntity atualizar(@PathVariable(value = "cpf") String cpf, @RequestBody UsuarioDTO dto) {
        Optional<Usuario> usuario = repository.findById(cpf);
        if (usuario.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado para atualizar.");
        }
        var usuarioAtualizado = usuario.get();
        BeanUtils.copyProperties(dto, usuarioAtualizado);
        return ResponseEntity.status(HttpStatus.OK).body(repository.save(usuarioAtualizado));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class) // Metodo de prevenção de erro do @Valid
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(errors);
    }

}
