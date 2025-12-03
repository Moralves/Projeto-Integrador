package com.vitalistech.sosrota.config;

import com.vitalistech.sosrota.dominio.modelo.Bairro;
import com.vitalistech.sosrota.dominio.modelo.RuaConexao;
import com.vitalistech.sosrota.dominio.repositorio.BairroRepositorio;
import com.vitalistech.sosrota.dominio.repositorio.RuaConexaoRepositorio;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Carrega os dados iniciais de bairros e conexões viárias a partir de arquivos CSV.
 * Os arquivos esperados ficam em src/main/resources/data.
 */
@Configuration
public class CarregamentoDadosIniciaisModel {

    @Bean
    public CommandLineRunner carregarDadosIniciais(BairroRepositorio bairroRepositorio,
                                                   RuaConexaoRepositorio ruaConexaoRepositorio) {
        return args -> {
            try {
                if (bairroRepositorio.count() == 0) {
                    Path caminhoBairros = Path.of("src/main/resources/data/bairros.csv");
                    if (Files.exists(caminhoBairros)) {
                        List<String> linhas = Files.readAllLines(caminhoBairros);
                        for (String linha : linhas) {
                            String nome = linha.trim();
                            if (!nome.isBlank()) {
                                Bairro bairro = new Bairro();
                                bairro.setNome(nome);
                                bairroRepositorio.save(bairro);
                            }
                        }
                    }
                }

                if (ruaConexaoRepositorio.count() == 0) {
                    Path caminhoRuas = Path.of("src/main/resources/data/ruas_conexoes.csv");
                    if (Files.exists(caminhoRuas)) {
                        List<String> linhas = Files.readAllLines(caminhoRuas);
                        for (String linha : linhas) {
                            if (linha.isBlank()) continue;
                            String[] partes = linha.split(";");
                            if (partes.length < 3) continue;

                            String nomeOrigem = partes[0].trim();
                            String nomeDestino = partes[1].trim();
                            double distancia = Double.parseDouble(partes[2].trim());

                            Bairro origem = bairroRepositorio.findByNome(nomeOrigem);
                            Bairro destino = bairroRepositorio.findByNome(nomeDestino);

                            if (origem != null && destino != null) {
                                RuaConexao conexao = new RuaConexao();
                                conexao.setBairroOrigem(origem);
                                conexao.setBairroDestino(destino);
                                conexao.setDistanciaKm(distancia);
                                ruaConexaoRepositorio.save(conexao);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }
}
