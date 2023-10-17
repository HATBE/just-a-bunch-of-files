import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Router } from '@angular/router';
import { Emitters } from '../emitters/emitters';
import { FullUserModel } from '../models/full-user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiEndpoint = `${environment.apiEndpoint}auth/`;

  private authHeader = new HttpHeaders({'Authorization': `Bearer ${localStorage.getItem('authtoken')}`});

  constructor(
    private http: HttpClient,
    private router: Router
  ) { }

  isLoggedIn(): boolean {
    // if authtoken isset in localstorage, login == true
    // every x minutes its checked if this token exists / if this token is valid, if not, remove from localstorage...
    if(localStorage.getItem('authtoken') !== null) {
      return true;
    }
    return false;
  }

  login(username: string, password: string) {
    return this.http.post(this.apiEndpoint + 'login', {username: username, password: password}, {});
  }

  logout(navigate: boolean = true) {
    // if user is logged in: logout, else, do nothing
    if(this.isLoggedIn()) {
      Emitters.authEmitter.emit(false);
      localStorage.removeItem('authtoken');
      localStorage.removeItem('username');
      localStorage.removeItem('user_id');

      if(navigate) {
        this.router.navigate(['/login']);
      }
    }
  }

  register(username: string, email_address: string, password: string) {
    return this.http.post(this.apiEndpoint + 'register', {username: username, email_address: email_address, password: password}, {});
  }

  checkLogin() {
    // if user is logged in, check if login is still valid, if not, logout user
    if(this.isLoggedIn()) {
      this.getLoggedInUser().subscribe({
        next: () => {},
        error: () => {
          this.logout();
        }
      });
    }
  }

  getLoggedInUser() {
    return this.http.get<{status: boolean, data: {user: FullUserModel}}>(this.apiEndpoint + "login", {headers: this.authHeader});
  }
}
