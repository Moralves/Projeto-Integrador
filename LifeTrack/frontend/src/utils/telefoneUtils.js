/**
 * Formata telefone para o padrão brasileiro: (XX) XXXXX-XXXX ou (XX) XXXX-XXXX
 * @param {string} telefone - Número de telefone sem formatação
 * @returns {string} - Telefone formatado
 */
export const formatarTelefone = (telefone) => {
  if (!telefone) return '';
  
  // Remove tudo que não é número
  const apenasNumeros = telefone.replace(/\D/g, '');
  
  // Limita a 11 dígitos (máximo para celular brasileiro)
  const numerosLimitados = apenasNumeros.slice(0, 11);
  
  // Aplica a máscara conforme o tamanho
  if (numerosLimitados.length <= 2) {
    return `(${numerosLimitados}`;
  } else if (numerosLimitados.length <= 6) {
    return `(${numerosLimitados.slice(0, 2)}) ${numerosLimitados.slice(2)}`;
  } else if (numerosLimitados.length <= 10) {
    return `(${numerosLimitados.slice(0, 2)}) ${numerosLimitados.slice(2, 6)}-${numerosLimitados.slice(6)}`;
  } else {
    // 11 dígitos (celular)
    return `(${numerosLimitados.slice(0, 2)}) ${numerosLimitados.slice(2, 7)}-${numerosLimitados.slice(7)}`;
  }
};

/**
 * Remove a formatação do telefone, deixando apenas números
 * @param {string} telefone - Telefone formatado
 * @returns {string} - Telefone apenas com números
 */
export const removerFormatacaoTelefone = (telefone) => {
  if (!telefone) return '';
  return telefone.replace(/\D/g, '');
};

/**
 * Valida se o telefone está no formato correto
 * @param {string} telefone - Telefone para validar
 * @returns {boolean} - True se válido
 */
export const validarTelefone = (telefone) => {
  const apenasNumeros = removerFormatacaoTelefone(telefone);
  // Telefone brasileiro: 10 dígitos (fixo) ou 11 dígitos (celular)
  return apenasNumeros.length === 10 || apenasNumeros.length === 11;
};


