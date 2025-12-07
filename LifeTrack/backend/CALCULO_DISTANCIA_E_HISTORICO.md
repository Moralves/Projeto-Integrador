# 📍 Cálculo de Distância e Histórico em Tempo Real

## 🗺️ Cálculo de Distância

### Como Funciona

O cálculo de distância entre a **base da ambulância** e o **destino da ocorrência** é realizado usando o **Algoritmo de Dijkstra** sobre um grafo de conexões viárias.

### Processo Completo

1. **Grafo de Conexões**
   - O sistema utiliza a tabela `ruas_conexoes` que contém todas as conexões entre bairros
   - Cada conexão tem uma distância em quilômetros
   - O grafo é **não direcionado** (bidirecional)
   - Se existe origem 9 → destino 16, não precisa existir origem 16 → destino 9 na tabela
   - O algoritmo Dijkstra cria automaticamente conexões reversas com a mesma distância

2. **Algoritmo Dijkstra**
   - Localização: `com.vitalistech.sosrota.util.AlgoritmoDijkstra`
   - Calcula o **caminho mais curto** entre dois bairros
   - Retorna a distância total em quilômetros e o caminho completo

3. **Cálculo no Despacho**
   ```java
   // Em OcorrenciaServico.despacharOcorrencia()
   ResultadoRota rota = AlgoritmoDijkstra.calcularRota(
       a.getBairroBase(),        // Origem: base da ambulância
       ocorrencia.getBairroLocal(), // Destino: local da ocorrência
       todasConexoes             // Todas as conexões do banco
   );
   
   double distKm = rota.getDistanciaKm();
   ```

4. **Armazenamento**
   - A distância calculada é salva no campo `distanciaKm` da tabela `atendimentos`
   - O caminho completo é salvo na tabela `atendimento_rota_conexao` para rastreabilidade

### Exemplo Prático

```
Ambulância na base "Centro" → Ocorrência em "Jardim das Flores"

1. Sistema busca todas as conexões do banco
2. Constrói grafo: Centro ↔ Bairro X ↔ Bairro Y ↔ Jardim das Flores
3. Dijkstra calcula: Centro → Bairro X (2km) → Bairro Y (3km) → Jardim das Flores (1.5km)
4. Retorna: distância total = 6.5 km
5. Salva no atendimento: distanciaKm = 6.5
```

## ✅ Finalização Automática da OS

### Quando a Ambulância Chega ao Destino

Quando a ambulância chega ao local da ocorrência, a **OS é finalizada automaticamente**:

1. **Endpoint**: `POST /api/ocorrencias/atendimentos/{idAtendimento}/chegada`
2. **Método**: `OcorrenciaServico.registrarChegada()`
3. **Ações Automáticas**:
   - Registra `dataHoraChegada` no atendimento
   - Calcula tempo de deslocamento (despacho até chegada)
   - Define tempo de retorno (igual ao tempo de deslocamento)
   - **Finaliza a OS**: status muda para `CONCLUIDA`
   - Calcula SLA: tempo até chegada + tempo de retorno
   - Registra no histórico com todas as informações

### Fluxo Completo

```
1. Ocorrência ABERTA
   ↓
2. Ambulância DESPACHADA
   - Calcula distância (Dijkstra): base → destino
   - Salva distanciaKm no atendimento
   - Status: DESPACHADA
   ↓
3. Ambulância CHEGA ao local
   - Registra dataHoraChegada
   - Calcula tempo de deslocamento
   - OS FINALIZADA AUTOMATICAMENTE
   - Status: CONCLUIDA
   - Histórico atualizado
   ↓
4. Tempo de retorno contabilizado
   - Igual ao tempo de deslocamento
   - SLA = tempo até chegada + tempo de retorno
```

## 📊 Histórico em Tempo Real

### Informações Registradas

O histórico de ocorrência contém:

1. **Tipo de Ocorrência**: Tipo da ocorrência no momento da ação
2. **Identificação da Ambulância**: Placa da ambulância envolvida
3. **Ação da Ambulância**: 
   - "Indo até o local" (quando despachada)
   - "Retornando para base" (quando chega ao local)

### Exemplo de Registro no Histórico

**Despacho:**
```
Tipo: ACIDENTE - Ocorrência despachada. 
Ambulância: ABC-1234 (BASICA) - Distância: 5.2 km
Ação: Indo até o local
```

**Chegada:**
```
Tipo: ACIDENTE - Ambulância ABC-1234 chegou ao local. 
OS finalizada automaticamente.
Tempo de deslocamento: 8 minutos. 
Tempo de retorno estimado: 8 minutos.
Ação: Retornando para base
```

### Atualização em Tempo Real

1. **Backend**: Histórico é registrado automaticamente quando:
   - Ocorrência é aberta
   - Ambulância é despachada
   - Ambulância chega ao local (OS finalizada)
   - Ocorrência é concluída manualmente

2. **Frontend**: Componente `HistoricoOcorrencia`:
   - Atualiza automaticamente a cada 3 segundos
   - Mostra todas as ações com timestamp
   - Exibe tipo de ocorrência, ambulância e ação
   - Para de atualizar quando OS está concluída

3. **Visualização**:
   - Timeline visual com ícones por tipo de ação
   - Cores diferentes para cada ação
   - Badge "Ao vivo" quando está atualizando
   - Informações completas: tipo, ambulância, ação, descrição

## 🔧 Arquivos Relacionados

### Backend
- `AlgoritmoDijkstra.java` - Algoritmo de cálculo de rota
- `OcorrenciaServico.java` - Lógica de despacho e chegada
- `HistoricoOcorrenciaServico.java` - Registro de histórico
- `HistoricoOcorrencia.java` - Modelo com campos de ambulância
- `HistoricoOcorrenciaDTO.java` - DTO com informações da ambulância

### Frontend
- `historicoService.js` - Serviço para buscar histórico
- `HistoricoOcorrencia.jsx` - Componente de histórico em tempo real
- `ListarOcorrencias.jsx` - Integração do histórico na lista

## 🔄 Retorno da Ambulância

### Quando a Ambulância Retorna à Base

Após a OS ser finalizada (quando a ambulância chega ao local), é possível registrar o retorno da ambulância à base:

1. **Endpoint**: `POST /api/ocorrencias/atendimentos/{idAtendimento}/retorno`
2. **Método**: `OcorrenciaServico.registrarRetorno()`
3. **Ações Automáticas**:
   - Registra `dataHoraRetorno` no atendimento
   - Calcula tempo de retorno (desde chegada até retorno)
   - **Marca ambulância como DISPONIVEL** novamente
   - Registra no histórico com informações de retorno

### Visualização do Retorno

- **Barra de Progresso**: O retorno aparece na barra de progresso quando a OS está finalizada
- **Importante**: O tempo de retorno **NÃO conta para o SLA**
- O SLA considera apenas: tempo até despacho + tempo de deslocamento (ida)
- O retorno é apenas informativo e permite rastrear quando a ambulância volta à base

### Disponibilidade da Ambulância

- Quando a ambulância retorna à base, ela fica **DISPONIVEL** novamente
- Pode ser utilizada em novas ocorrências imediatamente após o retorno

## 📝 Resumo

✅ **Cálculo de Distância**: Usa Dijkstra sobre grafo de conexões viárias (`ruas_conexoes`)  
✅ **Grafo Bidirecional**: Conexões reversas criadas automaticamente pelo algoritmo  
✅ **Finalização Automática**: OS finaliza quando ambulância chega ao local  
✅ **Retorno Visual**: Barra de progresso mostra retorno quando OS está finalizada  
✅ **Disponibilidade**: Ambulância fica disponível novamente ao retornar  
✅ **SLA Correto**: Apenas tempo até chegada (sem retorno)  
✅ **Histórico em Tempo Real**: Atualiza automaticamente a cada 3 segundos  
✅ **Informações Completas**: Tipo, ambulância e ação registrados no histórico  

## 🔄 Retorno da Ambulância

### Quando a Ambulância Retorna à Base

Após a OS ser finalizada (quando a ambulância chega ao local), é possível registrar o retorno da ambulância à base:

1. **Endpoint**: `POST /api/ocorrencias/atendimentos/{idAtendimento}/retorno`
2. **Método**: `OcorrenciaServico.registrarRetorno()`
3. **Ações Automáticas**:
   - Registra `dataHoraRetorno` no atendimento
   - Calcula tempo de retorno (desde chegada até retorno)
   - **Marca ambulância como DISPONIVEL** novamente
   - Registra no histórico com informações de retorno

### Visualização do Retorno

- **Barra de Progresso**: O retorno aparece na barra de progresso quando a OS está finalizada
- **Importante**: O tempo de retorno **NÃO conta para o SLA**
- O SLA considera apenas: tempo até despacho + tempo de deslocamento (ida)
- O retorno é apenas informativo e permite rastrear quando a ambulância volta à base

### Disponibilidade da Ambulância

- Quando a ambulância retorna à base, ela fica **DISPONIVEL** novamente
- Pode ser utilizada em novas ocorrências imediatamente após o retorno

---

**Sistema completo e funcional!** 🚀

