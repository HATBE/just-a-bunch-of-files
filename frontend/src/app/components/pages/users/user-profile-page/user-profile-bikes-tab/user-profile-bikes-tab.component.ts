import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'app-user-profile-bikes-tab',
  templateUrl: './user-profile-bikes-tab.component.html',
  styleUrls: ['./user-profile-bikes-tab.component.css']
})
export class UserProfileBikesTabComponent implements OnInit {
  @Input() userId: number | null = null;

  constructor() { }

  ngOnInit(): void {
  }

}
