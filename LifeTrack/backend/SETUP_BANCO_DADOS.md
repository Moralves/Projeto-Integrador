# 📋 Guia Completo de Setup do Banco de Dados

Este documento lista **todas as tabelas necessárias** e a **quantidade mínima de dados** em cada uma para o sistema funcionar corretamente.

## 🗄️ Tabelas Obrigatórias

### 1. **bairros** (OBRIGATÓRIA - Mínimo: 2 bairros)
**Descrição:** Vértices do grafo para o algoritmo de Dijkstra.

**Dados Mínimos:**
- **Mínimo: 2 bairros** (para ter pelo menos uma conexão)
- **Recomendado: 3-5 bairros** para testes básicos
- **Produção: Conforme sua cidade**

**Exemplo:**
```sql
INSERT INTO bairros (nome) VALUES 
    ('Centro'),
    ('Jardim das Flores');
```

**Por quê:** 
- Necessário para cadastrar ambulâncias (precisam de um bairro base)
- Necessário para registrar ocorrências (precisam de um bairro de origem)
- Necessário para o algoritmo de Dijkstra calcular rotas

---

### 2. **ruas_conexoes** (OBRIGATÓRIA - Mínimo: 1 conexão)
**Descrição:** Arestas do grafo (conexões entre bairros) para o Dijkstra.

**Dados Mínimos:**
- **Mínimo: 1 conexão** (ligando 2 bairros)
- **Recomendado: N-1 conexões** (onde N = número de bairros) para um grafo conectado
- **Produção: Todas as conexões viárias reais**

**Exemplo:**
```sql
-- Conectar Centro com Jardim das Flores (distância de 5 km)
INSERT INTO ruas_conexoes (id_bairro_origem, id_bairro_destino, distancia_km) 
VALUES (1, 2, 5.0);
```

**Por quê:**
- Sem conexões, o Dijkstra não consegue calcular rotas
- Ambulâncias não podem ser despachadas sem caminhos

**⚠️ IMPORTANTE:** O grafo é **bidirecional**, mas você só precisa inserir uma vez (ex: Centro → Jardim). O sistema trata automaticamente como bidirecional.

---

### 3. **profissionais** (OBRIGATÓRIA - Mínimo: 2 profissionais)
**Descrição:** Profissionais de saúde (médicos, enfermeiros, condutores).

**Dados Mínimos:**
- **Mínimo para Ambulância BÁSICA: 2 profissionais**
  - 1 CONDUTOR
  - 1 ENFERMEIRO
  
- **Mínimo para Ambulância UTI: 3 profissionais**
  - 1 CONDUTOR
  - 1 ENFERMEIRO
  - 1 MEDICO

- **Recomendado: 6-9 profissionais** (para ter equipes em diferentes turnos)

**Exemplo:**
```sql
-- Para uma ambulância BÁSICA (mínimo)
INSERT INTO profissionais (nome, funcao, contato, turno, status, ativo) VALUES 
    ('João Silva', 'CONDUTOR', '(11) 99999-1111', 'MANHA', 'DISPONIVEL', true),
    ('Maria Santos', 'ENFERMEIRO', '(11) 99999-2222', 'MANHA', 'DISPONIVEL', true);

-- Para uma ambulância UTI (adicione um médico)
INSERT INTO profissionais (nome, funcao, contato, turno, status, ativo) VALUES 
    ('Dr. Carlos Oliveira', 'MEDICO', '(11) 99999-3333', 'MANHA', 'DISPONIVEL', true);
```

**Por quê:**
- Equipes precisam de profissionais para funcionar
- Sem equipe completa, ambulâncias não podem ser despachadas

**⚠️ IMPORTANTE:** 
- Todos os profissionais de uma equipe devem estar no **mesmo turno**
- Profissionais devem estar com status `DISPONIVEL` e `ativo = true`

---

### 4. **ambulancias** (OBRIGATÓRIA - Mínimo: 1 ambulância)
**Descrição:** Ambulâncias cadastradas no sistema.

**Dados Mínimos:**
- **Mínimo: 1 ambulância** (BÁSICA ou UTI)
- **Recomendado: 2 ambulâncias** (1 BÁSICA + 1 UTI) para testes completos

**Exemplo:**
```sql
-- Ambulância BÁSICA
INSERT INTO ambulancias (placa, tipo, status, id_bairro_base, ativa) 
VALUES ('ABC-1234', 'BASICA', 'DISPONIVEL', 1, true);

-- Ambulância UTI (opcional)
INSERT INTO ambulancias (placa, tipo, status, id_bairro_base, ativa) 
VALUES ('XYZ-5678', 'UTI', 'DISPONIVEL', 1, true);
```

**Por quê:**
- Sem ambulâncias, não há como despachar ocorrências
- Cada ambulância precisa de um bairro base (já cadastrado)

**⚠️ IMPORTANTE:**
- `id_bairro_base` deve existir na tabela `bairros`
- Status deve ser `DISPONIVEL` para poder ser despachada

---

### 5. **equipes** (OBRIGATÓRIA - Mínimo: 1 equipe)
**Descrição:** Equipes vinculadas às ambulâncias.

**Dados Mínimos:**
- **Mínimo: 1 equipe** (vinculada a 1 ambulância)
- **Recomendado: 1 equipe por ambulância**

**Exemplo:**
```sql
-- Equipe para ambulância BÁSICA (id_ambulancia = 1)
INSERT INTO equipes (descricao, id_ambulancia, ativa) 
VALUES ('Equipe Manhã - Básica', 1, true);
```

**Por quê:**
- Ambulâncias precisam de equipes para serem despachadas
- Sistema valida se a equipe está completa antes de despachar

---

### 6. **equipe_profissional** (OBRIGATÓRIA - Mínimo: 2 registros)
**Descrição:** Relacionamento entre equipes e profissionais.

**Dados Mínimos:**
- **Para Ambulância BÁSICA: 2 registros**
  - 1 CONDUTOR
  - 1 ENFERMEIRO
  
- **Para Ambulância UTI: 3 registros**
  - 1 CONDUTOR
  - 1 ENFERMEIRO
  - 1 MEDICO

**Exemplo:**
```sql
-- Equipe BÁSICA (id_equipe = 1)
-- Assumindo: id_profissional 1 = CONDUTOR, id_profissional 2 = ENFERMEIRO
INSERT INTO equipe_profissional (id_equipe, id_profissional) VALUES 
    (1, 1),  -- Condutor
    (1, 2);  -- Enfermeiro

-- Equipe UTI (id_equipe = 2, se tiver)
-- Assumindo: id_profissional 3 = MEDICO
INSERT INTO equipe_profissional (id_equipe, id_profissional) VALUES 
    (2, 1),  -- Condutor
    (2, 2),  -- Enfermeiro
    (2, 3);  -- Médico
```

**Por quê:**
- Vincula profissionais às equipes
- Sistema valida se a equipe tem todos os profissionais necessários

**⚠️ IMPORTANTE:**
- Todos os profissionais devem estar no **mesmo turno**
- Profissionais devem estar `DISPONIVEL` e `ativo = true`

---

### 7. **usuarios** (OBRIGATÓRIA - Mínimo: 1 usuário)
**Descrição:** Usuários do sistema (operadores, administradores).

**Dados Mínimos:**
- **Mínimo: 1 usuário** (admin ou operador)
- **Recomendado: 2 usuários** (1 admin + 1 operador)

**Exemplo:**
```sql
-- Usuário administrador
-- Senha: admin123 (hash BCrypt)
INSERT INTO usuarios (login, senha_hash, perfil, nome, email, ativo) 
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', 'Administrador', 'admin@sosrota.com', true);
```

**Por quê:**
- Necessário para autenticação no sistema
- Operadores registram ocorrências

**⚠️ IMPORTANTE:** 
- Use o script `CRIAR_USUARIO_ADMIN.sql` ou `ATUALIZAR_SENHA_ADMIN.sql` para criar usuários com hash correto

---

## 📊 Tabelas Opcionais (Preenchidas Automaticamente)

### 8. **ocorrencias** (OPCIONAL - Preenchida pelo sistema)
**Descrição:** Ocorrências registradas pelos operadores.

**Dados Mínimos:** 0 (será preenchida quando operadores registrarem ocorrências)

---

### 9. **atendimentos** (OPCIONAL - Preenchida pelo sistema)
**Descrição:** Atendimentos realizados (quando ambulâncias são despachadas).

**Dados Mínimos:** 0 (será preenchida quando ocorrências forem despachadas)

---

### 10. **atendimento_rota_conexao** (OPCIONAL - Preenchida pelo sistema)
**Descrição:** Caminho calculado pelo Dijkstra para cada atendimento.

**Dados Mínimos:** 0 (será preenchida automaticamente quando atendimentos forem criados)

**⚠️ IMPORTANTE:** Esta tabela precisa ser criada! Execute o script:
```sql
-- Use o arquivo: CREATE_TABLE_ATENDIMENTO_ROTA_CONEXAO.sql
```

---

## 📝 Scripts SQL Necessários (Ordem de Execução)

### 1. **schema.sql** (Base)
```sql
-- Cria todas as tabelas principais
-- Execute primeiro
```

### 2. **ALTER_TABLE_PROFISSIONAIS.sql** (Atualização)
```sql
-- Adiciona campos turno e status na tabela profissionais
-- Execute após schema.sql
```

### 3. **CREATE_TABLE_ATENDIMENTO_ROTA_CONEXAO.sql** (Nova tabela)
```sql
-- Cria tabela para armazenar caminhos do Dijkstra
-- Execute após schema.sql
```

### 4. **CRIAR_USUARIO_ADMIN.sql** ou **ATUALIZAR_SENHA_ADMIN.sql** (Usuários)
```sql
-- Cria usuário administrador
-- Execute após schema.sql
```

---

## ✅ Checklist Mínimo para Sistema Funcionar

### Configuração Básica (Mínimo Absoluto):
- [ ] **2 bairros** cadastrados
- [ ] **1 conexão** entre os bairros (ruas_conexoes)
- [ ] **2 profissionais** (1 CONDUTOR + 1 ENFERMEIRO) - mesmo turno
- [ ] **1 ambulância BÁSICA** cadastrada
- [ ] **1 equipe** vinculada à ambulância
- [ ] **2 registros** em equipe_profissional (vinculando os 2 profissionais)
- [ ] **1 usuário** (admin ou operador)

### Configuração Recomendada (Para Testes Completos):
- [ ] **3-5 bairros** cadastrados
- [ ] **N-1 conexões** (grafo conectado)
- [ ] **6-9 profissionais** (2-3 de cada função, em diferentes turnos)
- [ ] **2 ambulâncias** (1 BÁSICA + 1 UTI)
- [ ] **2 equipes** (1 para cada ambulância)
- [ ] **2 usuários** (1 admin + 1 operador)

---

## 🚀 Exemplo de Setup Completo (SQL)

```sql
-- 1. BAIRROS (mínimo 2)
INSERT INTO bairros (nome) VALUES 
    ('Centro'),
    ('Jardim das Flores'),
    ('Vila Nova')
ON CONFLICT (nome) DO NOTHING;

-- 2. CONEXÕES (mínimo 1, recomendado N-1)
INSERT INTO ruas_conexoes (id_bairro_origem, id_bairro_destino, distancia_km) 
VALUES 
    (1, 2, 5.0),  -- Centro → Jardim (5 km)
    (2, 3, 3.5),  -- Jardim → Vila Nova (3.5 km)
    (1, 3, 7.0)   -- Centro → Vila Nova (7 km)
ON CONFLICT (id_bairro_origem, id_bairro_destino) DO NOTHING;

-- 3. PROFISSIONAIS (mínimo 2 para BÁSICA, 3 para UTI)
INSERT INTO profissionais (nome, funcao, contato, turno, status, ativo) VALUES 
    ('João Silva', 'CONDUTOR', '(11) 99999-1111', 'MANHA', 'DISPONIVEL', true),
    ('Maria Santos', 'ENFERMEIRO', '(11) 99999-2222', 'MANHA', 'DISPONIVEL', true),
    ('Dr. Carlos Oliveira', 'MEDICO', '(11) 99999-3333', 'MANHA', 'DISPONIVEL', true)
ON CONFLICT DO NOTHING;

-- 4. AMBULÂNCIAS (mínimo 1)
INSERT INTO ambulancias (placa, tipo, status, id_bairro_base, ativa) 
VALUES 
    ('ABC-1234', 'BASICA', 'DISPONIVEL', 1, true),
    ('XYZ-5678', 'UTI', 'DISPONIVEL', 1, true)
ON CONFLICT (placa) DO NOTHING;

-- 5. EQUIPES (mínimo 1)
INSERT INTO equipes (descricao, id_ambulancia, ativa) 
VALUES 
    ('Equipe Manhã - Básica', 1, true),
    ('Equipe Manhã - UTI', 2, true)
ON CONFLICT DO NOTHING;

-- 6. EQUIPE_PROFISSIONAL (mínimo 2 para BÁSICA, 3 para UTI)
INSERT INTO equipe_profissional (id_equipe, id_profissional) VALUES 
    (1, 1),  -- Equipe Básica: Condutor
    (1, 2),  -- Equipe Básica: Enfermeiro
    (2, 1),  -- Equipe UTI: Condutor
    (2, 2),  -- Equipe UTI: Enfermeiro
    (2, 3)   -- Equipe UTI: Médico
ON CONFLICT (id_equipe, id_profissional) DO NOTHING;

-- 7. USUÁRIOS (mínimo 1)
-- Use o script CRIAR_USUARIO_ADMIN.sql para criar com hash correto
```

---

## ⚠️ Validações Importantes

### Para Criar Equipe:
1. ✅ Ambulância deve existir e estar `ativa = true`
2. ✅ Profissionais devem estar `ativo = true`
3. ✅ Profissionais devem estar com `status = 'DISPONIVEL'`
4. ✅ Todos os profissionais devem estar no **mesmo turno**
5. ✅ Profissionais não podem estar em outra equipe ativa
6. ✅ Equipe BÁSICA precisa: CONDUTOR + ENFERMEIRO
7. ✅ Equipe UTI precisa: CONDUTOR + ENFERMEIRO + MEDICO

### Para Despachar Ocorrência:
1. ✅ Ocorrência deve estar com `status = 'ABERTA'`
2. ✅ Deve existir ambulância `DISPONIVEL` do tipo correto
3. ✅ Ambulância deve ter equipe completa
4. ✅ Deve existir caminho (conexões) entre bairro da ambulância e bairro da ocorrência
5. ✅ Tempo estimado deve estar dentro do SLA

---

## 📊 Resumo por Tabela

| Tabela | Mínimo | Recomendado | Obrigatória? |
|--------|--------|-------------|--------------|
| **bairros** | 2 | 3-5 | ✅ SIM |
| **ruas_conexoes** | 1 | N-1 | ✅ SIM |
| **profissionais** | 2 (BÁSICA) / 3 (UTI) | 6-9 | ✅ SIM |
| **ambulancias** | 1 | 2 | ✅ SIM |
| **equipes** | 1 | 2 | ✅ SIM |
| **equipe_profissional** | 2 (BÁSICA) / 3 (UTI) | 5-6 | ✅ SIM |
| **usuarios** | 1 | 2 | ✅ SIM |
| **ocorrencias** | 0 | - | ❌ NÃO (auto) |
| **atendimentos** | 0 | - | ❌ NÃO (auto) |
| **atendimento_rota_conexao** | 0 | - | ❌ NÃO (auto) |

---

## 🎯 Conclusão

**Para o sistema funcionar corretamente, você precisa de:**

1. **Estrutura do grafo:** 2+ bairros + 1+ conexões
2. **Recursos humanos:** 2-3 profissionais (dependendo do tipo de ambulância)
3. **Recursos físicos:** 1+ ambulância com equipe completa
4. **Acesso:** 1+ usuário para operar o sistema

**Total mínimo:** ~10 registros distribuídos em 7 tabelas obrigatórias.

