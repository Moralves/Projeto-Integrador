package com.vitalistech.sosrota.web.controlador;

import com.vitalistech.sosrota.dominio.modelo.Ambulancia;
import com.vitalistech.sosrota.dominio.modelo.Bairro;
import com.vitalistech.sosrota.dominio.modelo.StatusAmbulancia;
import com.vitalistech.sosrota.dominio.repositorio.AmbulanciaRepositorio;
import com.vitalistech.sosrota.dominio.repositorio.AtendimentoRepositorio;
import com.vitalistech.sosrota.dominio.repositorio.BairroRepositorio;
import com.vitalistech.sosrota.web.dto.CriarAmbulanciaDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints de gestão de ambulâncias.
 */
@RestController
@RequestMapping("/api/ambulancias")
@CrossOrigin(origins = "*")
public class AmbulanciaControlador {

    private final AmbulanciaRepositorio ambulanciaRepositorio;
    private final BairroRepositorio bairroRepositorio;
    private final AtendimentoRepositorio atendimentoRepositorio;

    public AmbulanciaControlador(AmbulanciaRepositorio ambulanciaRepositorio,
                                 BairroRepositorio bairroRepositorio,
                                 AtendimentoRepositorio atendimentoRepositorio) {
        this.ambulanciaRepositorio = ambulanciaRepositorio;
        this.bairroRepositorio = bairroRepositorio;
        this.atendimentoRepositorio = atendimentoRepositorio;
    }

    @GetMapping
    public List<Ambulancia> listar() {
        return ambulanciaRepositorio.findAll();
    }

    @GetMapping("/{id}/em-atendimento")
    public ResponseEntity<Boolean> verificarEmAtendimento(@PathVariable Long id) {
        List<com.vitalistech.sosrota.dominio.modelo.Atendimento> atendimentosAtivos = 
            atendimentoRepositorio.findAtendimentosAtivosPorAmbulancia(id);
        return ResponseEntity.ok(!atendimentosAtivos.isEmpty());
    }

    @PostMapping
    public ResponseEntity<Ambulancia> cadastrar(@RequestBody @Valid CriarAmbulanciaDTO dto) {
        Bairro bairroBase = bairroRepositorio.findById(dto.getIdBairroBase())
                .orElseThrow(() -> new IllegalArgumentException("Bairro não encontrado"));

        Ambulancia ambulancia = new Ambulancia();
        ambulancia.setPlaca(dto.getPlaca());
        ambulancia.setTipo(dto.getTipo());
        ambulancia.setBairroBase(bairroBase);
        ambulancia.setStatus(StatusAmbulancia.DISPONIVEL);
        ambulancia.setAtiva(true);

        return ResponseEntity.ok(ambulanciaRepositorio.save(ambulancia));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ambulancia> atualizar(@PathVariable Long id,
                                                @RequestBody @Valid Ambulancia dados) {
        Ambulancia a = ambulanciaRepositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ambulância não encontrada"));
        
        // Verificar se a ambulância está em atendimento
        List<com.vitalistech.sosrota.dominio.modelo.Atendimento> atendimentosAtivos = 
            atendimentoRepositorio.findAtendimentosAtivosPorAmbulancia(id);
        
        if (!atendimentosAtivos.isEmpty()) {
            throw new IllegalStateException(
                "Não é possível editar uma ambulância que está em atendimento. Finalize o atendimento antes de editar.");
        }
        
        a.setPlaca(dados.getPlaca());
        a.setTipo(dados.getTipo());
        a.setBairroBase(dados.getBairroBase());
        return ResponseEntity.ok(ambulanciaRepositorio.save(a));
    }

    @PutMapping("/{id}/ativar")
    public ResponseEntity<Void> ativar(@PathVariable Long id) {
        Ambulancia a = ambulanciaRepositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ambulância não encontrada"));
        
        // Verificar se a ambulância está em atendimento
        List<com.vitalistech.sosrota.dominio.modelo.Atendimento> atendimentosAtivos = 
            atendimentoRepositorio.findAtendimentosAtivosPorAmbulancia(id);
        
        if (!atendimentosAtivos.isEmpty()) {
            throw new IllegalStateException(
                "Não é possível ativar uma ambulância que está em atendimento. Finalize o atendimento antes.");
        }
        
        a.setAtiva(true);
        a.setStatus(StatusAmbulancia.DISPONIVEL);
        ambulanciaRepositorio.save(a);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/desativar")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        Ambulancia a = ambulanciaRepositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ambulância não encontrada"));
        
        // Verificar se a ambulância está em atendimento
        List<com.vitalistech.sosrota.dominio.modelo.Atendimento> atendimentosAtivos = 
            atendimentoRepositorio.findAtendimentosAtivosPorAmbulancia(id);
        
        if (!atendimentosAtivos.isEmpty()) {
            throw new IllegalStateException(
                "Não é possível desativar uma ambulância que está em atendimento. Finalize o atendimento antes.");
        }
        
        // Verificar se status está EM_ATENDIMENTO
        if (a.getStatus() == StatusAmbulancia.EM_ATENDIMENTO) {
            throw new IllegalStateException(
                "Não é possível desativar uma ambulância com status EM_ATENDIMENTO. Finalize o atendimento antes.");
        }
        
        a.setAtiva(false);
        a.setStatus(StatusAmbulancia.INATIVA);
        ambulanciaRepositorio.save(a);
        return ResponseEntity.ok().build();
    }
}
