import { Injectable } from '@angular/core';
import { HttpService } from '../core/http.service';
import {
  CreateUserRequestDto,
  UserDetailResponseDto,
  UserListResponseDto
} from './user.dtos';

@Injectable({
  providedIn: 'root'
})
export class UserService extends HttpService {
  constructor() {
    super();
    this.setEntity('users');
  }

  public getAll(): Promise<UserListResponseDto[]> {
    return this.get<UserListResponseDto[]>([]);
  }

  public getById(userId: string): Promise<UserDetailResponseDto> {
    return this.get<UserDetailResponseDto>([userId]);
  }

  public create(request: CreateUserRequestDto): Promise<UserListResponseDto> {
    return this.post<UserListResponseDto, CreateUserRequestDto>([], request);
  }
}
