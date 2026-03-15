import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="inventory-container animate-up">
      <div class="header">
        <h1>Medical Inventory</h1>
        <p>Monitor and manage hospital supplies and pharmaceutical stock</p>
      </div>

      <div class="placeholder-card">
        <span class="material-symbols-outlined">inventory_2</span>
        <h3>Inventory Management Module</h3>
        <p>This module is currently being synchronized with the central pharmacy database.</p>
        <div class="loading-bar">
          <div class="fill"></div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .inventory-container { padding: 32px; }
    .header { margin-bottom: 32px; }
    h1 { font-size: 28px; font-weight: 700; margin: 0 0 8px; }
    p { color: #94A3B8; }
    .placeholder-card { 
      background: #111827; 
      border: 1px dashed rgba(34, 163, 255, 0.3); 
      border-radius: 20px; 
      padding: 64px; 
      text-align: center;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 16px;
    }
    .material-symbols-outlined { font-size: 64px; color: #22A3FF; }
    h3 { font-size: 20px; font-weight: 600; margin: 0; }
    .loading-bar { width: 300px; height: 4px; background: rgba(255,255,255,0.05); border-radius: 2px; overflow: hidden; }
    .fill { width: 60%; height: 100%; background: #22A3FF; animation: move 2s infinite ease-in-out; }
    @keyframes move { 0% { transform: translateX(-100%); } 100% { transform: translateX(200%); } }
  `]
})
export class InventoryComponent {}
