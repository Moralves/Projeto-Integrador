import { useState, useRef, useEffect } from 'react';

/**
 * Componente de select com autocomplete (permite digitação e seleção)
 */
function AutocompleteSelect({ 
  options, 
  value, 
  onChange, 
  placeholder = "Digite ou selecione...",
  getOptionLabel = (opt) => opt.label || opt.nome || opt,
  getOptionValue = (opt) => opt.value || opt.id || opt,
  required = false
}) {
  const [isOpen, setIsOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [highlightedIndex, setHighlightedIndex] = useState(-1);
  const containerRef = useRef(null);
  const inputRef = useRef(null);

  // Encontrar a opção selecionada
  const selectedOption = options.find(opt => {
    const optValue = getOptionValue(opt);
    return String(optValue) === String(value);
  });

  // Filtrar opções baseado no termo de busca
  const filteredOptions = options.filter(opt => {
    const label = getOptionLabel(opt).toLowerCase();
    return label.includes(searchTerm.toLowerCase());
  });

  // Atualizar searchTerm quando value mudar externamente
  useEffect(() => {
    if (value && value !== '' && selectedOption) {
      setSearchTerm(getOptionLabel(selectedOption));
    } else {
      setSearchTerm('');
    }
  }, [value]);

  // Fechar quando clicar fora
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (containerRef.current && !containerRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleInputChange = (e) => {
    const term = e.target.value;
    setSearchTerm(term);
    setIsOpen(true);
    setHighlightedIndex(-1);

    // Se o termo corresponder exatamente a uma opção, selecionar
    const exactMatch = options.find(opt => 
      getOptionLabel(opt).toLowerCase() === term.toLowerCase()
    );
    if (exactMatch) {
      onChange(getOptionValue(exactMatch));
    } else if (term === '') {
      onChange('');
    }
  };

  const handleSelect = (option) => {
    const optionValue = getOptionValue(option);
    const optionLabel = getOptionLabel(option);
    onChange(optionValue);
    setSearchTerm(optionLabel);
    setIsOpen(false);
    inputRef.current?.blur();
  };

  const handleKeyDown = (e) => {
    if (!isOpen && (e.key === 'ArrowDown' || e.key === 'Enter')) {
      setIsOpen(true);
      return;
    }

    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setHighlightedIndex(prev => 
        prev < filteredOptions.length - 1 ? prev + 1 : prev
      );
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setHighlightedIndex(prev => prev > 0 ? prev - 1 : -1);
    } else if (e.key === 'Enter' && highlightedIndex >= 0) {
      e.preventDefault();
      handleSelect(filteredOptions[highlightedIndex]);
    } else if (e.key === 'Escape') {
      setIsOpen(false);
      inputRef.current?.blur();
    }
  };

  return (
    <div ref={containerRef} style={{ position: 'relative', width: '100%' }}>
      <input
        ref={inputRef}
        type="text"
        value={searchTerm}
        onChange={handleInputChange}
        onFocus={() => setIsOpen(true)}
        onKeyDown={handleKeyDown}
        placeholder={placeholder}
        required={required}
        style={{
          width: '100%',
          padding: '12px',
          border: '1px solid #d1d5db',
          borderRadius: '8px',
          fontSize: '1rem',
          boxSizing: 'border-box'
        }}
      />
      {isOpen && filteredOptions.length > 0 && (
        <div style={{
          position: 'absolute',
          top: '100%',
          left: 0,
          right: 0,
          backgroundColor: 'white',
          border: '1px solid #d1d5db',
          borderRadius: '8px',
          marginTop: '4px',
          maxHeight: '200px',
          overflowY: 'auto',
          zIndex: 1000,
          boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)'
        }}>
          {filteredOptions.map((option, index) => {
            const optionValue = getOptionValue(option);
            const optionLabel = getOptionLabel(option);
            const isHighlighted = index === highlightedIndex;
            const isSelected = optionValue === value;

            return (
              <div
                key={optionValue}
                onClick={() => handleSelect(option)}
                onMouseEnter={() => setHighlightedIndex(index)}
                style={{
                  padding: '12px',
                  cursor: 'pointer',
                  backgroundColor: isHighlighted 
                    ? '#f3f4f6' 
                    : isSelected 
                      ? '#e0e7ff' 
                      : 'white',
                  borderBottom: index < filteredOptions.length - 1 
                    ? '1px solid #e5e7eb' 
                    : 'none',
                  fontWeight: isSelected ? '600' : '400',
                  color: isSelected ? '#2563eb' : '#374151'
                }}
              >
                {optionLabel}
              </div>
            );
          })}
        </div>
      )}
      {isOpen && filteredOptions.length === 0 && searchTerm && (
        <div style={{
          position: 'absolute',
          top: '100%',
          left: 0,
          right: 0,
          backgroundColor: 'white',
          border: '1px solid #d1d5db',
          borderRadius: '8px',
          marginTop: '4px',
          padding: '12px',
          zIndex: 1000,
          boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
          color: '#6b7280'
        }}>
          Nenhuma opção encontrada
        </div>
      )}
    </div>
  );
}

export default AutocompleteSelect;

