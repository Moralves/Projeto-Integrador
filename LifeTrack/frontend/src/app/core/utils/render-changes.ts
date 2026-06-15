import { ChangeDetectorRef } from '@angular/core';

export function renderChanges(cdr: ChangeDetectorRef): void {
  try {
    cdr.detectChanges();
  } catch {
    cdr.markForCheck();
  }
}
