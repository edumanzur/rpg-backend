# Implementação: Validação de Requisitos e Paginação

Data: 18/05/2026
Testes: ✅ 193/193 passando

## 1. Validação de Requisitos de Habilidades e Equipamentos

### Problema Resolvido
AbilityRequirement e EquipmentRequirement existiam como dados mas nunca eram validados quando associados a personagens. Agora há endpoints para verificar se um personagem pode usar uma habilidade ou equipamento.

### Novas DTOs

#### CharacterAbilityValidationDTO
```java
- characterId: Long
- abilityId: Long
- abilityName: String
- canUse: Boolean (indica se o personagem pode usar a habilidade)
- requirementChecks: List<RequirementCheckDTO>
  - requiredClass: String
  - meetsLevelRequirement: Boolean
  - requiredLevel: Integer
  - characterLevel: Integer
  - meetsStrengthRequirement: Boolean
  - requiredStrength: Integer
  - characterStrength: Integer
  - meetsDexterityRequirement: Boolean
  - requiredDexterity: Integer
  - characterDexterity: Integer
  - meetsConstitutionRequirement: Boolean
  - requiredConstitution: Integer
  - characterConstitution: Integer
  - meetsIntelligenceRequirement: Boolean
  - requiredIntelligence: Integer
  - characterIntelligence: Integer
  - meetsWisdomRequirement: Boolean
  - requiredWisdom: Integer
  - characterWisdom: Integer
  - meetsCharismaRequirement: Boolean
  - requiredCharisma: Integer
  - characterCharisma: Integer
  - canUseForThisClass: Boolean
```

#### CharacterEquipmentValidationDTO
Similar ao anterior, mas sem requirements de nível (equipamentos não têm minLevel).

### Novos Endpoints

#### GET `/characters/{id}/validate/ability/{abilityId}`
Valida se um personagem pode usar uma habilidade específica.

**Resposta (200 OK):**
```json
{
  "characterId": 1,
  "abilityId": 5,
  "abilityName": "Fireball",
  "canUse": true,
  "requirementChecks": [
    {
      "requiredClass": "Mage",
      "meetsLevelRequirement": true,
      "requiredLevel": 3,
      "characterLevel": 10,
      "meetsStrengthRequirement": true,
      "requiredStrength": 0,
      "characterStrength": 11,
      "meetsDexterityRequirement": true,
      "requiredDexterity": 0,
      "characterDexterity": 15,
      "meetsConstitutionRequirement": true,
      "requiredConstitution": 0,
      "characterConstitution": 12,
      "meetsIntelligenceRequirement": true,
      "requiredIntelligence": 2,
      "characterIntelligence": 18,
      "meetsWisdomRequirement": true,
      "requiredWisdom": 0,
      "characterWisdom": 14,
      "meetsCharismaRequirement": true,
      "requiredCharisma": 0,
      "characterCharisma": 13,
      "canUseForThisClass": true
    }
  ]
}
```

#### GET `/characters/{id}/validate/equipment/{equipmentId}`
Valida se um personagem pode equipar um item específico.

**Resposta (200 OK):**
Semelhante ao endpoint de habilidade, mas sem validação de nível.

### Implementação Técnica

#### Cálculo de Stats
Os stats do personagem são calculados como:
```
Base (10) + Bonus da Classe + Bonus da Raça
```

Exemplo para um Ranger (Dexterity +2) de Raça Humana (Dexterity +1):
```
Dexterity = 10 + 2 + 1 = 13
```

#### Validação de Classe
Um personagem só pode usar um requisito se:
1. Sua classe corresponde à classe do requisito
2. Atende TODOS os requisitos de stats

---

## 2. Paginação para findByMasterId e findByUserId

### Problema Resolvido
Os métodos `findByMasterId` e `findByUserId` retornavam `List` sem paginação, problematicamente com grandes volumes de dados.

### Novos Métodos no Repository

#### CampaignRepository
```java
// Existente - retorna List
List<Campaign> findByMasterId(Long masterId);

// Novo - com paginação
Page<Campaign> findByMasterId(Long masterId, Pageable pageable);
```

#### CharacterRepository
```java
// Existente - retorna List
List<Character> findByUserId(Long userId);

// Novo - com paginação
Page<Character> findByUserId(Long userId, Pageable pageable);
```

### Novos Métodos nos Services

#### CampaignService
```java
// Existente
public List<CampaignResponseDTO> findCampaignsByMasterId(Authentication auth, Long masterId)

// Novo
public Page<CampaignResponseDTO> findCampaignsByMasterId(Authentication auth, Long masterId, Pageable pageable)
```

#### CharacterService
```java
// Existente
public List<CharacterResponseDTO> findCharactersByUserId(Authentication auth, Long userId)

// Novo
public Page<CharacterResponseDTO> findCharactersByUserId(Authentication auth, Long userId, Pageable pageable)
```

### Novos Endpoints

#### GET `/campaigns/master/{masterId}/page`
Retorna campanhas de um mestre com paginação.

**Query Parameters:**
- `page` (0-indexed): número da página (padrão: 0)
- `size`: número de registros por página (padrão: 20)
- `sort`: campo para ordenação (ex: `name,desc`)

**Exemplo:**
```
GET /campaigns/master/1/page?page=0&size=10
```

**Resposta (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Campaign 1",
      "description": "Epic Quest",
      "masterId": 1,
      "status": true,
      "createdAt": "2026-05-18T10:00:00",
      "updatedAt": "2026-05-18T10:00:00"
    }
  ],
  "pageable": {
    "sort": {
      "empty": false,
      "sorted": true,
      "unsorted": false
    },
    "offset": 0,
    "pageNumber": 0,
    "pageSize": 10,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 1,
  "totalElements": 1,
  "last": true,
  "size": 10,
  "number": 0,
  "sort": {
    "empty": false,
    "sorted": true,
    "unsorted": false
  },
  "numberOfElements": 1,
  "first": true,
  "empty": false
}
```

#### GET `/characters/user/{userId}/page`
Retorna personagens de um usuário com paginação.

**Query Parameters:**
Mesmos que acima.

**Exemplo:**
```
GET /characters/user/1/page?page=0&size=10&sort=name,asc
```

### Compatibilidade

Os métodos antigos continuam disponíveis para manter compatibilidade com código existente:
- `GET /campaigns/master/{masterId}` - retorna List
- `GET /characters/user/{userId}` - retorna List

Os novos endpoints fornecem alternativas paginadas:
- `GET /campaigns/master/{masterId}/page` - retorna Page
- `GET /characters/user/{userId}/page` - retorna Page

---

## 3. Testes Adicionados

✅ **testValidateAbilityUsageSuccess** - Valida que um personagem pode usar uma habilidade  
✅ **testFindCharactersByUserIdWithPaginationSuccess** - Testa paginação de personagens por usuário  

Total: **2 novos testes**  
Resultado: **193/193 testes passando**

---

## 4. Estrutura de Dados de Stats

Os stats dos personagens são compostos por 6 atributos (D&D 5e style):
- Strength (Força)
- Dexterity (Destreza)
- Constitution (Constituição)
- Intelligence (Inteligência)
- Wisdom (Sabedoria)
- Charisma (Carisma)

Cada atributo é calculado como:
```
Base (10) + Class Bonus + Race Bonus + Equipment Bonus (futuro)
```

---

## 5. Notas de Implementação

### Helper: getCharacterStat()
Método privado que calcula o stat atual de um personagem baseado em sua classe e raça.

### Authorization
Validação de requisitos respeita autorização existente:
- Apenas proprietários do personagem ou ADMINs podem validar requisitos
- Validação é read-only (não modifica dados)

### Performance
- Queries pagináveis evitam carregar desnecessariamente grandes volumes de dados
- Stats são calculados em memória (sem queries adicionais)
- Método getCharacterStat() usa switch para performance

---

## 6. Exemplo de Uso

### Validar se um personagem pode usar uma habilidade:
```bash
curl -X GET "http://localhost:8080/characters/1/validate/ability/5" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json"
```

### Validar se um personagem pode equipar um item:
```bash
curl -X GET "http://localhost:8080/characters/1/validate/equipment/3" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json"
```

### Listar campanhas com paginação:
```bash
curl -X GET "http://localhost:8080/campaigns/master/1/page?page=0&size=10&sort=name,asc" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json"
```

### Listar personagens com paginação:
```bash
curl -X GET "http://localhost:8080/characters/user/1/page?page=0&size=10&sort=level,desc" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json"
```

