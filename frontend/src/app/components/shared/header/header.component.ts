import { Component, OnInit } from '@angular/core';
import { faCog, faGear, faMotorcycle, faRightFromBracket, faRightToBracket, faUser } from '@fortawesome/free-solid-svg-icons';
import { Emitters } from 'src/app/emitters/emitters';
import { FullUserModel } from 'src/app/models/full-user.model';
import { AuthService } from 'src/app/services/auth.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {
  faRightFromBracket = faRightFromBracket;
  faLeftFromBracket = faRightToBracket
  faRightToBracket = faRightToBracket;
  faMotorcycle = faMotorcycle;
  faCog = faCog;
  faUser = faUser;
  faGear = faGear;

  isLoggedIn: boolean = false;
  loggedInUser: FullUserModel | null = null;

  constructor(
    private authService: AuthService
    ) { }

  ngOnInit(): void {
    this.authService.getLoggedInUser().subscribe(data => {
      this.loggedInUser = data.data.user;
      this.isLoggedIn = true;
    });

    Emitters.authEmitter.subscribe((auth: boolean) => {
      this.isLoggedIn = auth;
    });
  }
}
