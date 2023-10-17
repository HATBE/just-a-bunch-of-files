import { Component, Input, OnInit } from '@angular/core';
import { IconDefinition } from '@fortawesome/free-brands-svg-icons';
import { faHouse } from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-navbtn',
  templateUrl: './navbtn.component.html',
  styleUrls: ['./navbtn.component.css']
})
export class NavbtnComponent implements OnInit {
  @Input() name = "";
  @Input() link = "";
  @Input() noActive: boolean = false;
  @Input() icon: IconDefinition = faHouse;

  active: string = 'active';

  constructor() { }

  ngOnInit(): void {
    if(this.noActive) {
      this.active = '';
    }
  }

}
