import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'app-user-profile-feed-tab',
  templateUrl: './user-profile-feed-tab.component.html',
  styleUrls: ['./user-profile-feed-tab.component.css']
})
export class UserProfileFeedTabComponent implements OnInit {
  @Input() userId: number | null = null;

  constructor() { }

  ngOnInit(): void {
  }

}
