import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'app-user-profile-about-tab',
  templateUrl: './user-profile-about-tab.component.html',
  styleUrls: ['./user-profile-about-tab.component.css']
})
export class UserProfileAboutTabComponent implements OnInit {
  @Input() userId: number | null = null;

  constructor() { }

  ngOnInit(): void {
  }

}
