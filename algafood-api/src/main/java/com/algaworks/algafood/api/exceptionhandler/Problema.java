package com.algaworks.algafood.api.exceptionhandler;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Problema {

	/**
	 * Builder é um padrão de projeto para construir objeto de forma mais fluente.
	 */
	private LocalDateTime dataHora;
	private String mensagem;
	
}
