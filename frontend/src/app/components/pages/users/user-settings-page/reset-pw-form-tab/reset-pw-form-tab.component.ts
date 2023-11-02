import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { UsersService } from 'src/app/services/users.service';

@Component({
  selector: 'app-reset-pw-form',
  templateUrl: './reset-pw-form-tab.component.html',
  styleUrls: ['./reset-pw-form-tab.component.css']
})
export class ResetPwFormComponent implements OnInit {
  isLoading: boolean = false;
  error: string = "";
  successMessage: string = "";

  changePasswordForm: FormGroup;

  constructor(
    private formBuilder: FormBuilder,
    private userService: UsersService,
  ) {
    this.changePasswordForm = this.formBuilder.group({
      oldPassword: '',
      newPassword: '',
      repeatNewPassword: ''
    });
  }

  ngOnInit(): void {
  }

  onSubmitChangePassword() {
    this.isLoading = true;
    const oldPassword = this.changePasswordForm.getRawValue().oldPassword;
    const newPassword = this.changePasswordForm.getRawValue().newPassword;
    const repeatNewPassword = this.changePasswordForm.getRawValue().repeatNewPassword;

    if(oldPassword === '' || newPassword === '' || repeatNewPassword === '') {
      this.isLoading = false;
      return;
    }

    if(newPassword != repeatNewPassword) {
      this.isLoading = false;
      this.error = 'Deine Passwörter stimmen nicht überein!'
      return;
    }

    if(oldPassword === newPassword) {
      this.isLoading = false;
      this.error = 'Dein neues Passwort darf nicht dasselbe wie das alte sein!'
      return;
    }

    this.userService.changePassword(localStorage.getItem('user_id'), oldPassword, newPassword)
    .subscribe({
      next: (data: any) => {
        this.isLoading = false;
        this.error = "";
        this.successMessage = data.message;
        this.clearForm();
      },
      error: (data: any) => {
        this.error = data.error.message || data.statusText || 'Unknown error';
        this.isLoading = false;
      }
    });
  }

  clearForm() {
    this.changePasswordForm.reset()
  }

}
