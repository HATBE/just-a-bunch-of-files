import { Component, OnInit, Input } from '@angular/core';
import { faComment, faHeart, faHeartCrack } from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-user-profile-feed-tab',
  templateUrl: './user-profile-feed-tab.component.html',
  styleUrls: ['./user-profile-feed-tab.component.css']
})
export class UserProfileFeedTabComponent implements OnInit {
  faHeart = faHeart;
  faHeartCrack = faHeartCrack;
  faComment = faComment

  @Input() userId: number | null = null;

  constructor() { }

  ngOnInit(): void {
  }

}
