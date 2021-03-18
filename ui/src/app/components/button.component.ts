import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-button',
  template: `<label class="button" [ngStyle]="{'color': color, 'background': background}">
    <span>&nbsp;{{title}}&nbsp;</span>
    <img [src]="image">
  </label>`,
  styles: ['img{ height: 100%;}', ' .button{ height: 26px;}']
})
export class ButtonComponent implements OnInit {
  @Input() image: string = '';
  @Input() title: string = '';
  @Input() color: string = 'white';
  @Input() background: string = '';

  constructor() {
  }

  ngOnInit() {
  }

}
