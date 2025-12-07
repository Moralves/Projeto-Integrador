# ⏱️ Sistema de Timer Profissional para Ocorrências

## 📋 Visão Geral

Sistema completo de rastreamento de tempo em tempo real para ocorrências, permitindo monitoramento profissional do andamento desde a abertura até a conclusão.

## ✨ Funcionalidades Implementadas

### 1. **Timer em Tempo Real**
- Calcula tempos decorridos em todas as etapas
- Monitora tempo restante do SLA
- Identifica quando SLA está em risco ou excedido
- Formata tempos de forma legível

### 2. **Registro de Chegada Separado**
- Registra chegada da ambulância sem fechar a ocorrência
- Muda status para `EM_ATENDIMENTO`
- Permite continuar o atendimento antes de concluir

### 3. **Histórico Profissional**
- Registra todas as etapas com timestamp
- Inclui ação `CHEGADA` no histórico
- Mantém rastreabilidade completa

## 🔌 Endpoints Disponíveis

### 1. Despachar Ocorrência (com Timer)
```
POST /api/ocorrencias/{id}/despachar
Headers: X-User-Id: {userId}
```
Despacha uma ambulância para a ocorrência e retorna tanto o atendimento criado quanto as informações do timer em tempo real.

**Resposta:**
```json
{
  "atendimento": {
    "id": 1,
    "dataHoraDespacho": "2025-01-07T14:02:00",
    "distanciaKm": 8.5,
    "ambulancia": { ... },
    "equipe": { ... }
  },
  "timer": {
    "idOcorrencia": 1,
    "status": "DESPACHADA",
    "tempoTotalDecorridoMinutos": 2,
    "slaMinutos": 15,
    "tempoRestanteMinutos": 13,
    "slaEmRisco": false,
    "tempoTotalFormatado": "2m 0s",
    "tempoRestanteFormatado": "13m 0s"
  }
}
```

### 2. Obter Informações do Timer
```
GET /api/ocorrencias/{id}/timer
```
Retorna todas as informações do timer em tempo real. Use este endpoint para atualizações periódicas do timer:
- Tempos decorridos em cada etapa
- Status atual da ocorrência
- Informações de SLA (tempo restante, se está em risco, se foi excedido)
- Tempos formatados para exibição

**Resposta exemplo:**
```json
{
  "idOcorrencia": 1,
  "status": "EM_ATENDIMENTO",
  "dataHoraAbertura": "2025-01-07T14:00:00",
  "dataHoraDespacho": "2025-01-07T14:02:00",
  "dataHoraChegada": "2025-01-07T14:15:00",
  "tempoTotalDecorridoMinutos": 25,
  "tempoAteChegadaMinutos": 13,
  "tempoAposChegadaMinutos": 12,
  "slaMinutos": 15,
  "tempoRestanteMinutos": -10,
  "slaExcedido": true,
  "slaEmRisco": false,
  "tempoTotalFormatado": "25m 0s",
  "tempoRestanteFormatado": "-10m 0s"
}
```

### 2. Registrar Chegada (Sem Fechar)
```
POST /api/ocorrencias/atendimentos/{idAtendimento}/chegada
Headers: X-User-Id: {userId}
```
Registra a chegada da ambulância e muda status para `EM_ATENDIMENTO`. A ocorrência permanece em andamento.

### 3. Registrar Chegada e Fechar (Compatibilidade)
```
POST /api/ocorrencias/atendimentos/{idAtendimento}/chegada-e-fechar
Headers: X-User-Id: {userId}
```
Mantido para compatibilidade. Registra chegada e fecha automaticamente.

### 4. Concluir Ocorrência
```
POST /api/ocorrencias/{id}/concluir
Headers: X-User-Id: {userId}
```
Finaliza a ocorrência após o atendimento estar completo.

## 📊 Fluxo Completo

1. **ABERTA** → Ocorrência criada
2. **DESPACHADA** → Ambulância despachada
3. **EM_ATENDIMENTO** → Ambulância chegou ao local (novo!)
4. **CONCLUIDA** → Atendimento finalizado

## 🔧 DTOs

### TimerOcorrenciaDTO
Contém todas as informações do timer:
- Datas e horas de cada etapa
- Tempos decorridos em minutos
- Status de SLA (risco, excedido, tempo restante)
- Informações formatadas para exibição
- Status das etapas (foi despachada, chegou local, foi concluída)

## 📝 Histórico de Ações

Novas ações registradas:
- `ABERTURA` - Ocorrência aberta
- `DESPACHO` - Ambulância despachada
- **`CHEGADA`** - Ambulância chegou ao local (NOVO!)
- `ALTERACAO_STATUS` - Status alterado
- `CANCELAMENTO` - Ocorrência cancelada
- `CONCLUSAO` - Ocorrência concluída

## 🎯 Uso Recomendado

### No Frontend:

1. **Ao Despachar Ocorrência:**
   - O endpoint `/api/ocorrencias/{id}/despachar` já retorna o timer automaticamente
   - Use `response.timer` para exibir as informações do timer imediatamente após o despacho
   - Não é necessário fazer uma chamada adicional para obter o timer inicial

2. **Exibir Timer em Tempo Real (após despacho):**
   - Fazer polling a cada 1-5 segundos no endpoint `/api/ocorrencias/{id}/timer`
   - Exibir tempo decorrido, tempo restante do SLA
   - Alertar visualmente quando SLA está em risco

3. **Registrar Chegada:**
   - Quando a ambulância chegar, chamar `/api/ocorrencias/atendimentos/{idAtendimento}/chegada`
   - Status muda para `EM_ATENDIMENTO`
   - Timer continua contando o tempo após a chegada

3. **Finalizar:**
   - Após atendimento completo, chamar `/api/ocorrencias/{id}/concluir`
   - Ocorrência é fechada com todos os cálculos de SLA

## 📦 Arquivos Modificados

1. `AcaoHistorico.java` - Adicionado `CHEGADA`
2. `TimerOcorrenciaDTO.java` - Novo DTO criado
3. `OcorrenciaServico.java` - Novos métodos:
   - `obterInformacoesTimer()`
   - `registrarChegada()`
   - `formatarTempo()`
4. `OcorrenciaControlador.java` - Novos endpoints
5. `AtendimentoRepositorio.java` - Método `findByOcorrenciaId()`
6. `setup.sql` - Atualizado constraint do histórico

## ⚡ Melhorias Futuras (Sugestões)

- WebSocket para atualizações em tempo real (sem polling)
- Notificações quando SLA está em risco
- Gráficos de tempo por etapa
- Relatórios de performance de tempo

---

**Sistema pronto para uso profissional!** 🚀

