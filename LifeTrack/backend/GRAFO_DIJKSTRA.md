# 🗺️ Grafo e Algoritmo de Dijkstra - Documentação Técnica

## 📋 Estrutura do Grafo

O sistema modela a cidade como um **grafo não direcionado ponderado**:

- **Vértices (Nós)**: Bairros da cidade
- **Arestas**: Ruas/conexões viárias entre bairros
- **Pesos**: Distância em quilômetros (km)

## 🗄️ Estrutura no Banco de Dados

### Tabela: `ruas_conexoes`

```sql
CREATE TABLE ruas_conexoes (
    id BIGSERIAL PRIMARY KEY,
    id_bairro_origem BIGINT NOT NULL,
    id_bairro_destino BIGINT NOT NULL,
    distancia_km DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (id_bairro_origem) REFERENCES bairros(id),
    FOREIGN KEY (id_bairro_destino) REFERENCES bairros(id),
    UNIQUE(id_bairro_origem, id_bairro_destino)
);
```

**Campos:**
- `id_bairro_origem`: Bairro de origem da conexão
- `id_bairro_destino`: Bairro de destino da conexão
- `distancia_km`: Distância em quilômetros entre os bairros

**Importante:**
- O grafo é **não direcionado** (bidirecional)
- Cada conexão representa uma rua que liga dois bairros
- A distância é a mesma em ambos os sentidos

## 🔄 Como o Sistema Usa o Grafo

### 1. Carregamento das Conexões

```java
// Em OcorrenciaServico e AnaliseEstrategicaServico
List<RuaConexao> todasConexoes = ruaConexaoRepositorio.findAll();
```

Todas as conexões são carregadas do banco de dados uma vez e reutilizadas.

### 2. Construção do Grafo no Dijkstra

O algoritmo `AlgoritmoDijkstra.calcularRota()` recebe:
- `bairroOrigem`: Bairro onde a ambulância está (vértice inicial)
- `bairroDestino`: Bairro onde é a ocorrência (vértice destino)
- `todasConexoes`: Lista de todas as arestas do grafo

**Processo:**
1. Constrói lista de adjacência a partir das conexões
2. Cria conexões reversas automaticamente (grafo bidirecional)
3. Executa algoritmo de Dijkstra
4. Retorna menor caminho e distância total

### 3. Cálculo de Rotas

**Exemplo:**
```
Ambulância na base "Centro" → Ocorrência em "Jardim das Flores"

1. Sistema busca todas as conexões do banco
2. Constrói grafo com Dijkstra
3. Calcula: Centro → [Bairro X] → [Bairro Y] → Jardim das Flores
4. Retorna: distância total (ex: 8.5 km) e caminho completo
```

## 📊 Uso do Dijkstra no Sistema

### 1. **Sugestão de Ambulâncias (RF05)**
```java
// OcorrenciaServico.sugerirAmbulancias()
ResultadoRota rota = AlgoritmoDijkstra.calcularRota(
    ambulancia.getBairroBase(),    // Origem: base da ambulância
    ocorrencia.getBairroLocal(),    // Destino: local da ocorrência
    todasConexoes                   // Arestas do grafo
);
```

### 2. **Despacho de Ambulância (RF06)**
```java
// OcorrenciaServico.despacharOcorrencia()
ResultadoRota rota = AlgoritmoDijkstra.calcularRota(
    a.getBairroBase(),
    ocorrencia.getBairroLocal(),
    todasConexoes
);
```

### 3. **Análise Estratégica**
```java
// AnaliseEstrategicaServico.calcularTempoMedioResposta()
ResultadoRota rota = AlgoritmoDijkstra.calcularRota(
    base,              // Testa cada bairro como possível base
    bairroDestino,     // Para cada bairro com ocorrências
    todasConexoes
);
```

## 🔧 Implementação do Algoritmo

### Estrutura de Dados Utilizada

O algoritmo usa estruturas da biblioteca `java.util`:

- **`List<RuaConexao>`**: Lista de todas as conexões (arestas)
- **`Map<Long, List<RuaConexao>>`**: Lista de adjacência
- **`PriorityQueue<long[]>`**: Fila de prioridade para Dijkstra
- **`Map<Long, Double>`**: Distâncias mínimas calculadas
- **`Set<Long>`**: Vértices visitados

### Fluxo do Algoritmo

```
1. Construir grafo (lista de adjacência)
   └─> Para cada conexão (A → B, distância X):
       ├─> Adicionar A → B com peso X
       └─> Adicionar B → A com peso X (grafo não direcionado)

2. Inicializar Dijkstra
   └─> Distância origem = 0
   └─> Distância todos outros = ∞
   └─> Fila de prioridade: [origem, 0]

3. Processar vértices
   └─> Enquanto fila não vazia:
       ├─> Remover vértice com menor distância
       ├─> Para cada vizinho:
       │   ├─> Calcular nova distância
       │   └─> Se menor, atualizar e adicionar à fila
       └─> Se chegou ao destino, parar

4. Reconstruir caminho
   └─> Usar mapa de predecessores
   └─> Retornar lista de bairros do caminho
```

## 📝 Exemplo Prático

### Dados no Banco:

**Bairros:**
- ID 1: Centro
- ID 2: Jardim das Flores
- ID 3: Vila Nova

**Conexões (ruas_conexoes):**
```
ID | Origem | Destino | Distância
1  |   1    |    2    |   5.0 km
2  |   1    |    3    |   3.0 km
3  |   2    |    3    |   4.0 km
```

### Cálculo de Rota:

**Origem:** Centro (ID 1)  
**Destino:** Jardim das Flores (ID 2)

**Grafo construído:**
```
Centro (1) ──5.0km──> Jardim (2)
   │                      │
   │ 3.0km                │ 4.0km
   │                      │
   └─────────> Vila (3) <──┘
```

**Dijkstra encontra:**
- Caminho direto: Centro → Jardim (5.0 km)
- Caminho alternativo: Centro → Vila → Jardim (3.0 + 4.0 = 7.0 km)
- **Resultado:** Caminho direto com 5.0 km

## ✅ Validações e Garantias

1. **Grafo Conectado**: O sistema assume que todos os bairros são alcançáveis
2. **Conexões Bidirecionais**: Criadas automaticamente no algoritmo
3. **Distâncias Positivas**: Validadas no banco (NOT NULL, DECIMAL)
4. **Sem Ciclos Negativos**: Não aplicável (distâncias sempre positivas)

## 🚨 Tratamento de Erros

- **Sem caminho**: Retorna `distanciaKm = POSITIVE_INFINITY`
- **Bairro não encontrado**: Validação antes de calcular
- **Conexões vazias**: Retorna caminho vazio

## 📚 Referências

- **Algoritmo**: Dijkstra (1959) - Caminho mínimo em grafos
- **Estrutura de Dados**: `java.util.List`, `java.util.Map`, `java.util.PriorityQueue`
- **Complexidade**: O((V + E) log V) onde V = vértices, E = arestas

---

**Última atualização:** Dezembro 2024



