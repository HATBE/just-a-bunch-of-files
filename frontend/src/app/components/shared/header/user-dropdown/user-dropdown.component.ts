import { Component, OnInit, Input } from '@angular/core';
import { faGear, faRightFromBracket, faUser } from '@fortawesome/free-solid-svg-icons';
import { FullUserModel } from 'src/app/models/full-user.model';
import { AuthService } from 'src/app/services/auth.service';

@Component({
  selector: 'app-user-dropdown',
  templateUrl: './user-dropdown.component.html',
  styleUrls: ['./user-dropdown.component.css']
})
export class UserDropdownComponent implements OnInit {
  faGear = faGear;
  faRightFromBracket = faRightFromBracket;
  faUser = faUser;

  @Input() loggedInUser: FullUserModel | null = null;

  constructor(
    private authService: AuthService
  ) { }

  ngOnInit(): void {
  }

  logout() {
    this.authService.logout();
  }

}
