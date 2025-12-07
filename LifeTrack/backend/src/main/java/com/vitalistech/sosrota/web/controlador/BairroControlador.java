package com.vitalistech.sosrota.web.controlador;

import com.vitalistech.sosrota.dominio.modelo.Bairro;
import com.vitalistech.sosrota.dominio.modelo.TipoAmbulancia;
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

    /**
     * Retorna bairros sugeridos para posicionamento de ambulâncias.
     * 
     * @param tipoAmbulancia Tipo de ambulância sendo cadastrada (opcional).
     *                       Se fornecido, a análise considera apenas ocorrências relevantes
     *                       e evita aglomeração do mesmo tipo.
     * @return Lista de bairros sugeridos ordenados por prioridade
     */
    @GetMapping("/sugeridos")
    public List<BairroSugeridoDTO> obterBairrosSugeridos(
            @RequestParam(required = false) String tipoAmbulancia) {
        TipoAmbulancia tipo = null;
        if (tipoAmbulancia != null && !tipoAmbulancia.isEmpty()) {
            try {
                tipo = TipoAmbulancia.valueOf(tipoAmbulancia.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Tipo inválido, usar null (análise geral)
            }
        }
        return analiseEstrategicaServico.obterBairrosSugeridos(tipo);
    }
}

