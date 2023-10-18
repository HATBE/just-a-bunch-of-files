import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Router } from '@angular/router';
import { Emitters } from '../emitters/emitters';
import { FullUserModel } from '../models/full-user.model';
import { Pagination } from '../models/pagination.model';
import { ListUserModel } from '../models/list-user.model';

@Injectable({
  providedIn: 'root'
})
export class UsersService {
  private apiEndpoint = `${environment.apiEndpoint}users/`;

  private authHeader = new HttpHeaders({'Authorization': `Bearer ${localStorage.getItem('authtoken')}`});

  constructor(
    private http: HttpClient,
    private router: Router
  ) { }

  getUser(id: any) {
    return this.http.get<{status: boolean, data: {user: FullUserModel}}>(this.apiEndpoint + id, {headers: this.authHeader});
  }

  getUsers(page: number = 1) {
    return this.http.get<{status: boolean, data: {users: [ListUserModel], pagination: Pagination}}>(this.apiEndpoint  + "?page=" + page, {headers: this.authHeader});
  }

}
