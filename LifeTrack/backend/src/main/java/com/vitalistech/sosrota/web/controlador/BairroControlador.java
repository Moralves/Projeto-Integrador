package com.vitalistech.sosrota.web.controlador;

import com.vitalistech.sosrota.dominio.modelo.Bairro;
import com.vitalistech.sosrota.dominio.repositorio.BairroRepositorio;
import com.vitalistech.sosrota.dominio.servico.AnaliseEstrategicaServico;
import com.vitalistech.sosrota.web.dto.BairroSugeridoDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints para gerenciar bairros.
 */
@RestController
@RequestMapping("/api/bairros")
@CrossOrigin(origins = "*")
public class BairroControlador {

    private final BairroRepositorio bairroRepositorio;
    private final AnaliseEstrategicaServico analiseEstrategicaServico;

    public BairroControlador(BairroRepositorio bairroRepositorio,
                            AnaliseEstrategicaServico analiseEstrategicaServico) {
        this.bairroRepositorio = bairroRepositorio;
        this.analiseEstrategicaServico = analiseEstrategicaServico;
    }

    @GetMapping
    public List<Bairro> listar() {
        return bairroRepositorio.findAll();
    }

    @GetMapping("/sugeridos")
    public List<BairroSugeridoDTO> obterBairrosSugeridos() {
        return analiseEstrategicaServico.obterBairrosSugeridos();
    }
}

