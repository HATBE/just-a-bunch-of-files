import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'app-user-profile-friends-tab',
  templateUrl: './user-profile-friends-tab.component.html',
  styleUrls: ['./user-profile-friends-tab.component.css']
})
export class UserProfileFriendsTabComponent implements OnInit {
  @Input() userId: number | null = null;

  constructor() { }

  ngOnInit(): void {
  }

}
