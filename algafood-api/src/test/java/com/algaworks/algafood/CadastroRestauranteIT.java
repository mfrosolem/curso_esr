package com.algaworks.algafood;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.algaworks.algafood.domain.model.Cozinha;
import com.algaworks.algafood.domain.model.Restaurante;
import com.algaworks.algafood.domain.repository.CozinhaRepository;
import com.algaworks.algafood.domain.repository.RestauranteRepository;
import com.algaworks.algafood.util.DatabaseCleaner;
import com.algaworks.algafood.util.ResourceUtils;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("/application-test.properties")
public class CadastroRestauranteIT {
	
	private static final String VIOLACAO_DE_REGRA_DE_NEGOCIO_PROBLEM_TYPE = "Violação de regra de negócio";

    private static final String DADOS_INVALIDOS_PROBLEM_TITLE = "Dados inválidos";
    
	private static final int RESTAURANTE_ID_INEXISTENTE = 100;

	@LocalServerPort
	private int port;
	
	@Autowired
	private DatabaseCleaner databaseCleaner;
	
	@Autowired
	private RestauranteRepository restauranteRepository;
	
	@Autowired
	private CozinhaRepository cozinhaRepository;
	
	private int quantidadeRestaurantesCadastrados;
	private Restaurante restauranteLellis;
	private String jsonCorretoRestauranteNewYorkBarbecue;
	private String jsonIncorretoRestauranteNewYorkBarbecueCozinhaInexistente;
	private String jsonIncorretoRestauranteNewYorkBarbecueSemCozinha;
	private String jsonIncorretoRestauranteNewYorkBarbecueSemFrete;
	
	@Before
	public void setUp() {
		RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
		RestAssured.port = port;
		RestAssured.basePath = "/restaurantes";
		
		jsonCorretoRestauranteNewYorkBarbecue = 
				ResourceUtils.getContentFromRessource("/json/correto/restaurante-new-york-barbecue.json");
		
		jsonIncorretoRestauranteNewYorkBarbecueCozinhaInexistente = 
				ResourceUtils.getContentFromRessource("/json/incorreto/restaurante-new-york-barbecue-com-cozinha-inexistente.json");
		
		jsonIncorretoRestauranteNewYorkBarbecueSemCozinha = 
				ResourceUtils.getContentFromRessource("/json/incorreto/restaurante-new-york-barbecue-sem-cozinha.json");
		
		jsonIncorretoRestauranteNewYorkBarbecueSemFrete = 
				ResourceUtils.getContentFromRessource("/json/incorreto/restaurante-new-york-barbecue-sem-frete.json");
		
		databaseCleaner.clearTables();
		prepararDados();
		
	}
	
	@Test
	public void deveRetornar200_QuandoConsultarRestaurantes() {
		given()
			.accept(ContentType.JSON)
		.when()
			.get()
		.then()
			.statusCode(HttpStatus.OK.value());
	}
	
	
	@Test
	public void deveConterRestaurantes_QuandoConsultarRestaurantes() {
		given()
			.accept(ContentType.JSON)
		.when()
			.get()
		.then()
			.body("", hasSize(quantidadeRestaurantesCadastrados));
	}
	
	@Test
	public void deveRetornarRespostaEStatusCorreto_QuandoConsultarRestauranteExistente() {
		given()
			.pathParam("restauranteId", restauranteLellis.getId())
			.accept(ContentType.JSON)
		.when()
			.get("/{restauranteId}")
		.then()
			.statusCode(HttpStatus.OK.value())
			.body("nome", equalTo(restauranteLellis.getNome()));
	}
	
	@Test
	public void deveRetornarStatus404_QuandoConsultarRestauranteInexistente() {
		given()
			.pathParam("restauranteId", RESTAURANTE_ID_INEXISTENTE)
			.accept(ContentType.JSON)
		.when()
			.get("/{restauranteId}")
		.then()
			.statusCode(HttpStatus.NOT_FOUND.value());
	}
	
	@Test
	public void deveRetornarStatus201_QuandoCadastrarRestaurante() {
		given()
			.body(jsonCorretoRestauranteNewYorkBarbecue)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post()
		.then()
			.statusCode(HttpStatus.CREATED.value());
	}
	
	@Test
	public void deveRetornar400_QuandoCadastrarRestauranteComCozinhaInexistente() {
		given()
			.body(jsonIncorretoRestauranteNewYorkBarbecueCozinhaInexistente)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post()
		.then()
			.statusCode(HttpStatus.BAD_REQUEST.value())
			.body("title", equalTo(VIOLACAO_DE_REGRA_DE_NEGOCIO_PROBLEM_TYPE));
	}
	
	@Test
	public void deveRetornar400_QuandoCadastrarRestauranteSemCozinha() {
		given()
			.body(jsonIncorretoRestauranteNewYorkBarbecueSemCozinha)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post()
		.then()
			.statusCode(HttpStatus.BAD_REQUEST.value())
			.body("title", equalTo(DADOS_INVALIDOS_PROBLEM_TITLE));
	}
	
	@Test
	public void deveRetornar400_QuandoCadastrarRestauranteSemFrete() {
		given()
			.body(jsonIncorretoRestauranteNewYorkBarbecueSemFrete)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post()
		.then()
			.statusCode(HttpStatus.BAD_REQUEST.value())
			.body("title", equalTo(DADOS_INVALIDOS_PROBLEM_TITLE));
	}
	
	
	private void prepararDados() {
		
		Cozinha cozinha1 = new Cozinha();
		cozinha1.setNome("Tailandesa");
		cozinhaRepository.save(cozinha1);
		
		Cozinha cozinha2 = new Cozinha();
		cozinha2.setNome("Americana");
		cozinhaRepository.save(cozinha2);
		
		Restaurante restaurante1 = new Restaurante();
		restaurante1.setCozinha(cozinha1);
		restaurante1.setNome("Lellis");
		restaurante1.setTaxaFrete(new BigDecimal("10.0"));
		restauranteRepository.save(restaurante1);
		restauranteLellis = restaurante1;
		
		Restaurante restaurante2 = new Restaurante();
		restaurante2.setCozinha(cozinha1);
		restaurante2.setNome("Bela Cintra");
		restaurante2.setTaxaFrete(new BigDecimal("15.0"));
		restaurante2 = restauranteRepository.save(restaurante2);
		
		quantidadeRestaurantesCadastrados = (int) restauranteRepository.count();
	}

}
