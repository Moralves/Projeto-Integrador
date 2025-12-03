# 🎯 Recomendação: Interface do Operador

Este documento apresenta uma recomendação profissional para implementar a interface do **operador** que registra chamadas de emergência no sistema LifeTrack.

---

## 📋 Visão Geral

### Objetivo
Criar uma interface específica para operadores (usuários com perfil `USER`) que permite:
- ✅ Registrar novas ocorrências de emergência
- ✅ Visualizar ocorrências registradas
- ✅ Filtrar e buscar ocorrências
- ✅ Ver detalhes de ocorrências

### Diferença entre Admin e Operador

| Funcionalidade | Admin | Operador |
|----------------|-------|----------|
| Gerenciar usuários | ✅ | ❌ |
| Gerenciar ambulâncias | ✅ | ❌ |
| Gerenciar equipes | ✅ | ❌ |
| Gerenciar profissionais | ✅ | ❌ |
| **Registrar ocorrências** | ✅ | ✅ |
| **Visualizar ocorrências** | ✅ | ✅ |
| Despachar ocorrências | ✅ | ❌ (futuro) |

---

## 🏗️ Arquitetura Recomendada

### Estrutura de Arquivos

```
frontend/src/
├── pages/
│   ├── Login.jsx                    ✅ (já existe)
│   ├── admin/
│   │   ├── AdminLayout.jsx          ✅ (já existe)
│   │   └── sections/                ✅ (já existe)
│   └── operador/                    🆕 (NOVO)
│       ├── OperatorLayout.jsx       🆕 Layout do operador
│       ├── OperatorLayout.css       🆕 Estilos do layout
│       └── sections/
│           ├── RegistrarOcorrencia.jsx  🆕 Formulário de registro
│           ├── ListarOcorrencias.jsx    🆕 Lista de ocorrências
│           └── DetalhesOcorrencia.jsx   🆕 (opcional) Detalhes
│
└── services/
    ├── authService.js               ✅ (já existe)
    ├── ocorrenciaService.js         🆕 Service para ocorrências
    └── bairroService.js             🆕 Service para bairros (para select)
```

---

## 🎨 Interface do Operador

### 1. Layout Principal (OperatorLayout.jsx)

**Características:**
- Sidebar simplificada (apenas 2 opções)
- Área de conteúdo principal
- Botão de logout
- Design profissional e intuitivo

**Menu:**
- 📝 **Registrar Ocorrência** (página principal)
- 📋 **Ocorrências Registradas** (lista)
- 🚪 **Sair** (logout)

### 2. Página: Registrar Ocorrência

**Formulário deve conter:**

1. **Bairro/Localização** (Select obrigatório)
   - Carregar lista de bairros do backend
   - Campo obrigatório

2. **Tipo de Ocorrência** (Select obrigatório)
   - Opções sugeridas:
     - Acidente de Trânsito
     - Atendimento Médico
     - Resgate
     - Incêndio
     - Outros
   - Ou campo de texto livre

3. **Gravidade** (Select obrigatório)
   - BAIXA
   - MEDIA
   - ALTA
   - (Conforme enum `Gravidade`)

4. **Observações** (Textarea opcional)
   - Campo de texto livre
   - Máximo 1000 caracteres
   - Placeholder: "Descreva detalhes adicionais da ocorrência..."

5. **Botões:**
   - "Registrar Ocorrência" (primário)
   - "Limpar" (secundário)

**Validações:**
- Todos os campos obrigatórios devem ser preenchidos
- Feedback visual de sucesso/erro
- Mensagem de confirmação após registro

### 3. Página: Ocorrências Registradas

**Funcionalidades:**

1. **Lista de Ocorrências**
   - Tabela com colunas:
     - ID
     - Data/Hora
     - Bairro
     - Tipo
     - Gravidade (com badge colorido)
     - Status (com badge colorido)
     - Ações (ver detalhes)

2. **Filtros**
   - Por status (Todas, Abertas, Em Atendimento, Concluídas)
   - Por gravidade (Todas, Baixa, Média, Alta)
   - Por data (opcional)

3. **Busca**
   - Campo de busca por tipo ou bairro

4. **Paginação** (se necessário)
   - Limitar a 20-50 ocorrências por página

**Badges de Status:**
- 🟢 **ABERTA** - Verde
- 🟡 **EM_ATENDIMENTO** - Amarelo
- 🔵 **CONCLUIDA** - Azul
- 🔴 **CANCELADA** - Vermelho

**Badges de Gravidade:**
- 🟢 **BAIXA** - Verde claro
- 🟡 **MEDIA** - Amarelo
- 🔴 **ALTA** - Vermelho

---

## 🔧 Implementação Técnica

### 1. Service: ocorrenciaService.js

```javascript
const API_URL = 'http://localhost:8081/api';

export const ocorrenciaService = {
  async listar() {
    const response = await fetch(`${API_URL}/ocorrencias`);
    if (!response.ok) throw new Error('Erro ao listar ocorrências');
    return response.json();
  },

  async registrar(dados) {
    const response = await fetch(`${API_URL}/ocorrencias`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(dados),
    });
    if (!response.ok) {
      const error = await response.text();
      throw new Error(error || 'Erro ao registrar ocorrência');
    }
    return response.json();
  },

  async buscarPorId(id) {
    const response = await fetch(`${API_URL}/ocorrencias/${id}`);
    if (!response.ok) throw new Error('Erro ao buscar ocorrência');
    return response.json();
  },
};
```

### 2. Service: bairroService.js

```javascript
const API_URL = 'http://localhost:8081/api';

export const bairroService = {
  async listar() {
    const response = await fetch(`${API_URL}/bairros`);
    if (!response.ok) throw new Error('Erro ao listar bairros');
    return response.json();
  },
};
```

### 3. Atualizar App.jsx

**Lógica de roteamento baseada em perfil:**

```javascript
// Se ADMIN → AdminLayout
// Se USER → OperatorLayout
// Se não autenticado → Login

const user = authService.getCurrentUser();
if (user?.perfil === 'ADMIN') {
  return <AdminLayout />;
} else if (user?.perfil === 'USER') {
  return <OperatorLayout />;
} else {
  return <Login />;
}
```

### 4. Backend: Endpoint de Bairros

**⚠️ IMPORTANTE:** O endpoint `GET /api/bairros` **NÃO EXISTE** ainda.

**Precisa criar:**
- `BairroControlador.java` com endpoint `listar()`

**Código sugerido:**

```java
package com.vitalistech.sosrota.web.controlador;

import com.vitalistech.sosrota.dominio.modelo.Bairro;
import com.vitalistech.sosrota.dominio.repositorio.BairroRepositorio;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bairros")
@CrossOrigin(origins = "*")
public class BairroControlador {

    private final BairroRepositorio bairroRepositorio;

    public BairroControlador(BairroRepositorio bairroRepositorio) {
        this.bairroRepositorio = bairroRepositorio;
    }

    @GetMapping
    public List<Bairro> listar() {
        return bairroRepositorio.findAll();
    }
}
```

**Localização:** `backend/src/main/java/com/vitalistech/sosrota/web/controlador/BairroControlador.java`

---

## 🎯 Fluxo de Uso

### Fluxo: Registrar Ocorrência

```
1. Operador faz login
   └─> Sistema identifica perfil "USER"
       └─> Redireciona para OperatorLayout

2. Operador acessa "Registrar Ocorrência"
   └─> Formulário carrega
       ├─> Select de bairros (busca do backend)
       └─> Campos vazios prontos para preenchimento

3. Operador preenche formulário
   ├─> Seleciona bairro
   ├─> Seleciona tipo de ocorrência
   ├─> Seleciona gravidade
   └─> (Opcional) Adiciona observações

4. Operador clica em "Registrar"
   └─> ocorrenciaService.registrar(dados)
       └─> POST /api/ocorrencias
           └─> OcorrenciaControlador.registrar()
               ├─> Valida dados
               ├─> Cria ocorrência com status "ABERTA"
               └─> Retorna ocorrência criada

5. Frontend recebe resposta
   ├─> Mostra mensagem de sucesso
   ├─> Limpa formulário
   └─> (Opcional) Redireciona para lista
```

### Fluxo: Visualizar Ocorrências

```
1. Operador acessa "Ocorrências Registradas"
   └─> useEffect() executa
       └─> ocorrenciaService.listar()
           └─> GET /api/ocorrencias
               └─> OcorrenciaControlador.listar()
                   └─> Retorna todas as ocorrências

2. Frontend renderiza lista
   ├─> Aplica filtros (se houver)
   ├─> Aplica busca (se houver)
   └─> Exibe tabela formatada

3. Operador pode:
   ├─> Filtrar por status/gravidade
   ├─> Buscar por texto
   └─> Ver detalhes (modal ou página)
```

---

## 🎨 Design e UX

### Princípios de Design

1. **Simplicidade**
   - Interface limpa e focada
   - Apenas funcionalidades essenciais
   - Menos opções = menos confusão

2. **Eficiência**
   - Formulário de registro rápido
   - Campos grandes e fáceis de clicar
   - Feedback imediato

3. **Clareza**
   - Labels descritivos
   - Mensagens de erro claras
   - Confirmações visuais

### Paleta de Cores Sugerida

- **Primária:** Azul (#2563eb) - Botões principais
- **Sucesso:** Verde (#10b981) - Confirmações
- **Atenção:** Amarelo (#f59e0b) - Avisos
- **Erro:** Vermelho (#ef4444) - Erros
- **Neutro:** Cinza (#6b7280) - Textos secundários

### Responsividade

- **Desktop-first** (conforme solicitado)
- Layout otimizado para telas grandes
- Tabelas com scroll horizontal se necessário
- Formulários em 2 colunas (se espaço permitir)

---

## ✅ Checklist de Implementação

### Backend
- [ ] **CRIAR** `BairroControlador.java` com endpoint `GET /api/bairros`
  - O modelo `Bairro` e `BairroRepositorio` já existem
  - Precisa criar apenas o controlador REST
- [ ] Verificar se `GET /api/ocorrencias` retorna dados completos
- [ ] Verificar se `POST /api/ocorrencias` está funcionando
- [ ] Testar endpoints com Postman/Insomnia

### Frontend - Estrutura
- [ ] Criar pasta `frontend/src/pages/operador/`
- [ ] Criar `OperatorLayout.jsx`
- [ ] Criar `OperatorLayout.css`
- [ ] Criar pasta `frontend/src/pages/operador/sections/`

### Frontend - Services
- [ ] Criar `ocorrenciaService.js`
- [ ] Criar `bairroService.js`
- [ ] Testar chamadas de API

### Frontend - Componentes
- [ ] Criar `RegistrarOcorrencia.jsx`
- [ ] Criar `ListarOcorrencias.jsx`
- [ ] Implementar formulário de registro
- [ ] Implementar lista com filtros
- [ ] Adicionar validações
- [ ] Adicionar feedback visual

### Frontend - Integração
- [ ] Atualizar `App.jsx` para rotear por perfil
- [ ] Testar login como operador
- [ ] Testar login como admin
- [ ] Verificar redirecionamentos

### Testes
- [ ] Testar registro de ocorrência
- [ ] Testar listagem de ocorrências
- [ ] Testar filtros
- [ ] Testar busca
- [ ] Testar validações
- [ ] Testar mensagens de erro

---

## 🚀 Próximos Passos

### Fase 1: MVP (Mínimo Viável)
1. Layout básico do operador
2. Formulário de registro funcional
3. Lista simples de ocorrências

### Fase 2: Melhorias
1. Filtros e busca
2. Detalhes da ocorrência
3. Paginação
4. Notificações em tempo real (futuro)

### Fase 3: Funcionalidades Avançadas
1. Edição de ocorrências (se permitido)
2. Cancelamento de ocorrências
3. Histórico de ações
4. Relatórios

---

## 📝 Observações Importantes

### Segurança
- ✅ Operador só pode **criar** ocorrências
- ✅ Operador pode **visualizar** todas as ocorrências
- ❌ Operador **não pode** editar ou deletar
- ❌ Operador **não pode** despachar (reservado para admin)

### Performance
- Carregar bairros apenas uma vez (cache no frontend)
- Paginar ocorrências se houver muitas
- Lazy loading de imagens (se houver)

### Acessibilidade
- Labels descritivos
- Contraste adequado
- Navegação por teclado
- Mensagens de erro claras

---

## 🎓 Exemplo de Código

### OperatorLayout.jsx (Estrutura)

```javascript
import { useState } from 'react';
import RegistrarOcorrencia from './sections/RegistrarOcorrencia';
import ListarOcorrencias from './sections/ListarOcorrencias';
import './OperatorLayout.css';

function OperatorLayout({ onLogout }) {
  const [activeSection, setActiveSection] = useState('registrar');

  const menuItems = [
    { id: 'registrar', label: 'Registrar Ocorrência', icon: '📝' },
    { id: 'ocorrencias', label: 'Ocorrências', icon: '📋' },
  ];

  const renderContent = () => {
    switch (activeSection) {
      case 'registrar':
        return <RegistrarOcorrencia />;
      case 'ocorrencias':
        return <ListarOcorrencias />;
      default:
        return <RegistrarOcorrencia />;
    }
  };

  return (
    <div className="operator-layout">
      <aside className="operator-sidebar">
        {/* Header, Menu, Footer */}
      </aside>
      <main className="operator-content">
        {renderContent()}
      </main>
    </div>
  );
}
```

---

## 💡 Dicas de Implementação

1. **Comece pelo Service**
   - Crie os services primeiro
   - Teste as chamadas de API
   - Depois crie os componentes

2. **Reutilize Estilos**
   - Use classes similares ao AdminLayout
   - Mantenha consistência visual
   - Adapte cores conforme necessário

3. **Validação Dupla**
   - Validação no frontend (UX)
   - Validação no backend (segurança)

4. **Feedback Constante**
   - Loading states
   - Mensagens de sucesso
   - Mensagens de erro claras

---

## 📞 Dúvidas?

Se tiver dúvidas durante a implementação:
1. Consulte o código existente do AdminLayout
2. Veja os padrões já estabelecidos
3. Teste cada funcionalidade isoladamente
4. Use o console do navegador para debug

---

**Boa implementação! 🚀**

