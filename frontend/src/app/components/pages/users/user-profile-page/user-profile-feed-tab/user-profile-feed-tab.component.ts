import { Component, OnInit, Input } from '@angular/core';
import { faComment, faHeart, faHeartCrack } from '@fortawesome/free-solid-svg-icons';
import { Pagination } from 'src/app/models/pagination.model';

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

  pagination: Pagination = {page: 1, limit: 1, maxPages: 1, maxCount: 1, hasLast: false, hasNext: false};

  isLoading: boolean = false;
  isReLoading: boolean = false;

  error: string = '';
  data: any = null;

  constructor() { }

  ngOnInit(): void {

  }

  onPageSwitch(event: any) {

  }

}
