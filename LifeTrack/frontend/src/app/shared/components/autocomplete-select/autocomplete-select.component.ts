import { Component, Input, Output, EventEmitter, forwardRef, ElementRef, ViewChild, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, FormsModule } from '@angular/forms';

@Component({
  selector: 'app-autocomplete-select',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './autocomplete-select.component.html',
  styleUrls: ['./autocomplete-select.component.css'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => AutocompleteSelectComponent),
      multi: true
    }
  ]
})
export class AutocompleteSelectComponent implements ControlValueAccessor {
  @Input() options: any[] = [];
  @Input() placeholder: string = "Digite ou selecione...";
  @Input() required: boolean = false;
  
  @Input() getOptionLabel: (opt: any) => string = (opt) => opt?.label || opt?.nome || String(opt);
  @Input() getOptionValue: (opt: any) => any = (opt) => opt?.value || opt?.id || opt;

  @Output() valueChange = new EventEmitter<any>();

  isOpen: boolean = false;
  searchTerm: string = '';
  highlightedIndex: number = -1;
  innerValue: any = '';

  @ViewChild('inputRef') inputRef!: ElementRef<HTMLInputElement>;
  @ViewChild('containerRef') containerRef!: ElementRef<HTMLDivElement>;

  // Form Control callbacks
  onChange: any = () => {};
  onTouched: any = () => {};

  @HostListener('document:mousedown', ['$event'])
  onClickOutside(event: Event) {
    if (this.containerRef?.nativeElement && !this.containerRef.nativeElement.contains(event.target as Node)) {
      this.isOpen = false;
    }
  }

  get filteredOptions(): any[] {
    if (!this.searchTerm) return this.options;
    const term = this.searchTerm.toLowerCase();
    return this.options.filter(opt => 
      this.getOptionLabel(opt).toLowerCase().includes(term)
    );
  }

  // ControlValueAccessor methods
  writeValue(value: any): void {
    this.innerValue = value;
    this.updateSearchTermFromValue(value);
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState?(isDisabled: boolean): void {
    // Implement if necessary
  }

  private updateSearchTermFromValue(value: any) {
    if (value !== undefined && value !== null && value !== '') {
      const selectedOpt = this.options.find(opt => String(this.getOptionValue(opt)) === String(value));
      if (selectedOpt) {
        this.searchTerm = this.getOptionLabel(selectedOpt);
      } else {
        this.searchTerm = '';
      }
    } else {
      this.searchTerm = '';
    }
  }

  handleInputChange(event: any) {
    const term = event.target.value;
    this.searchTerm = term;
    this.isOpen = true;
    this.highlightedIndex = -1;

    const exactMatch = this.options.find(opt => 
      this.getOptionLabel(opt).toLowerCase() === term.toLowerCase()
    );

    if (exactMatch) {
      this.emitValue(this.getOptionValue(exactMatch));
    } else if (term === '') {
      this.emitValue('');
    }
  }

  handleFocus() {
    this.isOpen = true;
  }

  handleSelect(option: any) {
    const optionValue = this.getOptionValue(option);
    const optionLabel = this.getOptionLabel(option);
    
    this.emitValue(optionValue);
    this.searchTerm = optionLabel;
    this.isOpen = false;
    
    if (this.inputRef) {
      this.inputRef.nativeElement.blur();
    }
  }

  private emitValue(value: any) {
    this.innerValue = value;
    this.onChange(value);
    this.valueChange.emit(value);
  }

  handleKeyDown(event: KeyboardEvent) {
    if (!this.isOpen && (event.key === 'ArrowDown' || event.key === 'Enter')) {
      this.isOpen = true;
      return;
    }

    const filtered = this.filteredOptions;

    if (event.key === 'ArrowDown') {
      event.preventDefault();
      this.highlightedIndex = this.highlightedIndex < filtered.length - 1 ? this.highlightedIndex + 1 : this.highlightedIndex;
    } else if (event.key === 'ArrowUp') {
      event.preventDefault();
      this.highlightedIndex = this.highlightedIndex > 0 ? this.highlightedIndex - 1 : -1;
    } else if (event.key === 'Enter' && this.highlightedIndex >= 0) {
      event.preventDefault();
      this.handleSelect(filtered[this.highlightedIndex]);
    } else if (event.key === 'Escape') {
      this.isOpen = false;
      if (this.inputRef) {
        this.inputRef.nativeElement.blur();
      }
    }
  }
}
