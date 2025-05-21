package br.com.fiap.mottu.dto;

import br.com.fiap.mottu.models.Endereco;
import br.com.fiap.mottu.models.Moto;

import java.util.Date;

public record UsuarioDTO(
        String cpf,
        Endereco cep,
        Moto placa,
        Date dataNascimento,
        String nome
) {}
