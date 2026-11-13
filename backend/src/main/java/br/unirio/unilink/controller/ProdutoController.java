package br.unirio.unilink.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.unirio.unilink.model.NovoProduto;
import br.unirio.unilink.model.Produto;
import br.unirio.unilink.model.Reserva;
import br.unirio.unilink.model.Solicitante;
import br.unirio.unilink.service.ProdutoService;

/**
 * Joga pra Rolo: ofertas, anuncios, reserva e compra (UC05 e UC11).
 */
@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService service;

    public ProdutoController(ProdutoService service) {
        this.service = service;
    }

    // GET /api/produtos -> todas as ofertas, das mais novas para as mais antigas
    @GetMapping
    public List<Produto> listar() {
        return service.listar();
    }

    // GET /api/produtos/pedidos?email=... -> produtos que o estudante reservou ou comprou (FA01 do UC05)
    @GetMapping("/pedidos")
    public List<Produto> pedidos(@RequestParam String email) {
        return service.pedidos(email);
    }

    // POST /api/produtos -> anuncia um item (UC11)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void anunciar(@RequestBody NovoProduto novo) {
        service.anunciar(novo);
    }

    // POST /api/produtos/{id}/reserva -> reserva o item e gera o Pix (UC05)
    @PostMapping("/{id}/reserva")
    public Reserva reservar(@PathVariable int id, @RequestBody Solicitante comprador) {
        return service.reservar(id, comprador.email());
    }

    // DELETE /api/produtos/{id}/reserva?email=... -> desiste da compra (FE01 do UC05)
    @DeleteMapping("/{id}/reserva")
    public Produto cancelarReserva(@PathVariable int id, @RequestParam String email) {
        return service.cancelarReserva(id, email);
    }

    // POST /api/produtos/{id}/compra -> conclui o Pix simulado
    @PostMapping("/{id}/compra")
    public Produto concluirCompra(@PathVariable int id, @RequestBody Solicitante comprador) {
        return service.concluirCompra(id, comprador.email());
    }
}
