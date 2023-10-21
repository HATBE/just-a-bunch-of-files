import { Component, OnInit, Input } from '@angular/core';
import { faPen } from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-user-profile-bikes-tab',
  templateUrl: './user-profile-bikes-tab.component.html',
  styleUrls: ['./user-profile-bikes-tab.component.css']
})
export class UserProfileBikesTabComponent implements OnInit {
  faPen = faPen;

  yourself: boolean = false;

  @Input() userId: number | null = null;

  constructor() {
  }

  ngOnInit(): void {
    if(this.userId == localStorage.getItem('user_id')) {
      this.yourself = true;
    }
  }

}
