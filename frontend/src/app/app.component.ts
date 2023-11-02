import { Component, OnInit } from '@angular/core';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'ridersgallery-frontend';

  constructor(
    private authService: AuthService,
  ) { }

  ngOnInit() {
    this.checkLogin();
  }

  checkLogin() {
    this.authService.checkLogin();
    setInterval(() => {
      this.authService.checkLogin();
    }, 300_000); // every five minutes
  }

}
